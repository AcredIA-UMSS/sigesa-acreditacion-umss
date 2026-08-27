package com.umss.sigesa.domain.exception;

import java.util.UUID;

public class EvidenceNotFoundException extends RuntimeException {

    public EvidenceNotFoundException(UUID evidenceId) {
        super("Evidencia no encontrada: " + evidenceId);
    }
}
