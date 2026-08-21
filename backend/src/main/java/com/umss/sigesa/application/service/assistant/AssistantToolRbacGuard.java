package com.umss.sigesa.application.service.assistant;

import com.umss.sigesa.application.model.assistant.AssistantAgentProfile;
import com.umss.sigesa.application.model.assistant.AssistantAuthContext;
import com.umss.sigesa.application.model.assistant.AssistantToolDefinition;
import com.umss.sigesa.application.model.assistant.ToolExecutionResult;

import java.util.Optional;

/**
 * Defensa en profundidad RBAC: rol JWT + subset del agente embebido.
 */
public final class AssistantToolRbacGuard {

    private AssistantToolRbacGuard() {
    }

    public static Optional<ToolExecutionResult> denyIfUnauthorized(
            AssistantToolDefinition definition,
            AssistantAuthContext auth,
            AssistantAgentProfile agentProfile,
            AssistantToolRegistry toolRegistry,
            String toolId) {
        if (definition == null) {
            return Optional.of(ToolExecutionResult.failure("TOOL_NOT_FOUND", "Tool desconocida: " + toolId));
        }

        if (auth == null || auth.role() == null
                || !definition.allowedRoles().contains(auth.role().trim().toUpperCase())) {
            return Optional.of(ToolExecutionResult.failure(
                    "ACCESS_DENIED",
                    "No tiene permisos para ejecutar la tool '" + toolId + "'."));
        }

        if (agentProfile != null
                && agentProfile != AssistantAgentProfile.GENERAL
                && !toolRegistry.isToolAllowedForAgent(toolId, agentProfile)) {
            return Optional.of(ToolExecutionResult.failure(
                    "ACCESS_DENIED",
                    "La tool '" + toolId + "' no está permitida en el agente '"
                            + agentProfile.name().toLowerCase() + "'."));
        }

        return Optional.empty();
    }
}
