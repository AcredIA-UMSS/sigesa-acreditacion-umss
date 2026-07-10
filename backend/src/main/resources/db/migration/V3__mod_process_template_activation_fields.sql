-- MOD-PROCESS: campos de activación de plantilla (DD-UC-003 / API-PROC-02)

ALTER TABLE template ADD COLUMN IF NOT EXISTS active_period VARCHAR(20);
ALTER TABLE template ADD COLUMN IF NOT EXISTS activated_at TIMESTAMP;
