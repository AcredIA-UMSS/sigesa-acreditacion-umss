package com.umss.sigesa.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "Estado de freeze del proceso (API-WF-18…20)")
public class ProcessFreezeResponseDto {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID id;
    private UUID processId;
    private String mode;
    private boolean active;
    private LocalDateTime frozenAt;
    private UUID frozenBy;
    private LocalDateTime liftedAt;
    private UUID liftedBy;
}
