package com.umss.sigesa.application.model.assistant;

import java.util.Map;
import java.util.Set;

public record AssistantToolDefinition(
        String id,
        String description,
        Set<String> allowedRoles,
        String sideEffect,
        Map<String, Object> parameterSchema
) {
}
