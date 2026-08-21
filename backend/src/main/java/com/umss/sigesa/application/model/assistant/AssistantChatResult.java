package com.umss.sigesa.application.model.assistant;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public record AssistantChatResult(
        String reply,
        String toolId,
        List<String> sourceTables,
        AssistantResolutionPath path,
        boolean llmInvoked,
        List<AssistantToolStep> steps
) {

    public AssistantChatResult {
        steps = steps == null ? List.of() : List.copyOf(steps);
    }

    public AssistantChatResult(String reply,
                               String toolId,
                               List<String> sourceTables,
                               AssistantResolutionPath path,
                               boolean llmInvoked) {
        this(reply, toolId, sourceTables, path, llmInvoked, List.of());
    }

    public static AssistantChatResult outOfScope(String reply) {
        return new AssistantChatResult(reply, null, List.of(), AssistantResolutionPath.OUT_OF_SCOPE, false);
    }

    public static AssistantChatResult fromSteps(String reply,
                                                AssistantResolutionPath path,
                                                boolean llmInvoked,
                                                List<AssistantToolStep> steps) {
        if (steps == null || steps.isEmpty()) {
            return new AssistantChatResult(reply, null, List.of(), path, llmInvoked, List.of());
        }
        AssistantToolStep last = steps.getLast();
        Set<String> tables = new LinkedHashSet<>();
        for (AssistantToolStep step : steps) {
            if (step.sourceTables() != null) {
                tables.addAll(step.sourceTables());
            }
        }
        return new AssistantChatResult(
                reply,
                last.toolId(),
                new ArrayList<>(tables),
                path,
                llmInvoked,
                steps);
    }
}
