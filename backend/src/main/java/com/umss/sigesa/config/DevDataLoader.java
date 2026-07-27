package com.umss.sigesa.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Profile("!prod")
@Order(200)
public class DevDataLoader implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public DevDataLoader(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedTemplates();
        seedProcesses();
    }

    private void seedTemplates() {
        seedTemplate(DevSeedData.TEMPLATE_CEUB_2026, "CEUB 2026", "CEUB", true, DevSeedData.TAXONOMY_CEUB_VERSION);
        seedTemplate(DevSeedData.TEMPLATE_ARCUSUR_2026, "ARCU-SUR 2026", "ARCU_SUR", true, DevSeedData.TAXONOMY_ARCUSUR_VERSION);
        seedTemplate(DevSeedData.TEMPLATE_DRAFT, "Draft Template", "CEUB", false, DevSeedData.TAXONOMY_DRAFT_VERSION);
    }

    private void seedTemplate(UUID id, String name, String type, boolean validated, String taxonomyVersion) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM templates WHERE id = ?", Integer.class, id);
        if (count != null && count > 0) {
            return;
        }

        String activePeriod = validated ? DevSeedData.PERIOD_2026_1 : null;
        Timestamp activatedAt = validated ? Timestamp.valueOf(LocalDateTime.of(2026, 1, 1, 0, 0)) : null;

        jdbcTemplate.update(
                "INSERT INTO templates (id, name, type, validated, taxonomy_version, active_period, activated_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                id, name, type, validated, taxonomyVersion, activePeriod, activatedAt);

        // Standard phases and subphases for the template
        UUID phase1Id = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO template_phases (id, name, phase_order, template_id) VALUES (?, ?, ?, ?)",
                phase1Id, "Autoevaluación", 1, id);

        jdbcTemplate.update(
                "INSERT INTO template_subphases (id, name, subphase_order, template_phase_id) VALUES (?, ?, ?, ?)",
                UUID.randomUUID(), "Recopilación de Evidencias", 1, phase1Id);

        jdbcTemplate.update(
                "INSERT INTO template_subphases (id, name, subphase_order, template_phase_id) VALUES (?, ?, ?, ?)",
                UUID.randomUUID(), "Análisis y Juicio", 2, phase1Id);

        UUID phase2Id = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO template_phases (id, name, phase_order, template_id) VALUES (?, ?, ?, ?)",
                phase2Id, "Evaluación Externa", 2, id);

        jdbcTemplate.update(
                "INSERT INTO template_subphases (id, name, subphase_order, template_phase_id) VALUES (?, ?, ?, ?)",
                UUID.randomUUID(), "Visita de Pares", 1, phase2Id);
    }

    private void seedProcesses() {
        LocalDateTime base = LocalDateTime.of(2026, 1, 15, 10, 0);

        seedProcess(
                DevSeedData.PROCESS_INF_SIS_CEUB_ACTIVE,
                DevSeedData.TEMPLATE_CEUB_2026,
                DevSeedData.PROGRAM_INF_SIS,
                DevSeedData.PERIOD_2026_1,
                "CEUB",
                "ACTIVE",
                DevSeedData.TAXONOMY_CEUB_VERSION,
                base
        );

        seedProcess(
                DevSeedData.PROCESS_CEUB_CLOSED,
                DevSeedData.TEMPLATE_CEUB_2026,
                DevSeedData.PROGRAM_CEUB,
                DevSeedData.PERIOD_2025_2,
                "CEUB",
                "CLOSED",
                DevSeedData.TAXONOMY_CEUB_VERSION,
                base.minusMonths(6)
        );

        seedProcess(
                DevSeedData.PROCESS_ARCUSUR_ARCHIVED,
                DevSeedData.TEMPLATE_ARCUSUR_2026,
                DevSeedData.PROGRAM_ARCUSUR,
                DevSeedData.PERIOD_2025_2,
                "ARCU_SUR",
                "ARCHIVED",
                DevSeedData.TAXONOMY_ARCUSUR_VERSION,
                base.minusYears(1)
        );
    }

    private void seedProcess(UUID id,
                             UUID templateId,
                             UUID careerId,
                             String period,
                             String type,
                             String status,
                             String taxonomySnapshotVersion,
                             LocalDateTime createdAt) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM accreditation_processes WHERE id = ?", Integer.class, id);
        if (count != null && count > 0) {
            return;
        }

        Timestamp createdTimestamp = Timestamp.valueOf(createdAt);

        jdbcTemplate.update(
                "INSERT INTO accreditation_processes (id, template_id, career_id, period, type, status, taxonomy_snapshot_version, start_date, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                id, templateId, careerId, period, type, status, taxonomySnapshotVersion, createdTimestamp, createdTimestamp);

        // Matching phases and subphases for the process instance
        UUID phase1Id = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO phases (id, name, phase_order, process_id) VALUES (?, ?, ?, ?)",
                phase1Id, "Autoevaluación", 1, id);

        jdbcTemplate.update(
                "INSERT INTO subphases (id, name, subphase_order, phase_id) VALUES (?, ?, ?, ?)",
                UUID.randomUUID(), "Recopilación de Evidencias", 1, phase1Id);

        jdbcTemplate.update(
                "INSERT INTO subphases (id, name, subphase_order, phase_id) VALUES (?, ?, ?, ?)",
                UUID.randomUUID(), "Análisis y Juicio", 2, phase1Id);

        UUID phase2Id = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO phases (id, name, phase_order, process_id) VALUES (?, ?, ?, ?)",
                phase2Id, "Evaluación Externa", 2, id);

        jdbcTemplate.update(
                "INSERT INTO subphases (id, name, subphase_order, phase_id) VALUES (?, ?, ?, ?)",
                UUID.randomUUID(), "Visita de Pares", 1, phase2Id);
    }
}
