package com.umss.sigesa.adapter.in.web.dto;

import java.util.UUID;

public record ApproveIndicatorResponse(
        String newState,
        UUID stateHistoryId,
        String event
) {
}
