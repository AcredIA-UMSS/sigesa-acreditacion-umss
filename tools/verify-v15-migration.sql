-- Verificación read-only V15 (ADR-0004 M2)
-- Ejecutar: docker exec -i sigesa-postgres psql -U sigesa_user -d sigesa -f - < tools/verify-v15-migration.sql

\echo '=== Flyway (esperado: V14 y V15 si Flyway está habilitado en prod) ==='
SELECT version, description, success
FROM flyway_schema_history
ORDER BY installed_rank;

\echo '=== Conteos legacy vs v2 ==='
SELECT 'phases' AS entity, COUNT(*)::text AS cnt FROM phases
UNION ALL SELECT 'subphases', COUNT(*)::text FROM subphases
UNION ALL SELECT 'level1_nodes', COUNT(*)::text FROM level1_nodes
UNION ALL SELECT 'level2_nodes', COUNT(*)::text FROM level2_nodes
UNION ALL SELECT 'level3_nodes', COUNT(*)::text FROM level3_nodes
UNION ALL SELECT 'indicators', COUNT(*)::text FROM indicators
UNION ALL SELECT 'template_phases', COUNT(*)::text FROM template_phases
UNION ALL SELECT 'template_level1_nodes', COUNT(*)::text FROM template_level1_nodes
UNION ALL SELECT 'template_indicators', COUNT(*)::text FROM template_indicators
UNION ALL SELECT 'evidence_normative', COUNT(*)::text FROM evidence WHERE normative_indicator_id IS NOT NULL
UNION ALL SELECT 'evidence_subphase', COUNT(*)::text FROM evidence WHERE subphase_id IS NOT NULL;

\echo '=== UUID preservados (level1.id = phase.id) ==='
SELECT COUNT(*) AS level1_matching_phase_id
FROM level1_nodes l1
JOIN phases p ON p.id = l1.id;

\echo '=== Indicadores con legacy_subphase_id ==='
SELECT COUNT(*) AS indicators_with_legacy
FROM indicators
WHERE legacy_subphase_id IS NOT NULL;

\echo '=== Procesos sin árbol v2 completo (L1 sin L2) ==='
SELECT l1.process_id, l1.name
FROM level1_nodes l1
WHERE NOT EXISTS (SELECT 1 FROM level2_nodes l2 WHERE l2.level1_id = l1.id)
LIMIT 10;

\echo '=== Criterio OK ==='
\echo 'OK si: level1_nodes = phases, indicators = subphases, level2/level3 > 0, template_level1 = template_phases'
