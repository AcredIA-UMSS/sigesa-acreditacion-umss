package com.umss.sigesa.adapter.in.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PhaseProgressResponseDto {
    private UUID phaseId;
    private int totalSubphases;
    private int approvedSubphases;
    private int pendingSubphases;
    private double progressPercent;
    private String state;
}
