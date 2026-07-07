-- MOD-DASH & MOD-REPORT DDL Migration (DD-UC-011 / PR-IMPL-011)

CREATE TABLE IF NOT EXISTS tb_program_dashboard_summary (
    program_id UUID PRIMARY KEY,
    program_name VARCHAR(255) NOT NULL,
    total_indicators INT NOT NULL DEFAULT 0,
    overall_progress_percentage DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    approved_evidences INT NOT NULL DEFAULT 0,
    rejected_evidences INT NOT NULL DEFAULT 0,
    pending_observations INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tb_program_phase_summary (
    id UUID PRIMARY KEY,
    program_id UUID NOT NULL,
    phase_id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    percentage DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    status VARCHAR(50) NOT NULL,
    CONSTRAINT fk_phase_summary_program FOREIGN KEY (program_id) REFERENCES tb_program_dashboard_summary(program_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS tb_report_export_job (
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

CREATE TABLE IF NOT EXISTS tb_observation (
    observation_id VARCHAR(100) PRIMARY KEY,
    program_id UUID NOT NULL,
    indicator_id VARCHAR(100) NOT NULL,
    indicator_code VARCHAR(100) NOT NULL,
    indicator_title VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    phase_id INT,
    status VARCHAR(50) NOT NULL,
    remediation_url VARCHAR(512)
);

CREATE INDEX IF NOT EXISTS idx_obs_program_estado_deadline
    ON tb_observation (program_id, status, due_date ASC);
