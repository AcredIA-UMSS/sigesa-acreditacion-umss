package com.umss.sigesa.adapter.out.evidence;

import com.umss.sigesa.adapter.out.persistance.NotificationOutboxJpaRepository;
import com.umss.sigesa.adapter.out.persistance.entity.NotificationOutboxJpaEntity;
import com.umss.sigesa.application.port.in.EnqueueDomainNotificationUseCase;
import com.umss.sigesa.application.port.out.NotificationOutboxPort;
import com.umss.sigesa.application.port.out.NotificationOutboxWritePort;
import com.umss.sigesa.domain.model.notification.DomainNotificationCommand;
import com.umss.sigesa.domain.model.notification.NotificationChannel;
import com.umss.sigesa.domain.model.notification.NotificationEventType;
import com.umss.sigesa.domain.model.notification.NotificationRecipient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Dual role: {@link NotificationOutboxWritePort} for dual-channel enqueue, and legacy
 * {@link NotificationOutboxPort} facade that delegates to {@link EnqueueDomainNotificationUseCase}
 * without synthesizing emails.
 */
@Component
@Primary
public class JpaNotificationOutboxAdapter implements NotificationOutboxPort, NotificationOutboxWritePort {

    private static final Logger log = LoggerFactory.getLogger(JpaNotificationOutboxAdapter.class);

    private final NotificationOutboxJpaRepository outboxRepository;
    private final EnqueueDomainNotificationUseCase enqueueDomainNotificationUseCase;

    public JpaNotificationOutboxAdapter(
            NotificationOutboxJpaRepository outboxRepository,
            @Lazy EnqueueDomainNotificationUseCase enqueueDomainNotificationUseCase) {
        this.outboxRepository = outboxRepository;
        this.enqueueDomainNotificationUseCase = enqueueDomainNotificationUseCase;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void enqueueEvidenceUploaded(UUID indicatorId, UUID evidenceId, UUID programId) {
        enqueueDomainNotificationUseCase.enqueue(new DomainNotificationCommand(
                NotificationEventType.EvidenceUploaded,
                indicatorId,
                programId,
                null,
                Map.of(
                        "indicatorId", indicatorId.toString(),
                        "evidenceId", evidenceId.toString(),
                        "programId", programId != null ? programId.toString() : "")));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void enqueue(String eventType, UUID recipientId, Map<String, String> payload) {
        NotificationEventType type = NotificationEventType.fromWire(eventType);
        UUID aggregateId = parseUuid(payload.get("indicatorId"))
                .or(() -> parseUuid(payload.get("level1Id")))
                .or(() -> parseUuid(payload.get("processId")))
                .orElse(recipientId != null ? recipientId : UUID.randomUUID());
        UUID programId = parseUuid(payload.get("programId")).orElse(null);
        UUID processId = parseUuid(payload.get("processId")).orElse(null);

        // Legacy callers passed careerId as recipientId — treat as programId when programId absent.
        if (programId == null && recipientId != null
                && (type == NotificationEventType.IndicatorApproved
                || type == NotificationEventType.IndicatorRejected
                || type == NotificationEventType.Level1Completed)) {
            programId = recipientId;
        }

        enqueueDomainNotificationUseCase.enqueue(new DomainNotificationCommand(
                type,
                aggregateId,
                programId,
                processId,
                payload == null ? Map.of() : payload));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Optional<UUID> saveIfAbsent(
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
            String initialStatus) {
        if (outboxRepository.existsByIdempotencyKey(idempotencyKey)) {
            return Optional.empty();
        }

        UUID id = UUID.randomUUID();
        NotificationOutboxJpaEntity entity = NotificationOutboxJpaEntity.builder()
                .id(id)
                .eventType(eventType.name())
                .recipientId(recipient.userId())
                .recipientEmail(recipient.email())
                .recipientRole(recipient.role() != null ? recipient.role().name() : null)
                .programId(programId)
                .processId(processId)
                .channel(channel.name())
                .idempotencyKey(idempotencyKey)
                .payloadJson(payloadJson)
                .subject(subject)
                .body(body)
                .deepLink(deepLink)
                .status(initialStatus != null ? initialStatus : "PENDING")
                .retryCount(0)
                .maxRetries(5)
                .createdAt(LocalDateTime.now())
                .sentAt("SENT".equals(initialStatus) ? LocalDateTime.now() : null)
                .build();

        outboxRepository.save(entity);
        log.info("NOTIFICATION_OUTBOX_ENQUEUED id={} eventType={} channel={} recipientUserId={}",
                id, eventType, channel, recipient.userId());
        return Optional.of(id);
    }

    @Override
    public boolean existsByIdempotencyKey(String idempotencyKey) {
        return outboxRepository.existsByIdempotencyKey(idempotencyKey);
    }

    private static Optional<UUID> parseUuid(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(raw.trim()));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
