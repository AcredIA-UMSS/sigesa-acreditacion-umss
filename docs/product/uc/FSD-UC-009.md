---
id: FSD-UC-009
nombre: Aprobar Indicador
estado: Pendiente
release: v1.0
actor_principal: "[TD]"
trazabilidad_prd: PRD-US-010
modulo: MOD-WORKFLOW
reglas: FSD-BR-04
ultima_actualizacion: "2026-06-15"
---

# FSD-UC-009 — Aprobar Indicador

## Contexto

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-009 · PRD-US-010 |
| **API** | `POST /indicators/{id}/approve` |
| **Estados origen** | `SUBIDO` o `SUBSANADO` |

## Flujo principal

1. [TD] valida Evidencia conforme.
2. Invoca `POST /indicators/{id}/approve`.
3. Audit Service inserta transición a `APROBADO` en `indicator_state_history`.
4. Publica `IndicatorApproved`.
5. Notification Service notifica [CC] (UC-015).
6. Orchestration Service evalúa cierre de Fase (UC-010).

## Excepciones y flujos alternos

| Condición | Respuesta |
|-----------|-----------|
| [CC] intenta aprobar | `403 FORBIDDEN_ROLE` |
| Estado inválido | `409 INVALID_STATE` |

## Postcondiciones

Indicador `APROBADO`; evento `IndicatorApproved` publicado.

## Diagramas

- [Workflow aprobación](../diagramas/D-FLOW-001-workflow-aprobacion.mmd)
- [Estados indicador](../diagramas/FSD-UC-006_008_009_estados_indicador.mmd)
- [Aprobación/rechazo](../diagramas/MAR-SEQ-003-aprobacion-rechazo-subfase.mmd)

## Escenarios Gherkin

```gherkin
# language: es
@PRD-US-010 @FSD-UC-009 @TC-07b
Característica: Aprobación de Indicador

  Escenario: Aprobación exitosa
    Dado un [TD] con Evidencia conforme
    Cuando aprueba el Indicador
    Entonces el estado pasa a Aprobado
    Y el [CC] recibe notificación en un máximo de 15 minutos

  Escenario: Último indicador prepara cierre de fase
    Dado todos los Indicadores de la Fase en Aprobado salvo uno
    Cuando el [TD] aprueba el último pendiente
    Entonces el sistema marca la Fase como lista para cierre según reglas
```
