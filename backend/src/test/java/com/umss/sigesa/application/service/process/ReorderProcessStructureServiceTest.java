package com.umss.sigesa.application.service.process;

import com.umss.sigesa.application.port.out.ProcessStructurePort;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.Phase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReorderProcessStructureServiceTest {

    @Mock
    private ProcessStructurePort processStructurePort;

    @Mock
    private ProcessStructureGuard guard;

    @InjectMocks
    private ReorderProcessStructureService reorderProcessStructureService;

    @Test
    void shouldReorderPhasesAndSubphases() {
        UUID processId = UUID.randomUUID();
        UUID phaseId = UUID.randomUUID();
        UUID phaseA = UUID.randomUUID();
        UUID phaseB = UUID.randomUUID();
        UUID subA = UUID.randomUUID();
        UUID subB = UUID.randomUUID();

        AccreditationProcess process = AccreditationProcess.builder()
                .status("ACTIVE")
                .phases(List.of(
                        Phase.builder().id(phaseA).order(2).build(),
                        Phase.builder().id(phaseB).order(1).build()))
                .build();

        when(processStructurePort.loadActiveProcess(processId)).thenReturn(process, process);

        reorderProcessStructureService.execute(
                processId,
                List.of(phaseB, phaseA),
                Map.of(phaseId, List.of(subB, subA)));

        verify(guard).ensureProcessActive(process);
        verify(processStructurePort).reorderPhases(processId, List.of(phaseB, phaseA));
        verify(processStructurePort).reorderSubphases(processId, phaseId, List.of(subB, subA));
    }
}
