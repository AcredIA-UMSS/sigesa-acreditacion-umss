package com.umss.sigesa.application.model.notification;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationItem(
        UUID id,
        String eventType,
        UUID recipientId,
        String recipientEmail,
        String subject,
        String body,
        String deepLink,
        String status,
        LocalDateTime createdAt,
        LocalDateTime readAt
) {}
