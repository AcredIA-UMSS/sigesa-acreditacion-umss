package com.umss.sigesa.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record EvidenceSearchHit(
        UUID evidenceId,
        UUID level1Id,
        String level1Name,
        String level3Name,
        UUID processId,
        UUID indicatorId,
        String indicatorCode,
        String indicatorTitle,
        int version,
        String description,
        String originalFilename,
        LocalDateTime uploadedAt,
        UUID uploadedBy,
        boolean blobAvailable
) {
}
