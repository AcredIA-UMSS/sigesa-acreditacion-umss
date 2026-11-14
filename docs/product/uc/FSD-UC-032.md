---
id: FSD-UC-032
nombre: Observaciones del técnico sobre evidencias del proceso
estado: implementado
release: v1.0
actor_principal: "[TD]"
actores_secundarios: "[CC]"
modulo: MOD-WORKFLOW
pr_impl: PR-IMPL-046
design_doc: DD-UC-032
ultima_actualizacion: "2026-11-04"
---

# FSD-UC-032 — Observaciones del técnico sobre evidencias

## Contexto

| Campo | Valor |
| --- | --- |
| **Actor** | [TD] registra. [CC] consulta, responde y marca el seguimiento. |
| **Precondiciones** | Proceso visible para el rol (FSD-UC-019). La evidencia pertenece a un indicador de la misma carrera que el proceso. |
| **No incluye** | Cambiar el estado del indicador. Reutilizar `tb_observation` del dashboard. |

## Flujo principal

1. [TD] abre el detalle del proceso y ve las evidencias de esa carrera.
2. Registra descripción, tipo, prioridad, cumplimiento y recomendación. Queda en `BORRADOR`.
3. [TD] envía. Pasa a `ENVIADA`.
4. [CC] de la carrera marca `EN_REVISION`, responde (`RESPONDIDA`) y, cuando corresponde, `ATENDIDA`.
5. [TD] cierra (`CERRADA`). Cada respuesta queda en el historial.

## Estados

`BORRADOR` → `ENVIADA` → `EN_REVISION` → `RESPONDIDA` → `ATENDIDA` → `CERRADA`.

| Transición | Rol |
| --- | --- |
| Crear y editar borrador, enviar, cerrar | [TD]. Enviar y editar solo el autor. |
| Revisar, responder, atender | [CC] de la carrera del proceso. |

## Excepciones

| Condición | Respuesta |
| --- | --- |
| [CC] de otra carrera | `404 PROCESS_NOT_FOUND` |
| Rol distinto de [TD] o [CC] | `403 FORBIDDEN_ROLE` |
| Evidencia de otra carrera | `422 EVIDENCE_NOT_IN_PROCESS` |
| Descripción menor a 10 caracteres o sin recomendación al enviar | `422 OBSERVATION_INCOMPLETE` |
| Transición que no está en la tabla | `409 INVALID_OBSERVATION_STATE` |

## Escenarios

```gherkin
# language: es
Característica: Observación de evidencia

  Escenario: El técnico envía y el coordinador responde
    Dado un [TD] y una evidencia de la carrera del proceso
    Cuando guarda la observación y la envía
    Entonces el [CC] de esa carrera la ve en estado Enviada
    Y al responder queda un mensaje en el historial

  Escenario: El coordinador no ve un proceso de otra carrera
    Dado un [CC] de la carrera A
    Cuando consulta observaciones de un proceso de la carrera B
    Entonces recibe 404
```
