package com.umss.sigesa.application.service.workflow;

import com.umss.sigesa.application.port.in.ClosePhaseUseCase;
import com.umss.sigesa.application.port.out.NotificationOutboxPort;
import com.umss.sigesa.application.port.out.PhaseWorkflowPort;
import com.umss.sigesa.domain.exception.InvalidPhaseStateException;
import com.umss.sigesa.domain.exception.InvalidRoleException;
import com.umss.sigesa.domain.exception.PhaseClosureBlockedException;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.PendingSubphase;
import com.umss.sigesa.domain.model.PhaseCompleteResult;
import com.umss.sigesa.domain.model.PhaseState;
import com.umss.sigesa.domain.model.SubphaseState;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ClosePhaseService implements ClosePhaseUseCase {

    private static final Set<String> ALLOWED_ROLES = Set.of("TD");

    private final PhaseWorkflowPort phaseWorkflowPort;
    private final NotificationOutboxPort notificationOutbox;

    public ClosePhaseService(PhaseWorkflowPort phaseWorkflowPort,
                             NotificationOutboxPort notificationOutbox) {
        this.phaseWorkflowPort = phaseWorkflowPort;
        this.notificationOutbox = notificationOutbox;
    }

    @Override
    public PhaseCompleteResult close(UUID processId, UUID phaseId, UUID actorId, String actorRole) {
        assertRole(actorRole);

        PhaseWorkflowPort.PhaseContext context = phaseWorkflowPort.findPhaseContext(processId, phaseId)
                .orElseThrow(() -> new ProcessNotFoundException(
                        "Fase no encontrada en el proceso: " + phaseId));

        PhaseState currentState = phaseWorkflowPort.getCurrentState(phaseId);
        if (currentState == PhaseState.COMPLETADA) {
            throw new InvalidPhaseStateException("La fase ya está completada.");
        }

        List<PhaseWorkflowPort.SubphaseStatusItem> subphases =
                phaseWorkflowPort.listSubphasesWithStatus(phaseId);

        List<PendingSubphase> pending = subphases.stream()
                .filter(item -> item.status() != SubphaseState.APROBADO)
                .map(item -> new PendingSubphase(
                        item.subphaseId(),
                        item.name(),
                        item.status(),
                        item.order()))
                .toList();

        if (!pending.isEmpty()) {
            throw new PhaseClosureBlockedException(
                    "No se puede cerrar la fase: hay subfases pendientes de aprobación.",
                    pending);
        }

        phaseWorkflowPort.updateState(phaseId, PhaseState.COMPLETADA);

        notificationOutbox.enqueue(
                "PhaseCompleted",
                context.careerId(),
                Map.of(
                        "phaseId", phaseId.toString(),
                        "processId", processId.toString(),
                        "newState", PhaseState.COMPLETADA.name()));

        return new PhaseCompleteResult(
                phaseId,
                currentState,
                PhaseState.COMPLETADA,
                "PhaseCompleted");
    }

    private static void assertRole(String actorRole) {
        String normalized = actorRole != null ? actorRole.trim().toUpperCase(Locale.ROOT) : "";
        if (!ALLOWED_ROLES.contains(normalized)) {
            throw new InvalidRoleException("Solo el técnico [TD] puede cerrar fases.");
        }
    }
}
