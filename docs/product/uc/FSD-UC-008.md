---
id: FSD-UC-008
nombre: Rechazar Indicador
estado: Pendiente
release: v1.0
actor_principal: "[TD]"
trazabilidad_prd: PRD-US-009
modulo: MOD-WORKFLOW
reglas: FSD-BR-04, FSD-BR-05
ultima_actualizacion: "2026-06-15"
---

# FSD-UC-008 — Rechazar Indicador

## Contexto

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-008, 009 · PRD-US-009 |
| **API** | `POST /indicators/{id}/reject` |
| **Estados origen** | `SUBIDO` o `SUBSANADO` |

## Flujo principal

1. [TD] revisa Indicador en `SUBIDO` o `SUBSANADO`.
2. Ingresa justificación (mín. 20 caracteres).
3. Sistema crea `Observation` e inserta transición `SUBIDO|SUBSANADO → OBSERVADO` en `indicator_state_history`.
4. Notifica [CC] en ≤ 15 min (FSD-BR-13).

## Excepciones y flujos alternos

| Condición | Respuesta |
|-----------|-----------|
| Justificación vacía o < mínimo | `422 JUSTIFICATION_REQUIRED` |
| Actor no [TD] | `403 FORBIDDEN_ROLE` |
| Estado inválido | `409 INVALID_STATE` |

## Postcondiciones

Indicador `OBSERVADO`; entidad `Observation` creada; [CC] notificado.

## Diagramas

- [Aprobación/rechazo](../diagramas/MAR-SEQ-003-aprobacion-rechazo-subfase.mmd)
- [Estados indicador](../diagramas/FSD-UC-006_008_009_estados_indicador.mmd)
- [Secuencia observaciones](../diagramas/diag-03-seq-observaciones.mmd)
- [Workflow aprobación](../diagramas/D-FLOW-001-workflow-aprobacion.mmd)

## Escenarios Gherkin

```gherkin
# language: es
@PRD-US-009 @FSD-UC-008 @NFR-018 @TC-SAD-003
Característica: Rechazo de Indicador

  Escenario: Rechazo con justificación obligatoria
    Dado un [TD] revisando un Indicador
    Cuando confirma rechazo sin texto de justificación
    Entonces el sistema impide el rechazo
    Y solicita motivo obligatorio

  Escenario: Rechazo exitoso notifica al CC
    Dado un [TD] con justificación válida
    Cuando confirma el rechazo del Indicador
    Entonces el Indicador pasa a estado Observado
    Y el [CC] recibe notificación en un máximo de 15 minutos
```
