package com.umss.sigesa.domain.exception;

import java.util.UUID;

public class ObservationNotFoundException extends RuntimeException {

    public ObservationNotFoundException(UUID observationId) {
        super("Observación no encontrada: " + observationId);
    }
}
