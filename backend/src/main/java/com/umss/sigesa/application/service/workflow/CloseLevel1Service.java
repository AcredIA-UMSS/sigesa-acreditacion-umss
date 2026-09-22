package com.umss.sigesa.application.service.workflow;

import com.umss.sigesa.application.port.in.CloseLevel1UseCase;
import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.application.port.out.NormativeIndicatorWorkflowPort;
import com.umss.sigesa.application.port.out.NotificationOutboxPort;
import com.umss.sigesa.domain.exception.InvalidLevel1StateException;
import com.umss.sigesa.domain.exception.InvalidRoleException;
import com.umss.sigesa.domain.exception.Level1ClosureBlockedException;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.IndicatorState;
import com.umss.sigesa.domain.model.Level1CompleteResult;
import com.umss.sigesa.domain.model.PendingIndicator;
import com.umss.sigesa.domain.model.PhaseState;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CloseLevel1Service implements CloseLevel1UseCase {

    private static final Set<String> ALLOWED_ROLES = Set.of("TD");

    private final NormativeHierarchyQueryPort hierarchyQueryPort;
    private final NormativeIndicatorWorkflowPort workflowPort;
    private final NotificationOutboxPort notificationOutbox;

    public CloseLevel1Service(NormativeHierarchyQueryPort hierarchyQueryPort,
                              NormativeIndicatorWorkflowPort workflowPort,
                              NotificationOutboxPort notificationOutbox) {
        this.hierarchyQueryPort = hierarchyQueryPort;
        this.workflowPort = workflowPort;
        this.notificationOutbox = notificationOutbox;
    }

    @Override
    public Level1CompleteResult close(UUID processId, UUID level1Id, UUID actorId, String actorRole) {
        assertRole(actorRole);

        NormativeHierarchyQueryPort.Level1Context context = hierarchyQueryPort.findLevel1Context(level1Id)
                .filter(item -> item.processId().equals(processId))
                .orElseThrow(() -> new ProcessNotFoundException(
                        "Nivel 1 no encontrado en el proceso: " + level1Id));

        if (context.status() == PhaseState.COMPLETADA) {
            throw new InvalidLevel1StateException("El Nivel 1 ya está completado.");
        }

        List<NormativeHierarchyQueryPort.IndicatorStatusItem> indicators =
                hierarchyQueryPort.listIndicatorsWithStatusByLevel1Id(level1Id);

        List<PendingIndicator> pending = indicators.stream()
                .filter(item -> item.status() != IndicatorState.APROBADO)
                .map(item -> new PendingIndicator(
                        item.indicatorId(),
                        item.code(),
                        item.name(),
                        item.status(),
                        item.order()))
                .toList();

        if (!pending.isEmpty()) {
            throw new Level1ClosureBlockedException(
                    "No se puede cerrar el Nivel 1: hay indicadores pendientes de aprobación.",
                    pending);
        }

        workflowPort.updateLevel1Status(level1Id, PhaseState.COMPLETADA);

        notificationOutbox.enqueue(
                "Level1Completed",
                context.careerId(),
                Map.of(
                        "level1Id", level1Id.toString(),
                        "processId", processId.toString(),
                        "newState", PhaseState.COMPLETADA.name()));

        return new Level1CompleteResult(
                level1Id,
                context.status(),
                PhaseState.COMPLETADA,
                "Level1Completed");
    }

    private static void assertRole(String actorRole) {
        String normalized = actorRole != null ? actorRole.trim().toUpperCase(Locale.ROOT) : "";
        if (!ALLOWED_ROLES.contains(normalized)) {
            throw new InvalidRoleException("Solo el técnico [TD] puede cerrar niveles 1.");
        }
    }
}
