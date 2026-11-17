-- Release 2.1.0 — M10 freeze/Sala de Pares + M11 E7 + CAE_MEMBER (ADR-0005 / FSD-UC-030)
-- H2 de tests no usa esta migración (ddl-auto); unicidad y CHECKs se aplican en dominio/servicio.

-- ---------------------------------------------------------------------------
-- 1. Rol CAE_MEMBER (VARCHAR 20)
-- ---------------------------------------------------------------------------

ALTER TABLE app_user ALTER COLUMN role TYPE VARCHAR(20);

ALTER TABLE app_user DROP CONSTRAINT IF EXISTS app_user_role_check;

ALTER TABLE app_user ADD CONSTRAINT app_user_role_check
    CHECK (role IN ('CC', 'TD', 'JD', 'EE', 'CAE_MEMBER'));

-- ---------------------------------------------------------------------------
-- 2. process_freeze
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS process_freeze (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    process_id   UUID NOT NULL UNIQUE REFERENCES accreditation_processes(id) ON DELETE CASCADE,
    mode         VARCHAR(32) NOT NULL,
    frozen_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    frozen_by    UUID REFERENCES app_user(id),
    lifted_at    TIMESTAMPTZ,
    lifted_by    UUID REFERENCES app_user(id),
    CONSTRAINT chk_process_freeze_mode CHECK (mode IN ('AUDIT_READONLY', 'IMPROVEMENT_PARTIAL'))
);

CREATE INDEX IF NOT EXISTS idx_process_freeze_process ON process_freeze (process_id);

-- ---------------------------------------------------------------------------
-- 3. peer_room_checklist_items
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS peer_room_checklist_items (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    process_id   UUID NOT NULL REFERENCES accreditation_processes(id) ON DELETE CASCADE,
    item_code    VARCHAR(64) NOT NULL,
    label        TEXT NOT NULL,
    checked      BOOLEAN NOT NULL DEFAULT FALSE,
    checked_by   UUID REFERENCES app_user(id),
    checked_at   TIMESTAMPTZ,
    CONSTRAINT uk_peer_room_checklist_process_code UNIQUE (process_id, item_code)
);

CREATE INDEX IF NOT EXISTS idx_peer_room_checklist_process ON peer_room_checklist_items (process_id);

-- ---------------------------------------------------------------------------
-- 4. indicator_delegations
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS indicator_delegations (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    process_id    UUID NOT NULL REFERENCES accreditation_processes(id) ON DELETE CASCADE,
    user_id       UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    level1_id     UUID,
    level2_id     UUID,
    indicator_id  UUID,
    permission    VARCHAR(32) NOT NULL DEFAULT 'EVIDENCE_AND_FODA',
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_indicator_delegations_one_scope CHECK (
        (CASE WHEN level1_id IS NOT NULL THEN 1 ELSE 0 END)
        + (CASE WHEN level2_id IS NOT NULL THEN 1 ELSE 0 END)
        + (CASE WHEN indicator_id IS NOT NULL THEN 1 ELSE 0 END) = 1
    )
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_indicator_delegations_scope
    ON indicator_delegations (
        process_id,
        user_id,
        COALESCE(level1_id, '00000000-0000-0000-0000-000000000000'),
        COALESCE(level2_id, '00000000-0000-0000-0000-000000000000'),
        COALESCE(indicator_id, '00000000-0000-0000-0000-000000000000')
    );

CREATE INDEX IF NOT EXISTS idx_indicator_delegations_process ON indicator_delegations (process_id);

-- ---------------------------------------------------------------------------
-- 5. M11 — columnas de justificación / evidencia de avance
-- ---------------------------------------------------------------------------

ALTER TABLE improvement_plan_items
    ADD COLUMN IF NOT EXISTS justification TEXT;

ALTER TABLE improvement_plan_items
    ADD COLUMN IF NOT EXISTS progress_evidence_asset_id UUID;

-- ---------------------------------------------------------------------------
-- 6. Backfill entregables E5 / E6 / E7
-- ---------------------------------------------------------------------------

INSERT INTO stage_deliverables (id, stage_id, deliverable_code, approval_status, created_at, updated_at)
SELECT gen_random_uuid(), ms.id, extra.code, 'PENDING', now(), now()
FROM methodological_stages ms
CROSS JOIN (VALUES
    ('PRESENTATION', 'COMMUNITY_VALIDATION_MINUTES'),
    ('PRESENTATION', 'FORMAL_SUBMISSION_PACKAGE'),
    ('EXTERNAL_PREP', 'SIMULATION_REPORT'),
    ('EXTERNAL_PREP', 'PEER_ROOM_CHECKLIST'),
    ('POST_ACCREDITATION', 'IMPROVEMENT_PROGRESS_REPORT')
) AS extra(stage_code, code)
WHERE ms.code = extra.stage_code
  AND NOT EXISTS (
      SELECT 1 FROM stage_deliverables sd
      WHERE sd.stage_id = ms.id AND sd.deliverable_code = extra.code
  );
