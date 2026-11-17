package com.umss.sigesa.domain.exception;

public class AssistantGatewayTimeoutException extends RuntimeException {

    public static final String FRIENDLY_MESSAGE = "El asistente tardó demasiado. Intente de nuevo.";

    public AssistantGatewayTimeoutException() {
        super(FRIENDLY_MESSAGE);
    }

    public AssistantGatewayTimeoutException(Throwable cause) {
        super(FRIENDLY_MESSAGE, cause);
    }
}
