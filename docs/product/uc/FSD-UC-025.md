---
id: FSD-UC-025
nombre: Timeline etapas metodológicas
estado: Implementado
release: v2.1
actor_principal: "[CC], [TD], [JD]"
trazabilidad_prd: PRD-US-025
modulo: MOD-WORKFLOW
reglas: FSD-BR-25.1, FSD-BR-25.2
ultima_actualizacion: "2026-09-09"
design_doc: DD-UC-025
pr_impl: PR-IMPL-M6-025
nota_implementacion: "M6: 7 etapas + entregables E1–E2 bootstrap al crear proceso; timeline UI en detalle; API-WF-04…08. UC-026–028 comparten DD/PR-IMPL."
---

# FSD-UC-025 — Timeline etapas metodológicas

## Contexto

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | [ADR-0005](../../adr/ADR-0005-workflow-metodologico-evaluacion-transversal.md) · [FSD §8–§10](../FSD.md) |
| **Design Doc** | [`DD-UC-025`](../../design/DD-UC-025.md) |
| **Relacionados** | FSD-UC-026 (submit) · FSD-UC-027 (aprobar/observar) · FSD-UC-028 (compuerta) — ver [FSD §9](../FSD.md#9-etapas-metodológicas-y-entregables) |
| **Precondiciones** | Proceso `ACTIVE`; usuario autenticado con rol CC, TD o JD |
| **Pantalla** | `/procesos/{processId}` — sección **Timeline metodológico** |

Al crear un proceso (UC-003), el sistema instancia **7 etapas metodológicas** (`MethodologicalStage`) y sus **entregables** de catálogo E1–E2. La etapa 1 inicia en `IN_PROGRESS`; el resto en `PENDING`. `currentStageId` apunta a E1.

> **Principio ADR-0005:** N1 (Dimensión/Área) **≠** Etapa metodológica. El workflow es el eje **temporal**; el árbol normativo es **transversal**.

## Flujo principal — Consultar timeline

1. [CC], [TD] o [JD] abre detalle del proceso.
2. El sistema lista las 7 etapas ordenadas (`stageOrder` 1–7) con estado, fechas y entregables.
3. La UI resalta la etapa activa (`currentStageId`) y muestra badges de estado por etapa.
4. [TD] puede aprobar entregables individuales (E1/E2) desde la timeline.
5. [CC] puede enviar la etapa activa a revisión (UC-026).
6. [TD]/[JD] aprueban u observan etapa en revisión (UC-027); la aprobación exige compuerta PASS (UC-028).

## Estados de etapa

`PENDING` → `IN_PROGRESS` → `SUBMITTED_FOR_REVIEW` → (`OBSERVED` | `APPROVED`)

## Excepciones

| ID | Condición | Respuesta |
|----|-----------|-----------|
| A1 | Proceso inexistente o fuera de alcance [CC] | `404 PROCESS_NOT_FOUND` |
| A2 | Etapa inexistente en el proceso | `404 STAGE_NOT_FOUND` |
| A3 | Aprobar etapa con compuerta fallida | `409 STAGE_GATE_BLOCKED` + `failedRules[]` |
| A4 | Transición de estado inválida | `409 INVALID_STAGE_STATE` |
| A5 | Rol no autorizado | `403 FORBIDDEN_ROLE` |

## Postcondiciones

- Timeline refleja estado persistido en `methodological_stages` y `stage_deliverables`.
- Aprobación de etapa registra `stage_gate_evaluations` (append-only).

## API (resumen)

Ver [`api_contracts.md`](../api_contracts.md) § API-WF-04…08.

| Método | Ruta | Rol |
|--------|------|-----|
| GET | `/api/v1/processes/{processId}/stages` | CC, TD, JD |
| POST | `…/stages/{stageId}/deliverables/{deliverableId}/approve` | TD |

## Fuera de alcance (M6)

- Métricas completitud global (UC-029).
- Congelamiento / Sala de Pares (UC-030).
- Plan de Mejoras post-acreditación (UC-031).
- Entregables E3–E7 (M8–M11).

## Escenarios Gherkin

```gherkin
# language: es
Escenario: Timeline visible al abrir proceso
  Dado un proceso ACTIVE con etapas bootstrap E1–E7
  Cuando [CC] abre /procesos/{id}
  Entonces ve 7 etapas ordenadas con E1 en IN_PROGRESS

Escenario: Aprobar entregable E1
  Dado entregable E1 en estado PENDING
  Cuando [TD] aprueba el entregable
  Entonces el entregable queda APPROVED y la timeline se actualiza
```
