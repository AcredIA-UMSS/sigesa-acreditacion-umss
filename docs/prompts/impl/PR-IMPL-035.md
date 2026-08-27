---
id: PR-IMPL-035
feature_asociado: DD-UC-005
fsd_uc:
  - FSD-UC-005
fecha: "2026-08-27"
version: "1.0"
estado: Aprobado
autor: "@sigesa-orchestrator"
---

# PR-IMPL-035 — Versionado y bloqueo de borrado (FSD-UC-005)

## Objetivo

Implementar API-EVD-03 y API-EVD-04: listado de versiones append-only y rechazo de DELETE con auditoría.

## In-Scope

- Backend hexagonal: use cases, puerto `EvidenceLifecycleQueryPort`, controller, tests
- Frontend: panel historial de versiones en listado de evidencias por subfase
- Docs: FSD-UC-005, DTP, PM sprint 3

## Out-of-Scope

- POST `/evidences/{id}/versions` (UC-006)
- Campos `observationId` / `supersedesId` en BD (derivados de `version_number` en v1.0)

## Criterios de cierre

1. GET versiones devuelve vigente marcada con `current: true`
2. DELETE siempre 409 + `AUDIT_DELETE_DENIED` en log
3. RBAC scope carrera para [CC]
4. `pnpm tsc -b` + tests unitarios verdes
