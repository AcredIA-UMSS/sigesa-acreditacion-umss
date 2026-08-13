package com.umss.sigesa.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Respuesta de carga de evidencia v1 (FSD-UC-004)")
public record UploadEvidenceResponse(
        @Schema(description = "Identificador de la evidencia", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID evidenceId,
        @Schema(description = "Versión creada (v1)", requiredMode = Schema.RequiredMode.REQUIRED)
        int version,
        @Schema(description = "SHA-256 del contenido", requiredMode = Schema.RequiredMode.REQUIRED)
        String contentHash,
        @Schema(description = "Evento de dominio publicado", example = "EvidenceUploaded")
        String event,
        @Schema(description = "Estado vigente del indicador", example = "SUBIDO", requiredMode = Schema.RequiredMode.REQUIRED)
        String currentState
) {
}
