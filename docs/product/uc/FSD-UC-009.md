---
id: FSD-UC-009
nombre: Aprobar Subfase
estado: Implementado
release: v1.0
actor_principal: "[TD]"
trazabilidad_prd: PRD-US-010
modulo: MOD-WORKFLOW
reglas: FSD-BR-04
ultima_actualizacion: "2026-08-27"
---

# FSD-UC-009 — Aprobar Subfase

## Contexto (sprint 3)

| Campo | Valor |
|-------|-------|
| **API** | `POST /api/v1/subphases/{subphaseId}/approve` |
| **Precondición** | ≥1 evidencia; sin observación OPEN |
| **Estados** | `SUBIDO` o `SUBSANADO` → `APROBADO` |

## Flujo principal

1. [TD] valida evidencia conforme en la subfase.
2. Invoca aprobar desde la vista de proceso.
3. Sistema transiciona subfase a `APROBADO`.

## Excepciones

| Condición | Respuesta |
|-----------|-----------|
| Sin evidencia | `409 EVIDENCE_REQUIRED` |
| Observación OPEN | `409 SUBSANATION_NOT_ALLOWED` |
| Estado inválido | `409 INVALID_STATE` |
