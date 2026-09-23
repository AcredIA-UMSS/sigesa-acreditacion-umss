package com.umss.sigesa.application.service.workflow;

import com.umss.sigesa.application.port.out.EvaluationMetricsPort;
import com.umss.sigesa.application.port.out.MethodologicalStagePort;
import com.umss.sigesa.domain.model.MethodologicalStage;
import com.umss.sigesa.domain.model.MethodologicalStageCode;
import com.umss.sigesa.domain.model.MethodologicalStageStatus;
import com.umss.sigesa.domain.model.StageDeliverable;
import com.umss.sigesa.domain.model.StageDeliverableApprovalStatus;
import com.umss.sigesa.domain.model.StageDeliverableCode;
import com.umss.sigesa.domain.model.StageGateResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StageGateEvaluatorTest {

    @Mock
    private EvaluationMetricsPort evaluationMetricsPort;

    @Mock
    private MethodologicalStagePort stagePort;

    private StageGateEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new StageGateEvaluator(evaluationMetricsPort, stagePort);
    }

    @Test
    void preparatoryGatePassesWhenMandatoryDeliverablesApproved() {
        MethodologicalStage stage = preparatoryStage(true);
        StageGateResult result = evaluator.evaluate(stage);
        assertTrue(result.pass());
    }

    @Test
    void preparatoryGateBlocksWhenDeliverablePending() {
        MethodologicalStage stage = preparatoryStage(false);
        StageGateResult result = evaluator.evaluate(stage);
        assertFalse(result.pass());
    }

    @Test
    void collectionGateBlocksWithoutEvidenceCompleteness() {
        UUID processId = UUID.randomUUID();
        MethodologicalStage stage = collectionStage(processId, true);
        when(evaluationMetricsPort.countIndicatorsByProcessId(processId)).thenReturn(10L);
        when(evaluationMetricsPort.countIndicatorsWithEvidenceByProcessId(processId)).thenReturn(5L);
        when(stagePort.countPrimarySurveyBatches(processId)).thenReturn(1L);

        StageGateResult result = evaluator.evaluate(stage);
        assertFalse(result.pass());
    }

    private static MethodologicalStage preparatoryStage(boolean allApproved) {
        List<StageDeliverable> deliverables = StageDeliverableCode.forStage(MethodologicalStageCode.PREPARATORY)
                .stream()
                .map(code -> StageDeliverable.builder()
                        .deliverableCode(code)
                        .approvalStatus(allApproved
                                ? StageDeliverableApprovalStatus.APPROVED
                                : StageDeliverableApprovalStatus.PENDING)
                        .build())
                .toList();

        return MethodologicalStage.builder()
                .code(MethodologicalStageCode.PREPARATORY)
                .status(MethodologicalStageStatus.SUBMITTED_FOR_REVIEW)
                .deliverables(deliverables)
                .build();
    }

    private static MethodologicalStage collectionStage(UUID processId, boolean deliverablesApproved) {
        List<StageDeliverable> deliverables = StageDeliverableCode.forStage(MethodologicalStageCode.COLLECTION)
                .stream()
                .map(code -> StageDeliverable.builder()
                        .deliverableCode(code)
                        .approvalStatus(deliverablesApproved
                                ? StageDeliverableApprovalStatus.APPROVED
                                : StageDeliverableApprovalStatus.PENDING)
                        .build())
                .toList();

        return MethodologicalStage.builder()
                .processId(processId)
                .code(MethodologicalStageCode.COLLECTION)
                .status(MethodologicalStageStatus.SUBMITTED_FOR_REVIEW)
                .deliverables(deliverables)
                .build();
    }
}
