-- MOD-AUTH: índice parcial para asignaciones activas (DD-UC-001).
-- Aplicar en PostgreSQL prod vía Flyway/Liquibase o pipeline de migraciones.
-- H2 dev: también aplicado en runtime por AuthSchemaInitializer.

CREATE UNIQUE INDEX IF NOT EXISTS uk_upa_active
    ON user_program_assignment (user_id, program_id)
    WHERE revoked_at IS NULL;
