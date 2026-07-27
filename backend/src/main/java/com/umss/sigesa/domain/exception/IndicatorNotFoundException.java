package com.umss.sigesa.domain.exception;

import java.util.UUID;

public class IndicatorNotFoundException extends RuntimeException {

    public IndicatorNotFoundException(UUID indicatorId) {
        super("Indicator not found: " + indicatorId);
    }
}
