package com.umss.sigesa.domain.exception;

public class AssistantCompletionException extends RuntimeException {

    public AssistantCompletionException(String message) {
        super(message);
    }

    public AssistantCompletionException(String message, Throwable cause) {
        super(message, cause);
    }
}
