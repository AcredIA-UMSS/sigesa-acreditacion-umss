package com.umss.sigesa.application.service.evidence;

import com.umss.sigesa.adapter.in.web.dto.EvidenceSearchDetailDto;
import com.umss.sigesa.adapter.in.web.dto.SearchQueryResponseDto;
import com.umss.sigesa.application.port.out.AssistantQueryPort;
import com.umss.sigesa.application.port.out.SearchEvidenceQueryPort;
import com.umss.sigesa.config.AssistantProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SearchEvidenceServiceTest {

    private SearchEvidenceQueryPort queryPort;
    private AssistantQueryPort assistantQueryPort;
    private AssistantProperties assistantProperties;
    private SearchEvidenceService searchEvidenceService;

    @BeforeEach
    void setUp() {
        queryPort = mock(SearchEvidenceQueryPort.class);
        assistantQueryPort = mock(AssistantQueryPort.class);
        assistantProperties = mock(AssistantProperties.class);
        searchEvidenceService = new SearchEvidenceService(queryPort, assistantQueryPort, assistantProperties);
    }

    @Test
    void testSearchKeywordExactMatch_Escenario1() {
        UUID programId = UUID.randomUUID();
        List<UUID> scope = List.of(programId);

        EvidenceSearchDetailDto detail = new EvidenceSearchDetailDto(
                UUID.randomUUID(), "file.pdf", "desc", "Infraestructura", "CRT-04", "Sistemas", LocalDateTime.now()
        );
        when(queryPort.executeSearch(null, "Infraestructura", scope)).thenReturn(List.of(detail));

        SearchQueryResponseDto response = searchEvidenceService.search("infraestructura", true, UUID.randomUUID(), "CC", scope);

        assertEquals("KEYWORD", response.routingPath());
        assertFalse(response.results().isEmpty());
        verifyNoInteractions(assistantQueryPort);
    }

    @Test
    void testSearchSemanticLLMMatch_Escenario2() {
        UUID programId = UUID.randomUUID();
        List<UUID> scope = List.of(programId);

        when(assistantProperties.isEnabled()).thenReturn(true);
        when(assistantQueryPort.classifyAndRoute("aulas de computacion")).thenReturn(Map.of(
                "routingPath", "LLM",
                "termino", "computacion",
                "dimension", "Infraestructura"
        ));

        EvidenceSearchDetailDto detail = new EvidenceSearchDetailDto(
                UUID.randomUUID(), "file.pdf", "aulas de computacion", "Infraestructura", "CRT-04", "Sistemas", LocalDateTime.now()
        );
        when(queryPort.executeSearch("computacion", "Infraestructura", scope)).thenReturn(List.of(detail));

        SearchQueryResponseDto response = searchEvidenceService.search("aulas de computacion", true, UUID.randomUUID(), "CC", scope);

        assertEquals("LLM", response.routingPath());
        assertEquals("buscar_evidencias_por_parametros", response.toolUsed());
        assertFalse(response.results().isEmpty());
    }

    @Test
    void testSearchOutOfScope_Escenario3() {
        when(assistantProperties.isEnabled()).thenReturn(true);
        when(assistantQueryPort.classifyAndRoute("clima en paris")).thenReturn(Map.of(
                "routingPath", "REFUSAL",
                "status", "OUT_OF_SCOPE"
        ));

        SearchQueryResponseDto response = searchEvidenceService.search("clima en paris", true, UUID.randomUUID(), "TD", Collections.emptyList());

        assertEquals("REFUSAL", response.routingPath());
        assertTrue(response.results().isEmpty());
        assertNotNull(response.message());
    }

    @Test
    void testSearchIADesactivada_Escenario4() {
        when(assistantProperties.isEnabled()).thenReturn(false);
        // Debería intentar búsqueda clásica
        when(queryPort.executeSearch("aulas", null, Collections.emptyList())).thenReturn(Collections.emptyList());

        SearchQueryResponseDto response = searchEvidenceService.search("aulas", true, UUID.randomUUID(), "TD", Collections.emptyList());

        // Con la lógica actualizada, con IA desactivada se realiza una búsqueda clásica ILIKE en vez de REFUSAL
        assertEquals("KEYWORD", response.routingPath());
    }

    @Test
    void testSearchRoleIsolation_TDvsCC() {
        UUID systemsProgramId = UUID.randomUUID();
        List<UUID> ccScope = List.of(systemsProgramId);

        // Caso 1: Técnico DUEA (TD) - no aplica filtro de scope (pasa null)
        searchEvidenceService.search("aulas", false, UUID.randomUUID(), "TD", ccScope);
        verify(queryPort).executeSearch("aulas", null, null);

        // Caso 2: Coordinador de Carrera (CC) - aplica filtro de scope (pasa ccScope)
        searchEvidenceService.search("aulas", false, UUID.randomUUID(), "CC", ccScope);
        verify(queryPort).executeSearch("aulas", null, ccScope);
    }
}
