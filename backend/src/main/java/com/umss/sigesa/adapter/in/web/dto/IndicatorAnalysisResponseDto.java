package com.umss.sigesa.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "Análisis FODA de un indicador (API-WF-10)")
public class IndicatorAnalysisResponseDto {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID id;
    private UUID indicatorId;
    private UUID processId;
    private String fodaQuadrant;
    private String compliance;
    private String synthesis;
    private UUID authorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
