package com.umss.sigesa.domain.model;

import java.util.List;
import java.util.UUID;

public record CoordinatorKpiSection(
        UUID programId,
        String programName,
        Integer totalIndicators,
        Double overallProgressPercentage,
        Integer approvedEvidences,
        Integer rejectedEvidences,
        Integer pendingObservations,
        List<PhaseProgressSummary> phaseProgressList,
        List<BottleneckSummary> bottlenecks
) {}
