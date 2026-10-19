-- Migration V23: Create notification outbox table for transactional decoupled alerts (FSD-UC-015 / FEAT-05)

CREATE TABLE IF NOT EXISTS notification_outbox (
    id UUID PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    recipient_id UUID,
    recipient_email VARCHAR(255),
    subject VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    deep_link VARCHAR(500),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    max_retries INT NOT NULL DEFAULT 5,
    next_retry_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    sent_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_outbox_poll ON notification_outbox(status, next_retry_at);
CREATE INDEX IF NOT EXISTS idx_outbox_created_at ON notification_outbox(created_at);
