-- =============================================================================
-- SIGESA - Consolidación de Esquema: Depuración de Tablas Fantasma (Ghost Tables)
-- =============================================================================
-- Este script elimina formalmente las tablas que quedaron huérfanas en PostgreSQL:
-- 1. phases / subphases (Eliminadas en Flyway V16; reemplazadas por methodological_stages)
-- 2. template_phases / template_subphases (Eliminadas en Flyway V16; reemplazadas por el árbol normativo de plantillas)
-- 3. evaluation_dimension / evaluation_criterion (DDL legacy no mapeado en el backend; reemplazado por level1_nodes y level2_nodes)
-- =============================================================================

-- 1. Eliminar cualquier foreign key residual en 'indicator' que apunte a 'phases'
DO $$
DECLARE
    fk_name TEXT;
BEGIN
    FOR fk_name IN (
        SELECT tc.constraint_name
        FROM information_schema.table_constraints tc
        JOIN information_schema.constraint_column_usage ccu ON ccu.constraint_name = tc.constraint_name
        WHERE tc.constraint_type = 'FOREIGN KEY'
          AND tc.table_name = 'indicator'
          AND ccu.table_name = 'phases'
    ) LOOP
        EXECUTE 'ALTER TABLE indicator DROP CONSTRAINT IF EXISTS ' || quote_ident(fk_name) || ' CASCADE;';
    END LOOP;
END $$;

-- 2. Eliminar formalmente las 6 tablas fantasma
DROP TABLE IF EXISTS subphases CASCADE;
DROP TABLE IF EXISTS phases CASCADE;
DROP TABLE IF EXISTS template_subphases CASCADE;
DROP TABLE IF EXISTS template_phases CASCADE;
DROP TABLE IF EXISTS evaluation_criterion CASCADE;
DROP TABLE IF EXISTS evaluation_dimension CASCADE;

-- 3. Verificación de eliminación
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
  AND table_name IN ('phases', 'subphases', 'template_phases', 'template_subphases', 'evaluation_criterion', 'evaluation_dimension');
