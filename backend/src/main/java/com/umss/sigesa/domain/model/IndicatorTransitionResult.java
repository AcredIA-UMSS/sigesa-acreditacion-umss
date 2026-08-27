package com.umss.sigesa.domain.model;

import java.util.UUID;

public record IndicatorTransitionResult(
        UUID indicatorId,
        IndicatorState previousState,
        IndicatorState newState,
        UUID stateHistoryId
) {
}
