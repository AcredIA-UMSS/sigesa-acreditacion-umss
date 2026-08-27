package com.umss.sigesa.application.service.subphase;

import com.umss.sigesa.application.port.in.AddSubphaseObservationUseCase;
import com.umss.sigesa.application.port.out.SubphaseObservationPort;
import com.umss.sigesa.application.port.out.SubphaseQueryPort;
import com.umss.sigesa.domain.exception.InvalidRoleException;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.SubphaseObservation;
import com.umss.sigesa.domain.model.SubphaseObservationStatus;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class AddSubphaseObservationService implements AddSubphaseObservationUseCase {

    private static final Set<String> ALLOWED_ROLES = Set.of("TD", "JD");

    private final SubphaseQueryPort subphaseQueryPort;
    private final SubphaseObservationPort observationPort;

    public AddSubphaseObservationService(SubphaseQueryPort subphaseQueryPort,
                                         SubphaseObservationPort observationPort) {
        this.subphaseQueryPort = subphaseQueryPort;
        this.observationPort = observationPort;
    }

    @Override
    public SubphaseObservation add(UUID subphaseId, String body, UUID authorId, String authorRole) {
        String normalizedRole = authorRole != null ? authorRole.trim().toUpperCase(Locale.ROOT) : "";
        if (!ALLOWED_ROLES.contains(normalizedRole)) {
            throw new InvalidRoleException("Solo TD o JD pueden registrar observaciones de subfase.");
        }
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("La observación no puede estar vacía.");
        }
        subphaseQueryPort.findContext(subphaseId)
                .orElseThrow(() -> new ProcessNotFoundException("Subfase no encontrada: " + subphaseId));

        observationPort.findLatestOpenBySubphaseId(subphaseId).ifPresent(existing -> {
            throw new IllegalStateException(
                    "Ya existe una observación pendiente de subsanación en esta subfase.");
        });

        LocalDateTime now = LocalDateTime.now();
        SubphaseObservation observation = SubphaseObservation.builder()
                .id(UUID.randomUUID())
                .subphaseId(subphaseId)
                .authorId(authorId)
                .authorRole(normalizedRole)
                .body(body.trim())
                .status(SubphaseObservationStatus.OPEN)
                .createdAt(now)
                .updatedAt(now)
                .build();
        return observationPort.save(observation);
    }
}
