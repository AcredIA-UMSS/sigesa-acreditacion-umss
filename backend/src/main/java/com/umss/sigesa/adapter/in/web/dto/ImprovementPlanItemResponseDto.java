package com.umss.sigesa.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "Ítem del Plan de Mejoras (API-WF-13…16)")
public class ImprovementPlanItemResponseDto {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID id;
    private UUID processId;
    private UUID sourceIndicatorId;
    private String objective;
    private String activity;
    private UUID responsibleId;
    private LocalDate deadline;
    private int progress;
    private String justification;
    private UUID progressEvidenceAssetId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
