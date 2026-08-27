---
id: FSD-UC-008
nombre: Rechazar Subfase
estado: Implementado
release: v1.0
actor_principal: "[TD]"
trazabilidad_prd: PRD-US-009
modulo: MOD-WORKFLOW
reglas: FSD-BR-04, FSD-BR-05
ultima_actualizacion: "2026-08-27"
---

# FSD-UC-008 — Rechazar Subfase

## Contexto (sprint 3)

| Campo | Valor |
|-------|-------|
| **API** | `POST /api/v1/subphases/{subphaseId}/reject` |
| **Precondición** | ≥1 evidencia cargada en la subfase |
| **Estados** | `SUBIDO` o `SUBSANADO` → `OBSERVADO` |

## Flujo principal

1. [TD] revisa subfase con evidencia cargada en detalle de proceso.
2. Ingresa justificación (mín. 20 caracteres).
3. Sistema crea `subphase_observation` OPEN y transiciona subfase a `OBSERVADO`.
4. [CC] subsana (UC-006).

## Excepciones

| Condición | Respuesta |
|-----------|-----------|
| Sin evidencia | `409 EVIDENCE_REQUIRED` |
| Justificación corta | `422 JUSTIFICATION_REQUIRED` |
| Observación OPEN existente | `409 INVALID_STATE` |
