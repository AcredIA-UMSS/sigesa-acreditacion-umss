package com.umss.sigesa.domain.exception;

public class UploadInProgressException extends RuntimeException {

    public UploadInProgressException() {
        super("Upload already in progress for this indicator");
    }
}
