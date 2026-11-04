package com.umss.sigesa.domain.exception;

public class ActiveExportAlreadyExistsException extends RuntimeException {
    public ActiveExportAlreadyExistsException(String message) {
        super(message);
    }
}
