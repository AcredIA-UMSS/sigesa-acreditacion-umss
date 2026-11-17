package com.umss.sigesa.domain.exception;

import java.util.UUID;

public class DelegationNotFoundException extends RuntimeException {

    public DelegationNotFoundException(UUID delegationId) {
        super("Delegación no encontrada: " + delegationId);
    }
}
