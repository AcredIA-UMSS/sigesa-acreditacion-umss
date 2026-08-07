package com.umss.sigesa.application.service.process;

import com.umss.sigesa.application.model.process.ProcessQueryContext;
import com.umss.sigesa.application.model.process.ProcessSummary;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.ProgramCatalogPort;
import com.umss.sigesa.application.port.out.TemplatePort;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListProcessesServiceTest {

    @Mock
    private ProcessQueryPort processQueryPort;
    @Mock
    private ProgramCatalogPort programCatalogPort;
    @Mock
    private TemplatePort templatePort;

    private ListProcessesService service;

    private final UUID careerA = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private final UUID careerB = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
    private final UUID templateId = UUID.fromString("850e8400-e29b-41d4-a716-446655440010");
    private final UUID processA = UUID.randomUUID();
    private final UUID processB = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new ListProcessesService(processQueryPort, programCatalogPort, templatePort);
    }

    @Test
    void jdSeesAllProcesses() {
        when(processQueryPort.findAllSummaryItems()).thenReturn(List.of(
                listItem(processA, careerA),
                listItem(processB, careerB)
        ));
        stubEnrichment(careerA, "INF-SIS", "Ingeniería de Sistemas");
        stubEnrichment(careerB, "CIV", "Ingeniería Civil");

        List<ProcessSummary> result = service.list(new ProcessQueryContext("JD", List.of()));

        assertEquals(2, result.size());
        verify(processQueryPort).findAllSummaryItems();
    }

    @Test
    void tdSeesAllProcesses() {
        when(processQueryPort.findAllSummaryItems()).thenReturn(List.of(listItem(processA, careerA)));
        stubEnrichment(careerA, "INF-SIS", "Ingeniería de Sistemas");

        List<ProcessSummary> result = service.list(new ProcessQueryContext("TD", List.of()));

        assertEquals(1, result.size());
        verify(processQueryPort).findAllSummaryItems();
    }

    @Test
    void ccSeesOnlyAssignedCareer() {
        when(processQueryPort.findSummaryItemsByCareerIds(List.of(careerA)))
                .thenReturn(List.of(listItem(processA, careerA)));
        stubEnrichment(careerA, "INF-SIS", "Ingeniería de Sistemas");

        List<ProcessSummary> result = service.list(new ProcessQueryContext("CC", List.of(careerA)));

        assertEquals(1, result.size());
        assertEquals(careerA, result.get(0).careerId());
        verify(processQueryPort).findSummaryItemsByCareerIds(List.of(careerA));
    }

    @Test
    void ccWithEmptyScopeReturnsEmptyList() {
        List<ProcessSummary> result = service.list(new ProcessQueryContext("CC", List.of()));

        assertTrue(result.isEmpty());
    }

    private ProcessQueryPort.ProcessListItem listItem(UUID processId, UUID careerId) {
        return new ProcessQueryPort.ProcessListItem(
                processId, careerId, templateId, "ACTIVE", LocalDateTime.now(), 2, 5
        );
    }

    private void stubEnrichment(UUID careerId, String code, String name) {
        when(programCatalogPort.findById(careerId))
                .thenReturn(Optional.of(new ProgramCatalogPort.ProgramEntry(careerId, code, name)));
        when(templatePort.findMetadataById(templateId))
                .thenReturn(Optional.of(Template.builder().id(templateId).name("CEUB 2026").type("CEUB").build()));
    }
}
