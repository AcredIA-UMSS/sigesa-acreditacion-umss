package com.umss.sigesa.application.model.assistant;

import java.util.List;

public record ChatCompletionResult(String content, List<ToolCall> toolCalls) {

    public boolean hasToolCalls() {
        return toolCalls != null && !toolCalls.isEmpty();
    }
}
