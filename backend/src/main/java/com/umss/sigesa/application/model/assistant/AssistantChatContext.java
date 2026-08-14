package com.umss.sigesa.application.model.assistant;

import java.util.UUID;

public record AssistantChatContext(
        AssistantAgentProfile agentProfile,
        UUID processId,
        String careerName,
        String careerCode,
        String templateType,
        String phaseCatalogPrompt,
        UUID focusUserId,
        UUID programId
) {

    public static AssistantChatContext general() {
        return new AssistantChatContext(
                AssistantAgentProfile.GENERAL, null, null, null, null, null, null, null);
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
                phaseCatalogPrompt,
                null,
                null);
    }

    public static AssistantChatContext users(UUID focusUserId, UUID programId) {
        return new AssistantChatContext(
                AssistantAgentProfile.USERS,
                null,
                null,
                null,
                null,
                null,
                focusUserId,
                programId);
    }

    public static AssistantChatContext evidence(UUID programId) {
        return new AssistantChatContext(
                AssistantAgentProfile.EVIDENCE,
                null,
                null,
                null,
                null,
                null,
                null,
                programId);
    }

    public boolean isPhasesAgent() {
        return agentProfile == AssistantAgentProfile.PHASES;
    }

    public boolean isUsersAgent() {
        return agentProfile == AssistantAgentProfile.USERS;
    }

    public boolean isEvidenceAgent() {
        return agentProfile == AssistantAgentProfile.EVIDENCE;
    }

    public boolean hasProcessBinding() {
        return processId != null;
    }
}
