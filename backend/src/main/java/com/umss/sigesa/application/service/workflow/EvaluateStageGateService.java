package com.umss.sigesa.application.service.workflow;

import com.umss.sigesa.application.port.in.EvaluateStageGateUseCase;
import com.umss.sigesa.application.port.out.MethodologicalStagePort;
import com.umss.sigesa.domain.exception.InvalidRoleException;
import com.umss.sigesa.domain.exception.StageNotFoundException;
import com.umss.sigesa.domain.model.MethodologicalStage;
import com.umss.sigesa.domain.model.StageGateResult;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class EvaluateStageGateService implements EvaluateStageGateUseCase {

    private static final Set<String> ALLOWED_ROLES = Set.of("TD");

    private final MethodologicalStagePort stagePort;
    private final StageGateEvaluator gateEvaluator;

    public EvaluateStageGateService(MethodologicalStagePort stagePort, StageGateEvaluator gateEvaluator) {
        this.stagePort = stagePort;
        this.gateEvaluator = gateEvaluator;
    }

    @Override
    public StageGateResult evaluate(UUID processId, UUID stageId, UUID actorId, String actorRole) {
        assertRole(actorRole);

        MethodologicalStage stage = stagePort.findByIdAndProcessId(stageId, processId)
                .orElseThrow(() -> new StageNotFoundException("Etapa no encontrada en el proceso."));

        return gateEvaluator.evaluate(stage);
    }

    private static void assertRole(String actorRole) {
        String normalized = actorRole != null ? actorRole.trim().toUpperCase(Locale.ROOT) : "";
        if (!ALLOWED_ROLES.contains(normalized)) {
            throw new InvalidRoleException("Solo [TD] puede evaluar compuertas de etapa.");
        }
    }
}
