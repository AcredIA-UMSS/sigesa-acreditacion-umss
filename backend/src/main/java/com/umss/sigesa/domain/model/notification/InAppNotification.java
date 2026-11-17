package com.umss.sigesa.domain.model.notification;

import java.time.LocalDateTime;
import java.util.UUID;

public record InAppNotification(
        UUID id,
        UUID outboxId,
        UUID userId,
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
