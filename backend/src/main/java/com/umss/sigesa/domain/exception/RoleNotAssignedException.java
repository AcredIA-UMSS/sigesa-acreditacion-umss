package com.umss.sigesa.domain.exception;

public class RoleNotAssignedException extends RuntimeException {

    public RoleNotAssignedException() {
        super("Acceso denegado: el usuario no tiene rol asignado.");
    }
}
