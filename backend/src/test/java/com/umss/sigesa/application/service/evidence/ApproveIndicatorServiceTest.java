package com.umss.sigesa.application.service.evidence;

import com.umss.sigesa.application.port.in.ApproveIndicatorUseCase.ApproveResult;
import com.umss.sigesa.application.port.out.EvidenceControlQueryPort;
import com.umss.sigesa.application.port.out.IndicatorRepositoryPort;
import com.umss.sigesa.application.port.out.NotificationOutboxPort;
import com.umss.sigesa.application.port.out.ObservationRepositoryPort;
import com.umss.sigesa.adapter.out.persistance.entity.ObservationEntity;
import com.umss.sigesa.application.model.evidence.EvidenceControlItem;
import com.umss.sigesa.domain.exception.ForbiddenRoleException;
import com.umss.sigesa.domain.exception.IndicatorNotFoundException;
import com.umss.sigesa.domain.exception.InvalidIndicatorStateException;
import com.umss.sigesa.domain.exception.EvidenceNotFoundException;
import com.umss.sigesa.domain.model.Indicator;
import com.umss.sigesa.domain.model.IndicatorState;
import com.umss.sigesa.domain.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ApproveIndicatorServiceTest {

    private IndicatorRepositoryPort indicatorRepository;
    private EvidenceControlQueryPort evidenceControlQueryPort;
    private ObservationRepositoryPort observationRepositoryPort;
    private NotificationOutboxPort notificationOutbox;
    private ApproveIndicatorService service;

    @BeforeEach
    void setUp() {
        indicatorRepository = mock(IndicatorRepositoryPort.class);
        evidenceControlQueryPort = mock(EvidenceControlQueryPort.class);
        observationRepositoryPort = mock(ObservationRepositoryPort.class);
        notificationOutbox = mock(NotificationOutboxPort.class);
        service = new ApproveIndicatorService(
                indicatorRepository,
                evidenceControlQueryPort,
                observationRepositoryPort,
                notificationOutbox
        );
    }

    @Test
    @DisplayName("Aprobación exitosa desde estado SUBIDO")
    void approve_successFromSubido() {
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

        ObservationEntity obs1 = new ObservationEntity();
        obs1.setStatus("PENDIENTE_SUBSANACION");
        ObservationEntity obs2 = new ObservationEntity();
        obs2.setStatus("PENDIENTE_SUBSANACION");
        List<ObservationEntity> observations = new ArrayList<>(List.of(obs1, obs2));
        when(observationRepositoryPort.findByIndicatorId(indicatorId.toString())).thenReturn(observations);

        ApproveResult result = service.approve(indicatorId, actorId, Role.TD);

        assertNotNull(result);
        assertEquals(IndicatorState.APROBADO, result.newState());
        assertEquals("IndicatorApproved", result.event());
        assertEquals("RESOLVED", obs1.getStatus());
        assertEquals("RESOLVED", obs2.getStatus());

        verify(indicatorRepository).appendStateHistory(any());
        verify(observationRepositoryPort).saveAll(observations);
        verify(notificationOutbox).enqueue(eq("IndicatorApproved"), eq(programId), any());
    }

    @Test
    @DisplayName("Aprobación exitosa desde estado SUBSANADO")
    void approve_successFromSubsanado() {
        UUID indicatorId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        Indicator indicator = new Indicator(indicatorId, programId, UUID.randomUUID(), UUID.randomUUID());

        when(indicatorRepository.findById(indicatorId)).thenReturn(Optional.of(indicator));
        when(indicatorRepository.getCurrentState(indicatorId)).thenReturn(IndicatorState.SUBSANADO);

        EvidenceControlItem item = new EvidenceControlItem(
                indicatorId, programId, UUID.randomUUID(), UUID.randomUUID(),
                IndicatorState.SUBSANADO, UUID.randomUUID(), 2, "hash2", "subsanado desc", LocalDateTime.now()
        );
        when(evidenceControlQueryPort.findByIndicatorId(indicatorId)).thenReturn(Optional.of(item));
        when(observationRepositoryPort.findByIndicatorId(indicatorId.toString())).thenReturn(new ArrayList<>());

        ApproveResult result = service.approve(indicatorId, actorId, Role.TD);

        assertNotNull(result);
        assertEquals(IndicatorState.APROBADO, result.newState());
    }

    @Test
    @DisplayName("Falla si el rol no es TD (Lanza ForbiddenRoleException)")
    void approve_failsIfNotTD() {
        assertThrows(ForbiddenRoleException.class, () ->
                service.approve(UUID.randomUUID(), UUID.randomUUID(), Role.CC)
        );
        assertThrows(ForbiddenRoleException.class, () ->
                service.approve(UUID.randomUUID(), UUID.randomUUID(), Role.JD)
        );
    }

    @Test
    @DisplayName("Falla si el indicador no existe")
    void approve_failsIfIndicatorNotFound() {
        UUID indicatorId = UUID.randomUUID();
        when(indicatorRepository.findById(indicatorId)).thenReturn(Optional.empty());

        assertThrows(IndicatorNotFoundException.class, () ->
                service.approve(indicatorId, UUID.randomUUID(), Role.TD)
        );
    }

    @Test
    @DisplayName("Falla si el estado actual no es SUBIDO ni SUBSANADO (PENDIENTE)")
    void approve_failsIfStateIsPendiente() {
        UUID indicatorId = UUID.randomUUID();
        Indicator indicator = new Indicator(indicatorId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        when(indicatorRepository.findById(indicatorId)).thenReturn(Optional.of(indicator));
        when(indicatorRepository.getCurrentState(indicatorId)).thenReturn(IndicatorState.PENDIENTE);

        assertThrows(InvalidIndicatorStateException.class, () ->
                service.approve(indicatorId, UUID.randomUUID(), Role.TD)
        );
    }

    @Test
    @DisplayName("Falla si el estado actual es OBSERVADO")
    void approve_failsIfStateIsObservado() {
        UUID indicatorId = UUID.randomUUID();
        Indicator indicator = new Indicator(indicatorId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        when(indicatorRepository.findById(indicatorId)).thenReturn(Optional.of(indicator));
        when(indicatorRepository.getCurrentState(indicatorId)).thenReturn(IndicatorState.OBSERVADO);

        assertThrows(InvalidIndicatorStateException.class, () ->
                service.approve(indicatorId, UUID.randomUUID(), Role.TD)
        );
    }

    @Test
    @DisplayName("Falla si el estado actual ya es APROBADO")
    void approve_failsIfStateIsAprobado() {
        UUID indicatorId = UUID.randomUUID();
        Indicator indicator = new Indicator(indicatorId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        when(indicatorRepository.findById(indicatorId)).thenReturn(Optional.of(indicator));
        when(indicatorRepository.getCurrentState(indicatorId)).thenReturn(IndicatorState.APROBADO);

        assertThrows(InvalidIndicatorStateException.class, () ->
                service.approve(indicatorId, UUID.randomUUID(), Role.TD)
        );
    }

    @Test
    @DisplayName("Falla si no existe registro de evidencia")
    void approve_failsIfNoEvidence() {
        UUID indicatorId = UUID.randomUUID();
        Indicator indicator = new Indicator(indicatorId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        when(indicatorRepository.findById(indicatorId)).thenReturn(Optional.of(indicator));
        when(indicatorRepository.getCurrentState(indicatorId)).thenReturn(IndicatorState.SUBIDO);
        when(evidenceControlQueryPort.findByIndicatorId(indicatorId)).thenReturn(Optional.empty());

        assertThrows(EvidenceNotFoundException.class, () ->
                service.approve(indicatorId, UUID.randomUUID(), Role.TD)
        );
    }

    @Test
    @DisplayName("Falla si el registro de evidencia tiene ID nulo")
    void approve_failsIfEvidenceIdIsNull() {
        UUID indicatorId = UUID.randomUUID();
        Indicator indicator = new Indicator(indicatorId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        when(indicatorRepository.findById(indicatorId)).thenReturn(Optional.of(indicator));
        when(indicatorRepository.getCurrentState(indicatorId)).thenReturn(IndicatorState.SUBIDO);

        EvidenceControlItem item = new EvidenceControlItem(
                indicatorId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                IndicatorState.SUBIDO, null, 1, "hash", "desc", LocalDateTime.now()
        );
        when(evidenceControlQueryPort.findByIndicatorId(indicatorId)).thenReturn(Optional.of(item));

        assertThrows(EvidenceNotFoundException.class, () ->
                service.approve(indicatorId, UUID.randomUUID(), Role.TD)
        );
    }
}
