package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.MethodologicalStage;

import java.util.UUID;

public interface ApproveStageUseCase {

    MethodologicalStage approve(UUID processId, UUID stageId, UUID actorId, String actorRole);
}
