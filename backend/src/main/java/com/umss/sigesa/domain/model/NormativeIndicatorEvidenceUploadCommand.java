package com.umss.sigesa.domain.model;

import java.util.UUID;

public record NormativeIndicatorEvidenceUploadCommand(
        UUID indicatorId,
        String description,
        byte[] fileContent,
        String contentType,
        String originalFilename,
        String externalUrl,
        UUID uploadedBy
) {
}
