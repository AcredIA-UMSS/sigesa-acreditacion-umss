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
public class SubphaseObservation {
    private UUID id;
    private UUID subphaseId;
    private UUID authorId;
    private String authorRole;
    private String body;
    private SubphaseObservationStatus status;
    private LocalDateTime resolvedAt;
    private UUID resolvedVersionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
