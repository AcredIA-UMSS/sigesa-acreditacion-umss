package com.umss.sigesa.adapter.in.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SubphaseTransitionResponseDto {
    private UUID subphaseId;
    private String previousState;
    private String newState;
}
