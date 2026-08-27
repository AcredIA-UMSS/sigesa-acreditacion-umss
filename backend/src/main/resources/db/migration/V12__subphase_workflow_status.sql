-- Estado de workflow por subfase (modelo v1.1: Proceso → Fase → Subfase → Evidencia)

ALTER TABLE subphases
    ADD COLUMN IF NOT EXISTS status VARCHAR(32) NOT NULL DEFAULT 'PENDIENTE';

UPDATE subphases s
SET status = 'SUBIDO'
WHERE EXISTS (
    SELECT 1 FROM evidence e WHERE e.subphase_id = s.id
);

UPDATE subphases s
SET status = 'APROBADO'
WHERE status = 'SUBIDO'
  AND NOT EXISTS (
      SELECT 1 FROM subphase_observation o
      WHERE o.subphase_id = s.id AND o.status = 'OPEN'
  )
  AND EXISTS (
      SELECT 1 FROM evidence e WHERE e.subphase_id = s.id
  );

CREATE INDEX IF NOT EXISTS idx_subphases_status ON subphases(status);
