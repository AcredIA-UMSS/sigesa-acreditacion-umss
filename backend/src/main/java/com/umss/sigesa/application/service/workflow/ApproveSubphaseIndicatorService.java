package com.umss.sigesa.application.service.workflow;

import com.umss.sigesa.application.port.in.ApproveSubphaseIndicatorUseCase;
import com.umss.sigesa.application.port.out.NotificationOutboxPort;
import com.umss.sigesa.application.port.out.SubphaseEvidenceQueryPort;
import com.umss.sigesa.application.port.out.SubphaseObservationPort;
import com.umss.sigesa.application.port.out.SubphaseQueryPort;
import com.umss.sigesa.domain.exception.EvidenceRequiredException;
import com.umss.sigesa.domain.exception.InvalidRoleException;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.exception.SubsanationNotAllowedException;
import com.umss.sigesa.domain.model.SubphaseApproveResult;
import com.umss.sigesa.domain.model.SubphaseState;
import com.umss.sigesa.domain.model.SubphaseTransitionResult;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ApproveSubphaseIndicatorService implements ApproveSubphaseIndicatorUseCase {

    private static final Set<String> ALLOWED_ROLES = Set.of("TD");

    private final SubphaseQueryPort subphaseQueryPort;
    private final SubphaseEvidenceQueryPort evidenceQueryPort;
    private final SubphaseObservationPort observationPort;
    private final SubphaseTransitionHelper transitionHelper;
    private final NotificationOutboxPort notificationOutbox;

    public ApproveSubphaseIndicatorService(SubphaseQueryPort subphaseQueryPort,
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
    public SubphaseApproveResult approve(UUID subphaseId, UUID actorId, String actorRole) {
        assertRole(actorRole);
        SubphaseQueryPort.SubphaseContext context = subphaseQueryPort.findContext(subphaseId)
                .orElseThrow(() -> new ProcessNotFoundException("Subfase no encontrada: " + subphaseId));

        if (!evidenceQueryPort.hasEvidences(subphaseId)) {
            throw new EvidenceRequiredException(
                    "No se puede aprobar: la subfase no tiene evidencias cargadas.");
        }

        observationPort.findLatestOpenBySubphaseId(subphaseId).ifPresent(open -> {
            throw new SubsanationNotAllowedException(
                    "Hay una observación pendiente; debe subsanarse antes de aprobar.");
        });

        SubphaseTransitionResult transition = transitionHelper.transition(
                subphaseId,
                SubphaseState.APROBADO,
                SubphaseTransitionHelper.REVIEWABLE_STATES);

        notificationOutbox.enqueue(
                "SubphaseApproved",
                context.careerId(),
                Map.of("subphaseId", subphaseId.toString(), "newState", SubphaseState.APROBADO.name()));

        return new SubphaseApproveResult(subphaseId, transition);
    }

    private static void assertRole(String actorRole) {
        String normalized = actorRole != null ? actorRole.trim().toUpperCase(Locale.ROOT) : "";
        if (!ALLOWED_ROLES.contains(normalized)) {
            throw new InvalidRoleException("Solo el técnico [TD] puede aprobar subfases.");
        }
    }
}
