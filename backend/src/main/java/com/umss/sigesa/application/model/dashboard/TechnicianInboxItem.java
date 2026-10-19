package com.umss.sigesa.application.model.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnicianInboxItem {

    private UUID indicatorId;
    private String indicatorCode;
    private String indicatorDescription;
    private String status;
    private String dimensionName;
    private String criterionName;
    private String subcriterionName;
    private String pathHierarchy;
    private UUID programId;
    private String programName;
    private UUID evidenceId;
    private String evidenceFilename;
    private LocalDateTime evidenceUploadedAt;
    private long activeObservationsCount;
    private LocalDateTime updatedAt;
}
