package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.StageGateResult;

import java.util.UUID;

public interface EvaluateStageGateUseCase {

    StageGateResult evaluate(UUID processId, UUID stageId, UUID actorId, String actorRole);
}
