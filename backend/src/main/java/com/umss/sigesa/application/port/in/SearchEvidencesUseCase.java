package com.umss.sigesa.application.port.in;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SearchEvidencesUseCase {

    EvidenceSearchPage search(EvidenceSearchQuery query);

    record EvidenceSearchQuery(
            UUID programId,
            Integer phaseId,
            UUID indicatorId,
            String query,
            List<UUID> allowedProgramIds,
            int page,
            int size
    ) {
    }

    record EvidenceSearchPage(
            List<EvidenceSearchItem> content,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {
    }

    record EvidenceSearchItem(
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
}
