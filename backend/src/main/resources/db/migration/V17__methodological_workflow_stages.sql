-- Release 2.1.0 — Workflow metodológico (ADR-0005 M6)
-- Etapas metodológicas, entregables E1–E2, compuertas, encuestas primarias.

-- ---------------------------------------------------------------------------
-- 1. Extensión accreditation_processes
-- ---------------------------------------------------------------------------

ALTER TABLE accreditation_processes
    ADD COLUMN IF NOT EXISTS operational_mode VARCHAR(32) NOT NULL DEFAULT 'ACTIVE';

ALTER TABLE accreditation_processes
    ADD COLUMN IF NOT EXISTS current_stage_id UUID;

ALTER TABLE accreditation_processes
    DROP CONSTRAINT IF EXISTS chk_accreditation_processes_operational_mode;

ALTER TABLE accreditation_processes
    ADD CONSTRAINT chk_accreditation_processes_operational_mode
        CHECK (operational_mode IN ('ACTIVE', 'AUDIT_READONLY', 'IMPROVEMENT_MONITORING'));

CREATE INDEX IF NOT EXISTS idx_accreditation_processes_current_stage
    ON accreditation_processes (current_stage_id);

-- ---------------------------------------------------------------------------
-- 2. Etapas metodológicas
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS methodological_stages (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    process_id      UUID NOT NULL REFERENCES accreditation_processes(id) ON DELETE CASCADE,
    stage_order     INT NOT NULL,
    code            VARCHAR(32) NOT NULL,
    status          VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    started_at      TIMESTAMPTZ,
    closed_at       TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_methodological_stages_process_order UNIQUE (process_id, stage_order),
    CONSTRAINT uk_methodological_stages_process_code UNIQUE (process_id, code),
    CONSTRAINT chk_methodological_stages_order CHECK (stage_order BETWEEN 1 AND 7),
    CONSTRAINT chk_methodological_stages_code CHECK (code IN (
        'PREPARATORY', 'COLLECTION', 'SYSTEMATIZATION', 'DRAFTING',
        'PRESENTATION', 'EXTERNAL_PREP', 'POST_ACCREDITATION'
    )),
    CONSTRAINT chk_methodological_stages_status CHECK (status IN (
        'PENDING', 'IN_PROGRESS', 'SUBMITTED_FOR_REVIEW', 'OBSERVED', 'APPROVED'
    ))
);

CREATE INDEX IF NOT EXISTS idx_methodological_stages_process
    ON methodological_stages (process_id);

-- FK current_stage_id (después de crear la tabla)
ALTER TABLE accreditation_processes
    DROP CONSTRAINT IF EXISTS fk_accreditation_processes_current_stage;

ALTER TABLE accreditation_processes
    ADD CONSTRAINT fk_accreditation_processes_current_stage
        FOREIGN KEY (current_stage_id) REFERENCES methodological_stages(id);

-- ---------------------------------------------------------------------------
-- 3. Entregables por etapa
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS stage_deliverables (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    stage_id                UUID NOT NULL REFERENCES methodological_stages(id) ON DELETE CASCADE,
    deliverable_code        VARCHAR(64) NOT NULL,
    approval_status         VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    document_asset_id       UUID,
    approved_by             UUID REFERENCES app_users(id),
    technical_observations  TEXT,
    approved_at             TIMESTAMPTZ,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_stage_deliverables_stage_code UNIQUE (stage_id, deliverable_code),
    CONSTRAINT chk_stage_deliverables_approval CHECK (approval_status IN (
        'PENDING', 'APPROVED', 'REJECTED'
    ))
);

CREATE INDEX IF NOT EXISTS idx_stage_deliverables_stage
    ON stage_deliverables (stage_id);

-- ---------------------------------------------------------------------------
-- 4. Evaluaciones de compuerta (append-only)
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS stage_gate_evaluations (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    stage_id            UUID NOT NULL REFERENCES methodological_stages(id) ON DELETE CASCADE,
    gate_rules_snapshot JSONB NOT NULL DEFAULT '{}',
    result              VARCHAR(16) NOT NULL,
    block_reason        TEXT,
    evaluated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    evaluated_by        UUID REFERENCES app_users(id),
    CONSTRAINT chk_stage_gate_evaluations_result CHECK (result IN ('PASS', 'BLOCK'))
);

CREATE INDEX IF NOT EXISTS idx_stage_gate_evaluations_stage
    ON stage_gate_evaluations (stage_id, evaluated_at DESC);

-- ---------------------------------------------------------------------------
-- 5. Encuestas primarias (Etapa 2)
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS primary_survey_batches (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    process_id          UUID NOT NULL REFERENCES accreditation_processes(id) ON DELETE CASCADE,
    audience            VARCHAR(32) NOT NULL,
    responses_count     INT NOT NULL DEFAULT 0,
    document_asset_id   UUID,
    registered_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_primary_survey_batches_audience CHECK (audience IN (
        'STUDENTS', 'FACULTY', 'GRADUATES', 'EMPLOYERS', 'STAFF'
    ))
);

CREATE INDEX IF NOT EXISTS idx_primary_survey_batches_process
    ON primary_survey_batches (process_id);

-- ---------------------------------------------------------------------------
-- 6. Backfill procesos existentes (7 etapas + entregables E1/E2)
-- ---------------------------------------------------------------------------

DO $$
DECLARE
    proc RECORD;
    stage_id UUID;
    e1_codes TEXT[] := ARRAY[
        'HCC_RESOLUTION', 'WORK_SCHEDULE', 'PRIOR_RECOMMENDATIONS_REPORT', 'BUDGET_FORECAST'
    ];
    e2_codes TEXT[] := ARRAY[
        'SECONDARY_EVIDENCE_COMPLETE', 'PRIMARY_SURVEY_REPORT', 'COLLECTION_GAP_REPORT'
    ];
    code TEXT;
BEGIN
    FOR proc IN SELECT id FROM accreditation_processes LOOP
        IF NOT EXISTS (
            SELECT 1 FROM methodological_stages ms WHERE ms.process_id = proc.id
        ) THEN
            -- E1 PREPARATORY (IN_PROGRESS)
            INSERT INTO methodological_stages (process_id, stage_order, code, status, started_at)
            VALUES (proc.id, 1, 'PREPARATORY', 'IN_PROGRESS', now())
            RETURNING id INTO stage_id;

            UPDATE accreditation_processes
            SET current_stage_id = stage_id, operational_mode = 'ACTIVE'
            WHERE id = proc.id;

            FOREACH code IN ARRAY e1_codes LOOP
                INSERT INTO stage_deliverables (stage_id, deliverable_code)
                VALUES (stage_id, code);
            END LOOP;

            -- E2 COLLECTION (PENDING)
            INSERT INTO methodological_stages (process_id, stage_order, code, status)
            VALUES (proc.id, 2, 'COLLECTION', 'PENDING')
            RETURNING id INTO stage_id;

            FOREACH code IN ARRAY e2_codes LOOP
                INSERT INTO stage_deliverables (stage_id, deliverable_code)
                VALUES (stage_id, code);
            END LOOP;

            -- E3–E7 PENDING
            INSERT INTO methodological_stages (process_id, stage_order, code, status) VALUES
                (proc.id, 3, 'SYSTEMATIZATION', 'PENDING'),
                (proc.id, 4, 'DRAFTING', 'PENDING'),
                (proc.id, 5, 'PRESENTATION', 'PENDING'),
                (proc.id, 6, 'EXTERNAL_PREP', 'PENDING'),
                (proc.id, 7, 'POST_ACCREDITATION', 'PENDING');
        END IF;
    END LOOP;
END $$;
