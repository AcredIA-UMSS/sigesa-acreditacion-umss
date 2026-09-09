package com.umss.sigesa.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StageDeliverable {
    private UUID id;
    private UUID stageId;
    private StageDeliverableCode deliverableCode;
    private StageDeliverableApprovalStatus approvalStatus;
    private UUID approvedBy;
    private String technicalObservations;
    private LocalDateTime approvedAt;
}
