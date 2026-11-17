package com.umss.sigesa.domain.model.observation;

import java.time.Instant;
import java.util.UUID;

public record ObservationMessage(
        UUID id,
        UUID authorId,
        String authorRole,
        String body,
        Instant createdAt
) {
}
