package com.umss.sigesa.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@Schema(description = "Resultado de generación de borrador E4 (API-WF-17)")
public class GenerateDeliverableResponseDto {

    private UUID deliverableId;
    private UUID documentAssetId;
    private String folioCode;
    private String title;
}
