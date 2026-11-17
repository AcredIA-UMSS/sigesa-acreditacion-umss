package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.StageDeliverable;

import java.util.UUID;

public interface RecordDeliverableFeedbackUseCase {

    StageDeliverable record(
            UUID processId,
            UUID stageId,
            UUID deliverableId,
            String technicalObservations,
            String actorRole);
}
