package com.umss.sigesa.adapter.in.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SubphaseRejectResponseDto {
    private UUID subphaseId;
    private UUID observationId;
    private SubphaseTransitionResponseDto transition;
}
