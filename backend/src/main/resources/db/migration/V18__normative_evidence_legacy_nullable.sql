-- Cargas normativas v2 (API-EVD-01) no usan taxonomía legacy criterion_id / evidence.indicator_id.
-- V9 ya lo declaraba; este script asegura el estado en entornos Flyway completos.

ALTER TABLE evidence_version
    ALTER COLUMN criterion_id DROP NOT NULL;

ALTER TABLE evidence
    ALTER COLUMN indicator_id DROP NOT NULL;

ALTER TABLE evidence
    DROP CONSTRAINT IF EXISTS evidence_indicator_id_key;

ALTER TABLE evidence
    DROP CONSTRAINT IF EXISTS ukeeyit5ntrv7eiun68fkooerle;

CREATE UNIQUE INDEX IF NOT EXISTS uk_evidence_normative_indicator
    ON evidence (normative_indicator_id)
    WHERE normative_indicator_id IS NOT NULL;
