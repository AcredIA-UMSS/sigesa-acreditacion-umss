package com.umss.sigesa.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Schema(description = "Alta de delegación CAE. Exactamente un alcance.")
public class CreateDelegationRequestDto {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID userId;
    private UUID level1Id;
    private UUID level2Id;
    private UUID indicatorId;
}
