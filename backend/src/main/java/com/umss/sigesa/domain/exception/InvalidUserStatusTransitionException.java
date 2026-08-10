package com.umss.sigesa.domain.exception;

public class InvalidUserStatusTransitionException extends RuntimeException {

    public InvalidUserStatusTransitionException(String message) {
        super(message);
    }
}
