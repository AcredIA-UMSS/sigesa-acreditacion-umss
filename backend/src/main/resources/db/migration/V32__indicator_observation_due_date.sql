-- PR-IMPL-053 / BR-NOTIFY-001: due dates for DeadlineApproaching on real indicator observations
ALTER TABLE indicator_observation
    ADD COLUMN IF NOT EXISTS due_date DATE;

CREATE INDEX IF NOT EXISTS idx_indicator_observation_due_date
    ON indicator_observation (status, due_date);

-- Backfill OPEN observations without due_date: created_at + 3 days (default window)
UPDATE indicator_observation
SET due_date = (created_at::date + INTERVAL '3 days')
WHERE status = 'OPEN' AND due_date IS NULL;
