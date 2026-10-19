package com.umss.sigesa.adapter.in.web.dto;

import com.umss.sigesa.application.model.dashboard.TechnicianInboxItem;
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
public class TechnicianInboxResponseDto {

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

    public static TechnicianInboxResponseDto from(TechnicianInboxItem item) {
        return TechnicianInboxResponseDto.builder()
                .indicatorId(item.getIndicatorId())
                .indicatorCode(item.getIndicatorCode())
                .indicatorDescription(item.getIndicatorDescription())
                .status(item.getStatus())
                .dimensionName(item.getDimensionName())
                .criterionName(item.getCriterionName())
                .subcriterionName(item.getSubcriterionName())
                .pathHierarchy(item.getPathHierarchy())
                .programId(item.getProgramId())
                .programName(item.getProgramName())
                .evidenceId(item.getEvidenceId())
                .evidenceFilename(item.getEvidenceFilename())
                .evidenceUploadedAt(item.getEvidenceUploadedAt())
                .activeObservationsCount(item.getActiveObservationsCount())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
