package com.umss.sigesa.adapter.out.persistance.mapper;

import com.umss.sigesa.adapter.out.persistance.entity.MethodologicalStageJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.StageDeliverableJpaEntity;
import com.umss.sigesa.domain.model.MethodologicalStage;
import com.umss.sigesa.domain.model.MethodologicalStageCode;
import com.umss.sigesa.domain.model.MethodologicalStageStatus;
import com.umss.sigesa.domain.model.StageDeliverable;
import com.umss.sigesa.domain.model.StageDeliverableApprovalStatus;
import com.umss.sigesa.domain.model.StageDeliverableCode;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class MethodologicalStagePersistenceMapper {

    public MethodologicalStage toDomain(MethodologicalStageJpaEntity entity) {
        List<StageDeliverable> deliverables = entity.getDeliverables().stream()
                .sorted(Comparator.comparing(StageDeliverableJpaEntity::getDeliverableCode))
                .map(this::toDeliverableDomain)
                .toList();
        return MethodologicalStage.builder()
                .id(entity.getId())
                .processId(entity.getProcess().getId())
                .order(entity.getOrder())
                .code(MethodologicalStageCode.valueOf(entity.getCode()))
                .status(MethodologicalStageStatus.valueOf(entity.getStatus()))
                .startedAt(entity.getStartedAt())
                .closedAt(entity.getClosedAt())
                .deliverables(deliverables)
                .build();
    }

    public StageDeliverable toDeliverableDomain(StageDeliverableJpaEntity entity) {
        return StageDeliverable.builder()
                .id(entity.getId())
                .stageId(entity.getStage().getId())
                .deliverableCode(StageDeliverableCode.valueOf(entity.getDeliverableCode()))
                .approvalStatus(StageDeliverableApprovalStatus.valueOf(entity.getApprovalStatus()))
                .approvedBy(entity.getApprovedBy())
                .technicalObservations(entity.getTechnicalObservations())
                .approvedAt(entity.getApprovedAt())
                .build();
    }
}
