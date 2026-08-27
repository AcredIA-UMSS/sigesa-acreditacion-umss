package com.umss.sigesa.domain.model;

import java.util.UUID;

public record SubphaseEvidenceSubsanationCommand(
        UUID subphaseId,
        UUID evidenceId,
        UUID observationId,
        String description,
        byte[] fileContent,
        String contentType,
        String originalFilename,
        UUID uploadedBy) {
}
