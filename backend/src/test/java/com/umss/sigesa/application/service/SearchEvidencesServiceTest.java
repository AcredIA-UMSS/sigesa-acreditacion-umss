package com.umss.sigesa.application.service;

import com.umss.sigesa.application.port.in.SearchEvidencesUseCase;
import com.umss.sigesa.application.port.out.EvidenceRepositoryPort;
import com.umss.sigesa.application.port.out.IndicatorCatalogPort;
import com.umss.sigesa.config.DevSeedData;
import com.umss.sigesa.domain.model.Evidence;
import com.umss.sigesa.domain.model.EvidenceVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchEvidencesService Unit Tests")
class SearchEvidencesServiceTest {

    @Mock
    private EvidenceRepositoryPort evidenceRepository;

    @Mock
    private IndicatorCatalogPort indicatorCatalog;

    private SearchEvidencesService service;

    @BeforeEach
    void setUp() {
        service = new SearchEvidencesService(evidenceRepository, indicatorCatalog);
    }

    @Test
    @DisplayName("Filtra por alcance de carrera CC y texto de búsqueda")
    void search_filtersByProgramScopeAndQuery() {
        UUID evidenceId = DevSeedData.EVIDENCE_LABS;
        UUID versionId = DevSeedData.EVIDENCE_LABS_V1;
        LocalDateTime createdAt = LocalDateTime.of(2026, 6, 1, 10, 0);

        Evidence evidence = new Evidence(evidenceId, DevSeedData.INDICATOR_102, versionId, createdAt);
        EvidenceVersion version = new EvidenceVersion(
                versionId,
                evidenceId,
                1,
                "Inventario fotográfico laboratorios",
                "demo-evidence-labs.pdf",
                "abc123",
                null,
                UUID.randomUUID(),
                createdAt
        );

        when(evidenceRepository.findAll()).thenReturn(List.of(evidence));
        when(indicatorCatalog.findById(DevSeedData.INDICATOR_102)).thenReturn(Optional.of(
                new IndicatorCatalogPort.IndicatorEntry(
                        DevSeedData.INDICATOR_102,
                        "IND-3.1.2",
                        "Infraestructura de Laboratorios",
                        DevSeedData.PROGRAM_INF_SIS,
                        2,
                        DevSeedData.CRITERION_3_1
                )
        ));
        when(evidenceRepository.findVersionById(versionId)).thenReturn(Optional.of(version));

        SearchEvidencesUseCase.EvidenceSearchPage page = service.search(
                new SearchEvidencesUseCase.EvidenceSearchQuery(
                        null,
                        null,
                        null,
                        "inventario",
                        List.of(DevSeedData.PROGRAM_INF_SIS),
                        0,
                        10
                )
        );

        assertEquals(1, page.totalElements());
        assertEquals(evidenceId, page.content().get(0).evidenceId());
    }

    @Test
    @DisplayName("Excluye evidencias fuera del alcance CC")
    void search_excludesOutOfScopePrograms() {
        UUID evidenceId = DevSeedData.EVIDENCE_LABS;
        UUID versionId = DevSeedData.EVIDENCE_LABS_V1;
        LocalDateTime createdAt = LocalDateTime.now();

        Evidence evidence = new Evidence(evidenceId, DevSeedData.INDICATOR_102, versionId, createdAt);
        EvidenceVersion version = new EvidenceVersion(
                versionId, evidenceId, 1, "desc", "key", "hash", null, UUID.randomUUID(), createdAt
        );

        when(evidenceRepository.findAll()).thenReturn(List.of(evidence));
        when(indicatorCatalog.findById(DevSeedData.INDICATOR_102)).thenReturn(Optional.of(
                new IndicatorCatalogPort.IndicatorEntry(
                        DevSeedData.INDICATOR_102,
                        "IND-3.1.2",
                        "Infraestructura",
                        DevSeedData.PROGRAM_INF_SIS,
                        2,
                        DevSeedData.CRITERION_3_1
                )
        ));
        when(evidenceRepository.findVersionById(versionId)).thenReturn(Optional.of(version));

        SearchEvidencesUseCase.EvidenceSearchPage page = service.search(
                new SearchEvidencesUseCase.EvidenceSearchQuery(
                        null, null, null, null, List.of(DevSeedData.PROGRAM_CEUB), 0, 10
                )
        );

        assertTrue(page.content().isEmpty());
    }
}
