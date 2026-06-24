package com.umss.sigesa.config;

import jakarta.persistence.EntityManager;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("!prod")
@Order(100)
public class AuthSchemaInitializer implements ApplicationRunner {

    private final EntityManager entityManager;

    public AuthSchemaInitializer(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String dialect = entityManager.getEntityManagerFactory()
                .getProperties()
                .getOrDefault("hibernate.dialect", "")
                .toString().toLowerCase();

        if (dialect.contains("postgresql")) {
            entityManager.createNativeQuery("""
                    CREATE UNIQUE INDEX IF NOT EXISTS uk_upa_active
                    ON user_program_assignment(user_id, program_id)
                    WHERE revoked_at IS NULL
                    """).executeUpdate();
        }
        //este sirve
    }
}