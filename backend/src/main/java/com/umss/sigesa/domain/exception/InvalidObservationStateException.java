package com.umss.sigesa.domain.exception;

public class InvalidObservationStateException extends RuntimeException {

    private final String code;

    public InvalidObservationStateException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
