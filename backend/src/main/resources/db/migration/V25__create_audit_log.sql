-- Migration V24: Create immutable append-only audit log table (FSD-UC-017 / FEAT-07)

CREATE TABLE IF NOT EXISTS audit_log (
    id UUID PRIMARY KEY,
    actor_id UUID,
    actor_email VARCHAR(255) NOT NULL,
    actor_role VARCHAR(50),
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id VARCHAR(255) NOT NULL,
    payload_diff TEXT,
    ip_address VARCHAR(50),
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_audit_log_created_at ON audit_log(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_log_actor ON audit_log(actor_email);
CREATE INDEX IF NOT EXISTS idx_audit_log_entity ON audit_log(entity_type, entity_id);
