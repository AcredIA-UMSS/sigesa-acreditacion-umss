package com.umss.sigesa.application.service.workflow;

import com.umss.sigesa.application.port.in.ApproveIndicatorUseCase;
import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.application.port.out.NormativeIndicatorEvidenceQueryPort;
import com.umss.sigesa.application.port.out.NormativeIndicatorObservationPort;
import com.umss.sigesa.application.port.out.NormativeIndicatorWorkflowPort;
import com.umss.sigesa.application.port.out.NotificationOutboxPort;
import com.umss.sigesa.domain.exception.EvidenceRequiredException;
import com.umss.sigesa.domain.exception.IndicatorNotFoundException;
import com.umss.sigesa.domain.exception.InvalidIndicatorStateException;
import com.umss.sigesa.domain.exception.InvalidRoleException;
import com.umss.sigesa.domain.exception.SubsanationNotAllowedException;
import com.umss.sigesa.domain.model.IndicatorState;
import com.umss.sigesa.domain.model.IndicatorWorkflowResult;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ApproveIndicatorService implements ApproveIndicatorUseCase {

    private static final Set<String> ALLOWED_ROLES = Set.of("TD");
    private static final Set<IndicatorState> REVIEWABLE_STATES =
            EnumSet.of(IndicatorState.SUBIDO, IndicatorState.SUBSANADO);

    private final NormativeHierarchyQueryPort hierarchyQueryPort;
    private final NormativeIndicatorEvidenceQueryPort evidenceQueryPort;
    private final NormativeIndicatorObservationPort observationPort;
    private final NormativeIndicatorWorkflowPort workflowPort;
    private final NotificationOutboxPort notificationOutbox;

    public ApproveIndicatorService(
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
    public IndicatorWorkflowResult approve(UUID indicatorId, UUID actorId, String actorRole) {
        assertRole(actorRole);

        NormativeHierarchyQueryPort.NormativeIndicatorContext context = hierarchyQueryPort
                .findIndicatorContext(indicatorId)
                .orElseThrow(() -> new IndicatorNotFoundException(indicatorId));

        if (!evidenceQueryPort.hasEvidences(indicatorId)) {
            throw new EvidenceRequiredException(
                    "No se puede aprobar: el indicador no tiene evidencia cargada.");
        }

        observationPort.findLatestOpenByIndicatorId(indicatorId).ifPresent(open -> {
            throw new SubsanationNotAllowedException(
                    "Hay una observación pendiente; debe subsanarse antes de aprobar.");
        });

        IndicatorState currentState = context.status();
        if (!REVIEWABLE_STATES.contains(currentState)) {
            throw new InvalidIndicatorStateException(
                    "Indicador " + indicatorId + " en estado " + currentState
                            + "; se requiere " + REVIEWABLE_STATES);
        }

        workflowPort.updateIndicatorStatus(indicatorId, IndicatorState.APROBADO);

        notificationOutbox.enqueue(
                "IndicatorApproved",
                context.careerId(),
                Map.of(
                        "indicatorId", indicatorId.toString(),
                        "newState", IndicatorState.APROBADO.name()));

        return new IndicatorWorkflowResult(
                indicatorId,
                currentState,
                IndicatorState.APROBADO,
                null,
                null);
    }

    private static void assertRole(String actorRole) {
        String normalized = actorRole != null ? actorRole.trim().toUpperCase(Locale.ROOT) : "";
        if (!ALLOWED_ROLES.contains(normalized)) {
            throw new InvalidRoleException("Solo el técnico [TD] puede aprobar indicadores.");
        }
    }
}
