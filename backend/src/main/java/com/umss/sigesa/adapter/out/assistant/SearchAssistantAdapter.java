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

        String systemPrompt = "Eres un asistente de búsqueda y enrutamiento inteligente para el sistema de acreditación universitaria SIGESA. Tu tarea es enrutar las consultas del usuario utilizando las herramientas provistas para mapear sinónimos a criterios oficiales.\n" +
                "Si la consulta no está relacionada con la acreditación universitaria, no intentes responder ni uses ninguna herramienta; simplemente responde con la palabra 'OUT_OF_SCOPE'.";

        List<String> dimensions = dimensionRepository.findAll().stream()
                .map(EvaluationDimensionEntity::getName)
                .toList();
        if (dimensions.isEmpty()) {
            dimensions = List.of("Infraestructura", "Plan de Estudios", "Docentes", "Administracion");
        }

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

        try {
            ChatCompletionResult completion = chatCompletionPort.complete(request);

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

            if (completion.content() != null && completion.content().contains("OUT_OF_SCOPE")) {
                result.put("routingPath", "REFUSAL");
                result.put("status", "OUT_OF_SCOPE");
                result.put("llmThought", "Consulta clasificada por el LLM como fuera del alcance del proceso de acreditación de SIGESA.");
                return result;
            }

        } catch (Exception e) {
            result.put("routingPath", "REFUSAL");
            result.put("llmThought", "Error de conexión o timeout al interactuar con el LLM: " + e.getMessage());
        }

        result.put("routingPath", "REFUSAL");
        result.put("llmThought", "El LLM no invocó la herramienta de búsqueda esperada ni clasificó la consulta.");
        return result;
    }
}
