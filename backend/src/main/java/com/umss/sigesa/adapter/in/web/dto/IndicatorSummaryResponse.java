package com.umss.sigesa.adapter.in.web.dto;

import java.util.UUID;

public record IndicatorSummaryResponse(
        UUID id,
        String code,
        String title,
        UUID programId,
        int phaseId,
        UUID criterionId,
        String currentState
) {
}
