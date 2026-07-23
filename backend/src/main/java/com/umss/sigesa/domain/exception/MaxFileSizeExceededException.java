package com.umss.sigesa.domain.exception;

public class MaxFileSizeExceededException extends RuntimeException {
    public MaxFileSizeExceededException(String message) {
        super(message);
    }
}
