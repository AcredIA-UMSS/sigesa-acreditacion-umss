---
id: FSD-UC-017
nombre: Bitácora de auditoría
estado: Implementado
release: v1.0
actor_principal: "[JD]"
trazabilidad_prd: PRD-US-022
modulo: MOD-AUDIT
reglas: FSD-BR-15
ultima_actualizacion: "2026-11-15"
---

# FSD-UC-017 — Bitácora de auditoría

## Contexto

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-018 · PRD-US-022 |
| **API** | `GET /api/v1/audit/logs` y `GET /api/v1/audit/logs/export` |
| **Pantalla** | `/admin/auditoria` (solo [JD], cliente Orval) |
| **Patrón** | Log **append-only** (login, transiciones, DELETE denegado, aprobación de indicador, etc.) |
| **Gherkin** | `@PRD-US-022 @FSD-UC-017 @TC-12` — `AuditLogGherkinIT` |

## Flujo principal

1. [JD] consulta `GET /api/v1/audit/logs` con filtros (actor, acción, rango fechas).
2. Sistema devuelve log append-only paginado.
3. [JD] exporta CSV para auditoría externa.

## Excepciones y flujos alternos

| Condición | Respuesta |
|-----------|-----------|
| Actor no [JD] | `403` |
| Rango fechas inválido (`from` &gt; `to`) | `422` `{ "error": "INVALID_DATE_RANGE", "message": "..." }` |

## Postcondiciones

Consulta o exportación sin modificar el log. La aprobación de indicador por [TD] (`POST /api/v1/indicators/{id}/approve`) escribe `INDICATOR_APPROVED` / entidad `INDICATOR`.

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
