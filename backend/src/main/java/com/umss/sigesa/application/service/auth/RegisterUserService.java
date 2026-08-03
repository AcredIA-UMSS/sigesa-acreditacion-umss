package com.umss.sigesa.application.service.auth;

import com.umss.sigesa.application.port.in.RegisterUserUseCase;
import com.umss.sigesa.application.port.out.AuditLogPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.domain.exception.DuplicateEmailException;
import com.umss.sigesa.domain.exception.InvalidRoleException;
import com.umss.sigesa.domain.exception.InvalidScopeException;
import com.umss.sigesa.domain.model.AppUser;
import com.umss.sigesa.domain.model.Email;
import com.umss.sigesa.domain.model.Role;
import com.umss.sigesa.domain.model.UserProgramAssignment;
import com.umss.sigesa.domain.model.UserProfile;
import com.umss.sigesa.domain.model.UserStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepositoryPort userRepository;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;
    private final AuditLogPort auditLogPort;

    public RegisterUserService(UserRepositoryPort userRepository,
                               UserProgramAssignmentRepositoryPort assignmentRepository,
                               AuditLogPort auditLogPort) {
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
        this.auditLogPort = auditLogPort;
    }

    @Override
    public RegisterResult register(RegisterUserCommand command) {
        Email emailVo = Email.of(command.email());
        Role role = parseRole(command.roleName());
        validateScope(role, command.programId());

        String firstName = UserProfile.normalizeName(command.firstName(), "Nombre(s)");
        String lastName = UserProfile.normalizeName(command.lastName(), "Apellido(s)");
        String phoneNumber = UserProfile.normalizePhone(command.phoneNumber());
        UserProfile.validatePassword(command.password());

        if (userRepository.findByEmail(emailVo).isPresent()) {
            throw new DuplicateEmailException();
        }

        LocalDateTime now = LocalDateTime.now();
        AppUser user = new AppUser(
                UUID.randomUUID(),
                emailVo,
                role,
                UserStatus.INACTIVE,
                now,
                now,
                firstName,
                lastName,
                phoneNumber
        );

        AppUser saved = userRepository.save(user, command.password());

        if (requiresProgramAssignment(role) && command.programId() != null) {
            UserProgramAssignment assignment = new UserProgramAssignment(
                    UUID.randomUUID(),
                    saved.getId(),
                    command.programId(),
                    now,
                    null
            );
            assignmentRepository.save(assignment);
        }

        auditLogPort.logUserRegistered(saved.getId(), saved.getEmail());
        return new RegisterResult(saved.getId(), saved.getStatus());
    }

    private void validateScope(Role role, UUID programId) {
        if (requiresProgramAssignment(role) && programId == null) {
            throw new InvalidScopeException("El rol [" + role.name() + "] requiere programId.");
        }
    }

    private static boolean requiresProgramAssignment(Role role) {
        return role == Role.CC || role == Role.EE;
    }

    private Role parseRole(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            throw new InvalidRoleException("null");
        }
        try {
            return Role.valueOf(roleName.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidRoleException(roleName);
        }
    }
}
