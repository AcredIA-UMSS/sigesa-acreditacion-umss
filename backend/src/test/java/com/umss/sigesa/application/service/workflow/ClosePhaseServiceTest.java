package com.umss.sigesa.application.service.workflow;

import com.umss.sigesa.application.port.out.NotificationOutboxPort;
import com.umss.sigesa.application.port.out.PhaseWorkflowPort;
import com.umss.sigesa.domain.exception.InvalidPhaseStateException;
import com.umss.sigesa.domain.exception.InvalidRoleException;
import com.umss.sigesa.domain.exception.PhaseClosureBlockedException;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.PhaseCompleteResult;
import com.umss.sigesa.domain.model.PhaseState;
import com.umss.sigesa.domain.model.SubphaseState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClosePhaseServiceTest {

    @Mock
    private PhaseWorkflowPort phaseWorkflowPort;

    @Mock
    private NotificationOutboxPort notificationOutbox;

    @InjectMocks
    private ClosePhaseService closePhaseService;

    @Test
    void shouldClosePhaseWhenAllSubphasesApproved() {
        UUID processId = UUID.randomUUID();
        UUID phaseId = UUID.randomUUID();
        UUID careerId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        when(phaseWorkflowPort.findPhaseContext(processId, phaseId))
                .thenReturn(Optional.of(new PhaseWorkflowPort.PhaseContext(
                        phaseId, processId, careerId, "Fase 1")));
        when(phaseWorkflowPort.getCurrentState(phaseId)).thenReturn(PhaseState.ABIERTA);
        when(phaseWorkflowPort.listSubphasesWithStatus(phaseId)).thenReturn(List.of(
                new PhaseWorkflowPort.SubphaseStatusItem(
                        UUID.randomUUID(), "Sub A", SubphaseState.APROBADO, 1),
                new PhaseWorkflowPort.SubphaseStatusItem(
                        UUID.randomUUID(), "Sub B", SubphaseState.APROBADO, 2)));

        PhaseCompleteResult result = closePhaseService.close(processId, phaseId, actorId, "TD");

        assertEquals(phaseId, result.phaseId());
        assertEquals(PhaseState.ABIERTA, result.previousState());
        assertEquals(PhaseState.COMPLETADA, result.newState());
        assertEquals("PhaseCompleted", result.event());
        verify(phaseWorkflowPort).updateState(phaseId, PhaseState.COMPLETADA);
        verify(notificationOutbox).enqueue(eq("PhaseCompleted"), eq(careerId), org.mockito.ArgumentMatchers.anyMap());
    }

    @Test
    void shouldClosePhaseWithNoSubphases() {
        UUID processId = UUID.randomUUID();
        UUID phaseId = UUID.randomUUID();
        UUID careerId = UUID.randomUUID();

        when(phaseWorkflowPort.findPhaseContext(processId, phaseId))
                .thenReturn(Optional.of(new PhaseWorkflowPort.PhaseContext(
                        phaseId, processId, careerId, "Fase vacía")));
        when(phaseWorkflowPort.getCurrentState(phaseId)).thenReturn(PhaseState.ABIERTA);
        when(phaseWorkflowPort.listSubphasesWithStatus(phaseId)).thenReturn(List.of());

        PhaseCompleteResult result = closePhaseService.close(processId, phaseId, UUID.randomUUID(), "TD");

        assertEquals(PhaseState.COMPLETADA, result.newState());
        verify(phaseWorkflowPort).updateState(phaseId, PhaseState.COMPLETADA);
    }

    @Test
    void shouldBlockClosureWhenPendingSubphasesExist() {
        UUID processId = UUID.randomUUID();
        UUID phaseId = UUID.randomUUID();
        UUID pendingId = UUID.randomUUID();

        when(phaseWorkflowPort.findPhaseContext(processId, phaseId))
                .thenReturn(Optional.of(new PhaseWorkflowPort.PhaseContext(
                        phaseId, processId, UUID.randomUUID(), "Fase 1")));
        when(phaseWorkflowPort.getCurrentState(phaseId)).thenReturn(PhaseState.ABIERTA);
        when(phaseWorkflowPort.listSubphasesWithStatus(phaseId)).thenReturn(List.of(
                new PhaseWorkflowPort.SubphaseStatusItem(
                        pendingId, "Pendiente", SubphaseState.OBSERVADO, 1),
                new PhaseWorkflowPort.SubphaseStatusItem(
                        UUID.randomUUID(), "Lista", SubphaseState.APROBADO, 2)));

        PhaseClosureBlockedException ex = assertThrows(
                PhaseClosureBlockedException.class,
                () -> closePhaseService.close(processId, phaseId, UUID.randomUUID(), "TD"));

        assertEquals(1, ex.getPendingSubphases().size());
        assertEquals(pendingId, ex.getPendingSubphases().getFirst().subphaseId());
    }

    @Test
    void shouldRejectNonTdRole() {
        assertThrows(InvalidRoleException.class,
                () -> closePhaseService.close(
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "CC"));
    }

    @Test
    void shouldRejectWhenPhaseNotFound() {
        UUID processId = UUID.randomUUID();
        UUID phaseId = UUID.randomUUID();
        when(phaseWorkflowPort.findPhaseContext(processId, phaseId)).thenReturn(Optional.empty());

        assertThrows(ProcessNotFoundException.class,
                () -> closePhaseService.close(processId, phaseId, UUID.randomUUID(), "TD"));
    }

    @Test
    void shouldRejectWhenPhaseAlreadyCompleted() {
        UUID processId = UUID.randomUUID();
        UUID phaseId = UUID.randomUUID();

        when(phaseWorkflowPort.findPhaseContext(processId, phaseId))
                .thenReturn(Optional.of(new PhaseWorkflowPort.PhaseContext(
                        phaseId, processId, UUID.randomUUID(), "Fase 1")));
        when(phaseWorkflowPort.getCurrentState(phaseId)).thenReturn(PhaseState.COMPLETADA);

        assertThrows(InvalidPhaseStateException.class,
                () -> closePhaseService.close(processId, phaseId, UUID.randomUUID(), "TD"));
    }

    @Test
    void shouldEnqueuePhaseCompletedWithPayload() {
        UUID processId = UUID.randomUUID();
        UUID phaseId = UUID.randomUUID();
        UUID careerId = UUID.randomUUID();

        when(phaseWorkflowPort.findPhaseContext(processId, phaseId))
                .thenReturn(Optional.of(new PhaseWorkflowPort.PhaseContext(
                        phaseId, processId, careerId, "Fase 1")));
        when(phaseWorkflowPort.getCurrentState(phaseId)).thenReturn(PhaseState.ABIERTA);
        when(phaseWorkflowPort.listSubphasesWithStatus(phaseId)).thenReturn(List.of());

        closePhaseService.close(processId, phaseId, UUID.randomUUID(), "td");

        ArgumentCaptor<java.util.Map<String, String>> payloadCaptor =
                ArgumentCaptor.forClass(java.util.Map.class);
        verify(notificationOutbox).enqueue(
                eq("PhaseCompleted"), eq(careerId), payloadCaptor.capture());
        assertEquals(phaseId.toString(), payloadCaptor.getValue().get("phaseId"));
        assertEquals(processId.toString(), payloadCaptor.getValue().get("processId"));
    }
}
