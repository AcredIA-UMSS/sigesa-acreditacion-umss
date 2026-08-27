---
id: FSD-UC-007
nombre: Buscar Evidencia
estado: Implementado
release: v1.0
actor_principal: "[CC] (alcance carrera), [TD] (global)"
trazabilidad_prd: PRD-US-004
modulo: MOD-EVIDENCE
reglas: FSD-BR-09
ultima_actualizacion: "2026-08-27"
---

# FSD-UC-007 — Buscar Evidencia

## Contexto

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-015 · PRD-US-004 · NFR-002 |
| **API** | `GET /api/v1/evidences/search` (API-EVD-02) |
| **Alcance v1 (2026-08-27)** | Buscador en detalle de proceso; FTS GIN PostgreSQL (V11) + fallback LIKE |

## Flujo principal

1. Usuario abre detalle de proceso (`/procesos/{id}`).
2. Aplica filtros: texto libre, fase, subfase (precargado `processId`).
3. Sistema consulta evidencias (versión vigente) con paginación.
4. Resultados muestran fase/subfase; enlace **Ir a subfase** hace scroll al bloque correspondiente.

## Excepciones y flujos alternos

| ID | Condición | Comportamiento |
|----|-----------|----------------|
| A1 | Sin resultados | Mensaje con sugerencia de ampliar filtros |
| A2 | [CC] sin carrera asignada | `403 PROGRAM_SCOPE_DENIED` |

## Postcondiciones

Lista paginada acotada al rol ([CC] solo su carrera; [TD]/[JD] global con filtros).

## Criterio de éxito

Tarea E2E mediana ≤ **2 min** (piloto).

## Diagramas

- [Búsqueda FTS multifiltro](../diagramas/MAR-SEQ-007-busqueda-fts-multifiltro.mmd)
- [AYL búsqueda FTS](../diagramas/AYL-SEQ-007-busqueda-fts.mmd)

## Escenarios Gherkin

```gherkin
# language: es
@PRD-US-004 @FSD-UC-007 @NFR-002 @TC-14
Característica: Búsqueda de Evidencia

  Escenario: Búsqueda en proceso con resultados
    Dado un usuario autenticado en el detalle de un proceso con evidencias
    Cuando busca por fase y término conocido
    Entonces el sistema muestra resultados con fase y subfase
    Y puede navegar a la subfase correspondiente

  Escenario: Sin resultados
    Dado que no existen evidencias que coincidan con el filtro
    Cuando ejecuta la búsqueda
    Entonces el sistema muestra sugerencia de ampliar filtros
```
