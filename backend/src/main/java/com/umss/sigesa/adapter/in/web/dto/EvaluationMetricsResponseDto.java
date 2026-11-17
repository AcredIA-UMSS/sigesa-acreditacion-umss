package com.umss.sigesa.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@Schema(description = "Métricas de completitud de evidencia, FODA y encuestas (API-WF-09)")
public class EvaluationMetricsResponseDto {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID processId;
    private long totalIndicators;
    private long indicatorsWithEvidence;
    private double evidenceCompletenessRatio;
    private long fodaComplete;
    private double fodaCompletenessRatio;
    private long primarySurveyBatchCount;
}
