package com.umss.sigesa.domain.model;

import java.util.List;

public record TechnicianKpiSection(
        Integer evidencesPendingReview,
        Integer assignedIndicators,
        Integer openActions,
        Integer available,
        List<RecentEvaluation> recentEvaluations
) {}
