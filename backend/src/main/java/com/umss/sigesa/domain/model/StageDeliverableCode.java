package com.umss.sigesa.domain.model;

import java.util.EnumSet;
import java.util.Set;

public enum StageDeliverableCode {
    HCC_RESOLUTION,
    WORK_SCHEDULE,
    PRIOR_RECOMMENDATIONS_REPORT,
    BUDGET_FORECAST,
    SECONDARY_EVIDENCE_COMPLETE,
    PRIMARY_SURVEY_REPORT,
    COLLECTION_GAP_REPORT;

    public static Set<StageDeliverableCode> forStage(MethodologicalStageCode stageCode) {
        return switch (stageCode) {
            case PREPARATORY -> EnumSet.of(
                    HCC_RESOLUTION,
                    WORK_SCHEDULE,
                    PRIOR_RECOMMENDATIONS_REPORT,
                    BUDGET_FORECAST);
            case COLLECTION -> EnumSet.of(
                    SECONDARY_EVIDENCE_COMPLETE,
                    PRIMARY_SURVEY_REPORT,
                    COLLECTION_GAP_REPORT);
            default -> EnumSet.noneOf(StageDeliverableCode.class);
        };
    }

    public static boolean isMandatoryForGate(MethodologicalStageCode stageCode, StageDeliverableCode deliverableCode) {
        return forStage(stageCode).contains(deliverableCode);
    }
}
