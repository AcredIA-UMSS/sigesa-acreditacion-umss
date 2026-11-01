package com.umss.sigesa.domain.model;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record AuditLogQuery(
        UUID actorId,
        String actorEmail,
        Set<AuditAction> actions,
        AuditResourceType resourceType,
        UUID resourceId,
        AuditResult result,
        Instant from,
        Instant to
) {
}
