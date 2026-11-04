-- =============================================================================
-- Seed de datos iniciales para el Laboratorio E3
-- Modelos Evaluadores: CEUB (10 Áreas) y ARCU-SUR (4 Dimensiones)
-- Carreras reales de la Universidad Mayor de San Simón (UMSS):
--   1. Licenciatura en Informática (FCT) -> Proceso CEUB (10 Áreas)
--   2. Ingeniería Electromecánica (FCT) -> Proceso CEUB (10 Áreas)
--   3. Licenciatura en Bioquímica y Farmacia (FBF) -> Proceso ARCU-SUR (4 Dimensiones)
--   4. Licenciatura en Odontología (FO) -> Proceso CEUB (10 Áreas)
--   5. Ciencias de la Educación (FHCE)
--   6. Fisioterapia y Kinesiología (FCMM)
-- =============================================================================

-- =============================================================================
-- 0. DDL PREVENTIVO: Asegura que todas las tablas existan antes de insertar seeds
-- Compatible tanto en arranque inicial de Docker/Podman como en bases ya migradas
-- =============================================================================

CREATE TABLE IF NOT EXISTS programs (
    id         UUID         PRIMARY KEY,
    code       VARCHAR(32)  NOT NULL,
    name       VARCHAR(255) NOT NULL,
    faculty    VARCHAR(128),
    active     BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_programs_code UNIQUE (code)
);

CREATE TABLE IF NOT EXISTS app_user (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone_number VARCHAR(20),
    failed_attempts INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_program_assignment (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    program_id UUID NOT NULL,
    assigned_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked_at TIMESTAMP WITHOUT TIME ZONE
);

ALTER TABLE user_program_assignment ADD COLUMN IF NOT EXISTS revoked_at TIMESTAMP WITHOUT TIME ZONE;

CREATE TABLE IF NOT EXISTS templates (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    evaluator_model VARCHAR(20),
    type VARCHAR(50),
    version VARCHAR(50),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS accreditation_processes (
    id UUID PRIMARY KEY,
    career_id UUID NOT NULL,
    template_id UUID NOT NULL,
    status VARCHAR(255) NOT NULL,
    start_date TIMESTAMP NOT NULL,
    operational_mode VARCHAR(50) DEFAULT 'ACTIVE'
);

ALTER TABLE accreditation_processes ADD COLUMN IF NOT EXISTS operational_mode VARCHAR(50) DEFAULT 'ACTIVE';

CREATE TABLE IF NOT EXISTS level1_nodes (
    id                  UUID PRIMARY KEY,
    process_id          UUID NOT NULL,
    name                VARCHAR(255) NOT NULL,
    level1_order        INT NOT NULL,
    description         TEXT,
    status              VARCHAR(32) NOT NULL DEFAULT 'ABIERTA',
    created_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS level2_nodes (
    id                  UUID PRIMARY KEY,
    level1_id           UUID NOT NULL,
    name                VARCHAR(255) NOT NULL,
    level2_order        INT NOT NULL,
    description         TEXT,
    created_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS level3_nodes (
    id                  UUID PRIMARY KEY,
    level2_id           UUID NOT NULL,
    name                VARCHAR(255) NOT NULL,
    level3_order        INT NOT NULL,
    description         TEXT,
    created_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS indicators (
    id                  UUID PRIMARY KEY,
    level3_id           UUID NOT NULL,
    code                VARCHAR(64) NOT NULL,
    description         TEXT NOT NULL,
    weight              NUMERIC(10, 4) NOT NULL DEFAULT 1.0000,
    indicator_order     INT NOT NULL,
    reference_url       VARCHAR(2048) NOT NULL,
    status              VARCHAR(32) NOT NULL DEFAULT 'PENDIENTE',
    created_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS evidence (
    id                     UUID PRIMARY KEY,
    created_at             TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    indicator_id           UUID,
    latest_version_id      UUID,
    normative_indicator_id UUID
);

CREATE TABLE IF NOT EXISTS document_assets (
    id                  UUID PRIMARY KEY,
    process_id          UUID NOT NULL,
    folio_code          VARCHAR(64) NOT NULL UNIQUE,
    title               VARCHAR(500) NOT NULL,
    storage_key         VARCHAR(500) NOT NULL,
    content_hash        VARCHAR(64) NOT NULL,
    is_institutional    BOOLEAN NOT NULL DEFAULT FALSE,
    uploaded_by         UUID NOT NULL,
    created_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS indicator_evidence_links (
    id                      UUID PRIMARY KEY,
    indicator_id            UUID NOT NULL,
    document_asset_id       UUID NOT NULL,
    evidence_id             UUID NOT NULL,
    is_primary_reference    BOOLEAN NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS indicator_observation (
    id                      UUID PRIMARY KEY,
    indicator_id            UUID NOT NULL,
    author_id               UUID NOT NULL,
    author_role             VARCHAR(10) NOT NULL,
    body                    TEXT NOT NULL,
    status                  VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    resolved_at             TIMESTAMP,
    resolved_version_id     UUID,
    created_at              TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS evidence_version (
    id                        UUID PRIMARY KEY,
    content_hash              VARCHAR(64) NOT NULL,
    created_at                TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by                UUID NOT NULL,
    criterion_id              UUID,
    description               VARCHAR(2000) NOT NULL,
    evidence_id               UUID NOT NULL,
    storage_key               VARCHAR(500) NOT NULL,
    version_number            INTEGER NOT NULL,
    external_url              VARCHAR(2048),
    indicator_observation_id  UUID,
    observation_id            UUID,
    original_filename         VARCHAR(500),
    supersedes_version_number INTEGER,
    blob_purged               BOOLEAN NOT NULL DEFAULT FALSE
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
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP
);

-- =============================================================================
-- 1. CARRERAS REALES DE LA UMSS (programs)
-- =============================================================================
DELETE FROM programs WHERE code IN ('ING-BIOMED', 'ING-IA', 'ING-CYBER', 'ING-ROB');

INSERT INTO programs (id, code, name, faculty, active)
VALUES
    ('e3000000-0000-0000-0001-000000000001', 'LIC-INF', 'Licenciatura en Informática', 'FCT', TRUE),
    ('e3000000-0000-0000-0001-000000000002', 'ING-ELMEC', 'Ingeniería Electromecánica', 'FCT', TRUE),
    ('e3000000-0000-0000-0001-000000000003', 'BIOQ-FARM', 'Licenciatura en Bioquímica y Farmacia', 'FBF', TRUE),
    ('e3000000-0000-0000-0001-000000000004', 'ODON', 'Licenciatura en Odontología', 'FO', TRUE),
    ('e3000000-0000-0000-0001-000000000005', 'CIEN-EDU', 'Ciencias de la Educación', 'FHCE', TRUE),
    ('e3000000-0000-0000-0001-000000000006', 'FIS-KIN', 'Licenciatura en Fisioterapia y Kinesiología', 'FCMM', TRUE),
    ('e3000000-0000-0000-0001-000000000007', 'LIC-MAT', 'Licenciatura en Matemáticas', 'FCT', TRUE),
    ('e3000000-0000-0000-0001-000000000008', 'LIC-FIS', 'Licenciatura en Física', 'FCT', TRUE),
    ('e3000000-0000-0000-0001-000000000009', 'ING-SIST-E3', 'Ingeniería de Sistemas - Mención Software', 'FCT', TRUE),
    ('e3000000-0000-0000-0001-000000000010', 'BIO-ING', 'Ingeniería Biomédica', 'FCT', TRUE),
    ('e3000000-0000-0000-0001-000000000011', 'ING-ROB-E3', 'Ingeniería Robótica y Automatización', 'FCT', TRUE),
    ('e3000000-0000-0000-0001-000000000012', 'ING-TELE-E3', 'Ingeniería en Telecomunicaciones y Redes', 'FCT', TRUE),
    ('e3000000-0000-0000-0001-000000000013', 'LIC-QUIM', 'Licenciatura en Química', 'FCT', TRUE),
    ('e3000000-0000-0000-0001-000000000014', 'ING-HIDR', 'Ingeniería de Recursos Hídricos', 'FCT', TRUE),
    ('e3000000-0000-0000-0001-000000000015', 'ING-AGRO-E3', 'Ingeniería Agroindustrial', 'FCAyP', TRUE)
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    code = EXCLUDED.code,
    faculty = EXCLUDED.faculty,
    active = EXCLUDED.active;

-- =============================================================================
-- 2. USUARIOS DEL SISTEMA (app_user)
-- =============================================================
DELETE FROM app_user WHERE email IN ('coord.biomed@umss.edu.bo', 'coord.ia@umss.edu.bo', 'coord.cyber@umss.edu.bo', 'coord.rob@umss.edu.bo');

INSERT INTO app_user (
    id, email, password_hash, role, status, first_name, last_name, phone_number, failed_attempts, created_at, updated_at
) VALUES
    ('e3000000-0000-0000-0002-000000000001', 'coord.informatica@umss.edu.bo', '$argon2id$v=19$m=16384,t=2,p=1$z5SuTQXKid+sUWQiI5d0FQ$nqZDUEXON6VZSTqTpvcrxMly3GNVl9RPOjz/onysnhg', 'CC', 'ACTIVE', 'Ing. Marcelo', 'Quiroga', '+591-4-4250101', 0, NOW(), NOW()),
    ('e3000000-0000-0000-0002-000000000002', 'coord.electromecanica@umss.edu.bo', '$argon2id$v=19$m=16384,t=2,p=1$z5SuTQXKid+sUWQiI5d0FQ$nqZDUEXON6VZSTqTpvcrxMly3GNVl9RPOjz/onysnhg', 'CC', 'ACTIVE', 'Ing. Gonzalo', 'Torrico', '+591-4-4250102', 0, NOW(), NOW()),
    ('e3000000-0000-0000-0002-000000000003', 'coord.bioquimica@umss.edu.bo', '$argon2id$v=19$m=16384,t=2,p=1$z5SuTQXKid+sUWQiI5d0FQ$nqZDUEXON6VZSTqTpvcrxMly3GNVl9RPOjz/onysnhg', 'CC', 'ACTIVE', 'Dra. Elena', 'Camacho', '+591-4-4250103', 0, NOW(), NOW()),
    ('e3000000-0000-0000-0002-000000000004', 'coord.odontologia@umss.edu.bo', '$argon2id$v=19$m=16384,t=2,p=1$z5SuTQXKid+sUWQiI5d0FQ$nqZDUEXON6VZSTqTpvcrxMly3GNVl9RPOjz/onysnhg', 'CC', 'ACTIVE', 'Dr. Javier', 'Montaño', '+591-4-4250104', 0, NOW(), NOW()),
    ('e3000000-0000-0000-0002-000000000005', 'evaluador.e3@umss.edu.bo', '$argon2id$v=19$m=16384,t=2,p=1$z5SuTQXKid+sUWQiI5d0FQ$nqZDUEXON6VZSTqTpvcrxMly3GNVl9RPOjz/onysnhg', 'EE', 'ACTIVE', 'Dr. Roberto', 'Gómez', '+591-4-4250105', 0, NOW(), NOW()),
    ('e3000000-0000-0000-0002-000000000006', 'cae.e3@umss.edu.bo', '$argon2id$v=19$m=16384,t=2,p=1$z5SuTQXKid+sUWQiI5d0FQ$nqZDUEXON6VZSTqTpvcrxMly3GNVl9RPOjz/onysnhg', 'CAE_MEMBER', 'ACTIVE', 'Ing. Lucía', 'Fernández', '+591-4-4250106', 0, NOW(), NOW()),
    ('e3000000-0000-0000-0002-000000000007', 'coord.sistemas@umss.edu.bo', '$argon2id$v=19$m=16384,t=2,p=1$z5SuTQXKid+sUWQiI5d0FQ$nqZDUEXON6VZSTqTpvcrxMly3GNVl9RPOjz/onysnhg', 'CC', 'ACTIVE', 'Ing. Boris', 'Calancha', '+591-4-4250107', 0, NOW(), NOW()),
    ('e3000000-0000-0000-0002-000000000008', 'coord.civil@umss.edu.bo', '$argon2id$v=19$m=16384,t=2,p=1$z5SuTQXKid+sUWQiI5d0FQ$nqZDUEXON6VZSTqTpvcrxMly3GNVl9RPOjz/onysnhg', 'CC', 'ACTIVE', 'Ing. Patricia', 'Vargas', '+591-4-4250108', 0, NOW(), NOW()),
    ('e3000000-0000-0000-0002-000000000009', 'coord.medicina@umss.edu.bo', '$argon2id$v=19$m=16384,t=2,p=1$z5SuTQXKid+sUWQiI5d0FQ$nqZDUEXON6VZSTqTpvcrxMly3GNVl9RPOjz/onysnhg', 'CC', 'ACTIVE', 'Dr. Carlos', 'Arnez', '+591-4-4250109', 0, NOW(), NOW()),
    ('e3000000-0000-0000-0002-000000000010', 'coord.admin@umss.edu.bo', '$argon2id$v=19$m=16384,t=2,p=1$z5SuTQXKid+sUWQiI5d0FQ$nqZDUEXON6VZSTqTpvcrxMly3GNVl9RPOjz/onysnhg', 'CC', 'ACTIVE', 'Lic. Fernando', 'Rocha', '+591-4-4250110', 0, NOW(), NOW()),
    ('e3000000-0000-0000-0002-000000000011', 'coord.derecho@umss.edu.bo', '$argon2id$v=19$m=16384,t=2,p=1$z5SuTQXKid+sUWQiI5d0FQ$nqZDUEXON6VZSTqTpvcrxMly3GNVl9RPOjz/onysnhg', 'CC', 'ACTIVE', 'Dra. Miriam', 'Claure', '+591-4-4250111', 0, NOW(), NOW()),
    ('e3000000-0000-0000-0002-000000000012', 'coord.industrial@umss.edu.bo', '$argon2id$v=19$m=16384,t=2,p=1$z5SuTQXKid+sUWQiI5d0FQ$nqZDUEXON6VZSTqTpvcrxMly3GNVl9RPOjz/onysnhg', 'CC', 'ACTIVE', 'Ing. Álvaro', 'Guzmán', '+591-4-4250112', 0, NOW(), NOW()),
    ('e3000000-0000-0000-0002-000000000013', 'coord.enfermeria@umss.edu.bo', '$argon2id$v=19$m=16384,t=2,p=1$z5SuTQXKid+sUWQiI5d0FQ$nqZDUEXON6VZSTqTpvcrxMly3GNVl9RPOjz/onysnhg', 'CC', 'ACTIVE', 'Lic. Carmen', 'Mercado', '+591-4-4250113', 0, NOW(), NOW()),
    ('e3000000-0000-0000-0002-000000000014', 'coord.arquitectura@umss.edu.bo', '$argon2id$v=19$m=16384,t=2,p=1$z5SuTQXKid+sUWQiI5d0FQ$nqZDUEXON6VZSTqTpvcrxMly3GNVl9RPOjz/onysnhg', 'CC', 'ACTIVE', 'Arq. Diego', 'Morales', '+591-4-4250114', 0, NOW(), NOW()),
    ('e3000000-0000-0000-0002-000000000015', 'coord.economia@umss.edu.bo', '$argon2id$v=19$m=16384,t=2,p=1$z5SuTQXKid+sUWQiI5d0FQ$nqZDUEXON6VZSTqTpvcrxMly3GNVl9RPOjz/onysnhg', 'CC', 'ACTIVE', 'Lic. Gabriela', 'Siles', '+591-4-4250115', 0, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET
    email = EXCLUDED.email,
    first_name = EXCLUDED.first_name,
    last_name = EXCLUDED.last_name,
    status = EXCLUDED.status;

-- =============================================================================
-- 3. ASIGNACIÓN USUARIO - CARRERA (user_program_assignment)
-- =============================================================================
INSERT INTO user_program_assignment (id, user_id, program_id, assigned_at, revoked_at)
VALUES
    ('e3000000-0000-0000-0004-000000000001', 'e3000000-0000-0000-0002-000000000001', 'e3000000-0000-0000-0001-000000000001', NOW(), NULL),
    ('e3000000-0000-0000-0004-000000000002', 'e3000000-0000-0000-0002-000000000002', 'e3000000-0000-0000-0001-000000000002', NOW(), NULL),
    ('e3000000-0000-0000-0004-000000000003', 'e3000000-0000-0000-0002-000000000003', 'e3000000-0000-0000-0001-000000000003', NOW(), NULL),
    ('e3000000-0000-0000-0004-000000000004', 'e3000000-0000-0000-0002-000000000004', 'e3000000-0000-0000-0001-000000000004', NOW(), NULL),
    ('e3000000-0000-0000-0004-000000000005', 'e3000000-0000-0000-0002-000000000007', 'e3000000-0000-0000-0001-000000000007', NOW(), NULL),
    ('e3000000-0000-0000-0004-000000000006', 'e3000000-0000-0000-0002-000000000008', 'e3000000-0000-0000-0001-000000000008', NOW(), NULL),
    ('e3000000-0000-0000-0004-000000000007', 'e3000000-0000-0000-0002-000000000009', 'e3000000-0000-0000-0001-000000000009', NOW(), NULL),
    ('e3000000-0000-0000-0004-000000000008', 'e3000000-0000-0000-0002-000000000010', 'e3000000-0000-0000-0001-000000000010', NOW(), NULL),
    ('e3000000-0000-0000-0004-000000000009', 'e3000000-0000-0000-0002-000000000011', 'e3000000-0000-0000-0001-000000000011', NOW(), NULL),
    ('e3000000-0000-0000-0004-000000000010', 'e3000000-0000-0000-0002-000000000012', 'e3000000-0000-0000-0001-000000000012', NOW(), NULL),
    ('e3000000-0000-0000-0004-000000000011', 'e3000000-0000-0000-0002-000000000013', 'e3000000-0000-0000-0001-000000000013', NOW(), NULL),
    ('e3000000-0000-0000-0004-000000000012', 'e3000000-0000-0000-0002-000000000014', 'e3000000-0000-0000-0001-000000000014', NOW(), NULL),
    ('e3000000-0000-0000-0004-000000000013', 'e3000000-0000-0000-0002-000000000015', 'e3000000-0000-0000-0001-000000000015', NOW(), NULL)
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- 4. PROCESOS DE ACREDITACIÓN ABIERTOS / ACTIVOS (accreditation_processes)
-- Plantillas:
--   - 850e8400-e29b-41d4-a716-446655440010: CEUB 2026 (10 Áreas)
--   - 850e8400-e29b-41d4-a716-446655440011: ARCU-SUR 2026 (4 Dimensiones)
-- =============================================================================
INSERT INTO accreditation_processes (id, career_id, template_id, status, start_date, operational_mode)
VALUES
    ('e3000000-0000-0003-0000-000000000001', 'e3000000-0000-0000-0001-000000000001', '850e8400-e29b-41d4-a716-446655440010', 'ACTIVE', NOW() - INTERVAL '40 days', 'ACTIVE'),
    ('e3000000-0000-0003-0000-000000000002', 'e3000000-0000-0000-0001-000000000002', '850e8400-e29b-41d4-a716-446655440010', 'ACTIVE', NOW() - INTERVAL '20 days', 'ACTIVE'),
    ('e3000000-0000-0003-0000-000000000003', 'e3000000-0000-0000-0001-000000000003', '850e8400-e29b-41d4-a716-446655440011', 'ACTIVE', NOW() - INTERVAL '10 days', 'ACTIVE'),
    ('e3000000-0000-0003-0000-000000000004', 'e3000000-0000-0000-0001-000000000004', '850e8400-e29b-41d4-a716-446655440010', 'ACTIVE', NOW() - INTERVAL '3 days', 'ACTIVE'),
    ('e3000000-0000-0003-0000-000000000005', 'e3000000-0000-0000-0001-000000000007', '850e8400-e29b-41d4-a716-446655440010', 'ACTIVE', NOW() - INTERVAL '30 days', 'ACTIVE'),
    ('e3000000-0000-0003-0000-000000000006', 'e3000000-0000-0000-0001-000000000008', '850e8400-e29b-41d4-a716-446655440010', 'ACTIVE', NOW() - INTERVAL '25 days', 'ACTIVE'),
    ('e3000000-0000-0003-0000-000000000007', 'e3000000-0000-0000-0001-000000000009', '850e8400-e29b-41d4-a716-446655440011', 'ACTIVE', NOW() - INTERVAL '15 days', 'ACTIVE'),
    ('e3000000-0000-0003-0000-000000000008', 'e3000000-0000-0000-0001-000000000010', '850e8400-e29b-41d4-a716-446655440010', 'ACTIVE', NOW() - INTERVAL '12 days', 'ACTIVE'),
    ('e3000000-0000-0003-0000-000000000009', 'e3000000-0000-0000-0001-000000000011', '850e8400-e29b-41d4-a716-446655440010', 'ACTIVE', NOW() - INTERVAL '8 days', 'ACTIVE'),
    ('e3000000-0000-0003-0000-000000000010', 'e3000000-0000-0000-0001-000000000012', '850e8400-e29b-41d4-a716-446655440010', 'ACTIVE', NOW() - INTERVAL '5 days', 'ACTIVE'),
    ('e3000000-0000-0003-0000-000000000011', 'e3000000-0000-0000-0001-000000000013', '850e8400-e29b-41d4-a716-446655440011', 'ACTIVE', NOW() - INTERVAL '2 days', 'ACTIVE')
ON CONFLICT (id) DO UPDATE SET
    status = EXCLUDED.status,
    operational_mode = EXCLUDED.operational_mode;

-- =============================================================================
-- 5. ÁRBOL NORMATIVO CEUB (10 ÁREAS REALES)
-- Proceso: e3000000-0000-0003-0000-000000000001 (Licenciatura en Informática)
-- =============================================================================

-- Limpieza en cascada de nodos e indicadores de prueba E3 (CEUB y ARCU-SUR)
DELETE FROM indicator_evidence_links WHERE indicator_id >= 'e3000000-0004-0000-0000-000000000001' AND indicator_id <= 'e3000000-0004-0000-0000-000000000024';
DELETE FROM document_assets WHERE folio_code LIKE 'SIGESA-CEUB-2026-%' OR folio_code LIKE 'SIGESA-ARCU-2026-%';
DELETE FROM evidence_version WHERE id >= 'e3000000-0009-0000-0000-000000000001' AND id <= 'e3000000-0009-0000-0000-000000000030';
DELETE FROM indicator_observation WHERE indicator_id >= 'e3000000-0004-0000-0000-000000000001' AND indicator_id <= 'e3000000-0004-0000-0000-000000000024';
DELETE FROM evidence WHERE id >= 'e3000000-0005-0000-0000-000000000001' AND id <= 'e3000000-0005-0000-0000-000000000024';
DELETE FROM indicators WHERE id >= 'e3000000-0004-0000-0000-000000000001' AND id <= 'e3000000-0004-0000-0000-000000000024';
DELETE FROM level3_nodes WHERE id >= 'e3000000-0003-0000-0000-000000000001' AND id <= 'e3000000-0003-0000-0000-000000000024';
DELETE FROM level2_nodes WHERE id >= 'e3000000-0002-0000-0000-000000000001' AND id <= 'e3000000-0002-0000-0000-000000000024';
DELETE FROM level1_nodes WHERE process_id IN ('e3000000-0000-0003-0000-000000000001', 'e3000000-0000-0003-0000-000000000003');

-- 5.1 Las 10 Áreas Oficiales del CEUB (Nivel 1)
INSERT INTO level1_nodes (id, process_id, name, level1_order, description, status, created_at, updated_at)
VALUES
('e3000000-0001-0000-0000-000000000001', 'e3000000-0000-0003-0000-000000000001', 'Área 1: Normas Generales y Definición Institucional', 1, 'Marco normativo, personería y principios estatutarios de la carrera en la UMSS.', 'ABIERTA', NOW(), NOW()),
('e3000000-0001-0000-0000-000000000002', 'e3000000-0000-0003-0000-000000000001', 'Área 2: Proyecto Académico y Plan de Estudios', 2, 'Objetivos educacionales, perfil profesional del graduado y estructura curricular.', 'ABIERTA', NOW(), NOW()),
('e3000000-0001-0000-0000-000000000003', 'e3000000-0000-0003-0000-000000000001', 'Área 3: Administración y Gestión Académica', 3, 'Estructura directiva, cogobierno paritario docente-estudiantil y procesos de gestión.', 'ABIERTA', NOW(), NOW()),
('e3000000-0001-0000-0000-000000000004', 'e3000000-0000-0003-0000-000000000001', 'Área 4: Docentes y Carrera Académica', 4, 'Admisión docente, categorización, escalafón y evaluación del rendimiento académico.', 'ABIERTA', NOW(), NOW()),
('e3000000-0001-0000-0000-000000000005', 'e3000000-0000-0003-0000-000000000001', 'Área 5: Estudiantes y Titulación', 5, 'Régimen de admisión, permanencia, tasas de titulación y bienestar estudiantil.', 'ABIERTA', NOW(), NOW()),
('e3000000-0001-0000-0000-000000000006', 'e3000000-0000-0003-0000-000000000001', 'Área 6: Investigación Científica y Tecnológica', 6, 'Líneas de investigación, centros de I+D, publicaciones científicas y patentes.', 'ABIERTA', NOW(), NOW()),
('e3000000-0001-0000-0000-000000000007', 'e3000000-0000-0003-0000-000000000001', 'Área 7: Interacción Social y Extensión Universitaria', 7, 'Vinculación de la carrera con la sociedad, convenios interinstitucionales y pasantías.', 'ABIERTA', NOW(), NOW()),
('e3000000-0001-0000-0000-000000000008', 'e3000000-0000-0003-0000-000000000001', 'Área 8: Recursos Educativos y Bibliotecas', 8, 'Biblioteca especializada, hemeroteca, plataformas LMS y software educativo.', 'ABIERTA', NOW(), NOW()),
('e3000000-0001-0000-0000-000000000009', 'e3000000-0000-0003-0000-000000000001', 'Área 9: Administración y Gestión Financiera', 9, 'Presupuesto operativo anual, ejecución del gasto y sostenibilidad financiera.', 'ABIERTA', NOW(), NOW()),
('e3000000-0001-0000-0000-000000000010', 'e3000000-0000-0003-0000-000000000001', 'Área 10: Infraestructura, Laboratorios y Equipamiento', 10, 'Aulas, laboratorios especializados, gabinetes de cómputo y bioseguridad.', 'ABIERTA', NOW(), NOW());

-- 5.2 Variables CEUB (Nivel 2)
INSERT INTO level2_nodes (id, level1_id, name, level2_order, description, created_at, updated_at)
SELECT
    ('e3000000-0002-0000-0000-' || LPAD(i::text, 12, '0'))::uuid,
    ('e3000000-0001-0000-0000-' || LPAD(i::text, 12, '0'))::uuid,
    'Variable ' || i || '.1: Componentes Operativos del Área ' || i,
    1,
    'Definición de criterios operativos y requisitos de evaluación formal CEUB.',
    NOW(),
    NOW()
FROM generate_series(1, 10) AS i;

-- 5.3 Sub-variables CEUB (Nivel 3)
INSERT INTO level3_nodes (id, level2_id, name, level3_order, description, created_at, updated_at)
SELECT
    ('e3000000-0003-0000-0000-' || LPAD(i::text, 12, '0'))::uuid,
    ('e3000000-0002-0000-0000-' || LPAD(i::text, 12, '0'))::uuid,
    'Sub-variable ' || i || '.1.1: Criterios Verificables del Área ' || i,
    1,
    'Parámetros e indicadores normativos de cumplimiento obligatorio.',
    NOW(),
    NOW()
FROM generate_series(1, 10) AS i;

-- 5.4 Indicadores CEUB (Hojas Verificables con estados variados)
INSERT INTO indicators (id, level3_id, code, description, weight, indicator_order, reference_url, status, created_at, updated_at)
VALUES
('e3000000-0004-0000-0000-000000000001', 'e3000000-0003-0000-0000-000000000001', 'CEUB-IND-1.1', 'Estatuto Orgánico de la UMSS, Resoluciones de Creación y Reglamentación de Carrera', 1.0, 1, 'https://duea.umss.edu.bo/normativa/ceub-area1', 'APROBADO', NOW(), NOW()),
('e3000000-0004-0000-0000-000000000002', 'e3000000-0003-0000-0000-000000000002', 'CEUB-IND-2.1', 'Plan de Estudios, Malla Curricular Oficial y Planes Globales de Asignaturas', 1.0, 1, 'https://duea.umss.edu.bo/normativa/ceub-area2', 'APROBADO', NOW(), NOW()),
('e3000000-0004-0000-0000-000000000003', 'e3000000-0003-0000-0000-000000000003', 'CEUB-IND-3.1', 'Manual de Organización y Funciones (MOF) y Actas del H. Consejo de Carrera', 1.0, 1, 'https://duea.umss.edu.bo/normativa/ceub-area3', 'APROBADO', NOW(), NOW()),
('e3000000-0004-0000-0000-000000000004', 'e3000000-0003-0000-0000-000000000004', 'CEUB-IND-4.1', 'Escalafón Docente, Actas de Concurso de Méritos y Evaluaciones Periódicas', 1.0, 1, 'https://duea.umss.edu.bo/normativa/ceub-area4', 'SUBSANADO', NOW(), NOW()),
('e3000000-0004-0000-0000-000000000005', 'e3000000-0003-0000-0000-000000000005', 'CEUB-IND-5.1', 'Reglamento de Admisión, Registro de Titulados y Eficiencia Terminal', 1.0, 1, 'https://duea.umss.edu.bo/normativa/ceub-area5', 'APROBADO', NOW(), NOW()),
('e3000000-0004-0000-0000-000000000006', 'e3000000-0003-0000-0000-000000000006', 'CEUB-IND-6.1', 'Proyectos de Investigación Científica Registrados en DICYT y Artículos Indexados', 1.0, 1, 'https://duea.umss.edu.bo/normativa/ceub-area6', 'SUBIDO', NOW(), NOW()),
('e3000000-0004-0000-0000-000000000007', 'e3000000-0003-0000-0000-000000000007', 'CEUB-IND-7.1', 'Convenios Bilaterales de Cooperación e Informes de Proyectos de Interacción Social', 1.0, 1, 'https://duea.umss.edu.bo/normativa/ceub-area7', 'APROBADO', NOW(), NOW()),
('e3000000-0004-0000-0000-000000000008', 'e3000000-0003-0000-0000-000000000008', 'CEUB-IND-8.1', 'Inventario Bibliográfico, Acceso a Repositorios Digitales y Licencias de Software', 1.0, 1, 'https://duea.umss.edu.bo/normativa/ceub-area8', 'APROBADO', NOW(), NOW()),
('e3000000-0004-0000-0000-000000000009', 'e3000000-0003-0000-0000-000000000009', 'CEUB-IND-9.1', 'Plan Operativo Anual (POA) de la Carrera y Reporte de Ejecución Presupuestaria', 1.0, 1, 'https://duea.umss.edu.bo/normativa/ceub-area9', 'OBSERVADO', NOW(), NOW()),
('e3000000-0004-0000-0000-000000000010', 'e3000000-0003-0000-0000-000000000010', 'CEUB-IND-10.1', 'Inventario Certificado de Laboratorios de Cómputo y Protocolos de Mantenimiento', 1.0, 1, 'https://duea.umss.edu.bo/normativa/ceub-area10', 'APROBADO', NOW(), NOW());

-- =============================================================================
-- 6. ÁRBOL NORMATIVO ARCU-SUR (4 DIMENSIONES REALES DEL MERCOSUR)
-- Proceso: e3000000-0000-0003-0000-000000000003 (Lic. en Bioquímica y Farmacia)
-- =============================================================================

-- 6.1 Las 4 Dimensiones Oficiales ARCU-SUR (Nivel 1)
INSERT INTO level1_nodes (id, process_id, name, level1_order, description, status, created_at, updated_at)
VALUES
('e3000000-0001-0000-0000-000000000021', 'e3000000-0000-0003-0000-000000000003', 'Dimensión 1: Contexto Institucional', 1, 'Marco institucional, gobierno universitario, políticas de calidad e internacionalización regional.', 'ABIERTA', NOW(), NOW()),
('e3000000-0001-0000-0000-000000000022', 'e3000000-0000-0003-0000-000000000003', 'Dimensión 2: Proyecto Académico', 2, 'Objetivos educacionales, perfil de egreso armonizado regionalmente, investigación y extensión.', 'ABIERTA', NOW(), NOW()),
('e3000000-0001-0000-0000-000000000023', 'e3000000-0000-0003-0000-000000000003', 'Dimensión 3: Comunidad Universitaria', 3, 'Estudiantes, cuerpo docente cualificado, personal técnico y seguimiento de graduados.', 'ABIERTA', NOW(), NOW()),
('e3000000-0001-0000-0000-000000000024', 'e3000000-0000-0003-0000-000000000003', 'Dimensión 4: Infraestructura y Recursos', 4, 'Laboratorios clínicos y farmacéuticos certificados, bibliotecas especializadas y sostenibilidad.', 'ABIERTA', NOW(), NOW());

-- 6.2 Componentes ARCU-SUR (Nivel 2)
INSERT INTO level2_nodes (id, level1_id, name, level2_order, description, created_at, updated_at)
SELECT
    ('e3000000-0002-0000-0000-' || LPAD((20 + i)::text, 12, '0'))::uuid,
    ('e3000000-0001-0000-0000-' || LPAD((20 + i)::text, 12, '0'))::uuid,
    'Componente ' || i || '.1: Criterios ARCU-SUR Dimensión ' || i,
    1,
    'Criterios regionales de calidad acordados por la Red de Agencias de Acreditación del MERCOSUR.',
    NOW(),
    NOW()
FROM generate_series(1, 4) AS i;

-- 6.3 Criterios ARCU-SUR (Nivel 3)
INSERT INTO level3_nodes (id, level2_id, name, level3_order, description, created_at, updated_at)
SELECT
    ('e3000000-0003-0000-0000-' || LPAD((20 + i)::text, 12, '0'))::uuid,
    ('e3000000-0002-0000-0000-' || LPAD((20 + i)::text, 12, '0'))::uuid,
    'Criterio ' || i || '.1.1: Estándares Regionales MERCOSUR Dimensión ' || i,
    1,
    'Estándares de calidad de educación superior reconocidos internacionalmente.',
    NOW(),
    NOW()
FROM generate_series(1, 4) AS i;

-- 6.4 Indicadores ARCU-SUR
INSERT INTO indicators (id, level3_id, code, description, weight, indicator_order, reference_url, status, created_at, updated_at)
VALUES
('e3000000-0004-0000-0000-000000000021', 'e3000000-0003-0000-0000-000000000021', 'ARCU-IND-1.1', 'Plan Estratégico de Desarrollo de la Carrera y Convenios de Movilidad Estudiantil MERCOSUR', 1.0, 1, 'https://duea.umss.edu.bo/normativa/arcusur-dim1', 'APROBADO', NOW(), NOW()),
('e3000000-0004-0000-0000-000000000022', 'e3000000-0003-0000-0000-000000000022', 'ARCU-IND-2.1', 'Malla Curricular Homologada y Programas Analíticos con Enfoque en Salud Comunitaria', 1.0, 1, 'https://duea.umss.edu.bo/normativa/arcusur-dim2', 'SUBIDO', NOW(), NOW()),
('e3000000-0004-0000-0000-000000000023', 'e3000000-0003-0000-0000-000000000023', 'ARCU-IND-3.1', 'Cuerpo Docente con Grados de Maestría/Doctorado y Encuestas de Desempeño Profesional', 1.0, 1, 'https://duea.umss.edu.bo/normativa/arcusur-dim3', 'APROBADO', NOW(), NOW()),
('e3000000-0004-0000-0000-000000000024', 'e3000000-0003-0000-0000-000000000024', 'ARCU-IND-4.1', 'Manuales de Bioseguridad y Certificación de Laboratorios Farmacéuticos por SEDES/AGEMED', 1.0, 1, 'https://duea.umss.edu.bo/normativa/arcusur-dim4', 'OBSERVADO', NOW(), NOW());

-- =============================================================================
-- 7. EXPEDIENTE DOCUMENTAL REQUERIDO (document_assets, evidence_version, indicator_evidence_links)
-- Archivos físicos en el silo (.pdf, .csv, .txt) con ciclo de vida: Aprobado, Subido, Observado, Subsanado
-- =============================================================================

-- 7.1 Evidencias base para las 10 Áreas CEUB (Lic. en Informática)
INSERT INTO evidence (id, indicator_id, normative_indicator_id, latest_version_id, created_at)
SELECT
    ('e3000000-0005-0000-0000-' || LPAD(i::text, 12, '0'))::uuid,
    ('e3000000-0004-0000-0000-' || LPAD(i::text, 12, '0'))::uuid,
    ('e3000000-0004-0000-0000-' || LPAD(i::text, 12, '0'))::uuid,
    -- Para el Área 4 asignamos la versión 2 (subsanada); para el resto la versión 1
    CASE WHEN i = 4 
         THEN ('e3000000-0009-0000-0000-000000000014')::uuid 
         ELSE ('e3000000-0009-0000-0000-' || LPAD(i::text, 12, '0'))::uuid 
    END,
    NOW() - (i || ' days')::interval
FROM generate_series(1, 10) AS i;

-- 7.2 DocumentAssets oficiales CEUB (PDF, CSV, TXT)
INSERT INTO document_assets (id, process_id, folio_code, title, storage_key, content_hash, is_institutional, uploaded_by, created_at)
VALUES
('e3000000-0006-0000-0000-000000000001', 'e3000000-0000-0003-0000-000000000001', 'SIGESA-CEUB-2026-F01', 'Estatuto Orgánico de la UMSS y Resolución HCU de Carrera', 'dossier_ceub_area1_estatuto.pdf', encode(sha256('dossier_ceub_area1_estatuto'::bytea), 'hex'), TRUE, 'e3000000-0000-0000-0002-000000000001', NOW()),
('e3000000-0006-0000-0000-000000000002', 'e3000000-0000-0003-0000-000000000001', 'SIGESA-CEUB-2026-F02', 'Plan de Estudios y Malla Curricular Oficial de Informática', 'dossier_ceub_area2_malla.pdf', encode(sha256('dossier_ceub_area2_malla'::bytea), 'hex'), FALSE, 'e3000000-0000-0000-0002-000000000001', NOW()),
('e3000000-0006-0000-0000-000000000003', 'e3000000-0000-0003-0000-000000000001', 'SIGESA-CEUB-2026-F03', 'Manual de Organización y Funciones de la Carrera de Informática', 'dossier_ceub_area3_mof.pdf', encode(sha256('dossier_ceub_area3_mof'::bytea), 'hex'), FALSE, 'e3000000-0000-0000-0002-000000000001', NOW()),
('e3000000-0006-0000-0000-000000000004', 'e3000000-0000-0003-0000-000000000001', 'SIGESA-CEUB-2026-F04', 'Escalafón Docente y Actas de Concurso de Méritos Docentes (v1)', 'dossier_ceub_area4_docentes.pdf', encode(sha256('dossier_ceub_area4_docentes'::bytea), 'hex'), FALSE, 'e3000000-0000-0000-0002-000000000001', NOW()),
('e3000000-0006-0000-0000-000000000014', 'e3000000-0000-0003-0000-000000000001', 'SIGESA-CEUB-2026-F04-V2', 'Acta Facultativa de Homologación de Concurso de Méritos Docentes (Subsanación v2)', 'dossier_ceub_area4_subsanacion_acta.txt', encode(sha256('dossier_ceub_area4_subsanacion_acta'::bytea), 'hex'), FALSE, 'e3000000-0000-0000-0002-000000000001', NOW()),
('e3000000-0006-0000-0000-000000000005', 'e3000000-0000-0003-0000-000000000001', 'SIGESA-CEUB-2026-F05', 'Métricas de Admisión, Registro de Titulados y Rendimiento Estudiantil', 'dossier_ceub_area5_estudiantes.csv', encode(sha256('dossier_ceub_area5_estudiantes'::bytea), 'hex'), FALSE, 'e3000000-0000-0000-0002-000000000001', NOW()),
('e3000000-0006-0000-0000-000000000006', 'e3000000-0000-0003-0000-000000000001', 'SIGESA-CEUB-2026-F06', 'Catálogo de Proyectos de Investigación DICYT y Publicaciones Indexadas', 'dossier_ceub_area6_investigacion.txt', encode(sha256('dossier_ceub_area6_investigacion'::bytea), 'hex'), FALSE, 'e3000000-0000-0000-0002-000000000001', NOW()),
('e3000000-0006-0000-0000-000000000007', 'e3000000-0000-0003-0000-000000000001', 'SIGESA-CEUB-2026-F07', 'Convenios Marco de Interacción Social y Pasantías Profesionales', 'dossier_ceub_area7_extension.txt', encode(sha256('dossier_ceub_area7_extension'::bytea), 'hex'), FALSE, 'e3000000-0000-0000-0002-000000000001', NOW()),
('e3000000-0006-0000-0000-000000000008', 'e3000000-0000-0003-0000-000000000001', 'SIGESA-CEUB-2026-F08', 'Inventario Bibliográfico Especializado y Licencias de Software', 'dossier_ceub_area8_biblioteca.csv', encode(sha256('dossier_ceub_area8_biblioteca'::bytea), 'hex'), FALSE, 'e3000000-0000-0000-0002-000000000001', NOW()),
('e3000000-0006-0000-0000-000000000009', 'e3000000-0000-0003-0000-000000000001', 'SIGESA-CEUB-2026-F09', 'Ejecución Presupuestaria Anual por Partida Fiscal (POA)', 'dossier_ceub_area9_poa.csv', encode(sha256('dossier_ceub_area9_poa'::bytea), 'hex'), FALSE, 'e3000000-0000-0000-0002-000000000001', NOW()),
('e3000000-0006-0000-0000-000000000010', 'e3000000-0000-0003-0000-000000000001', 'SIGESA-CEUB-2026-F10', 'Inventario Técnico de Estaciones y Servidores de Laboratorio', 'dossier_ceub_area10_laboratorios.csv', encode(sha256('dossier_ceub_area10_laboratorios'::bytea), 'hex'), FALSE, 'e3000000-0000-0000-0002-000000000001', NOW());

INSERT INTO indicator_evidence_links (id, indicator_id, document_asset_id, evidence_id, is_primary_reference, created_at)
SELECT
    ('e3000000-0007-0000-0000-' || LPAD(i::text, 12, '0'))::uuid,
    ('e3000000-0004-0000-0000-' || LPAD(i::text, 12, '0'))::uuid,
    ('e3000000-0006-0000-0000-' || LPAD(i::text, 12, '0'))::uuid,
    ('e3000000-0005-0000-0000-' || LPAD(i::text, 12, '0'))::uuid,
    TRUE,
    NOW()
FROM generate_series(1, 10) AS i;

-- Enlace adicional para el activo subsanado V2 en el Área 4
INSERT INTO indicator_evidence_links (id, indicator_id, document_asset_id, evidence_id, is_primary_reference, created_at)
VALUES (
    'e3000000-0007-0000-0000-000000000014',
    'e3000000-0004-0000-0000-000000000004',
    'e3000000-0006-0000-0000-000000000014',
    'e3000000-0005-0000-0000-000000000004',
    FALSE,
    NOW()
);

-- 7.3 Evidencias y DocumentAssets para las 4 Dimensiones ARCU-SUR (Bioquímica y Farmacia)
INSERT INTO evidence (id, indicator_id, normative_indicator_id, latest_version_id, created_at)
SELECT
    ('e3000000-0005-0000-0000-' || LPAD((20 + i)::text, 12, '0'))::uuid,
    ('e3000000-0004-0000-0000-' || LPAD((20 + i)::text, 12, '0'))::uuid,
    ('e3000000-0004-0000-0000-' || LPAD((20 + i)::text, 12, '0'))::uuid,
    ('e3000000-0009-0000-0000-' || LPAD((20 + i)::text, 12, '0'))::uuid,
    NOW() - (i || ' days')::interval
FROM generate_series(1, 4) AS i;

INSERT INTO document_assets (id, process_id, folio_code, title, storage_key, content_hash, is_institutional, uploaded_by, created_at)
VALUES
('e3000000-0006-0000-0000-000000000021', 'e3000000-0000-0003-0000-000000000003', 'SIGESA-ARCU-2026-F01', 'Plan Estratégico Quinquenal y Convenios de Movilidad MERCOSUR', 'dossier_arcusur_dim1_estrategico.pdf', encode(sha256('dossier_arcusur_dim1_estrategico'::bytea), 'hex'), TRUE, 'e3000000-0000-0000-0002-000000000003', NOW()),
('e3000000-0006-0000-0000-000000000022', 'e3000000-0000-0003-0000-000000000003', 'SIGESA-ARCU-2026-F02', 'Tabla de Créditos y Equivalencias Regionales MERCOSUR', 'dossier_arcusur_dim2_malla.csv', encode(sha256('dossier_arcusur_dim2_malla'::bytea), 'hex'), FALSE, 'e3000000-0000-0000-0002-000000000003', NOW()),
('e3000000-0006-0000-0000-000000000023', 'e3000000-0000-0003-0000-000000000003', 'SIGESA-ARCU-2026-F03', 'Padrón Docente con Grados de Postgrado y Encuestas de Egresados', 'dossier_arcusur_dim3_docentes.txt', encode(sha256('dossier_arcusur_dim3_docentes'::bytea), 'hex'), FALSE, 'e3000000-0000-0000-0002-000000000003', NOW()),
('e3000000-0006-0000-0000-000000000024', 'e3000000-0000-0003-0000-000000000003', 'SIGESA-ARCU-2026-F04', 'Certificación de Bioseguridad SEDES/AGEMED y Equipamiento Clínico', 'dossier_arcusur_dim4_laboratorios.pdf', encode(sha256('dossier_arcusur_dim4_laboratorios'::bytea), 'hex'), FALSE, 'e3000000-0000-0000-0002-000000000003', NOW());

INSERT INTO indicator_evidence_links (id, indicator_id, document_asset_id, evidence_id, is_primary_reference, created_at)
SELECT
    ('e3000000-0007-0000-0000-' || LPAD((20 + i)::text, 12, '0'))::uuid,
    ('e3000000-0004-0000-0000-' || LPAD((20 + i)::text, 12, '0'))::uuid,
    ('e3000000-0006-0000-0000-' || LPAD((20 + i)::text, 12, '0'))::uuid,
    ('e3000000-0005-0000-0000-' || LPAD((20 + i)::text, 12, '0'))::uuid,
    TRUE,
    NOW()
FROM generate_series(1, 4) AS i;

-- 7.4 Registro de Observaciones en Indicadores (indicator_observation)
-- 1 Observación resuelta (Área 4), 1 abierta (Área 9), 1 abierta (ARCU Dim 4)
INSERT INTO indicator_observation (
    id, author_id, author_role, body, created_at, resolved_at, resolved_version_id, status, updated_at, indicator_id
) VALUES
    ('e3000000-0008-0000-0000-000000000001', 'e3000000-0000-0000-0002-000000000005', 'EE', 
     'Discrepancia financiera detectada: La ejecución en la partida 200 no coincide con el balance SIGMA-UMSS. Se requiere certificación complementaria de la DAF.',
     NOW() - INTERVAL '3 days', NULL, NULL, 'OPEN', NOW() - INTERVAL '3 days', 'e3000000-0004-0000-0000-000000000009'),
    ('e3000000-0008-0000-0000-000000000002', 'e3000000-0000-0000-0002-000000000006', 'CAE_MEMBER', 
     'Observación de bioseguridad: Los laboratorios farmacéuticos requieren certificación Nivel 2 del SEDES y resolución sanitaria AGEMED.',
     NOW() - INTERVAL '5 days', NULL, NULL, 'OPEN', NOW() - INTERVAL '5 days', 'e3000000-0004-0000-0000-000000000024'),
    ('e3000000-0008-0000-0000-000000000003', 'e3000000-0000-0000-0002-000000000005', 'EE', 
     'Falta adjuntar resolución del H. Consejo Facultativo de ratificación de docentes ordinarios del concurso de méritos.',
     NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 hours', 'e3000000-0009-0000-0000-000000000014', 'RESOLVED', NOW() - INTERVAL '6 hours', 'e3000000-0004-0000-0000-000000000004');

-- 7.5 Historial de Versiones de Evidencias en el Silo (evidence_version)
-- Versiones 1 para todos los indicadores
INSERT INTO evidence_version (
    id, content_hash, created_at, created_by, criterion_id, description, evidence_id, storage_key, version_number, external_url, indicator_observation_id, observation_id, original_filename, supersedes_version_number, blob_purged
) VALUES
('e3000000-0009-0000-0000-000000000001', encode(sha256('dossier_ceub_area1_estatuto'::bytea), 'hex'), NOW() - INTERVAL '10 days', 'e3000000-0000-0000-0002-000000000001', NULL, 'Estatuto Orgánico de la UMSS 2025', 'e3000000-0005-0000-0000-000000000001', 'dossier_ceub_area1_estatuto.pdf', 1, NULL, NULL, NULL, 'dossier_ceub_area1_estatuto.pdf', NULL, FALSE),
('e3000000-0009-0000-0000-000000000002', encode(sha256('dossier_ceub_area2_malla'::bytea), 'hex'), NOW() - INTERVAL '9 days', 'e3000000-0000-0000-0002-000000000001', NULL, 'Malla Curricular Oficial de Informática', 'e3000000-0005-0000-0000-000000000002', 'dossier_ceub_area2_malla.pdf', 1, NULL, NULL, NULL, 'dossier_ceub_area2_malla.pdf', NULL, FALSE),
('e3000000-0009-0000-0000-000000000003', encode(sha256('dossier_ceub_area3_mof'::bytea), 'hex'), NOW() - INTERVAL '8 days', 'e3000000-0000-0000-0002-000000000001', NULL, 'Manual de Organización y Funciones MOF', 'e3000000-0005-0000-0000-000000000003', 'dossier_ceub_area3_mof.pdf', 1, NULL, NULL, NULL, 'dossier_ceub_area3_mof.pdf', NULL, FALSE),
('e3000000-0009-0000-0000-000000000004', encode(sha256('dossier_ceub_area4_docentes'::bytea), 'hex'), NOW() - INTERVAL '7 days', 'e3000000-0000-0000-0002-000000000001', NULL, 'Escalafón Docente y Actas Preliminares (v1 observada)', 'e3000000-0005-0000-0000-000000000004', 'dossier_ceub_area4_docentes.pdf', 1, NULL, NULL, NULL, 'dossier_ceub_area4_docentes.pdf', NULL, FALSE),
('e3000000-0009-0000-0000-000000000005', encode(sha256('dossier_ceub_area5_estudiantes'::bytea), 'hex'), NOW() - INTERVAL '6 days', 'e3000000-0000-0000-0002-000000000001', NULL, 'Métricas de Titulación y Deserción Estudiantil CSV', 'e3000000-0005-0000-0000-000000000005', 'dossier_ceub_area5_estudiantes.csv', 1, NULL, NULL, NULL, 'dossier_ceub_area5_estudiantes.csv', NULL, FALSE),
('e3000000-0009-0000-0000-000000000006', encode(sha256('dossier_ceub_area6_investigacion'::bytea), 'hex'), NOW() - INTERVAL '5 days', 'e3000000-0000-0000-0002-000000000001', NULL, 'Proyectos de Investigación DICYT TXT', 'e3000000-0005-0000-0000-000000000006', 'dossier_ceub_area6_investigacion.txt', 1, NULL, NULL, NULL, 'dossier_ceub_area6_investigacion.txt', NULL, FALSE),
('e3000000-0009-0000-0000-000000000007', encode(sha256('dossier_ceub_area7_extension'::bytea), 'hex'), NOW() - INTERVAL '4 days', 'e3000000-0000-0000-0002-000000000001', NULL, 'Convenios de Pasantías y Extensión TXT', 'e3000000-0005-0000-0000-000000000007', 'dossier_ceub_area7_extension.txt', 1, NULL, NULL, NULL, 'dossier_ceub_area7_extension.txt', NULL, FALSE),
('e3000000-0009-0000-0000-000000000008', encode(sha256('dossier_ceub_area8_biblioteca'::bytea), 'hex'), NOW() - INTERVAL '3 days', 'e3000000-0000-0000-0002-000000000001', NULL, 'Catálogo Bibliográfico y Software Educativo CSV', 'e3000000-0005-0000-0000-000000000008', 'dossier_ceub_area8_biblioteca.csv', 1, NULL, NULL, NULL, 'dossier_ceub_area8_biblioteca.csv', NULL, FALSE),
('e3000000-0009-0000-0000-000000000009', encode(sha256('dossier_ceub_area9_poa'::bytea), 'hex'), NOW() - INTERVAL '2 days', 'e3000000-0000-0000-0002-000000000001', NULL, 'Ejecución Presupuestaria Anual CSV (observada)', 'e3000000-0005-0000-0000-000000000009', 'dossier_ceub_area9_poa.csv', 1, NULL, NULL, NULL, 'dossier_ceub_area9_poa.csv', NULL, FALSE),
('e3000000-0009-0000-0000-000000000010', encode(sha256('dossier_ceub_area10_laboratorios'::bytea), 'hex'), NOW() - INTERVAL '1 day', 'e3000000-0000-0000-0002-000000000001', NULL, 'Inventario de Estaciones de Cómputo CSV', 'e3000000-0005-0000-0000-000000000010', 'dossier_ceub_area10_laboratorios.csv', 1, NULL, NULL, NULL, 'dossier_ceub_area10_laboratorios.csv', NULL, FALSE),
('e3000000-0009-0000-0000-000000000021', encode(sha256('dossier_arcusur_dim1_estrategico'::bytea), 'hex'), NOW() - INTERVAL '4 days', 'e3000000-0000-0000-0002-000000000003', NULL, 'Plan Estratégico MERCOSUR PDF', 'e3000000-0005-0000-0000-000000000021', 'dossier_arcusur_dim1_estrategico.pdf', 1, NULL, NULL, NULL, 'dossier_arcusur_dim1_estrategico.pdf', NULL, FALSE),
('e3000000-0009-0000-0000-000000000022', encode(sha256('dossier_arcusur_dim2_malla'::bytea), 'hex'), NOW() - INTERVAL '3 days', 'e3000000-0000-0000-0002-000000000003', NULL, 'Malla Curricular ARCU-SUR CSV', 'e3000000-0005-0000-0000-000000000022', 'dossier_arcusur_dim2_malla.csv', 1, NULL, NULL, NULL, 'dossier_arcusur_dim2_malla.csv', NULL, FALSE),
('e3000000-0009-0000-0000-000000000023', encode(sha256('dossier_arcusur_dim3_docentes'::bytea), 'hex'), NOW() - INTERVAL '2 days', 'e3000000-0000-0000-0002-000000000003', NULL, 'Padrón Docente MERCOSUR TXT', 'e3000000-0005-0000-0000-000000000023', 'dossier_arcusur_dim3_docentes.txt', 1, NULL, NULL, NULL, 'dossier_arcusur_dim3_docentes.txt', NULL, FALSE),
('e3000000-0009-0000-0000-000000000024', encode(sha256('dossier_arcusur_dim4_laboratorios'::bytea), 'hex'), NOW() - INTERVAL '1 day', 'e3000000-0000-0000-0002-000000000003', NULL, 'Bioseguridad en Laboratorios PDF (observada)', 'e3000000-0005-0000-0000-000000000024', 'dossier_arcusur_dim4_laboratorios.pdf', 1, NULL, NULL, NULL, 'dossier_arcusur_dim4_laboratorios.pdf', NULL, FALSE),
-- Versión 2 Subsanada para el Área 4 (Escalafón Docente)
('e3000000-0009-0000-0000-000000000014', encode(sha256('dossier_ceub_area4_subsanacion_acta'::bytea), 'hex'), NOW() - INTERVAL '6 hours', 'e3000000-0000-0000-0002-000000000001', NULL, 'Subsanación: Acta facultativa homologada de concurso de méritos docentes', 'e3000000-0005-0000-0000-000000000004', 'dossier_ceub_area4_subsanacion_acta.txt', 2, NULL, 'e3000000-0008-0000-0000-000000000003', NULL, 'dossier_ceub_area4_subsanacion_acta.txt', 1, FALSE);

-- =============================================================================
-- 8. OBSERVACIONES Y ALERTAS (tb_observation & notification_outbox)
-- =============================================================================
DELETE FROM tb_observation WHERE observation_id LIKE 'OBS-E3-%';

INSERT INTO tb_observation (
    observation_id, program_id, indicator_id, indicator_code, indicator_title, description, issue_date, due_date, status, remediation_url
) VALUES
    ('OBS-E3-001', 'e3000000-0000-0000-0001-000000000001', 'e3000000-0004-0000-0000-000000000001', 'CEUB-IND-1.1', 'Normativa y Estatuto Orgánico', 'Resolución facultativa ratificada conforme a normativa CEUB.', CURRENT_DATE - 15, CURRENT_DATE - 2, 'APROBADO', '/processes/e3000000-0000-0003-0000-000000000001/indicators/101'),
    ('OBS-E3-002', 'e3000000-0000-0000-0001-000000000001', 'e3000000-0004-0000-0000-000000000004', 'CEUB-IND-4.1', 'Escalafón y Concursos Docentes', 'Acta aclaratoria RCF 342/2026 cargada en versión 2; expediente en nueva revisión técnica.', CURRENT_DATE - 5, CURRENT_DATE + 3, 'EN_REVISION', '/processes/e3000000-0000-0003-0000-000000000001/indicators/104'),
    ('OBS-E3-003', 'e3000000-0000-0000-0001-000000000003', 'e3000000-0004-0000-0000-000000000024', 'ARCU-IND-4.1', 'Certificación de Bioseguridad en Laboratorios', 'Los manuales de manejo de reactivos y desinfección requieren certificación actualizada del SEDES Cochabamba.', CURRENT_DATE - 7, CURRENT_DATE + 2, 'PENDIENTE_SUBSANACION', '/processes/e3000000-0000-0003-0000-000000000003/summary'),
    ('OBS-E3-004', 'e3000000-0000-0000-0001-000000000001', 'e3000000-0004-0000-0000-000000000010', 'CEUB-IND-10.1', 'Laboratorios de Computación y Equipamiento', 'Protocolos de mantenimiento preventivo y software libre verificados satisfactoriamente.', CURRENT_DATE - 10, CURRENT_DATE - 1, 'APROBADO', '/processes/e3000000-0000-0003-0000-000000000001/indicators/110'),
    ('OBS-E3-005', 'e3000000-0000-0000-0001-000000000001', 'e3000000-0004-0000-0000-000000000009', 'CEUB-IND-9.1', 'Ejecución Presupuestaria de Carrera (POA)', 'Discrepancia en partida 200 respecto al balance SIGMA-UMSS. Adjuntar certificación emitida por DAF.', CURRENT_DATE - 2, CURRENT_DATE + 1, 'PENDIENTE_SUBSANACION', '/processes/e3000000-0000-0003-0000-000000000001/indicators/109'),
    ('OBS-E3-006', 'e3000000-0000-0000-0001-000000000002', 'e3000000-0004-0000-0000-000000000002', 'CEUB-IND-2.1', 'Malla Curricular Electromecánica', 'Actualización de carga horaria en laboratorio de alta tensión conforme a estándares regionales.', CURRENT_DATE - 12, CURRENT_DATE + 4, 'EN_REVISION', '/processes/e3000000-0000-0003-0000-000000000002/indicators/102'),
    ('OBS-E3-007', 'e3000000-0000-0000-0001-000000000004', 'e3000000-0004-0000-0000-000000000003', 'CEUB-IND-3.1', 'Clínica Odontológica Docente-Asistencial', 'Verificar cumplimiento de protocolos de esterilización y autoclave en sillones odontológicos.', CURRENT_DATE - 8, CURRENT_DATE + 5, 'OBSERVADO', '/processes/e3000000-0000-0003-0000-000000000004/indicators/103'),
    ('OBS-E3-008', 'e3000000-0000-0000-0001-000000000007', 'e3000000-0004-0000-0000-000000000006', 'CEUB-IND-6.1', 'Producción Científica en Computación y Sistemas', 'Registrar convenios de pasantías de investigación vigentes con el Centro de Cómputo.', CURRENT_DATE - 6, CURRENT_DATE + 8, 'APROBADO', '/processes/e3000000-0000-0003-0000-000000000005/indicators/106'),
    ('OBS-E3-009', 'e3000000-0000-0000-0001-000000000008', 'e3000000-0004-0000-0000-000000000010', 'CEUB-IND-10.1', 'Laboratorio de Resistencia de Materiales y Suelos', 'Certificación de calibración de prensa hidráulica emitida por IBMETRO pendiente de carga.', CURRENT_DATE - 4, CURRENT_DATE + 2, 'PENDIENTE_SUBSANACION', '/processes/e3000000-0000-0003-0000-000000000006/indicators/110'),
    ('OBS-E3-010', 'e3000000-0000-0000-0001-000000000009', 'e3000000-0004-0000-0000-000000000021', 'ARCU-IND-1.1', 'Convenios de Internado Rotatorio y Campos Clínicos', 'Ratificar actas de rotación hospitalaria en Hospital Viedma y Maternológico German Urquidi.', CURRENT_DATE - 9, CURRENT_DATE - 1, 'APROBADO', '/processes/e3000000-0000-0003-0000-000000000007/indicators/101');

-- 8.1 Notificaciones Outbox - Limpieza general del bloque E3
DELETE FROM notification_outbox WHERE id >= 'e3000000-0000-0000-0000-000000000001' AND id <= 'e3000000-0000-0000-0000-ffffffffffff';

-- 8.2 Notificaciones críticas iniciales (PENDING)
INSERT INTO notification_outbox (
    id, event_type, recipient_id, recipient_email, subject, body, deep_link, status, retry_count, max_retries, next_retry_at, created_at, sent_at
) VALUES
    ('e3000000-0000-0000-0000-000000000001', 'ALERT_DEADLINE_IMMINENT', 'e3000000-0000-0000-0002-000000000001', 'coord.informatica@umss.edu.bo', 'ALERTA: Vencimiento de plazo para subsanación (Lic. en Informática)', 'La observación OBS-E3-001 sobre el indicador CEUB 1.1 vence en menos de 48 horas. Cargue el documento resolutivo a la brevedad.', '/processes/e3000000-0000-0003-0000-000000000001/indicators/101', 'PENDING', 0, 5, NULL, NOW() - INTERVAL '1 hour', NULL),
    ('e3000000-0000-0000-0000-000000000002', 'ALERT_DEADLINE_IMMINENT', 'e3000000-0000-0000-0002-000000000002', 'coord.electromecanica@umss.edu.bo', 'ALERTA URGENTE: Observación crítica en laboratorios eléctricos (Ing. Electromecánica)', 'El plazo para regularizar las actas de calibración vence mañana. Su proceso de acreditación podría quedar bloqueado.', '/processes/e3000000-0000-0003-0000-000000000002/indicators/201', 'PENDING', 0, 5, NULL, NOW() - INTERVAL '30 minutes', NULL),
    ('e3000000-0000-0000-0000-000000000003', 'ALERT_PEER_REVIEW_COMMENCING', 'e3000000-0000-0000-0002-000000000003', 'coord.bioquimica@umss.edu.bo', 'Notificación: Inicio de evaluación por comité de pares ARCU-SUR (Bioquímica y Farmacia)', 'El comité externo asignado iniciará la revisión documental de laboratorios y bioseguridad a partir del lunes.', '/processes/e3000000-0000-0003-0000-000000000003/summary', 'PENDING', 0, 5, NULL, NOW() - INTERVAL '2 hours', NULL),
    ('e3000000-0000-0000-0000-000000000004', 'ALERT_PROCESS_OPENED', 'e3000000-0000-0000-0002-000000000004', 'coord.odontologia@umss.edu.bo', 'Apertura formal de proceso de acreditación CEUB 2026 (Odontología)', 'Se ha dado inicio oficial al proceso de autoevaluación para la carrera de Odontología de la UMSS.', '/processes/e3000000-0000-0003-0000-000000000004', 'PENDING', 0, 5, NULL, NOW() - INTERVAL '5 hours', NULL);

-- 8.3 Lote masivo de notificaciones PENDING (39,996 registros adicionales -> 40,000 en total)
INSERT INTO notification_outbox (
    id, event_type, recipient_id, recipient_email, subject, body, deep_link, status, retry_count, max_retries, next_retry_at, created_at, sent_at
)
SELECT
    ('e3000000-0000-0000-0000-' || LPAD((4 + i)::text, 12, '0'))::uuid,
    CASE (i % 8)
        WHEN 0 THEN 'INDICATOR_DEADLINE_APPROACHING'
        WHEN 1 THEN 'EVIDENCE_SUBMITTED_FOR_REVIEW'
        WHEN 2 THEN 'EVIDENCE_REJECTED'
        WHEN 3 THEN 'PEER_REVIEW_ASSIGNED'
        WHEN 4 THEN 'AUTOEVAL_MILESTONE_EXPIRED'
        WHEN 5 THEN 'STAGE_TRANSITION_COMMENCED'
        WHEN 6 THEN 'OBSERVATION_CRITICAL_DEADLINE'
        ELSE 'REMEDIATION_ACTION_DUE'
    END,
    ('e3000000-0000-0000-0002-' || LPAD((1 + (i % 15))::text, 12, '0'))::uuid,
    CASE (i % 15)
        WHEN 0 THEN 'coord.informatica@umss.edu.bo'
        WHEN 1 THEN 'coord.electromecanica@umss.edu.bo'
        WHEN 2 THEN 'coord.bioquimica@umss.edu.bo'
        WHEN 3 THEN 'coord.odontologia@umss.edu.bo'
        WHEN 4 THEN 'evaluador.e3@umss.edu.bo'
        WHEN 5 THEN 'cae.e3@umss.edu.bo'
        WHEN 6 THEN 'coord.sistemas@umss.edu.bo'
        WHEN 7 THEN 'coord.civil@umss.edu.bo'
        WHEN 8 THEN 'coord.medicina@umss.edu.bo'
        WHEN 9 THEN 'coord.admin@umss.edu.bo'
        WHEN 10 THEN 'coord.derecho@umss.edu.bo'
        WHEN 11 THEN 'coord.industrial@umss.edu.bo'
        WHEN 12 THEN 'coord.enfermeria@umss.edu.bo'
        WHEN 13 THEN 'coord.arquitectura@umss.edu.bo'
        ELSE 'coord.economia@umss.edu.bo'
    END,
    'Notificación de acreditación SIGESA #' || (4 + i),
    'Detalle del evento generado para seguimiento del criterio y evidencia #' || (4 + i) || ' en el marco del proceso de autoevaluación.',
    '/processes/e3000000-0000-0003-0000-000000000001/indicators/' || (100 + (i % 20)),
    'PENDING',
    0,
    5,
    NULL,
    NOW() - ((i % 1440) || ' minutes')::interval,
    NULL
FROM generate_series(1, 39996) AS i;

-- 8.4 Lote masivo de notificaciones SENT (100,000 registros históricos)
INSERT INTO notification_outbox (
    id, event_type, recipient_id, recipient_email, subject, body, deep_link, status, retry_count, max_retries, next_retry_at, created_at, sent_at
)
SELECT
    ('e3000000-0000-0000-0000-' || LPAD((40000 + i)::text, 12, '0'))::uuid,
    CASE (i % 5)
        WHEN 0 THEN 'EVIDENCE_APPROVED'
        WHEN 1 THEN 'INDICATOR_COMPLETED'
        WHEN 2 THEN 'STAGE_TRANSITION'
        WHEN 3 THEN 'REPORT_GENERATED'
        ELSE 'ACCREDITATION_CONFIRMED'
    END,
    ('e3000000-0000-0000-0002-' || LPAD((1 + (i % 15))::text, 12, '0'))::uuid,
    CASE (i % 15)
        WHEN 0 THEN 'coord.informatica@umss.edu.bo'
        WHEN 1 THEN 'coord.electromecanica@umss.edu.bo'
        WHEN 2 THEN 'coord.bioquimica@umss.edu.bo'
        WHEN 3 THEN 'coord.odontologia@umss.edu.bo'
        WHEN 4 THEN 'evaluador.e3@umss.edu.bo'
        WHEN 5 THEN 'cae.e3@umss.edu.bo'
        WHEN 6 THEN 'coord.sistemas@umss.edu.bo'
        WHEN 7 THEN 'coord.civil@umss.edu.bo'
        WHEN 8 THEN 'coord.medicina@umss.edu.bo'
        WHEN 9 THEN 'coord.admin@umss.edu.bo'
        WHEN 10 THEN 'coord.derecho@umss.edu.bo'
        WHEN 11 THEN 'coord.industrial@umss.edu.bo'
        WHEN 12 THEN 'coord.enfermeria@umss.edu.bo'
        WHEN 13 THEN 'coord.arquitectura@umss.edu.bo'
        ELSE 'coord.economia@umss.edu.bo'
    END,
    'Confirmación de envío exitoso #' || (40000 + i),
    'La evidencia ha sido procesada y aprobada correctamente en la plataforma SIGESA.',
    '/processes/e3000000-0000-0003-0000-000000000001/summary',
    'SENT',
    0,
    5,
    NULL,
    NOW() - ((i % 90) || ' days')::interval - ((i % 24) || ' hours')::interval,
    NOW() - ((i % 90) || ' days')::interval - (((i % 24) - 1) || ' hours')::interval
FROM generate_series(1, 100000) AS i;

-- 8.5 Lote de notificaciones FAILED (7,000 registros con reintentos agotados)
INSERT INTO notification_outbox (
    id, event_type, recipient_id, recipient_email, subject, body, deep_link, status, retry_count, max_retries, next_retry_at, created_at, sent_at
)
SELECT
    ('e3000000-0000-0000-0000-' || LPAD((140000 + i)::text, 12, '0'))::uuid,
    'SMTP_DISPATCH_ALERT',
    ('e3000000-0000-0000-0002-000000000005')::uuid,
    'invalid_user_' || i || '@umss.edu.bo',
    'Falla en despacho SMTP #' || (140000 + i),
    'Error persistente al despachar notificación electrónica tras agotar 5 reintentos máximos.',
    '/admin/notifications/failed',
    'FAILED',
    5,
    5,
    NULL,
    NOW() - ((i % 30) || ' days')::interval,
    NULL
FROM generate_series(1, 7000) AS i;

-- 8.6 Lote de notificaciones RETRYING (3,000 registros pendientes en reintento)
INSERT INTO notification_outbox (
    id, event_type, recipient_id, recipient_email, subject, body, deep_link, status, retry_count, max_retries, next_retry_at, created_at, sent_at
)
SELECT
    ('e3000000-0000-0000-0000-' || LPAD((147000 + i)::text, 12, '0'))::uuid,
    'EVIDENCE_REVIEW_EXPEDITE',
    ('e3000000-0000-0000-0002-' || LPAD((1 + (i % 15))::text, 12, '0'))::uuid,
    'coord.informatica@umss.edu.bo',
    'Reintento de despacho de alerta #' || (147000 + i),
    'Intento de entrega diferido por saturación temporal del servidor de correo institucional.',
    '/processes/e3000000-0000-0003-0000-000000000001/indicators/104',
    'PENDING',
    (1 + (i % 4)),
    5,
    NOW() + ((i % 60) || ' minutes')::interval,
    NOW() - ((i % 120) || ' minutes')::interval,
    NULL
FROM generate_series(1, 3000) AS i;
