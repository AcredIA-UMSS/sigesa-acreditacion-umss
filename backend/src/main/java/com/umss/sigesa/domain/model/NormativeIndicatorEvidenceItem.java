package com.umss.sigesa.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record NormativeIndicatorEvidenceItem(
        UUID evidenceId,
        UUID indicatorId,
        int version,
        String description,
        String contentHash,
        String originalFilename,
        String externalUrl,
        LocalDateTime uploadedAt,
        UUID uploadedBy
) {
}
