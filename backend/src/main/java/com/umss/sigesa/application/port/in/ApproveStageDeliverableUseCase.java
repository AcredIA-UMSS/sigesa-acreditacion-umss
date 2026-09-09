package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.StageDeliverable;

import java.util.UUID;

public interface ApproveStageDeliverableUseCase {

    StageDeliverable approve(UUID processId, UUID stageId, UUID deliverableId, UUID actorId, String actorRole);
}
