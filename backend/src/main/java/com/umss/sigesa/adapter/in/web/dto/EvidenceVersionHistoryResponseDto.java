package com.umss.sigesa.adapter.in.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class EvidenceVersionHistoryResponseDto {

    private UUID versionId;
    private int version;
    private Integer supersedesVersion;
    private UUID observationId;
    private String description;
    private String contentHash;
    private String originalFilename;
    private UUID createdBy;
    private LocalDateTime createdAt;
    private boolean current;
    private boolean blobAvailable;
}
