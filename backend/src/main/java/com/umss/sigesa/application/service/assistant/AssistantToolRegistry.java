package com.umss.sigesa.application.service.assistant;

import com.umss.sigesa.application.model.assistant.AssistantToolDefinition;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class AssistantToolRegistry {

    private static final String LIST_USERS_ID = "list_users";

    private static final AssistantToolDefinition LIST_USERS = new AssistantToolDefinition(
            LIST_USERS_ID,
            "Lista los usuarios registrados en SIGESA con su correo institucional, rol y estado de cuenta. "
                    + "Opcionalmente filtra por rol (JD, CC, TD, EE) o por estado (INACTIVE, ACTIVE, DEACTIVATED). "
                    + "Solo disponible para Jefatura DUEA (JD). Usa esta tool cuando el usuario pregunte quiénes "
                    + "están registrados, qué rol tienen, cuántos coordinadores hay, o quién está inactivo o desactivado. "
                    + "No expone contraseñas ni permite modificar usuarios.",
            Set.of("JD"),
            "read",
            listUsersParameterSchema()
    );

    private final List<AssistantToolDefinition> allTools = List.of(LIST_USERS);

    public List<AssistantToolDefinition> toolsForRole(String role) {
        if (role == null || role.isBlank()) {
            return List.of();
        }
        String normalizedRole = role.trim().toUpperCase();
        return allTools.stream()
                .filter(tool -> tool.allowedRoles().contains(normalizedRole))
                .toList();
    }

    public Optional<AssistantToolDefinition> findById(String toolId) {
        if (toolId == null || toolId.isBlank()) {
            return Optional.empty();
        }
        return allTools.stream()
                .filter(tool -> tool.id().equals(toolId))
                .findFirst();
    }

    private static Map<String, Object> listUsersParameterSchema() {
        Map<String, Object> roleProperty = new LinkedHashMap<>();
        roleProperty.put("type", "string");
        roleProperty.put("enum", List.of("JD", "CC", "TD", "EE"));
        roleProperty.put("description", "Filtro opcional por rol de usuario.");

        Map<String, Object> statusProperty = new LinkedHashMap<>();
        statusProperty.put("type", "string");
        statusProperty.put("enum", List.of("INACTIVE", "ACTIVE", "DEACTIVATED"));
        statusProperty.put("description", "Filtro opcional por estado de cuenta.");

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("role", roleProperty);
        properties.put("status", statusProperty);

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", properties);
        schema.put("additionalProperties", false);
        return schema;
    }
}
