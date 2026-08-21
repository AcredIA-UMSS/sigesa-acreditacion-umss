package com.umss.sigesa.application.service.evidence;

import com.umss.sigesa.application.port.in.RejectIndicatorUseCase.RejectResult;
import com.umss.sigesa.application.port.out.EvidenceControlQueryPort;
import com.umss.sigesa.application.port.out.IndicatorRepositoryPort;
import com.umss.sigesa.application.port.out.NotificationOutboxPort;
import com.umss.sigesa.application.port.out.ObservationRepositoryPort;
import com.umss.sigesa.application.model.evidence.EvidenceControlItem;
import com.umss.sigesa.domain.exception.ForbiddenRoleException;
import com.umss.sigesa.domain.exception.IndicatorNotFoundException;
import com.umss.sigesa.domain.exception.InvalidIndicatorStateException;
import com.umss.sigesa.domain.exception.JustificationRequiredException;
import com.umss.sigesa.domain.exception.EvidenceNotFoundException;
import com.umss.sigesa.domain.model.Indicator;
import com.umss.sigesa.domain.model.IndicatorState;
import com.umss.sigesa.domain.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RejectIndicatorServiceTest {

    private IndicatorRepositoryPort indicatorRepository;
    private EvidenceControlQueryPort evidenceControlQueryPort;
    private ObservationRepositoryPort observationRepositoryPort;
    private NotificationOutboxPort notificationOutbox;
    private RejectIndicatorService service;

    @BeforeEach
    void setUp() {
        indicatorRepository = mock(IndicatorRepositoryPort.class);
        evidenceControlQueryPort = mock(EvidenceControlQueryPort.class);
        observationRepositoryPort = mock(ObservationRepositoryPort.class);
        notificationOutbox = mock(NotificationOutboxPort.class);
        service = new RejectIndicatorService(
                indicatorRepository,
                evidenceControlQueryPort,
                observationRepositoryPort,
                notificationOutbox
        );
    }

    @Test
    @DisplayName("Rechazo exitoso desde estado SUBIDO")
    void reject_successFromSubido() {
        UUID indicatorId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        Indicator indicator = new Indicator(indicatorId, programId, UUID.randomUUID(), UUID.randomUUID());

        when(indicatorRepository.findById(indicatorId)).thenReturn(Optional.of(indicator));
        when(indicatorRepository.getCurrentState(indicatorId)).thenReturn(IndicatorState.SUBIDO);

        EvidenceControlItem item = new EvidenceControlItem(
                indicatorId, programId, UUID.randomUUID(), UUID.randomUUID(),
                IndicatorState.SUBIDO, UUID.randomUUID(), 1, "hash", "desc", LocalDateTime.now()
        );
        when(evidenceControlQueryPort.findByIndicatorId(indicatorId)).thenReturn(Optional.of(item));

        RejectResult result = service.reject(indicatorId, "Esta es una justificacion valida de mas de 20 caracteres", actorId, Role.TD);

        assertNotNull(result);
        assertEquals(IndicatorState.OBSERVADO, result.newState());
        assertNotNull(result.observationId());
        assertTrue(result.observationId().startsWith("OBS-"));

        verify(indicatorRepository).appendStateHistory(any());
        verify(observationRepositoryPort).save(any());
        verify(notificationOutbox).enqueue(eq("IndicatorRejected"), eq(programId), any());
    }

    @Test
    @DisplayName("Rechazo exitoso desde estado SUBSANADO")
    void reject_successFromSubsanado() {
        UUID indicatorId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        Indicator indicator = new Indicator(indicatorId, programId, UUID.randomUUID(), UUID.randomUUID());

        when(indicatorRepository.findById(indicatorId)).thenReturn(Optional.of(indicator));
        when(indicatorRepository.getCurrentState(indicatorId)).thenReturn(IndicatorState.SUBSANADO);

        EvidenceControlItem item = new EvidenceControlItem(
                indicatorId, programId, UUID.randomUUID(), UUID.randomUUID(),
                IndicatorState.SUBSANADO, UUID.randomUUID(), 2, "hash2", "desc", LocalDateTime.now()
        );
        when(evidenceControlQueryPort.findByIndicatorId(indicatorId)).thenReturn(Optional.of(item));

        RejectResult result = service.reject(indicatorId, "La subsanación no responde al criterio normativo", actorId, Role.TD);

        assertNotNull(result);
        assertEquals(IndicatorState.OBSERVADO, result.newState());
    }

    @Test
    @DisplayName("Límite exacto: Justificación de exactamente 20 caracteres es válida")
    void reject_successBoundary20Chars() {
        UUID indicatorId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        Indicator indicator = new Indicator(indicatorId, programId, UUID.randomUUID(), UUID.randomUUID());

        when(indicatorRepository.findById(indicatorId)).thenReturn(Optional.of(indicator));
        when(indicatorRepository.getCurrentState(indicatorId)).thenReturn(IndicatorState.SUBIDO);

        EvidenceControlItem item = new EvidenceControlItem(
                indicatorId, programId, UUID.randomUUID(), UUID.randomUUID(),
                IndicatorState.SUBIDO, UUID.randomUUID(), 1, "hash", "desc", LocalDateTime.now()
        );
        when(evidenceControlQueryPort.findByIndicatorId(indicatorId)).thenReturn(Optional.of(item));

        // Exactly 20 chars: "12345678901234567890"
        RejectResult result = service.reject(indicatorId, "12345678901234567890", UUID.randomUUID(), Role.TD);
        assertNotNull(result);
        assertEquals(IndicatorState.OBSERVADO, result.newState());
    }

    @Test
    @DisplayName("Límite exacto: Justificación de 19 caracteres falla (JustificationRequiredException)")
    void reject_failsBoundary19Chars() {
        // Exactly 19 chars: "1234567890123456789"
        assertThrows(JustificationRequiredException.class, () ->
                service.reject(UUID.randomUUID(), "1234567890123456789", UUID.randomUUID(), Role.TD)
        );
    }

    @Test
    @DisplayName("Falla si la justificación es nula o vacía")
    void reject_failsIfJustificationNullOrEmpty() {
        assertThrows(JustificationRequiredException.class, () ->
                service.reject(UUID.randomUUID(), null, UUID.randomUUID(), Role.TD)
        );
        assertThrows(JustificationRequiredException.class, () ->
                service.reject(UUID.randomUUID(), "", UUID.randomUUID(), Role.TD)
        );
    }

    @Test
    @DisplayName("Falla si la justificación solo contiene espacios en blanco")
    void reject_failsIfJustificationOnlyWhitespace() {
        assertThrows(JustificationRequiredException.class, () ->
                service.reject(UUID.randomUUID(), "                    ", UUID.randomUUID(), Role.TD)
        );
    }

    @Test
    @DisplayName("Falla si el rol no es TD")
    void reject_failsIfNotTD() {
        assertThrows(ForbiddenRoleException.class, () ->
                service.reject(UUID.randomUUID(), "Justificacion valida de mas de 20 caracteres", UUID.randomUUID(), Role.CC)
        );
        assertThrows(ForbiddenRoleException.class, () ->
                service.reject(UUID.randomUUID(), "Justificacion valida de mas de 20 caracteres", UUID.randomUUID(), Role.JD)
        );
    }

    @Test
    @DisplayName("Falla si el indicador no existe")
    void reject_failsIfIndicatorNotFound() {
        UUID indicatorId = UUID.randomUUID();
        when(indicatorRepository.findById(indicatorId)).thenReturn(Optional.empty());

        assertThrows(IndicatorNotFoundException.class, () ->
                service.reject(indicatorId, "Justificacion de mas de 20 caracteres", UUID.randomUUID(), Role.TD)
        );
    }

    @Test
    @DisplayName("Falla si el estado actual es PENDIENTE")
    void reject_failsIfStateIsPendiente() {
        UUID indicatorId = UUID.randomUUID();
        Indicator indicator = new Indicator(indicatorId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        when(indicatorRepository.findById(indicatorId)).thenReturn(Optional.of(indicator));
        when(indicatorRepository.getCurrentState(indicatorId)).thenReturn(IndicatorState.PENDIENTE);

        assertThrows(InvalidIndicatorStateException.class, () ->
                service.reject(indicatorId, "Justificacion valida de mas de 20 caracteres", UUID.randomUUID(), Role.TD)
        );
    }

    @Test
    @DisplayName("Falla si el estado actual ya es OBSERVADO")
    void reject_failsIfStateIsObservado() {
        UUID indicatorId = UUID.randomUUID();
        Indicator indicator = new Indicator(indicatorId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        when(indicatorRepository.findById(indicatorId)).thenReturn(Optional.of(indicator));
        when(indicatorRepository.getCurrentState(indicatorId)).thenReturn(IndicatorState.OBSERVADO);

        assertThrows(InvalidIndicatorStateException.class, () ->
                service.reject(indicatorId, "Justificacion valida de mas de 20 caracteres", UUID.randomUUID(), Role.TD)
        );
    }

    @Test
    @DisplayName("Falla si el estado actual es APROBADO")
    void reject_failsIfStateIsAprobado() {
        UUID indicatorId = UUID.randomUUID();
        Indicator indicator = new Indicator(indicatorId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        when(indicatorRepository.findById(indicatorId)).thenReturn(Optional.of(indicator));
        when(indicatorRepository.getCurrentState(indicatorId)).thenReturn(IndicatorState.APROBADO);

        assertThrows(InvalidIndicatorStateException.class, () ->
                service.reject(indicatorId, "Justificacion valida de mas de 20 caracteres", UUID.randomUUID(), Role.TD)
        );
    }

    @Test
    @DisplayName("Falla si no existe evidencia para el indicador")
    void reject_failsIfNoEvidence() {
        UUID indicatorId = UUID.randomUUID();
        Indicator indicator = new Indicator(indicatorId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        when(indicatorRepository.findById(indicatorId)).thenReturn(Optional.of(indicator));
        when(indicatorRepository.getCurrentState(indicatorId)).thenReturn(IndicatorState.SUBIDO);
        when(evidenceControlQueryPort.findByIndicatorId(indicatorId)).thenReturn(Optional.empty());

        assertThrows(EvidenceNotFoundException.class, () ->
                service.reject(indicatorId, "Justificacion de mas de 20 caracteres", UUID.randomUUID(), Role.TD)
        );
    }
}
