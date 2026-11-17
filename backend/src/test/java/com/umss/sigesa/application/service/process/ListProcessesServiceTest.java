package com.umss.sigesa.application.service.process;

import com.umss.sigesa.application.model.process.ProcessQueryContext;
import com.umss.sigesa.application.model.process.ProcessSummary;
import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.ProcessResponsiblePort;
import com.umss.sigesa.application.port.out.ProgramCatalogPort;
import com.umss.sigesa.application.port.out.TemplatePort;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.domain.model.AppUser;
import com.umss.sigesa.domain.model.Email;
import com.umss.sigesa.domain.model.ProcessResponsibleAssignment;
import com.umss.sigesa.domain.model.Template;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListProcessesServiceTest {

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

    private ListProcessesService service;

    private final UUID careerA = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private final UUID careerB = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
    private final UUID templateId = UUID.fromString("850e8400-e29b-41d4-a716-446655440010");
    private final UUID processA = UUID.randomUUID();
    private final UUID processB = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new ListProcessesService(
                processQueryPort,
                programCatalogPort,
                templatePort,
                processResponsiblePort,
                userRepositoryPort,
                normativeHierarchyQueryPort);
        lenient().when(processResponsiblePort.findActiveByProcessIds(org.mockito.ArgumentMatchers.any())).thenReturn(List.of());
        lenient().when(normativeHierarchyQueryPort.countLevel1NodesByProcessIds(org.mockito.ArgumentMatchers.any()))
                .thenReturn(Map.of());
        lenient().when(normativeHierarchyQueryPort.countIndicatorsByProcessIds(org.mockito.ArgumentMatchers.any()))
                .thenReturn(Map.of());
        lenient().when(templatePort.findMetadataByIds(org.mockito.ArgumentMatchers.any()))
                .thenReturn(Map.of(templateId, Template.builder().id(templateId).name("CEUB 2026").type("CEUB").build()));
    }

    @Test
    void jdSeesAllProcesses() {
        when(processQueryPort.findAllSummaryItems()).thenReturn(List.of(
                listItem(processA, careerA),
                listItem(processB, careerB)
        ));
        when(programCatalogPort.findByIds(List.of(careerA, careerB))).thenReturn(Map.of(
                careerA, new ProgramCatalogPort.ProgramEntry(careerA, "INF-SIS", "Ingeniería de Sistemas"),
                careerB, new ProgramCatalogPort.ProgramEntry(careerB, "CIV", "Ingeniería Civil")
        ));
        when(normativeHierarchyQueryPort.countLevel1NodesByProcessIds(List.of(processA, processB)))
                .thenReturn(Map.of(processA, 5L, processB, 3L));
        when(normativeHierarchyQueryPort.countIndicatorsByProcessIds(List.of(processA, processB)))
                .thenReturn(Map.of(processA, 25L, processB, 15L));

        List<ProcessSummary> result = service.list(new ProcessQueryContext("JD", List.of()));

        assertEquals(2, result.size());
        assertEquals("INF-SIS", result.get(0).careerCode());
        assertEquals(5, result.get(0).level1Count());
        assertEquals(25, result.get(0).indicatorCount());
        assertEquals("CIV", result.get(1).careerCode());
        assertEquals(3, result.get(1).level1Count());
        assertEquals(15, result.get(1).indicatorCount());
        verify(processQueryPort).findAllSummaryItems();
        verify(programCatalogPort).findByIds(List.of(careerA, careerB));
        verify(templatePort).findMetadataByIds(List.of(templateId));
    }

    @Test
    void tdSeesAllProcesses() {
        when(processQueryPort.findAllSummaryItems()).thenReturn(List.of(listItem(processA, careerA)));
        stubEnrichment(careerA, "INF-SIS", "Ingeniería de Sistemas");

        List<ProcessSummary> result = service.list(new ProcessQueryContext("TD", List.of()));

        assertEquals(1, result.size());
        verify(processQueryPort).findAllSummaryItems();
        verify(programCatalogPort).findByIds(List.of(careerA));
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

    @Test
    void returnsEmptyImmediatelyWhenNoProcessesFound() {
        when(processQueryPort.findAllSummaryItems()).thenReturn(List.of());

        List<ProcessSummary> result = service.list(new ProcessQueryContext("JD", List.of()));

        assertTrue(result.isEmpty());
        verify(processQueryPort).findAllSummaryItems();
        verifyNoInteractions(programCatalogPort, templatePort, normativeHierarchyQueryPort, processResponsiblePort, userRepositoryPort);
    }

    @Test
    void resolvesResponsiblesInBatch() {
        when(processQueryPort.findAllSummaryItems()).thenReturn(List.of(listItem(processA, careerA)));
        stubEnrichment(careerA, "INF-SIS", "Ingeniería de Sistemas");
        UUID userId = UUID.randomUUID();
        LocalDateTime assignedAt = LocalDateTime.now();
        when(processResponsiblePort.findActiveByProcessIds(List.of(processA)))
                .thenReturn(List.of(ProcessResponsibleAssignment.builder()
                        .processId(processA)
                        .userId(userId)
                        .assignedAt(assignedAt)
                        .build()));
        when(userRepositoryPort.findByIds(List.of(userId)))
                .thenReturn(Map.of(userId, new AppUser(
                        userId,
                        Email.of("perez@umss.edu.bo"),
                        com.umss.sigesa.domain.model.Role.CC,
                        com.umss.sigesa.domain.model.UserStatus.ACTIVE,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        "Dr.",
                        "Perez",
                        null
                )));

        List<ProcessSummary> result = service.list(new ProcessQueryContext("JD", List.of()));

        assertEquals(1, result.size());
        assertNotNull(result.get(0).responsible());
        assertEquals("Dr. Perez", result.get(0).responsible().fullName());
        assertEquals("perez@umss.edu.bo", result.get(0).responsible().email());
        verify(processResponsiblePort).findActiveByProcessIds(List.of(processA));
        verify(userRepositoryPort).findByIds(List.of(userId));
    }

    private ProcessQueryPort.ProcessListItem listItem(UUID processId, UUID careerId) {
        return new ProcessQueryPort.ProcessListItem(
                processId, careerId, templateId, "ACTIVE", LocalDateTime.now()
        );
    }

    private void stubEnrichment(UUID careerId, String code, String name) {
        when(programCatalogPort.findByIds(List.of(careerId)))
                .thenReturn(Map.of(careerId, new ProgramCatalogPort.ProgramEntry(careerId, code, name)));
        when(templatePort.findMetadataByIds(List.of(templateId)))
                .thenReturn(Map.of(templateId, Template.builder().id(templateId).name("CEUB 2026").type("CEUB").build()));
    }
}
