package com.umss.sigesa.application.model.assistant;

public enum AssistantAgentProfile {
    GENERAL,
    PHASES;

    public static AssistantAgentProfile fromAgentId(String agentId) {
        if (agentId != null && "phases".equalsIgnoreCase(agentId.trim())) {
            return PHASES;
        }
        return GENERAL;
    }
}
