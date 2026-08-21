package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.IndicatorState;
import java.util.UUID;

public interface ApproveIndicatorUseCase {
    ApproveResult approve(UUID indicatorId, UUID actorId, com.umss.sigesa.domain.model.Role actorRole);

    record ApproveResult(
            IndicatorState newState,
            UUID stateHistoryId,
            String event
    ) {}
}
