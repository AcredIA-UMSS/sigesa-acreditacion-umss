package com.umss.sigesa.domain.model.notification;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * Context for dual-channel fan-out. {@code aggregateId} is the primary entity of the event
 * (indicatorId, processId, level1Id, observationId, etc.).
 */
public record DomainNotificationCommand(
        NotificationEventType eventType,
        UUID aggregateId,
        UUID programId,
        UUID processId,
        Map<String, String> payload
) {
    public DomainNotificationCommand {
        if (eventType == null) {
            throw new IllegalArgumentException("eventType is required");
        }
        if (aggregateId == null) {
            throw new IllegalArgumentException("aggregateId is required");
        }
        payload = payload == null ? Map.of() : Collections.unmodifiableMap(payload);
    }

    public String payloadValue(String key) {
        return payload.getOrDefault(key, "");
    }
}
