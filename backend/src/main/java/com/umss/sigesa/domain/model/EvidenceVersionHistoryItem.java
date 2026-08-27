package com.umss.sigesa.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record EvidenceVersionHistoryItem(
        UUID versionId,
        int version,
        Integer supersedesVersion,
        UUID observationId,
        String description,
        String contentHash,
        String originalFilename,
        UUID createdBy,
        LocalDateTime createdAt,
        boolean current,
        boolean blobAvailable) {
}
