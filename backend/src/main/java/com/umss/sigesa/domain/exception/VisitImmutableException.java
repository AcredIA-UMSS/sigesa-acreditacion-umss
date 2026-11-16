package com.umss.sigesa.domain.exception;

public class VisitImmutableException extends RuntimeException {

    public static final String CLIENT_MESSAGE = "La visita COMPLETED no puede modificarse.";

    public VisitImmutableException() {
        super(CLIENT_MESSAGE);
    }
}
