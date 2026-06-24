---
id: FSD-UC-017
nombre: Bitácora de auditoría
estado: Pendiente
release: v1.0
actor_principal: "[JD]"
trazabilidad_prd: PRD-US-022
modulo: MOD-AUDIT
reglas: FSD-BR-15
ultima_actualizacion: "2026-06-15"
---

# FSD-UC-017 — Bitácora de auditoría

## Contexto

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-018 · PRD-US-022 |
| **API** | `GET /audit/logs` |
| **Patrón** | Log **append-only** (login, transiciones, DELETE denegado, etc.) |

## Flujo principal

1. [JD] consulta `GET /audit/logs` con filtros (actor, acción, rango fechas).
2. Sistema devuelve log append-only paginado.
3. [JD] exporta CSV para auditoría externa.

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
