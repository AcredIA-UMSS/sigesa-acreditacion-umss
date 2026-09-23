package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.MethodologicalStage;
import com.umss.sigesa.domain.model.OperationalMode;
import com.umss.sigesa.domain.model.StageDeliverable;
import com.umss.sigesa.domain.model.StageDeliverableApprovalStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MethodologicalStagePort {

    List<MethodologicalStage> findByProcessIdOrderByOrder(UUID processId);

    Optional<MethodologicalStage> findByIdAndProcessId(UUID stageId, UUID processId);

    Optional<MethodologicalStage> findNextStage(UUID processId, int currentOrder);

    void saveStages(List<MethodologicalStage> stages);

    void updateStageStatus(UUID stageId, com.umss.sigesa.domain.model.MethodologicalStageStatus status);

    void markStageClosed(UUID stageId);

    void markStageStarted(UUID stageId);

    void updateProcessCurrentStage(UUID processId, UUID stageId, OperationalMode operationalMode);

    Optional<StageDeliverable> findDeliverableById(UUID deliverableId);

    void updateDeliverableApproval(
            UUID deliverableId,
            StageDeliverableApprovalStatus status,
            UUID approvedBy,
            String technicalObservations);

    void saveGateEvaluation(
            UUID stageId,
            UUID evaluatedBy,
            com.umss.sigesa.domain.model.StageGateResult gateResult,
            String rulesSnapshotJson);

    long countPrimarySurveyBatches(UUID processId);
}
