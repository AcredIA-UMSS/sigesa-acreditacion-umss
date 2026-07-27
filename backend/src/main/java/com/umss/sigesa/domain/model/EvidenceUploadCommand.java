package com.umss.sigesa.domain.model;

import java.util.UUID;

public record EvidenceUploadCommand(
        UUID indicatorId,
        UUID criterionId,
        String description,
        byte[] fileContent,
        String contentType,
        String originalFilename,
        UUID uploadedBy
) {
}
