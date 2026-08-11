ALTER TABLE templates ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE templates ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED';
ALTER TABLE templates ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT now();
ALTER TABLE templates ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT now();

ALTER TABLE template_phases ADD COLUMN IF NOT EXISTS description TEXT;

ALTER TABLE template_subphases ADD COLUMN IF NOT EXISTS reference_url VARCHAR(2048);
ALTER TABLE template_subphases ADD COLUMN IF NOT EXISTS description TEXT;

ALTER TABLE phases ADD COLUMN IF NOT EXISTS description TEXT;

ALTER TABLE subphases ADD COLUMN IF NOT EXISTS reference_url VARCHAR(2048);
ALTER TABLE subphases ADD COLUMN IF NOT EXISTS description TEXT;

UPDATE templates SET status = 'PUBLISHED' WHERE status IS NULL;

UPDATE template_subphases
SET reference_url = 'https://duea.umss.edu.bo/normativa/pendiente'
WHERE reference_url IS NULL;

UPDATE subphases
SET reference_url = 'https://duea.umss.edu.bo/normativa/pendiente'
WHERE reference_url IS NULL;

CREATE INDEX IF NOT EXISTS idx_templates_status_type ON templates (status, type);
