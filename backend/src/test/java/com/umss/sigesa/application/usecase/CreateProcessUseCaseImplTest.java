package com.umss.sigesa.application.usecase;

import com.umss.sigesa.application.port.out.AccreditationProcessPort;
import com.umss.sigesa.application.port.out.ProgramCatalogPort;
import com.umss.sigesa.application.port.out.TemplatePort;
import com.umss.sigesa.domain.exception.ProcessAlreadyActiveException;
import com.umss.sigesa.domain.exception.ProgramNotFoundException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.Template;
import com.umss.sigesa.domain.model.TemplatePhase;
import com.umss.sigesa.domain.model.TemplateSubphase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateProcessUseCaseImplTest {

    @Mock
    private AccreditationProcessPort processPort;

    @Mock
    private TemplatePort templatePort;

    @Mock
    private ProgramCatalogPort programCatalogPort;

    @InjectMocks
    private CreateProcessUseCaseImpl useCase;

    private UUID careerId;
    private UUID templateId;
    private Template template;

    @BeforeEach
    void setUp() {
        careerId = UUID.randomUUID();
        templateId = UUID.randomUUID();

        TemplateSubphase subphase = TemplateSubphase.builder()
                .name("Sub 1")
                .order(1)
                .build();

        List<TemplateSubphase> subphasesList = new ArrayList<>();
        subphasesList.add(subphase);

        TemplatePhase phase = TemplatePhase.builder()
                .name("Fase 1")
                .order(1)
                .subphases(subphasesList)
                .build();

        List<TemplatePhase> phasesList = new ArrayList<>();
        phasesList.add(phase);

        template = Template.builder()
                .id(templateId)
                .name("CEUB")
                .type("CEUB")
                .phases(phasesList)
                .build();
    }

    @Test
    void shouldThrowExceptionIfProgramNotFound() {
        when(programCatalogPort.findById(careerId)).thenReturn(Optional.empty());

        assertThrows(ProgramNotFoundException.class, () -> useCase.createProcess(careerId, templateId));

        verify(processPort, never()).existsActiveProcessByCareerAndTemplateType(any(), any());
        verify(processPort, never()).save(any());
    }

    @Test
    void shouldThrowExceptionIfActiveProcessExistsForSameTemplateType() {
        when(programCatalogPort.findById(careerId))
                .thenReturn(Optional.of(new ProgramCatalogPort.ProgramEntry(careerId, "INF-SIS", "Ingeniería de Sistemas")));
        when(templatePort.findById(templateId)).thenReturn(Optional.of(template));
        when(processPort.existsActiveProcessByCareerAndTemplateType(careerId, "CEUB")).thenReturn(true);

        assertThrows(ProcessAlreadyActiveException.class, () -> useCase.createProcess(careerId, templateId));

        verify(processPort, never()).save(any());
    }

    @Test
    void shouldCreateProcessSuccessfully() {
        when(programCatalogPort.findById(careerId))
                .thenReturn(Optional.of(new ProgramCatalogPort.ProgramEntry(careerId, "INF-SIS", "Ingeniería de Sistemas")));
        when(templatePort.findById(templateId)).thenReturn(Optional.of(template));
        when(processPort.existsActiveProcessByCareerAndTemplateType(careerId, "CEUB")).thenReturn(false);
        when(processPort.save(any(AccreditationProcess.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AccreditationProcess result = useCase.createProcess(careerId, templateId);

        assertNotNull(result);
        assertEquals("ACTIVE", result.getStatus());
        assertEquals(careerId, result.getCareerId());
        assertEquals(1, result.getPhases().size());
        assertEquals("Fase 1", result.getPhases().get(0).getName());
        assertEquals(1, result.getPhases().get(0).getSubphases().size());

        verify(processPort, times(1)).save(any(AccreditationProcess.class));
    }

    @Test
    void shouldAllowActiveProcessWithDifferentTemplateType() {
        Template arcuTemplate = Template.builder()
                .id(UUID.randomUUID())
                .name("ARCU-SUR")
                .type("ARCU-SUR")
                .phases(template.getPhases())
                .build();

        when(programCatalogPort.findById(careerId))
                .thenReturn(Optional.of(new ProgramCatalogPort.ProgramEntry(careerId, "INF-SIS", "Ingeniería de Sistemas")));
        when(templatePort.findById(arcuTemplate.getId())).thenReturn(Optional.of(arcuTemplate));
        when(processPort.existsActiveProcessByCareerAndTemplateType(careerId, "ARCU-SUR")).thenReturn(false);
        when(processPort.save(any(AccreditationProcess.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AccreditationProcess result = useCase.createProcess(careerId, arcuTemplate.getId());

        assertNotNull(result);
        verify(processPort).existsActiveProcessByCareerAndTemplateType(eq(careerId), eq("ARCU-SUR"));
        verify(processPort, times(1)).save(any(AccreditationProcess.class));
    }
}
