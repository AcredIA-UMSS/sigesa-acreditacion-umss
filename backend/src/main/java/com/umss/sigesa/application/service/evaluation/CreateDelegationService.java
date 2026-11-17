package com.umss.sigesa.application.service.evaluation;

import com.umss.sigesa.application.port.in.CreateDelegationUseCase;
import com.umss.sigesa.application.port.out.IndicatorDelegationPort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.domain.exception.InvalidCaeUserException;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.exception.ValidationException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.AppUser;
import com.umss.sigesa.domain.model.IndicatorDelegation;
import com.umss.sigesa.domain.model.Role;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class CreateDelegationService implements CreateDelegationUseCase {

    private final ProcessQueryPort processQueryPort;
    private final IndicatorDelegationPort delegationPort;
    private final UserRepositoryPort userRepository;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;

    public CreateDelegationService(
            ProcessQueryPort processQueryPort,
            IndicatorDelegationPort delegationPort,
            UserRepositoryPort userRepository,
            UserProgramAssignmentRepositoryPort assignmentRepository) {
        this.processQueryPort = processQueryPort;
        this.delegationPort = delegationPort;
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public IndicatorDelegation create(
            UUID processId,
            UUID userId,
            UUID level1Id,
            UUID level2Id,
            UUID indicatorId,
            UUID requesterId,
            List<String> roles,
            List<UUID> jwtProgramScope) {
        AccreditationProcess process = processQueryPort.findDetailById(processId)
                .orElseThrow(() -> new ProcessNotFoundException(processId));
        EvaluationAccessGuard.assertProcessVisible(
                processId, process.getCareerId(), requesterId, roles, jwtProgramScope, assignmentRepository);

        if (userId == null) {
            throw new ValidationException("userId es obligatorio.");
        }
        AppUser target = userRepository.findById(userId).orElseThrow(InvalidCaeUserException::new);
        if (target.getRole() != Role.CAE_MEMBER) {
            throw new InvalidCaeUserException();
        }

        return delegationPort.save(IndicatorDelegation.create(
                processId, userId, level1Id, level2Id, indicatorId, LocalDateTime.now()));
    }
}
