package com.umss.sigesa.domain.model;

import java.util.UUID;

public record GeneratedStageDeliverable(
        UUID deliverableId,
        UUID documentAssetId,
        String folioCode,
        String title
) {
}
