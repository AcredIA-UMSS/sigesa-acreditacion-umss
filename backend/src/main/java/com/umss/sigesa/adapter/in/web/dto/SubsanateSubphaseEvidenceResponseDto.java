package com.umss.sigesa.adapter.in.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SubsanateSubphaseEvidenceResponseDto {
    private UUID evidenceId;
    private int version;
    private UUID observationId;
    private int supersedesVersion;
    private String contentHash;
    private String event;
}
