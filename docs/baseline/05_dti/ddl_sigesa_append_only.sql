-- SIGESA / AcredIA — DDL PostgreSQL append-only (Dorada v1.0)
-- Timestamp: 2026-05-16T16:06:15-04:00
-- Invariantes: sin DELETE normativo; FK ON DELETE RESTRICT; versionado en evidence_version;
-- transiciones de Indicator por INSERT en indicator_state_history, sin UPDATE de estado.
-- Referencia: docs/05_dti/modelo_datos.md

BEGIN;

-- ---------------------------------------------------------------------------
-- Extensiones
-- ---------------------------------------------------------------------------
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ---------------------------------------------------------------------------
-- Tipos enumerados
-- ---------------------------------------------------------------------------
CREATE TYPE entity_status AS ENUM ('ACTIVO', 'ANULADO');
CREATE TYPE indicator_status AS ENUM (
  'PENDIENTE', 'SUBIDO', 'OBSERVADO', 'SUBSANADO', 'APROBADO'
);
CREATE TYPE phase_status AS ENUM ('ABIERTA', 'COMPLETADA');
CREATE TYPE process_status AS ENUM ('EN_PROCESO', 'ACREDITADO', 'VENCIDO');
CREATE TYPE template_status AS ENUM ('ACTIVO', 'ARCHIVADO');
CREATE TYPE actor_role AS ENUM ('CC', 'TD', 'JD', 'SYSTEM');
CREATE TYPE modality_type AS ENUM ('CEUB', 'ARCU_SUR');

-- ---------------------------------------------------------------------------
-- Maestros
-- ---------------------------------------------------------------------------
CREATE TABLE faculty (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code          VARCHAR(16) NOT NULL UNIQUE,
  name          VARCHAR(255) NOT NULL,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE academic_program (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  faculty_id    UUID NOT NULL REFERENCES faculty(id) ON DELETE RESTRICT,
  code          VARCHAR(32) NOT NULL UNIQUE,
  name          VARCHAR(255) NOT NULL,
  estado        entity_status NOT NULL DEFAULT 'ACTIVO',
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE app_user (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email         VARCHAR(255) NOT NULL UNIQUE,
  display_name  VARCHAR(255) NOT NULL,
  estado        entity_status NOT NULL DEFAULT 'ACTIVO',
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT app_user_email_umss CHECK (email LIKE '%@umss.edu.bo')
);

CREATE TABLE user_program_assignment (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id       UUID NOT NULL REFERENCES app_user(id) ON DELETE RESTRICT,
  program_id    UUID REFERENCES academic_program(id) ON DELETE RESTRICT,
  role_code     actor_role NOT NULL,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by    UUID NOT NULL REFERENCES app_user(id) ON DELETE RESTRICT,
  created_by_role actor_role NOT NULL,
  UNIQUE (user_id, program_id, role_code)
);

-- ---------------------------------------------------------------------------
-- Plantilla normativa
-- ---------------------------------------------------------------------------
CREATE TABLE accreditation_template (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  modality      modality_type NOT NULL,
  version       INTEGER NOT NULL,
  estado        template_status NOT NULL DEFAULT 'ACTIVO',
  activated_at  TIMESTAMPTZ,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by    UUID NOT NULL REFERENCES app_user(id) ON DELETE RESTRICT,
  created_by_role actor_role NOT NULL DEFAULT 'JD',
  UNIQUE (modality, version)
);

CREATE TABLE template_phase (
  id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  template_id           UUID NOT NULL REFERENCES accreditation_template(id) ON DELETE RESTRICT,
  sequence_no           INTEGER NOT NULL,
  name                  VARCHAR(255) NOT NULL,
  normative_deadline    DATE,
  UNIQUE (template_id, sequence_no)
);

CREATE TABLE evaluation_dimension (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  template_id   UUID NOT NULL REFERENCES accreditation_template(id) ON DELETE RESTRICT,
  code          VARCHAR(64) NOT NULL,
  name          VARCHAR(255) NOT NULL,
  UNIQUE (template_id, code)
);

CREATE TABLE evaluation_criterion (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  dimension_id  UUID NOT NULL REFERENCES evaluation_dimension(id) ON DELETE RESTRICT,
  code          VARCHAR(64) NOT NULL,
  description   TEXT NOT NULL,
  UNIQUE (dimension_id, code)
);

CREATE TABLE indicator_catalog (
  id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  template_phase_id UUID NOT NULL REFERENCES template_phase(id) ON DELETE RESTRICT,
  criterion_id      UUID NOT NULL REFERENCES evaluation_criterion(id) ON DELETE RESTRICT,
  code              VARCHAR(64) NOT NULL,
  requirement_text  TEXT NOT NULL,
  UNIQUE (template_phase_id, code)
);

-- ---------------------------------------------------------------------------
-- Proceso en ejecución
-- ---------------------------------------------------------------------------
CREATE TABLE accreditation_process (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  program_id      UUID NOT NULL REFERENCES academic_program(id) ON DELETE RESTRICT,
  template_id     UUID NOT NULL REFERENCES accreditation_template(id) ON DELETE RESTRICT,
  management_year INTEGER NOT NULL,
  estado          process_status NOT NULL DEFAULT 'EN_PROCESO',
  started_on      DATE NOT NULL,
  ended_on        DATE,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by      UUID NOT NULL REFERENCES app_user(id) ON DELETE RESTRICT,
  created_by_role actor_role NOT NULL DEFAULT 'JD'
);

-- Un proceso activo por carrera + plantilla + gestión (BRD-RB-02)
CREATE UNIQUE INDEX uq_process_active_per_program
  ON accreditation_process (program_id, template_id, management_year)
  WHERE estado = 'EN_PROCESO';

CREATE TABLE phase (
  id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  process_id        UUID NOT NULL REFERENCES accreditation_process(id) ON DELETE RESTRICT,
  template_phase_id UUID NOT NULL REFERENCES template_phase(id) ON DELETE RESTRICT,
  created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by        UUID NOT NULL REFERENCES app_user(id) ON DELETE RESTRICT,
  created_by_role   actor_role NOT NULL,
  UNIQUE (process_id, template_phase_id)
);

CREATE TABLE indicator (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  phase_id      UUID NOT NULL REFERENCES phase(id) ON DELETE RESTRICT,
  catalog_id    UUID NOT NULL REFERENCES indicator_catalog(id) ON DELETE RESTRICT,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by    UUID NOT NULL REFERENCES app_user(id) ON DELETE RESTRICT,
  created_by_role actor_role NOT NULL DEFAULT 'SYSTEM',
  UNIQUE (phase_id, catalog_id)
);

CREATE INDEX idx_indicator_phase ON indicator (phase_id);

-- ---------------------------------------------------------------------------
-- Evidencia append-only
-- ---------------------------------------------------------------------------
CREATE TABLE evidence (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  indicator_id    UUID NOT NULL UNIQUE REFERENCES indicator(id) ON DELETE RESTRICT,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by      UUID NOT NULL REFERENCES app_user(id) ON DELETE RESTRICT,
  created_by_role actor_role NOT NULL
);

CREATE TABLE evidence_version (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  evidence_id     UUID NOT NULL REFERENCES evidence(id) ON DELETE RESTRICT,
  version         INTEGER NOT NULL,
  supersedes_id   UUID REFERENCES evidence_version(id) ON DELETE RESTRICT,
  storage_key     VARCHAR(512) NOT NULL,
  content_sha256  CHAR(64) NOT NULL,
  byte_size       BIGINT NOT NULL CHECK (byte_size > 0),
  mime_type       VARCHAR(128) NOT NULL,
  observation_id  UUID,
  estado          entity_status NOT NULL DEFAULT 'ACTIVO',
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by      UUID NOT NULL REFERENCES app_user(id) ON DELETE RESTRICT,
  created_by_role actor_role NOT NULL,
  UNIQUE (evidence_id, version),
  CONSTRAINT evidence_version_chain CHECK (
    (version = 1 AND supersedes_id IS NULL)
    OR (version > 1 AND supersedes_id IS NOT NULL)
  )
);

CREATE INDEX idx_evidence_version_evidence ON evidence_version (evidence_id, version DESC);
CREATE INDEX idx_evidence_version_hash ON evidence_version (content_sha256);

-- ---------------------------------------------------------------------------
-- Observaciones y transiciones
-- ---------------------------------------------------------------------------
CREATE TABLE observation (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  indicator_id    UUID NOT NULL REFERENCES indicator(id) ON DELETE RESTRICT,
  version         INTEGER NOT NULL,
  supersedes_id   UUID REFERENCES observation(id) ON DELETE RESTRICT,
  justification   TEXT NOT NULL CHECK (char_length(justification) >= 20),
  estado          entity_status NOT NULL DEFAULT 'ACTIVO',
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by      UUID NOT NULL REFERENCES app_user(id) ON DELETE RESTRICT,
  created_by_role actor_role NOT NULL DEFAULT 'TD',
  UNIQUE (indicator_id, version)
);

ALTER TABLE evidence_version
  ADD CONSTRAINT fk_evidence_version_observation
  FOREIGN KEY (observation_id) REFERENCES observation(id) ON DELETE RESTRICT;

CREATE TABLE indicator_state_history (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  indicator_id    UUID NOT NULL REFERENCES indicator(id) ON DELETE RESTRICT,
  previous_state  indicator_status NOT NULL,
  new_state       indicator_status NOT NULL,
  reason          TEXT,
  correlation_id  UUID NOT NULL,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by      UUID NOT NULL REFERENCES app_user(id) ON DELETE RESTRICT,
  created_by_role actor_role NOT NULL
);

CREATE INDEX idx_indicator_state_history_indicator
  ON indicator_state_history (indicator_id, created_at DESC);

CREATE VIEW indicator_current_view AS
  SELECT DISTINCT ON (indicator_id)
    indicator_id,
    new_state AS current_state,
    created_by_role AS last_changed_by_role,
    created_at AS last_changed_at
  FROM indicator_state_history
  ORDER BY indicator_id, created_at DESC;

CREATE TABLE phase_state_history (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  phase_id        UUID NOT NULL REFERENCES phase(id) ON DELETE RESTRICT,
  previous_state  phase_status NOT NULL,
  new_state       phase_status NOT NULL,
  reason          TEXT,
  correlation_id  UUID NOT NULL,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by      UUID NOT NULL REFERENCES app_user(id) ON DELETE RESTRICT,
  created_by_role actor_role NOT NULL DEFAULT 'SYSTEM'
);

CREATE INDEX idx_phase_state_history_phase
  ON phase_state_history (phase_id, created_at DESC);

CREATE VIEW phase_current_view AS
  SELECT DISTINCT ON (phase_id)
    phase_id,
    new_state AS current_state,
    created_by_role AS last_changed_by_role,
    created_at AS last_changed_at
  FROM phase_state_history
  ORDER BY phase_id, created_at DESC;

-- ---------------------------------------------------------------------------
-- Auditoría y notificaciones
-- ---------------------------------------------------------------------------
CREATE TABLE audit_log (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  action_code     VARCHAR(64) NOT NULL,
  entity_type     VARCHAR(32),
  entity_id       UUID,
  payload         JSONB NOT NULL DEFAULT '{}',
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by      UUID REFERENCES app_user(id) ON DELETE RESTRICT,
  created_by_role actor_role NOT NULL DEFAULT 'SYSTEM'
);

CREATE INDEX idx_audit_log_created_at ON audit_log (created_at DESC);

CREATE TABLE notification_outbox (
  id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  event_type          VARCHAR(64) NOT NULL,
  recipient_user_id   UUID NOT NULL REFERENCES app_user(id) ON DELETE RESTRICT,
  payload_json        JSONB NOT NULL,
  delivery_status     VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
  delivered_at        TIMESTAMPTZ
);

CREATE TABLE publication_snapshot (
  id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  process_id        UUID NOT NULL REFERENCES accreditation_process(id) ON DELETE RESTRICT,
  public_payload    JSONB NOT NULL,
  published_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  published_by      UUID NOT NULL REFERENCES app_user(id) ON DELETE RESTRICT,
  published_by_role actor_role NOT NULL DEFAULT 'JD'
);

-- ---------------------------------------------------------------------------
-- Función: registrar transición de indicador (append-only)
-- ---------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_indicator_transition(
  p_indicator_id UUID,
  p_to_status indicator_status,
  p_actor_id UUID,
  p_actor_role actor_role,
  p_reason TEXT DEFAULT NULL,
  p_correlation_id UUID DEFAULT gen_random_uuid()
) RETURNS VOID AS $$
DECLARE
  v_from indicator_status;
BEGIN
  IF NOT EXISTS (SELECT 1 FROM indicator WHERE id = p_indicator_id) THEN
    RAISE EXCEPTION 'INDICATOR_NOT_FOUND';
  END IF;

  SELECT current_state INTO v_from
  FROM indicator_current_view
  WHERE indicator_id = p_indicator_id;

  v_from := COALESCE(v_from, 'PENDIENTE'::indicator_status);

  INSERT INTO indicator_state_history (
    indicator_id, previous_state, new_state, reason,
    correlation_id, created_by, created_by_role
  ) VALUES (
    p_indicator_id, v_from, p_to_status, p_reason,
    p_correlation_id, p_actor_id, p_actor_role
  );
END;
$$ LANGUAGE plpgsql;

COMMIT;

-- ===========================================================================
-- RUNBOOK (migración)
-- ===========================================================================
-- Aplicar:
--   psql -U sigesa -d sigesa -f ddl_sigesa_append_only.sql
--
-- Rollback (solo entornos vacíos de desarrollo):
--   DROP SCHEMA public CASCADE; CREATE SCHEMA public;
--
-- Verificación post-migración:
--   SELECT tablename FROM pg_tables WHERE schemaname = 'public' ORDER BY 1;
--   \d evidence_version   -- confirmar supersedes_id y sin deleted_at
--
-- Política aplicación:
--   REVOKE UPDATE, DELETE ON evidence, evidence_version, observation, indicator_state_history, audit_log FROM sigesa_app;
--   REVOKE UPDATE, DELETE ON phase_state_history FROM sigesa_app;
--   Usar solo INSERT en indicator_state_history vía fn_indicator_transition.
