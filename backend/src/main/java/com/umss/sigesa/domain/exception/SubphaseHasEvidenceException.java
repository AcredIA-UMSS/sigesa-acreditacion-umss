package com.umss.sigesa.domain.exception;

import java.util.UUID;

public class SubphaseHasEvidenceException extends RuntimeException {

    public SubphaseHasEvidenceException(UUID subphaseId) {
        super("La subfase " + subphaseId + " tiene evidencia o workflow iniciado y no puede eliminarse.");
    }
}
