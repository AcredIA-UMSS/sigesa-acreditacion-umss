package com.umss.sigesa.application.service.workflow;

import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.application.port.out.NormativeIndicatorWorkflowPort;
import com.umss.sigesa.application.port.out.NotificationOutboxPort;
import com.umss.sigesa.domain.exception.InvalidLevel1StateException;
import com.umss.sigesa.domain.exception.InvalidRoleException;
import com.umss.sigesa.domain.exception.Level1ClosureBlockedException;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.IndicatorState;
import com.umss.sigesa.domain.model.Level1CompleteResult;
import com.umss.sigesa.domain.model.PhaseState;
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
class CloseLevel1ServiceTest {

    @Mock
    private NormativeHierarchyQueryPort hierarchyQueryPort;

    @Mock
    private NormativeIndicatorWorkflowPort workflowPort;

    @Mock
    private NotificationOutboxPort notificationOutbox;

    @InjectMocks
    private CloseLevel1Service closeLevel1Service;

    @Test
    void shouldCloseLevel1WhenAllIndicatorsApproved() {
        UUID processId = UUID.randomUUID();
        UUID level1Id = UUID.randomUUID();
        UUID careerId = UUID.randomUUID();

        when(hierarchyQueryPort.findLevel1Context(level1Id))
                .thenReturn(Optional.of(new NormativeHierarchyQueryPort.Level1Context(
                        level1Id, processId, careerId, "Dimensión 1", PhaseState.ABIERTA)));
        when(hierarchyQueryPort.listIndicatorsWithStatusByLevel1Id(level1Id)).thenReturn(List.of(
                new NormativeHierarchyQueryPort.IndicatorStatusItem(
                        UUID.randomUUID(), "IND-1", "Indicador A", IndicatorState.APROBADO, 1),
                new NormativeHierarchyQueryPort.IndicatorStatusItem(
                        UUID.randomUUID(), "IND-2", "Indicador B", IndicatorState.APROBADO, 2)));

        Level1CompleteResult result = closeLevel1Service.close(processId, level1Id, UUID.randomUUID(), "TD");

        assertEquals(level1Id, result.level1Id());
        assertEquals(PhaseState.ABIERTA, result.previousState());
        assertEquals(PhaseState.COMPLETADA, result.newState());
        assertEquals("Level1Completed", result.event());
        verify(workflowPort).updateLevel1Status(level1Id, PhaseState.COMPLETADA);
        verify(notificationOutbox).enqueue(eq("Level1Completed"), eq(careerId), org.mockito.ArgumentMatchers.anyMap());
    }

    @Test
    void shouldCloseLevel1WithNoIndicators() {
        UUID processId = UUID.randomUUID();
        UUID level1Id = UUID.randomUUID();
        UUID careerId = UUID.randomUUID();

        when(hierarchyQueryPort.findLevel1Context(level1Id))
                .thenReturn(Optional.of(new NormativeHierarchyQueryPort.Level1Context(
                        level1Id, processId, careerId, "Área vacía", PhaseState.ABIERTA)));
        when(hierarchyQueryPort.listIndicatorsWithStatusByLevel1Id(level1Id)).thenReturn(List.of());

        Level1CompleteResult result = closeLevel1Service.close(processId, level1Id, UUID.randomUUID(), "TD");

        assertEquals(PhaseState.COMPLETADA, result.newState());
        verify(workflowPort).updateLevel1Status(level1Id, PhaseState.COMPLETADA);
    }

    @Test
    void shouldBlockClosureWhenPendingIndicatorsExist() {
        UUID processId = UUID.randomUUID();
        UUID level1Id = UUID.randomUUID();
        UUID pendingId = UUID.randomUUID();

        when(hierarchyQueryPort.findLevel1Context(level1Id))
                .thenReturn(Optional.of(new NormativeHierarchyQueryPort.Level1Context(
                        level1Id, processId, UUID.randomUUID(), "Dimensión 1", PhaseState.ABIERTA)));
        when(hierarchyQueryPort.listIndicatorsWithStatusByLevel1Id(level1Id)).thenReturn(List.of(
                new NormativeHierarchyQueryPort.IndicatorStatusItem(
                        pendingId, "IND-1", "Pendiente", IndicatorState.OBSERVADO, 1),
                new NormativeHierarchyQueryPort.IndicatorStatusItem(
                        UUID.randomUUID(), "IND-2", "Listo", IndicatorState.APROBADO, 2)));

        Level1ClosureBlockedException ex = assertThrows(
                Level1ClosureBlockedException.class,
                () -> closeLevel1Service.close(processId, level1Id, UUID.randomUUID(), "TD"));

        assertEquals(1, ex.getPendingIndicators().size());
        assertEquals(pendingId, ex.getPendingIndicators().getFirst().indicatorId());
    }

    @Test
    void shouldRejectNonTdRole() {
        assertThrows(InvalidRoleException.class,
                () -> closeLevel1Service.close(
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "CC"));
    }

    @Test
    void shouldRejectWhenLevel1NotFound() {
        UUID processId = UUID.randomUUID();
        UUID level1Id = UUID.randomUUID();
        when(hierarchyQueryPort.findLevel1Context(level1Id)).thenReturn(Optional.empty());

        assertThrows(ProcessNotFoundException.class,
                () -> closeLevel1Service.close(processId, level1Id, UUID.randomUUID(), "TD"));
    }

    @Test
    void shouldRejectWhenLevel1BelongsToDifferentProcess() {
        UUID processId = UUID.randomUUID();
        UUID level1Id = UUID.randomUUID();
        when(hierarchyQueryPort.findLevel1Context(level1Id))
                .thenReturn(Optional.of(new NormativeHierarchyQueryPort.Level1Context(
                        level1Id, UUID.randomUUID(), UUID.randomUUID(), "Dimensión 1", PhaseState.ABIERTA)));

        assertThrows(ProcessNotFoundException.class,
                () -> closeLevel1Service.close(processId, level1Id, UUID.randomUUID(), "TD"));
    }

    @Test
    void shouldRejectWhenLevel1AlreadyCompleted() {
        UUID processId = UUID.randomUUID();
        UUID level1Id = UUID.randomUUID();

        when(hierarchyQueryPort.findLevel1Context(level1Id))
                .thenReturn(Optional.of(new NormativeHierarchyQueryPort.Level1Context(
                        level1Id, processId, UUID.randomUUID(), "Dimensión 1", PhaseState.COMPLETADA)));

        assertThrows(InvalidLevel1StateException.class,
                () -> closeLevel1Service.close(processId, level1Id, UUID.randomUUID(), "TD"));
    }

    @Test
    void shouldEnqueueLevel1CompletedWithPayload() {
        UUID processId = UUID.randomUUID();
        UUID level1Id = UUID.randomUUID();
        UUID careerId = UUID.randomUUID();

        when(hierarchyQueryPort.findLevel1Context(level1Id))
                .thenReturn(Optional.of(new NormativeHierarchyQueryPort.Level1Context(
                        level1Id, processId, careerId, "Dimensión 1", PhaseState.ABIERTA)));
        when(hierarchyQueryPort.listIndicatorsWithStatusByLevel1Id(level1Id)).thenReturn(List.of());

        closeLevel1Service.close(processId, level1Id, UUID.randomUUID(), "td");

        ArgumentCaptor<java.util.Map<String, String>> payloadCaptor =
                ArgumentCaptor.forClass(java.util.Map.class);
        verify(notificationOutbox).enqueue(
                eq("Level1Completed"), eq(careerId), payloadCaptor.capture());
        assertEquals(level1Id.toString(), payloadCaptor.getValue().get("level1Id"));
        assertEquals(processId.toString(), payloadCaptor.getValue().get("processId"));
    }
}
