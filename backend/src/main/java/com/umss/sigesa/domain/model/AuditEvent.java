package com.umss.sigesa.domain.model;

import java.time.Instant;
import java.util.UUID;

public record AuditEvent(
        UUID id,
        UUID actorId,
        String actorEmail,
        AuditAction action,
        AuditResourceType resourceType,
        UUID resourceId,
        String description,
        Instant occurredAt,
        AuditResult result,
        String ipAddress,
        String payload
) {
}
