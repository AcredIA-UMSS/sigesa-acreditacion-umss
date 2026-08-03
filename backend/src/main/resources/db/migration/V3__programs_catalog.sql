-- Catálogo de carreras UMSS (MOD-PROCESS / FSD-UC-003)

CREATE TABLE IF NOT EXISTS programs (
    id         UUID         PRIMARY KEY,
    code       VARCHAR(32)  NOT NULL,
    name       VARCHAR(255) NOT NULL,
    faculty    VARCHAR(128),
    active     BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_programs_code UNIQUE (code)
);

CREATE INDEX IF NOT EXISTS idx_programs_name ON programs (name);
CREATE INDEX IF NOT EXISTS idx_programs_active ON programs (active);
