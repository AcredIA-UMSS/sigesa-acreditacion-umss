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
    @Mock
    private com.umss.sigesa.application.port.out.ProcessFreezePort freezePort;
    @Mock
    private com.umss.sigesa.application.port.out.PeerRoomChecklistPort checklistPort;
    @Mock
    private com.umss.sigesa.application.port.out.ImprovementPlanItemPort planItemPort;

    private StageGateEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new StageGateEvaluator(
                evaluationMetricsPort, stagePort, freezePort, checklistPort, planItemPort);
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

    @Test
    void collectionGateBlocksWithoutSurveyBatch() {
        UUID processId = UUID.randomUUID();
        MethodologicalStage stage = collectionStage(processId, true);
        when(evaluationMetricsPort.countIndicatorsByProcessId(processId)).thenReturn(10L);
        when(evaluationMetricsPort.countIndicatorsWithEvidenceByProcessId(processId)).thenReturn(10L);
        when(stagePort.countPrimarySurveyBatches(processId)).thenReturn(0L);

        StageGateResult result = evaluator.evaluate(stage);
        assertFalse(result.pass());
        assertTrue(result.failedRules().stream().anyMatch(rule -> rule.toLowerCase().contains("encuesta")));
    }

    @Test
    void collectionGatePassesWhenEvidenceSurveyAndDeliverablesComplete() {
        UUID processId = UUID.randomUUID();
        MethodologicalStage stage = collectionStage(processId, true);
        when(evaluationMetricsPort.countIndicatorsByProcessId(processId)).thenReturn(4L);
        when(evaluationMetricsPort.countIndicatorsWithEvidenceByProcessId(processId)).thenReturn(4L);
        when(stagePort.countPrimarySurveyBatches(processId)).thenReturn(1L);

        StageGateResult result = evaluator.evaluate(stage);
        assertTrue(result.pass());
    }

    @Test
    void systematizationGateBlocksWhenFodaIncomplete() {
        UUID processId = UUID.randomUUID();
        MethodologicalStage stage = systematizationStage(processId, true);
        when(evaluationMetricsPort.countIndicatorsByProcessId(processId)).thenReturn(3L);
        when(evaluationMetricsPort.countFodaCompleteByProcessId(processId)).thenReturn(2L);

        StageGateResult result = evaluator.evaluate(stage);
        assertFalse(result.pass());
        assertTrue(result.failedRules().stream().anyMatch(rule -> rule.contains("FODA")));
    }

    @Test
    void systematizationGateBlocksWhenNoIndicators() {
        UUID processId = UUID.randomUUID();
        MethodologicalStage stage = systematizationStage(processId, true);
        when(evaluationMetricsPort.countIndicatorsByProcessId(processId)).thenReturn(0L);
        when(evaluationMetricsPort.countFodaCompleteByProcessId(processId)).thenReturn(0L);

        StageGateResult result = evaluator.evaluate(stage);
        assertFalse(result.pass());
    }

    @Test
    void systematizationGateBlocksWhenE3DeliverablePending() {
        UUID processId = UUID.randomUUID();
        MethodologicalStage stage = systematizationStage(processId, false);
        when(evaluationMetricsPort.countIndicatorsByProcessId(processId)).thenReturn(2L);
        when(evaluationMetricsPort.countFodaCompleteByProcessId(processId)).thenReturn(2L);

        StageGateResult result = evaluator.evaluate(stage);
        assertFalse(result.pass());
        assertTrue(result.failedRules().stream().anyMatch(rule -> rule.contains("FODA_FORMS_COMPLETE")
                || rule.contains("SYNTHESIS_MATRIX")));
    }

    @Test
    void systematizationGatePassesWhenFodaAndDeliverablesComplete() {
        UUID processId = UUID.randomUUID();
        MethodologicalStage stage = systematizationStage(processId, true);
        when(evaluationMetricsPort.countIndicatorsByProcessId(processId)).thenReturn(2L);
        when(evaluationMetricsPort.countFodaCompleteByProcessId(processId)).thenReturn(2L);

        StageGateResult result = evaluator.evaluate(stage);
        assertTrue(result.pass());
    }

    @Test
    void draftingGateBlocksWithoutDocumentAsset() {
        UUID processId = UUID.randomUUID();
        MethodologicalStage stage = draftingStage(processId, true, false);
        StageGateResult result = evaluator.evaluate(stage);
        assertFalse(result.pass());
        assertTrue(result.failedRules().stream().anyMatch(rule -> rule.contains("sin documento")));
    }

    @Test
    void draftingGateBlocksWhenDeliverablePending() {
        UUID processId = UUID.randomUUID();
        MethodologicalStage stage = draftingStage(processId, false, true);
        StageGateResult result = evaluator.evaluate(stage);
        assertFalse(result.pass());
        assertTrue(result.failedRules().stream().anyMatch(rule -> rule.contains("pendiente de aprobación")));
    }

    @Test
    void draftingGatePassesWhenAllE4GeneratedAndApproved() {
        UUID processId = UUID.randomUUID();
        MethodologicalStage stage = draftingStage(processId, true, true);
        StageGateResult result = evaluator.evaluate(stage);
        assertTrue(result.pass());
    }

    @Test
    void presentationGateBlocksWithoutActiveFreeze() {
        UUID processId = UUID.randomUUID();
        MethodologicalStage stage = stageWithCodes(
                processId, MethodologicalStageCode.PRESENTATION, true, false);
        when(freezePort.findByProcessId(processId)).thenReturn(java.util.Optional.empty());

        StageGateResult result = evaluator.evaluate(stage);
        assertFalse(result.pass());
        assertTrue(result.failedRules().stream().anyMatch(rule -> rule.contains("AUDIT_READONLY")));
    }

    @Test
    void presentationGatePassesWithApprovedE5AndActiveFreeze() {
        UUID processId = UUID.randomUUID();
        MethodologicalStage stage = stageWithCodes(
                processId, MethodologicalStageCode.PRESENTATION, true, false);
        when(freezePort.findByProcessId(processId)).thenReturn(java.util.Optional.of(
                com.umss.sigesa.domain.model.ProcessFreeze.builder()
                        .processId(processId)
                        .mode(com.umss.sigesa.domain.model.ProcessFreezeMode.AUDIT_READONLY)
                        .frozenAt(java.time.LocalDateTime.now())
                        .build()));

        assertTrue(evaluator.evaluate(stage).pass());
    }

    @Test
    void externalPrepGateBlocksWhenChecklistIncomplete() {
        UUID processId = UUID.randomUUID();
        MethodologicalStage stage = stageWithCodes(
                processId, MethodologicalStageCode.EXTERNAL_PREP, true, false);
        when(freezePort.findByProcessId(processId)).thenReturn(java.util.Optional.of(
                com.umss.sigesa.domain.model.ProcessFreeze.builder()
                        .processId(processId)
                        .mode(com.umss.sigesa.domain.model.ProcessFreezeMode.AUDIT_READONLY)
                        .frozenAt(java.time.LocalDateTime.now())
                        .build()));
        when(checklistPort.allChecked(processId)).thenReturn(false);

        StageGateResult result = evaluator.evaluate(stage);
        assertFalse(result.pass());
        assertTrue(result.failedRules().stream().anyMatch(rule -> rule.contains("Checklist")));
    }

    @Test
    void externalPrepGatePassesWithFreezeApprovedE6AndChecklist() {
        UUID processId = UUID.randomUUID();
        MethodologicalStage stage = stageWithCodes(
                processId, MethodologicalStageCode.EXTERNAL_PREP, true, false);
        when(freezePort.findByProcessId(processId)).thenReturn(java.util.Optional.of(
                com.umss.sigesa.domain.model.ProcessFreeze.builder()
                        .processId(processId)
                        .mode(com.umss.sigesa.domain.model.ProcessFreezeMode.AUDIT_READONLY)
                        .frozenAt(java.time.LocalDateTime.now())
                        .build()));
        when(checklistPort.allChecked(processId)).thenReturn(true);

        assertTrue(evaluator.evaluate(stage).pass());
    }

    @Test
    void postAccreditationGateBlocksOverdueItemWithoutJustification() {
        UUID processId = UUID.randomUUID();
        MethodologicalStage stage = stageWithCodes(
                processId, MethodologicalStageCode.POST_ACCREDITATION, true, true);
        when(planItemPort.findByProcessId(processId)).thenReturn(List.of(
                com.umss.sigesa.domain.model.ImprovementPlanItem.builder()
                        .id(UUID.randomUUID())
                        .processId(processId)
                        .objective("Cerrar brecha")
                        .deadline(java.time.LocalDate.now().minusDays(1))
                        .progress(40)
                        .justification(" ")
                        .build()));

        StageGateResult result = evaluator.evaluate(stage);
        assertFalse(result.pass());
        assertTrue(result.failedRules().stream().anyMatch(rule -> rule.contains("justificación")));
    }

    @Test
    void postAccreditationGatePassesWhenOverdueJustifiedAndE7Approved() {
        UUID processId = UUID.randomUUID();
        MethodologicalStage stage = stageWithCodes(
                processId, MethodologicalStageCode.POST_ACCREDITATION, true, true);
        when(planItemPort.findByProcessId(processId)).thenReturn(List.of(
                com.umss.sigesa.domain.model.ImprovementPlanItem.builder()
                        .id(UUID.randomUUID())
                        .processId(processId)
                        .objective("Cerrar brecha")
                        .deadline(java.time.LocalDate.now().minusDays(1))
                        .progress(40)
                        .justification("Retraso por calendario institucional")
                        .build()));

        assertTrue(evaluator.evaluate(stage).pass());
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

    private static MethodologicalStage systematizationStage(UUID processId, boolean deliverablesApproved) {
        List<StageDeliverable> deliverables = StageDeliverableCode.forStage(MethodologicalStageCode.SYSTEMATIZATION)
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
                .code(MethodologicalStageCode.SYSTEMATIZATION)
                .status(MethodologicalStageStatus.SUBMITTED_FOR_REVIEW)
                .deliverables(deliverables)
                .build();
    }

    private static MethodologicalStage draftingStage(
            UUID processId,
            boolean deliverablesApproved,
            boolean withDocument) {
        List<StageDeliverable> deliverables = StageDeliverableCode.forStage(MethodologicalStageCode.DRAFTING)
                .stream()
                .map(code -> StageDeliverable.builder()
                        .deliverableCode(code)
                        .documentAssetId(withDocument ? UUID.randomUUID() : null)
                        .approvalStatus(deliverablesApproved
                                ? StageDeliverableApprovalStatus.APPROVED
                                : StageDeliverableApprovalStatus.PENDING)
                        .build())
                .toList();

        return MethodologicalStage.builder()
                .processId(processId)
                .code(MethodologicalStageCode.DRAFTING)
                .status(MethodologicalStageStatus.SUBMITTED_FOR_REVIEW)
                .deliverables(deliverables)
                .build();
    }

    private static MethodologicalStage stageWithCodes(
            UUID processId,
            MethodologicalStageCode code,
            boolean deliverablesApproved,
            boolean withDocument) {
        List<StageDeliverable> deliverables = StageDeliverableCode.forStage(code)
                .stream()
                .map(deliverableCode -> StageDeliverable.builder()
                        .deliverableCode(deliverableCode)
                        .documentAssetId(withDocument ? UUID.randomUUID() : null)
                        .approvalStatus(deliverablesApproved
                                ? StageDeliverableApprovalStatus.APPROVED
                                : StageDeliverableApprovalStatus.PENDING)
                        .build())
                .toList();

        return MethodologicalStage.builder()
                .processId(processId)
                .code(code)
                .status(MethodologicalStageStatus.SUBMITTED_FOR_REVIEW)
                .deliverables(deliverables)
                .build();
    }
}
