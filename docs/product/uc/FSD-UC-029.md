---
id: FSD-UC-029
nombre: Métricas de completitud y análisis FODA
estado: Hecho (M8)
release: v2.1
actor_principal: "[CC], [TD], [JD]"
trazabilidad_prd: PRD-US-025
modulo: MOD-EVALUATION
reglas: FSD-BR-29.1, FSD-BR-29.2, FSD-BR-29.3
ultima_actualizacion: "2026-10-13"
design_doc: DD-UC-029
pr_impl: PR-IMPL-M8-029
nota_implementacion: "M8: IndicatorAnalysis (FODA) + GET evaluation-metrics + registro encuestas primarias. Compuerta E2→E3 (UC-028) consume estas métricas. CAE_MEMBER fuera de este corte."
---

# FSD-UC-029 — Métricas de completitud y análisis FODA

## Contexto

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | [ADR-0005](../../adr/ADR-0005-workflow-metodologico-evaluacion-transversal.md) · [FSD §8–§10](../FSD.md) |
| **Design Doc** | [`DD-UC-029`](../../design/DD-UC-029.md) |
| **Relacionados** | FSD-UC-028 (compuerta E2→E3 / cierre E3) · FSD-UC-004 (evidencia) · FSD-UC-025 (timeline) |
| **Precondiciones** | Proceso `ACTIVE`; usuario autenticado CC, TD o JD |
| **Pantalla** | `/procesos/{processId}` — árbol normativo (formulario FODA por indicador) + panel métricas; timeline (lote de encuesta en E2) |

El módulo de evaluación es **transversal** (ADR-0005): el FODA no es una etapa aislada; se escribe por indicador y alimenta métricas y compuertas.

> **Principio:** Un indicador tiene **a lo sumo un** `IndicatorAnalysis`. Completo = cuadrante FODA + juicio de cumplimiento + síntesis no vacía.

## Flujo principal — Consultar métricas

1. [CC], [TD] o [JD] abre el detalle del proceso.
2. El sistema calcula `EvaluationMetrics`: total de indicadores, con evidencia, con FODA completo, ratio evidencias, ratio FODA, lotes de encuesta primaria.
3. La UI muestra el tablero de completitud (E2/E3).

## Flujo — Registrar / actualizar FODA

1. [CC] abre un indicador del árbol.
2. Completa `fodaQuadrant`, `compliance` y `synthesis`.
3. PUT upsert; el autor queda en `authorId`.
4. Las métricas y el preview de compuerta E3 se actualizan.

## Flujo — Registrar encuesta primaria (habilita E2→E3)

1. [CC] en etapa `COLLECTION` registra un lote (`audience`, `responsesCount`, `documentAssetId` opcional).
2. `countPrimarySurveyBatches ≥ 1` satisface la regla de encuesta de la compuerta E2.

## Excepciones

| ID | Condición | Respuesta |
|----|-----------|-----------|
| A1 | Proceso inexistente o fuera de alcance [CC] | `404 PROCESS_NOT_FOUND` |
| A2 | Indicador inexistente o de otro proceso | `404 INDICATOR_NOT_FOUND` |
| A3 | FODA incompleto (cuadrante, cumplimiento o síntesis vacíos) | `400 VALIDATION_ERROR` |
| A4 | Rol no autorizado a escribir FODA | `403 FORBIDDEN_ROLE` |
| A5 | Asset de encuesta de otro proceso | `403 PROGRAM_SCOPE_DENIED` |
| A6 | Lote con `responsesCount < 1` | `400 VALIDATION_ERROR` |

## Postcondiciones

- Existe a lo sumo una fila `indicator_analysis` por indicador.
- `GET …/evaluation-metrics` refleja evidencias, FODA y lotes de encuesta.
- Compuerta E2 (UC-028) usa evidencias 100% + ≥1 lote; cierre E3 usa FODA 100% + entregables E3.

## API (resumen)

Ver [`api_contracts.md`](../api_contracts.md) § API-WF-09…12.

| Método | Ruta | Rol |
|--------|------|-----|
| GET | `/api/v1/processes/{processId}/evaluation-metrics` | CC, TD, JD |
| GET | `/api/v1/indicators/{indicatorId}/analysis` | CC, TD, JD |
| PUT | `/api/v1/indicators/{indicatorId}/analysis` | CC |
| POST | `/api/v1/processes/{processId}/survey-batches` | CC |
| GET | `/api/v1/processes/{processId}/survey-batches` | CC, TD, JD |

## Fuera de alcance (M8)

- Rol `CAE_MEMBER` e `IndicatorDelegation` (delegación granular).
- Auto-import Plan de Mejoras desde debilidades FODA (UC-031 / M9).
- Entregables E4–E7, congelamiento, Sala de Pares.

## Escenarios Gherkin

```gherkin
# language: es
Escenario: FODA completo cuenta en métricas
  Dado un proceso con 2 indicadores y evidencia en ambos
  Cuando [CC] guarda FODA completo en ambos
  Entonces evaluation-metrics.fodaComplete equals 2
  Y la compuerta SYSTEMATIZATION no falla por FODA

Escenario: Compuerta E2 bloqueada sin encuesta
  Dado 100% indicadores con evidencia y entregables E2 aprobados
  Y 0 lotes de encuesta primaria
  Cuando [TD] previsualiza la compuerta COLLECTION
  Entonces pass es false y failedRules incluye encuesta
```
