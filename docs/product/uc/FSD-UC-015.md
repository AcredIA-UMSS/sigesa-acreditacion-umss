---
id: FSD-UC-015
nombre: Notificaciones y alertas
estado: En progreso
release: v2.0
actor_principal: Sistema (outbox → correo UMSS + in-app)
trazabilidad_prd: PRD-US-017, PRD-US-018, PRD-US-019
modulo: MOD-NOTIFY
reglas: FSD-BR-13
ultima_actualizacion: "2026-11-12"
---

# FSD-UC-015 — Notificaciones y alertas

## Contexto

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-013 · PRD-US-017–019 · NFR-004 · BR-NOTIFY-001 · DD-UC-038 |
| **Patrón** | Dual-canal: `notification_outbox` (EMAIL) + `in_app_notification` (misma TX) + worker SMTP |
| **SLA** | Eventos críticos ≤ **15 min** |
| **Remitente** | `no-reply@umss.edu.bo` |

## Flujo principal

1. Sistema detecta evento de dominio y resuelve destinatarios reales `@umss.edu.bo` (ACTIVE).
2. Encola EMAIL en `notification_outbox` + inserta fila corta in-app (idempotency por canal).
3. Worker SMTP (`PENDING→PROCESSING`) envía HTML institucional; in-app visible aunque SMTP falle.
4. Marca outbox SENT o reintenta con backoff (máx. 5 → FAILED).

## Eventos disparadores

| Evento | Destinatario | Deep link |
|--------|--------------|-----------|
| `IndicatorApproved` / `IndicatorRejected` | [CC] (responsable + asignación programa) | Indicador (+ observationId) |
| `EvidenceUploaded` / `EvidenceSubsanated` | [TD] | Bandeja técnico |
| `DeadlineApproaching` | [CC] | Indicador / observación |
| `Level1Completed` | [CC] | Nivel 1 |
| `ProcessClosed` | [JD] ACTIVE | `/executive/semaphore?processId=` (**override:** solo si status ya `CLOSED`; sin gate EE 100%) |

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
    Dado un Nivel 1 con fecha límite en 3 días y configuración de alerta activa
    Cuando el job de alertas se ejecuta
    Entonces el [CC] recibe correo institucional con enlace directo al Indicador

@PRD-US-019 @FSD-UC-015 @TC-10c
  Escenario: Nueva carga en bandeja [TD]
    Dado un [CC] que confirma carga de Evidencia
    Cuando el registro queda en estado Pendiente de revisión
    Entonces el [TD] asignado recibe notificación con enlace a la bandeja filtrada
```
