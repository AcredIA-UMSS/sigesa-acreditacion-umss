package com.umss.sigesa.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Vínculo de un DocumentAsset existente a un indicador (API-EVD-06)")
public record LinkDocumentAssetRequestDto(
        @Schema(description = "Identificador del documento físico", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID documentAssetId
) {
}
