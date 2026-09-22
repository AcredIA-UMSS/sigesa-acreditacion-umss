---
id: PR-IMPL-M6-025
feature_asociado: DD-UC-025
fsd_uc:
  - "FSD-UC-025"
  - "FSD-UC-026"
  - "FSD-UC-027"
  - "FSD-UC-028"
fecha: "2026-09-09"
version: "1.0"
estado: Ejecutado
autor: "Cursor Agent (M6 workflow metodológico)"
skill_origen: sigesa-orchestrator
alcance: full-stack
depende_de:
  - "PR-IMPL-003"
bloquea_a:
  - "M7 DocumentAsset"
  - "M8 FODA compuertas E3"
---

# Prompt Contract — Implementación `PR-IMPL-M6-025`

> **Design doc fuente:** [`DD-UC-025`](../../design/DD-UC-025.md)  
> **FSD:** UC-025…028 · **ADR:** [ADR-0005](../../adr/ADR-0005-workflow-metodologico-evaluacion-transversal.md)  
> **Milestone:** M6 (release 2.1.0 parcial)

---

## 1. Propósito y Objetivo

Implementar el **workflow metodológico base** (7 etapas, entregables E1–E2, transiciones submit/approve/observe, compuertas preview) según ADR-0005 y DD-UC-025:

- Flyway **V17** — tablas `methodological_stages`, `stage_deliverables`, `stage_gate_evaluations`, `primary_survey_batches`; columnas `operational_mode`, `current_stage_id` en `accreditation_processes`.
- Bootstrap de etapas al crear proceso (`MethodologicalStageBootstrapper`).
- API **API-WF-04…08** vía `StageWorkflowController`.
- UI timeline en detalle de proceso (`MethodologicalStageTimeline` + container Orval).
- Tests unitarios `StageGateEvaluatorTest`.

---

## 2. Límites de Alcance

### In-Scope

| Área | Entregables |
|------|-------------|
| **Dominio** | `MethodologicalStage`, `StageDeliverable`, enums status/code, excepciones gate/state |
| **Puertos IN** | List/Submit/Approve/Observe/EvaluateGate/ApproveDeliverable use cases |
| **Puertos OUT** | `MethodologicalStagePort`, `EvaluationMetricsPort` |
| **Servicios** | `MethodologicalStageBootstrapper`, `StageGateEvaluator`, servicios workflow |
| **Persistencia** | JPA entities, adapters, repositorios Spring Data |
| **REST** | `StageWorkflowController` + DTOs respuesta |
| **Frontend** | Timeline en `ProcessDetailView`; hooks Orval `stage-workflow` |
| **Docs** | `FSD-UC-025`, `api_contracts.md` WF-04…08, sync FSD/DTP/ADR |

### Out-of-Scope

- UC-029 métricas completitud endpoint dedicado.
- UC-030 congelamiento / modo `AUDIT_READONLY`.
- UC-031 Plan de Mejoras.
- Entregables E3–E7 y compuertas FODA/documentales (M8–M11).
- `DocumentAsset` M:N (M7).

---

## 3. Restricciones

| ID | Regla |
|----|-------|
| R1 | Dominio puro; sin Spring/JPA en domain. |
| R2 | Compuerta fallida → `409 STAGE_GATE_BLOCKED` con `failedRules[]`. |
| R3 | Bootstrap idempotente: 7 etapas + catálogo E1/E2 por proceso nuevo. |
| R4 | Solo Orval en frontend; prohibido fetch manual. |
| R5 | Post-ejecución: `@save-prompt-mapping` + `@dtp-sync`. |

---

## 4. Validación

- [x] `./mvnw test` — suite backend (~286 tests) OK incl. `StageGateEvaluatorTest`
- [x] Orval regenerado — `frontend/src/api/endpoints/stage-workflow/`
- [ ] Docker smoke E2E timeline (operativo: rebuild backend para V17 / `operational_mode`)

---

## 5. Trazabilidad documental

| Artefacto | Acción |
|-----------|--------|
| `docs/product/uc/FSD-UC-025.md` | Crear atom UC |
| `docs/product/FSD.md` | UC-025…028 Implementado; §8/T-014; reconciliar índice v2 |
| `docs/product/DTP.md` | Changelog M6 + §B.8 MOD-WORKFLOW metodológico |
| `docs/adr/ADR-0005` | Trazabilidad UC-025…028 implementados (M6) |
| `docs/sprints/sprint_03/PROMPT_MAPPING.md` | PM-010 |
