package com.umss.sigesa.domain.exception;

import java.util.UUID;

public class ProgramNotFoundException extends RuntimeException {

    public ProgramNotFoundException(UUID programId) {
        super("Carrera no encontrada con ID: " + programId);
    }
}
