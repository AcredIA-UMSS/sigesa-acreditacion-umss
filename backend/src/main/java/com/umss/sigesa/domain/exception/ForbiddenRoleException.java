package com.umss.sigesa.domain.exception;

public class ForbiddenRoleException extends RuntimeException {
    public ForbiddenRoleException(String message) {
        super(message);
    }
}
