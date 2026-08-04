package com.umss.sigesa.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ProcessSummaryResponseDto {
    private UUID id;
    private UUID careerId;
    private String careerCode;
    private String careerName;
    private UUID templateId;
    private String templateName;
    private String templateType;
    private String status;
    private LocalDateTime startDate;
    private int phaseCount;
    private int subphaseCount;
}
