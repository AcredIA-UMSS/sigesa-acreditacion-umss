-- MOD-DASH & MOD-REPORT DDL Migration (DD-UC-011 / PR-IMPL-011)

-- Alter core observation table to add additional fields needed for dashboard queries
ALTER TABLE observation ADD COLUMN IF NOT EXISTS program_id UUID;
ALTER TABLE observation ADD COLUMN IF NOT EXISTS indicator_id VARCHAR(100);
ALTER TABLE observation ADD COLUMN IF NOT EXISTS indicator_code VARCHAR(100);
ALTER TABLE observation ADD COLUMN IF NOT EXISTS indicator_title VARCHAR(255);
ALTER TABLE observation ADD COLUMN IF NOT EXISTS due_date DATE;
ALTER TABLE observation ADD COLUMN IF NOT EXISTS status VARCHAR(50);
ALTER TABLE observation ADD COLUMN IF NOT EXISTS remediation_url VARCHAR(512);

-- Create index for observation table
CREATE INDEX IF NOT EXISTS idx_obs_program_estado_deadline
    ON observation (program_id, status, due_date ASC);

-- Create report_export_job table (without tb_ prefix)
CREATE TABLE IF NOT EXISTS report_export_job (
    job_id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    program_id UUID NOT NULL,
    format VARCHAR(20) NOT NULL,
    phase_id INT,
    status VARCHAR(50) NOT NULL,
    progress_percentage INT NOT NULL DEFAULT 0,
    file_path VARCHAR(512),
    error_message VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
