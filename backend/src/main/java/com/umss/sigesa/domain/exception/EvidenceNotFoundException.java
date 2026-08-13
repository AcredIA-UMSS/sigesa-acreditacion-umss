package com.umss.sigesa.domain.exception;

import java.util.UUID;

public class EvidenceNotFoundException extends RuntimeException {
    public EvidenceNotFoundException(UUID versionId) {
        super("No se encontró la versión de evidencia: " + versionId);
    }
}
