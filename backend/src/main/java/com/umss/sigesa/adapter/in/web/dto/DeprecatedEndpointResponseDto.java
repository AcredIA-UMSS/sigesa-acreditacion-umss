package com.umss.sigesa.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "DeprecatedEndpointResponse", description = "Respuesta de endpoint retirado (HTTP 410)")
public class DeprecatedEndpointResponseDto {
    @Schema(example = "ENDPOINT_DEPRECATED")
    private String error;

    @Schema(example = "La carga por indicador fue retirada. Use POST /api/v1/subphases/{subphaseId}/evidences.")
    private String message;

    @Schema(description = "Identificador legacy recibido en la ruta", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private String indicatorId;
}
