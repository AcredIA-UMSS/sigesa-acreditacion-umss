---
id: DD-UC-006
fsd_ref: FSD-UC-006
titulo: "Subsanación de evidencia en subfase (historial liviano)"
modulo: MOD-EVIDENCE
estado: Aprobado
fecha: "2026-08-27"
prompts:
  - PR-IMPL-036
---

# DD-UC-006 — Subsanar evidencia en subfase

## Reglas de negocio

| ID | Regla |
|----|-------|
| BR-06 | Subsanación solo con observación **OPEN** de TD/JD |
| BR-06.1 | **Una subsanación por observación**; al subsanar → observación **RESOLVED** |
| BR-06.2 | Historial append-only; versión anterior conserva metadatos pero **sin blob** (`blob_purged=true`) |

## API

| Método | Ruta | Rol |
|--------|------|-----|
| POST | `/api/v1/subphases/{subphaseId}/evidences/{evidenceId}/subsanate` | CC |
| GET | `/api/v1/subphases/{subphaseId}/subsanation-eligibility` | CC, TD, JD |

Multipart subsanate: `file`, `description`, `observationId`.

## Modelo

- `subphase_observation.status`: `OPEN` | `RESOLVED`
- `evidence_version.observation_id`, `supersedes_version_number`, `blob_purged`, `original_filename`
