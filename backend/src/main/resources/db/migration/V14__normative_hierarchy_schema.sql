-- Release 2.0.0 — Jerarquía normativa CEUB/ARCU-SUR (ADR-0004 M1)
-- Modelo: Nivel 1 → Nivel 2 → Nivel 3 → Indicador → Evidencia
-- Legacy v1.x (phases/subphases) permanece intacto; migración de datos en V15+.

-- ---------------------------------------------------------------------------
-- 1. Plantillas — alias explícito del modelo evaluador
-- ---------------------------------------------------------------------------

ALTER TABLE templates
    ADD COLUMN IF NOT EXISTS evaluator_model VARCHAR(20);

UPDATE templates
SET evaluator_model = type
WHERE evaluator_model IS NULL;

ALTER TABLE templates
    ALTER COLUMN evaluator_model SET NOT NULL;

ALTER TABLE templates
    DROP CONSTRAINT IF EXISTS chk_templates_evaluator_model;

ALTER TABLE templates
    ADD CONSTRAINT chk_templates_evaluator_model
        CHECK (evaluator_model IN ('CEUB', 'ARCU-SUR'));

CREATE INDEX IF NOT EXISTS idx_templates_evaluator_model_status
    ON templates (evaluator_model, status);

-- ---------------------------------------------------------------------------
-- 2. Árbol normativo en plantilla (TemplateLevel1 → … → TemplateIndicator)
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS template_level1_nodes (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    template_id     UUID NOT NULL REFERENCES templates(id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    level1_order    INT NOT NULL,
    description     TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_template_level1_order UNIQUE (template_id, level1_order)
);

CREATE INDEX IF NOT EXISTS idx_template_level1_template
    ON template_level1_nodes (template_id);

CREATE TABLE IF NOT EXISTS template_level2_nodes (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    template_level1_id      UUID NOT NULL REFERENCES template_level1_nodes(id) ON DELETE CASCADE,
    name                    VARCHAR(255) NOT NULL,
    level2_order            INT NOT NULL,
    description             TEXT,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_template_level2_order UNIQUE (template_level1_id, level2_order)
);

CREATE INDEX IF NOT EXISTS idx_template_level2_level1
    ON template_level2_nodes (template_level1_id);

CREATE TABLE IF NOT EXISTS template_level3_nodes (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    template_level2_id      UUID NOT NULL REFERENCES template_level2_nodes(id) ON DELETE CASCADE,
    name                    VARCHAR(255) NOT NULL,
    level3_order            INT NOT NULL,
    description             TEXT,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_template_level3_order UNIQUE (template_level2_id, level3_order)
);

CREATE INDEX IF NOT EXISTS idx_template_level3_level2
    ON template_level3_nodes (template_level2_id);

CREATE TABLE IF NOT EXISTS template_indicators (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    template_level3_id      UUID NOT NULL REFERENCES template_level3_nodes(id) ON DELETE CASCADE,
    code                    VARCHAR(64) NOT NULL,
    description             TEXT NOT NULL,
    weight                  NUMERIC(10, 4) NOT NULL DEFAULT 1.0000,
    indicator_order         INT NOT NULL,
    reference_url           VARCHAR(2048) NOT NULL,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_template_indicator_code UNIQUE (template_level3_id, code),
    CONSTRAINT uk_template_indicator_order UNIQUE (template_level3_id, indicator_order),
    CONSTRAINT chk_template_indicator_weight CHECK (weight >= 0)
);

CREATE INDEX IF NOT EXISTS idx_template_indicators_level3
    ON template_indicators (template_level3_id);

-- ---------------------------------------------------------------------------
-- 3. Árbol normativo en proceso (Level1 → … → Indicator)
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS level1_nodes (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    process_id          UUID NOT NULL REFERENCES accreditation_processes(id) ON DELETE CASCADE,
    name                VARCHAR(255) NOT NULL,
    level1_order        INT NOT NULL,
    description         TEXT,
    status              VARCHAR(32) NOT NULL DEFAULT 'ABIERTA',
    legacy_phase_id     UUID REFERENCES phases(id) ON DELETE SET NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_level1_process_order UNIQUE (process_id, level1_order),
    CONSTRAINT chk_level1_status CHECK (status IN ('ABIERTA', 'COMPLETADA'))
);

CREATE INDEX IF NOT EXISTS idx_level1_process
    ON level1_nodes (process_id);

CREATE INDEX IF NOT EXISTS idx_level1_status
    ON level1_nodes (status);

CREATE UNIQUE INDEX IF NOT EXISTS uk_level1_legacy_phase
    ON level1_nodes (legacy_phase_id)
    WHERE legacy_phase_id IS NOT NULL;

CREATE TABLE IF NOT EXISTS level2_nodes (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    level1_id           UUID NOT NULL REFERENCES level1_nodes(id) ON DELETE CASCADE,
    name                VARCHAR(255) NOT NULL,
    level2_order        INT NOT NULL,
    description         TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_level2_order UNIQUE (level1_id, level2_order)
);

CREATE INDEX IF NOT EXISTS idx_level2_level1
    ON level2_nodes (level1_id);

CREATE TABLE IF NOT EXISTS level3_nodes (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    level2_id           UUID NOT NULL REFERENCES level2_nodes(id) ON DELETE CASCADE,
    name                VARCHAR(255) NOT NULL,
    level3_order        INT NOT NULL,
    description         TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_level3_order UNIQUE (level2_id, level3_order)
);

CREATE INDEX IF NOT EXISTS idx_level3_level2
    ON level3_nodes (level2_id);

CREATE TABLE IF NOT EXISTS indicators (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    level3_id           UUID NOT NULL REFERENCES level3_nodes(id) ON DELETE CASCADE,
    code                VARCHAR(64) NOT NULL,
    description         TEXT NOT NULL,
    weight              NUMERIC(10, 4) NOT NULL DEFAULT 1.0000,
    indicator_order     INT NOT NULL,
    reference_url       VARCHAR(2048) NOT NULL,
    status              VARCHAR(32) NOT NULL DEFAULT 'PENDIENTE',
    legacy_subphase_id  UUID REFERENCES subphases(id) ON DELETE SET NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_indicator_code UNIQUE (level3_id, code),
    CONSTRAINT uk_indicator_order UNIQUE (level3_id, indicator_order),
    CONSTRAINT chk_indicator_weight CHECK (weight >= 0),
    CONSTRAINT chk_indicator_status CHECK (
        status IN ('PENDIENTE', 'SUBIDO', 'OBSERVADO', 'SUBSANADO', 'APROBADO')
    )
);

CREATE INDEX IF NOT EXISTS idx_indicators_level3
    ON indicators (level3_id);

CREATE INDEX IF NOT EXISTS idx_indicators_status
    ON indicators (status);

CREATE UNIQUE INDEX IF NOT EXISTS uk_indicators_legacy_subphase
    ON indicators (legacy_subphase_id)
    WHERE legacy_subphase_id IS NOT NULL;

-- ---------------------------------------------------------------------------
-- 4. Observaciones de workflow v2 (sucesor de subphase_observation)
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS indicator_observation (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    indicator_id            UUID NOT NULL REFERENCES indicators(id) ON DELETE CASCADE,
    author_id               UUID NOT NULL,
    author_role             VARCHAR(10) NOT NULL,
    body                    TEXT NOT NULL,
    status                  VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    resolved_at             TIMESTAMP,
    resolved_version_id     UUID,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_indicator_observation_status CHECK (status IN ('OPEN', 'RESOLVED'))
);

CREATE INDEX IF NOT EXISTS idx_indicator_observation_indicator
    ON indicator_observation (indicator_id);

CREATE INDEX IF NOT EXISTS idx_indicator_observation_status
    ON indicator_observation (indicator_id, status);

-- ---------------------------------------------------------------------------
-- 5. Puentes hacia evidencia v2 (nullable hasta migración M2)
-- ---------------------------------------------------------------------------

ALTER TABLE evidence
    ADD COLUMN IF NOT EXISTS normative_indicator_id UUID REFERENCES indicators(id);

CREATE INDEX IF NOT EXISTS idx_evidence_normative_indicator
    ON evidence (normative_indicator_id);

ALTER TABLE evidence_version
    ADD COLUMN IF NOT EXISTS external_url VARCHAR(2048);

ALTER TABLE evidence_version
    ADD COLUMN IF NOT EXISTS indicator_observation_id UUID REFERENCES indicator_observation(id);

CREATE INDEX IF NOT EXISTS idx_evidence_version_indicator_observation
    ON evidence_version (indicator_observation_id);

-- Nota: evidence.indicator_id (tabla legacy `indicator`) y evidence.subphase_id
-- permanecen para compatibilidad v1.x. V15+ poblará normative_indicator_id.
