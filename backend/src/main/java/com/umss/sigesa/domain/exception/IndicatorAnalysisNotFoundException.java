package com.umss.sigesa.domain.exception;

import java.util.UUID;

public class IndicatorAnalysisNotFoundException extends RuntimeException {

    public IndicatorAnalysisNotFoundException(UUID indicatorId) {
        super("No hay análisis FODA para el indicador: " + indicatorId);
    }
}
