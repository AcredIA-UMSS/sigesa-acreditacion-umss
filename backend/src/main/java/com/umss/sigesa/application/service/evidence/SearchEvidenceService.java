package com.umss.sigesa.application.service.evidence;

import com.umss.sigesa.adapter.out.persistance.EvaluationDimensionJpaRepository;
import com.umss.sigesa.adapter.out.persistance.entity.EvaluationDimensionEntity;
import com.umss.sigesa.adapter.in.web.dto.EvidenceSearchDetailDto;
import com.umss.sigesa.adapter.in.web.dto.SearchQueryResponseDto;
import com.umss.sigesa.application.port.in.SearchEvidenceUseCase;
import com.umss.sigesa.application.port.out.AssistantQueryPort;
import com.umss.sigesa.application.port.out.SearchEvidenceQueryPort;
import com.umss.sigesa.config.AssistantProperties;

import java.util.Collections;
import java.util.List;
import java.util.Map;
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

        // Control de seguridad: Si es Coordinador (CC) y no tiene carreras asignadas, abortar con resultado vacío.
        if ("CC".equalsIgnoreCase(role)) {
            if (programScope == null || programScope.isEmpty()) {
                return new SearchQueryResponseDto(query, "KEYWORD", null, "Ninguno", null, Collections.emptyList(), null);
            }
        }

        // Determinar el ámbito de la consulta: CC se restringe a su scope; TD y otros roles tienen acceso global (null).
        List<UUID> effectiveScope = "CC".equalsIgnoreCase(role) ? programScope : null;

        // Extraer año de la query si existe un número de 4 dígitos (ej. 2024)
        Pattern yearPattern = Pattern.compile("\\b(19|20)\\d{2}\\b");
        Matcher yearMatcher = yearPattern.matcher(cleanQuery);
        Integer detectedYear = null;
        if (yearMatcher.find()) {
            detectedYear = Integer.parseInt(yearMatcher.group());
            // Limpiamos el año de la query para la búsqueda literal por texto
            cleanQuery = cleanQuery.replace(yearMatcher.group(), "").replaceAll("\\s+", " ").trim();
        }

        // Si la query es vacía, retornamos todos los objetos accesibles
        if (cleanQuery.isEmpty()) {
            List<EvidenceSearchDetailDto> results = queryPort.executeSearch(null, null, detectedYear, effectiveScope);
            return new SearchQueryResponseDto("", "KEYWORD", null, "evidence, evidence_version, indicator, programs", null, results, queryPort.getLastExecutedSql());
        }

        // Escenario 1.1: Catálogo dinámico de dimensiones de acreditación
        String matchedDimension = matchKeywordCatalog(cleanQuery);
        if (matchedDimension != null) {
            List<EvidenceSearchDetailDto> results = queryPort.executeSearch(null, matchedDimension, detectedYear, effectiveScope);
            return new SearchQueryResponseDto(query, "KEYWORD", null, "evidence, evidence_version, indicator, programs", null, results, queryPort.getLastExecutedSql());
        }

        // Escenario 1.2: Búsqueda clásica directa (si encuentra datos con ILIKE la primera vez, los retorna y salta el LLM)
        List<EvidenceSearchDetailDto> directResults = queryPort.executeSearch(cleanQuery, null, detectedYear, effectiveScope);
        if (!directResults.isEmpty()) {
            return new SearchQueryResponseDto(query, "KEYWORD", null, "evidence, evidence_version, indicator, programs", null, directResults, queryPort.getLastExecutedSql());
        }

        // Evaluar estado de IA (xAiEnabled de la petición y configuración global)
        boolean iaEnabled = xAiEnabled && assistantProperties.isEnabled();

        if (iaEnabled) {
            try {
                // Escenario 2 & 3: Resolución vía LLM
                Map<String, String> routingResult = assistantQueryPort.classifyAndRoute(query);
                String path = routingResult.getOrDefault("routingPath", "REFUSAL");

                if ("REFUSAL".equalsIgnoreCase(path) || "OUT_OF_SCOPE".equalsIgnoreCase(routingResult.get("status"))) {
                    return new SearchQueryResponseDto(
                            query,
                            "REFUSAL",
                            null,
                            "Ninguno",
                            "Lo siento, la consulta está fuera del alcance de SIGESA. Solo puedo asistirte en búsquedas relacionadas con el proceso de acreditación (ej. evidencias, infraestructura, docentes).",
                            Collections.emptyList(),
                            null
                    );
                }

                String termino = routingResult.get("termino");
                String dimension = routingResult.get("dimension");
                Integer anioVal = null;
                if (routingResult.containsKey("anio") && routingResult.get("anio") != null) {
                    try {
                        anioVal = Integer.parseInt(routingResult.get("anio"));
                    } catch (NumberFormatException ignored) {}
                }

                List<EvidenceSearchDetailDto> results = queryPort.executeSearch(termino, dimension, anioVal, effectiveScope);
                return new SearchQueryResponseDto(query, "LLM", "buscar_evidencias_por_parametros", "evidence, evidence_version, indicator, programs", null, results, queryPort.getLastExecutedSql());
            } catch (Exception e) {
                // Fallback elegante en caso de falla de la IA: reutilizamos la búsqueda tradicional ILIKE directa
                return new SearchQueryResponseDto(query, "KEYWORD", null, "evidence, evidence_version, indicator, programs", null, directResults, queryPort.getLastExecutedSql());
            }
        } else {
            // Escenario 4: IA Desactivada (Reutilizamos la búsqueda tradicional ILIKE directa)
            return new SearchQueryResponseDto(query, "KEYWORD", null, "evidence, evidence_version, indicator, programs", null, directResults, queryPort.getLastExecutedSql());
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

    private SearchQueryResponseDto executeFallbackRefusal(String query) {
        return new SearchQueryResponseDto(
                query,
                "REFUSAL",
                null,
                "Ninguno",
                "La búsqueda inteligente por sinónimos está desactivada. Intente buscar con palabras clave exactas.",
                Collections.emptyList(),
                null
        );
    }
}
