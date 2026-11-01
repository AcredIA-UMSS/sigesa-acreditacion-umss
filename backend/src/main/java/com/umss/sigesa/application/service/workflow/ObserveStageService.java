package com.umss.sigesa.application.service.workflow;

import com.umss.sigesa.application.port.in.ObserveStageUseCase;
import com.umss.sigesa.application.port.out.MethodologicalStagePort;
import com.umss.sigesa.domain.exception.InvalidRoleException;
import com.umss.sigesa.domain.exception.InvalidStageStateException;
import com.umss.sigesa.domain.exception.StageNotFoundException;
import com.umss.sigesa.domain.model.MethodologicalStage;
import com.umss.sigesa.domain.model.MethodologicalStageStatus;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class ObserveStageService implements ObserveStageUseCase {

    private static final Set<String> ALLOWED_ROLES = Set.of("TD", "JD");

    private final MethodologicalStagePort stagePort;

    public ObserveStageService(MethodologicalStagePort stagePort) {
        this.stagePort = stagePort;
    }

    @Override
    public MethodologicalStage observe(
            UUID processId,
            UUID stageId,
            UUID actorId,
            String actorRole,
            String observations) {
        assertRole(actorRole);

        MethodologicalStage stage = stagePort.findByIdAndProcessId(stageId, processId)
                .orElseThrow(() -> new StageNotFoundException("Etapa no encontrada en el proceso."));

        if (stage.getStatus() != MethodologicalStageStatus.SUBMITTED_FOR_REVIEW) {
            throw new InvalidStageStateException("Solo se puede observar una etapa en SUBMITTED_FOR_REVIEW.");
        }

        stagePort.updateStageStatus(stageId, MethodologicalStageStatus.OBSERVED);
        return stagePort.findByIdAndProcessId(stageId, processId)
                .orElseThrow(() -> new StageNotFoundException("Etapa no encontrada tras observación."));
    }

    private static void assertRole(String actorRole) {
        String normalized = actorRole != null ? actorRole.trim().toUpperCase(Locale.ROOT) : "";
        if (!ALLOWED_ROLES.contains(normalized)) {
            throw new InvalidRoleException("Solo [TD] o [JD] pueden observar etapas metodológicas.");
        }
    }
}
