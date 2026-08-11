package com.umss.sigesa.application.service.assistant;

import com.umss.sigesa.application.model.assistant.ToolExecutionResult;

import java.util.LinkedHashMap;
import java.util.Map;

final class AssistantConfirmationSupport {

    private AssistantConfirmationSupport() {
    }

    static ToolExecutionResult confirmationRequired(String action, Map<String, Object> preview, String message) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("confirmationRequired", true);
        data.put("action", action);
        data.put("preview", preview);
        data.put("message", message);
        return ToolExecutionResult.success(data);
    }

    static ToolExecutionResult executed(String action, Map<String, Object> result, String message) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("confirmationRequired", false);
        data.put("executed", true);
        data.put("action", action);
        data.put("result", result);
        data.put("message", message);
        return ToolExecutionResult.success(data);
    }

    static boolean isConfirmed(com.fasterxml.jackson.databind.JsonNode args) {
        return args != null && args.has("confirmed") && args.get("confirmed").asBoolean(false);
    }
}
