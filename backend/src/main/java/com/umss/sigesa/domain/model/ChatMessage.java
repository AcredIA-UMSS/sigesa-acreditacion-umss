package com.umss.sigesa.domain.model;

import java.util.List;

public record ChatMessage(
        ChatRole role,
        String content,
        String toolCallId,
        List<ChatToolCall> toolCalls
) {

    public ChatMessage(ChatRole role, String content) {
        this(role, content, null, List.of());
    }

    public ChatMessage(ChatRole role, String content, String toolCallId) {
        this(role, content, toolCallId, List.of());
    }
}
