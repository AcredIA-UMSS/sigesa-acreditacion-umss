package com.umss.sigesa.application.service.evidence;

import com.umss.sigesa.adapter.out.persistance.EvaluationDimensionJpaRepository;
import com.umss.sigesa.adapter.out.persistance.entity.EvaluationDimensionEntity;
import com.umss.sigesa.adapter.in.web.dto.EvidenceSearchDetailDto;
import com.umss.sigesa.adapter.in.web.dto.SearchQueryResponseDto;
import com.umss.sigesa.adapter.in.web.dto.SearchSubsetDto;
import com.umss.sigesa.application.port.in.SearchEvidenceUseCase;
import com.umss.sigesa.application.port.out.AssistantQueryPort;
import com.umss.sigesa.application.port.out.SearchEvidenceQueryPort;
import com.umss.sigesa.config.AssistantProperties;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.umss.sigesa.application.model.evidence.SearchFilters;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchEvidenceService implements SearchEvidenceUseCase {

    private final SearchEvidenceQueryPort queryPort;
    private final AssistantQueryPort assistantQueryPort;
    private final AssistantProperties assistantProperties;
    private final EvaluationDimensionJpaRepository dimensionRepository;

    public SearchEvidenceService(SearchEvidenceQueryPort queryPort,
                                 AssistantQueryPort assistantQueryPort,
                                 AssistantProperties assistantProperties,
                                 EvaluationDimensionJpaRepository dimensionRepository) {
        this.queryPort = queryPort;
        this.assistantQueryPort = assistantQueryPort;
        this.assistantProperties = assistantProperties;
        this.dimensionRepository = dimensionRepository;
    }

    @Override
    public SearchQueryResponseDto search(String query, boolean xAiEnabled, UUID userId, String role, List<UUID> programScope) {
        String cleanQuery = (query == null) ? "" : query.trim().toLowerCase();

        if ("CC".equalsIgnoreCase(role)) {
            if (programScope == null || programScope.isEmpty()) {
                return new SearchQueryResponseDto(query, "KEYWORD", Collections.emptyList(), 
                    "El Coordinador de Carrera no tiene programas asignados en su perfil de seguridad.", null);
            }
        }

        List<UUID> effectiveScope = "CC".equalsIgnoreCase(role) ? programScope : null;

        Pattern yearPattern = Pattern.compile("\\b(19|20)\\d{2}\\b");
        Matcher yearMatcher = yearPattern.matcher(cleanQuery);
        Integer detectedYear = null;
        if (yearMatcher.find()) {
            detectedYear = Integer.parseInt(yearMatcher.group());
            cleanQuery = cleanQuery.replace(yearMatcher.group(), "").replaceAll("\\s+", " ").trim();
        }

        LocalDate start = (detectedYear != null) ? LocalDate.of(detectedYear, 1, 1) : null;
        LocalDate end = (detectedYear != null) ? LocalDate.of(detectedYear, 12, 31) : null;

        if (cleanQuery.isEmpty()) {
            SearchFilters filters = SearchFilters.builder()
                    .fechaInicio(start)
                    .fechaFin(end)
                    .build();
            List<EvidenceSearchDetailDto> results = queryPort.executeSearch(filters, effectiveScope);
            return new SearchQueryResponseDto("", "KEYWORD", List.of(new SearchSubsetDto("Todas las evidencias", results)), 
                "Consulta de texto vacía. Retornando todas las evidencias accesibles.", null);
        }

        String matchedDimension = matchKeywordCatalog(cleanQuery);
        if (matchedDimension != null) {
            SearchFilters filters = SearchFilters.builder()
                    .dimension(matchedDimension)
                    .fechaInicio(start)
                    .fechaFin(end)
                    .build();
            List<EvidenceSearchDetailDto> results = queryPort.executeSearch(filters, effectiveScope);
            return new SearchQueryResponseDto(query, "KEYWORD", List.of(new SearchSubsetDto("Resultados Exactos", results)), 
                "Coincidencia exacta de dimensión encontrada.", null);
        }

        SearchFilters directFilters = SearchFilters.builder()
                .termino(cleanQuery)
                .fechaInicio(start)
                .fechaFin(end)
                .build();
        List<EvidenceSearchDetailDto> directResults = queryPort.executeSearch(directFilters, effectiveScope);
        if (!directResults.isEmpty()) {
            return new SearchQueryResponseDto(query, "KEYWORD", List.of(new SearchSubsetDto("Búsqueda Clásica", directResults)), 
                "Resultados localizados directamente (ILIKE).", null);
        }

        boolean iaEnabled = xAiEnabled && assistantProperties.isEnabled();

        if (iaEnabled) {
            try {
                Map<String, String> routingResult = assistantQueryPort.classifyAndRoute(query);
                String path = routingResult.getOrDefault("routingPath", "REFUSAL");

                if ("OUT_OF_SCOPE".equalsIgnoreCase(routingResult.get("status"))) {
                    return new SearchQueryResponseDto(query, "REFUSAL", Collections.emptyList(),
                            "Lo siento, la consulta está fuera del alcance de SIGESA.", routingResult.getOrDefault("llmThought", "Fuera de alcance."));
                }

                if ("REFUSAL".equalsIgnoreCase(path)) {
                    // Si hubo un error técnico o falta de invocación de herramienta, hacer fallback a búsqueda clásica en lugar de rechazar
                    return new SearchQueryResponseDto(query, "KEYWORD", List.of(new SearchSubsetDto("Fallback Clásico (IA no disponible)", directResults)),
                            "La IA no pudo estructurar la consulta. Mostrando resultados clásicos.", routingResult.getOrDefault("llmThought", "Fallback técnico."));
                }

                SearchFilters llmFilters = SearchFilters.builder()
                        .termino(routingResult.get("termino"))
                        .dimension(routingResult.get("dimension"))
                        .criterioCodigo(routingResult.get("criterioCodigo"))
                        .build();

                List<EvidenceSearchDetailDto> results = queryPort.executeSearch(llmFilters, effectiveScope);
                return new SearchQueryResponseDto(query, "LLM_MULTIPATH", List.of(new SearchSubsetDto("Resultados Multi-Token MCP", results)), 
                    "Búsqueda estructurada por MCP", routingResult.get("llmThought"));
            } catch (Exception e) {
                return new SearchQueryResponseDto(query, "KEYWORD", List.of(new SearchSubsetDto("Fallback Clásico", directResults)), 
                    "Error al conectar con IA. Fallback automático.", null);
            }
        } else {
            return new SearchQueryResponseDto(query, "KEYWORD", List.of(new SearchSubsetDto("Búsqueda Directa", directResults)), 
                "Búsqueda inteligente desactivada.", null);
        }
    }

    private String matchKeywordCatalog(String query) {
        try {
            return dimensionRepository.findAll().stream()
                    .map(EvaluationDimensionEntity::getName)
                    .filter(name -> name.equalsIgnoreCase(query))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
}
