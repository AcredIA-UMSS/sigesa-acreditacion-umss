package com.umss.sigesa.application.service.workflow;

import com.umss.sigesa.application.port.in.RejectIndicatorUseCase;
import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.application.port.out.NormativeIndicatorEvidenceQueryPort;
import com.umss.sigesa.application.port.out.NormativeIndicatorObservationPort;
import com.umss.sigesa.application.port.out.NormativeIndicatorWorkflowPort;
import com.umss.sigesa.application.port.out.NotificationOutboxPort;
import com.umss.sigesa.domain.exception.EvidenceRequiredException;
import com.umss.sigesa.domain.exception.IndicatorNotFoundException;
import com.umss.sigesa.domain.exception.InvalidIndicatorStateException;
import com.umss.sigesa.domain.exception.InvalidRoleException;
import com.umss.sigesa.domain.exception.JustificationRequiredException;
import com.umss.sigesa.domain.model.IndicatorObservation;
import com.umss.sigesa.domain.model.IndicatorState;
import com.umss.sigesa.domain.model.IndicatorWorkflowResult;
import com.umss.sigesa.domain.model.IndicatorObservationStatus;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class RejectIndicatorService implements RejectIndicatorUseCase {

    private static final int MIN_JUSTIFICATION_LENGTH = 20;
    private static final Set<String> ALLOWED_ROLES = Set.of("TD");
    private static final Set<IndicatorState> REVIEWABLE_STATES =
            EnumSet.of(IndicatorState.SUBIDO, IndicatorState.SUBSANADO);

    private final NormativeHierarchyQueryPort hierarchyQueryPort;
    private final NormativeIndicatorEvidenceQueryPort evidenceQueryPort;
    private final NormativeIndicatorObservationPort observationPort;
    private final NormativeIndicatorWorkflowPort workflowPort;
    private final NotificationOutboxPort notificationOutbox;

    public RejectIndicatorService(
            NormativeHierarchyQueryPort hierarchyQueryPort,
            NormativeIndicatorEvidenceQueryPort evidenceQueryPort,
            NormativeIndicatorObservationPort observationPort,
            NormativeIndicatorWorkflowPort workflowPort,
            NotificationOutboxPort notificationOutbox) {
        this.hierarchyQueryPort = hierarchyQueryPort;
        this.evidenceQueryPort = evidenceQueryPort;
        this.observationPort = observationPort;
        this.workflowPort = workflowPort;
        this.notificationOutbox = notificationOutbox;
    }

    @Override
    public IndicatorWorkflowResult reject(UUID indicatorId, String justification, UUID actorId, String actorRole) {
        assertRole(actorRole);
        validateJustification(justification);

        NormativeHierarchyQueryPort.NormativeIndicatorContext context = hierarchyQueryPort
                .findIndicatorContext(indicatorId)
                .orElseThrow(() -> new IndicatorNotFoundException(indicatorId));

        if (!evidenceQueryPort.hasEvidences(indicatorId)) {
            throw new EvidenceRequiredException(
                    "No se puede rechazar: el indicador no tiene evidencia cargada.");
        }

        observationPort.findLatestOpenByIndicatorId(indicatorId).ifPresent(existing -> {
            throw new InvalidIndicatorStateException(
                    "Ya existe una observación pendiente de subsanación en este indicador.");
        });

        IndicatorState currentState = context.status();
        if (!REVIEWABLE_STATES.contains(currentState)) {
            throw new InvalidIndicatorStateException(
                    "Indicador " + indicatorId + " en estado " + currentState
                            + "; se requiere " + REVIEWABLE_STATES);
        }

        LocalDateTime now = LocalDateTime.now();
        UUID observationId = UUID.randomUUID();
        IndicatorObservation observation = IndicatorObservation.builder()
                .id(observationId)
                .indicatorId(indicatorId)
                .authorId(actorId)
                .authorRole(actorRole.trim().toUpperCase(Locale.ROOT))
                .body(justification.trim())
                .status(IndicatorObservationStatus.OPEN)
                .createdAt(now)
                .updatedAt(now)
                .build();
        observationPort.save(observation);

        workflowPort.updateIndicatorStatus(indicatorId, IndicatorState.OBSERVADO);

        notificationOutbox.enqueue(
                "IndicatorRejected",
                context.careerId(),
                Map.of(
                        "indicatorId", indicatorId.toString(),
                        "observationId", observationId.toString(),
                        "newState", IndicatorState.OBSERVADO.name()));

        return new IndicatorWorkflowResult(
                indicatorId,
                currentState,
                IndicatorState.OBSERVADO,
                null,
                observationId);
    }

    private static void assertRole(String actorRole) {
        String normalized = actorRole != null ? actorRole.trim().toUpperCase(Locale.ROOT) : "";
        if (!ALLOWED_ROLES.contains(normalized)) {
            throw new InvalidRoleException("Solo el técnico [TD] puede rechazar indicadores.");
        }
    }

    private static void validateJustification(String justification) {
        if (justification == null || justification.trim().length() < MIN_JUSTIFICATION_LENGTH) {
            throw new JustificationRequiredException(
                    "La justificación debe tener al menos " + MIN_JUSTIFICATION_LENGTH + " caracteres.");
        }
    }
}
