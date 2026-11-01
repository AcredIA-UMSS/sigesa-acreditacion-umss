package com.umss.sigesa.domain.exception;

public class AuditEventNotFoundException extends RuntimeException {

    public AuditEventNotFoundException() {
        super("Evento de auditoría no encontrado.");
    }
}
