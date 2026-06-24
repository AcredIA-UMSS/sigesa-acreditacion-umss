---
id: FSD-UC-015
nombre: Notificaciones y alertas
estado: Pendiente
release: v1.0
actor_principal: Sistema (outbox → correo UMSS)
trazabilidad_prd: PRD-US-017, PRD-US-018, PRD-US-019
modulo: MOD-NOTIFY
reglas: FSD-BR-13
ultima_actualizacion: "2026-06-15"
---

# FSD-UC-015 — Notificaciones y alertas

## Contexto

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-013 · PRD-US-017–019 · NFR-004 |
| **Patrón** | `notification_outbox` + worker SMTP |
| **SLA** | Eventos críticos ≤ **15 min** |

## Flujo principal

1. Sistema detecta evento de dominio (aprobación, rechazo, plazo, nueva carga).
2. Encola mensaje en `notification_outbox`.
3. Worker SMTP envía correo institucional con enlace profundo.
4. Marca outbox como procesado o reintenta con backoff.

## Eventos disparadores

| Evento | Destinatario |
|--------|--------------|
| Aprobación/rechazo Indicador | [CC] |
| Plazo próximo (job programado) | [CC] |
| Nueva Evidencia cargada | [TD] |

## Excepciones y flujos alternos

| Condición | Comportamiento |
|-----------|----------------|
| Fallo SMTP | Reintento; alerta operaciones si agotado |
| Usuario sin email válido | Log error; no bloquea transacción principal |

## Postcondiciones

Notificación entregada o encolada para reintento; SLA medido.

## Diagramas

- [Notificaciones SMTP](../diagramas/AYL-SEQ-006-notificaciones-smtp.mmd)

## Escenarios Gherkin

```gherkin
# language: es
@PRD-US-017 @FSD-UC-015 @NFR-004 @TC-10a
Característica: Notificaciones a [CC]

  Escenario: Enlace profundo desde notificación
    Dado un [CC] que recibe notificación de rechazo
    Cuando abre el enlace del correo
    Entonces aterriza en el Indicador y observación sin reautenticación adicional si la sesión sigue activa

@PRD-US-018 @FSD-UC-015 @TC-10b
  Escenario: Alerta de plazo próximo
    Dado una Fase con fecha límite en 3 días y configuración de alerta activa
    Cuando el job de alertas se ejecuta
    Entonces el [CC] recibe correo institucional con enlace directo al Indicador

@PRD-US-019 @FSD-UC-015 @TC-10c
  Escenario: Nueva carga en bandeja [TD]
    Dado un [CC] que confirma carga de Evidencia
    Cuando el registro queda en estado Pendiente de revisión
    Entonces el [TD] asignado recibe notificación con enlace a la bandeja filtrada
```
