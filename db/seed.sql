-- Seed SQL file for SIGESA (PostgreSQL)
-- NOTA: No inserta usuarios en 'app_user' ni 'user_program_assignment' (ya existen en BD).
-- Genera datos maestros (5 registros por tabla) y tablas transaccionales (≥ 20 registros con FKs válidas).

-- 1. ESTRUCTURA DE TABLAS (CREATE TABLE IF NOT EXISTS)
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
('OBS-2026-019', '990e8400-e29b-41d4-a716-446655440004', 'a10e8400-e29b-41d4-a716-446655440002', 'IND-INF-01', 'Licencias de Software', 'Falta listar licencias académicas.', NOW() - INTERVAL '11 days', NOW() + INTERVAL '5 days', 1, 'PENDIENTE_SUBSANACION', '/coordinator/evidences/IND-501/subsanar'),
('OBS-2026-020', '990e8400-e29b-41d4-a716-446655440004', 'a10e8400-e29b-41d4-a716-446655440003', 'IND-INF-02', 'Servidores de Prácticas', 'Acceso SSH verificado correctamente.', NOW() - INTERVAL '20 days', NOW() - INTERVAL '5 days', 1, 'APROBADO', '/coordinator/evidences/IND-502/subsanar')
ON CONFLICT (observation_id) DO NOTHING;
