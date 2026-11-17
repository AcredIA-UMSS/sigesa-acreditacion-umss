package com.umss.sigesa.domain.exception;

public class NewsNotPublishedException extends RuntimeException {

    public static final String CLIENT_MESSAGE = "Solo se puede destacar una noticia publicada.";

    public NewsNotPublishedException() {
        super(CLIENT_MESSAGE);
    }
}
