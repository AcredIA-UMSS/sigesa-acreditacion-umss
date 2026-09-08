---
id: FSD-UC-009
nombre: Aprobar Indicador
estado: Implementado v2
release: v2.0
actor_principal: "[TD]"
trazabilidad_prd: PRD-US-010
modulo: MOD-WORKFLOW
reglas: FSD-BR-04
ultima_actualizacion: "2026-09-08"
nota_implementacion: "API-WF-02 activo: POST /indicators/{id}/approve sobre indicador normativo v2. Legacy subfase coexistiendo hasta M5."
---

# FSD-UC-009 — Aprobar Indicador

## Contexto (v2.0)

| Campo | Valor |
|-------|-------|
| **API v2.0** | `POST /api/v1/indicators/{indicatorId}/approve` |
| **API legacy** | `POST /api/v1/subphases/{subphaseId}/approve` |
| **Precondición** | ≥1 evidencia; sin observación OPEN |
| **Estados** | `SUBIDO` o `SUBSANADO` → `APROBADO` |

## Flujo principal

1. [TD] valida evidencia conforme en el indicador.
2. Invoca aprobar desde la vista de proceso.
3. Sistema transiciona indicador a `APROBADO`.

## Excepciones

| Condición | Respuesta |
|-----------|-----------|
| Sin evidencia | `409 EVIDENCE_REQUIRED` |
| Observación OPEN | `409 SUBSANATION_NOT_ALLOWED` |
| Estado inválido | `409 INVALID_STATE` |
