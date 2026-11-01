package com.umss.sigesa.config.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnvironmentSecurityValidatorTest {

    @Test
    @DisplayName("Dev / Test profile does not fail even if prod variables are absent")
    void testDevProfileDoesNotFail() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("dev");

        EnvironmentSecurityValidator validator = new EnvironmentSecurityValidator(env);
        assertThatCode(validator::afterSingletonsInstantiated).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Prod profile fails when JWT secret is missing")
    void testProdFailsWhenJwtSecretMissing() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");
        env.setProperty("spring.datasource.url", "jdbc:postgresql://db-cluster.aws.internal:5432/sigesa");
        env.setProperty("spring.datasource.password", "SuperSecurePassword123!");

        EnvironmentSecurityValidator validator = new EnvironmentSecurityValidator(env);
        assertThatThrownBy(validator::afterSingletonsInstantiated)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SIGESA_JWT_SECRET");
    }

    @Test
    @DisplayName("Prod profile fails when JWT secret is shorter than 32 bytes (256 bits)")
    void testProdFailsWhenJwtSecretTooShort() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");
        env.setProperty("sigesa.jwt.secret", "short-secret-12345");
        env.setProperty("spring.datasource.url", "jdbc:postgresql://db-cluster.aws.internal:5432/sigesa");
        env.setProperty("spring.datasource.password", "SuperSecurePassword123!");

        EnvironmentSecurityValidator validator = new EnvironmentSecurityValidator(env);
        assertThatThrownBy(validator::afterSingletonsInstantiated)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("256 bits");
    }

    @Test
    @DisplayName("Prod profile fails when JWT secret is default dev placeholder")
    void testProdFailsWhenJwtSecretIsPlaceholder() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");
        env.setProperty("sigesa.jwt.secret", "sigesa-dev-jwt-secret-key-minimum-256-bits-required-for-hmac-sha256");
        env.setProperty("spring.datasource.url", "jdbc:postgresql://db-cluster.aws.internal:5432/sigesa");
        env.setProperty("spring.datasource.password", "SuperSecurePassword123!");

        EnvironmentSecurityValidator validator = new EnvironmentSecurityValidator(env);
        assertThatThrownBy(validator::afterSingletonsInstantiated)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("coincide con un secreto por defecto");
    }

    @Test
    @DisplayName("Prod profile fails when DATABASE_URL points to localhost")
    void testProdFailsWhenDatabasePointsToLocalhost() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");
        env.setProperty("sigesa.jwt.secret", "a".repeat(40));
        env.setProperty("spring.datasource.url", "jdbc:postgresql://localhost:5432/sigesa");
        env.setProperty("spring.datasource.password", "SuperSecurePassword123!");

        EnvironmentSecurityValidator validator = new EnvironmentSecurityValidator(env);
        assertThatThrownBy(validator::afterSingletonsInstantiated)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("localhost");
    }

    @Test
    @DisplayName("Prod profile fails when DATABASE_PASSWORD is missing")
    void testProdFailsWhenDatabasePasswordMissing() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");
        env.setProperty("sigesa.jwt.secret", "a".repeat(40));
        env.setProperty("spring.datasource.url", "jdbc:postgresql://db-cluster.internal:5432/sigesa");

        EnvironmentSecurityValidator validator = new EnvironmentSecurityValidator(env);
        assertThatThrownBy(validator::afterSingletonsInstantiated)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("DATABASE_PASSWORD");
    }

    @Test
    @DisplayName("Prod profile passes when all variables are valid and strong")
    void testProdPassesWithValidConfig() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");
        env.setProperty("sigesa.jwt.secret", "VerySecureProductionSecretKeyThatHasMoreThan256Bits1234567890!");
        env.setProperty("spring.datasource.url", "jdbc:postgresql://prod-db.umss.edu.bo:5432/sigesa_prod");
        env.setProperty("spring.datasource.password", "P@ssw0rdProd2026!!");

        EnvironmentSecurityValidator validator = new EnvironmentSecurityValidator(env);
        assertThatCode(validator::afterSingletonsInstantiated).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("SecretSanitizer correctly masks sensitive keys and JDBC URLs")
    void testSecretSanitizer() {
        assertThat(SecretSanitizer.isSensitiveKey("jwtSecret")).isTrue();
        assertThat(SecretSanitizer.isSensitiveKey("spring.datasource.password")).isTrue();
        assertThat(SecretSanitizer.isSensitiveKey("applicationName")).isFalse();

        assertThat(SecretSanitizer.sanitizeValue("jwtSecret", "my-secret")).isEqualTo("******");
        assertThat(SecretSanitizer.sanitizeValue("appName", "SIGESA")).isEqualTo("SIGESA");
        assertThat(SecretSanitizer.sanitizeValue("password", null)).isNull();

        assertThat(SecretSanitizer.mask("secret-super-long")).startsWith("se").endsWith("ng");
        assertThat(SecretSanitizer.mask("short")).isEqualTo("******");
        assertThat(SecretSanitizer.mask(null)).isNull();

        String jdbcUrl = "jdbc:postgresql://sigesa_user:mySecretPassword123@db.prod.internal:5432/sigesa";
        assertThat(SecretSanitizer.sanitizeJdbcUrl(jdbcUrl))
                .isEqualTo("jdbc:postgresql://sigesa_user:******@db.prod.internal:5432/sigesa");
        assertThat(SecretSanitizer.sanitizeJdbcUrl(null)).isNull();
    }
}
