package com.umss.sigesa.domain.exception;

public class InvalidDateRangeException extends RuntimeException {

    public static final String CLIENT_MESSAGE =
            "El rango de fechas es inválido: 'from' no puede ser posterior a 'to'.";

    public InvalidDateRangeException() {
        super(CLIENT_MESSAGE);
    }
}
