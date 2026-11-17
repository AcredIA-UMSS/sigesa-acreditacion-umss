package com.umss.sigesa.domain.exception;

public class InvalidCaeUserException extends RuntimeException {

    public InvalidCaeUserException() {
        super("El usuario destinatario debe tener rol CAE_MEMBER.");
    }
}
