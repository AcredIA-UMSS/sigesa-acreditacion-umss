package com.umss.sigesa.application.service.workflow;

import com.umss.sigesa.application.port.in.SubmitStageForReviewUseCase;
import com.umss.sigesa.application.port.out.MethodologicalStagePort;
import com.umss.sigesa.domain.exception.InvalidRoleException;
import com.umss.sigesa.domain.exception.InvalidStageStateException;
import com.umss.sigesa.domain.exception.StageNotFoundException;
import com.umss.sigesa.domain.model.MethodologicalStage;
import com.umss.sigesa.domain.model.MethodologicalStageStatus;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class SubmitStageForReviewService implements SubmitStageForReviewUseCase {

    private static final Set<String> ALLOWED_ROLES = Set.of("CC");

    private final MethodologicalStagePort stagePort;

    public SubmitStageForReviewService(MethodologicalStagePort stagePort) {
        this.stagePort = stagePort;
    }

    @Override
    public MethodologicalStage submit(UUID processId, UUID stageId, UUID actorId, String actorRole) {
        assertRole(actorRole);

        MethodologicalStage stage = stagePort.findByIdAndProcessId(stageId, processId)
                .orElseThrow(() -> new StageNotFoundException("Etapa no encontrada en el proceso."));

        if (stage.getStatus() != MethodologicalStageStatus.IN_PROGRESS
                && stage.getStatus() != MethodologicalStageStatus.OBSERVED) {
            throw new InvalidStageStateException(
                    "Solo se puede enviar a revisión una etapa IN_PROGRESS u OBSERVED.");
        }

        stagePort.updateStageStatus(stageId, MethodologicalStageStatus.SUBMITTED_FOR_REVIEW);
        return reload(processId, stageId);
    }

    private MethodologicalStage reload(UUID processId, UUID stageId) {
        return stagePort.findByIdAndProcessId(stageId, processId)
                .orElseThrow(() -> new StageNotFoundException("Etapa no encontrada tras actualización."));
    }

    private static void assertRole(String actorRole) {
        String normalized = actorRole != null ? actorRole.trim().toUpperCase(Locale.ROOT) : "";
        if (!ALLOWED_ROLES.contains(normalized)) {
            throw new InvalidRoleException("Solo el coordinador [CC] puede enviar etapas a revisión.");
        }
    }
}
