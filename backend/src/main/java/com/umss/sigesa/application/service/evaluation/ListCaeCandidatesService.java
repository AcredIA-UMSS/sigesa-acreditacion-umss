package com.umss.sigesa.application.service.evaluation;

import com.umss.sigesa.application.port.in.ListCaeCandidatesUseCase;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.AppUser;
import com.umss.sigesa.domain.model.Role;
import com.umss.sigesa.domain.model.UserStatus;

import java.util.List;
import java.util.UUID;

public class ListCaeCandidatesService implements ListCaeCandidatesUseCase {

    private final ProcessQueryPort processQueryPort;
    private final UserRepositoryPort userRepository;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;

    public ListCaeCandidatesService(
            ProcessQueryPort processQueryPort,
            UserRepositoryPort userRepository,
            UserProgramAssignmentRepositoryPort assignmentRepository) {
        this.processQueryPort = processQueryPort;
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public List<AppUser> list(UUID processId, UUID requesterId, List<String> roles, List<UUID> jwtProgramScope) {
        AccreditationProcess process = processQueryPort.findDetailById(processId)
                .orElseThrow(() -> new ProcessNotFoundException(processId));
        EvaluationAccessGuard.assertProcessVisible(
                processId, process.getCareerId(), requesterId, roles, jwtProgramScope, assignmentRepository);

        UUID careerId = process.getCareerId();
        return userRepository.findAllFiltered(Role.CAE_MEMBER, UserStatus.ACTIVE).stream()
                .filter(user -> assignmentRepository.findActiveByUserId(user.getId()).stream()
                        .anyMatch(assignment -> assignment.getProgramId().equals(careerId)))
                .toList();
    }
}
