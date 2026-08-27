package com.umss.sigesa.application.service.workflow;

import com.umss.sigesa.application.port.in.ApproveIndicatorUseCase;
import com.umss.sigesa.application.port.out.IndicatorRepositoryPort;
import com.umss.sigesa.application.port.out.NotificationOutboxPort;
import com.umss.sigesa.application.port.out.SubphaseEvidenceQueryPort;
import com.umss.sigesa.application.port.out.SubphaseObservationPort;
import com.umss.sigesa.domain.exception.EvidenceRequiredException;
import com.umss.sigesa.domain.exception.InvalidRoleException;
import com.umss.sigesa.domain.exception.IndicatorNotFoundException;
import com.umss.sigesa.domain.exception.SubsanationNotAllowedException;
import com.umss.sigesa.domain.model.Indicator;
import com.umss.sigesa.domain.model.IndicatorState;
import com.umss.sigesa.domain.model.IndicatorTransitionResult;
import com.umss.sigesa.domain.model.IndicatorWorkflowResult;
import com.umss.sigesa.domain.model.Role;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ApproveIndicatorService implements ApproveIndicatorUseCase {

    private static final Set<String> ALLOWED_ROLES = Set.of("TD");

    private final IndicatorRepositoryPort indicatorRepository;
    private final SubphaseEvidenceQueryPort evidenceQueryPort;
    private final SubphaseObservationPort observationPort;
    private final IndicatorTransitionHelper transitionHelper;
    private final NotificationOutboxPort notificationOutbox;

    public ApproveIndicatorService(IndicatorRepositoryPort indicatorRepository,
                                   SubphaseEvidenceQueryPort evidenceQueryPort,
                                   SubphaseObservationPort observationPort,
                                   IndicatorTransitionHelper transitionHelper,
                                   NotificationOutboxPort notificationOutbox) {
        this.indicatorRepository = indicatorRepository;
        this.evidenceQueryPort = evidenceQueryPort;
        this.observationPort = observationPort;
        this.transitionHelper = transitionHelper;
        this.notificationOutbox = notificationOutbox;
    }

    @Override
    public IndicatorWorkflowResult approve(UUID indicatorId, UUID actorId, String actorRole) {
        assertRole(actorRole);
        Indicator indicator = indicatorRepository.findById(indicatorId)
                .orElseThrow(() -> new IndicatorNotFoundException(indicatorId));

        if (!evidenceQueryPort.hasEvidenceForIndicator(indicatorId)) {
            throw new EvidenceRequiredException(
                    "No se puede aprobar: el indicador no tiene evidencia cargada.");
        }

        assertNoOpenObservations(indicatorId);

        IndicatorTransitionResult transition = transitionHelper.transition(
                indicatorId,
                IndicatorState.APROBADO,
                actorId,
                Role.TD,
                IndicatorTransitionHelper.REVIEWABLE_STATES);

        notificationOutbox.enqueue(
                "IndicatorApproved",
                indicator.getProgramId(),
                Map.of("indicatorId", indicatorId.toString()));

        return new IndicatorWorkflowResult(
                indicatorId,
                transition.previousState(),
                transition.newState(),
                transition.stateHistoryId(),
                null);
    }

    private void assertNoOpenObservations(UUID indicatorId) {
        List<UUID> subphaseIds = evidenceQueryPort.findSubphaseIdsByIndicatorId(indicatorId);
        for (UUID subphaseId : subphaseIds) {
            observationPort.findLatestOpenBySubphaseId(subphaseId).ifPresent(open -> {
                throw new SubsanationNotAllowedException(
                        "Hay una observación pendiente en la subfase vinculada.");
            });
        }
    }

    private static void assertRole(String actorRole) {
        String normalized = actorRole != null ? actorRole.trim().toUpperCase(Locale.ROOT) : "";
        if (!ALLOWED_ROLES.contains(normalized)) {
            throw new InvalidRoleException("Solo el técnico [TD] puede aprobar indicadores.");
        }
    }
}
