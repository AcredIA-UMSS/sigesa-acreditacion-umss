package com.umss.sigesa.domain.model;

import java.util.UUID;

public record EvaluationMetrics(
        UUID processId,
        long totalIndicators,
        long indicatorsWithEvidence,
        double evidenceCompletenessRatio,
        long fodaComplete,
        double fodaCompletenessRatio,
        long primarySurveyBatchCount
) {

    public static EvaluationMetrics of(
            UUID processId,
            long totalIndicators,
            long indicatorsWithEvidence,
            long fodaComplete,
            long primarySurveyBatchCount) {
        double evidenceRatio = totalIndicators == 0 ? 0.0d : (double) indicatorsWithEvidence / totalIndicators;
        double fodaRatio = totalIndicators == 0 ? 0.0d : (double) fodaComplete / totalIndicators;
        return new EvaluationMetrics(
                processId,
                totalIndicators,
                indicatorsWithEvidence,
                evidenceRatio,
                fodaComplete,
                fodaRatio,
                primarySurveyBatchCount);
    }
}
