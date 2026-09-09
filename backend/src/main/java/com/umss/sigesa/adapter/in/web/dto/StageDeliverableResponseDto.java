package com.umss.sigesa.adapter.in.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class StageDeliverableResponseDto {
    private UUID id;
    private UUID stageId;
    private String deliverableCode;
    private String approvalStatus;
    private UUID approvedBy;
    private String technicalObservations;
    private LocalDateTime approvedAt;
}
