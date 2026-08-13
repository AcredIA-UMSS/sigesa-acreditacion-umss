package com.umss.sigesa.domain.exception;

public class AssistantAgentAccessDeniedException extends RuntimeException {

    public AssistantAgentAccessDeniedException(String agent) {
        super("El agente '" + agent + "' solo está disponible para el rol JD.");
    }
}
