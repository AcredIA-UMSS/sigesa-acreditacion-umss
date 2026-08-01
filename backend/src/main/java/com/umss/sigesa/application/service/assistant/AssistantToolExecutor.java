package com.umss.sigesa.application.service.assistant;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umss.sigesa.application.model.assistant.AssistantAuthContext;
import com.umss.sigesa.application.model.assistant.AssistantToolDefinition;
import com.umss.sigesa.application.model.assistant.ToolExecutionResult;
import com.umss.sigesa.application.port.in.ListUsersUseCase;
import com.umss.sigesa.domain.exception.InvalidFilterException;
import com.umss.sigesa.domain.exception.InvalidRoleException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AssistantToolExecutor {

    private static final String LIST_USERS_ID = "list_users";

    private final AssistantToolRegistry toolRegistry;
    private final ListUsersUseCase listUsersUseCase;
    private final ObjectMapper objectMapper;

    public AssistantToolExecutor(AssistantToolRegistry toolRegistry,
                                 ListUsersUseCase listUsersUseCase,
                                 ObjectMapper objectMapper) {
        this.toolRegistry = toolRegistry;
        this.listUsersUseCase = listUsersUseCase;
        this.objectMapper = objectMapper;
    }

    public String execute(String toolId, String argumentsJson, AssistantAuthContext auth) {
        AssistantToolDefinition definition = toolRegistry.findById(toolId)
                .orElse(null);
        if (definition == null) {
            return serialize(ToolExecutionResult.failure("TOOL_NOT_FOUND", "Tool desconocida: " + toolId));
        }

        if (auth == null || auth.role() == null || !definition.allowedRoles().contains(auth.role().trim().toUpperCase())) {
            return serialize(ToolExecutionResult.failure(
                    "ACCESS_DENIED",
                    "No tiene permisos para ejecutar la tool '" + toolId + "'."));
        }

        ToolExecutionResult result = switch (toolId) {
            case LIST_USERS_ID -> executeListUsers(argumentsJson);
            default -> ToolExecutionResult.failure("TOOL_NOT_FOUND", "Tool desconocida: " + toolId);
        };

        return serialize(result);
    }

    private ToolExecutionResult executeListUsers(String argumentsJson) {
        try {
            String roleFilter = null;
            String statusFilter = null;

            if (argumentsJson != null && !argumentsJson.isBlank()) {
                JsonNode args = objectMapper.readTree(argumentsJson);
                if (args.hasNonNull("role")) {
                    roleFilter = args.get("role").asText();
                }
                if (args.hasNonNull("status")) {
                    statusFilter = args.get("status").asText();
                }
            }

            List<ListUsersUseCase.UserSummary> users = listUsersUseCase.list(roleFilter, statusFilter);
            List<Map<String, Object>> userPayload = users.stream()
                    .map(this::toUserMap)
                    .toList();

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("users", userPayload);
            data.put("total", userPayload.size());
            return ToolExecutionResult.success(data);
        } catch (InvalidRoleException ex) {
            return ToolExecutionResult.failure("INVALID_ROLE", ex.getMessage());
        } catch (InvalidFilterException ex) {
            return ToolExecutionResult.failure("INVALID_FILTER", ex.getMessage());
        } catch (JsonProcessingException ex) {
            return ToolExecutionResult.failure("INVALID_ARGUMENTS", "No se pudieron interpretar los argumentos de la tool.");
        }
    }

    private Map<String, Object> toUserMap(ListUsersUseCase.UserSummary user) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("userId", user.userId().toString());
        map.put("email", user.email());
        map.put("role", user.role());
        map.put("status", user.status());
        map.put("programIds", user.programIds().stream().map(Object::toString).toList());
        return map;
    }

    private String serialize(ToolExecutionResult result) {
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException ex) {
            return "{\"ok\":false,\"data\":null,\"error\":{\"code\":\"SERIALIZATION_ERROR\","
                    + "\"message\":\"No se pudo serializar el resultado de la tool.\"}}";
        }
    }
}
