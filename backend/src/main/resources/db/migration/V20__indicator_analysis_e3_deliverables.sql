-- Release 2.1.0 — M8 IndicatorAnalysis FODA + entregables E3 (ADR-0005 / FSD-UC-029)
-- Un análisis por indicador; backfill de entregables SYSTEMATIZATION.
-- La tabla física de indicadores es `indicators` (alias lógico normative_indicators).

-- ---------------------------------------------------------------------------
-- 1. indicator_analysis
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS indicator_analysis (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    indicator_id    UUID NOT NULL UNIQUE REFERENCES indicators(id) ON DELETE CASCADE,
    process_id      UUID NOT NULL REFERENCES accreditation_processes(id) ON DELETE CASCADE,
    foda_quadrant   VARCHAR(32),
    compliance      VARCHAR(32),
    synthesis       TEXT,
    author_id       UUID REFERENCES app_users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_indicator_analysis_foda CHECK (
        foda_quadrant IS NULL OR foda_quadrant IN ('STRENGTH', 'WEAKNESS', 'OPPORTUNITY', 'THREAT')
    ),
    CONSTRAINT chk_indicator_analysis_compliance CHECK (
        compliance IS NULL OR compliance IN ('COMPLIES', 'PARTIAL', 'DOES_NOT_COMPLY')
    )
);

CREATE INDEX IF NOT EXISTS idx_indicator_analysis_process
    ON indicator_analysis (process_id);

-- ---------------------------------------------------------------------------
-- 2. Encuesta primaria: audience libre 1–32 (API-WF-11; V17 era enum CEUB)
-- ---------------------------------------------------------------------------

ALTER TABLE primary_survey_batches
    DROP CONSTRAINT IF EXISTS chk_primary_survey_batches_audience;

ALTER TABLE primary_survey_batches
    DROP CONSTRAINT IF EXISTS chk_primary_survey_batches_audience_len;

ALTER TABLE primary_survey_batches
    ADD CONSTRAINT chk_primary_survey_batches_audience_len
        CHECK (char_length(btrim(audience)) BETWEEN 1 AND 32);

-- ---------------------------------------------------------------------------
-- 3. Backfill entregables E3 en etapas SYSTEMATIZATION existentes
-- ---------------------------------------------------------------------------

INSERT INTO stage_deliverables (stage_id, deliverable_code)
SELECT ms.id, e3.code
FROM methodological_stages ms
CROSS JOIN (VALUES
    ('FODA_FORMS_COMPLETE'),
    ('SYNTHESIS_MATRIX')
) AS e3(code)
WHERE ms.code = 'SYSTEMATIZATION'
  AND NOT EXISTS (
      SELECT 1
      FROM stage_deliverables sd
      WHERE sd.stage_id = ms.id
        AND sd.deliverable_code = e3.code
  );
