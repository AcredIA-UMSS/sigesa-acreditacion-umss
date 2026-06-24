package com.umss.sigesa.adapter.out.auth;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;

public final class PasswordUtils {

    private PasswordUtils() {
    }

    public static String encode(PasswordEncoder encoder, char[] rawPassword) {
        try {
            return encoder.encode(new String(rawPassword));
        } finally {
            clear(rawPassword);
        }
    }

    public static boolean matches(PasswordEncoder encoder, char[] rawPassword, String passwordHash) {
        try {
            return encoder.matches(new String(rawPassword), passwordHash);
        } finally {
            clear(rawPassword);
        }
    }

    public static void clear(char[] password) {
        if (password != null) {
            Arrays.fill(password, '\0');
        }
    }
}