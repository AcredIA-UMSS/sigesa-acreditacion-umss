---
id: DD-UC-009
fsd_ref: FSD-UC-009
titulo: "Aprobación de subfase"
modulo: MOD-WORKFLOW
estado: Aprobado
fecha: "2026-08-27"
prompts:
  - PR-IMPL-038
---

# DD-UC-009 — Aprobar subfase

## Reglas (sprint 3)

| ID | Regla |
|----|-------|
| BR-09.1 | Aprobación solo si la subfase tiene **≥1 evidencia** |
| BR-09.2 | Sin observación OPEN pendiente |
| BR-09.3 | Estado origen: `SUBIDO` o `SUBSANADO` → `APROBADO` |

## API

| Método | Ruta | Rol |
|--------|------|-----|
| POST | `/api/v1/subphases/{subphaseId}/approve` | TD |
