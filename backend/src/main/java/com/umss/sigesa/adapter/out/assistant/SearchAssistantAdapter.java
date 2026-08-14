package com.umss.sigesa.adapter.out.assistant;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umss.sigesa.adapter.out.persistance.EvaluationDimensionJpaRepository;
import com.umss.sigesa.adapter.out.persistance.entity.EvaluationDimensionEntity;
import com.umss.sigesa.application.model.assistant.AssistantToolDefinition;
import com.umss.sigesa.application.model.assistant.ChatCompletionRequest;
import com.umss.sigesa.application.model.assistant.ChatCompletionResult;
import com.umss.sigesa.application.model.assistant.ToolCall;
import com.umss.sigesa.application.port.out.AssistantQueryPort;
import com.umss.sigesa.application.port.out.ChatCompletionPort;
import com.umss.sigesa.domain.model.ChatMessage;
import com.umss.sigesa.domain.model.ChatRole;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SearchAssistantAdapter implements AssistantQueryPort {

    private final ChatCompletionPort chatCompletionPort;
    private final EvaluationDimensionJpaRepository dimensionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SearchAssistantAdapter(ChatCompletionPort chatCompletionPort,
                                  EvaluationDimensionJpaRepository dimensionRepository) {
        this.chatCompletionPort = chatCompletionPort;
        this.dimensionRepository = dimensionRepository;
    }

    @Override
    public Map<String, String> classifyAndRoute(String query) {
        Map<String, String> result = new HashMap<>();

        List<String> dimensions = dimensionRepository.findAll().stream()
                .map(EvaluationDimensionEntity::getName)
                .toList();
        if (dimensions.isEmpty()) {
            dimensions = List.of("Infraestructura", "Plan de Estudios", "Docentes", "Administracion");
        }

        String systemPrompt = "Eres un enrutador de búsquedas inteligente para el sistema de acreditación SIGESA.\n" +
                "Tu única tarea es analizar la consulta del usuario y clasificarla.\n\n" +
                "Si la consulta está relacionada directa o indirectamente con el ámbito universitario (infraestructura, docencia, materias, estudiantes, administración, etc.), debes responder EXCLUSIVAMENTE con un bloque JSON en texto plano formateado así, sin textos aclaratorios ni explicaciones:\n" +
                "{\n" +
                "  \"termino\": \"termino de búsqueda limpio y simplificado\",\n" +
                "  \"dimension\": \"Nombre de la dimensión oficial\"\n" +
                "}\n\n" +
                "Dimensiones oficiales del catálogo que debes utilizar (mapea el sinónimo a la más cercana):\n" +
                String.join("\n", dimensions.stream().map(d -> "- " + d).toList()) + "\n\n" +
                "Si la consulta es completamente ajena al ámbito universitario (recetas de cocina, chistes, deportes, espectáculos, etc.), responde únicamente con la palabra: OUT_OF_SCOPE\n" +
                "PROHIBIDO escribir explicaciones, introducciones o textos adicionales. Responde únicamente con el bloque JSON o con 'OUT_OF_SCOPE'.";

        Map<String, Object> schema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "dimension", Map.of(
                                "type", "string",
                                "enum", dimensions,
                                "description", "Dimensión o criterio oficial al que se mapea la búsqueda del usuario."
                        ),
                        "termino", Map.of(
                                "type", "string",
                                "description", "Término limpio extraído de la búsqueda para usar en la consulta de texto."
                        ),
                        "criterioCodigo", Map.of(
                                "type", "string",
                                "description", "Código oficial de criterio si se menciona (ej: CRT-04, CRT-01)."
                        ),
                        "fechaInicio", Map.of(
                                "type", "string",
                                "description", "Fecha de inicio opcional en formato ISO-8601 (YYYY-MM-DD)."
                        ),
                        "fechaFin", Map.of(
                                "type", "string",
                                "description", "Fecha de fin opcional en formato ISO-8601 (YYYY-MM-DD)."
                        )
                ),
                "required", List.of("termino")
        );

        java.util.Set<String> allowedRoles = java.util.Set.of("CC", "TD", "JD");
        AssistantToolDefinition tool = new AssistantToolDefinition(
                "buscar_evidencias_por_parametros",
                "Busca documentos de evidencia mapeando sinónimos a términos y dimensiones oficiales de acreditación.",
                allowedRoles,
                "NONE",
                schema
        );

        ChatCompletionRequest request = new ChatCompletionRequest(
                List.of(
                        new ChatMessage(ChatRole.SYSTEM, systemPrompt),
                        new ChatMessage(ChatRole.USER, query)
                ),
                List.of(tool)
        );

        ChatCompletionResult completion = null;
        String parserError = null;

        try {
            completion = chatCompletionPort.complete(request);

            if (completion.toolCalls() != null && !completion.toolCalls().isEmpty()) {
                ToolCall call = completion.toolCalls().get(0);
                if ("buscar_evidencias_por_parametros".equals(call.name())) {
                    JsonNode args = objectMapper.readTree(call.argumentsJson());
                    result.put("routingPath", "LLM");
                    result.put("termino", args.path("termino").asText());
                    result.put("dimension", args.path("dimension").asText(null));
                    result.put("criterioCodigo", args.path("criterioCodigo").asText(null));
                    result.put("fechaInicio", args.path("fechaInicio").asText(null));
                    result.put("fechaFin", args.path("fechaFin").asText(null));
                    
                    String term = args.path("termino").asText();
                    String dim = args.path("dimension").asText(null);
                    String thought = "El usuario busca \"" + query + "\". Traduciendo a término oficial: \"" + term + "\"" 
                            + (dim != null ? " y dimensión: \"" + dim + "\"" : "") + ".";
                    result.put("llmThought", thought);
                    return result;
                }
            }

            // Fallback: Si el LLM no llamó formalmente a la tool, pero devolvió un bloque JSON en texto plano
            if (completion.content() != null && completion.content().contains("{") && completion.content().contains("}")) {
                try {
                    String jsonText = completion.content().substring(
                            completion.content().indexOf("{"),
                            completion.content().lastIndexOf("}") + 1
                    );
                    JsonNode args = objectMapper.readTree(jsonText);
                    result.put("routingPath", "LLM");
                    result.put("termino", args.path("termino").asText(query));
                    result.put("dimension", args.path("dimension").asText(null));
                    result.put("criterioCodigo", args.path("criterioCodigo").asText(null));
                    result.put("fechaInicio", args.path("fechaInicio").asText(null));
                    result.put("fechaFin", args.path("fechaFin").asText(null));
                    
                    String term = args.path("termino").asText(query);
                    String dim = args.path("dimension").asText(null);
                    String thought = "El usuario busca \"" + query + "\". Traduciendo a término oficial: \"" + term + "\"" 
                            + (dim != null ? " y dimensión: \"" + dim + "\"" : "") + " (JSON fallback).";
                    result.put("llmThought", thought);
                    return result;
                } catch (Exception e) {
                    parserError = "Fallo al deserializar bloque JSON detectado: " + e.getMessage();
                }
            }

            if (completion.content() != null && completion.content().contains("OUT_OF_SCOPE")) {
                result.put("routingPath", "REFUSAL");
                result.put("status", "OUT_OF_SCOPE");
                result.put("llmThought", "Consulta clasificada por el LLM como fuera del alcance del proceso de acreditación de SIGESA.");
                return result;
            }

        } catch (Exception e) {
            result.put("routingPath", "REFUSAL");
            result.put("llmThought", "Error de conexión o timeout al interactuar con el LLM: " + e.getMessage());
            return result;
        }

        result.put("routingPath", "REFUSAL");
        String rawContent = (completion != null) ? completion.content() : "null";
        result.put("llmThought", "El LLM no invocó la herramienta ni retornó un JSON estructurado. Respuesta cruda del LLM: \"" + rawContent + "\"" 
                + (parserError != null ? " | Detalle: " + parserError : ""));
        return result;
    }
}
