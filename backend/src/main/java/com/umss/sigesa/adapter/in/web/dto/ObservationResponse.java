package com.umss.sigesa.adapter.in.web.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ObservationResponse(
        UUID id,
        UUID processId,
        UUID evidenceId,
        UUID indicatorId,
        String description,
        String kind,
        String priority,
        String compliance,
        String recommendation,
        UUID authorId,
        String status,
        Instant createdAt,
        Instant updatedAt,
        List<ObservationMessageResponse> messages
) {
}
