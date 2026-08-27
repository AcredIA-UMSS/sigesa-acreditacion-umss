CREATE TABLE IF NOT EXISTS normative_document (
    id UUID PRIMARY KEY,
    title VARCHAR(512) NOT NULL,
    template_type VARCHAR(32) NOT NULL,
    phase_name VARCHAR(256),
    subphase_name VARCHAR(256),
    source_url VARCHAR(2048),
    body_text TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_normative_document_template_type
    ON normative_document (template_type);

ALTER TABLE normative_document
    ADD COLUMN IF NOT EXISTS search_vector tsvector
    GENERATED ALWAYS AS (
        to_tsvector('spanish', coalesce(title, '') || ' ' || coalesce(body_text, ''))
    ) STORED;

CREATE INDEX IF NOT EXISTS idx_normative_document_search_vector
    ON normative_document USING GIN (search_vector);
