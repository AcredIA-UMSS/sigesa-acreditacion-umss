package com.umss.sigesa.adapter.in.web.dto;

import com.umss.sigesa.application.model.notification.NotificationItem;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponseDto(
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
) {
    public static NotificationResponseDto from(NotificationItem item) {
        return new NotificationResponseDto(
                item.id(),
                item.eventType(),
                item.recipientId(),
                item.recipientEmail(),
                item.subject(),
                item.body(),
                item.deepLink(),
                item.status(),
                item.createdAt(),
                item.readAt()
        );
    }
}
