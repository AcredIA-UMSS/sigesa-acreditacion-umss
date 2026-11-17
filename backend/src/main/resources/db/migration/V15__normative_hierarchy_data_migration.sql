-- Release 2.0.0 — Migración de datos legacy v1.x → jerarquía normativa v2.0 (ADR-0004 M2)
-- Idempotente: re-ejecutable sin duplicar filas ni romper FKs.
-- Preserva UUIDs de fase/subfase en level1/indicators para trazabilidad.
-- Legacy (phases, subphases, subphase_observation) permanece intacto.

-- ---------------------------------------------------------------------------
-- A. Plantillas: template_phases → árbol normativo v2
-- ---------------------------------------------------------------------------

INSERT INTO template_level1_nodes (
    id,
    template_id,
    name,
    level1_order,
    description,
    created_at,
    updated_at
)
SELECT
    tp.id,
    tp.template_id,
    tp.name,
    tp.phase_order,
    tp.description,
    COALESCE(t.created_at, now()),
    COALESCE(t.updated_at, now())
FROM template_phases tp
JOIN templates t ON t.id = tp.template_id
WHERE NOT EXISTS (
    SELECT 1 FROM template_level1_nodes tl1 WHERE tl1.id = tp.id
);

INSERT INTO template_level2_nodes (
    id,
    template_level1_id,
    name,
    level2_order,
    description,
    created_at,
    updated_at
)
SELECT
    gen_random_uuid(),
    tl1.id,
    'General',
    1,
    'Nodo placeholder N2 — migración ADR-0004 M2 (refinar vía UC-022)',
    tl1.created_at,
    tl1.updated_at
FROM template_level1_nodes tl1
WHERE NOT EXISTS (
    SELECT 1
    FROM template_level2_nodes tl2
    WHERE tl2.template_level1_id = tl1.id
      AND tl2.level2_order = 1
);

INSERT INTO template_level3_nodes (
    id,
    template_level2_id,
    name,
    level3_order,
    description,
    created_at,
    updated_at
)
SELECT
    gen_random_uuid(),
    tl2.id,
    'General',
    1,
    'Nodo placeholder N3 — migración ADR-0004 M2 (refinar vía UC-022)',
    tl2.created_at,
    tl2.updated_at
FROM template_level2_nodes tl2
WHERE NOT EXISTS (
    SELECT 1
    FROM template_level3_nodes tl3
    WHERE tl3.template_level2_id = tl2.id
      AND tl3.level3_order = 1
);

INSERT INTO template_indicators (
    id,
    template_level3_id,
    code,
    description,
    weight,
    indicator_order,
    reference_url,
    created_at,
    updated_at
)
SELECT
    ts.id,
    tl3.id,
    'LEG-' || upper(substr(replace(ts.id::text, '-', ''), 1, 8)),
    trim(
        COALESCE(NULLIF(trim(ts.description), ''), ts.name)
        || CASE
            WHEN ts.requirements IS NOT NULL
                 AND trim(ts.requirements) <> ''
                 AND trim(ts.requirements) IS DISTINCT FROM trim(COALESCE(ts.description, ''))
            THEN E'\n\n' || ts.requirements
            ELSE ''
           END
    ),
    1.0000,
    ts.subphase_order,
    COALESCE(
        NULLIF(trim(ts.reference_url), ''),
        'https://duea.umss.edu.bo/normativa/pendiente'
    ),
    COALESCE(t.created_at, now()),
    COALESCE(t.updated_at, now())
FROM template_subphases ts
JOIN template_phases tp ON tp.id = ts.template_phase_id
JOIN template_level1_nodes tl1 ON tl1.id = tp.id
JOIN template_level2_nodes tl2
    ON tl2.template_level1_id = tl1.id
   AND tl2.level2_order = 1
JOIN template_level3_nodes tl3
    ON tl3.template_level2_id = tl2.id
   AND tl3.level3_order = 1
JOIN templates t ON t.id = tp.template_id
WHERE NOT EXISTS (
    SELECT 1 FROM template_indicators ti WHERE ti.id = ts.id
);

-- ---------------------------------------------------------------------------
-- B. Procesos: phases → level1_nodes (+ placeholders N2/N3)
-- ---------------------------------------------------------------------------

INSERT INTO level1_nodes (
    id,
    process_id,
    name,
    level1_order,
    description,
    status,
    legacy_phase_id,
    created_at,
    updated_at
)
SELECT
    p.id,
    p.process_id,
    p.name,
    p.phase_order,
    p.description,
    CASE
        WHEN p.status IN ('ABIERTA', 'COMPLETADA') THEN p.status
        ELSE 'ABIERTA'
    END,
    p.id,
    now(),
    now()
FROM phases p
WHERE NOT EXISTS (
    SELECT 1 FROM level1_nodes l1 WHERE l1.id = p.id
);

INSERT INTO level2_nodes (
    id,
    level1_id,
    name,
    level2_order,
    description,
    created_at,
    updated_at
)
SELECT
    gen_random_uuid(),
    l1.id,
    'General',
    1,
    'Nodo placeholder N2 — migración ADR-0004 M2 (refinar vía UC-022)',
    l1.created_at,
    l1.updated_at
FROM level1_nodes l1
WHERE NOT EXISTS (
    SELECT 1
    FROM level2_nodes l2
    WHERE l2.level1_id = l1.id
      AND l2.level2_order = 1
);

INSERT INTO level3_nodes (
    id,
    level2_id,
    name,
    level3_order,
    description,
    created_at,
    updated_at
)
SELECT
    gen_random_uuid(),
    l2.id,
    'General',
    1,
    'Nodo placeholder N3 — migración ADR-0004 M2 (refinar vía UC-022)',
    l2.created_at,
    l2.updated_at
FROM level2_nodes l2
WHERE NOT EXISTS (
    SELECT 1
    FROM level3_nodes l3
    WHERE l3.level2_id = l2.id
      AND l3.level3_order = 1
);

-- ---------------------------------------------------------------------------
-- C. Procesos: subphases → indicators
-- ---------------------------------------------------------------------------

INSERT INTO indicators (
    id,
    level3_id,
    code,
    description,
    weight,
    indicator_order,
    reference_url,
    status,
    legacy_subphase_id,
    created_at,
    updated_at
)
SELECT
    s.id,
    l3.id,
    'LEG-' || upper(substr(replace(s.id::text, '-', ''), 1, 8)),
    trim(
        COALESCE(NULLIF(trim(s.description), ''), s.name)
        || CASE
            WHEN s.requirements IS NOT NULL
                 AND trim(s.requirements) <> ''
                 AND trim(s.requirements) IS DISTINCT FROM trim(COALESCE(s.description, ''))
            THEN E'\n\n' || s.requirements
            ELSE ''
           END
    ),
    1.0000,
    s.subphase_order,
    COALESCE(
        NULLIF(trim(s.reference_url), ''),
        'https://duea.umss.edu.bo/normativa/pendiente'
    ),
    CASE s.status
        WHEN 'PENDIENTE' THEN 'PENDIENTE'
        WHEN 'SUBIDO' THEN 'SUBIDO'
        WHEN 'OBSERVADO' THEN 'OBSERVADO'
        WHEN 'SUBSANADO' THEN 'SUBSANADO'
        WHEN 'APROBADO' THEN 'APROBADO'
        ELSE 'PENDIENTE'
    END,
    s.id,
    now(),
    now()
FROM subphases s
JOIN phases p ON p.id = s.phase_id
JOIN level1_nodes l1 ON l1.id = p.id
JOIN level2_nodes l2
    ON l2.level1_id = l1.id
   AND l2.level2_order = 1
JOIN level3_nodes l3
    ON l3.level2_id = l2.id
   AND l3.level3_order = 1
WHERE NOT EXISTS (
    SELECT 1 FROM indicators i WHERE i.id = s.id OR i.legacy_subphase_id = s.id
);

-- ---------------------------------------------------------------------------
-- D. Observaciones: subphase_observation → indicator_observation
-- ---------------------------------------------------------------------------

INSERT INTO indicator_observation (
    id,
    indicator_id,
    author_id,
    author_role,
    body,
    status,
    resolved_at,
    resolved_version_id,
    created_at,
    updated_at
)
SELECT
    o.id,
    i.id,
    o.author_id,
    o.author_role,
    o.body,
    CASE
        WHEN o.status IN ('OPEN', 'RESOLVED') THEN o.status
        ELSE 'OPEN'
    END,
    o.resolved_at,
    o.resolved_version_id,
    o.created_at,
    o.updated_at
FROM subphase_observation o
JOIN indicators i ON i.legacy_subphase_id = o.subphase_id
WHERE NOT EXISTS (
    SELECT 1 FROM indicator_observation io WHERE io.id = o.id
);

-- ---------------------------------------------------------------------------
-- E. Evidencias: subphase_id → normative_indicator_id
-- ---------------------------------------------------------------------------

UPDATE evidence e
SET normative_indicator_id = i.id
FROM indicators i
WHERE e.subphase_id IS NOT NULL
  AND e.subphase_id = i.legacy_subphase_id
  AND e.normative_indicator_id IS NULL;

UPDATE evidence_version ev
SET indicator_observation_id = io.id
FROM indicator_observation io
WHERE ev.observation_id IS NOT NULL
  AND ev.observation_id = io.id
  AND ev.indicator_observation_id IS NULL;

-- Nota: evidence.indicator_id (tabla legacy `indicator`, dashboard v0) no se migra
-- en M2; solo evidencias vinculadas por subphase_id pasan a normative_indicator_id.
