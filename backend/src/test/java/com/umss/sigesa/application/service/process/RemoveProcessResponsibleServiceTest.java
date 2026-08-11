package com.umss.sigesa.application.service.process;

import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.ProcessResponsiblePort;
import com.umss.sigesa.domain.model.AccreditationProcess;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RemoveProcessResponsibleServiceTest {

    @Mock
    private ProcessQueryPort processQueryPort;
    @Mock
    private ProcessResponsiblePort processResponsiblePort;
    @Mock
    private ProcessStructureGuard processStructureGuard;

    @InjectMocks
    private RemoveProcessResponsibleService service;

    @Test
    void shouldRevokeActiveAssignment() {
        UUID processId = UUID.randomUUID();
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.of(
                AccreditationProcess.builder()
                        .id(processId)
                        .status("ACTIVE")
                        .startDate(LocalDateTime.now())
                        .phases(List.of())
                        .build()));

        service.remove(processId);

        verify(processResponsiblePort).revokeActiveByProcessId(processId);
    }
}
