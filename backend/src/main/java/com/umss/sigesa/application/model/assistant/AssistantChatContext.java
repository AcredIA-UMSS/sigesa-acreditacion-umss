package com.umss.sigesa.application.model.assistant;

import java.util.UUID;

public record AssistantChatContext(
        AssistantAgentProfile agentProfile,
        UUID processId,
        String careerName,
        String careerCode,
        String templateType,
        String phaseCatalogPrompt
) {

    public static AssistantChatContext general() {
        return new AssistantChatContext(AssistantAgentProfile.GENERAL, null, null, null, null, null);
    }

    public static AssistantChatContext phases(UUID processId,
                                              String careerName,
                                              String careerCode,
                                              String templateType) {
        return phases(processId, careerName, careerCode, templateType, null);
    }

    public static AssistantChatContext phases(UUID processId,
                                              String careerName,
                                              String careerCode,
                                              String templateType,
                                              String phaseCatalogPrompt) {
        return new AssistantChatContext(
                AssistantAgentProfile.PHASES,
                processId,
                careerName,
                careerCode,
                templateType,
                phaseCatalogPrompt);
    }

    public boolean isPhasesAgent() {
        return agentProfile == AssistantAgentProfile.PHASES;
    }

    public boolean hasProcessBinding() {
        return processId != null;
    }
}
