CREATE TABLE process_responsible_assignment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    process_id UUID NOT NULL REFERENCES accreditation_processes(id),
    user_id UUID NOT NULL REFERENCES app_user(id),
    assigned_by UUID NOT NULL REFERENCES app_user(id),
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    revoked_at TIMESTAMPTZ NULL
);

CREATE UNIQUE INDEX uk_pra_active_process
    ON process_responsible_assignment (process_id)
    WHERE revoked_at IS NULL;

CREATE UNIQUE INDEX uk_pra_active_user
    ON process_responsible_assignment (user_id)
    WHERE revoked_at IS NULL;

CREATE INDEX idx_pra_user ON process_responsible_assignment (user_id);
CREATE INDEX idx_pra_process ON process_responsible_assignment (process_id);
