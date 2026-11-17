package com.umss.sigesa.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record InAppNotificationResponseDto(
        UUID id,
        String eventType,
        String title,
        String body,
        String deepLink,
        UUID programId,
        UUID processId,
        LocalDateTime readAt,
        LocalDateTime createdAt
) {
}
