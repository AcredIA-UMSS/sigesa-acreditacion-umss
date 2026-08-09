-- Seed SQL file to populate evidence and evidence versions for multiple programs / careers in SIGESA

-- Asegurar que las carreras CEUB y ARCU-SUR existan en la tabla maestro de programas (programs)
-- para que los JOINs en las consultas de búsqueda no descarten los registros.
INSERT INTO programs (id, code, name, faculty, active)
VALUES 
('660e8400-e29b-41d4-a716-446655440001', 'CEUB-DEMO', 'Coordinación CEUB (demo)', 'Tecnología', TRUE),
('770e8400-e29b-41d4-a716-446655440002', 'ARCUSUR-DEMO', 'Coordinación ARCU-SUR (demo)', 'Tecnología', TRUE)
ON CONFLICT (id) DO NOTHING;

-- Clean up existing data to avoid PK / unique constraint collisions
DELETE FROM evidence_version;
DELETE FROM evidence;

-- ==========================================
-- 1. Ingeniería de Sistemas (program: 550e8400-e29b-41d4-a716-446655440000)
-- ==========================================

-- Evidencia 1 (Indicador: a10e8400-e29b-41d4-a716-446655440001)
INSERT INTO evidence (id, indicator_id, latest_version_id, created_at)
VALUES ('e11e8400-e29b-41d4-a716-446655440001', 'a10e8400-e29b-41d4-a716-446655440001', 'f11e8400-e29b-41d4-a716-446655440001', NOW() - INTERVAL '15 days');

INSERT INTO evidence_version (id, evidence_id, version_number, content_hash, criterion_id, description, storage_key, created_by, created_at)
VALUES (
    'f11e8400-e29b-41d4-a716-446655440001', 
    'e11e8400-e29b-41d4-a716-446655440001', 
    1, 
    'a6f9828e67a4d538e9a1b6c7d2f8e1a0b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8', 
    '550e8400-e29b-41d4-a716-446655440002', -- Dimensión: Infraestructura (CRT-04)
    'Planos aprobados y distribución de laboratorios de computación y aulas para Ingeniería de Sistemas.', 
    'planos_distribucion_aulas.pdf', 
    '17228eb7-02f3-4542-a754-a86edfb9299a', -- cc@umss.edu.bo
    NOW() - INTERVAL '15 days'
);

-- Evidencia 2 (Indicador: a10e8400-e29b-41d4-a716-446655440002)
INSERT INTO evidence (id, indicator_id, latest_version_id, created_at)
VALUES ('e11e8400-e29b-41d4-a716-446655440002', 'a10e8400-e29b-41d4-a716-446655440002', 'f11e8400-e29b-41d4-a716-446655440002', NOW() - INTERVAL '10 days');

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
);

-- Evidencia 3 (Indicador: a10e8400-e29b-41d4-a716-446655440003)
INSERT INTO evidence (id, indicator_id, latest_version_id, created_at)
VALUES ('e11e8400-e29b-41d4-a716-446655440003', 'a10e8400-e29b-41d4-a716-446655440003', 'f11e8400-e29b-41d4-a716-446655440003', NOW() - INTERVAL '8 days');

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
);


-- ==========================================
-- 2. Coordinación CEUB (program: 660e8400-e29b-41d4-a716-446655440001)
-- ==========================================

-- Evidencia 4 (Indicador: b10e8400-e29b-41d4-a716-446655440011)
INSERT INTO evidence (id, indicator_id, latest_version_id, created_at)
VALUES ('e11e8400-e29b-41d4-a716-446655440004', 'b10e8400-e29b-41d4-a716-446655440011', 'f11e8400-e29b-41d4-a716-446655440004', NOW() - INTERVAL '20 days');

INSERT INTO evidence_version (id, evidence_id, version_number, content_hash, criterion_id, description, storage_key, created_by, created_at)
VALUES (
    'f11e8400-e29b-41d4-a716-446655440004', 
    'e11e8400-e29b-41d4-a716-446655440004', 
    1, 
    'c7f9828e67a4d538e9a1b6c7d2f8e1a0b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e0', 
    '550e8400-e29b-41d4-a716-446655440002', 
    'Acta de verificación y aprobación de infraestructura física general de acuerdo al marco del CEUB.', 
    'verificacion_infraestructura_ceub.pdf', 
    '10dbe819-a094-4bf0-a852-a06ce3fa6c08', -- td@umss.edu.bo
    NOW() - INTERVAL '20 days'
);

-- Evidencia 5 (Indicador: b10e8400-e29b-41d4-a716-446655440012)
INSERT INTO evidence (id, indicator_id, latest_version_id, created_at)
VALUES ('e11e8400-e29b-41d4-a716-446655440005', 'b10e8400-e29b-41d4-a716-446655440012', 'f11e8400-e29b-41d4-a716-446655440005', NOW() - INTERVAL '18 days');

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
);

-- Evidencia 6 (Indicador: b10e8400-e29b-41d4-a716-446655440013)
INSERT INTO evidence (id, indicator_id, latest_version_id, created_at)
VALUES ('e11e8400-e29b-41d4-a716-446655440006', 'b10e8400-e29b-41d4-a716-446655440013', 'f11e8400-e29b-41d4-a716-446655440006', NOW() - INTERVAL '12 days');

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
);


-- ==========================================
-- 3. Coordinación ARCU-SUR (program: 770e8400-e29b-41d4-a716-446655440002)
-- ==========================================

-- Evidencia 7 (Indicador: c10e8400-e29b-41d4-a716-446655440016)
INSERT INTO evidence (id, indicator_id, latest_version_id, created_at)
VALUES ('e11e8400-e29b-41d4-a716-446655440007', 'c10e8400-e29b-41d4-a716-446655440016', 'f11e8400-e29b-41d4-a716-446655440007', NOW() - INTERVAL '5 days');

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
);

-- Evidencia 8 (Indicador: c10e8400-e29b-41d4-a716-446655440017)
INSERT INTO evidence (id, indicator_id, latest_version_id, created_at)
VALUES ('e11e8400-e29b-41d4-a716-446655440008', 'c10e8400-e29b-41d4-a716-446655440017', 'f11e8400-e29b-41d4-a716-446655440008', NOW() - INTERVAL '4 days');

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
);

-- Evidencia 9 (Indicador: c10e8400-e29b-41d4-a716-446655440009)
-- Indicador: c10e8400-e29b-41d4-a716-446655440018
INSERT INTO evidence (id, indicator_id, latest_version_id, created_at)
VALUES ('e11e8400-e29b-41d4-a716-446655440009', 'c10e8400-e29b-41d4-a716-446655440018', 'f11e8400-e29b-41d4-a716-446655440009', NOW() - INTERVAL '2 days');

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
);
