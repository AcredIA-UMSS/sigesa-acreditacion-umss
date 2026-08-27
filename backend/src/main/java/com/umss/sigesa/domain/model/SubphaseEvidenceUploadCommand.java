package com.umss.sigesa.domain.model;

import java.util.UUID;

public record SubphaseEvidenceUploadCommand(
        UUID subphaseId,
        String description,
        byte[] fileContent,
        String contentType,
        String originalFilename,
        UUID uploadedBy
) {
}
