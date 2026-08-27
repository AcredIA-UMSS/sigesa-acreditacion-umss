package com.umss.sigesa.domain.model;

import java.util.UUID;

public record IndicatorWorkflowResult(
        UUID indicatorId,
        IndicatorState previousState,
        IndicatorState newState,
        UUID stateHistoryId,
        UUID observationId
) {
}
