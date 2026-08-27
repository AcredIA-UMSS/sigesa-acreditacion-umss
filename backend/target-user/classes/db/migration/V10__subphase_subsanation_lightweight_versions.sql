-- FSD-UC-006 — subsanación por subfase, observación OPEN/RESOLVED, historial liviano (blob_purged)

ALTER TABLE subphase_observation
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'OPEN';

ALTER TABLE subphase_observation
    ADD COLUMN IF NOT EXISTS resolved_at TIMESTAMP;

ALTER TABLE subphase_observation
    ADD COLUMN IF NOT EXISTS resolved_version_id UUID;

UPDATE subphase_observation SET status = 'OPEN' WHERE status IS NULL OR TRIM(status) = '';

ALTER TABLE evidence_version
    ADD COLUMN IF NOT EXISTS observation_id UUID REFERENCES subphase_observation(id);

ALTER TABLE evidence_version
    ADD COLUMN IF NOT EXISTS supersedes_version_number INT;

ALTER TABLE evidence_version
    ADD COLUMN IF NOT EXISTS blob_purged BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE evidence_version
    ADD COLUMN IF NOT EXISTS original_filename VARCHAR(500);
