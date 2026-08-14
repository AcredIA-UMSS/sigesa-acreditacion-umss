package com.umss.sigesa.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(name = "UploadableIndicatorResponse")
public record UploadableIndicatorResponse(
        UUID indicatorId,
        String code,
        String title,
        UUID criterionId,
        String criterionCode,
        String criterionTitle,
        String currentState
) {
}
