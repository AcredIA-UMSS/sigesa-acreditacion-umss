-- Bitácora append-only (FSD-UC-017 / DD-UC-017).
-- El trigger rechaza UPDATE y DELETE para cualquier rol que no desactive el trigger.
-- No es inmutabilidad ante un superusuario.

CREATE TABLE audit_log (
    id UUID PRIMARY KEY,
    actor_id UUID NULL,
    actor_email VARCHAR(150) NULL,
    action VARCHAR(40) NOT NULL,
    resource_type VARCHAR(30) NOT NULL,
    resource_id UUID NULL,
    description VARCHAR(500) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    result VARCHAR(10) NOT NULL,
    ip_address VARCHAR(45) NULL,
    payload TEXT NULL
);

CREATE INDEX idx_audit_log_occurred_at ON audit_log (occurred_at);
CREATE INDEX idx_audit_log_actor_id ON audit_log (actor_id);
CREATE INDEX idx_audit_log_action ON audit_log (action);
CREATE INDEX idx_audit_log_resource ON audit_log (resource_type, resource_id);

CREATE OR REPLACE FUNCTION audit_log_reject_mutation()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    RAISE EXCEPTION 'audit_log is append-only';
END;
$$;

CREATE TRIGGER trg_audit_log_no_update
    BEFORE UPDATE ON audit_log
    FOR EACH ROW
    EXECUTE FUNCTION audit_log_reject_mutation();

CREATE TRIGGER trg_audit_log_no_delete
    BEFORE DELETE ON audit_log
    FOR EACH ROW
    EXECUTE FUNCTION audit_log_reject_mutation();
