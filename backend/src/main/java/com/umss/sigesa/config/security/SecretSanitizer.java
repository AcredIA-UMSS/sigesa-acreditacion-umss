package com.umss.sigesa.config.security;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SecretSanitizer {

    private static final Set<String> SENSITIVE_KEYWORDS = Set.of(
            "password", "secret", "token", "jwt", "apikey", "api_key",
            "privatekey", "private_key", "credential", "auth"
    );

    private static final Pattern JDBC_URL_USER_PASS_PATTERN =
            Pattern.compile("(?i)(//[^:]+):([^@]+)@");

    private SecretSanitizer() {
    }

    public static boolean isSensitiveKey(String key) {
        if (key == null) {
            return false;
        }
        String normalized = key.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        return SENSITIVE_KEYWORDS.stream().anyMatch(normalized::contains);
    }

    public static String sanitizeValue(String key, String value) {
        if (value == null) {
            return null;
        }
        if (isSensitiveKey(key)) {
            return "******";
        }
        return value;
    }

    public static String mask(String secret) {
        if (secret == null) {
            return null;
        }
        int length = secret.length();
        if (length <= 6) {
            return "******";
        }
        return secret.substring(0, 2) + "******" + secret.substring(length - 2);
    }

    public static String sanitizeJdbcUrl(String url) {
        if (url == null) {
            return null;
        }
        Matcher matcher = JDBC_URL_USER_PASS_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.replaceFirst("$1:******@");
        }
        return url;
    }
}
