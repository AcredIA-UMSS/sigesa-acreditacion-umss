package com.umss.sigesa.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Levantar freeze hacia ACTIVE o IMPROVEMENT_MONITORING")
public class LiftFreezeRequestDto {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "ACTIVE")
    private String targetMode;
}
