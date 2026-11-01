package com.umss.sigesa.domain.model;

import java.util.UUID;

public record EvidenceAuditDetails(
        UUID indicatorId,
        UUID programId,
        UUID criterionId,
        String originalFilename
) {
    public EvidenceAuditDetails {
        originalFilename = sanitizeFilename(originalFilename);
    }

    public static String sanitizeFilename(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String name = raw.replace('\\', '/');
        int slash = name.lastIndexOf('/');
        if (slash >= 0) {
            name = name.substring(slash + 1);
        }
        name = name.replace("\r", "").replace("\n", "").replace("\"", "").trim();
        if (name.length() > 180) {
            name = name.substring(0, 180);
        }
        return name.isBlank() ? null : name;
    }
}
