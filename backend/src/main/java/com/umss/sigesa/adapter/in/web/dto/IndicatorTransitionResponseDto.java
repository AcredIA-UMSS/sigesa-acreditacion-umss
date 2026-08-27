package com.umss.sigesa.adapter.in.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class IndicatorTransitionResponseDto {
    private UUID indicatorId;
    private String previousState;
    private String newState;
    private UUID stateHistoryId;
}
