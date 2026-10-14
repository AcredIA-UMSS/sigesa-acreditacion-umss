package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.GeneratedStageDeliverable;

import java.util.List;
import java.util.UUID;

public interface GenerateStageDeliverableDocumentUseCase {

    GeneratedStageDeliverable generate(
            UUID processId,
            UUID stageId,
            UUID deliverableId,
            UUID requesterId,
            List<String> roles,
            List<UUID> jwtProgramScope);
}
