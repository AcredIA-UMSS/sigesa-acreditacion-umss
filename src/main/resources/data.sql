-- Datos de prueba: Universidad Mayor de San Simón (UMSS) - Cochabamba, Bolivia
-- Seeds for report module (postgres profile / E2E). Fase seeds removed — main uses MOD-AUTH/process model.

-- =========================
-- Report definitions for E2E tests
-- =========================

INSERT INTO report_definition (codigo, nombre, descripcion, owner_role, audiences, filters_allowed, metrics, version, created_at, updated_at)
VALUES (
  'E2E-KPIS',
  'E2E KPIs Report',
  'Report used by automated E2E to validate KPI export and counts',
  'CC',
  '{"roles":["CC","TD"]}',
  '{}',
  '{}',
  1,
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
);

INSERT INTO report_definition (codigo, nombre, descripcion, owner_role, audiences, filters_allowed, metrics, version, created_at, updated_at)
VALUES (
  'E2E-DETAILED',
  'E2E Detailed Report',
  'Detailed rows export for E2E tests',
  'CC',
  '{"roles":["CC","TD"]}',
  '{}',
  '{}',
  1,
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
);
