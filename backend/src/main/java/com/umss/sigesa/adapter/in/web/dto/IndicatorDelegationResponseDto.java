package com.umss.sigesa.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "Delegación CAE sobre N1, N2 o indicador")
public class IndicatorDelegationResponseDto {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID id;
    private UUID processId;
    private UUID userId;
    private UUID level1Id;
    private UUID level2Id;
    private UUID indicatorId;
    private String permission;
    private LocalDateTime createdAt;
}
