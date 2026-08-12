package com.umss.sigesa.application.service.assistant;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Vista previa legible para tools de escritura del agente users (análogo a {@code SubphaseOrderPlan}).
 */
public record AssistantUserActionPlan(
        String action,
        String summary,
        Map<String, Object> preview
) {

    public static AssistantUserActionPlan of(String action, String summary, Map<String, Object> preview) {
        Map<String, Object> copy = new LinkedHashMap<>(preview);
        copy.put("requestedAction", action);
        copy.put("planSummary", summary);
        return new AssistantUserActionPlan(action, summary, copy);
    }
}
