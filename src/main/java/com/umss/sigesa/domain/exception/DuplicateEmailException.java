package com.umss.sigesa.domain.exception;

public class DuplicateEmailException extends RuntimeException {

    public static final String MESSAGE = "El correo ya está registrado.";

    public DuplicateEmailException() {
        super(MESSAGE);
    }
}
