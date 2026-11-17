package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.AccessSessionJpaEntity;
import com.umss.sigesa.application.access.AccessSessionHolder;
import com.umss.sigesa.application.port.out.AccessSessionPort;
import com.umss.sigesa.application.port.out.AuditLogPort;
import com.umss.sigesa.domain.model.access.AccessSession;
import com.umss.sigesa.domain.model.access.AccessSessionEndReason;
import com.umss.sigesa.domain.model.access.AccessSessionStatus;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AccessSessionJpaAdapter implements AccessSessionPort {

    private static final int USER_AGENT_LIMIT = 512;

    private final AccessSessionJpaRepository repository;
    private final AuditLogPort auditLogPort;

    public AccessSessionJpaAdapter(AccessSessionJpaRepository repository, AuditLogPort auditLogPort) {
        this.repository = repository;
        this.auditLogPort = auditLogPort;
    }

    @Override
    @Transactional
    public UUID openSuccessful(UUID userId, String fullName, String email, String role) {
        LocalDateTime now = LocalDateTime.now();
        HttpServletRequest request = currentRequest();
        AccessSessionJpaEntity entity = AccessSessionJpaEntity.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .fullName(blankToNull(fullName))
                .email(email)
                .role(role)
                .startedAt(now)
                .ipAddress(clientIp(request))
                .userAgent(userAgent(request))
                .status(AccessSessionStatus.ACTIVE)
                .createdAt(now)
                .build();
        repository.save(entity);
        AccessSessionHolder.bind(entity.getId());
        return entity.getId();
    }

    @Override
    @Transactional
    public void recordFailure(String email) {
        LocalDateTime now = LocalDateTime.now();
        HttpServletRequest request = currentRequest();
        String safeEmail = email == null || email.isBlank() ? "desconocido" : email.trim();
        AccessSessionJpaEntity entity = AccessSessionJpaEntity.builder()
                .id(UUID.randomUUID())
                .email(safeEmail)
                .startedAt(now)
                .endedAt(now)
                .endReason(AccessSessionEndReason.FAILED.name())
                .ipAddress(clientIp(request))
                .userAgent(userAgent(request))
                .status(AccessSessionStatus.FAILED)
                .createdAt(now)
                .build();
        repository.save(entity);
        AccessSessionHolder.bind(entity.getId());
        auditLogPort.logLoginFailed(safeEmail);
        AccessSessionHolder.clear();
    }

    @Override
    @Transactional
    public void close(UUID sessionId, AccessSessionEndReason reason) {
        if (sessionId == null || reason == null) {
            return;
        }
        repository.findById(sessionId).ifPresent(entity -> {
            if (entity.getStatus() != AccessSessionStatus.ACTIVE) {
                return;
            }
            AccessSession closed = toDomain(entity).close(reason, LocalDateTime.now());
            entity.setStatus(closed.status());
            entity.setEndReason(closed.endReason() == null ? null : closed.endReason().name());
            entity.setEndedAt(closed.endedAt());
            repository.save(entity);
        });
    }

    @Override
    @Transactional
    public void revokeActiveByUser(UUID userId) {
        if (userId == null) {
            return;
        }
        repository.findByUserIdAndStatus(userId, AccessSessionStatus.ACTIVE)
                .forEach(entity -> close(entity.getId(), AccessSessionEndReason.REVOKED));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AccessSession> findById(UUID sessionId) {
        return repository.findById(sessionId).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccessSession> search(String email, String role, AccessSessionStatus status, String ipAddress,
                                      LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return repository.search(blankToNull(email), blankToNull(role), status, blankToNull(ipAddress), from, to, pageable)
                .map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAll() {
        return repository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(AccessSessionStatus status) {
        return repository.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public long countStartedSince(LocalDateTime from) {
        return repository.countByStartedAtGreaterThanEqual(from);
    }

    @Override
    @Transactional(readOnly = true)
    public long countDistinctActiveUsers() {
        return repository.countDistinctUsersByStatus(AccessSessionStatus.ACTIVE);
    }

    private AccessSession toDomain(AccessSessionJpaEntity entity) {
        AccessSessionEndReason reason = null;
        if (entity.getEndReason() != null && !entity.getEndReason().isBlank()) {
            reason = AccessSessionEndReason.valueOf(entity.getEndReason());
        }
        return new AccessSession(
                entity.getId(),
                entity.getUserId(),
                entity.getFullName(),
                entity.getEmail(),
                entity.getRole(),
                entity.getStartedAt(),
                entity.getEndedAt(),
                reason,
                entity.getIpAddress(),
                entity.getUserAgent(),
                entity.getStatus());
    }

    private static HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }

    static String clientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    static String userAgent(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String agent = request.getHeader("User-Agent");
        if (agent == null || agent.isBlank()) {
            return null;
        }
        return agent.length() <= USER_AGENT_LIMIT ? agent : agent.substring(0, USER_AGENT_LIMIT);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
