package com.umss.sigesa.application.service.workflow;

import com.umss.sigesa.application.port.in.RejectSubphaseIndicatorUseCase;
import com.umss.sigesa.application.port.out.NotificationOutboxPort;
import com.umss.sigesa.application.port.out.SubphaseEvidenceQueryPort;
import com.umss.sigesa.application.port.out.SubphaseObservationPort;
import com.umss.sigesa.application.port.out.SubphaseQueryPort;
import com.umss.sigesa.domain.exception.EvidenceRequiredException;
import com.umss.sigesa.domain.exception.InvalidRoleException;
import com.umss.sigesa.domain.exception.JustificationRequiredException;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.SubphaseObservation;
import com.umss.sigesa.domain.model.SubphaseObservationStatus;
import com.umss.sigesa.domain.model.SubphaseRejectResult;
import com.umss.sigesa.domain.model.SubphaseState;
import com.umss.sigesa.domain.model.SubphaseTransitionResult;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class RejectSubphaseIndicatorService implements RejectSubphaseIndicatorUseCase {

    private static final int MIN_JUSTIFICATION_LENGTH = 20;
    private static final Set<String> ALLOWED_ROLES = Set.of("TD");

    private final SubphaseQueryPort subphaseQueryPort;
    private final SubphaseEvidenceQueryPort evidenceQueryPort;
    private final SubphaseObservationPort observationPort;
    private final SubphaseTransitionHelper transitionHelper;
    private final NotificationOutboxPort notificationOutbox;

    public RejectSubphaseIndicatorService(SubphaseQueryPort subphaseQueryPort,
                                          SubphaseEvidenceQueryPort evidenceQueryPort,
                                          SubphaseObservationPort observationPort,
                                          SubphaseTransitionHelper transitionHelper,
                                          NotificationOutboxPort notificationOutbox) {
        this.subphaseQueryPort = subphaseQueryPort;
        this.evidenceQueryPort = evidenceQueryPort;
        this.observationPort = observationPort;
        this.transitionHelper = transitionHelper;
        this.notificationOutbox = notificationOutbox;
    }

    @Override
    public SubphaseRejectResult reject(UUID subphaseId, String justification, UUID actorId, String actorRole) {
        assertRole(actorRole);
        validateJustification(justification);

        SubphaseQueryPort.SubphaseContext context = subphaseQueryPort.findContext(subphaseId)
                .orElseThrow(() -> new ProcessNotFoundException("Subfase no encontrada: " + subphaseId));

        if (!evidenceQueryPort.hasEvidences(subphaseId)) {
            throw new EvidenceRequiredException(
                    "No se puede rechazar: la subfase no tiene evidencias cargadas.");
        }

        observationPort.findLatestOpenBySubphaseId(subphaseId).ifPresent(existing -> {
            throw new IllegalStateException(
                    "Ya existe una observación pendiente de subsanación en esta subfase.");
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
        SubphaseObservation saved = observationPort.save(observation);

        SubphaseTransitionResult transition = transitionHelper.transition(
                subphaseId,
                SubphaseState.OBSERVADO,
                SubphaseTransitionHelper.REVIEWABLE_STATES);

        notificationOutbox.enqueue(
                "SubphaseRejected",
                context.careerId(),
                Map.of(
                        "subphaseId", subphaseId.toString(),
                        "observationId", saved.getId().toString(),
                        "newState", SubphaseState.OBSERVADO.name()));

        return new SubphaseRejectResult(subphaseId, saved.getId(), transition);
    }

    private static void assertRole(String actorRole) {
        String normalized = actorRole != null ? actorRole.trim().toUpperCase(Locale.ROOT) : "";
        if (!ALLOWED_ROLES.contains(normalized)) {
            throw new InvalidRoleException("Solo el técnico [TD] puede rechazar subfases.");
        }
    }

    private static void validateJustification(String justification) {
        if (justification == null || justification.trim().length() < MIN_JUSTIFICATION_LENGTH) {
            throw new JustificationRequiredException(
                    "La justificación debe tener al menos " + MIN_JUSTIFICATION_LENGTH + " caracteres.");
        }
    }
}
