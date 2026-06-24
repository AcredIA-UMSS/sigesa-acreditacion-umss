package com.umss.sigesa.domain.exception;

public class ProcessAlreadyActiveException extends RuntimeException {
    public ProcessAlreadyActiveException(String message) {
        super(message);
    }
}

