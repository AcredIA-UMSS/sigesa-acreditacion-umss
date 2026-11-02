package com.umss.sigesa.domain.exception;

public class NotificationModuleUnavailableException extends RuntimeException {

    public NotificationModuleUnavailableException(String message) {
        super(message);
    }

    public NotificationModuleUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
