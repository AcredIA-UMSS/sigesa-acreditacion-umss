package com.umss.sigesa.application.model.assistant;

import com.umss.sigesa.domain.model.ChatMessage;

import java.util.List;

public record ChatCompletionRequest(
        List<ChatMessage> messages,
        List<AssistantToolDefinition> tools
) {
}
