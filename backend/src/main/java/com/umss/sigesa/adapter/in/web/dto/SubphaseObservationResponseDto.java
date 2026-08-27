package com.umss.sigesa.adapter.in.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class SubphaseObservationResponseDto {
    private UUID id;
    private UUID subphaseId;
    private UUID authorId;
    private String authorRole;
    private String body;
    private String status;
    private LocalDateTime resolvedAt;
    private UUID resolvedVersionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
