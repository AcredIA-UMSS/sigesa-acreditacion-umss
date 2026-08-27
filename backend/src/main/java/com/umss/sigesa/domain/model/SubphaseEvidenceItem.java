package com.umss.sigesa.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record SubphaseEvidenceItem(
        UUID evidenceId,
        UUID subphaseId,
        UUID indicatorId,
        int version,
        String description,
        String contentHash,
        String originalFilename,
        LocalDateTime uploadedAt,
        UUID uploadedBy
) {
}
