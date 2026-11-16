-- V27: snapshot público de acreditación (FSD-UC-016 / FSD-BR-10).
-- Sin columnas de observaciones internas. PDF de certificado no se persiste.

CREATE TABLE IF NOT EXISTS publication_snapshot (
    id                      UUID PRIMARY KEY,
    slug                    VARCHAR(64)  NOT NULL,
    program_name            VARCHAR(255) NOT NULL,
    accreditation_status    VARCHAR(32)  NOT NULL,
    valid_from              DATE         NOT NULL,
    valid_until             DATE         NOT NULL,
    published               BOOLEAN      NOT NULL DEFAULT FALSE,
    certificate_published   BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_publication_snapshot_slug UNIQUE (slug)
);

CREATE INDEX IF NOT EXISTS idx_publication_snapshot_published
    ON publication_snapshot (published);
