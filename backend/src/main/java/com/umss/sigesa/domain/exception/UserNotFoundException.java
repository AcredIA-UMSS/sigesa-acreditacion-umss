package com.umss.sigesa.domain.exception;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(UUID userId) {
        super("Usuario no encontrado: " + userId);
    }
}
