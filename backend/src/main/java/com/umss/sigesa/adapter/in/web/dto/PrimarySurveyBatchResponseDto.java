package com.umss.sigesa.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "Lote de encuesta primaria (API-WF-11 / WF-12)")
public class PrimarySurveyBatchResponseDto {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID id;
    private UUID processId;
    private String audience;
    private int responsesCount;
    private UUID documentAssetId;
    private LocalDateTime registeredAt;
}
