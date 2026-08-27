---
id: DD-UC-007
fsd_ref: FSD-UC-007
titulo: "Búsqueda de evidencias en proceso"
modulo: MOD-EVIDENCE
estado: Aprobado
fecha: "2026-08-27"
prompts:
  - PR-IMPL-037
---

# DD-UC-007 — Buscar evidencia

## API

`GET /api/v1/evidences/search` (API-EVD-02)

**Query:** `processId?`, `phaseId?`, `subphaseId?`, `programId?`, `q?`, `managementYear?`, `page=0`, `size=20`

**200 item:**

```json
{
  "evidenceId", "subphaseId", "subphaseName",
  "phaseId", "phaseName", "processId",
  "version", "description", "originalFilename",
  "uploadedAt", "uploadedBy", "blobAvailable"
}
```

## FTS

Flyway V11: GIN `search_vector` en `evidence_version`. Dev/H2: fallback LIKE.

## RBAC

[CC] filtrado por carrera; [TD]/[JD] global con filtros.
