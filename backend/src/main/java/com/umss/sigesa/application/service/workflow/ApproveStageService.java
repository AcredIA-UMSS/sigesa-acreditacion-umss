package com.umss.sigesa.application.service.workflow;

import com.umss.sigesa.application.port.in.ApproveStageUseCase;
import com.umss.sigesa.application.port.out.MethodologicalStagePort;
import com.umss.sigesa.domain.exception.InvalidRoleException;
import com.umss.sigesa.domain.exception.InvalidStageStateException;
import com.umss.sigesa.domain.exception.StageGateBlockedException;
import com.umss.sigesa.domain.exception.StageNotFoundException;
import com.umss.sigesa.domain.model.MethodologicalStage;
import com.umss.sigesa.domain.model.MethodologicalStageStatus;
import com.umss.sigesa.domain.model.OperationalMode;
import com.umss.sigesa.domain.model.StageGateResult;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class ApproveStageService implements ApproveStageUseCase {

    private static final Set<String> ALLOWED_ROLES = Set.of("TD", "JD");

    private final MethodologicalStagePort stagePort;
    private final StageGateEvaluator gateEvaluator;

    public ApproveStageService(MethodologicalStagePort stagePort, StageGateEvaluator gateEvaluator) {
        this.stagePort = stagePort;
        this.gateEvaluator = gateEvaluator;
    }

    @Override
    public MethodologicalStage approve(UUID processId, UUID stageId, UUID actorId, String actorRole) {
        assertRole(actorRole);

        MethodologicalStage stage = stagePort.findByIdAndProcessId(stageId, processId)
                .orElseThrow(() -> new StageNotFoundException("Etapa no encontrada en el proceso."));

        if (stage.getStatus() != MethodologicalStageStatus.SUBMITTED_FOR_REVIEW) {
            throw new InvalidStageStateException("Solo se puede aprobar una etapa en SUBMITTED_FOR_REVIEW.");
        }

        StageGateResult gateResult = gateEvaluator.evaluate(stage);
        stagePort.saveGateEvaluation(
                stageId,
                actorId,
                gateResult,
                gateEvaluator.toRulesSnapshotJson(stage, gateResult));

        if (!gateResult.pass()) {
            throw new StageGateBlockedException(gateResult.summary(), gateResult);
        }

        stagePort.updateStageStatus(stageId, MethodologicalStageStatus.APPROVED);
        stagePort.markStageClosed(stageId);

        stagePort.findNextStage(processId, stage.getOrder()).ifPresent(next -> {
            stagePort.updateStageStatus(next.getId(), MethodologicalStageStatus.IN_PROGRESS);
            stagePort.markStageStarted(next.getId());
            stagePort.updateProcessCurrentStage(processId, next.getId(), OperationalMode.ACTIVE);
        });

        return stagePort.findByIdAndProcessId(stageId, processId)
                .orElseThrow(() -> new StageNotFoundException("Etapa no encontrada tras aprobación."));
    }

    private static void assertRole(String actorRole) {
        String normalized = actorRole != null ? actorRole.trim().toUpperCase(Locale.ROOT) : "";
        if (!ALLOWED_ROLES.contains(normalized)) {
            throw new InvalidRoleException("Solo [TD] o [JD] pueden aprobar etapas metodológicas.");
        }
    }
}
