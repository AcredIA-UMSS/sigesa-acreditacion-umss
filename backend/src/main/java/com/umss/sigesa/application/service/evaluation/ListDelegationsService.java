package com.umss.sigesa.application.service.evaluation;

import com.umss.sigesa.application.port.in.ListDelegationsUseCase;
import com.umss.sigesa.application.port.out.IndicatorDelegationPort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.IndicatorDelegation;

import java.util.List;
import java.util.UUID;

public class ListDelegationsService implements ListDelegationsUseCase {

    private final ProcessQueryPort processQueryPort;
    private final IndicatorDelegationPort delegationPort;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;

    public ListDelegationsService(
            ProcessQueryPort processQueryPort,
            IndicatorDelegationPort delegationPort,
            UserProgramAssignmentRepositoryPort assignmentRepository) {
        this.processQueryPort = processQueryPort;
        this.delegationPort = delegationPort;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public List<IndicatorDelegation> list(
            UUID processId, UUID requesterId, List<String> roles, List<UUID> jwtProgramScope) {
        AccreditationProcess process = processQueryPort.findDetailById(processId)
                .orElseThrow(() -> new ProcessNotFoundException(processId));
        EvaluationAccessGuard.assertProcessVisible(
                processId, process.getCareerId(), requesterId, roles, jwtProgramScope, assignmentRepository);
        return delegationPort.findByProcessId(processId);
    }
}
