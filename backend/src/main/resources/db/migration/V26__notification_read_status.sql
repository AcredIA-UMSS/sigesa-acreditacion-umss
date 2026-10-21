-- Migration V26: Add read_at timestamp to notification_outbox for in-app notification center (FSD-UC-015 / PRD-US-017 / PRD-US-018)

ALTER TABLE notification_outbox ADD COLUMN IF NOT EXISTS read_at TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_outbox_recipient_read ON notification_outbox(recipient_id, read_at);
CREATE INDEX IF NOT EXISTS idx_outbox_recipient_email_read ON notification_outbox(recipient_email, read_at);
