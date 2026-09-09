package com.umss.sigesa.application.service.workflow;

import com.umss.sigesa.application.port.out.MethodologicalStagePort;
import com.umss.sigesa.domain.model.MethodologicalStage;
import com.umss.sigesa.domain.model.MethodologicalStageCode;
import com.umss.sigesa.domain.model.MethodologicalStageStatus;
import com.umss.sigesa.domain.model.StageDeliverable;
import com.umss.sigesa.domain.model.StageDeliverableApprovalStatus;
import com.umss.sigesa.domain.model.StageDeliverableCode;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class MethodologicalStageBootstrapper {

    private final MethodologicalStagePort stagePort;

    public MethodologicalStageBootstrapper(MethodologicalStagePort stagePort) {
        this.stagePort = stagePort;
    }

    public void bootstrapForProcess(UUID processId) {
        List<MethodologicalStage> existing = stagePort.findByProcessIdOrderByOrder(processId);
        if (!existing.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<MethodologicalStage> stages = new ArrayList<>();

        for (MethodologicalStageCode code : MethodologicalStageCode.canonicalOrder()) {
            MethodologicalStageStatus status = code == MethodologicalStageCode.PREPARATORY
                    ? MethodologicalStageStatus.IN_PROGRESS
                    : MethodologicalStageStatus.PENDING;

            List<StageDeliverable> deliverables = StageDeliverableCode.forStage(code).stream()
                    .map(deliverableCode -> StageDeliverable.builder()
                            .deliverableCode(deliverableCode)
                            .approvalStatus(StageDeliverableApprovalStatus.PENDING)
                            .build())
                    .toList();

            stages.add(MethodologicalStage.builder()
                    .processId(processId)
                    .order(code.order())
                    .code(code)
                    .status(status)
                    .startedAt(code == MethodologicalStageCode.PREPARATORY ? now : null)
                    .deliverables(deliverables)
                    .build());
        }

        stagePort.saveStages(stages);
    }
}
