package com.umss.sigesa.application.model.assistant;

public record ToolExecutionResult(boolean ok, Object data, ToolError error) {

    public record ToolError(String code, String message) {
    }

    public static ToolExecutionResult success(Object data) {
        return new ToolExecutionResult(true, data, null);
    }

    public static ToolExecutionResult failure(String code, String message) {
        return new ToolExecutionResult(false, null, new ToolError(code, message));
    }
}
