-- Release 2.1.0 — M7 DocumentAsset M:N (ADR-0005 / FSD-UC-004 / FSD-UC-005)
-- Blob físico en document_assets; Evidence sigue 1:1 por indicador normativo.

-- ---------------------------------------------------------------------------
-- 1. document_assets
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS document_assets (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    process_id          UUID NOT NULL REFERENCES accreditation_processes(id) ON DELETE CASCADE,
    folio_code          VARCHAR(64) NOT NULL,
    title               VARCHAR(500) NOT NULL,
    storage_key         VARCHAR(500) NOT NULL,
    content_hash        VARCHAR(64) NOT NULL,
    is_institutional    BOOLEAN NOT NULL DEFAULT FALSE,
    uploaded_by         UUID NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_document_assets_folio UNIQUE (folio_code),
    CONSTRAINT uk_document_assets_process_hash UNIQUE (process_id, content_hash)
);

CREATE INDEX IF NOT EXISTS idx_document_assets_process
    ON document_assets (process_id);

CREATE INDEX IF NOT EXISTS idx_document_assets_hash
    ON document_assets (content_hash);

-- ---------------------------------------------------------------------------
-- 2. indicator_evidence_links
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS indicator_evidence_links (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    indicator_id            UUID NOT NULL REFERENCES indicators(id) ON DELETE CASCADE,
    document_asset_id       UUID NOT NULL REFERENCES document_assets(id),
    evidence_id             UUID NOT NULL REFERENCES evidence(id) ON DELETE CASCADE,
    is_primary_reference    BOOLEAN NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_indicator_evidence_link UNIQUE (indicator_id, document_asset_id)
);

CREATE INDEX IF NOT EXISTS idx_indicator_evidence_links_asset
    ON indicator_evidence_links (document_asset_id);

CREATE INDEX IF NOT EXISTS idx_indicator_evidence_links_evidence
    ON indicator_evidence_links (evidence_id);

-- FK opcional desde entregables de etapa (columna ya existe en V17)
ALTER TABLE stage_deliverables
    DROP CONSTRAINT IF EXISTS fk_stage_deliverables_document_asset;

ALTER TABLE stage_deliverables
    ADD CONSTRAINT fk_stage_deliverables_document_asset
        FOREIGN KEY (document_asset_id) REFERENCES document_assets(id);

-- ---------------------------------------------------------------------------
-- 3. Backfill desde evidencias normativas vigentes (idempotente)
-- ---------------------------------------------------------------------------

WITH latest_versions AS (
    SELECT DISTINCT ON (e.id)
        e.id                    AS evidence_id,
        e.normative_indicator_id,
        ev.content_hash,
        ev.storage_key,
        ev.description,
        ev.created_by,
        ev.created_at,
        ev.original_filename
    FROM evidence e
    JOIN evidence_version ev ON ev.evidence_id = e.id
    WHERE e.normative_indicator_id IS NOT NULL
    ORDER BY e.id, ev.version_number DESC
),
with_process AS (
    SELECT
        lv.*,
        l1.process_id
    FROM latest_versions lv
    JOIN indicators i ON i.id = lv.normative_indicator_id
    JOIN level3_nodes l3 ON l3.id = i.level3_id
    JOIN level2_nodes l2 ON l2.id = l3.level2_id
    JOIN level1_nodes l1 ON l1.id = l2.level1_id
    WHERE lv.content_hash IS NOT NULL AND btrim(lv.content_hash) <> ''
),
first_per_hash AS (
    SELECT DISTINCT ON (process_id, content_hash)
        gen_random_uuid() AS asset_id,
        process_id,
        content_hash,
        storage_key,
        COALESCE(NULLIF(btrim(original_filename), ''), NULLIF(btrim(description), ''), 'Evidencia') AS title,
        created_by,
        created_at
    FROM with_process
    ORDER BY process_id, content_hash, created_at ASC
)
INSERT INTO document_assets (
    id, process_id, folio_code, title, storage_key, content_hash,
    is_institutional, uploaded_by, created_at
)
SELECT
    f.asset_id,
    f.process_id,
    'SIGESA-' || to_char(f.created_at, 'YYYYMMDD') || '-' || lower(substr(f.content_hash, 1, 8))
        || CASE
            WHEN COUNT(*) OVER (
                PARTITION BY to_char(f.created_at, 'YYYYMMDD'), lower(substr(f.content_hash, 1, 8))
            ) > 1
            THEN '-' || substr(replace(f.process_id::text, '-', ''), 1, 4)
            ELSE ''
           END,
    LEFT(f.title, 500),
    f.storage_key,
    f.content_hash,
    FALSE,
    f.created_by,
    f.created_at
FROM first_per_hash f
WHERE NOT EXISTS (
    SELECT 1
    FROM document_assets da
    WHERE da.process_id = f.process_id
      AND da.content_hash = f.content_hash
);

INSERT INTO indicator_evidence_links (
    id, indicator_id, document_asset_id, evidence_id, is_primary_reference, created_at
)
SELECT
    gen_random_uuid(),
    src.normative_indicator_id,
    da.id,
    src.evidence_id,
    TRUE,
    src.created_at
FROM (
    SELECT DISTINCT ON (e.id)
        e.id                    AS evidence_id,
        e.normative_indicator_id,
        ev.content_hash,
        ev.created_at,
        l1.process_id
    FROM evidence e
    JOIN evidence_version ev ON ev.evidence_id = e.id
    JOIN indicators i ON i.id = e.normative_indicator_id
    JOIN level3_nodes l3 ON l3.id = i.level3_id
    JOIN level2_nodes l2 ON l2.id = l3.level2_id
    JOIN level1_nodes l1 ON l1.id = l2.level1_id
    WHERE e.normative_indicator_id IS NOT NULL
    ORDER BY e.id, ev.version_number DESC
) src
JOIN document_assets da
    ON da.process_id = src.process_id
   AND da.content_hash = src.content_hash
WHERE NOT EXISTS (
    SELECT 1
    FROM indicator_evidence_links iel
    WHERE iel.indicator_id = src.normative_indicator_id
      AND iel.document_asset_id = da.id
);
