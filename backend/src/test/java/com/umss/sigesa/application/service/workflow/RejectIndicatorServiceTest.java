package com.umss.sigesa.application.service.workflow;

import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.application.port.out.NormativeIndicatorEvidenceQueryPort;
import com.umss.sigesa.application.port.out.NormativeIndicatorObservationPort;
import com.umss.sigesa.application.port.out.NormativeIndicatorWorkflowPort;
import com.umss.sigesa.application.port.out.NotificationOutboxPort;
import com.umss.sigesa.domain.exception.EvidenceRequiredException;
import com.umss.sigesa.domain.exception.IndicatorNotFoundException;
import com.umss.sigesa.domain.exception.InvalidIndicatorStateException;
import com.umss.sigesa.domain.exception.JustificationRequiredException;
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
@DisplayName("RejectIndicatorService — FSD-UC-008 v2")
class RejectIndicatorServiceTest {

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
    private RejectIndicatorService service;

    @Test
    @DisplayName("Rechazo exitoso crea observación OPEN y transiciona a OBSERVADO")
    void reject_success() {
        UUID indicatorId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID careerId = UUID.randomUUID();
        String justification = "La evidencia no cumple el criterio mínimo exigido.";

        when(hierarchyQueryPort.findIndicatorContext(indicatorId))
                .thenReturn(Optional.of(new NormativeHierarchyQueryPort.NormativeIndicatorContext(
                        indicatorId, UUID.randomUUID(), careerId, UUID.randomUUID(),
                        "N1", "IND-1", "Descripción", IndicatorState.SUBIDO)));
        when(evidenceQueryPort.hasEvidences(indicatorId)).thenReturn(true);
        when(observationPort.findLatestOpenByIndicatorId(indicatorId)).thenReturn(Optional.empty());
        when(observationPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.reject(indicatorId, justification, actorId, "TD");

        assertEquals(IndicatorState.SUBIDO, result.previousState());
        assertEquals(IndicatorState.OBSERVADO, result.newState());
        assertEquals(indicatorId, result.indicatorId());
        verify(workflowPort).updateIndicatorStatus(indicatorId, IndicatorState.OBSERVADO);
        verify(observationPort).save(any(IndicatorObservation.class));
        verify(notificationOutbox).enqueue(eq("IndicatorRejected"), eq(careerId), any());
    }

    @Test
    @DisplayName("Rechaza sin evidencia cargada")
    void reject_requiresEvidence() {
        UUID indicatorId = UUID.randomUUID();
        when(hierarchyQueryPort.findIndicatorContext(indicatorId))
                .thenReturn(Optional.of(new NormativeHierarchyQueryPort.NormativeIndicatorContext(
                        indicatorId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                        "N1", "IND-1", "Descripción", IndicatorState.SUBIDO)));
        when(evidenceQueryPort.hasEvidences(indicatorId)).thenReturn(false);

        assertThrows(EvidenceRequiredException.class, () -> service.reject(
                indicatorId, "Justificación válida de veinte chars", UUID.randomUUID(), "TD"));
    }

    @Test
    @DisplayName("Rechaza con observación OPEN existente")
    void reject_rejectsExistingOpenObservation() {
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

        assertThrows(InvalidIndicatorStateException.class, () -> service.reject(
                indicatorId, "Justificación válida de veinte chars", UUID.randomUUID(), "TD"));
    }

    @Test
    @DisplayName("Justificación demasiado corta")
    void reject_requiresJustification() {
        assertThrows(JustificationRequiredException.class, () -> service.reject(
                UUID.randomUUID(), "corta", UUID.randomUUID(), "TD"));
    }

    @Test
    @DisplayName("Indicador inexistente")
    void reject_indicatorNotFound() {
        UUID indicatorId = UUID.randomUUID();
        when(hierarchyQueryPort.findIndicatorContext(indicatorId)).thenReturn(Optional.empty());

        assertThrows(IndicatorNotFoundException.class, () -> service.reject(
                indicatorId, "Justificación válida de veinte chars", UUID.randomUUID(), "TD"));
    }
}
