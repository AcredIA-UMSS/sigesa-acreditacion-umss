---
id: FSD-UC-008
nombre: Rechazar Indicador
estado: Implementado v2
release: v2.0
actor_principal: "[TD]"
trazabilidad_prd: PRD-US-009
modulo: MOD-WORKFLOW
reglas: FSD-BR-04, FSD-BR-05
ultima_actualizacion: "2026-09-08"
nota_implementacion: "API-WF-01 activo: POST /indicators/{id}/reject con indicator_observation v2 (post-M5)."
---

# FSD-UC-008 — Rechazar Indicador

## Contexto (v2.0)

| Campo | Valor |
|-------|-------|
| **API v2.0** | `POST /api/v1/indicators/{indicatorId}/reject` |
| **API legacy** | `POST /api/v1/subphases/{subphaseId}/reject` |
| **Precondición** | ≥1 evidencia cargada en el indicador |
| **Estados** | `SUBIDO` o `SUBSANADO` → `OBSERVADO` |

## Flujo principal

1. [TD] revisa indicador con evidencia cargada en detalle de proceso.
2. Ingresa justificación (mín. 20 caracteres).
3. Sistema crea `indicator_observation` OPEN y transiciona indicador a `OBSERVADO`.
4. [CC] subsana (UC-006).

## Excepciones

| Condición | Respuesta |
|-----------|-----------|
| Sin evidencia | `409 EVIDENCE_REQUIRED` |
| Justificación corta | `422 JUSTIFICATION_REQUIRED` |
| Observación OPEN existente | `409 INVALID_STATE` |
