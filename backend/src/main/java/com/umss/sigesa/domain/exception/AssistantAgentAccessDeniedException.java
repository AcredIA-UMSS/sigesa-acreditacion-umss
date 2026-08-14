package com.umss.sigesa.domain.exception;

public class AssistantAgentAccessDeniedException extends RuntimeException {

    public AssistantAgentAccessDeniedException(String agent) {
        this(agent, "JD");
    }

    public AssistantAgentAccessDeniedException(String agent, String allowedRolesDescription) {
        super("El agente '" + agent + "' solo está disponible para el rol "
                + allowedRolesDescription + ".");
    }
}
