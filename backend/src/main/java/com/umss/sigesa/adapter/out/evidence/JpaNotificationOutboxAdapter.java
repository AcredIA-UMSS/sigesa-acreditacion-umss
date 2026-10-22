package com.umss.sigesa.adapter.out.evidence;

import com.umss.sigesa.adapter.out.persistance.NotificationOutboxJpaRepository;
import com.umss.sigesa.adapter.out.persistance.entity.NotificationOutboxJpaEntity;
import com.umss.sigesa.application.port.out.NotificationOutboxPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
@Primary
public class JpaNotificationOutboxAdapter implements NotificationOutboxPort {

    private static final Logger log = LoggerFactory.getLogger(JpaNotificationOutboxAdapter.class);

    private final NotificationOutboxJpaRepository outboxRepository;

    public JpaNotificationOutboxAdapter(NotificationOutboxJpaRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void enqueueEvidenceUploaded(UUID indicatorId, UUID evidenceId, UUID programId) {
        String deepLink = "/evaluation/program/" + programId + "/indicator/" + indicatorId;
        String subject = "Nueva evidencia registrada - Indicador " + indicatorId;
        String body = String.format("Se ha subido la evidencia %s para el indicador %s en el programa %s.",
                evidenceId, indicatorId, programId);

        NotificationOutboxJpaEntity entity = NotificationOutboxJpaEntity.builder()
                .id(UUID.randomUUID())
                .eventType("EvidenceUploaded")
                .recipientId(programId)
                .recipientEmail("coordinacion-" + programId + "@umss.edu.bo")
                .subject(subject)
                .body(body)
                .deepLink(deepLink)
                .status("PENDING")
                .retryCount(0)
                .maxRetries(5)
                .createdAt(LocalDateTime.now())
                .build();

        outboxRepository.save(entity);
        log.info("NOTIFICATION_OUTBOX_ENQUEUED id={} eventType=EvidenceUploaded indicatorId={}",
                entity.getId(), indicatorId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void enqueue(String eventType, UUID recipientId, Map<String, String> payload) {
        String indicatorId = payload.getOrDefault("indicatorId", "");
        String observationId = payload.getOrDefault("observationId", "");
        String newState = payload.getOrDefault("newState", "");

        String deepLink = "/evaluation/indicator/" + indicatorId;
        if (!observationId.isBlank()) {
            deepLink += "?observationId=" + observationId;
        }

        String subject = buildSubject(eventType, indicatorId, newState);
        String body = buildBody(eventType, indicatorId, newState, payload);

        NotificationOutboxJpaEntity entity = NotificationOutboxJpaEntity.builder()
                .id(UUID.randomUUID())
                .eventType(eventType)
                .recipientId(recipientId)
                .recipientEmail("notificaciones-" + recipientId + "@umss.edu.bo")
                .subject(subject)
                .body(body)
                .deepLink(deepLink)
                .status("PENDING")
                .retryCount(0)
                .maxRetries(5)
                .createdAt(LocalDateTime.now())
                .build();

        outboxRepository.save(entity);
        log.info("NOTIFICATION_OUTBOX_ENQUEUED id={} eventType={} recipientId={}",
                entity.getId(), eventType, recipientId);
    }

    private String buildSubject(String eventType, String indicatorId, String newState) {
        return switch (eventType) {
            case "IndicatorRejected" -> "Atención: Indicador observado (" + indicatorId + ")";
            case "IndicatorApproved" -> "Confirmación: Indicador aprobado (" + indicatorId + ")";
            case "Level1Completed" -> "Hito alcanzado: Dimensión completada";
            default -> "Notificación del sistema SIGESA: " + eventType;
        };
    }

    private String buildBody(String eventType, String indicatorId, String newState, Map<String, String> payload) {
        StringBuilder sb = new StringBuilder();
        sb.append("Evento: ").append(eventType).append("\n");
        if (!indicatorId.isBlank()) {
            sb.append("Indicador: ").append(indicatorId).append("\n");
        }
        if (!newState.isBlank()) {
            sb.append("Nuevo estado: ").append(newState).append("\n");
        }
        payload.forEach((k, v) -> {
            if (!k.equals("indicatorId") && !k.equals("newState")) {
                sb.append(k).append(": ").append(v).append("\n");
            }
        });
        return sb.toString();
    }
}
