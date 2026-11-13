-- V31: Dual-channel notifications (EMAIL outbox + in-app) — DD-UC-038 / PR-IMPL-053 / FSD-UC-015

ALTER TABLE notification_outbox
    ADD COLUMN IF NOT EXISTS recipient_role VARCHAR(20),
    ADD COLUMN IF NOT EXISTS program_id UUID,
    ADD COLUMN IF NOT EXISTS process_id UUID,
    ADD COLUMN IF NOT EXISTS channel VARCHAR(20) NOT NULL DEFAULT 'EMAIL',
    ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(200),
    ADD COLUMN IF NOT EXISTS payload_json TEXT,
    ADD COLUMN IF NOT EXISTS error_detail VARCHAR(500);

-- Backfill idempotency for legacy rows so UNIQUE can apply
UPDATE notification_outbox
SET idempotency_key = CONCAT(COALESCE(event_type, 'UNKNOWN'), ':', id::text, ':LEGACY:EMAIL')
WHERE idempotency_key IS NULL;

ALTER TABLE notification_outbox
    ALTER COLUMN idempotency_key SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_notification_outbox_idempotency
    ON notification_outbox (idempotency_key);

CREATE INDEX IF NOT EXISTS idx_outbox_program ON notification_outbox (program_id);
CREATE INDEX IF NOT EXISTS idx_outbox_process ON notification_outbox (process_id);

CREATE TABLE IF NOT EXISTS in_app_notification (
    id UUID PRIMARY KEY,
    outbox_id UUID,
    user_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    title VARCHAR(255) NOT NULL,
    body VARCHAR(500) NOT NULL,
    deep_link VARCHAR(500),
    program_id UUID,
    process_id UUID,
    read_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_in_app_user_unread
    ON in_app_notification (user_id, read_at, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_in_app_user_created
    ON in_app_notification (user_id, created_at DESC);
