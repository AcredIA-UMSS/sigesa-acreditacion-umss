package com.umss.sigesa.application.service.workflow;

import com.umss.sigesa.application.port.in.RejectIndicatorUseCase;
import com.umss.sigesa.application.port.out.IndicatorRepositoryPort;
import com.umss.sigesa.application.port.out.NotificationOutboxPort;
import com.umss.sigesa.application.port.out.SubphaseEvidenceQueryPort;
import com.umss.sigesa.application.port.out.SubphaseObservationPort;
import com.umss.sigesa.domain.exception.EvidenceRequiredException;
import com.umss.sigesa.domain.exception.InvalidRoleException;
import com.umss.sigesa.domain.exception.IndicatorNotFoundException;
import com.umss.sigesa.domain.exception.JustificationRequiredException;
import com.umss.sigesa.domain.model.Indicator;
import com.umss.sigesa.domain.model.IndicatorState;
import com.umss.sigesa.domain.model.IndicatorTransitionResult;
import com.umss.sigesa.domain.model.IndicatorWorkflowResult;
import com.umss.sigesa.domain.model.Role;
import com.umss.sigesa.domain.model.SubphaseObservation;
import com.umss.sigesa.domain.model.SubphaseObservationStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class RejectIndicatorService implements RejectIndicatorUseCase {

    private static final int MIN_JUSTIFICATION_LENGTH = 20;
    private static final Set<String> ALLOWED_ROLES = Set.of("TD");

    private final IndicatorRepositoryPort indicatorRepository;
    private final SubphaseEvidenceQueryPort evidenceQueryPort;
    private final SubphaseObservationPort observationPort;
    private final IndicatorTransitionHelper transitionHelper;
    private final NotificationOutboxPort notificationOutbox;

    public RejectIndicatorService(IndicatorRepositoryPort indicatorRepository,
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
    public IndicatorWorkflowResult reject(UUID indicatorId, String justification, UUID actorId, String actorRole) {
        assertRole(actorRole);
        validateJustification(justification);

        Indicator indicator = indicatorRepository.findById(indicatorId)
                .orElseThrow(() -> new IndicatorNotFoundException(indicatorId));

        if (!evidenceQueryPort.hasEvidenceForIndicator(indicatorId)) {
            throw new EvidenceRequiredException(
                    "No se puede rechazar: el indicador no tiene evidencia cargada.");
        }

        UUID observationId = createObservationIfSubphaseLinked(indicatorId, justification, actorId, actorRole);

        IndicatorTransitionResult transition = transitionHelper.transition(
                indicatorId,
                IndicatorState.OBSERVADO,
                actorId,
                Role.TD,
                IndicatorTransitionHelper.REVIEWABLE_STATES);

        notificationOutbox.enqueue(
                "IndicatorRejected",
                indicator.getProgramId(),
                Map.of(
                        "indicatorId", indicatorId.toString(),
                        "observationId", observationId != null ? observationId.toString() : ""));

        return new IndicatorWorkflowResult(
                indicatorId,
                transition.previousState(),
                transition.newState(),
                transition.stateHistoryId(),
                observationId);
    }

    private UUID createObservationIfSubphaseLinked(
            UUID indicatorId, String justification, UUID actorId, String actorRole) {
        List<UUID> subphaseIds = evidenceQueryPort.findSubphaseIdsByIndicatorId(indicatorId);
        if (subphaseIds.isEmpty()) {
            return null;
        }
        UUID subphaseId = subphaseIds.getFirst();
        observationPort.findLatestOpenBySubphaseId(subphaseId).ifPresent(existing -> {
            throw new IllegalStateException(
                    "Ya existe una observación pendiente en la subfase vinculada.");
        });
        LocalDateTime now = LocalDateTime.now();
        SubphaseObservation observation = SubphaseObservation.builder()
                .id(UUID.randomUUID())
                .subphaseId(subphaseId)
                .authorId(actorId)
                .authorRole(actorRole.trim().toUpperCase(Locale.ROOT))
                .body(justification.trim())
                .status(SubphaseObservationStatus.OPEN)
                .createdAt(now)
                .updatedAt(now)
                .build();
        return observationPort.save(observation).getId();
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
