package com.umss.sigesa.domain.exception;

public class NewsPublishedConflictException extends RuntimeException {

    public static final String CLIENT_MESSAGE = "Despublique la noticia antes de eliminarla.";

    public NewsPublishedConflictException() {
        super(CLIENT_MESSAGE);
    }
}
