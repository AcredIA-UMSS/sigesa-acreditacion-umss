package com.umss.sigesa.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Alta o actualización de FODA por indicador (API-WF-10)")
public class UpsertIndicatorAnalysisRequestDto {

    @NotBlank
    @Schema(allowableValues = {"STRENGTH", "WEAKNESS", "OPPORTUNITY", "THREAT"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String fodaQuadrant;

    @NotBlank
    @Schema(allowableValues = {"COMPLIES", "PARTIAL", "DOES_NOT_COMPLY"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String compliance;

    @NotBlank
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String synthesis;
}
