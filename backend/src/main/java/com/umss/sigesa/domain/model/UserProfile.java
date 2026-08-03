package com.umss.sigesa.domain.model;

import com.umss.sigesa.domain.exception.InvalidUserProfileException;
import com.umss.sigesa.domain.exception.WeakPasswordException;

import java.util.regex.Pattern;

public final class UserProfile {

    private static final Pattern NAME_PATTERN = Pattern.compile(
            "^(?=.*[\\p{L}])[\\p{L}\\s'.-]+$",
            Pattern.UNICODE_CHARACTER_CLASS
    );
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[67]\\d{7}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,}$");

    private UserProfile() {
    }

    public static String normalizeName(String value, String fieldLabel) {
        if (value == null || value.isBlank()) {
            throw new InvalidUserProfileException(fieldLabel + " es obligatorio.");
        }
        String trimmed = value.trim().replaceAll("\\s+", " ");
        if (!NAME_PATTERN.matcher(trimmed).matches()) {
            throw new InvalidUserProfileException(
                    fieldLabel + " debe contener al menos una letra y no puede ser solo números o símbolos.");
        }
        return trimmed;
    }

    public static String normalizePhone(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidUserProfileException("El celular es obligatorio.");
        }
        String digits = value.replaceAll("\\D", "");
        if (!PHONE_PATTERN.matcher(digits).matches()) {
            throw new InvalidUserProfileException(
                    "El celular debe tener 8 dígitos y estar entre 60000000 y 79999999.");
        }
        return digits;
    }

    public static void validatePassword(char[] password) {
        if (password == null || password.length == 0) {
            throw new WeakPasswordException();
        }
        if (!PASSWORD_PATTERN.matcher(new String(password)).matches()) {
            throw new WeakPasswordException();
        }
    }

    public static String fullName(String firstName, String lastName) {
        if (firstName == null && lastName == null) {
            return "";
        }
        if (firstName == null || firstName.isBlank()) {
            return lastName == null ? "" : lastName.trim();
        }
        if (lastName == null || lastName.isBlank()) {
            return firstName.trim();
        }
        return firstName.trim() + " " + lastName.trim();
    }
}
