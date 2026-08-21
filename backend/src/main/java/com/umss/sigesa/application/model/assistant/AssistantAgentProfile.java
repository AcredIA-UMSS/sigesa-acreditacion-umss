package com.umss.sigesa.application.model.assistant;

public enum AssistantAgentProfile {
    GENERAL,
    PHASES,
    USERS,
    EVIDENCE;

    public static AssistantAgentProfile fromAgentId(String agentId) {
        if (agentId == null || agentId.isBlank()) {
            return GENERAL;
        }
        return switch (agentId.trim().toLowerCase()) {
            case "phases" -> PHASES;
            case "users" -> USERS;
            case "evidence" -> EVIDENCE;
            default -> GENERAL;
        };
    }

    public String agentId() {
        return switch (this) {
            case PHASES -> "phases";
            case USERS -> "users";
            case EVIDENCE -> "evidence";
            case GENERAL -> "general";
        };
    }
}
