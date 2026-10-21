package com.umss.sigesa.application.service.workflow;

import com.umss.sigesa.application.port.out.PhaseWorkflowPort;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.PhaseProgressResult;
import com.umss.sigesa.domain.model.PhaseState;
import com.umss.sigesa.domain.model.SubphaseState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPhaseProgressServiceTest {

    @Mock
    private PhaseWorkflowPort phaseWorkflowPort;

    @InjectMocks
    private GetPhaseProgressService getPhaseProgressService;

    @Test
    void returnsPartialProgressRoundedToOneDecimalPlace() {
        UUID processId = UUID.randomUUID();
        UUID phaseId = UUID.randomUUID();
        givenExistingPhase(processId, phaseId);
        when(phaseWorkflowPort.listSubphasesWithStatus(phaseId)).thenReturn(List.of(
                subphase(SubphaseState.APROBADO),
                subphase(SubphaseState.APROBADO),
                subphase(SubphaseState.APROBADO),
                subphase(SubphaseState.PENDIENTE),
                subphase(SubphaseState.OBSERVADO),
                subphase(SubphaseState.SUBIDO),
                subphase(SubphaseState.SUBSANADO)));
        when(phaseWorkflowPort.getCurrentState(phaseId)).thenReturn(PhaseState.ABIERTA);

        PhaseProgressResult result = getPhaseProgressService.getProgress(processId, phaseId);

        assertEquals(phaseId, result.phaseId());
        assertEquals(7, result.totalSubphases());
        assertEquals(3, result.approvedSubphases());
        assertEquals(4, result.pendingSubphases());
        assertEquals(42.9, result.progressPercent());
        assertEquals(PhaseState.ABIERTA, result.state());
        verify(phaseWorkflowPort).listSubphasesWithStatus(phaseId);
    }

    @Test
    void returnsOneHundredPercentWhenAllSubphasesAreApproved() {
        UUID processId = UUID.randomUUID();
        UUID phaseId = UUID.randomUUID();
        givenExistingPhase(processId, phaseId);
        when(phaseWorkflowPort.listSubphasesWithStatus(phaseId)).thenReturn(List.of(
                subphase(SubphaseState.APROBADO),
                subphase(SubphaseState.APROBADO)));
        when(phaseWorkflowPort.getCurrentState(phaseId)).thenReturn(PhaseState.COMPLETADA);

        PhaseProgressResult result = getPhaseProgressService.getProgress(processId, phaseId);

        assertEquals(2, result.totalSubphases());
        assertEquals(2, result.approvedSubphases());
        assertEquals(0, result.pendingSubphases());
        assertEquals(100.0, result.progressPercent());
        assertEquals(PhaseState.COMPLETADA, result.state());
    }

    @Test
    void returnsZeroProgressWhenPhaseHasNoSubphases() {
        UUID processId = UUID.randomUUID();
        UUID phaseId = UUID.randomUUID();
        givenExistingPhase(processId, phaseId);
        when(phaseWorkflowPort.listSubphasesWithStatus(phaseId)).thenReturn(List.of());
        when(phaseWorkflowPort.getCurrentState(phaseId)).thenReturn(PhaseState.ABIERTA);

        PhaseProgressResult result = getPhaseProgressService.getProgress(processId, phaseId);

        assertEquals(0, result.totalSubphases());
        assertEquals(0, result.approvedSubphases());
        assertEquals(0, result.pendingSubphases());
        assertEquals(0.0, result.progressPercent());
    }

    @Test
    void rejectsMissingProcessPhaseRelationship() {
        UUID processId = UUID.randomUUID();
        UUID phaseId = UUID.randomUUID();
        when(phaseWorkflowPort.findPhaseContext(processId, phaseId)).thenReturn(Optional.empty());

        assertThrows(ProcessNotFoundException.class,
                () -> getPhaseProgressService.getProgress(processId, phaseId));
    }

    private void givenExistingPhase(UUID processId, UUID phaseId) {
        when(phaseWorkflowPort.findPhaseContext(processId, phaseId))
                .thenReturn(Optional.of(new PhaseWorkflowPort.PhaseContext(
                        phaseId, processId, UUID.randomUUID(), "Fase")));
    }

    private PhaseWorkflowPort.SubphaseStatusItem subphase(SubphaseState state) {
        return new PhaseWorkflowPort.SubphaseStatusItem(UUID.randomUUID(), "Subfase", state, 1);
    }
}
