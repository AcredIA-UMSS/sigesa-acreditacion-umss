-- FEAT-02 / GitHub #51 — lock de upload atómico (F12-03). Visible a N réplicas.
-- H2 de tests no usa Flyway (ddl-auto); Hibernate mapea EvidenceUploadLockEntity.

CREATE TABLE IF NOT EXISTS evidence_upload_lock (
    indicator_id UUID PRIMARY KEY,
    acquired_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
