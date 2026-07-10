package com.umss.sigesa.adapter.in.web.dto;

import com.umss.sigesa.domain.model.ProcessType;

import java.time.LocalDateTime;
import java.util.UUID;

public record TemplateSummaryResponse(
        UUID id,
        boolean validated,
        String taxonomyVersion,
        String activePeriod,
        LocalDateTime activatedAt,
        ProcessType type
) {
}
