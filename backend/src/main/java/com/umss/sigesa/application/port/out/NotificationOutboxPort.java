package com.umss.sigesa.application.port.out;

import java.util.Map;
import java.util.UUID;

public interface NotificationOutboxPort {

    void enqueueEvidenceUploaded(UUID indicatorId, UUID evidenceId, UUID programId);

    void enqueue(String eventType, UUID recipientId, Map<String, String> payload);
}
