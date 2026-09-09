package com.umss.sigesa.application.service.workflow;

import com.umss.sigesa.application.port.out.EvaluationMetricsPort;
import com.umss.sigesa.application.port.out.MethodologicalStagePort;
import com.umss.sigesa.domain.model.MethodologicalStage;
import com.umss.sigesa.domain.model.MethodologicalStageCode;
import com.umss.sigesa.domain.model.StageDeliverable;
import com.umss.sigesa.domain.model.StageDeliverableApprovalStatus;
import com.umss.sigesa.domain.model.StageDeliverableCode;
import com.umss.sigesa.domain.model.StageGateResult;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StageGateEvaluator {

    private final EvaluationMetricsPort evaluationMetricsPort;
    private final MethodologicalStagePort stagePort;

    public StageGateEvaluator(EvaluationMetricsPort evaluationMetricsPort,
                              MethodologicalStagePort stagePort) {
        this.evaluationMetricsPort = evaluationMetricsPort;
        this.stagePort = stagePort;
    }

    public StageGateResult evaluate(MethodologicalStage stage) {
        return switch (stage.getCode()) {
            case PREPARATORY -> evaluatePreparatory(stage);
            case COLLECTION -> evaluateCollection(stage);
            default -> StageGateResult.pass("Compuerta no configurada para etapa " + stage.getCode());
        };
    }

    public String toRulesSnapshotJson(MethodologicalStage stage, StageGateResult result) {
        return "{\"stageCode\":\"" + stage.getCode().name()
                + "\",\"pass\":" + result.pass()
                + ",\"failedRules\":" + listToJson(result.failedRules()) + "}";
    }

    private StageGateResult evaluatePreparatory(MethodologicalStage stage) {
        List<String> failed = new ArrayList<>();
        for (StageDeliverableCode code : StageDeliverableCode.forStage(MethodologicalStageCode.PREPARATORY)) {
            StageDeliverable deliverable = findDeliverable(stage, code);
            if (deliverable == null || deliverable.getApprovalStatus() != StageDeliverableApprovalStatus.APPROVED) {
                failed.add("Entregable obligatorio pendiente: " + code.name());
            }
        }
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

        for (StageDeliverableCode code : StageDeliverableCode.forStage(MethodologicalStageCode.COLLECTION)) {
            StageDeliverable deliverable = findDeliverable(stage, code);
            if (deliverable == null || deliverable.getApprovalStatus() != StageDeliverableApprovalStatus.APPROVED) {
                failed.add("Entregable obligatorio pendiente: " + code.name());
            }
        }

        if (failed.isEmpty()) {
            return StageGateResult.pass("Compuerta E2 satisfecha: evidencias y encuestas completas.");
        }
        return StageGateResult.block(failed, "Compuerta E2 bloqueada.");
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
