package com.umss.sigesa.domain.exception;

import java.util.UUID;

public class EvidenceNotInProcessException extends RuntimeException {

    public EvidenceNotInProcessException(UUID evidenceId) {
        super("La evidencia no pertenece a la carrera del proceso: " + evidenceId);
    }
}
