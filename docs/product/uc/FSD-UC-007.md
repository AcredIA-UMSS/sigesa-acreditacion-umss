---
id: FSD-UC-007
nombre: Buscar Evidencia
estado: Pendiente
release: v1.0
actor_principal: "[CC] (alcance carrera), [TD] (global)"
trazabilidad_prd: PRD-US-004
modulo: MOD-EVIDENCE
reglas: —
ultima_actualizacion: "2026-06-15"
---

# FSD-UC-007 — Buscar Evidencia

## Contexto

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-015 · PRD-US-004 · NFR-002 |
| **API** | `GET /evidences/search` |

## Flujo principal

1. Usuario aplica filtros: carrera, Fase, Indicador, texto, gestión.
2. Sistema consulta índice de búsqueda (FTS multifiltro).
3. Presenta resultados paginados con enlace directo a Evidencia/Indicador.

## Excepciones y flujos alternos

| ID | Condición | Comportamiento |
|----|-----------|----------------|
| A1 | Sin resultados | Mensaje con sugerencia de ampliar filtros |

## Postcondiciones

Lista paginada de evidencias acotada al rol ([CC] solo su carrera).

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

  Escenario: Búsqueda con resultados en tiempo de tarea acotado
    Dado un [TD] autenticado con Evidencias indexadas en el piloto
    Cuando busca por carrera, Fase e Indicador con término conocido
    Entonces el sistema muestra resultados relevantes
    Y la tarea completa de localizar y abrir la Evidencia correcta toma como máximo 2 minutos

  Escenario: Sin resultados
    Dado que no existen Evidencias que coincidan con el filtro
    Cuando ejecuta la búsqueda
    Entonces el sistema muestra "No se encontraron resultados" con sugerencia de ampliar filtros
```
