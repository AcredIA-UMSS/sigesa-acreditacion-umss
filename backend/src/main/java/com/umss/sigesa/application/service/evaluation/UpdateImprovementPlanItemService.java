package com.umss.sigesa.application.service.evaluation;

import com.umss.sigesa.application.port.in.UpdateImprovementPlanItemUseCase;
import com.umss.sigesa.application.port.out.ImprovementPlanItemPort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.service.workflow.ProcessFreezeGuard;
import com.umss.sigesa.domain.exception.ImprovementItemNotFoundException;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.ImprovementPlanItem;
import com.umss.sigesa.domain.model.UpdateImprovementPlanItemCommand;

import java.time.LocalDateTime;

public class UpdateImprovementPlanItemService implements UpdateImprovementPlanItemUseCase {

    private final ProcessQueryPort processQueryPort;
    private final ImprovementPlanItemPort planItemPort;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;
    private final ProcessFreezeGuard freezeGuard;

    public UpdateImprovementPlanItemService(
            ProcessQueryPort processQueryPort,
            ImprovementPlanItemPort planItemPort,
            UserProgramAssignmentRepositoryPort assignmentRepository,
            ProcessFreezeGuard freezeGuard) {
        this.processQueryPort = processQueryPort;
        this.planItemPort = planItemPort;
        this.assignmentRepository = assignmentRepository;
        this.freezeGuard = freezeGuard;
    }

    @Override
    public ImprovementPlanItem update(UpdateImprovementPlanItemCommand command) {
        AccreditationProcess process = processQueryPort.findDetailById(command.processId())
                .orElseThrow(() -> new ProcessNotFoundException(command.processId()));
        EvaluationAccessGuard.assertProcessVisible(
                command.processId(),
                process.getCareerId(),
                command.requesterId(),
                command.roles(),
                command.jwtProgramScope(),
                assignmentRepository);
        freezeGuard.assertPlanWritable(command.processId());

        ImprovementPlanItem item = planItemPort.findByIdAndProcessId(command.itemId(), command.processId())
                .orElseThrow(() -> new ImprovementItemNotFoundException(command.itemId()));
        item.applyUpdate(
                command.objective(),
                command.activity(),
                command.responsibleId(),
                command.deadline(),
                command.progress(),
                command.justification(),
                command.progressEvidenceAssetId(),
                LocalDateTime.now());
        return planItemPort.save(item);
    }
}
