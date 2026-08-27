package com.umss.sigesa.adapter.in.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class SubphaseEvidenceResponseDto {
    private UUID evidenceId;
    private UUID subphaseId;
    private UUID indicatorId;
    private int version;
    private String description;
    private String contentHash;
    private String originalFilename;
    private LocalDateTime uploadedAt;
    private UUID uploadedBy;
}
