package com.umss.sigesa.adapter.out.evidence;

import com.umss.sigesa.application.port.out.NotificationOutboxPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class NoOpNotificationOutboxAdapter implements NotificationOutboxPort {

    private static final Logger log = LoggerFactory.getLogger(NoOpNotificationOutboxAdapter.class);

    @Override
    public void enqueueEvidenceUploaded(UUID indicatorId, UUID evidenceId, UUID programId) {
        enqueue("EvidenceUploaded", programId, Map.of(
                "indicatorId", indicatorId.toString(),
                "evidenceId", evidenceId.toString(),
                "programId", programId.toString()
        ));
    }

    @Override
    public void enqueue(String eventType, UUID recipientId, Map<String, String> payload) {
        log.info("NOTIFICATION_OUTBOX eventType={} recipientId={} payload={}", eventType, recipientId, payload);
    }
}
