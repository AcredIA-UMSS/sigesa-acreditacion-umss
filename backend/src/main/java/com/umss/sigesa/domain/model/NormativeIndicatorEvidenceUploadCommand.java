package com.umss.sigesa.domain.model;

import java.util.List;
import java.util.UUID;

public record NormativeIndicatorEvidenceUploadCommand(
        UUID indicatorId,
        String description,
        byte[] fileContent,
        String contentType,
        String originalFilename,
        String externalUrl,
        UUID uploadedBy,
        List<UUID> jwtProgramScope
) {
    public NormativeIndicatorEvidenceUploadCommand(
            UUID indicatorId,
            String description,
            byte[] fileContent,
            String contentType,
            String originalFilename,
            String externalUrl,
            UUID uploadedBy) {
        this(
                indicatorId,
                description,
                fileContent,
                contentType,
                originalFilename,
                externalUrl,
                uploadedBy,
                List.of());
    }
}
