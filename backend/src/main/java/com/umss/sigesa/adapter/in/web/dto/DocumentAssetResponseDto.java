package com.umss.sigesa.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "Metadatos de DocumentAsset (API-EVD-07)")
public class DocumentAssetResponseDto {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID id;
    @Schema(example = "SIGESA-20261013-abcd1234")
    private String folioCode;
    private String title;
    private String contentHash;
    private UUID processId;
    private List<UUID> linkedIndicatorIds;
    private LocalDateTime createdAt;
}
