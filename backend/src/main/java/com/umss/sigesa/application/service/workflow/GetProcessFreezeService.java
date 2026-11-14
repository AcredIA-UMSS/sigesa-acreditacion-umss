package com.umss.sigesa.application.service.workflow;

import com.umss.sigesa.application.port.in.GetProcessFreezeUseCase;
import com.umss.sigesa.application.port.out.ProcessFreezePort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.service.evaluation.EvaluationAccessGuard;
import com.umss.sigesa.domain.exception.FreezeNotFoundException;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.ProcessFreeze;

import java.util.List;
import java.util.UUID;

public class GetProcessFreezeService implements GetProcessFreezeUseCase {

    private final ProcessQueryPort processQueryPort;
    private final ProcessFreezePort freezePort;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;

    public GetProcessFreezeService(
            ProcessQueryPort processQueryPort,
            ProcessFreezePort freezePort,
            UserProgramAssignmentRepositoryPort assignmentRepository) {
        this.processQueryPort = processQueryPort;
        this.freezePort = freezePort;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public ProcessFreeze get(UUID processId, UUID requesterId, List<String> roles, List<UUID> jwtProgramScope) {
        AccreditationProcess process = processQueryPort.findDetailById(processId)
                .orElseThrow(() -> new ProcessNotFoundException(processId));
        EvaluationAccessGuard.assertProcessVisible(
                processId, process.getCareerId(), requesterId, roles, jwtProgramScope, assignmentRepository);
        return freezePort.findByProcessId(processId)
                .orElseThrow(() -> new FreezeNotFoundException(processId));
    }
}
