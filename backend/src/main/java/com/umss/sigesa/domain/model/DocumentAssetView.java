package com.umss.sigesa.domain.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record DocumentAssetView(
        UUID id,
        String folioCode,
        String title,
        String contentHash,
        UUID processId,
        List<UUID> linkedIndicatorIds,
        LocalDateTime createdAt
) {
}
