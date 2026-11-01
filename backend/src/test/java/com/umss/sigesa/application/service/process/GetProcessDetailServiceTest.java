package com.umss.sigesa.application.service.process;

import com.umss.sigesa.application.model.process.EnrichedProcessDetail;
import com.umss.sigesa.application.model.process.ProcessQueryContext;
import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.ProcessResponsiblePort;
import com.umss.sigesa.application.port.out.ProgramCatalogPort;
import com.umss.sigesa.application.port.out.TemplatePort;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.Level1Node;
import com.umss.sigesa.domain.model.Level2Node;
import com.umss.sigesa.domain.model.Level3Node;
import com.umss.sigesa.domain.model.NormativeIndicator;
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
    @Mock
    private ProcessResponsiblePort processResponsiblePort;
    @Mock
    private UserRepositoryPort userRepositoryPort;
    @Mock
    private NormativeHierarchyQueryPort normativeHierarchyQueryPort;

    private GetProcessDetailService service;

    private final UUID processId = UUID.randomUUID();
    private final UUID careerA = UUID.randomUUID();
    private final UUID careerB = UUID.randomUUID();
    private final UUID templateId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new GetProcessDetailService(
                processQueryPort,
                programCatalogPort,
                templatePort,
                processResponsiblePort,
                userRepositoryPort,
                normativeHierarchyQueryPort);
    }

    @Test
    void jdGetsDetailWithTemplateMetadata() {
        AccreditationProcess process = buildProcess();
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.of(process));
        stubEnrichment(careerA);
        when(normativeHierarchyQueryPort.findProcessTree(processId)).thenReturn(Optional.empty());

        EnrichedProcessDetail detail = service.getDetail(processId, new ProcessQueryContext("JD", List.of()));

        assertEquals("CEUB 2026", detail.templateName());
        assertEquals(0, detail.level1Nodes().size());
    }

    @Test
    void jdGetsDetailWithImmutableNormativeTree() {
        AccreditationProcess process = buildProcess();
        when(processQueryPort.findDetailById(processId)).thenReturn(Optional.of(process));
        stubEnrichment(careerA);

        Level1Node level1B = Level1Node.builder()
                .id(UUID.randomUUID())
                .name("N1-B")
                .order(2)
                .level2Nodes(List.of())
                .build();
        Level1Node level1A = Level1Node.builder()
                .id(UUID.randomUUID())
                .name("N1-A")
                .order(1)
                .level2Nodes(List.of(Level2Node.builder()
                        .id(UUID.randomUUID())
                        .name("N2")
                        .order(1)
                        .level3Nodes(List.of(Level3Node.builder()
                                .id(UUID.randomUUID())
                                .name("N3")
                                .order(1)
                                .indicators(List.of(
                                        NormativeIndicator.builder().id(UUID.randomUUID()).code("I2").order(2).build(),
                                        NormativeIndicator.builder().id(UUID.randomUUID()).code("I1").order(1).build()
                                ))
                                .build()))
                        .build()))
                .build();

        when(normativeHierarchyQueryPort.findProcessTree(processId))
                .thenReturn(Optional.of(new NormativeHierarchyQueryPort.ProcessNormativeTree(
                        processId, List.of(level1B, level1A))));

        EnrichedProcessDetail detail = service.getDetail(processId, new ProcessQueryContext("JD", List.of()));

        assertEquals("N1-A", detail.level1Nodes().get(0).getName());
        assertEquals("I1", detail.level1Nodes().get(0).getLevel2Nodes().get(0)
                .getLevel3Nodes().get(0).getIndicators().get(0).getCode());
    }

    @Test
    void ccCannotAccessForeignProcess() {
        AccreditationProcess process = AccreditationProcess.builder()
                .id(processId)
                .careerId(careerB)
                .templateId(templateId)
                .status("ACTIVE")
                .startDate(LocalDateTime.now())
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

    private AccreditationProcess buildProcess() {
        return AccreditationProcess.builder()
                .id(processId)
                .careerId(careerA)
                .templateId(templateId)
                .status("ACTIVE")
                .startDate(LocalDateTime.now())
                .build();
    }

    private void stubEnrichment(UUID careerId) {
        when(programCatalogPort.findById(careerId))
                .thenReturn(Optional.of(new ProgramCatalogPort.ProgramEntry(careerId, "INF-SIS", "Ingeniería de Sistemas")));
        when(templatePort.findMetadataById(templateId))
                .thenReturn(Optional.of(Template.builder().id(templateId).name("CEUB 2026").type("CEUB").build()));
    }
}
