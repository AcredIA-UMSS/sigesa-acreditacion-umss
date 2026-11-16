package com.umss.sigesa.domain.exception;

public class NewsNotFoundException extends RuntimeException {

    public static final String CLIENT_MESSAGE = "La noticia no existe.";

    public NewsNotFoundException() {
        super(CLIENT_MESSAGE);
    }
}
