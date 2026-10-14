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
    COLLECTION_GAP_REPORT,
    FODA_FORMS_COMPLETE,
    SYNTHESIS_MATRIX,
    SELF_ASSESSMENT_REPORT,
    OFFICIAL_DATA_FORM,
    IMPROVEMENT_PLAN,
    ACADEMIC_DEV_PLAN,
    COMMUNITY_VALIDATION_MINUTES,
    FORMAL_SUBMISSION_PACKAGE,
    SIMULATION_REPORT,
    PEER_ROOM_CHECKLIST,
    IMPROVEMENT_PROGRESS_REPORT;

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
            case SYSTEMATIZATION -> EnumSet.of(
                    FODA_FORMS_COMPLETE,
                    SYNTHESIS_MATRIX);
            case DRAFTING -> EnumSet.of(
                    SELF_ASSESSMENT_REPORT,
                    OFFICIAL_DATA_FORM,
                    IMPROVEMENT_PLAN,
                    ACADEMIC_DEV_PLAN);
            case PRESENTATION -> EnumSet.of(
                    COMMUNITY_VALIDATION_MINUTES,
                    FORMAL_SUBMISSION_PACKAGE);
            case EXTERNAL_PREP -> EnumSet.of(
                    SIMULATION_REPORT,
                    PEER_ROOM_CHECKLIST);
            case POST_ACCREDITATION -> EnumSet.of(IMPROVEMENT_PROGRESS_REPORT);
            default -> EnumSet.noneOf(StageDeliverableCode.class);
        };
    }

    public static boolean isE4(StageDeliverableCode deliverableCode) {
        return forStage(MethodologicalStageCode.DRAFTING).contains(deliverableCode);
    }

    public static boolean isE7(StageDeliverableCode deliverableCode) {
        return forStage(MethodologicalStageCode.POST_ACCREDITATION).contains(deliverableCode);
    }

    public static boolean isMandatoryForGate(MethodologicalStageCode stageCode, StageDeliverableCode deliverableCode) {
        return forStage(stageCode).contains(deliverableCode);
    }
}
