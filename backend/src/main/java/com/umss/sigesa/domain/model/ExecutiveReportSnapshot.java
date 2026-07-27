package com.umss.sigesa.domain.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ExecutiveReportSnapshot(
        LocalDateTime generatedAt,
        ExecutiveReportFilters filters,
        List<ProgramSummary> programs
) {
    public record ProgramSummary(
            UUID programId,
            String programName,
            String semaphore,
            int totalIndicators,
            int approvedIndicators
    ) {
    }
}
