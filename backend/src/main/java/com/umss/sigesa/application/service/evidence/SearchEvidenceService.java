package com.umss.sigesa.application.service.evidence;

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

public class SearchEvidenceService implements SearchEvidenceUseCase {

    private final SearchEvidenceQueryPort queryPort;
    private final AssistantQueryPort assistantQueryPort;
    private final AssistantProperties assistantProperties;

    public SearchEvidenceService(SearchEvidenceQueryPort queryPort,
                                 AssistantQueryPort assistantQueryPort,
                                 AssistantProperties assistantProperties) {
        this.queryPort = queryPort;
        this.assistantQueryPort = assistantQueryPort;
        this.assistantProperties = assistantProperties;
    }

    @Override
    public SearchQueryResponseDto search(String query, boolean xAiEnabled, UUID userId, String role, List<UUID> programScope) {
        String cleanQuery = (query == null) ? "" : query.trim().toLowerCase();

        // Control de seguridad: Si es Coordinador (CC) y no tiene carreras asignadas, abortar con resultado vacío.
        if ("CC".equalsIgnoreCase(role)) {
            if (programScope == null || programScope.isEmpty()) {
                return new SearchQueryResponseDto(query, "KEYWORD", null, "Ninguno", null, Collections.emptyList());
            }
        }

        // Determinar el ámbito de la consulta: CC se restringe a su scope; TD y otros roles tienen acceso global (null).
        List<UUID> effectiveScope = "CC".equalsIgnoreCase(role) ? programScope : null;

        // Si la query es vacía, retornamos todos los objetos accesibles
        if (cleanQuery.isEmpty()) {
            List<EvidenceSearchDetailDto> results = queryPort.executeSearch(null, null, effectiveScope);
            return new SearchQueryResponseDto("", "KEYWORD", null, "evidence, evidence_version, indicator, programs", null, results);
        }

        // Escenario 1: Catálogo local exacto
        String matchedDimension = matchKeywordCatalog(cleanQuery);
        if (matchedDimension != null) {
            List<EvidenceSearchDetailDto> results = queryPort.executeSearch(null, matchedDimension, effectiveScope);
            return new SearchQueryResponseDto(query, "KEYWORD", null, "evidence, evidence_version, indicator, programs", null, results);
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
                            Collections.emptyList()
                    );
                }

                String termino = routingResult.get("termino");
                String dimension = routingResult.get("dimension");

                List<EvidenceSearchDetailDto> results = queryPort.executeSearch(termino, dimension, effectiveScope);
                return new SearchQueryResponseDto(query, "LLM", "buscar_evidencias_por_parametros", "evidence, evidence_version, indicator, programs", null, results);
            } catch (Exception e) {
                // Fallback elegante en caso de falla de la IA: búsqueda tradicional ILIKE
                List<EvidenceSearchDetailDto> results = queryPort.executeSearch(cleanQuery, null, effectiveScope);
                return new SearchQueryResponseDto(query, "KEYWORD", null, "evidence, evidence_version, indicator, programs", null, results);
            }
        } else {
            // Escenario 4: IA Desactivada (Búsqueda tradicional ILIKE sobre descripción e identificador)
            List<EvidenceSearchDetailDto> results = queryPort.executeSearch(cleanQuery, null, effectiveScope);
            return new SearchQueryResponseDto(query, "KEYWORD", null, "evidence, evidence_version, indicator, programs", null, results);
        }
    }

    private String matchKeywordCatalog(String query) {
        return switch (query) {
            case "infraestructura" -> "Infraestructura";
            case "docentes" -> "Docentes";
            case "plan de estudios" -> "Plan de Estudios";
            case "administracion", "administración" -> "Administracion";
            default -> null;
        };
    }

    private SearchQueryResponseDto executeFallbackRefusal(String query) {
        return new SearchQueryResponseDto(
                query,
                "REFUSAL",
                null,
                "Ninguno",
                "La búsqueda inteligente por sinónimos está desactivada. Intente buscar con palabras clave exactas.",
                Collections.emptyList()
        );
    }
}
