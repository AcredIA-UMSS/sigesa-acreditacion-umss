package com.umss.sigesa.application.service.process;

import com.umss.sigesa.application.model.process.EnrichedProcessDetail;
import com.umss.sigesa.application.model.process.ProcessQueryContext;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.ProgramCatalogPort;
import com.umss.sigesa.application.port.out.TemplatePort;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.Phase;
import com.umss.sigesa.domain.model.Subphase;
import com.umss.sigesa.domain.model.Template;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetProcessDetailServiceTest {

    @Mock
    private ProcessQueryPort processQueryPort;
    @Mock
    private ProgramCatalogPort programCatalogPort;
    @Mock
    private TemplatePort templatePort;

    private GetProcessDetailService service;

    private final UUID processId = UUID.randomUUID();
    private final UUID careerA = UUID.randomUUID();
    private final UUID careerB = UUID.randomUUID();
    private final UUID templateId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new GetProcessDetailService(processQueryPort, programCatalogPort, templatePort);
    }

    @Test
    void jdGetsDetailWithSortedPhases() {
        AccreditationProcess process = buildProcessWithUnsortedPhases();
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.of(process));
        stubEnrichment(careerA);

        EnrichedProcessDetail detail = service.getDetail(processId, new ProcessQueryContext("JD", List.of()));

        assertEquals("Fase 1", detail.phases().get(0).getName());
        assertEquals("Sub 1", detail.phases().get(0).getSubphases().get(0).getName());
        assertEquals("CEUB 2026", detail.templateName());
    }

    @Test
    void ccCannotAccessForeignProcess() {
        AccreditationProcess process = AccreditationProcess.builder()
                .id(processId)
                .careerId(careerB)
                .templateId(templateId)
                .status("ACTIVE")
                .startDate(LocalDateTime.now())
                .phases(List.of())
                .build();
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.of(process));

        assertThrows(ProcessNotFoundException.class,
                () -> service.getDetail(processId, new ProcessQueryContext("CC", List.of(careerA))));
    }

    @Test
    void missingProcessThrowsNotFound() {
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.empty());

        assertThrows(ProcessNotFoundException.class,
                () -> service.getDetail(processId, new ProcessQueryContext("JD", List.of())));
    }

    private AccreditationProcess buildProcessWithUnsortedPhases() {
        Phase phase2 = Phase.builder()
                .id(UUID.randomUUID())
                .name("Fase 2")
                .order(2)
                .subphases(List.of(Subphase.builder().id(UUID.randomUUID()).name("Sub 2").order(2).build()))
                .build();
        Phase phase1 = Phase.builder()
                .id(UUID.randomUUID())
                .name("Fase 1")
                .order(1)
                .subphases(List.of(
                        Subphase.builder().id(UUID.randomUUID()).name("Sub 2").order(2).build(),
                        Subphase.builder().id(UUID.randomUUID()).name("Sub 1").order(1).build()
                ))
                .build();

        return AccreditationProcess.builder()
                .id(processId)
                .careerId(careerA)
                .templateId(templateId)
                .status("ACTIVE")
                .startDate(LocalDateTime.now())
                .phases(new java.util.ArrayList<>(List.of(phase2, phase1)))
                .build();
    }

    private void stubEnrichment(UUID careerId) {
        when(programCatalogPort.findById(careerId))
                .thenReturn(Optional.of(new ProgramCatalogPort.ProgramEntry(careerId, "INF-SIS", "Ingeniería de Sistemas")));
        when(templatePort.findMetadataById(templateId))
                .thenReturn(Optional.of(Template.builder().id(templateId).name("CEUB 2026").type("CEUB").build()));
    }
}
