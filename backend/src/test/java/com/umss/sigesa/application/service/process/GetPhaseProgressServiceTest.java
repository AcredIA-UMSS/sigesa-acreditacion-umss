package com.umss.sigesa.application.service.process;

import com.umss.sigesa.application.model.process.PhaseProgress;
import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.IndicatorState;
import com.umss.sigesa.domain.model.PhaseState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPhaseProgressServiceTest {

    @Mock
    private NormativeHierarchyQueryPort normativeHierarchyQueryPort;

    private GetPhaseProgressService service;
    private final UUID processId = UUID.randomUUID();
    private final UUID phaseId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new GetPhaseProgressService(normativeHierarchyQueryPort);
        when(normativeHierarchyQueryPort.findLevel1Context(phaseId))
                .thenReturn(Optional.of(new NormativeHierarchyQueryPort.Level1Context(
                        phaseId, processId, UUID.randomUUID(), "Fase", PhaseState.ABIERTA)));
    }

    @Test
    void calculatesPartialProgressRoundedToOneDecimal() {
        when(normativeHierarchyQueryPort.listIndicatorsWithStatusByLevel1Id(phaseId))
                .thenReturn(List.of(indicator(IndicatorState.APROBADO), indicator(IndicatorState.PENDIENTE),
                        indicator(IndicatorState.PENDIENTE), indicator(IndicatorState.SUBIDO),
                        indicator(IndicatorState.PENDIENTE), indicator(IndicatorState.PENDIENTE)));

        PhaseProgress progress = service.getProgress(processId, phaseId);

        assertEquals(6, progress.totalSubphases());
        assertEquals(1, progress.approvedSubphases());
        assertEquals(5, progress.pendingSubphases());
        assertEquals(16.7, progress.progressPercent());
        assertEquals(PhaseState.ABIERTA, progress.state());
    }

    @Test
    void returnsOneHundredPercentWhenAllIndicatorsAreApproved() {
        when(normativeHierarchyQueryPort.listIndicatorsWithStatusByLevel1Id(phaseId))
                .thenReturn(List.of(indicator(IndicatorState.APROBADO), indicator(IndicatorState.APROBADO)));

        PhaseProgress progress = service.getProgress(processId, phaseId);

        assertEquals(2, progress.totalSubphases());
        assertEquals(2, progress.approvedSubphases());
        assertEquals(0, progress.pendingSubphases());
        assertEquals(100.0, progress.progressPercent());
    }

    @Test
    void returnsZeroProgressWhenThereAreNoIndicators() {
        when(normativeHierarchyQueryPort.listIndicatorsWithStatusByLevel1Id(phaseId)).thenReturn(List.of());

        PhaseProgress progress = service.getProgress(processId, phaseId);

        assertEquals(0, progress.totalSubphases());
        assertEquals(0, progress.approvedSubphases());
        assertEquals(0, progress.pendingSubphases());
        assertEquals(0.0, progress.progressPercent());
    }

    @Test
    void missingPhaseThrowsNotFound() {
        when(normativeHierarchyQueryPort.findLevel1Context(phaseId)).thenReturn(Optional.empty());

        assertThrows(ProcessNotFoundException.class, () -> service.getProgress(processId, phaseId));
    }

    @Test
    void phaseFromAnotherProcessThrowsNotFound() {
        UUID otherProcessId = UUID.randomUUID();
        when(normativeHierarchyQueryPort.findLevel1Context(phaseId))
                .thenReturn(Optional.of(new NormativeHierarchyQueryPort.Level1Context(
                        phaseId, otherProcessId, UUID.randomUUID(), "Fase", PhaseState.ABIERTA)));

        assertThrows(ProcessNotFoundException.class, () -> service.getProgress(processId, phaseId));
    }

    private NormativeHierarchyQueryPort.IndicatorStatusItem indicator(IndicatorState state) {
        return new NormativeHierarchyQueryPort.IndicatorStatusItem(
                UUID.randomUUID(), "IND", "Indicador", state, 1);
    }
}
