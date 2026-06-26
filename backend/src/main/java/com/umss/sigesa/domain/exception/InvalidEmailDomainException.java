package com.umss.sigesa.domain.exception;

public class InvalidEmailDomainException extends RuntimeException {

    public InvalidEmailDomainException(String message) {
        super(message);
    }
}
