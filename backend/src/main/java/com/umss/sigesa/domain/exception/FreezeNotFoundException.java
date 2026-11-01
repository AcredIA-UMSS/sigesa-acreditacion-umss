package com.umss.sigesa.domain.exception;

import java.util.UUID;

public class FreezeNotFoundException extends RuntimeException {

    public FreezeNotFoundException(UUID processId) {
        super("No existe freeze registrado para el proceso " + processId + ".");
    }
}
