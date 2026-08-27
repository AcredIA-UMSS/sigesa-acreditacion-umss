---
id: PR-IMPL-037
fsd_uc:
  - FSD-UC-007
feature_asociado: DD-UC-007
estado: Aprobado
---

# PR-IMPL-037 — Búsqueda de evidencias en vista de proceso

## Alcance

- Backend `GET /api/v1/evidences/search` (API-EVD-02)
- Flyway V11: FTS GIN `evidence_version.search_vector` + ranking `ts_rank`
- Fallback LIKE si PostgreSQL no tiene columna (H2/dev)
- Frontend: panel buscador en detalle de proceso (fases/subfases)
- Docs + PM-004 sprint 3

## Fuera de alcance

- Página global `/evidencias/buscar` dedicada
