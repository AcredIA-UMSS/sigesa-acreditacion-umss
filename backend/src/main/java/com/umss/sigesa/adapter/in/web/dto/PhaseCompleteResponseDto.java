package com.umss.sigesa.adapter.in.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PhaseCompleteResponseDto {
    private UUID phaseId;
    private String previousState;
    private String newState;
    private String event;
}
