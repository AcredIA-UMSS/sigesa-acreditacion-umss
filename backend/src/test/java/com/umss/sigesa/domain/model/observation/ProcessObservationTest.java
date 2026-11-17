package com.umss.sigesa.domain.model.observation;

import com.umss.sigesa.domain.exception.InvalidObservationStateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProcessObservationTest {

    private final Instant now = Instant.parse("2026-11-04T12:00:00Z");
    private final UUID author = UUID.randomUUID();

    @Test
    @DisplayName("el técnico envía, el coordinador responde y el técnico cierra")
    void flujoCompleto() {
        ProcessObservation observation = draft();
        observation.submit(now);
        observation.startReview(now);
        observation.reply(UUID.randomUUID(), "CC", "Se revisará el anexo", now);
        observation.attend(UUID.randomUUID(), "CC", "Se reemplazó el archivo", now);
        observation.close(author, "TD", "Queda cerrada", now);

        assertEquals(ObservationStatus.CERRADA, observation.getStatus());
        assertEquals(3, observation.getMessages().size());
    }

    @Test
    @DisplayName("no se envía un borrador sin recomendación")
    void envioIncompleto() {
        ProcessObservation observation = ProcessObservation.draft(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "Falta la firma del anexo institucional", ObservationKind.FORMA,
                ObservationPriority.ALTA, ComplianceLevel.NO_CUMPLE, " ", author, now);

        InvalidObservationStateException error = assertThrows(
                InvalidObservationStateException.class, () -> observation.submit(now));
        assertEquals("OBSERVATION_INCOMPLETE", error.getCode());
        assertEquals(ObservationStatus.BORRADOR, observation.getStatus());
    }

    @Test
    @DisplayName("el coordinador no responde una observación que sigue en borrador")
    void respuestaFueraDeEstado() {
        ProcessObservation observation = draft();
        assertThrows(InvalidObservationStateException.class,
                () -> observation.reply(UUID.randomUUID(), "CC", "Respuesta", now));
    }

    private ProcessObservation draft() {
        return ProcessObservation.draft(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "El anexo no cubre el criterio de infraestructura", ObservationKind.CUMPLIMIENTO,
                ObservationPriority.ALTA, ComplianceLevel.CUMPLE_PARCIAL,
                "Adjuntar el plano actualizado", author, now);
    }
}
