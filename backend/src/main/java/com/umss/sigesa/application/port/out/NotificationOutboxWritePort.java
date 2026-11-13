package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.notification.NotificationChannel;
import com.umss.sigesa.domain.model.notification.NotificationEventType;
import com.umss.sigesa.domain.model.notification.NotificationRecipient;

import java.util.Optional;
import java.util.UUID;

public interface NotificationOutboxWritePort {

    /**
     * Persists an outbox row if {@code idempotencyKey} is absent.
     *
     * @return outbox id when inserted; empty when skipped due to idempotency
     */
    Optional<UUID> saveIfAbsent(
            String idempotencyKey,
            NotificationEventType eventType,
            NotificationChannel channel,
            NotificationRecipient recipient,
            UUID programId,
            UUID processId,
            String subject,
            String body,
            String deepLink,
            String payloadJson,
            String initialStatus);

    boolean existsByIdempotencyKey(String idempotencyKey);

    static String idempotencyKey(
            NotificationEventType eventType,
            String aggregateKey,
            UUID recipientUserId,
            NotificationChannel channel) {
        return eventType.name() + ":" + aggregateKey + ":" + recipientUserId + ":" + channel.name();
    }
}
