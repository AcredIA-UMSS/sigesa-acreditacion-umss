package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.AuditLogEntity;
import com.umss.sigesa.application.port.out.AuditLogPort;
import com.umss.sigesa.application.port.out.AuditLogQueryPort;
import com.umss.sigesa.domain.model.AuditAction;
import com.umss.sigesa.domain.model.AuditActorEmail;
import com.umss.sigesa.domain.model.AuditEvent;
import com.umss.sigesa.domain.model.AuditLogQuery;
import com.umss.sigesa.domain.model.AuditResourceType;
import com.umss.sigesa.domain.model.AuditResult;
import com.umss.sigesa.domain.model.Email;
import com.umss.sigesa.domain.model.EvidenceAuditDetails;
import com.umss.sigesa.domain.model.Role;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AuditLogJpaAdapter implements AuditLogPort, AuditLogQueryPort {

    static final String LOGIN_SUCCESS = "Inicio de sesión exitoso";
    static final String LOGIN_FAILURE = "Intento de inicio de sesión fallido";
    static final String LOGOUT = "Cierre de sesión";
    static final String SESSION_EXPIRED = "Sesión expirada";
    static final String USER_REGISTERED = "Alta de usuario";
    static final String USER_ACTIVATED = "Activación de usuario";
    static final String USER_DEACTIVATED = "Desactivación de usuario";
    static final String REPORT_REQUESTED = "Solicitud de reporte ejecutivo";
    static final String EVIDENCE_UPLOADED = "Carga de evidencia";

    private final AuditLogJpaRepository repository;
    private final Clock clock;

    public AuditLogJpaAdapter(AuditLogJpaRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void logLogin(UUID userId, Email email) {
        persist(AuditAction.LOGIN, userId, email.value(), AuditResourceType.USER, userId,
                LOGIN_SUCCESS, AuditResult.SUCCESS, null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logLoginFailure(String attemptedEmail) {
        persist(AuditAction.LOGIN, null, AuditActorEmail.snapshot(attemptedEmail),
                AuditResourceType.USER, null, LOGIN_FAILURE, AuditResult.FAILURE, null);
    }

    @Override
    @Transactional
    public void logLogout(UUID userId, Email email) {
        persist(AuditAction.LOGOUT, userId, email == null ? null : email.value(), AuditResourceType.USER, userId,
                LOGOUT, AuditResult.SUCCESS, null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSessionExpired(UUID userId, String email) {
        persist(AuditAction.SESSION_EXPIRED, userId, AuditActorEmail.snapshot(email), AuditResourceType.USER, userId,
                SESSION_EXPIRED, AuditResult.FAILURE, null);
    }

    @Override
    @Transactional
    public void logUserRegistered(UUID userId, Email email, Role role) {
        String payload = role == null ? null : "{\"role\":\"" + role.name() + "\"}";
        persist(AuditAction.USER_REGISTERED, userId, email.value(), AuditResourceType.USER, userId,
                USER_REGISTERED, AuditResult.SUCCESS, payload);
    }

    @Override
    @Transactional
    public void logUserDeactivated(UUID userId, Email email) {
        persist(AuditAction.USER_DEACTIVATED, userId, email.value(), AuditResourceType.USER, userId,
                USER_DEACTIVATED, AuditResult.SUCCESS, null);
    }

    @Override
    @Transactional
    public void logUserActivated(UUID userId, Email email) {
        persist(AuditAction.USER_ACTIVATED, userId, email.value(), AuditResourceType.USER, userId,
                USER_ACTIVATED, AuditResult.SUCCESS, null);
    }

    @Override
    @Transactional
    public void logReportRequested(UUID requesterId, UUID jobId) {
        persist(AuditAction.REPORT_REQUESTED, requesterId, null, AuditResourceType.REPORT, jobId,
                REPORT_REQUESTED, AuditResult.SUCCESS, null);
    }

    @Override
    @Transactional
    public void logEvidenceUploaded(UUID uploadedBy, UUID evidenceId, EvidenceAuditDetails details) {
        String fileName = details.originalFilename();
        String description = fileName == null ? EVIDENCE_UPLOADED : EVIDENCE_UPLOADED + ": " + fileName;
        if (description.length() > 500) {
            description = description.substring(0, 500);
        }
        persist(AuditAction.EVIDENCE_UPLOADED, uploadedBy, null, AuditResourceType.EVIDENCE, evidenceId,
                description, AuditResult.SUCCESS, evidencePayload(details));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditEvent> find(AuditLogQuery query, Pageable pageable) {
        return repository.findAll(AuditLogSpecifications.matching(query), pageable).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AuditEvent> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    private void persist(AuditAction action,
                         UUID actorId,
                         String actorEmail,
                         AuditResourceType resourceType,
                         UUID resourceId,
                         String description,
                         AuditResult result,
                         String payload) {
        AuditLogEntity entity = new AuditLogEntity();
        entity.setId(UUID.randomUUID());
        entity.setActorId(actorId);
        entity.setActorEmail(actorEmail);
        entity.setAction(action);
        entity.setResourceType(resourceType);
        entity.setResourceId(resourceId);
        entity.setDescription(description);
        entity.setOccurredAt(clock.instant());
        entity.setResult(result);
        entity.setIpAddress(clientIp());
        entity.setPayload(payload);
        repository.save(entity);
    }

    private AuditEvent toDomain(AuditLogEntity entity) {
        return new AuditEvent(
                entity.getId(),
                entity.getActorId(),
                entity.getActorEmail(),
                entity.getAction(),
                entity.getResourceType(),
                entity.getResourceId(),
                entity.getDescription(),
                entity.getOccurredAt(),
                entity.getResult(),
                entity.getIpAddress(),
                entity.getPayload()
        );
    }

    private static String evidencePayload(EvidenceAuditDetails details) {
        StringBuilder json = new StringBuilder("{\"indicatorId\":\"")
                .append(details.indicatorId())
                .append("\"");
        if (details.programId() != null) {
            json.append(",\"programId\":\"").append(details.programId()).append("\"");
        }
        if (details.criterionId() != null) {
            json.append(",\"criterionId\":\"").append(details.criterionId()).append("\"");
        }
        if (details.originalFilename() != null) {
            json.append(",\"fileName\":\"").append(details.originalFilename()).append("\"");
        }
        return json.append('}').toString();
    }

    /**
     * Dirección del socket de la petición. No lee encabezados controlados por el cliente.
     */
    private static String clientIp() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
            return null;
        }
        HttpServletRequest request = servletAttributes.getRequest();
        String remote = request.getRemoteAddr();
        if (remote == null || remote.isBlank()) {
            return null;
        }
        return remote.length() > 45 ? remote.substring(0, 45) : remote;
    }
}
