package com.umss.sigesa.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Dev Docker usa {@code ddl-auto: update} con Flyway deshabilitado. V17 puede quedar a medias
 * (p. ej. {@code current_stage_id} sin {@code operational_mode}), rompiendo consultas JPA post-login.
 */
@Component
@Profile("dev")
@Order(0)
public class DevWorkflowSchemaAlignmentRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DevWorkflowSchemaAlignmentRunner.class);

    private final JdbcTemplate jdbcTemplate;

    public DevWorkflowSchemaAlignmentRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        alignAccreditationProcessColumns();
    }

    private void alignAccreditationProcessColumns() {
        try {
            jdbcTemplate.execute("""
                    ALTER TABLE accreditation_processes
                        ADD COLUMN IF NOT EXISTS operational_mode VARCHAR(32)
                    """);
            jdbcTemplate.update("""
                    UPDATE accreditation_processes
                    SET operational_mode = 'ACTIVE'
                    WHERE operational_mode IS NULL
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE accreditation_processes
                        ALTER COLUMN operational_mode SET DEFAULT 'ACTIVE'
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE accreditation_processes
                        ALTER COLUMN operational_mode SET NOT NULL
                    """);
            log.info("Dev schema: accreditation_processes.operational_mode alineado.");
        } catch (Exception ex) {
            log.warn("No se pudo alinear operational_mode en dev: {}", ex.getMessage());
        }
    }
}
