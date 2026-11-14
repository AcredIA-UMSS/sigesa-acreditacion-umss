package com.umss.sigesa.domain.model.access;

import java.time.LocalDateTime;
import java.util.UUID;

public record AccessSession(
        UUID id,
        UUID userId,
        String fullName,
        String email,
        String role,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        AccessSessionEndReason endReason,
        String ipAddress,
        String userAgent,
        AccessSessionStatus status
) {
    public AccessSession close(AccessSessionEndReason reason, LocalDateTime endedAt) {
        if (status != AccessSessionStatus.ACTIVE) {
            return this;
        }
        AccessSessionStatus next = switch (reason) {
            case LOGOUT -> AccessSessionStatus.CLOSED;
            case EXPIRED -> AccessSessionStatus.EXPIRED;
            case REVOKED -> AccessSessionStatus.REVOKED;
            case FAILED -> AccessSessionStatus.FAILED;
        };
        return new AccessSession(id, userId, fullName, email, role, startedAt, endedAt, reason, ipAddress, userAgent, next);
    }
}
