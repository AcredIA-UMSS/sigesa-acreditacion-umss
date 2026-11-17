package com.umss.sigesa.domain.model;

import java.util.List;
import java.util.UUID;

public record LinkDocumentAssetToIndicatorCommand(
        UUID indicatorId,
        UUID documentAssetId,
        UUID uploadedBy,
        List<UUID> jwtProgramScope
) {
    public LinkDocumentAssetToIndicatorCommand(
            UUID indicatorId,
            UUID documentAssetId,
            UUID uploadedBy) {
        this(indicatorId, documentAssetId, uploadedBy, List.of());
    }
}
