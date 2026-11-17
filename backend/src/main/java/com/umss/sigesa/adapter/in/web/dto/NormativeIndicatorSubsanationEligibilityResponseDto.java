package com.umss.sigesa.adapter.in.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class NormativeIndicatorSubsanationEligibilityResponseDto {
    private boolean canSubsanate;
    private UUID openObservationId;
    private String reason;
}
