package com.umss.sigesa.application.model.assistant;

import java.util.List;

public record AssistantChatResult(
        String reply,
        String toolId,
        List<String> sourceTables,
        AssistantResolutionPath path,
        boolean llmInvoked
) {
    public static AssistantChatResult outOfScope(String reply) {
        return new AssistantChatResult(reply, null, List.of(), AssistantResolutionPath.OUT_OF_SCOPE, false);
    }
}
