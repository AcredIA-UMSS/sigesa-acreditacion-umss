package com.umss.sigesa.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CoverageMatrix(
        UUID processId,
        String careerName,
        Instant generatedAt,
        Totals totals,
        List<DimensionNode> dimensions
) {

    public record Totals(
            int indicators,
            int withEvidence,
            int approved,
            int observed,
            int other,
            double coveragePercent
    ) {
    }

    public record DimensionNode(UUID id, String name, List<ComponentNode> components) {
    }

    public record ComponentNode(UUID id, String name, List<CriterionNode> criteria) {
    }

    public record CriterionNode(UUID id, String name, List<IndicatorRow> indicators) {
    }

    public record IndicatorRow(
            UUID id,
            String code,
            String description,
            String status,
            int evidenceCount
    ) {
    }
}
