package com.umss.sigesa.adapter.in.web.dto;

import java.util.UUID;

public record RejectIndicatorResponse(
        String newState,
        String observationId,
        UUID stateHistoryId
) {
}
