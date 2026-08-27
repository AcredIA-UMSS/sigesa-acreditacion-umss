package com.umss.sigesa.domain.exception;

public class EvidenceImmutableException extends RuntimeException {

    public EvidenceImmutableException() {
        super("La evidencia es inmutable; no se permite borrado físico (append-only).");
    }
}
