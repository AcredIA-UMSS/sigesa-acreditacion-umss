package com.umss.sigesa.application.service.evaluation;

import com.umss.sigesa.application.port.in.ListImprovementPlanUseCase;
import com.umss.sigesa.application.port.out.ImprovementPlanItemPort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.ImprovementPlanItem;

import java.util.List;
import java.util.UUID;

public class ListImprovementPlanService implements ListImprovementPlanUseCase {

    private final ProcessQueryPort processQueryPort;
    private final ImprovementPlanItemPort planItemPort;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;

    public ListImprovementPlanService(
            ProcessQueryPort processQueryPort,
            ImprovementPlanItemPort planItemPort,
            UserProgramAssignmentRepositoryPort assignmentRepository) {
        this.processQueryPort = processQueryPort;
        this.planItemPort = planItemPort;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public List<ImprovementPlanItem> list(
            UUID processId,
            UUID requesterId,
            List<String> roles,
            List<UUID> jwtProgramScope) {
        AccreditationProcess process = processQueryPort.findDetailById(processId)
                .orElseThrow(() -> new ProcessNotFoundException(processId));
        EvaluationAccessGuard.assertProcessVisible(
                processId,
                process.getCareerId(),
                requesterId,
                roles,
                jwtProgramScope,
                assignmentRepository);
        return planItemPort.findByProcessId(processId);
    }
}
