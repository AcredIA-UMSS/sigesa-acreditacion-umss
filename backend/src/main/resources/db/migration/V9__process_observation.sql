-- Observaciones de seguimiento entre técnico [TD] y coordinador [CC] (FSD-UC-032).

CREATE TABLE process_observation (
    id UUID PRIMARY KEY,
    process_id UUID NOT NULL,
    evidence_id UUID NOT NULL,
    indicator_id UUID NOT NULL,
    description VARCHAR(2000) NOT NULL,
    kind VARCHAR(30) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    compliance VARCHAR(20) NOT NULL,
    recommendation VARCHAR(2000) NULL,
    author_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_process_observation_process ON process_observation (process_id, updated_at DESC);

CREATE TABLE process_observation_message (
    id UUID PRIMARY KEY,
    observation_id UUID NOT NULL REFERENCES process_observation (id),
    author_id UUID NOT NULL,
    author_role VARCHAR(10) NOT NULL,
    body VARCHAR(2000) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_process_observation_message_obs ON process_observation_message (observation_id, created_at);
