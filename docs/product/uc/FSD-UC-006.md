---
id: FSD-UC-006
nombre: Subsanar Evidencia
estado: Pendiente
release: v1.0
actor_principal: "[CC]"
trazabilidad_prd: PRD-US-006
modulo: MOD-EVIDENCE
reglas: FSD-BR-06
ultima_actualizacion: "2026-06-15"
---

# FSD-UC-006 — Subsanar Evidencia

## Contexto

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-008 · PRD-US-006 · BRD-RB-16 |
| **Precondiciones** | Indicador `OBSERVADO`; existe `observationId` activo |

## Flujo principal

1. [CC] abre observación desde dashboard o enlace de correo.
2. Carga nueva versión (`POST /evidences/{id}/versions`) con `observationId`.
3. Sistema persiste v2 con `supersedesVersion`; **v1 intacta**.
4. Audit Service inserta transición `OBSERVADO → SUBSANADO`.
5. Notification Service notifica [TD] (UC-015).

## Excepciones y flujos alternos

| Condición | Respuesta |
|-----------|-----------|
| Indicador no en `OBSERVADO` | `409 INVALID_STATE` |
| Sin `observationId` | `422` |

## Postcondiciones

Cadena de versiones trazable a observación origen; v1 preservada (append-only).

## Diagramas

- [Secuencia subsanación](../diagramas/FSD-UC-006_subsanar_evidencia_secuencia.mmd)
- [Journey CC subsanación](../diagramas/PRD_journey_CC_subsanacion_secuencia.mmd)
- [Estados indicador](../diagramas/FSD-UC-006_008_009_estados_indicador.mmd)

## Escenarios Gherkin

```gherkin
# language: es
@PRD-US-006 @FSD-UC-006 @FSD-BR-06 @TC-06
Característica: Subsanación de Evidencia

  Escenario: Subsanación enlazada a observación
    Dado un Indicador en estado Observado con observación O-123
    Cuando el [CC] carga una nueva versión de Evidencia
    Entonces el sistema registra la versión 2 enlazada a O-123
    Y conserva la versión 1 sin eliminarla
```
