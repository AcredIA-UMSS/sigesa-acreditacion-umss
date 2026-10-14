package com.umss.sigesa.application.service.evaluation;

import com.umss.sigesa.application.port.in.CreateImprovementPlanItemUseCase;
import com.umss.sigesa.application.port.out.ImprovementPlanItemPort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.service.workflow.ProcessFreezeGuard;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.CreateImprovementPlanItemCommand;
import com.umss.sigesa.domain.model.ImprovementPlanItem;

import java.time.LocalDateTime;

public class CreateImprovementPlanItemService implements CreateImprovementPlanItemUseCase {

    private final ProcessQueryPort processQueryPort;
    private final ImprovementPlanItemPort planItemPort;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;
    private final ProcessFreezeGuard freezeGuard;

    public CreateImprovementPlanItemService(
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
    public ImprovementPlanItem create(CreateImprovementPlanItemCommand command) {
        AccreditationProcess process = processQueryPort.findDetailById(command.processId())
                .orElseThrow(() -> new ProcessNotFoundException(command.processId()));
        EvaluationAccessGuard.assertProcessVisible(
                command.processId(),
                process.getCareerId(),
                command.requesterId(),
                command.roles(),
                command.jwtProgramScope(),
                assignmentRepository);
        freezeGuard.assertWritable(command.processId());

        ImprovementPlanItem item = ImprovementPlanItem.createManual(
                command.processId(),
                command.objective(),
                command.activity(),
                command.responsibleId(),
                command.deadline(),
                command.progress(),
                LocalDateTime.now());
        return planItemPort.save(item);
    }
}
