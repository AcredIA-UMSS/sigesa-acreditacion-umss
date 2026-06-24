package com.umss.sigesa.config;

import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Aplica constraints DDL no generados por Hibernate (DD-UC-001).
 * PostgreSQL dev: índice parcial en runtime. Prod: Flyway ({@code application-prod.yaml}).
 * H2 test: omitido — H2 no soporta {@code CREATE INDEX ... WHERE}; la regla se valida en servicio.
 */
@Component
@Profile("!prod")
@Order(100)
public class AuthSchemaInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AuthSchemaInitializer.class);

    private final EntityManager entityManager;
    private final DataSource dataSource;

    public AuthSchemaInitializer(EntityManager entityManager, DataSource dataSource) {
        this.entityManager = entityManager;
        this.dataSource = dataSource;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!isPostgreSql()) {
            log.debug("Skipping partial index uk_upa_active: not PostgreSQL (H2 uses service-level validation)");
            return;
        }
        entityManager.createNativeQuery("""
                CREATE UNIQUE INDEX IF NOT EXISTS uk_upa_active
                ON user_program_assignment(user_id, program_id)
                WHERE revoked_at IS NULL
                """).executeUpdate();
    }

    private boolean isPostgreSql() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.getMetaData().getDatabaseProductName().toLowerCase().contains("postgresql");
        } catch (SQLException ex) {
            throw new IllegalStateException("Cannot detect database product", ex);
        }
    }
}
