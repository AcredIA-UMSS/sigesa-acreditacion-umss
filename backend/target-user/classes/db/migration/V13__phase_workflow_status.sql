-- Estado de workflow por fase (modelo v1.1: cierre cuando todas las subfases APROBADO)

ALTER TABLE phases
    ADD COLUMN IF NOT EXISTS status VARCHAR(32) NOT NULL DEFAULT 'ABIERTA';

CREATE INDEX IF NOT EXISTS idx_phases_status ON phases(status);
