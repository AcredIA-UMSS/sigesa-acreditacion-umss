-- FSD-UC-040 agenda de visita de pares. H2 de tests usa ddl-auto.

CREATE TABLE IF NOT EXISTS peer_visit (
    id           UUID PRIMARY KEY,
    process_id   UUID NOT NULL UNIQUE REFERENCES accreditation_processes(id) ON DELETE CASCADE,
    starts_on    DATE,
    ends_on      DATE,
    venue        VARCHAR(255),
    room         VARCHAR(128),
    status       VARCHAR(24) NOT NULL,
    notes        TEXT,
    created_by   UUID,
    created_at   TIMESTAMP NOT NULL,
    updated_at   TIMESTAMP NOT NULL,
    CONSTRAINT chk_peer_visit_status CHECK (status IN ('DRAFT', 'SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'))
);

CREATE TABLE IF NOT EXISTS peer_visit_item (
    id            UUID PRIMARY KEY,
    visit_id      UUID NOT NULL REFERENCES peer_visit(id) ON DELETE CASCADE,
    item_type     VARCHAR(24) NOT NULL,
    title         VARCHAR(255) NOT NULL,
    scheduled_at  TIMESTAMP,
    location      VARCHAR(255),
    notes         TEXT,
    done          BOOLEAN NOT NULL DEFAULT FALSE,
    item_order    INTEGER NOT NULL,
    CONSTRAINT chk_peer_visit_item_type CHECK (item_type IN ('INTERVIEW', 'FACILITY', 'DOCUMENT', 'SESSION'))
);

CREATE INDEX IF NOT EXISTS idx_peer_visit_item_visit ON peer_visit_item (visit_id);
