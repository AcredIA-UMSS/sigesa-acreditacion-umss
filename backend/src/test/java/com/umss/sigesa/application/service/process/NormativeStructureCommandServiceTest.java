package com.umss.sigesa.application.service.process;

import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.application.port.out.NormativeIndicatorWorkflowPort;
import com.umss.sigesa.application.port.out.NormativeStructurePort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.domain.exception.IndicatorHasEvidenceException;
import com.umss.sigesa.domain.exception.ProcessNotEditableException;
import com.umss.sigesa.domain.exception.ProcessStructureOrderConflictException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.IndicatorState;
import com.umss.sigesa.domain.model.Level1Node;
import com.umss.sigesa.domain.model.Level2Node;
import com.umss.sigesa.domain.model.Level3Node;
import com.umss.sigesa.domain.model.NormativeIndicator;
import com.umss.sigesa.domain.model.PhaseState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NormativeStructureCommandServiceTest {

    @Mock
    private ProcessQueryPort processQueryPort;
    @Mock
    private NormativeStructurePort structurePort;
    @Mock
    private NormativeHierarchyQueryPort hierarchyQueryPort;
    @Mock
    private NormativeIndicatorWorkflowPort indicatorWorkflowPort;

    private NormativeStructureCommandService service;

    private UUID processId;
    private UUID level1Id;
    private UUID level2Id;
    private UUID level3Id;
    private UUID indicatorId;

    @BeforeEach
    void setUp() {
        ProcessStructureGuard processStructureGuard = new ProcessStructureGuard();
        NormativeStructureGuard guard = new NormativeStructureGuard(processStructureGuard);
        service = new NormativeStructureCommandService(
                processQueryPort, structurePort, hierarchyQueryPort, indicatorWorkflowPort, guard);

        processId = UUID.randomUUID();
        level1Id = UUID.randomUUID();
        level2Id = UUID.randomUUID();
        level3Id = UUID.randomUUID();
        indicatorId = UUID.randomUUID();
    }

    @Test
    void shouldAddIndicatorToActiveProcess() {
        stubActiveProcess();
        stubProcessTree(indicatorList());

        NormativeIndicator saved = NormativeIndicator.builder()
                .id(UUID.randomUUID())
                .code("IND-PARC")
                .description("Indicador parcial")
                .weight(new BigDecimal("0.1"))
                .order(1)
                .referenceUrl("https://duea.umss.edu.bo/ref/informe")
                .status(IndicatorState.PENDIENTE)
                .build();

        when(structurePort.findProcessIdByLevel3(level3Id)).thenReturn(processId);
        when(structurePort.saveIndicator(eq(level3Id), any(NormativeIndicator.class))).thenReturn(saved);

        NormativeIndicator result = service.addIndicator(
                level3Id,
                "IND-PARC",
                "Indicador parcial",
                new BigDecimal("0.1"),
                1,
                "https://duea.umss.edu.bo/ref/informe");

        assertEquals("IND-PARC", result.getCode());
        verify(structurePort).saveIndicator(eq(level3Id), any(NormativeIndicator.class));
    }

    @Test
    void shouldBlockDeleteIndicatorWithWorkflowStarted() {
        stubActiveProcess();
        NormativeIndicator indicator = NormativeIndicator.builder()
                .id(indicatorId)
                .code("IND-01")
                .description("Con evidencia")
                .weight(BigDecimal.ONE)
                .order(1)
                .referenceUrl("https://duea.umss.edu.bo/ref")
                .status(IndicatorState.SUBIDO)
                .build();

        when(structurePort.findLevel3IdByIndicator(indicatorId)).thenReturn(level3Id);
        when(structurePort.findProcessIdByIndicator(indicatorId)).thenReturn(processId);
        when(hierarchyQueryPort.findIndicatorById(indicatorId)).thenReturn(Optional.of(indicator));
        when(indicatorWorkflowPort.isWorkflowStarted(indicatorId)).thenReturn(true);

        assertThrows(IndicatorHasEvidenceException.class, () -> service.deleteIndicator(indicatorId));
    }

    @Test
    void shouldBlockDeleteLevel1WithBlockingIndicators() {
        stubActiveProcess();
        NormativeIndicator blocking = NormativeIndicator.builder()
                .id(indicatorId)
                .code("IND-01")
                .description("Bloqueado")
                .weight(BigDecimal.ONE)
                .order(1)
                .referenceUrl("https://duea.umss.edu.bo/ref")
                .status(IndicatorState.SUBIDO)
                .build();

        when(structurePort.findIndicatorsUnderLevel1(level1Id)).thenReturn(List.of(blocking));
        when(indicatorWorkflowPort.isWorkflowStarted(indicatorId)).thenReturn(true);

        assertThrows(IndicatorHasEvidenceException.class, () -> service.deleteLevel1(processId, level1Id));
    }

    @Test
    void shouldRejectStructureChangesWhenProcessNotActive() {
        when(processQueryPort.findDetailById(processId))
                .thenReturn(Optional.of(AccreditationProcess.builder()
                        .id(processId)
                        .status("COMPLETED")
                        .build()));

        assertThrows(ProcessNotEditableException.class,
                () -> service.addLevel1(processId, "Dimensión", 1, null));
    }

    @Test
    void shouldRejectDuplicateLevel1Order() {
        stubActiveProcess();
        when(hierarchyQueryPort.findProcessTree(processId))
                .thenReturn(Optional.of(new NormativeHierarchyQueryPort.ProcessNormativeTree(
                        processId,
                        List.of(Level1Node.builder()
                                .id(level1Id)
                                .name("Existente")
                                .order(1)
                                .status(PhaseState.ABIERTA)
                                .build()))));

        assertThrows(ProcessStructureOrderConflictException.class,
                () -> service.addLevel1(processId, "Nueva dimensión", 1, null));
    }

    private void stubActiveProcess() {
        when(processQueryPort.findDetailById(processId))
                .thenReturn(Optional.of(AccreditationProcess.builder()
                        .id(processId)
                        .status("ACTIVE")
                        .build()));
    }

    private void stubProcessTree(List<NormativeIndicator> indicators) {
        Level3Node level3 = Level3Node.builder()
                .id(level3Id)
                .name("Criterio")
                .order(1)
                .indicators(indicators)
                .build();
        Level2Node level2 = Level2Node.builder()
                .id(level2Id)
                .name("Componente")
                .order(1)
                .level3Nodes(List.of(level3))
                .build();
        Level1Node level1 = Level1Node.builder()
                .id(level1Id)
                .name("Dimensión")
                .order(1)
                .status(PhaseState.ABIERTA)
                .level2Nodes(List.of(level2))
                .build();

        when(hierarchyQueryPort.findProcessTree(processId))
                .thenReturn(Optional.of(new NormativeHierarchyQueryPort.ProcessNormativeTree(
                        processId, List.of(level1))));
    }

    private List<NormativeIndicator> indicatorList() {
        return List.of();
    }
}
