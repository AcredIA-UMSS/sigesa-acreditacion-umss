package com.umss.sigesa.domain.exception;

public class EvidencePayloadTooLargeException extends RuntimeException {

    public EvidencePayloadTooLargeException(long maxBytes) {
        super("Evidence payload exceeds maximum size of " + maxBytes + " bytes");
    }
}
