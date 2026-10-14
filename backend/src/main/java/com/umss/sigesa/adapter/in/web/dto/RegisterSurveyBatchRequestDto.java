package com.umss.sigesa.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Schema(description = "Registro de lote de encuesta primaria (API-WF-11)")
public class RegisterSurveyBatchRequestDto {

    @NotBlank
    @Size(min = 1, max = 32)
    @Schema(example = "ESTUDIANTES", requiredMode = Schema.RequiredMode.REQUIRED)
    private String audience;

    @NotNull
    @Min(1)
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer responsesCount;

    private UUID documentAssetId;
}
