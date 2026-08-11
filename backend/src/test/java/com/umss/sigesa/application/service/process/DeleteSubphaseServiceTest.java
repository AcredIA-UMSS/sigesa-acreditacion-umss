package com.umss.sigesa.application.service.process;

import com.umss.sigesa.application.port.out.ProcessStructurePort;
import com.umss.sigesa.application.port.out.SubphaseWorkflowPort;
import com.umss.sigesa.domain.exception.SubphaseHasEvidenceException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.Phase;
import com.umss.sigesa.domain.model.Subphase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteSubphaseServiceTest {

    @Mock
    private ProcessStructurePort processStructurePort;

    @Mock
    private SubphaseWorkflowPort subphaseWorkflowPort;

    @Mock
    private ProcessStructureGuard guard;

    @InjectMocks
    private DeleteProcessSubphaseService deleteProcessSubphaseService;

    @Test
    void shouldRejectDeleteWhenBlockingEvidenceExists() {
        UUID processId = UUID.randomUUID();
        UUID phaseId = UUID.randomUUID();
        UUID subphaseId = UUID.randomUUID();
        Phase phase = Phase.builder()
                .id(phaseId)
                .subphases(List.of(Subphase.builder().id(subphaseId).order(1).build()))
                .build();
        AccreditationProcess process = AccreditationProcess.builder()
                .status("ACTIVE")
                .phases(List.of(phase))
                .build();

        when(processStructurePort.loadActiveProcess(processId)).thenReturn(process);
        when(guard.findPhase(process, phaseId)).thenReturn(phase);
        when(guard.findSubphase(phase, subphaseId)).thenReturn(phase.getSubphases().get(0));
        when(subphaseWorkflowPort.hasBlockingEvidence(subphaseId)).thenReturn(true);

        assertThrows(SubphaseHasEvidenceException.class,
                () -> deleteProcessSubphaseService.execute(processId, phaseId, subphaseId));
    }

    @Test
    void shouldDeleteWhenNoBlockingEvidence() {
        UUID processId = UUID.randomUUID();
        UUID phaseId = UUID.randomUUID();
        UUID subphaseId = UUID.randomUUID();
        Phase phase = Phase.builder()
                .id(phaseId)
                .subphases(List.of(Subphase.builder().id(subphaseId).order(1).build()))
                .build();
        AccreditationProcess process = AccreditationProcess.builder()
                .status("ACTIVE")
                .phases(List.of(phase))
                .build();

        when(processStructurePort.loadActiveProcess(processId)).thenReturn(process);
        when(guard.findPhase(process, phaseId)).thenReturn(phase);
        when(guard.findSubphase(phase, subphaseId)).thenReturn(phase.getSubphases().get(0));
        when(subphaseWorkflowPort.hasBlockingEvidence(subphaseId)).thenReturn(false);

        deleteProcessSubphaseService.execute(processId, phaseId, subphaseId);

        verify(processStructurePort).deleteSubphase(processId, phaseId, subphaseId);
    }
}
