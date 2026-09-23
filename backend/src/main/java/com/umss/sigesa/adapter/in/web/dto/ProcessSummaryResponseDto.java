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
    private String evaluatorModel;
    private String status;
    private LocalDateTime startDate;
    private int level1Count;
    private int indicatorCount;
    private ProcessResponsibleDto responsible;
}
