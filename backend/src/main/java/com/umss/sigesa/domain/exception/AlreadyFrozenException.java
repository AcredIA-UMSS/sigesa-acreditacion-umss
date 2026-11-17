package com.umss.sigesa.domain.exception;

public class AlreadyFrozenException extends RuntimeException {

    public AlreadyFrozenException() {
        super("El proceso ya tiene un freeze AUDIT_READONLY activo.");
    }
}
