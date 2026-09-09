---
id: DD-UC-025
fsd_ref: FSD-UC-025
titulo: "Timeline etapas metodológicas"
modulo: MOD-WORKFLOW
estado: Aprobado
fecha: "2026-09-08"
prompts:
  - PR-IMPL-M6-025
---

# DD-UC-025 — Timeline etapas metodológicas

## Contexto

[FSD-UC-025](../product/uc/FSD-UC-025.md) · [ADR-0005](../adr/ADR-0005-workflow-metodologico-evaluacion-transversal.md) · Contrato [API-WF-04](../product/api_contracts.md).

Release **2.1.0 / M6**: instanciar las 7 etapas metodológicas por proceso, entregables E1–E2, timeline UI y transiciones básicas submit/aprobar.

## Reglas

| ID | Regla |
|----|-------|
| BR-25.1 | Al crear proceso se generan 7 `MethodologicalStage` + entregables catálogo E1/E2 |
| BR-25.2 | Etapa 1 inicia `IN_PROGRESS`; resto `PENDING`; `currentStageId` apunta a E1 |
| BR-25.3 | Solo [CC] puede enviar etapa activa a revisión (`SUBMITTED_FOR_REVIEW`) |
| BR-25.4 | Solo [TD] o [JD] aprueban/rechazan etapa en revisión |
| BR-25.5 | Aprobar etapa exige compuerta `PASS`; fallo → `409 STAGE_GATE_BLOCKED` |
| BR-25.6 | Solo [TD] aprueba entregables individuales (`StageDeliverable`) |
| BR-25.7 | Compuerta E1: entregables obligatorios E1 = `APPROVED` |
| BR-25.8 | Compuerta E2: 100% indicadores con evidencia + ≥1 `PrimarySurveyBatch` |

## Modelo de datos

Ver [`modelo_datos.md`](../product/modelo_datos.md) §10 · Flyway `V17__methodological_workflow_stages.sql`.

| Tabla | Notas |
|-------|-------|
| `methodological_stages` | 7 filas/proceso; `stage_order` 1–7 |
| `stage_deliverables` | Catálogo E1 (4) + E2 (3) al bootstrap |
| `stage_gate_evaluations` | Append-only al aprobar etapa |
| `primary_survey_batches` | Registro encuestas E2 (vacío hasta carga) |
| `accreditation_processes` | +`operational_mode`, +`current_stage_id` |

Enum `MethodologicalStageStatus`: `PENDING` · `IN_PROGRESS` · `SUBMITTED_FOR_REVIEW` · `OBSERVED` · `APPROVED`.

## API

| Método | Ruta | Rol | UC |
|--------|------|-----|-----|
| GET | `/api/v1/processes/{processId}/stages` | CC, TD, JD | UC-025 |
| POST | `/api/v1/processes/{processId}/stages/{stageId}/submit` | CC | UC-026 |
| POST | `/api/v1/processes/{processId}/stages/{stageId}/approve` | TD, JD | UC-027 |
| POST | `/api/v1/processes/{processId}/stages/{stageId}/observe` | TD, JD | UC-027 |
| GET | `/api/v1/processes/{processId}/stages/{stageId}/gate` | TD | UC-028 |
| POST | `/api/v1/processes/{processId}/stages/{stageId}/deliverables/{deliverableId}/approve` | TD | UC-025 ext. |

## Capas (hexagonal)

| Capa | Artefacto |
|------|-----------|
| Port in | `ListProcessStagesUseCase`, `SubmitStageForReviewUseCase`, `ApproveStageUseCase`, `ObserveStageUseCase`, `EvaluateStageGateUseCase`, `ApproveStageDeliverableUseCase` |
| Port out | `MethodologicalStagePort`, `EvaluationMetricsPort` |
| Service | `MethodologicalStageBootstrapper`, `ListProcessStagesService`, `SubmitStageForReviewService`, `ApproveStageService`, `StageGateEvaluator` |
| Adapter in | `StageWorkflowController` |
| Adapter out | `MethodologicalStageJpaAdapter`, `EvaluationMetricsJpaAdapter` |

## Frontend

- Componente `MethodologicalStageTimeline` en detalle de proceso.
- Badges de estado por etapa; acciones submit (CC) y aprobar entregable (TD).
- Manejo `STAGE_GATE_BLOCKED` con reglas fallidas.

## Impacto specs vivas

- [x] `docs/product/FSD.md` — UC-025…028 → Implementado (M6)
- [x] `docs/product/uc/FSD-UC-025.md` — atom UC
- [x] `docs/product/api_contracts.md` — API-WF-04…08
- [x] `docs/product/DTP.md` — §B.8, V17, changelog M6
- [x] `docs/prompts/impl/PR-IMPL-M6-025.md`
- [x] `docs/sprints/sprint_03/PROMPT_MAPPING.md` — PM-010
