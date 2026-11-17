package com.umss.sigesa.config.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class SecretSanitizerTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "password", "user_password", "dbPassword", "PASSWORD",
            "jwtSecret", "SIGESA_JWT_SECRET", "token", "accessToken",
            "apiKey", "api_key", "secretKey", "authHeader", "credential"
    })
    @DisplayName("isSensitiveKey detects sensitive keywords regardless of casing or symbols")
    void testIsSensitiveKey(String key) {
        assertThat(SecretSanitizer.isSensitiveKey(key)).isTrue();
    }

    @Test
    @DisplayName("isSensitiveKey returns false for non-sensitive keys and null")
    void testIsSensitiveKeyNonSensitive() {
        assertThat(SecretSanitizer.isSensitiveKey(null)).isFalse();
        assertThat(SecretSanitizer.isSensitiveKey("username")).isFalse();
        assertThat(SecretSanitizer.isSensitiveKey("email")).isFalse();
        assertThat(SecretSanitizer.isSensitiveKey("status")).isFalse();
        assertThat(SecretSanitizer.isSensitiveKey("code")).isFalse();
    }

    @Test
    @DisplayName("sanitizeValue masks sensitive values and preserves non-sensitive values")
    void testSanitizeValue() {
        assertThat(SecretSanitizer.sanitizeValue("password", "super-secret")).isEqualTo("******");
        assertThat(SecretSanitizer.sanitizeValue("jwt_secret", "xyz123")).isEqualTo("******");
        assertThat(SecretSanitizer.sanitizeValue("username", "admin")).isEqualTo("admin");
        assertThat(SecretSanitizer.sanitizeValue("null_value_key", null)).isNull();
    }

    @Test
    @DisplayName("mask correctly preserves prefixes and suffixes or fully masks short strings")
    void testMask() {
        assertThat(SecretSanitizer.mask(null)).isNull();
        assertThat(SecretSanitizer.mask("")).isEqualTo("******");
        assertThat(SecretSanitizer.mask("12345")).isEqualTo("******");
        assertThat(SecretSanitizer.mask("123456")).isEqualTo("******");
        assertThat(SecretSanitizer.mask("abcdefgh")).isEqualTo("ab******gh");
        assertThat(SecretSanitizer.mask("myVeryLongSecretKey123")).isEqualTo("my******23");
    }

    @Test
    @DisplayName("sanitizeJdbcUrl replaces password with asterisks in standard JDBC credentials")
    void testSanitizeJdbcUrl() {
        assertThat(SecretSanitizer.sanitizeJdbcUrl(null)).isNull();
        String plainUrl = "jdbc:postgresql://postgres:myPassword123@prod-db.umss.edu.bo:5432/sigesa_db";
        String sanitized = SecretSanitizer.sanitizeJdbcUrl(plainUrl);
        assertThat(sanitized).isEqualTo("jdbc:postgresql://postgres:******@prod-db.umss.edu.bo:5432/sigesa_db");
        assertThat(sanitized).doesNotContain("myPassword123");

        // Edge case: JDBC url without embedded user/password
        String urlWithoutPass = "jdbc:postgresql://prod-db.umss.edu.bo:5432/sigesa_db";
        assertThat(SecretSanitizer.sanitizeJdbcUrl(urlWithoutPass)).isEqualTo(urlWithoutPass);
    }
}
