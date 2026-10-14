package com.umss.sigesa.application.service.workflow;

import com.umss.sigesa.application.port.out.EvaluationMetricsPort;
import com.umss.sigesa.application.port.out.ImprovementPlanItemPort;
import com.umss.sigesa.application.port.out.MethodologicalStagePort;
import com.umss.sigesa.application.port.out.PeerRoomChecklistPort;
import com.umss.sigesa.application.port.out.ProcessFreezePort;
import com.umss.sigesa.domain.model.ImprovementPlanItem;
import com.umss.sigesa.domain.model.MethodologicalStage;
import com.umss.sigesa.domain.model.MethodologicalStageCode;
import com.umss.sigesa.domain.model.ProcessFreeze;
import com.umss.sigesa.domain.model.StageDeliverable;
import com.umss.sigesa.domain.model.StageDeliverableApprovalStatus;
import com.umss.sigesa.domain.model.StageDeliverableCode;
import com.umss.sigesa.domain.model.StageGateResult;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StageGateEvaluator {

    private final EvaluationMetricsPort evaluationMetricsPort;
    private final MethodologicalStagePort stagePort;
    private final ProcessFreezePort freezePort;
    private final PeerRoomChecklistPort checklistPort;
    private final ImprovementPlanItemPort planItemPort;

    public StageGateEvaluator(
            EvaluationMetricsPort evaluationMetricsPort,
            MethodologicalStagePort stagePort,
            ProcessFreezePort freezePort,
            PeerRoomChecklistPort checklistPort,
            ImprovementPlanItemPort planItemPort) {
        this.evaluationMetricsPort = evaluationMetricsPort;
        this.stagePort = stagePort;
        this.freezePort = freezePort;
        this.checklistPort = checklistPort;
        this.planItemPort = planItemPort;
    }

    public StageGateResult evaluate(MethodologicalStage stage) {
        return switch (stage.getCode()) {
            case PREPARATORY -> evaluatePreparatory(stage);
            case COLLECTION -> evaluateCollection(stage);
            case SYSTEMATIZATION -> evaluateSystematization(stage);
            case DRAFTING -> evaluateDrafting(stage);
            case PRESENTATION -> evaluatePresentation(stage);
            case EXTERNAL_PREP -> evaluateExternalPrep(stage);
            case POST_ACCREDITATION -> evaluatePostAccreditation(stage);
        };
    }

    public String toRulesSnapshotJson(MethodologicalStage stage, StageGateResult result) {
        return "{\"stageCode\":\"" + stage.getCode().name()
                + "\",\"pass\":" + result.pass()
                + ",\"failedRules\":" + listToJson(result.failedRules()) + "}";
    }

    private StageGateResult evaluatePreparatory(MethodologicalStage stage) {
        List<String> failed = new ArrayList<>();
        addPendingDeliverables(stage, MethodologicalStageCode.PREPARATORY, failed);
        if (failed.isEmpty()) {
            return StageGateResult.pass("Todos los entregables E1 están aprobados.");
        }
        return StageGateResult.block(failed, "Compuerta E1 bloqueada: faltan entregables aprobados.");
    }

    private StageGateResult evaluateCollection(MethodologicalStage stage) {
        List<String> failed = new ArrayList<>();
        UUID processId = stage.getProcessId();

        long totalIndicators = evaluationMetricsPort.countIndicatorsByProcessId(processId);
        long withEvidence = evaluationMetricsPort.countIndicatorsWithEvidenceByProcessId(processId);

        if (totalIndicators == 0 || withEvidence < totalIndicators) {
            failed.add(String.format(
                    "Completitud de evidencias: %d/%d indicadores",
                    withEvidence,
                    totalIndicators));
        }

        long surveyBatches = stagePort.countPrimarySurveyBatches(processId);
        if (surveyBatches < 1) {
            failed.add("Se requiere al menos un lote de encuesta primaria registrado.");
        }

        addPendingDeliverables(stage, MethodologicalStageCode.COLLECTION, failed);

        if (failed.isEmpty()) {
            return StageGateResult.pass("Compuerta E2 satisfecha: evidencias y encuestas completas.");
        }
        return StageGateResult.block(failed, "Compuerta E2 bloqueada.");
    }

    private StageGateResult evaluateSystematization(MethodologicalStage stage) {
        List<String> failed = new ArrayList<>();
        UUID processId = stage.getProcessId();

        long totalIndicators = evaluationMetricsPort.countIndicatorsByProcessId(processId);
        long fodaComplete = evaluationMetricsPort.countFodaCompleteByProcessId(processId);

        if (totalIndicators == 0 || fodaComplete < totalIndicators) {
            failed.add(String.format(
                    "Completitud FODA: %d/%d indicadores",
                    fodaComplete,
                    totalIndicators));
        }

        addPendingDeliverables(stage, MethodologicalStageCode.SYSTEMATIZATION, failed);

        if (failed.isEmpty()) {
            return StageGateResult.pass("Compuerta E3 satisfecha: FODA y entregables completos.");
        }
        return StageGateResult.block(failed, "Compuerta E3 bloqueada.");
    }

    private StageGateResult evaluateDrafting(MethodologicalStage stage) {
        List<String> failed = new ArrayList<>();
        for (StageDeliverableCode code : StageDeliverableCode.forStage(MethodologicalStageCode.DRAFTING)) {
            StageDeliverable deliverable = findDeliverable(stage, code);
            if (deliverable == null) {
                failed.add("Entregable E4 ausente: " + code.name());
                continue;
            }
            if (deliverable.getDocumentAssetId() == null) {
                failed.add("Entregable E4 sin documento generado: " + code.name());
            }
            if (deliverable.getApprovalStatus() != StageDeliverableApprovalStatus.APPROVED) {
                failed.add("Entregable E4 pendiente de aprobación: " + code.name());
            }
        }
        if (failed.isEmpty()) {
            return StageGateResult.pass("Compuerta E4 satisfecha: documentos generados y aprobados.");
        }
        return StageGateResult.block(failed, "Compuerta E4 bloqueada.");
    }

    private StageGateResult evaluatePresentation(MethodologicalStage stage) {
        List<String> failed = new ArrayList<>();
        addPendingDeliverables(stage, MethodologicalStageCode.PRESENTATION, failed);
        if (!isActiveAuditReadonly(stage.getProcessId())) {
            failed.add("Se requiere freeze AUDIT_READONLY activo.");
        }
        if (failed.isEmpty()) {
            return StageGateResult.pass("Compuerta E5 satisfecha: entregables aprobados y freeze activo.");
        }
        return StageGateResult.block(failed, "Compuerta E5 bloqueada.");
    }

    private StageGateResult evaluateExternalPrep(MethodologicalStage stage) {
        List<String> failed = new ArrayList<>();
        if (!isActiveAuditReadonly(stage.getProcessId())) {
            failed.add("Se requiere freeze AUDIT_READONLY activo.");
        }
        addPendingDeliverables(stage, MethodologicalStageCode.EXTERNAL_PREP, failed);
        if (!checklistPort.allChecked(stage.getProcessId())) {
            failed.add("Checklist de Sala de Pares incompleto.");
        }
        if (failed.isEmpty()) {
            return StageGateResult.pass("Compuerta E6 satisfecha: freeze, entregables y checklist.");
        }
        return StageGateResult.block(failed, "Compuerta E6 bloqueada.");
    }

    private StageGateResult evaluatePostAccreditation(MethodologicalStage stage) {
        List<String> failed = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (ImprovementPlanItem item : planItemPort.findByProcessId(stage.getProcessId())) {
            if (item.getDeadline() != null
                    && item.getDeadline().isBefore(today)
                    && item.getProgress() < 100
                    && (item.getJustification() == null || item.getJustification().isBlank())) {
                failed.add("Ítem vencido sin justificación: " + item.getId());
            }
        }
        StageDeliverable e7 = findDeliverable(stage, StageDeliverableCode.IMPROVEMENT_PROGRESS_REPORT);
        if (e7 == null) {
            failed.add("Entregable E7 ausente: IMPROVEMENT_PROGRESS_REPORT");
        } else {
            if (e7.getDocumentAssetId() == null) {
                failed.add("Entregable E7 sin documento generado: IMPROVEMENT_PROGRESS_REPORT");
            }
            if (e7.getApprovalStatus() != StageDeliverableApprovalStatus.APPROVED) {
                failed.add("Entregable E7 pendiente de aprobación: IMPROVEMENT_PROGRESS_REPORT");
            }
        }
        if (failed.isEmpty()) {
            return StageGateResult.pass("Compuerta E7 satisfecha: justificación de vencidos y reporte aprobado.");
        }
        return StageGateResult.block(failed, "Compuerta E7 bloqueada.");
    }

    private boolean isActiveAuditReadonly(UUID processId) {
        return freezePort.findByProcessId(processId)
                .map(ProcessFreeze::isActiveAuditReadonly)
                .orElse(false);
    }

    private static void addPendingDeliverables(
            MethodologicalStage stage,
            MethodologicalStageCode stageCode,
            List<String> failed) {
        for (StageDeliverableCode code : StageDeliverableCode.forStage(stageCode)) {
            StageDeliverable deliverable = findDeliverable(stage, code);
            if (deliverable == null || deliverable.getApprovalStatus() != StageDeliverableApprovalStatus.APPROVED) {
                failed.add("Entregable obligatorio pendiente: " + code.name());
            }
        }
    }

    private static StageDeliverable findDeliverable(MethodologicalStage stage, StageDeliverableCode code) {
        return stage.getDeliverables().stream()
                .filter(item -> item.getDeliverableCode() == code)
                .findFirst()
                .orElse(null);
    }

    private static String listToJson(List<String> items) {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append('"').append(items.get(i).replace("\"", "\\\"")).append('"');
        }
        builder.append(']');
        return builder.toString();
    }
}
