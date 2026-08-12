package com.umss.sigesa.application.service.process;

import com.umss.sigesa.application.port.out.ProcessStructurePort;
import com.umss.sigesa.domain.exception.SubphaseLinkRequiredException;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddSubphaseServiceTest {

    @Mock
    private ProcessStructurePort processStructurePort;

    @Mock
    private ProcessStructureGuard guard;

    @InjectMocks
    private AddProcessSubphaseService addProcessSubphaseService;

    @Test
    void shouldAddSubphaseWithValidLink() {
        UUID processId = UUID.randomUUID();
        UUID phaseId = UUID.randomUUID();
        AccreditationProcess process = AccreditationProcess.builder()
                .status("ACTIVE")
                .phases(List.of(Phase.builder().id(phaseId).order(1).build()))
                .build();

        when(processStructurePort.loadActiveProcess(processId)).thenReturn(process);
        when(processStructurePort.saveSubphase(eq(processId), eq(phaseId), any(Subphase.class)))
                .thenAnswer(invocation -> {
                    Subphase subphase = invocation.getArgument(2);
                    return Subphase.builder()
                            .id(UUID.randomUUID())
                            .name(subphase.getName())
                            .order(subphase.getOrder())
                            .referenceUrl(subphase.getReferenceUrl())
                            .description(subphase.getDescription())
                            .build();
                });

        Subphase created = addProcessSubphaseService.execute(
                processId,
                phaseId,
                "Informe parcial",
                2,
                "https://duea.umss.edu.bo/ref/informe",
                "Descripción");

        assertNotNull(created.getId());
        assertEquals("Informe parcial", created.getName());
        assertEquals("https://duea.umss.edu.bo/ref/informe", created.getReferenceUrl());
        verify(guard).ensureProcessActive(process);
        verify(guard).ensureReferenceUrl("https://duea.umss.edu.bo/ref/informe");
        verify(processStructurePort).saveSubphase(eq(processId), eq(phaseId), any(Subphase.class));
    }

    @Test
    void shouldRejectInvalidReferenceUrl() {
        UUID processId = UUID.randomUUID();
        UUID phaseId = UUID.randomUUID();
        AccreditationProcess process = AccreditationProcess.builder()
                .status("ACTIVE")
                .phases(List.of(Phase.builder().id(phaseId).order(1).build()))
                .build();

        when(processStructurePort.loadActiveProcess(processId)).thenReturn(process);
        org.mockito.Mockito.doThrow(new SubphaseLinkRequiredException("invalid"))
                .when(guard).ensureReferenceUrl("http://bad.url");

        assertThrows(SubphaseLinkRequiredException.class, () -> addProcessSubphaseService.execute(
                processId, phaseId, "Sub", 1, "http://bad.url", null));
    }
}
