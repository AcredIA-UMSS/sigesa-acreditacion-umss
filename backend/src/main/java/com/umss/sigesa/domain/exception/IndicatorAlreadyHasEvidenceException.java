package com.umss.sigesa.domain.exception;

import java.util.UUID;

public class IndicatorAlreadyHasEvidenceException extends RuntimeException {

    public IndicatorAlreadyHasEvidenceException(UUID indicatorId) {
        super("El indicador ya tiene evidencia vigente: " + indicatorId);
    }
}
