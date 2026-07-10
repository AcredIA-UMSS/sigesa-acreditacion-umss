package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.AuthenticatedIdentity;

import java.util.UUID;

public interface ApproveIndicatorUseCase {

    ApproveIndicatorResult approve(UUID indicatorId, AuthenticatedIdentity identity);

    record ApproveIndicatorResult(
            String newState,
            UUID stateHistoryId,
            String event
    ) {
    }
}
