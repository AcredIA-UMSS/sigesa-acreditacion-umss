package com.umss.sigesa.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record EvidenceVersionSummaryResponse(
        int version,
        UUID supersedesId,
        String observationId,
        LocalDateTime createdAt,
        UUID createdBy
) {
}
