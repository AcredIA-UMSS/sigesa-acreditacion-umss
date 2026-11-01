package com.umss.sigesa.domain.exception;

public class ProcessFrozenException extends RuntimeException {

    public ProcessFrozenException() {
        super("El proceso está congelado en modo AUDIT_READONLY.");
    }

    public ProcessFrozenException(String message) {
        super(message);
    }
}
