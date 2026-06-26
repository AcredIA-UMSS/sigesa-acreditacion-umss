package com.umss.sigesa.domain.exception;

public class EvidenceUnclassifiedException extends RuntimeException {

    public EvidenceUnclassifiedException(String field) {
        super("Missing required field: " + field);
    }
}
