package com.umss.sigesa.adapter.in.web.dto;

import java.time.Instant;
import java.util.UUID;

public record ObservationMessageResponse(
        UUID id,
        UUID authorId,
        String authorRole,
        String body,
        Instant createdAt
) {
}
