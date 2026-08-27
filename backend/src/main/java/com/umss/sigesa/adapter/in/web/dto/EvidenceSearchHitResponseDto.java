package com.umss.sigesa.adapter.in.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class EvidenceSearchHitResponseDto {
    private UUID evidenceId;
    private UUID subphaseId;
    private String subphaseName;
    private UUID phaseId;
    private String phaseName;
    private UUID processId;
    private UUID indicatorId;
    private String indicatorCode;
    private String indicatorTitle;
    private int version;
    private String description;
    private String originalFilename;
    private LocalDateTime uploadedAt;
    private UUID uploadedBy;
    private boolean blobAvailable;
}
