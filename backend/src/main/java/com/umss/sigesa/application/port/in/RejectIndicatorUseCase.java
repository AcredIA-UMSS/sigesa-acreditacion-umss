package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.IndicatorState;
import java.util.UUID;

public interface RejectIndicatorUseCase {
    RejectResult reject(UUID indicatorId, String justification, UUID actorId, com.umss.sigesa.domain.model.Role actorRole);

    record RejectResult(
            IndicatorState newState,
            String observationId,
            UUID stateHistoryId
    ) {}
}
