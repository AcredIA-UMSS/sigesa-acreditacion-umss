package com.umss.sigesa.config;

import jakarta.persistence.EntityManager;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Aplica constraints DDL no generados por Hibernate (DD-UC-001).
 * Solo dev/test (H2): en prod usar Flyway ({@code application-prod.yaml}).
 */
@Component
@Profile("!prod")
@Order(100)
public class AuthSchemaInitializer implements ApplicationRunner {

    private final EntityManager entityManager;

    public AuthSchemaInitializer(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void run(ApplicationArguments args) {
        entityManager.createNativeQuery("""
                CREATE UNIQUE INDEX IF NOT EXISTS uk_upa_active
                ON user_program_assignment(user_id, program_id)
                WHERE revoked_at IS NULL
                """).executeUpdate();
    }
}
