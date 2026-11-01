package com.umss.sigesa.application.service.workflow;

import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.application.port.out.NormativeIndicatorEvidenceQueryPort;
import com.umss.sigesa.application.port.out.NormativeIndicatorObservationPort;
import com.umss.sigesa.application.port.out.NormativeIndicatorWorkflowPort;
import com.umss.sigesa.application.port.out.NotificationOutboxPort;
import com.umss.sigesa.domain.exception.EvidenceRequiredException;
import com.umss.sigesa.domain.exception.IndicatorNotFoundException;
import com.umss.sigesa.domain.exception.InvalidIndicatorStateException;
import com.umss.sigesa.domain.exception.SubsanationNotAllowedException;
import com.umss.sigesa.domain.model.IndicatorObservation;
import com.umss.sigesa.domain.model.IndicatorState;
import com.umss.sigesa.domain.model.IndicatorObservationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApproveIndicatorService — FSD-UC-009 v2")
class ApproveIndicatorServiceTest {

    @Mock
    private NormativeHierarchyQueryPort hierarchyQueryPort;
    @Mock
    private NormativeIndicatorEvidenceQueryPort evidenceQueryPort;
    @Mock
    private NormativeIndicatorObservationPort observationPort;
    @Mock
    private NormativeIndicatorWorkflowPort workflowPort;
    @Mock
    private NotificationOutboxPort notificationOutbox;

    @InjectMocks
    private ApproveIndicatorService service;

    @Test
    @DisplayName("Aprobación exitosa transiciona a APROBADO")
    void approve_success() {
        UUID indicatorId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID careerId = UUID.randomUUID();

        when(hierarchyQueryPort.findIndicatorContext(indicatorId))
                .thenReturn(Optional.of(new NormativeHierarchyQueryPort.NormativeIndicatorContext(
                        indicatorId, UUID.randomUUID(), careerId, UUID.randomUUID(),
                        "N1", "IND-1", "Descripción", IndicatorState.SUBIDO)));
        when(evidenceQueryPort.hasEvidences(indicatorId)).thenReturn(true);
        when(observationPort.findLatestOpenByIndicatorId(indicatorId)).thenReturn(Optional.empty());

        var result = service.approve(indicatorId, actorId, "TD");

        assertEquals(IndicatorState.SUBIDO, result.previousState());
        assertEquals(IndicatorState.APROBADO, result.newState());
        verify(workflowPort).updateIndicatorStatus(indicatorId, IndicatorState.APROBADO);
        verify(notificationOutbox).enqueue(eq("IndicatorApproved"), eq(careerId), any());
    }

    @Test
    @DisplayName("Rechaza aprobación sin evidencia")
    void approve_requiresEvidence() {
        UUID indicatorId = UUID.randomUUID();
        when(hierarchyQueryPort.findIndicatorContext(indicatorId))
                .thenReturn(Optional.of(new NormativeHierarchyQueryPort.NormativeIndicatorContext(
                        indicatorId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                        "N1", "IND-1", "Descripción", IndicatorState.SUBIDO)));
        when(evidenceQueryPort.hasEvidences(indicatorId)).thenReturn(false);

        assertThrows(EvidenceRequiredException.class, () ->
                service.approve(indicatorId, UUID.randomUUID(), "TD"));
    }

    @Test
    @DisplayName("Rechaza aprobación con observación OPEN")
    void approve_rejectsOpenObservation() {
        UUID indicatorId = UUID.randomUUID();
        when(hierarchyQueryPort.findIndicatorContext(indicatorId))
                .thenReturn(Optional.of(new NormativeHierarchyQueryPort.NormativeIndicatorContext(
                        indicatorId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                        "N1", "IND-1", "Descripción", IndicatorState.OBSERVADO)));
        when(evidenceQueryPort.hasEvidences(indicatorId)).thenReturn(true);
        when(observationPort.findLatestOpenByIndicatorId(indicatorId))
                .thenReturn(Optional.of(IndicatorObservation.builder()
                        .id(UUID.randomUUID())
                        .indicatorId(indicatorId)
                        .status(IndicatorObservationStatus.OPEN)
                        .build()));

        assertThrows(SubsanationNotAllowedException.class, () ->
                service.approve(indicatorId, UUID.randomUUID(), "TD"));
    }

    @Test
    @DisplayName("Rechaza aprobación en estado inválido")
    void approve_rejectsInvalidState() {
        UUID indicatorId = UUID.randomUUID();
        when(hierarchyQueryPort.findIndicatorContext(indicatorId))
                .thenReturn(Optional.of(new NormativeHierarchyQueryPort.NormativeIndicatorContext(
                        indicatorId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                        "N1", "IND-1", "Descripción", IndicatorState.APROBADO)));
        when(evidenceQueryPort.hasEvidences(indicatorId)).thenReturn(true);
        when(observationPort.findLatestOpenByIndicatorId(indicatorId)).thenReturn(Optional.empty());

        assertThrows(InvalidIndicatorStateException.class, () ->
                service.approve(indicatorId, UUID.randomUUID(), "TD"));
    }

    @Test
    @DisplayName("Indicador inexistente")
    void approve_indicatorNotFound() {
        UUID indicatorId = UUID.randomUUID();
        when(hierarchyQueryPort.findIndicatorContext(indicatorId)).thenReturn(Optional.empty());

        assertThrows(IndicatorNotFoundException.class, () ->
                service.approve(indicatorId, UUID.randomUUID(), "TD"));
    }
}
