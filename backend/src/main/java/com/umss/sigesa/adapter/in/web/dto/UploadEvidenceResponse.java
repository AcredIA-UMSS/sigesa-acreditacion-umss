package com.umss.sigesa.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Respuesta de carga o vínculo de evidencia (FSD-UC-004 / M7)")
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
        String currentState,
        @Schema(description = "Identificador del DocumentAsset físico")
        UUID documentAssetId,
        @Schema(description = "Folio institucional", example = "SIGESA-20261013-abcd1234")
        String folioCode,
        @Schema(description = "True si se reutilizó un blob existente del proceso")
        boolean reusedAsset
) {
    public UploadEvidenceResponse(
            UUID evidenceId,
            int version,
            String contentHash,
            String event,
            String currentState) {
        this(evidenceId, version, contentHash, event, currentState, null, null, false);
    }
}
