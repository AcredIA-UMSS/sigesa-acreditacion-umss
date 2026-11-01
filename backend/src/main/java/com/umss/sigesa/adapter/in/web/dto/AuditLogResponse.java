package com.umss.sigesa.adapter.in.web.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        UUID actorId,
        String actorEmail,
        String action,
        String resourceType,
        UUID resourceId,
        String description,
        OffsetDateTime occurredAt,
        String result,
        String ipAddress,
        String payload
) {
}
