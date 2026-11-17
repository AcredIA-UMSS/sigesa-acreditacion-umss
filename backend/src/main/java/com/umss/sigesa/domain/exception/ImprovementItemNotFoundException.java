package com.umss.sigesa.domain.exception;

import java.util.UUID;

public class ImprovementItemNotFoundException extends RuntimeException {

    public ImprovementItemNotFoundException(UUID itemId) {
        super("Ítem de plan de mejoras no encontrado: " + itemId);
    }
}
