package com.umss.sigesa.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record IndicatorStateHistoryEntry(
        UUID id,
        UUID indicatorId,
        IndicatorState previousState,
        IndicatorState newState,
        UUID actorId,
        Role actorRole,
        LocalDateTime createdAt
) {
}
