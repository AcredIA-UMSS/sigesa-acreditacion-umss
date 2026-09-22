package com.umss.sigesa.adapter.in.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class NormativeIndicatorEvidenceResponseDto {
    private UUID evidenceId;
    private UUID indicatorId;
    private int version;
    private String description;
    private String contentHash;
    private String originalFilename;
    private String externalUrl;
    private LocalDateTime uploadedAt;
    private UUID uploadedBy;
}
