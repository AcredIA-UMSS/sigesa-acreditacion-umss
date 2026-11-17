-- Optimización de consulta ListProcessesService (filtros de carrera, estado y orden de inicio)
CREATE INDEX IF NOT EXISTS idx_accreditation_processes_career_status
    ON accreditation_processes (career_id, status);

CREATE INDEX IF NOT EXISTS idx_accreditation_processes_status_start_date
    ON accreditation_processes (status, start_date DESC);
