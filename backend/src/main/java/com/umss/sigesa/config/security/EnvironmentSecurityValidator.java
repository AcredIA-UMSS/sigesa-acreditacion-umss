package com.umss.sigesa.config.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class EnvironmentSecurityValidator implements SmartInitializingSingleton {

    private static final Logger log = LoggerFactory.getLogger(EnvironmentSecurityValidator.class);

    private static final int MIN_JWT_SECRET_BYTES = 32; // 256 bits for HMAC-SHA256

    private static final Set<String> FORBIDDEN_PROD_SECRETS = Set.of(
            "sigesa-dev-jwt-secret-key-minimum-256-bits-required-for-hmac-sha256",
            "secret",
            "password",
            "changeme",
            "admin"
    );

    private final Environment environment;

    public EnvironmentSecurityValidator(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void afterSingletonsInstantiated() {
        validate(this.environment);
    }

    public void validate(Environment env) {
        if (env == null || !env.acceptsProfiles(Profiles.of("prod"))) {
            log.debug("Perfil prod no activo; validación estricta de secretos omitida para entorno de desarrollo/test.");
            return;
        }

        log.info("Iniciando validación estricta de seguridad y fail-fast para entorno 'prod'...");
        List<String> violations = new ArrayList<>();

        // 1. Validar JWT Secret
        String jwtSecret = env.getProperty("sigesa.jwt.secret");
        if (jwtSecret == null || jwtSecret.isBlank()) {
            violations.add("Falta la variable de entorno obligatoria 'SIGESA_JWT_SECRET' (sigesa.jwt.secret).");
        } else {
            byte[] bytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
            if (bytes.length < MIN_JWT_SECRET_BYTES) {
                violations.add("El secreto 'SIGESA_JWT_SECRET' debe tener al menos " + MIN_JWT_SECRET_BYTES
                        + " bytes (256 bits) para HMAC-SHA256.");
            }
            if (FORBIDDEN_PROD_SECRETS.contains(jwtSecret.trim())) {
                violations.add("El valor de 'SIGESA_JWT_SECRET' coincide con un secreto por defecto inseguro no permitido en producción.");
            }
        }

        // 2. Validar Datasource URL
        String dbUrl = env.getProperty("spring.datasource.url");
        if (dbUrl == null || dbUrl.isBlank()) {
            violations.add("Falta la variable de entorno obligatoria 'DATABASE_URL' (spring.datasource.url).");
        } else if (dbUrl.contains("localhost") || dbUrl.contains("127.0.0.1")) {
            violations.add("La URL de base de datos 'DATABASE_URL' apunta a localhost en entorno productivo.");
        }

        // 3. Validar Datasource Password
        String dbPass = env.getProperty("spring.datasource.password");
        if (dbPass == null || dbPass.isBlank()) {
            violations.add("Falta la variable de entorno obligatoria 'DATABASE_PASSWORD' o 'SPRING_DATASOURCE_PASSWORD'.");
        }

        if (!violations.isEmpty()) {
            String errorMessage = "Fallo de validación de seguridad de entorno (Fail-Fast): \n- "
                    + String.join("\n- ", violations);
            log.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }

        log.info("Validación de seguridad de entorno 'prod' completada exitosamente.");
    }
}
