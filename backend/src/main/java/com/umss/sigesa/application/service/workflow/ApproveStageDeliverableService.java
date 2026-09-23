package com.umss.sigesa.application.service.workflow;

import com.umss.sigesa.application.port.in.ApproveStageDeliverableUseCase;
import com.umss.sigesa.application.port.out.MethodologicalStagePort;
import com.umss.sigesa.domain.exception.InvalidRoleException;
import com.umss.sigesa.domain.exception.StageNotFoundException;
import com.umss.sigesa.domain.model.StageDeliverable;
import com.umss.sigesa.domain.model.StageDeliverableApprovalStatus;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class ApproveStageDeliverableService implements ApproveStageDeliverableUseCase {

    private static final Set<String> ALLOWED_ROLES = Set.of("TD");

    private final MethodologicalStagePort stagePort;

    public ApproveStageDeliverableService(MethodologicalStagePort stagePort) {
        this.stagePort = stagePort;
    }

    @Override
    public StageDeliverable approve(
            UUID processId,
            UUID stageId,
            UUID deliverableId,
            UUID actorId,
            String actorRole) {
        assertRole(actorRole);

        stagePort.findByIdAndProcessId(stageId, processId)
                .orElseThrow(() -> new StageNotFoundException("Etapa no encontrada en el proceso."));

        StageDeliverable deliverable = stagePort.findDeliverableById(deliverableId)
                .filter(item -> item.getStageId().equals(stageId))
                .orElseThrow(() -> new StageNotFoundException("Entregable no encontrado en la etapa."));

        stagePort.updateDeliverableApproval(
                deliverableId,
                StageDeliverableApprovalStatus.APPROVED,
                actorId,
                null);

        return stagePort.findDeliverableById(deliverableId)
                .orElseThrow(() -> new StageNotFoundException("Entregable no encontrado tras aprobación."));
    }

    private static void assertRole(String actorRole) {
        String normalized = actorRole != null ? actorRole.trim().toUpperCase(Locale.ROOT) : "";
        if (!ALLOWED_ROLES.contains(normalized)) {
            throw new InvalidRoleException("Solo [TD] puede aprobar entregables de etapa.");
        }
    }
}
