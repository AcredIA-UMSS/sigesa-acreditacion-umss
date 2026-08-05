package com.umss.sigesa.domain.exception;

public class WeakPasswordException extends RuntimeException {

    public static final String MESSAGE =
            "La contraseña debe tener al menos 8 caracteres e incluir letras y números.";

    public WeakPasswordException() {
        super(MESSAGE);
    }
}
