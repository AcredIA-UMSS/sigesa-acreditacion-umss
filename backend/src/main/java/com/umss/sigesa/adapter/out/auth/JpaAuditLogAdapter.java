package com.umss.sigesa.adapter.out.auth;

import com.umss.sigesa.adapter.out.persistance.AuditLogJpaRepository;
import com.umss.sigesa.adapter.out.persistance.entity.AuditLogJpaEntity;
import com.umss.sigesa.application.port.out.AuditLogPort;
import com.umss.sigesa.application.port.out.AuditLogQueryPort;
import com.umss.sigesa.config.security.SecretSanitizer;
import com.umss.sigesa.domain.model.AuditLogEntry;
import com.umss.sigesa.domain.model.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Component
@Primary
public class JpaAuditLogAdapter implements AuditLogPort, AuditLogQueryPort {

    private static final Logger log = LoggerFactory.getLogger(JpaAuditLogAdapter.class);
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final AuditLogJpaRepository auditLogRepository;
    private final com.umss.sigesa.application.port.out.UserRepositoryPort userRepositoryPort;

    public JpaAuditLogAdapter(AuditLogJpaRepository auditLogRepository,
                             com.umss.sigesa.application.port.out.UserRepositoryPort userRepositoryPort) {
        this.auditLogRepository = auditLogRepository;
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    @Transactional
    public void logLogin(UUID userId, Email email) {
        saveEntry(userId, email != null ? email.value() : "unknown", "USER",
                "LOGIN", "USER", userId.toString(), "Inicio de sesión exitoso", null);
    }

    @Override
    @Transactional
    public void logUserRegistered(UUID userId, Email email) {
        saveEntry(null, email != null ? email.value() : "system", "SYSTEM",
                "USER_REGISTERED", "USER", userId.toString(), "Usuario registrado", null);
    }

    @Override
    @Transactional
    public void logUserDeactivated(UUID userId, Email email) {
        saveEntry(null, email != null ? email.value() : "system", "JD",
                "USER_DEACTIVATED", "USER", userId.toString(), "Usuario desactivado", null);
    }

    @Override
    @Transactional
    public void logUserActivated(UUID userId, Email email) {
        saveEntry(null, email != null ? email.value() : "system", "JD",
                "USER_ACTIVATED", "USER", userId.toString(), "Usuario reactivado", null);
    }

    @Override
    @Transactional
    public void logReportRequested(UUID requesterId, UUID jobId) {
        String email = resolveUserEmail(requesterId);
        String role = resolveUserRole(requesterId, "JD");
        saveEntry(requesterId, email, role,
                "REPORT_REQUESTED", "REPORT_JOB", jobId.toString(), "Solicitud de exportación de reporte", null);
    }

    @Override
    @Transactional
    public void logEvidenceUploaded(UUID uploadedBy, UUID evidenceId, UUID indicatorId) {
        String email = resolveUserEmail(uploadedBy);
        String role = resolveUserRole(uploadedBy, "CC");
        saveEntry(uploadedBy, email, role,
                "EVIDENCE_UPLOADED", "EVIDENCE", evidenceId.toString(),
                "Carga de evidencia para indicador=" + indicatorId, null);
    }

    @Override
    @Transactional
    public void logDeleteDenied(UUID actorId, UUID evidenceId) {
        String email = resolveUserEmail(actorId);
        String role = resolveUserRole(actorId, "UNKNOWN");
        saveEntry(actorId, email, role,
                "DELETE_DENIED", "EVIDENCE", evidenceId.toString(),
                "Intento de eliminación física denegado por invariante append-only", null);
    }

    @Override
    @Transactional
    public void logEvidenceRetracted(UUID actorId, UUID evidenceId, UUID indicatorId) {
        String email = resolveUserEmail(actorId);
        String role = resolveUserRole(actorId, "CC");
        saveEntry(actorId, email, role,
                "EVIDENCE_RETRACTED", "EVIDENCE", evidenceId.toString(),
                "Retiro de evidencia no aprobada indicador=" + indicatorId, null);
    }

    private String resolveUserEmail(UUID userId) {
        if (userId == null) {
            return "system@umss.edu.bo";
        }
        return userRepositoryPort.findById(userId)
                .map(u -> u.getEmail().value())
                .orElse("user-" + userId + "@umss.edu.bo");
    }

    private String resolveUserRole(UUID userId, String defaultRole) {
        if (userId == null) {
            return "SYSTEM";
        }
        return userRepositoryPort.findById(userId)
                .map(u -> u.getRole() != null ? u.getRole().name() : defaultRole)
                .orElse(defaultRole);
    }

    @Transactional
    public void saveEntry(UUID actorId, String actorEmail, String actorRole, String action,
                          String entityType, String entityId, String payloadDiff, String ipAddress) {
        String sanitizedPayload = SecretSanitizer.sanitizeValue("payload", payloadDiff);

        AuditLogJpaEntity entity = AuditLogJpaEntity.builder()
                .id(UUID.randomUUID())
                .actorId(actorId)
                .actorEmail(actorEmail != null ? actorEmail : "system@umss.edu.bo")
                .actorRole(actorRole != null ? actorRole : "SYSTEM")
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .payloadDiff(sanitizedPayload)
                .ipAddress(ipAddress != null ? ipAddress : "127.0.0.1")
                .createdAt(LocalDateTime.now())
                .build();

        auditLogRepository.save(entity);

        // Registro estructurado a stdout (F12-04) sin filtrar secretos
        log.info("AUDIT_APPEND_ONLY action={} actor={} entityType={} entityId={}",
                action, entity.getActorEmail(), entityType, entityId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogEntry> findAuditLogs(String actorEmail, String action, String entityType,
                                            LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return auditLogRepository.findFiltered(actorEmail, action, entityType, from, to, pageable)
                .map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public void streamAuditLogsCsv(String actorEmail, String action, String entityType,
                                  LocalDateTime from, LocalDateTime to, OutputStream outputStream) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
            writer.write("id,created_at,actor_email,actor_role,action,entity_type,entity_id,ip_address,payload_diff\n");

            List<AuditLogJpaEntity> logs = auditLogRepository.findAllFiltered(actorEmail, action, entityType, from, to);
            for (AuditLogJpaEntity logItem : logs) {
                String line = String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                        logItem.getId(),
                        logItem.getCreatedAt() != null ? logItem.getCreatedAt().format(ISO_FORMATTER) : "",
                        escapeCsv(logItem.getActorEmail()),
                        escapeCsv(logItem.getActorRole()),
                        escapeCsv(logItem.getAction()),
                        escapeCsv(logItem.getEntityType()),
                        escapeCsv(logItem.getEntityId()),
                        escapeCsv(logItem.getIpAddress()),
                        escapeCsv(logItem.getPayloadDiff()));
                writer.write(line);
            }
            writer.flush();
        } catch (IOException e) {
            log.error("Error al transmitir archivo CSV de bitácora de auditoría", e);
            throw new RuntimeException("Fallo en streaming de CSV de auditoría", e);
        }
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\"", "\"\"");
    }

    private AuditLogEntry toDomain(AuditLogJpaEntity entity) {
        return AuditLogEntry.builder()
                .id(entity.getId())
                .actorId(entity.getActorId())
                .actorEmail(entity.getActorEmail())
                .actorRole(entity.getActorRole())
                .action(entity.getAction())
                .entityType(entity.getEntityType())
                .entityId(entity.getEntityId())
                .payloadDiff(entity.getPayloadDiff())
                .ipAddress(entity.getIpAddress())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
