package com.umss.sigesa.adapter.out.notification;

import com.umss.sigesa.adapter.out.persistance.NotificationOutboxJpaRepository;
import com.umss.sigesa.adapter.out.persistance.entity.NotificationOutboxJpaEntity;
import com.umss.sigesa.application.port.out.NotificationSenderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class NotificationOutboxWorker {

    private static final Logger log = LoggerFactory.getLogger(NotificationOutboxWorker.class);
    private static final int DEFAULT_BATCH_SIZE = 20;

    private final NotificationOutboxJpaRepository outboxRepository;
    private final NotificationSenderPort notificationSender;

    public NotificationOutboxWorker(NotificationOutboxJpaRepository outboxRepository,
                                    NotificationSenderPort notificationSender) {
        this.outboxRepository = outboxRepository;
        this.notificationSender = notificationSender;
    }

    @Scheduled(fixedDelayString = "${sigesa.outbox.poll-interval-ms:5000}")
    public void scheduleProcessing() {
        processPendingBatch(DEFAULT_BATCH_SIZE);
    }

    public int processPendingBatch(int batchSize) {
        List<NotificationOutboxJpaEntity> batch = fetchBatch(batchSize);
        if (batch.isEmpty()) {
            return 0;
        }

        log.debug("Procesando lote outbox: {} notificaciones pendientes.", batch.size());
        int processedCount = 0;
        for (NotificationOutboxJpaEntity entity : batch) {
            try {
                dispatchNotification(entity.getId());
                processedCount++;
            } catch (Exception e) {
                log.error("Fallo inesperado al procesar notificación outbox id={}", entity.getId(), e);
            }
        }
        return processedCount;
    }

    @Transactional(readOnly = true)
    protected List<NotificationOutboxJpaEntity> fetchBatch(int batchSize) {
        return outboxRepository.findPendingBatch("PENDING", LocalDateTime.now(), PageRequest.of(0, batchSize));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void dispatchNotification(UUID notificationId) {
        NotificationOutboxJpaEntity entity = outboxRepository.findById(notificationId).orElse(null);
        if (entity == null || !"PENDING".equals(entity.getStatus())) {
            return;
        }

        try {
            // Envío físico de la notificación (SMTP / cliente HTTP)
            notificationSender.send(
                    entity.getId(),
                    entity.getRecipientEmail(),
                    entity.getSubject(),
                    entity.getBody(),
                    entity.getDeepLink()
            );

            entity.setStatus("SENT");
            entity.setSentAt(LocalDateTime.now());
            outboxRepository.save(entity);
            log.info("Notificación outbox enviada exitosamente. id={}", notificationId);
        } catch (Exception ex) {
            int newRetryCount = entity.getRetryCount() + 1;
            entity.setRetryCount(newRetryCount);

            if (newRetryCount >= entity.getMaxRetries()) {
                entity.setStatus("FAILED");
                log.error("Notificación outbox marcada como FAILED tras {} reintentos. id={}",
                        newRetryCount, notificationId, ex);
            } else {
                // Exponential Backoff con Jitter: 2^retry * 2s + random(200..1500ms)
                long baseSeconds = (long) Math.pow(2, newRetryCount) * 2;
                long jitterMillis = ThreadLocalRandom.current().nextLong(200, 1500);
                LocalDateTime nextRetry = LocalDateTime.now().plusSeconds(baseSeconds).plusNanos(jitterMillis * 1_000_000);
                entity.setNextRetryAt(nextRetry);
                log.warn("Reintento programado para notificación id={} (intento {}/{} en {}s)",
                        notificationId, newRetryCount, entity.getMaxRetries(), baseSeconds);
            }
            outboxRepository.save(entity);
        }
    }
}
