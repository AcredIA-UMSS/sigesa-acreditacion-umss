package com.umss.sigesa.domain.model;

/**
 * Snapshot de correo para la bitácora. Solo conserva un {@code @umss.edu.bo}
 * válido de hasta 150 caracteres. Cualquier otro valor, incluida una contraseña
 * u otro texto libre, se descarta.
 */
public final class AuditActorEmail {

    static final int MAX_LENGTH = 150;
    private static final String UMSS_SUFFIX = "@umss.edu.bo";

    private AuditActorEmail() {
    }

    public static String snapshot(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim().toLowerCase();
        if (trimmed.isEmpty() || trimmed.length() > MAX_LENGTH) {
            return null;
        }
        int at = trimmed.indexOf('@');
        if (at <= 0 || at != trimmed.lastIndexOf('@') || !trimmed.endsWith(UMSS_SUFFIX)) {
            return null;
        }
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (Character.isWhitespace(c) || c == '"' || c == '\'' || c == '\\' || c == '<' || c == '>') {
                return null;
            }
        }
        return trimmed;
    }
}
