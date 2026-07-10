package com.umss.sigesa.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ActivateTemplateResponse(
        UUID templateId,
        String taxonomyVersion,
        String activePeriod,
        LocalDateTime activatedAt
) {
}
