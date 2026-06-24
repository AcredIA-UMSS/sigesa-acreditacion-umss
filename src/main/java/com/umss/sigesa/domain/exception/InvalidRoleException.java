package com.umss.sigesa.domain.exception;

public class InvalidRoleException extends RuntimeException {

    public InvalidRoleException(String role) {
        super("Rol inválido: " + role + ". Valores permitidos: CC, TD, JD.");
    }
}
