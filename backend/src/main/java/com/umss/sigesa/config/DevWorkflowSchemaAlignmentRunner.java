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
        alignNormativeEvidenceColumns();
        backfillDocumentAssets();
        alignSurveyAudienceConstraint();
        backfillSystematizationDeliverables();
        backfillDraftingDeliverables();
        alignImprovementPlanUniqueIndex();
        alignCaeRoleColumn();
        alignImprovementPlanM11Columns();
        backfillE5E6E7Deliverables();
        alignEvidenceUploadLockTable();
        alignReportExportJobFormatConstraint();
    }

    private void alignReportExportJobFormatConstraint() {
        try {
            jdbcTemplate.execute("""
                    ALTER TABLE tb_report_export_job DROP CONSTRAINT IF EXISTS tb_report_export_job_format_check;
                    ALTER TABLE tb_report_export_job ADD CONSTRAINT tb_report_export_job_format_check
                        CHECK (format::text = ANY (ARRAY['XLSX'::character varying, 'CSV'::character varying, 'PDF'::character varying, 'ZIP'::character varying]::text[]));
                    """);
            log.info("Dev schema: tb_report_export_job_format_check alineado con ZIP.");
        } catch (Exception ex) {
            log.warn("No se pudo alinear tb_report_export_job_format_check en dev: {}", ex.getMessage());
        }
    }

    private void alignEvidenceUploadLockTable() {
        try {
            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS evidence_upload_lock (
                        indicator_id UUID PRIMARY KEY,
                        acquired_at  TIMESTAMPTZ NOT NULL DEFAULT now()
                    )
                    """);
            log.info("Dev schema: evidence_upload_lock alineado.");
        } catch (Exception ex) {
            log.warn("No se pudo alinear evidence_upload_lock en dev: {}", ex.getMessage());
        }
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

    private void alignNormativeEvidenceColumns() {
        try {
            jdbcTemplate.execute("""
                    ALTER TABLE evidence_version
                        ALTER COLUMN criterion_id DROP NOT NULL
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE evidence
                        ALTER COLUMN indicator_id DROP NOT NULL
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE evidence
                        DROP CONSTRAINT IF EXISTS evidence_indicator_id_key
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE evidence
                        DROP CONSTRAINT IF EXISTS ukeeyit5ntrv7eiun68fkooerle
                    """);
            jdbcTemplate.execute("""
                    CREATE UNIQUE INDEX IF NOT EXISTS uk_evidence_normative_indicator
                        ON evidence (normative_indicator_id)
                        WHERE normative_indicator_id IS NOT NULL
                    """);
            log.info("Dev schema: evidence / evidence_version alineados para cargas normativas.");
        } catch (Exception ex) {
            log.warn("No se pudo alinear evidence en dev: {}", ex.getMessage());
        }
    }

    /**
     * Flyway V19 no corre en Docker dev. Hibernate crea las tablas; este backfill
     * agrupa evidencias normativas existentes por (process_id, content_hash).
     */
    private void backfillDocumentAssets() {
        try {
            Integer tables = jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(*) FROM information_schema.tables
                    WHERE table_schema = 'public' AND table_name = 'document_assets'
                    """,
                    Integer.class);
            if (tables == null || tables == 0) {
                return;
            }
            jdbcTemplate.execute("""
                    INSERT INTO document_assets (
                        id, process_id, folio_code, title, storage_key, content_hash,
                        is_institutional, uploaded_by, created_at
                    )
                    SELECT
                        f.asset_id,
                        f.process_id,
                        'SIGESA-' || to_char(f.created_at, 'YYYYMMDD') || '-' || lower(substr(f.content_hash, 1, 8))
                            || CASE
                                WHEN COUNT(*) OVER (
                                    PARTITION BY to_char(f.created_at, 'YYYYMMDD'), lower(substr(f.content_hash, 1, 8))
                                ) > 1
                                THEN '-' || substr(replace(f.process_id::text, '-', ''), 1, 4)
                                ELSE ''
                               END,
                        LEFT(f.title, 500),
                        f.storage_key,
                        f.content_hash,
                        FALSE,
                        f.created_by,
                        f.created_at
                    FROM (
                        SELECT DISTINCT ON (process_id, content_hash)
                            gen_random_uuid() AS asset_id,
                            process_id,
                            content_hash,
                            storage_key,
                            COALESCE(NULLIF(btrim(original_filename), ''), NULLIF(btrim(description), ''), 'Evidencia') AS title,
                            created_by,
                            created_at
                        FROM (
                            SELECT lv.*, l1.process_id
                            FROM (
                                SELECT DISTINCT ON (e.id)
                                    e.id AS evidence_id,
                                    e.normative_indicator_id,
                                    ev.content_hash,
                                    ev.storage_key,
                                    ev.description,
                                    ev.created_by,
                                    ev.created_at,
                                    ev.original_filename
                                FROM evidence e
                                JOIN evidence_version ev ON ev.evidence_id = e.id
                                WHERE e.normative_indicator_id IS NOT NULL
                                ORDER BY e.id, ev.version_number DESC
                            ) lv
                            JOIN indicators i ON i.id = lv.normative_indicator_id
                            JOIN level3_nodes l3 ON l3.id = i.level3_id
                            JOIN level2_nodes l2 ON l2.id = l3.level2_id
                            JOIN level1_nodes l1 ON l1.id = l2.level1_id
                            WHERE lv.content_hash IS NOT NULL AND btrim(lv.content_hash) <> ''
                        ) with_process
                        ORDER BY process_id, content_hash, created_at ASC
                    ) f
                    WHERE NOT EXISTS (
                        SELECT 1 FROM document_assets da
                        WHERE da.process_id = f.process_id AND da.content_hash = f.content_hash
                    )
                    """);
            jdbcTemplate.execute("""
                    INSERT INTO indicator_evidence_links (
                        id, indicator_id, document_asset_id, evidence_id, is_primary_reference, created_at
                    )
                    SELECT
                        gen_random_uuid(),
                        src.normative_indicator_id,
                        da.id,
                        src.evidence_id,
                        TRUE,
                        src.created_at
                    FROM (
                        SELECT DISTINCT ON (e.id)
                            e.id AS evidence_id,
                            e.normative_indicator_id,
                            ev.content_hash,
                            ev.created_at,
                            l1.process_id
                        FROM evidence e
                        JOIN evidence_version ev ON ev.evidence_id = e.id
                        JOIN indicators i ON i.id = e.normative_indicator_id
                        JOIN level3_nodes l3 ON l3.id = i.level3_id
                        JOIN level2_nodes l2 ON l2.id = l3.level2_id
                        JOIN level1_nodes l1 ON l1.id = l2.level1_id
                        WHERE e.normative_indicator_id IS NOT NULL
                        ORDER BY e.id, ev.version_number DESC
                    ) src
                    JOIN document_assets da
                        ON da.process_id = src.process_id AND da.content_hash = src.content_hash
                    WHERE NOT EXISTS (
                        SELECT 1 FROM indicator_evidence_links iel
                        WHERE iel.indicator_id = src.normative_indicator_id
                          AND iel.document_asset_id = da.id
                    )
                    """);
            log.info("Dev schema: document_assets / indicator_evidence_links backfill M7 aplicado.");
        } catch (Exception ex) {
            log.warn("No se pudo backfillear DocumentAsset en dev: {}", ex.getMessage());
        }
    }

    private void alignSurveyAudienceConstraint() {
        try {
            jdbcTemplate.execute("""
                    ALTER TABLE primary_survey_batches
                        DROP CONSTRAINT IF EXISTS chk_primary_survey_batches_audience
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE primary_survey_batches
                        DROP CONSTRAINT IF EXISTS chk_primary_survey_batches_audience_len
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE primary_survey_batches
                        ADD CONSTRAINT chk_primary_survey_batches_audience_len
                            CHECK (char_length(btrim(audience)) BETWEEN 1 AND 32)
                    """);
            log.info("Dev schema: audience de encuesta primaria alineado a 1–32.");
        } catch (Exception ex) {
            log.warn("No se pudo alinear audience de encuesta en dev: {}", ex.getMessage());
        }
    }

    private void backfillSystematizationDeliverables() {
        try {
            Integer tables = jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(*) FROM information_schema.tables
                    WHERE table_schema = 'public' AND table_name = 'stage_deliverables'
                    """,
                    Integer.class);
            if (tables == null || tables == 0) {
                return;
            }
            jdbcTemplate.execute("""
                    INSERT INTO stage_deliverables (id, stage_id, deliverable_code, approval_status, created_at, updated_at)
                    SELECT gen_random_uuid(), ms.id, e3.code, 'PENDING', now(), now()
                    FROM methodological_stages ms
                    CROSS JOIN (VALUES
                        ('FODA_FORMS_COMPLETE'),
                        ('SYNTHESIS_MATRIX')
                    ) AS e3(code)
                    WHERE ms.code = 'SYSTEMATIZATION'
                      AND NOT EXISTS (
                          SELECT 1 FROM stage_deliverables sd
                          WHERE sd.stage_id = ms.id AND sd.deliverable_code = e3.code
                      )
                    """);
            log.info("Dev schema: entregables E3 (FODA_FORMS_COMPLETE, SYNTHESIS_MATRIX) asegurados.");
        } catch (Exception ex) {
            log.warn("No se pudo backfillear entregables E3 en dev: {}", ex.getMessage());
        }
    }

    private void backfillDraftingDeliverables() {
        try {
            Integer tables = jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(*) FROM information_schema.tables
                    WHERE table_schema = 'public' AND table_name = 'stage_deliverables'
                    """,
                    Integer.class);
            if (tables == null || tables == 0) {
                return;
            }
            jdbcTemplate.execute("""
                    INSERT INTO stage_deliverables (id, stage_id, deliverable_code, approval_status, created_at, updated_at)
                    SELECT gen_random_uuid(), ms.id, e4.code, 'PENDING', now(), now()
                    FROM methodological_stages ms
                    CROSS JOIN (VALUES
                        ('SELF_ASSESSMENT_REPORT'),
                        ('OFFICIAL_DATA_FORM'),
                        ('IMPROVEMENT_PLAN'),
                        ('ACADEMIC_DEV_PLAN')
                    ) AS e4(code)
                    WHERE ms.code = 'DRAFTING'
                      AND NOT EXISTS (
                          SELECT 1 FROM stage_deliverables sd
                          WHERE sd.stage_id = ms.id AND sd.deliverable_code = e4.code
                      )
                    """);
            log.info("Dev schema: entregables E4 (informe, ficha, plan mejoras, plan académico) asegurados.");
        } catch (Exception ex) {
            log.warn("No se pudo backfillear entregables E4 en dev: {}", ex.getMessage());
        }
    }

    private void alignImprovementPlanUniqueIndex() {
        try {
            Integer tables = jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(*) FROM information_schema.tables
                    WHERE table_schema = 'public' AND table_name = 'improvement_plan_items'
                    """,
                    Integer.class);
            if (tables == null || tables == 0) {
                return;
            }
            jdbcTemplate.execute("""
                    CREATE UNIQUE INDEX IF NOT EXISTS uk_improvement_plan_items_process_source
                        ON improvement_plan_items (process_id, source_indicator_id)
                        WHERE source_indicator_id IS NOT NULL
                    """);
            log.info("Dev schema: índice único parcial de Plan de Mejoras asegurado.");
        } catch (Exception ex) {
            log.warn("No se pudo alinear índice único de Plan de Mejoras en dev: {}", ex.getMessage());
        }
    }

    private void alignCaeRoleColumn() {
        try {
            jdbcTemplate.execute("ALTER TABLE app_user ALTER COLUMN role TYPE VARCHAR(20)");
            jdbcTemplate.execute("ALTER TABLE app_user DROP CONSTRAINT IF EXISTS app_user_role_check");
            jdbcTemplate.execute("""
                    ALTER TABLE app_user ADD CONSTRAINT app_user_role_check
                        CHECK (role IN ('CC', 'TD', 'JD', 'EE', 'CAE_MEMBER'))
                    """);
            log.info("Dev schema: app_user.role alineado a VARCHAR(20) con CAE_MEMBER.");
        } catch (Exception ex) {
            log.warn("No se pudo alinear role CAE_MEMBER en dev: {}", ex.getMessage());
        }
    }

    private void alignImprovementPlanM11Columns() {
        try {
            jdbcTemplate.execute("""
                    ALTER TABLE improvement_plan_items
                        ADD COLUMN IF NOT EXISTS justification TEXT
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE improvement_plan_items
                        ADD COLUMN IF NOT EXISTS progress_evidence_asset_id UUID
                    """);
            log.info("Dev schema: columnas M11 de Plan de Mejoras alineadas.");
        } catch (Exception ex) {
            log.warn("No se pudo alinear columnas M11 del plan en dev: {}", ex.getMessage());
        }
    }

    private void backfillE5E6E7Deliverables() {
        try {
            Integer tables = jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(*) FROM information_schema.tables
                    WHERE table_schema = 'public' AND table_name = 'stage_deliverables'
                    """,
                    Integer.class);
            if (tables == null || tables == 0) {
                return;
            }
            jdbcTemplate.execute("""
                    INSERT INTO stage_deliverables (id, stage_id, deliverable_code, approval_status, created_at, updated_at)
                    SELECT gen_random_uuid(), ms.id, extra.code, 'PENDING', now(), now()
                    FROM methodological_stages ms
                    CROSS JOIN (VALUES
                        ('PRESENTATION', 'COMMUNITY_VALIDATION_MINUTES'),
                        ('PRESENTATION', 'FORMAL_SUBMISSION_PACKAGE'),
                        ('EXTERNAL_PREP', 'SIMULATION_REPORT'),
                        ('EXTERNAL_PREP', 'PEER_ROOM_CHECKLIST'),
                        ('POST_ACCREDITATION', 'IMPROVEMENT_PROGRESS_REPORT')
                    ) AS extra(stage_code, code)
                    WHERE ms.code = extra.stage_code
                      AND NOT EXISTS (
                          SELECT 1 FROM stage_deliverables sd
                          WHERE sd.stage_id = ms.id AND sd.deliverable_code = extra.code
                      )
                    """);
            log.info("Dev schema: entregables E5/E6/E7 asegurados.");
        } catch (Exception ex) {
            log.warn("No se pudo backfillear entregables E5–E7 en dev: {}", ex.getMessage());
        }
    }
}
