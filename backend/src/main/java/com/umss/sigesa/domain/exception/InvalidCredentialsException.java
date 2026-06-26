package com.umss.sigesa.domain.exception;

public class InvalidCredentialsException extends RuntimeException {

    public static final String GENERIC_MESSAGE = "Credenciales inválidas";

    public InvalidCredentialsException() {
        super(GENERIC_MESSAGE);
    }
}
