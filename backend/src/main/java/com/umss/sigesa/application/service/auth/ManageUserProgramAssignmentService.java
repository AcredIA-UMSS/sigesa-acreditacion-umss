package com.umss.sigesa.application.service.auth;

import com.umss.sigesa.application.port.in.ManageUserProgramAssignmentUseCase;
import com.umss.sigesa.application.port.out.ProgramCatalogPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.domain.exception.InvalidScopeException;
import com.umss.sigesa.domain.exception.UserNotFoundException;
import com.umss.sigesa.domain.model.AppUser;
import com.umss.sigesa.domain.model.Role;
import com.umss.sigesa.domain.model.UserProgramAssignment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ManageUserProgramAssignmentService implements ManageUserProgramAssignmentUseCase {

    private final UserRepositoryPort userRepository;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;
    private final ProgramCatalogPort programCatalogPort;

    public ManageUserProgramAssignmentService(UserRepositoryPort userRepository,
                                              UserProgramAssignmentRepositoryPort assignmentRepository,
                                              ProgramCatalogPort programCatalogPort) {
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
        this.programCatalogPort = programCatalogPort;
    }

    @Override
    public AssignmentResult assign(AssignCommand command) {
        if (command == null || command.userId() == null || command.programId() == null) {
            throw new InvalidScopeException("userId y programId son obligatorios.");
        }

        String action = command.action() == null ? "UPDATE" : command.action().trim().toUpperCase(Locale.ROOT);
        if (!"CREATE".equals(action) && !"UPDATE".equals(action)) {
            throw new InvalidScopeException("La acción debe ser CREATE o UPDATE.");
        }

        AppUser user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));

        if (user.getRole() != Role.CC && user.getRole() != Role.EE) {
            throw new InvalidScopeException(
                    "Solo usuarios [CC] o [EE] pueden tener asignación de carrera.");
        }

        if (programCatalogPort.findById(command.programId()).isEmpty()) {
            throw new InvalidScopeException("El programa indicado no existe.");
        }

        List<UserProgramAssignment> active = assignmentRepository.findActiveByUserId(user.getId());
        int revokedCount = 0;

        if ("CREATE".equals(action)) {
            boolean alreadyAssigned = active.stream()
                    .anyMatch(a -> a.getProgramId().equals(command.programId()));
            if (alreadyAssigned) {
                throw new InvalidScopeException("El usuario ya tiene asignación activa a ese programa.");
            }
            // Mínimo privilegio: una sola carrera activa — revocar otras antes de crear.
            if (!active.isEmpty()) {
                assignmentRepository.revokeAllActiveByUserId(user.getId());
                revokedCount = active.size();
            }
        } else {
            if (!active.isEmpty()) {
                assignmentRepository.revokeAllActiveByUserId(user.getId());
                revokedCount = active.size();
            }
        }

        UserProgramAssignment assignment = new UserProgramAssignment(
                UUID.randomUUID(),
                user.getId(),
                command.programId(),
                LocalDateTime.now(),
                null
        );
        assignmentRepository.save(assignment);

        return new AssignmentResult(user.getId(), command.programId(), action, revokedCount);
    }
}
