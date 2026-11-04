package com.umss.sigesa.domain.exception;

/**
 * 404 genérico del portal público (FSD-BR-10). El mensaje no revela si el slug existe.
 */
public class PublicResourceNotFoundException extends RuntimeException {

    public static final String CLIENT_MESSAGE = "El recurso solicitado no existe.";

    public PublicResourceNotFoundException() {
        super(CLIENT_MESSAGE);
    }
}
