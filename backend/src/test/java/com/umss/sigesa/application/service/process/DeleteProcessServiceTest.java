package com.umss.sigesa.application.service.process;

import com.umss.sigesa.application.port.out.AccreditationProcessPort;
import com.umss.sigesa.application.port.out.EvaluationMetricsPort;
import com.umss.sigesa.domain.exception.ProcessHasEvidenceException;
import com.umss.sigesa.domain.exception.ProcessNotDeletableException;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.AccreditationProcess;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteProcessServiceTest {

    @Mock
    private AccreditationProcessPort accreditationProcessPort;

    @Mock
    private EvaluationMetricsPort evaluationMetricsPort;

    @InjectMocks
    private DeleteProcessService service;

    private final UUID processId = UUID.randomUUID();

    @Test
    void shouldArchiveActiveProcessWithoutEvidence() {
        AccreditationProcess process = AccreditationProcess.builder()
                .id(processId)
                .status("ACTIVE")
                .build();
        when(accreditationProcessPort.findById(processId)).thenReturn(Optional.of(process));
        when(evaluationMetricsPort.countIndicatorsWithEvidenceByProcessId(processId)).thenReturn(0L);

        service.delete(processId);

        assertEquals("ARCHIVED", process.getStatus());
        verify(accreditationProcessPort).save(process);
    }

    @Test
    void shouldRejectWhenProcessNotFound() {
        when(accreditationProcessPort.findById(processId)).thenReturn(Optional.empty());

        assertThrows(ProcessNotFoundException.class, () -> service.delete(processId));
        verify(accreditationProcessPort, never()).save(any());
    }

    @Test
    void shouldArchiveClosedProcessEvenWithEvidence() {
        AccreditationProcess process = AccreditationProcess.builder()
                .id(processId)
                .status("CLOSED")
                .build();
        when(accreditationProcessPort.findById(processId)).thenReturn(Optional.of(process));

        service.delete(processId);

        assertEquals("ARCHIVED", process.getStatus());
        verify(accreditationProcessPort).save(process);
        verify(evaluationMetricsPort, never()).countIndicatorsWithEvidenceByProcessId(processId);
    }

    @Test
    void shouldRejectWhenProcessAlreadyArchived() {
        AccreditationProcess process = AccreditationProcess.builder()
                .id(processId)
                .status("ARCHIVED")
                .build();
        when(accreditationProcessPort.findById(processId)).thenReturn(Optional.of(process));

        assertThrows(ProcessNotDeletableException.class, () -> service.delete(processId));
        verify(accreditationProcessPort, never()).save(any());
    }

    @Test
    void shouldRejectWhenProcessHasEvidence() {
        AccreditationProcess process = AccreditationProcess.builder()
                .id(processId)
                .status("ACTIVE")
                .build();
        when(accreditationProcessPort.findById(processId)).thenReturn(Optional.of(process));
        when(evaluationMetricsPort.countIndicatorsWithEvidenceByProcessId(processId)).thenReturn(2L);

        assertThrows(ProcessHasEvidenceException.class, () -> service.delete(processId));
        verify(accreditationProcessPort, never()).save(any());
    }
}
