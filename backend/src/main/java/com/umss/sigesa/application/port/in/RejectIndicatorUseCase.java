package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.AuthenticatedIdentity;

import java.util.UUID;

public interface RejectIndicatorUseCase {

    RejectIndicatorResult reject(UUID indicatorId, String justification, AuthenticatedIdentity identity);

    record RejectIndicatorResult(
            String newState,
            String observationId,
            UUID stateHistoryId
    ) {
    }
}
