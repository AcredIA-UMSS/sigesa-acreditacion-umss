package com.umss.sigesa.domain.exception;

import java.util.UUID;

public class IndicatorHasEvidenceException extends RuntimeException {

    public IndicatorHasEvidenceException(UUID indicatorId) {
        super("El indicador tiene evidencias o workflow iniciado: " + indicatorId);
    }
}
