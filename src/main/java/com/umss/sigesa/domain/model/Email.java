package com.umss.sigesa.domain.model;

import com.umss.sigesa.domain.exception.InvalidCredentialsException;
import com.umss.sigesa.domain.exception.InvalidEmailDomainException;

import java.util.Objects;

public final class Email {

    private static final String UMSS_SUFFIX = "@umss.edu.bo";

    private final String value;

    private Email(String value) {
        this.value = value;
    }

    public static Email of(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new InvalidEmailDomainException("El correo es obligatorio.");
        }
        String normalized = raw.trim().toLowerCase();
        if (!normalized.endsWith(UMSS_SUFFIX)) {
            throw new InvalidEmailDomainException("Solo se permiten correos institucionales @umss.edu.bo.");
        }
        return new Email(normalized);
    }

    /**
     * Validación de email en login: cualquier formato o dominio inválido se trata como
     * credenciales inválidas (FSD-UC-001 A1), sin revelar la causa.
     */
    public static Email forLogin(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new InvalidCredentialsException();
        }
        String normalized = raw.trim().toLowerCase();
        if (!normalized.endsWith(UMSS_SUFFIX)) {
            throw new InvalidCredentialsException();
        }
        return new Email(normalized);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Email email)) {
            return false;
        }
        return Objects.equals(value, email.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
