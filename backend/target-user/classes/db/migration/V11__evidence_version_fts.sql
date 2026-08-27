-- FSD-UC-007 — FTS PostgreSQL sobre metadatos de versión vigente (GIN)

ALTER TABLE evidence_version
    ADD COLUMN IF NOT EXISTS search_vector tsvector
    GENERATED ALWAYS AS (
        to_tsvector(
            'spanish',
            coalesce(description, '') || ' ' || coalesce(original_filename, '')
        )
    ) STORED;

CREATE INDEX IF NOT EXISTS idx_evidence_version_search_vector
    ON evidence_version USING GIN (search_vector);
