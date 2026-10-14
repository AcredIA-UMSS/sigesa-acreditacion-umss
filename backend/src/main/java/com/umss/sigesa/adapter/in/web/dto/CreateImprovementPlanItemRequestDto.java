package com.umss.sigesa.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Schema(description = "Alta de ítem de Plan de Mejoras (API-WF-14)")
public class CreateImprovementPlanItemRequestDto {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String objective;
    private String activity;
    private UUID responsibleId;
    private LocalDate deadline;
    private Integer progress;
}
