package com.umss.sigesa.adapter.out.notification;

import com.umss.sigesa.adapter.out.persistance.NotificationOutboxJpaRepository;
import com.umss.sigesa.adapter.out.persistance.entity.NotificationOutboxJpaEntity;
import com.umss.sigesa.application.port.out.NotificationSenderPort;
import com.umss.sigesa.domain.model.notification.NotificationChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class NotificationOutboxWorker {

    private static final Logger log = LoggerFactory.getLogger(NotificationOutboxWorker.class);
    private static final int DEFAULT_BATCH_SIZE = 20;

    private final NotificationOutboxJpaRepository outboxRepository;
    private final NotificationSenderPort notificationSender;
    private final TransactionTemplate requiresNewTx;

    public NotificationOutboxWorker(
            NotificationOutboxJpaRepository outboxRepository,
            NotificationSenderPort notificationSender,
            org.springframework.transaction.PlatformTransactionManager transactionManager) {
        this.outboxRepository = outboxRepository;
        this.notificationSender = notificationSender;
        this.requiresNewTx = new TransactionTemplate(transactionManager);
        this.requiresNewTx.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
    }

    @Scheduled(fixedDelayString = "${sigesa.outbox.poll-interval-ms:5000}")
    public void scheduleProcessing() {
        processPendingBatch(DEFAULT_BATCH_SIZE);
    }

    public int processPendingBatch(int batchSize) {
        List<UUID> claimedIds = claimBatch(batchSize);
        if (claimedIds.isEmpty()) {
            return 0;
        }

        log.debug("Procesando lote outbox: {} notificaciones PENDING→PROCESSING.", claimedIds.size());
        int processedCount = 0;
        for (UUID id : claimedIds) {
            try {
                dispatchClaimed(id);
                processedCount++;
            } catch (Exception e) {
                log.error("Fallo inesperado al procesar notificación outbox id={}", id, e);
            }
        }
        return processedCount;
    }

    List<UUID> claimBatch(int batchSize) {
        return requiresNewTx.execute(status -> {
            List<NotificationOutboxJpaEntity> batch = outboxRepository.findPendingBatch(
                    "PENDING", LocalDateTime.now(), PageRequest.of(0, batchSize));
            List<UUID> claimed = new ArrayList<>(batch.size());
            for (NotificationOutboxJpaEntity entity : batch) {
                if (!NotificationChannel.EMAIL.name().equals(entity.getChannel())
                        && entity.getChannel() != null
                        && !entity.getChannel().isBlank()) {
                    continue;
                }
                entity.setStatus("PROCESSING");
                outboxRepository.save(entity);
                claimed.add(entity.getId());
            }
            return claimed;
        });
    }

    void dispatchClaimed(UUID notificationId) {
        NotificationOutboxJpaEntity snapshot = outboxRepository.findById(notificationId).orElse(null);
        if (snapshot == null || !"PROCESSING".equals(snapshot.getStatus())) {
            return;
        }

        try {
            notificationSender.send(
                    snapshot.getId(),
                    snapshot.getRecipientEmail(),
                    snapshot.getSubject(),
                    snapshot.getBody(),
                    snapshot.getDeepLink());
            markSent(notificationId);
            log.info("Notificación outbox enviada exitosamente. id={}", notificationId);
        } catch (Exception ex) {
            markFailure(notificationId, ex);
        }
    }

    /**
     * Legacy entry used by unit tests that still call dispatch on PENDING rows.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void dispatchNotification(UUID notificationId) {
        NotificationOutboxJpaEntity entity = outboxRepository.findById(notificationId).orElse(null);
        if (entity == null) {
            return;
        }
        if ("PENDING".equals(entity.getStatus())) {
            entity.setStatus("PROCESSING");
            outboxRepository.save(entity);
        }
        if (!"PROCESSING".equals(entity.getStatus())) {
            return;
        }
        try {
            notificationSender.send(
                    entity.getId(),
                    entity.getRecipientEmail(),
                    entity.getSubject(),
                    entity.getBody(),
                    entity.getDeepLink());
            entity.setStatus("SENT");
            entity.setSentAt(LocalDateTime.now());
            entity.setErrorDetail(null);
            outboxRepository.save(entity);
        } catch (Exception ex) {
            applyFailure(entity, ex);
            outboxRepository.save(entity);
        }
    }

    private void markSent(UUID notificationId) {
        requiresNewTx.executeWithoutResult(status -> {
            NotificationOutboxJpaEntity entity = outboxRepository.findById(notificationId).orElse(null);
            if (entity == null) {
                return;
            }
            entity.setStatus("SENT");
            entity.setSentAt(LocalDateTime.now());
            entity.setErrorDetail(null);
            outboxRepository.save(entity);
        });
    }

    private void markFailure(UUID notificationId, Exception ex) {
        requiresNewTx.executeWithoutResult(status -> {
            NotificationOutboxJpaEntity entity = outboxRepository.findById(notificationId).orElse(null);
            if (entity == null) {
                return;
            }
            applyFailure(entity, ex);
            outboxRepository.save(entity);
        });
    }

    private void applyFailure(NotificationOutboxJpaEntity entity, Exception ex) {
        int newRetryCount = entity.getRetryCount() + 1;
        entity.setRetryCount(newRetryCount);
        entity.setErrorDetail(sanitizeError(ex.getMessage()));

        if (newRetryCount >= entity.getMaxRetries()) {
            entity.setStatus("FAILED");
            log.error("Notificación outbox marcada como FAILED tras {} reintentos. id={}",
                    newRetryCount, entity.getId(), ex);
        } else {
            long baseSeconds = (long) Math.pow(2, newRetryCount) * 2;
            long jitterMillis = ThreadLocalRandom.current().nextLong(200, 1500);
            LocalDateTime nextRetry = LocalDateTime.now().plusSeconds(baseSeconds).plusNanos(jitterMillis * 1_000_000);
            entity.setNextRetryAt(nextRetry);
            entity.setStatus("PENDING");
            log.warn("Reintento programado para notificación id={} (intento {}/{} en {}s)",
                    entity.getId(), newRetryCount, entity.getMaxRetries(), baseSeconds);
        }
    }

    private static String sanitizeError(String message) {
        if (message == null || message.isBlank()) {
            return "unknown";
        }
        String sanitized = message.replaceAll("[\\r\\n\\t]+", " ").trim();
        return sanitized.length() > 500 ? sanitized.substring(0, 500) : sanitized;
    }
}
