-- Habilitar extensión pg_trgm para búsqueda aproximada de texto
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Índices GIN/GiST para mejorar el rendimiento de la búsqueda multi-token con MCP
CREATE INDEX IF NOT EXISTS idx_programs_name_trgm ON programs USING gin (name gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_evaluation_dimension_name_trgm ON evaluation_dimension USING gin (name gin_trgm_ops);
