---
id: DD-UC-008
fsd_ref: FSD-UC-008
titulo: "Rechazo de subfase"
modulo: MOD-WORKFLOW
estado: Aprobado
fecha: "2026-08-27"
prompts:
  - PR-IMPL-038
---

# DD-UC-008 — Rechazar subfase

## Reglas (sprint 3)

| ID | Regla |
|----|-------|
| BR-08.1 | Rechazo solo si la subfase tiene **≥1 evidencia** cargada |
| BR-08.2 | Subfase transiciona `SUBIDO` o `SUBSANADO` → `OBSERVADO` |
| BR-08.3 | Justificación mín. 20 caracteres; crea `subphase_observation` OPEN |
| BR-08.4 | Una observación OPEN por subfase |

## API

| Método | Ruta | Rol |
|--------|------|-----|
| POST | `/api/v1/subphases/{subphaseId}/reject` | TD |

Body: `{ "justification": "..." }`
