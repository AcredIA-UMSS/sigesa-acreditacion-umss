-- FSD-UC-022 / UC-004 — requisitos de subfase, evidencias múltiples por subfase, observaciones TD/JD

ALTER TABLE subphases
    ADD COLUMN IF NOT EXISTS requirements TEXT;

ALTER TABLE template_subphases
    ADD COLUMN IF NOT EXISTS requirements TEXT;

UPDATE subphases
SET requirements = COALESCE(requirements, description, 'Requisitos pendientes de definición')
WHERE requirements IS NULL OR TRIM(requirements) = '';

UPDATE template_subphases
SET requirements = COALESCE(requirements, description, 'Requisitos pendientes de definición')
WHERE requirements IS NULL OR TRIM(requirements) = '';

ALTER TABLE evidence
    ADD COLUMN IF NOT EXISTS subphase_id UUID REFERENCES subphases(id);

ALTER TABLE evidence
    ALTER COLUMN indicator_id DROP NOT NULL;

ALTER TABLE evidence
    DROP CONSTRAINT IF EXISTS evidence_indicator_id_key;

CREATE INDEX IF NOT EXISTS idx_evidence_subphase_id ON evidence(subphase_id);

ALTER TABLE evidence_version
    ALTER COLUMN criterion_id DROP NOT NULL;

CREATE TABLE IF NOT EXISTS subphase_observation (
    id              UUID PRIMARY KEY,
    subphase_id     UUID NOT NULL REFERENCES subphases(id) ON DELETE CASCADE,
    author_id       UUID NOT NULL,
    author_role     VARCHAR(10) NOT NULL,
    body            TEXT NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_subphase_observation_subphase ON subphase_observation(subphase_id);
