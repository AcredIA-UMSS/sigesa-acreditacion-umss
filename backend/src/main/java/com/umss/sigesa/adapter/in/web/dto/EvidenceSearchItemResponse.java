package com.umss.sigesa.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record EvidenceSearchItemResponse(
        UUID evidenceId,
        UUID indicatorId,
        String indicatorCode,
        String indicatorTitle,
        UUID programId,
        int phaseId,
        int latestVersion,
        String description,
        LocalDateTime createdAt
) {
}
