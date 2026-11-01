---
id: FSD-UC-017
nombre: Bitácora de auditoría
estado: En curso
release: v1.0
actor_principal: "[JD]"
trazabilidad_prd: PRD-US-022
modulo: MOD-AUDIT
reglas: FSD-BR-15
ultima_actualizacion: "2026-11-01"
---

# FSD-UC-017 — Bitácora de auditoría

## Contexto

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-018 · PRD-US-022 |
| **API** | `GET /api/v1/audit/logs`, `GET /api/v1/audit/logs/{id}`, `GET /api/v1/audit/logs/export`, `POST /api/v1/auth/logout` |
| **Patrón** | Log append-only. Persiste login, cierre de sesión, sesión expirada, alta y estado de cuenta, carga de evidencia y solicitud de reporte. |
| **Diseño** | [`DD-UC-017`](../../design/DD-UC-017.md) |

## Flujo principal

1. [JD] consulta `GET /api/v1/audit/logs` con filtros (actor, una o varias acciones, recurso, resultado, rango de fechas) y paginación.
2. Sistema devuelve el log append-only, ordenado por fecha descendente.
3. [JD] descarga `GET /api/v1/audit/logs/export` con los mismos filtros, tope 1000 filas y sin payload.

## Excepciones y flujos alternos

| Condición | Respuesta |
|-----------|-----------|
| Actor no [JD] | `403` |
| Rango fechas inválido | `422` |

## Postcondiciones

Consulta o exportación sin modificar el log.

## Diagramas

- [Auditoría y exportación](../diagramas/MAR-SEQ-009-auditoria-exportacion.mmd)

## Escenarios Gherkin

```gherkin
# language: es
@PRD-US-022 @FSD-UC-017 @TC-12
Característica: Bitácora de auditoría

  Escenario: Registro en bitácora
    Dado una acción de aprobación de Indicador por [TD]
    Cuando la acción se confirma
    Entonces el sistema registra actor, timestamp, acción e identificador de entidad
```
