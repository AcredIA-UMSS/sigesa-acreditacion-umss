package com.umss.sigesa.adapter.in.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class EvidenceSearchHitResponseDto {
    private UUID evidenceId;
    private UUID level1Id;
    private String level1Name;
    private String level3Name;
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
