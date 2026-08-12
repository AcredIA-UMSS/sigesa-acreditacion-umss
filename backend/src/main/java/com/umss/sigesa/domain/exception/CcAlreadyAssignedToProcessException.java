package com.umss.sigesa.domain.exception;

public class CcAlreadyAssignedToProcessException extends RuntimeException {

    public CcAlreadyAssignedToProcessException(String message) {
        super(message);
    }
}
