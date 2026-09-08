package com.umss.sigesa.application.usecase;

import com.umss.sigesa.application.port.out.AccreditationProcessPort;
import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.application.port.out.ProgramCatalogPort;
import com.umss.sigesa.application.port.out.TemplatePort;
import com.umss.sigesa.application.service.process.ProcessNormativeTreeCloner;
import com.umss.sigesa.domain.exception.ProcessAlreadyActiveException;
import com.umss.sigesa.domain.exception.ProgramNotFoundException;
import com.umss.sigesa.domain.exception.TemplateNotPublishedException;
import com.umss.sigesa.domain.exception.TemplateStructureIncompleteException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.Template;
import com.umss.sigesa.domain.model.TemplateLevel1Node;
import com.umss.sigesa.domain.model.TemplatePhase;
import com.umss.sigesa.domain.model.TemplateStatus;
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

    @Mock
    private NormativeHierarchyQueryPort hierarchyQueryPort;

    @Mock
    private ProcessNormativeTreeCloner normativeTreeCloner;

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
                .referenceUrl("https://duea.umss.edu.bo/guia/sub-1")
                .description("Guía subfase")
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
                .status(TemplateStatus.PUBLISHED)
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
    void shouldCreateProcessSuccessfullyWithLegacyStructure() {
        when(programCatalogPort.findById(careerId))
                .thenReturn(Optional.of(new ProgramCatalogPort.ProgramEntry(careerId, "INF-SIS", "Ingeniería de Sistemas")));
        when(templatePort.findById(templateId)).thenReturn(Optional.of(template));
        when(processPort.existsActiveProcessByCareerAndTemplateType(careerId, "CEUB")).thenReturn(false);
        when(hierarchyQueryPort.countIndicatorsByTemplateId(templateId)).thenReturn(0L);
        when(hierarchyQueryPort.findTemplateTree(templateId)).thenReturn(Optional.empty());
        when(processPort.save(any(AccreditationProcess.class))).thenAnswer(invocation -> {
            AccreditationProcess process = invocation.getArgument(0);
            process.setId(UUID.randomUUID());
            return process;
        });

        AccreditationProcess result = useCase.createProcess(careerId, templateId);

        assertNotNull(result);
        assertEquals("ACTIVE", result.getStatus());
        assertEquals(careerId, result.getCareerId());
        assertEquals(1, result.getPhases().size());
        assertEquals("Fase 1", result.getPhases().get(0).getName());
        assertEquals(1, result.getPhases().get(0).getSubphases().size());
        assertEquals("https://duea.umss.edu.bo/guia/sub-1",
                result.getPhases().get(0).getSubphases().get(0).getReferenceUrl());

        verify(processPort, times(1)).save(any(AccreditationProcess.class));
        verify(normativeTreeCloner, never()).cloneFromTemplate(any(), any());
    }

    @Test
    void shouldCloneNormativeTreeWhenTemplateHasIndicators() {
        UUID processId = UUID.randomUUID();
        List<TemplateLevel1Node> templateTree = List.of(
                TemplateLevel1Node.builder().name("Área").order(1).build());

        when(programCatalogPort.findById(careerId))
                .thenReturn(Optional.of(new ProgramCatalogPort.ProgramEntry(careerId, "INF-SIS", "Ingeniería de Sistemas")));
        when(templatePort.findById(templateId)).thenReturn(Optional.of(template));
        when(processPort.existsActiveProcessByCareerAndTemplateType(careerId, "CEUB")).thenReturn(false);
        when(hierarchyQueryPort.countIndicatorsByTemplateId(templateId)).thenReturn(2L);
        when(hierarchyQueryPort.findTemplateTree(templateId))
                .thenReturn(Optional.of(new NormativeHierarchyQueryPort.TemplateNormativeTree(templateId, templateTree)));
        when(processPort.save(any(AccreditationProcess.class))).thenAnswer(invocation -> {
            AccreditationProcess process = invocation.getArgument(0);
            process.setId(processId);
            return process;
        });

        useCase.createProcess(careerId, templateId);

        verify(normativeTreeCloner).cloneFromTemplate(eq(processId), eq(templateTree));
    }

    @Test
    void shouldRejectTemplateWithoutNormativeTreeOrLegacyPhases() {
        Template emptyTemplate = Template.builder()
                .id(templateId)
                .name("CEUB")
                .type("CEUB")
                .status(TemplateStatus.PUBLISHED)
                .phases(List.of())
                .build();

        when(programCatalogPort.findById(careerId))
                .thenReturn(Optional.of(new ProgramCatalogPort.ProgramEntry(careerId, "INF-SIS", "Ingeniería de Sistemas")));
        when(templatePort.findById(templateId)).thenReturn(Optional.of(emptyTemplate));
        when(processPort.existsActiveProcessByCareerAndTemplateType(careerId, "CEUB")).thenReturn(false);
        when(hierarchyQueryPort.countIndicatorsByTemplateId(templateId)).thenReturn(0L);
        when(hierarchyQueryPort.findTemplateTree(templateId)).thenReturn(Optional.empty());

        assertThrows(TemplateStructureIncompleteException.class, () -> useCase.createProcess(careerId, templateId));
        verify(processPort, never()).save(any());
    }

    @Test
    void shouldRejectDraftTemplate() {
        Template draftTemplate = Template.builder()
                .id(templateId)
                .name("CEUB")
                .type("CEUB")
                .status(TemplateStatus.DRAFT)
                .phases(template.getPhases())
                .build();

        when(programCatalogPort.findById(careerId))
                .thenReturn(Optional.of(new ProgramCatalogPort.ProgramEntry(careerId, "INF-SIS", "Ingeniería de Sistemas")));
        when(templatePort.findById(templateId)).thenReturn(Optional.of(draftTemplate));

        assertThrows(TemplateNotPublishedException.class, () -> useCase.createProcess(careerId, templateId));
        verify(processPort, never()).save(any());
    }

    @Test
    void shouldAllowActiveProcessWithDifferentTemplateType() {
        Template arcuTemplate = Template.builder()
                .id(UUID.randomUUID())
                .name("ARCU-SUR")
                .type("ARCU-SUR")
                .status(TemplateStatus.PUBLISHED)
                .phases(template.getPhases())
                .build();

        when(programCatalogPort.findById(careerId))
                .thenReturn(Optional.of(new ProgramCatalogPort.ProgramEntry(careerId, "INF-SIS", "Ingeniería de Sistemas")));
        when(templatePort.findById(arcuTemplate.getId())).thenReturn(Optional.of(arcuTemplate));
        when(processPort.existsActiveProcessByCareerAndTemplateType(careerId, "ARCU-SUR")).thenReturn(false);
        when(hierarchyQueryPort.countIndicatorsByTemplateId(arcuTemplate.getId())).thenReturn(0L);
        when(hierarchyQueryPort.findTemplateTree(arcuTemplate.getId())).thenReturn(Optional.empty());
        when(processPort.save(any(AccreditationProcess.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AccreditationProcess result = useCase.createProcess(careerId, arcuTemplate.getId());

        assertNotNull(result);
        verify(processPort).existsActiveProcessByCareerAndTemplateType(eq(careerId), eq("ARCU-SUR"));
        verify(processPort, times(1)).save(any(AccreditationProcess.class));
    }
}
