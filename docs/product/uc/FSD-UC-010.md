---
id: FSD-UC-010
nombre: Avanzar/cerrar Fase
estado: Pendiente
release: v1.0
actor_principal: "[TD]"
trazabilidad_prd: PRD-US-011
modulo: MOD-WORKFLOW
reglas: FSD-BR-07
ultima_actualizacion: "2026-06-15"
---

# FSD-UC-010 — Avanzar/cerrar Fase

## Contexto

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-010, 017 · PRD-US-011 |
| **Precondiciones** | Todos los Indicadores de la Fase en `APROBADO` (excl. N/A explícito) |
| **Hard constraint** | Ver LFSD §3 regla 2 |

## Flujo principal

1. Orchestration Service consume `IndicatorApproved` desde SQS FIFO (`MessageGroupId = phaseId`).
2. Sistema verifica conteo sobre `indicator_current_view`: indicadores aprobados = total.
3. Si corresponde, inserta `phase_state_history` con `COMPLETADA`.
4. Publica `PhaseCompleted`.

## Excepciones y flujos alternos

| Condición | Respuesta |
|-----------|-----------|
| Indicadores pendientes | `409 FASE_CIERRE_BLOQUEADO` + lista de Indicadores |
| [CC] intenta forzar cierre | `403 FORBIDDEN_ROLE` |

## Postcondiciones

Fase en estado `COMPLETADA`; evento `PhaseCompleted` publicado.

## Diagramas

- [Estados cierre fase](../diagramas/FSD-UC-010_cierre_fase_estados.mmd)
- [Journey TD cierre fase](../diagramas/PRD_journey_TD_cierre_fase_secuencia.mmd)
- [Flow cierre con pendientes](../diagramas/diag-08-flow-cierre-proceso-pendientes.mmd)
- [Proceso y cierre](../diagramas/FSD-UC-003_010_proceso_y_cierre_fase_secuencia.mmd)

## Escenarios Gherkin

```gherkin
# language: es
@PRD-US-011 @FSD-UC-010 @NFR-018 @TC-SAD-002
Característica: Avance y cierre de Fase

  Escenario: Avance de Fase bloqueado con indicadores pendientes
    Dado una Fase con al menos un Indicador no Aprobado
    Cuando el [TD] intenta cerrar la Fase
    Entonces el sistema rechaza la transición
    Y lista los Indicadores pendientes

  Escenario: Salto de estado no autorizado
    Dado un usuario [CC] sin permiso de cierre de Fase
    Cuando intenta forzar estado Cerrado en la Fase
    Entonces el sistema rechaza la operación
```
