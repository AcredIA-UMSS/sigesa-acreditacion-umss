-- Seed SQL file for SIGESA (PostgreSQL)
-- NOTA: No inserta usuarios en 'app_user' ni 'user_program_assignment' (ya existen en BD).
-- Genera datos maestros (5 registros por tabla) y tablas transaccionales (≥ 20 registros con FKs válidas).

-- 1. ESTRUCTURA DE TABLAS (CREATE TABLE IF NOT EXISTS)
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE IF NOT EXISTS programs (
    id UUID PRIMARY KEY,
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    faculty VARCHAR(128),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS evaluation_dimension (
    id UUID PRIMARY KEY,
    template_id UUID NOT NULL,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS evaluation_criterion (
    id UUID PRIMARY KEY,
    dimension_id UUID NOT NULL,
    code VARCHAR(64) NOT NULL,
    description TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS evidence (
    id UUID PRIMARY KEY,
    indicator_id UUID NOT NULL UNIQUE,
    latest_version_id UUID,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS evidence_version (
    id UUID PRIMARY KEY,
    evidence_id UUID NOT NULL,
    version_number INTEGER NOT NULL,
    content_hash VARCHAR(64) NOT NULL,
    criterion_id UUID NOT NULL,
    description VARCHAR(2000) NOT NULL,
    storage_key VARCHAR(500) NOT NULL,
    created_by UUID NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_programs_name_trgm ON programs USING gin (name gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_evaluation_dimension_name_trgm ON evaluation_dimension USING gin (name gin_trgm_ops);

CREATE TABLE IF NOT EXISTS accreditation_processes (
    id UUID PRIMARY KEY,
    career_id UUID NOT NULL,
    template_id UUID NOT NULL,
    status VARCHAR(255) NOT NULL,
    start_date TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS phases (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phase_order INTEGER NOT NULL,
    process_id UUID NOT NULL REFERENCES accreditation_processes(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS indicator (
    id UUID PRIMARY KEY,
    program_id UUID NOT NULL,
    criterion_id UUID NOT NULL,
    phase_id UUID REFERENCES phases(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS indicator_state_history (
    id UUID PRIMARY KEY,
    indicator_id UUID NOT NULL REFERENCES indicator(id) ON DELETE CASCADE,
    previous_state VARCHAR(20) NOT NULL,
    new_state VARCHAR(20) NOT NULL,
    actor_id UUID NOT NULL,
    actor_role VARCHAR(10) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS tb_observation (
    observation_id VARCHAR(255) PRIMARY KEY,
    program_id UUID NOT NULL,
    indicator_id VARCHAR(255) NOT NULL,
    indicator_code VARCHAR(255) NOT NULL,
    indicator_title VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    phase_id INTEGER,
    status VARCHAR(255) NOT NULL,
    remediation_url VARCHAR(512)
);

CREATE TABLE IF NOT EXISTS tb_program_dashboard_summary (
    program_id UUID PRIMARY KEY,
    program_name VARCHAR(255) NOT NULL,
    total_indicators INTEGER NOT NULL,
    overall_progress_percentage DOUBLE PRECISION NOT NULL,
    approved_evidences INTEGER NOT NULL,
    rejected_evidences INTEGER NOT NULL,
    pending_observations INTEGER NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS tb_program_phase_summary (
    id UUID PRIMARY KEY,
    program_id UUID NOT NULL REFERENCES tb_program_dashboard_summary(program_id) ON DELETE CASCADE,
    phase_id INTEGER NOT NULL,
    name VARCHAR(255) NOT NULL,
    percentage DOUBLE PRECISION NOT NULL,
    status VARCHAR(255) NOT NULL
);


-- 2. DATOS MAESTROS (5 REGISTROS POR TABLA)

-- 2.1 Procesos de Acreditación (5 registros)
INSERT INTO accreditation_processes (id, career_id, template_id, status, start_date) VALUES
('950e8400-e29b-41d4-a716-446655440020', '550e8400-e29b-41d4-a716-446655440000', '850e8400-e29b-41d4-a716-446655440010', 'ACTIVE', NOW() - INTERVAL '60 days'),
('950e8400-e29b-41d4-a716-446655440021', '660e8400-e29b-41d4-a716-446655440001', '850e8400-e29b-41d4-a716-446655440010', 'ACTIVE', NOW() - INTERVAL '45 days'),
('950e8400-e29b-41d4-a716-446655440022', '770e8400-e29b-41d4-a716-446655440002', '850e8400-e29b-41d4-a716-446655440011', 'ACTIVE', NOW() - INTERVAL '30 days'),
('950e8400-e29b-41d4-a716-446655440023', '550e8400-e29b-41d4-a716-446655440000', '850e8400-e29b-41d4-a716-446655440011', 'CLOSED', NOW() - INTERVAL '365 days'),
('950e8400-e29b-41d4-a716-446655440024', '660e8400-e29b-41d4-a716-446655440001', '850e8400-e29b-41d4-a716-446655440012', 'ARCHIVED', NOW() - INTERVAL '700 days')
ON CONFLICT (id) DO NOTHING;

-- 2.2 Fases (5 registros vinculados a los procesos)
INSERT INTO phases (id, name, phase_order, process_id) VALUES
('550e8400-e29b-41d4-a716-446655440004', 'Fase 1: Autoevaluación Inicial', 1, '950e8400-e29b-41d4-a716-446655440020'),
('550e8400-e29b-41d4-a716-446655440005', 'Fase 2: Verificación de Evidencias', 2, '950e8400-e29b-41d4-a716-446655440020'),
('550e8400-e29b-41d4-a716-446655440006', 'Fase 3: Dictamen de Pares Evaluadores', 3, '950e8400-e29b-41d4-a716-446655440020'),
('660e8400-e29b-41d4-a716-446655440004', 'Fase 1: Planificación CEUB', 1, '950e8400-e29b-41d4-a716-446655440021'),
('770e8400-e29b-41d4-a716-446655440004', 'Fase 1: Preparación ARCU-SUR', 1, '950e8400-e29b-41d4-a716-446655440022')
ON CONFLICT (id) DO NOTHING;

-- 2.3 Resúmenes de Dashboard de Programas (5 registros)
INSERT INTO tb_program_dashboard_summary (program_id, program_name, total_indicators, overall_progress_percentage, approved_evidences, rejected_evidences, pending_observations, updated_at) VALUES
('550e8400-e29b-41d4-a716-446655440000', 'Ingeniería de Sistemas', 20, 45.0, 9, 3, 5, NOW()),
('660e8400-e29b-41d4-a716-446655440001', 'Coordinación CEUB (demo)', 40, 80.0, 32, 2, 2, NOW()),
('770e8400-e29b-41d4-a716-446655440002', 'Coordinación ARCU-SUR (demo)', 50, 35.0, 17, 10, 14, NOW()),
('880e8400-e29b-41d4-a716-446655440003', 'Ingeniería Industrial (demo)', 30, 90.0, 27, 1, 0, NOW()),
('990e8400-e29b-41d4-a716-446655440004', 'Licenciatura en Informática', 25, 60.0, 15, 4, 3, NOW())
ON CONFLICT (program_id) DO NOTHING;

-- 2.4 Resúmenes de Fases por Programa (5 registros)
INSERT INTO tb_program_phase_summary (id, program_id, phase_id, name, percentage, status) VALUES
('550e8400-e29b-41d4-a716-446655440101', '550e8400-e29b-41d4-a716-446655440000', 1, 'Fase 1: Autoevaluación Inicial', 70.0, 'EN_PROCESO'),
('550e8400-e29b-41d4-a716-446655440102', '550e8400-e29b-41d4-a716-446655440000', 2, 'Fase 2: Verificación de Evidencias', 20.0, 'EN_PROCESO'),
('660e8400-e29b-41d4-a716-446655440101', '660e8400-e29b-41d4-a716-446655440001', 1, 'Fase 1: Planificación CEUB', 100.0, 'COMPLETED'),
('770e8400-e29b-41d4-a716-446655440101', '770e8400-e29b-41d4-a716-446655440002', 1, 'Fase 1: Preparación ARCU-SUR', 35.0, 'EN_PROCESO'),
('880e8400-e29b-41d4-a716-446655440101', '880e8400-e29b-41d4-a716-446655440003', 1, 'Fase 1: Autoevaluación Inicial', 90.0, 'EN_PROCESO')
ON CONFLICT (id) DO NOTHING;


-- 3. TABLAS TRANSACCIONALES (≥ 20 REGISTROS CON CLAVES FORÁNEAS)

-- 3.1 Indicadores (20 registros transaccionales)
INSERT INTO indicator (id, program_id, criterion_id, phase_id) VALUES
-- 10 Indicadores para Sistemas (Fase 1 y 2)
('a10e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440000', '550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440004'),
('a10e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440000', '550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440004'),
('a10e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440000', '550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440004'),
('a10e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440000', '550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440004'),
('a10e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440000', '550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440004'),
('a10e8400-e29b-41d4-a716-446655440006', '550e8400-e29b-41d4-a716-446655440000', '550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440005'),
('a10e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440000', '550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440005'),
('a10e8400-e29b-41d4-a716-446655440008', '550e8400-e29b-41d4-a716-446655440000', '550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440005'),
('a10e8400-e29b-41d4-a716-446655440009', '550e8400-e29b-41d4-a716-446655440000', '550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440005'),
('a10e8400-e29b-41d4-a716-446655440010', '550e8400-e29b-41d4-a716-446655440000', '550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440005'),
-- 5 Indicadores para CEUB
('b10e8400-e29b-41d4-a716-446655440011', '660e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', '660e8400-e29b-41d4-a716-446655440004'),
('b10e8400-e29b-41d4-a716-446655440012', '660e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', '660e8400-e29b-41d4-a716-446655440004'),
('b10e8400-e29b-41d4-a716-446655440013', '660e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', '660e8400-e29b-41d4-a716-446655440004'),
('b10e8400-e29b-41d4-a716-446655440014', '660e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', '660e8400-e29b-41d4-a716-446655440004'),
('b10e8400-e29b-41d4-a716-446655440015', '660e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', '660e8400-e29b-41d4-a716-446655440004'),
-- 5 Indicadores para ARCU-SUR
('c10e8400-e29b-41d4-a716-446655440016', '770e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440002', '770e8400-e29b-41d4-a716-446655440004'),
('c10e8400-e29b-41d4-a716-446655440017', '770e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440002', '770e8400-e29b-41d4-a716-446655440004'),
('c10e8400-e29b-41d4-a716-446655440018', '770e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440002', '770e8400-e29b-41d4-a716-446655440004'),
('c10e8400-e29b-41d4-a716-446655440019', '770e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440002', '770e8400-e29b-41d4-a716-446655440004'),
('c10e8400-e29b-41d4-a716-446655440020', '770e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440002', '770e8400-e29b-41d4-a716-446655440004')
ON CONFLICT (id) DO NOTHING;

-- 3.2 Historial de Estados de Indicadores (20 registros transaccionales)
INSERT INTO indicator_state_history (id, indicator_id, previous_state, new_state, actor_id, actor_role, created_at) VALUES
('d10e8400-e29b-41d4-a716-446655440001', 'a10e8400-e29b-41d4-a716-446655440001', 'PENDIENTE', 'APROBADO', 'b27e852d-9a3d-4c3e-9081-37f225d30002', 'TD', NOW() - INTERVAL '20 days'),
('d10e8400-e29b-41d4-a716-446655440002', 'a10e8400-e29b-41d4-a716-446655440002', 'PENDIENTE', 'APROBADO', 'b27e852d-9a3d-4c3e-9081-37f225d30002', 'TD', NOW() - INTERVAL '18 days'),
('d10e8400-e29b-41d4-a716-446655440003', 'a10e8400-e29b-41d4-a716-446655440003', 'PENDIENTE', 'APROBADO', 'b27e852d-9a3d-4c3e-9081-37f225d30002', 'TD', NOW() - INTERVAL '15 days'),
('d10e8400-e29b-41d4-a716-446655440004', 'a10e8400-e29b-41d4-a716-446655440004', 'PENDIENTE', 'OBSERVADO', 'b27e852d-9a3d-4c3e-9081-37f225d30002', 'TD', NOW() - INTERVAL '12 days'),
('d10e8400-e29b-41d4-a716-446655440005', 'a10e8400-e29b-41d4-a716-446655440005', 'PENDIENTE', 'OBSERVADO', 'b27e852d-9a3d-4c3e-9081-37f225d30002', 'TD', NOW() - INTERVAL '10 days'),
('d10e8400-e29b-41d4-a716-446655440006', 'a10e8400-e29b-41d4-a716-446655440006', 'PENDIENTE', 'APROBADO', 'b27e852d-9a3d-4c3e-9081-37f225d30002', 'TD', NOW() - INTERVAL '8 days'),
('d10e8400-e29b-41d4-a716-446655440007', 'a10e8400-e29b-41d4-a716-446655440007', 'PENDIENTE', 'OBSERVADO', 'b27e852d-9a3d-4c3e-9081-37f225d30002', 'TD', NOW() - INTERVAL '5 days'),
('d10e8400-e29b-41d4-a716-446655440008', 'a10e8400-e29b-41d4-a716-446655440008', 'PENDIENTE', 'PENDIENTE', 'b27e852d-9a3d-4c3e-9081-37f225d30002', 'CC', NOW() - INTERVAL '4 days'),
('d10e8400-e29b-41d4-a716-446655440009', 'a10e8400-e29b-41d4-a716-446655440009', 'PENDIENTE', 'PENDIENTE', 'b27e852d-9a3d-4c3e-9081-37f225d30002', 'CC', NOW() - INTERVAL '3 days'),
('d10e8400-e29b-41d4-a716-446655440010', 'a10e8400-e29b-41d4-a716-446655440010', 'PENDIENTE', 'PENDIENTE', 'b27e852d-9a3d-4c3e-9081-37f225d30002', 'CC', NOW() - INTERVAL '1 days'),

('d10e8400-e29b-41d4-a716-446655440011', 'b10e8400-e29b-41d4-a716-446655440011', 'PENDIENTE', 'APROBADO', 'b27e852d-9a3d-4c3e-9081-37f225d30002', 'TD', NOW() - INTERVAL '15 days'),
('d10e8400-e29b-41d4-a716-446655440012', 'b10e8400-e29b-41d4-a716-446655440012', 'PENDIENTE', 'APROBADO', 'b27e852d-9a3d-4c3e-9081-37f225d30002', 'TD', NOW() - INTERVAL '14 days'),
('d10e8400-e29b-41d4-a716-446655440013', 'b10e8400-e29b-41d4-a716-446655440013', 'PENDIENTE', 'APROBADO', 'b27e852d-9a3d-4c3e-9081-37f225d30002', 'TD', NOW() - INTERVAL '10 days'),
('d10e8400-e29b-41d4-a716-446655440014', 'b10e8400-e29b-41d4-a716-446655440014', 'PENDIENTE', 'APROBADO', 'b27e852d-9a3d-4c3e-9081-37f225d30002', 'TD', NOW() - INTERVAL '8 days'),
('d10e8400-e29b-41d4-a716-446655440015', 'b10e8400-e29b-41d4-a716-446655440015', 'PENDIENTE', 'OBSERVADO', 'b27e852d-9a3d-4c3e-9081-37f225d30002', 'TD', NOW() - INTERVAL '2 days'),

('d10e8400-e29b-41d4-a716-446655440016', 'c10e8400-e29b-41d4-a716-446655440016', 'PENDIENTE', 'APROBADO', 'b27e852d-9a3d-4c3e-9081-37f225d30002', 'TD', NOW() - INTERVAL '25 days'),
('d10e8400-e29b-41d4-a716-446655440017', 'c10e8400-e29b-41d4-a716-446655440017', 'PENDIENTE', 'OBSERVADO', 'b27e852d-9a3d-4c3e-9081-37f225d30002', 'TD', NOW() - INTERVAL '22 days'),
('d10e8400-e29b-41d4-a716-446655440018', 'c10e8400-e29b-41d4-a716-446655440018', 'PENDIENTE', 'OBSERVADO', 'b27e852d-9a3d-4c3e-9081-37f225d30002', 'TD', NOW() - INTERVAL '19 days'),
('d10e8400-e29b-41d4-a716-446655440019', 'c10e8400-e29b-41d4-a716-446655440019', 'PENDIENTE', 'OBSERVADO', 'b27e852d-9a3d-4c3e-9081-37f225d30002', 'TD', NOW() - INTERVAL '12 days'),
('d10e8400-e29b-41d4-a716-446655440020', 'c10e8400-e29b-41d4-a716-446655440020', 'PENDIENTE', 'PENDIENTE', 'b27e852d-9a3d-4c3e-9081-37f225d30002', 'CC', NOW() - INTERVAL '5 days')
ON CONFLICT (id) DO NOTHING;

-- 3.3 Observaciones (20 registros transaccionales)
INSERT INTO tb_observation (observation_id, program_id, indicator_id, indicator_code, indicator_title, description, issue_date, due_date, phase_id, status, remediation_url) VALUES
('OBS-2026-001', '550e8400-e29b-41d4-a716-446655440000', 'a10e8400-e29b-41d4-a716-446655440004', 'IND-1.1.1', 'Misión y Visión Institucional', 'Falta adjuntar resolución del HCF firmada.', NOW() - INTERVAL '15 days', NOW() + INTERVAL '5 days', 1, 'PENDIENTE_SUBSANACION', '/coordinator/evidences/IND-101/subsanar'),
('OBS-2026-002', '550e8400-e29b-41d4-a716-446655440000', 'a10e8400-e29b-41d4-a716-446655440005', 'IND-1.1.2', 'Objetivos del Plan de Estudios', 'Los objetivos no concuerdan con el perfil profesional vigente.', NOW() - INTERVAL '14 days', NOW() + INTERVAL '3 days', 1, 'PENDIENTE_SUBSANACION', '/coordinator/evidences/IND-102/subsanar'),
('OBS-2026-003', '550e8400-e29b-41d4-a716-446655440000', 'a10e8400-e29b-41d4-a716-446655440007', 'IND-2.1.1', 'Equipamiento de Laboratorios', 'Falta incluir el certificado de calibración de osciloscopios.', NOW() - INTERVAL '10 days', NOW() + INTERVAL '7 days', 2, 'PENDIENTE_SUBSANACION', '/coordinator/evidences/IND-103/subsanar'),
('OBS-2026-004', '550e8400-e29b-41d4-a716-446655440000', 'a10e8400-e29b-41d4-a716-446655440004', 'IND-1.1.1', 'Misión y Visión Institucional', 'Formato PDF ilegible en la página 3.', NOW() - INTERVAL '8 days', NOW() + INTERVAL '2 days', 1, 'EN_REVISION', '/coordinator/evidences/IND-101/subsanar'),
('OBS-2026-005', '550e8400-e29b-41d4-a716-446655440000', 'a10e8400-e29b-41d4-a716-446655440007', 'IND-2.1.1', 'Equipamiento de Laboratorios', 'Falta firma del jefe de laboratorios.', NOW() - INTERVAL '6 days', NOW() + INTERVAL '10 days', 2, 'EN_REVISION', '/coordinator/evidences/IND-103/subsanar'),
('OBS-2026-006', '550e8400-e29b-41d4-a716-446655440000', 'a10e8400-e29b-41d4-a716-446655440001', 'IND-1.2.1', 'Perfil de Egreso Evaluado', 'Completado y validado en revisión técnica.', NOW() - INTERVAL '25 days', NOW() - INTERVAL '5 days', 1, 'APROBADO', '/coordinator/evidences/IND-104/subsanar'),
('OBS-2026-007', '550e8400-e29b-41d4-a716-446655440000', 'a10e8400-e29b-41d4-a716-446655440002', 'IND-1.2.2', 'Estructura Curricular', 'Validación aprobada por DUEA.', NOW() - INTERVAL '22 days', NOW() - INTERVAL '2 days', 1, 'APROBADO', '/coordinator/evidences/IND-105/subsanar'),
('OBS-2026-008', '550e8400-e29b-41d4-a716-446655440000', 'a10e8400-e29b-41d4-a716-446655440003', 'IND-1.3.1', 'Carga Horaria Docente', 'Certificado validado.', NOW() - INTERVAL '20 days', NOW() - INTERVAL '1 days', 1, 'APROBADO', '/coordinator/evidences/IND-106/subsanar'),

('OBS-2026-009', '660e8400-e29b-41d4-a716-446655440001', 'b10e8400-e29b-41d4-a716-446655440015', 'IND-CEUB-01', 'Convenios Internacionales', 'Se solicita actualizar anexo del convenio marco.', NOW() - INTERVAL '12 days', NOW() + INTERVAL '8 days', 1, 'PENDIENTE_SUBSANACION', '/coordinator/evidences/IND-201/subsanar'),
('OBS-2026-010', '660e8400-e29b-41d4-a716-446655440001', 'b10e8400-e29b-41d4-a716-446655440015', 'IND-CEUB-01', 'Convenios Internacionales', 'Copia legalizada requerida.', NOW() - INTERVAL '5 days', NOW() + INTERVAL '12 days', 1, 'EN_REVISION', '/coordinator/evidences/IND-201/subsanar'),
('OBS-2026-011', '660e8400-e29b-41d4-a716-446655440001', 'b10e8400-e29b-41d4-a716-446655440011', 'IND-CEUB-02', 'Presupuesto Asignado', 'Aprobación presupuestaria verificada.', NOW() - INTERVAL '30 days', NOW() - INTERVAL '10 days', 1, 'APROBADO', '/coordinator/evidences/IND-202/subsanar'),
('OBS-2026-012', '660e8400-e29b-41d4-a716-446655440001', 'b10e8400-e29b-41d4-a716-446655440012', 'IND-CEUB-03', 'Infraestructura General', 'Inspección aprobada.', NOW() - INTERVAL '28 days', NOW() - INTERVAL '8 days', 1, 'APROBADO', '/coordinator/evidences/IND-203/subsanar'),

('OBS-2026-013', '770e8400-e29b-41d4-a716-446655440002', 'c10e8400-e29b-41d4-a716-446655440017', 'IND-ARC-01', 'Investigación Aplicada', 'No se presentó informe de avance de proyectos.', NOW() - INTERVAL '18 days', NOW() + INTERVAL '1 days', 1, 'PENDIENTE_SUBSANACION', '/coordinator/evidences/IND-301/subsanar'),
('OBS-2026-014', '770e8400-e29b-41d4-a716-446655440002', 'c10e8400-e29b-41d4-a716-446655440018', 'IND-ARC-02', 'Publicaciones Indexadas', 'Falta incluir enlaces DOI de artículos.', NOW() - INTERVAL '16 days', NOW() + INTERVAL '4 days', 1, 'PENDIENTE_SUBSANACION', '/coordinator/evidences/IND-302/subsanar'),
('OBS-2026-015', '770e8400-e29b-41d4-a716-446655440002', 'c10e8400-e29b-41d4-a716-446655440019', 'IND-ARC-03', 'Movilidad Docente', 'Certificados de movilidad sin sello de facultad.', NOW() - INTERVAL '14 days', NOW() + INTERVAL '6 days', 1, 'PENDIENTE_SUBSANACION', '/coordinator/evidences/IND-303/subsanar'),
('OBS-2026-016', '770e8400-e29b-41d4-a716-446655440002', 'c10e8400-e29b-41d4-a716-446655440017', 'IND-ARC-01', 'Investigación Aplicada', 'Revisión en progreso.', NOW() - INTERVAL '7 days', NOW() + INTERVAL '9 days', 1, 'EN_REVISION', '/coordinator/evidences/IND-301/subsanar'),
('OBS-2026-017', '770e8400-e29b-41d4-a716-446655440002', 'c10e8400-e29b-41d4-a716-446655440016', 'IND-ARC-04', 'Acreditación Previa', 'Documentación histórica completa.', NOW() - INTERVAL '35 days', NOW() - INTERVAL '15 days', 1, 'APROBADO', '/coordinator/evidences/IND-304/subsanar'),

('OBS-2026-018', '880e8400-e29b-41d4-a716-446655440003', 'a10e8400-e29b-41d4-a716-446655440001', 'IND-IND-01', 'Planes de Seguridad Industrial', 'Validado por comisión técnica.', NOW() - INTERVAL '40 days', NOW() - INTERVAL '20 days', 1, 'APROBADO', '/coordinator/evidences/IND-401/subsanar'),
('OBS-2026-020', '990e8400-e29b-41d4-a716-446655440004', 'a10e8400-e29b-41d4-a716-446655440003', 'IND-INF-02', 'Servidores de Prácticas', 'Acceso SSH verificado correctamente.', NOW() - INTERVAL '20 days', NOW() - INTERVAL '5 days', 1, 'APROBADO', '/coordinator/evidences/IND-502/subsanar')
ON CONFLICT (observation_id) DO NOTHING;

-- 4. DATOS DE PROGRAMAS, DIMENSIONES Y EVIDENCIAS
-- Asegurar que existan los programas académicos utilizados
INSERT INTO programs (id, code, name, faculty, active) VALUES
('550e8400-e29b-41d4-a716-446655440000', 'INF-SIS', 'Ingeniería de Sistemas', 'FCT', TRUE),
('660e8400-e29b-41d4-a716-446655440001', 'CEUB-DEMO', 'Coordinación CEUB (demo)', 'Tecnología', TRUE),
('770e8400-e29b-41d4-a716-446655440002', 'ARCUSUR-DEMO', 'Coordinación ARCU-SUR (demo)', 'Tecnología', TRUE)
ON CONFLICT (id) DO NOTHING;

-- Asegurar que existan las dimensiones de evaluación y sus criterios
INSERT INTO evaluation_dimension (id, template_id, code, name) VALUES
('550e8400-e29b-41d4-a716-446655440001', '850e8400-e29b-41d4-a716-446655440010', 'DIM-INFRA', 'Infraestructura'),
('550e8400-e29b-41d4-a716-446655440005', '850e8400-e29b-41d4-a716-446655440010', 'DIM-CURR', 'Plan de Estudios'),
('550e8400-e29b-41d4-a716-446655440006', '850e8400-e29b-41d4-a716-446655440010', 'DIM-DOC', 'Docentes'),
('550e8400-e29b-41d4-a716-446655440007', '850e8400-e29b-41d4-a716-446655440010', 'DIM-ADM', 'Administración')
ON CONFLICT (id) DO NOTHING;

INSERT INTO evaluation_criterion (id, dimension_id, code, description) VALUES
('550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440001', 'CRT-04', 'Criterio de Infraestructura física'),
('550e8400-e29b-41d4-a716-446655440052', '550e8400-e29b-41d4-a716-446655440005', 'CRT-05', 'Criterio de Plan de Estudios y Currículo'),
('550e8400-e29b-41d4-a716-446655440062', '550e8400-e29b-41d4-a716-446655440006', 'CRT-06', 'Criterio de Planta Docente y Desarrollo'),
('550e8400-e29b-41d4-a716-446655440072', '550e8400-e29b-41d4-a716-446655440007', 'CRT-07', 'Criterio de Gestión y Administración')
ON CONFLICT (id) DO NOTHING;

-- Evidencias e Historial de Versiones (para MCP Multi-Token Búsqueda)
-- Evidencia 1 (Indicador: a10e8400-e29b-41d4-a716-446655440001)
INSERT INTO evidence (id, indicator_id, latest_version_id, created_at)
VALUES ('e11e8400-e29b-41d4-a716-446655440001', 'a10e8400-e29b-41d4-a716-446655440001', 'f11e8400-e29b-41d4-a716-446655440001', NOW() - INTERVAL '15 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO evidence_version (id, evidence_id, version_number, content_hash, criterion_id, description, storage_key, created_by, created_at)
VALUES (
    'f11e8400-e29b-41d4-a716-446655440001', 
    'e11e8400-e29b-41d4-a716-446655440001', 
    1, 
    'a6f9828e67a4d538e9a1b6c7d2f8e1a0b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8', 
    '550e8400-e29b-41d4-a716-446655440002', 
    'Planos aprobados y distribución de laboratorios de computación y aulas para Ingeniería de Sistemas.', 
    'planos_distribucion_aulas.pdf', 
    '17228eb7-02f3-4542-a754-a86edfb9299a', 
    NOW() - INTERVAL '15 days'
)
ON CONFLICT (id) DO NOTHING;

-- Evidencia 2 (Indicador: a10e8400-e29b-41d4-a716-446655440002)
INSERT INTO evidence (id, indicator_id, latest_version_id, created_at)
VALUES ('e11e8400-e29b-41d4-a716-446655440002', 'a10e8400-e29b-41d4-a716-446655440002', 'f11e8400-e29b-41d4-a716-446655440002', NOW() - INTERVAL '10 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO evidence_version (id, evidence_id, version_number, content_hash, criterion_id, description, storage_key, created_by, created_at)
VALUES (
    'f11e8400-e29b-41d4-a716-446655440002', 
    'e11e8400-e29b-41d4-a716-446655440002', 
    1, 
    'b7f9828e67a4d538e9a1b6c7d2f8e1a0b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e9', 
    '550e8400-e29b-41d4-a716-446655440002', 
    'Inventario valorado de activos fijos tecnológicos y equipamiento de aulas interactivas de Sistemas.', 
    'inventario_equipos_aulas.pdf', 
    '17228eb7-02f3-4542-a754-a86edfb9299a', 
    NOW() - INTERVAL '10 days'
)
ON CONFLICT (id) DO NOTHING;

-- Evidencia 3 (Indicador: a10e8400-e29b-41d4-a716-446655440003)
INSERT INTO evidence (id, indicator_id, latest_version_id, created_at)
VALUES ('e11e8400-e29b-41d4-a716-446655440003', 'a10e8400-e29b-41d4-a716-446655440003', 'f11e8400-e29b-41d4-a716-446655440003', NOW() - INTERVAL '8 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO evidence_version (id, evidence_id, version_number, content_hash, criterion_id, description, storage_key, created_by, created_at)
VALUES (
    'f11e8400-e29b-41d4-a716-446655440003', 
    'e11e8400-e29b-41d4-a716-446655440003', 
    1, 
    'c7f9828e67a4d538e9a1b6c7d2f8e1a0b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e2', 
    '550e8400-e29b-41d4-a716-446655440002', 
    'Carga horaria docente de Sistemas y actas de distribución académica semestral.', 
    'distribucion_horaria_docentes.pdf', 
    '17228eb7-02f3-4542-a754-a86edfb9299a', 
    NOW() - INTERVAL '8 days'
)
ON CONFLICT (id) DO NOTHING;

-- Evidencia 4 (Indicador: b10e8400-e29b-41d4-a716-446655440011)
INSERT INTO evidence (id, indicator_id, latest_version_id, created_at)
VALUES ('e11e8400-e29b-41d4-a716-446655440004', 'b10e8400-e29b-41d4-a716-446655440011', 'f11e8400-e29b-41d4-a716-446655440004', NOW() - INTERVAL '20 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO evidence_version (id, evidence_id, version_number, content_hash, criterion_id, description, storage_key, created_by, created_at)
VALUES (
    'f11e8400-e29b-41d4-a716-446655440004', 
    'e11e8400-e29b-41d4-a716-446655440004', 
    1, 
    'c7f9828e67a4d538e9a1b6c7d2f8e1a0b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e0', 
    '550e8400-e29b-41d4-a716-446655440002', 
    'Acta de verificación y aprobación de infraestructura física general de acuerdo al marco del CEUB.', 
    'verificacion_infraestructura_ceub.pdf', 
    '10dbe819-a094-4bf0-a852-a06ce3fa6c08', 
    NOW() - INTERVAL '20 days'
)
ON CONFLICT (id) DO NOTHING;

-- Evidencia 5 (Indicador: b10e8400-e29b-41d4-a716-446655440012)
INSERT INTO evidence (id, indicator_id, latest_version_id, created_at)
VALUES ('e11e8400-e29b-41d4-a716-446655440005', 'b10e8400-e29b-41d4-a716-446655440012', 'f11e8400-e29b-41d4-a716-446655440005', NOW() - INTERVAL '18 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO evidence_version (id, evidence_id, version_number, content_hash, criterion_id, description, storage_key, created_by, created_at)
VALUES (
    'f11e8400-e29b-41d4-a716-446655440005', 
    'e11e8400-e29b-41d4-a716-446655440005', 
    1, 
    'd7f9828e67a4d538e9a1b6c7d2f8e1a0b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e5', 
    '550e8400-e29b-41d4-a716-446655440002', 
    'Reporte de auditoría externa y planos de áreas comunes de estudio autorizados por el CEUB.', 
    'auditoria_areas_comunes_ceub.pdf', 
    '10dbe819-a094-4bf0-a852-a06ce3fa6c08', 
    NOW() - INTERVAL '18 days'
)
ON CONFLICT (id) DO NOTHING;

-- Evidencia 6 (Indicador: b10e8400-e29b-41d4-a716-446655440013)
INSERT INTO evidence (id, indicator_id, latest_version_id, created_at)
VALUES ('e11e8400-e29b-41d4-a716-446655440006', 'b10e8400-e29b-41d4-a716-446655440013', 'f11e8400-e29b-41d4-a716-446655440006', NOW() - INTERVAL '12 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO evidence_version (id, evidence_id, version_number, content_hash, criterion_id, description, storage_key, created_by, created_at)
VALUES (
    'f11e8400-e29b-41d4-a716-446655440006', 
    'e11e8400-e29b-41d4-a716-446655440006', 
    1, 
    'e7f9828e67a4d538e9a1b6c7d2f8e1a0b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e6', 
    '550e8400-e29b-41d4-a716-446655440002', 
    'Certificaciones y licencias del software de laboratorio aprobado para la acreditación CEUB.', 
    'certificados_licencias_software.pdf', 
    '10dbe819-a094-4bf0-a852-a06ce3fa6c08', 
    NOW() - INTERVAL '12 days'
)
ON CONFLICT (id) DO NOTHING;

-- Evidencia 7 (Indicador: c10e8400-e29b-41d4-a716-446655440016)
INSERT INTO evidence (id, indicator_id, latest_version_id, created_at)
VALUES ('e11e8400-e29b-41d4-a716-446655440007', 'c10e8400-e29b-41d4-a716-446655440016', 'f11e8400-e29b-41d4-a716-446655440007', NOW() - INTERVAL '5 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO evidence_version (id, evidence_id, version_number, content_hash, criterion_id, description, storage_key, created_by, created_at)
VALUES (
    'f11e8400-e29b-41d4-a716-446655440007', 
    'e11e8400-e29b-41d4-a716-446655440007', 
    1, 
    'd7f9828e67a4d538e9a1b6c7d2f8e1a0b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e1', 
    '550e8400-e29b-41d4-a716-446655440002', 
    'Informe técnico final y planos del auditorio e instalaciones comunes bajo criterios ARCU-SUR.', 
    'informe_infraestructura_arcusur.pdf', 
    '10dbe819-a094-4bf0-a852-a06ce3fa6c08', 
    NOW() - INTERVAL '5 days'
)
ON CONFLICT (id) DO NOTHING;

-- Evidencia 8 (Indicador: c10e8400-e29b-41d4-a716-446655440017)
INSERT INTO evidence (id, indicator_id, latest_version_id, created_at)
VALUES ('e11e8400-e29b-41d4-a716-446655440008', 'c10e8400-e29b-41d4-a716-446655440017', 'f11e8400-e29b-41d4-a716-446655440008', NOW() - INTERVAL '4 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO evidence_version (id, evidence_id, version_number, content_hash, criterion_id, description, storage_key, created_by, created_at)
VALUES (
    'f11e8400-e29b-41d4-a716-446655440008', 
    'e11e8400-e29b-41d4-a716-446655440008', 
    1, 
    'f7f9828e67a4d538e9a1b6c7d2f8e1a0b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8', 
    '550e8400-e29b-41d4-a716-446655440002', 
    'Plan de mantenimiento correctivo y preventivo de laboratorios certificado por la comisión ARCU-SUR.', 
    'plan_mantenimiento_laboratorios.pdf', 
    '10dbe819-a094-4bf0-a852-a06ce3fa6c08', 
    NOW() - INTERVAL '4 days'
)
ON CONFLICT (id) DO NOTHING;

-- Evidencia 9 (Indicador: c10e8400-e29b-41d4-a716-446655440018)
INSERT INTO evidence (id, indicator_id, latest_version_id, created_at)
VALUES ('e11e8400-e29b-41d4-a716-446655440009', 'c10e8400-e29b-41d4-a716-446655440018', 'f11e8400-e29b-41d4-a716-446655440009', NOW() - INTERVAL '2 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO evidence_version (id, evidence_id, version_number, content_hash, criterion_id, description, storage_key, created_by, created_at)
VALUES (
    'f11e8400-e29b-41d4-a716-446655440009', 
    'e11e8400-e29b-41d4-a716-446655440009', 
    1, 
    '07f9828e67a4d538e9a1b6c7d2f8e1a0b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e9', 
    '550e8400-e29b-41d4-a716-446655440002', 
    'Manual de higiene, bioseguridad y señalética de aulas universitarias de la Facultad de Tecnología.', 
    'manual_bioseguridad_arcusur.pdf', 
    '10dbe819-a094-4bf0-a852-a06ce3fa6c08', 
    NOW() - INTERVAL '2 days'
)
ON CONFLICT (id) DO NOTHING;

-- Evidencia 10 (Indicador: a10e8400-e29b-41d4-a716-446655440004, Dimensión: Plan de Estudios)
INSERT INTO evidence (id, indicator_id, latest_version_id, created_at)
VALUES ('e11e8400-e29b-41d4-a716-446655440010', 'a10e8400-e29b-41d4-a716-446655440004', 'f11e8400-e29b-41d4-a716-446655440010', NOW() - INTERVAL '3 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO evidence_version (id, evidence_id, version_number, content_hash, criterion_id, description, storage_key, created_by, created_at)
VALUES (
    'f11e8400-e29b-41d4-a716-446655440010', 
    'e11e8400-e29b-41d4-a716-446655440010', 
    1, 
    'e6f9828e67a4d538e9a1b6c7d2f8e1a0b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e9', 
    '550e8400-e29b-41d4-a716-446655440052', 
    'Plan de estudios oficial y malla curricular de la carrera de Ingeniería de Sistemas aprobada por el CEUB.', 
    'plan_estudios_sistemas.pdf', 
    '17228eb7-02f3-4542-a754-a86edfb9299a', 
    NOW() - INTERVAL '3 days'
)
ON CONFLICT (id) DO NOTHING;

-- Evidencia 11 (Indicador: a10e8400-e29b-41d4-a716-446655440005, Dimensión: Docentes)
INSERT INTO evidence (id, indicator_id, latest_version_id, created_at)
VALUES ('e11e8400-e29b-41d4-a716-446655440011', 'a10e8400-e29b-41d4-a716-446655440005', 'f11e8400-e29b-41d4-a716-446655440011', NOW() - INTERVAL '2 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO evidence_version (id, evidence_id, version_number, content_hash, criterion_id, description, storage_key, created_by, created_at)
VALUES (
    'f11e8400-e29b-41d4-a716-446655440011', 
    'e11e8400-e29b-41d4-a716-446655440011', 
    1, 
    'f6f9828e67a4d538e9a1b6c7d2f8e1a0b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e0', 
    '550e8400-e29b-41d4-a716-446655440062', 
    'Planillas de sueldos y hoja de vida de los docentes de la carrera de Ingeniería de Sistemas.', 
    'hojas_vida_docentes.pdf', 
    '17228eb7-02f3-4542-a754-a86edfb9299a', 
    NOW() - INTERVAL '2 days'
)
ON CONFLICT (id) DO NOTHING;

-- Evidencia 12 (Indicador: a10e8400-e29b-41d4-a716-446655440006, Dimensión: Administración)
INSERT INTO evidence (id, indicator_id, latest_version_id, created_at)
VALUES ('e11e8400-e29b-41d4-a716-446655440012', 'a10e8400-e29b-41d4-a716-446655440006', 'f11e8400-e29b-41d4-a716-446655440012', NOW() - INTERVAL '1 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO evidence_version (id, evidence_id, version_number, content_hash, criterion_id, description, storage_key, created_by, created_at)
VALUES (
    'f11e8400-e29b-41d4-a716-446655440012', 
    'e11e8400-e29b-41d4-a716-446655440012', 
    1, 
    '06f9828e67a4d538e9a1b6c7d2f8e1a0b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e1', 
    '550e8400-e29b-41d4-a716-446655440072', 
    'Plan Estratégico Institucional (PEI) y presupuesto anual aprobado para la Facultad de Tecnología.', 
    'pei_presupuesto_facultad.pdf', 
    '17228eb7-02f3-4542-a754-a86edfb9299a', 
    NOW() - INTERVAL '1 days'
)
ON CONFLICT (id) DO NOTHING;
