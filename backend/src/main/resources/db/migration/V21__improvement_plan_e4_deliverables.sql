-- Release 2.1.0 — M9 Plan de Mejoras + entregables E4 (ADR-0005 / FSD-UC-031)
-- UNIQUE parcial (process_id, source_indicator_id) WHERE source_indicator_id IS NOT NULL.
-- H2 de tests no usa esta migración (ddl-auto); unicidad de source se aplica en dominio/servicio.

-- ---------------------------------------------------------------------------
-- 1. improvement_plan_items
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS improvement_plan_items (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    process_id           UUID NOT NULL REFERENCES accreditation_processes(id) ON DELETE CASCADE,
    source_indicator_id  UUID REFERENCES indicators(id) ON DELETE SET NULL,
    objective            TEXT NOT NULL,
    activity             TEXT,
    responsible_id       UUID REFERENCES app_users(id),
    deadline             DATE,
    progress             INT NOT NULL DEFAULT 0,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_improvement_plan_items_progress CHECK (progress BETWEEN 0 AND 100)
);

CREATE INDEX IF NOT EXISTS idx_improvement_plan_items_process
    ON improvement_plan_items (process_id);

-- PostgreSQL: índice único parcial. H2 no ejecuta este script en el perfil test.
CREATE UNIQUE INDEX IF NOT EXISTS uk_improvement_plan_items_process_source
    ON improvement_plan_items (process_id, source_indicator_id)
    WHERE source_indicator_id IS NOT NULL;

-- ---------------------------------------------------------------------------
-- 2. Backfill entregables E4 en etapas DRAFTING existentes
-- ---------------------------------------------------------------------------

INSERT INTO stage_deliverables (stage_id, deliverable_code)
SELECT ms.id, e4.code
FROM methodological_stages ms
CROSS JOIN (VALUES
    ('SELF_ASSESSMENT_REPORT'),
    ('OFFICIAL_DATA_FORM'),
    ('IMPROVEMENT_PLAN'),
    ('ACADEMIC_DEV_PLAN')
) AS e4(code)
WHERE ms.code = 'DRAFTING'
  AND NOT EXISTS (
      SELECT 1
      FROM stage_deliverables sd
      WHERE sd.stage_id = ms.id
        AND sd.deliverable_code = e4.code
  );
