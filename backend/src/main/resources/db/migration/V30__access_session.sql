-- Historial de ingresos. La bitácora audit_log sigue siendo el registro de acciones.

CREATE TABLE IF NOT EXISTS access_session (
    id UUID PRIMARY KEY,
    user_id UUID,
    full_name VARCHAR(255),
    email VARCHAR(255) NOT NULL,
    role VARCHAR(50),
    started_at TIMESTAMP NOT NULL,
    ended_at TIMESTAMP,
    end_reason VARCHAR(32),
    ip_address VARCHAR(64),
    user_agent VARCHAR(512),
    status VARCHAR(16) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_access_session_started ON access_session (started_at DESC);
CREATE INDEX IF NOT EXISTS idx_access_session_email ON access_session (email);
CREATE INDEX IF NOT EXISTS idx_access_session_status ON access_session (status);
CREATE INDEX IF NOT EXISTS idx_access_session_user ON access_session (user_id);

ALTER TABLE audit_log ADD COLUMN IF NOT EXISTS session_id UUID;
CREATE INDEX IF NOT EXISTS idx_audit_log_session ON audit_log (session_id);
