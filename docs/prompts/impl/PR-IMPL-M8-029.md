---
id: PR-IMPL-M8-029
feature_asociado: DD-UC-029
fsd_uc:
  - "FSD-UC-029"
  - "FSD-UC-028"
fecha: "2026-10-13"
version: "1.0"
estado: Listo
autor: "Cursor Agent (M8 FODA / E2→E3)"
skill_origen: sigesa-orchestrator
alcance: full-stack
depende_de:
  - "PR-IMPL-M6-025"
  - "PR-IMPL-M7-004"
bloquea_a:
  - "M9 Plan de Mejoras"
---

# Prompt Contract — Implementación `PR-IMPL-M8-029`

> **Design doc:** [`DD-UC-029`](../../design/DD-UC-029.md)  
> **FSD:** [UC-029](../../product/uc/FSD-UC-029.md), [UC-028](../../product/uc/FSD-UC-028.md)  
> **ADR:** [ADR-0005](../../adr/ADR-0005-workflow-metodologico-evaluacion-transversal.md)  
> **Milestone:** M8 (release 2.1.0)

---

## 1. Propósito y Objetivo

Implementar **FODA por indicador**, **métricas de completitud** y hacer **operativa** la compuerta **E2→E3**, más la compuerta de **cierre E3**:

- Flyway **V20** (`indicator_analysis`) + backfill entregables E3.
- PUT/GET `IndicatorAnalysis`; GET `evaluation-metrics`; POST/GET `survey-batches`.
- `StageGateEvaluator`: COLLECTION (ya existe) + SYSTEMATIZATION (nuevo).
- Bootstrapper: añadir entregables faltantes a etapas ya persistidas.
- UI: FODA en indicador, panel métricas, registro de encuesta en timeline E2.
- Tests ≥90 % en servicios nuevos; no romper WebMvc/workflow existente.

**No** usar versión Flyway ≤ V19.

## 2. Rol

Eres el implementador hexagonal SIGESA (Java 21 / Spring Boot 4 / React 19). Dominio puro; DTOs en web; Orval only en frontend; tokens Tailwind institucionales.

## 3. Límites de Alcance

### In-Scope

| Área | Entregables |
|------|-------------|
| Dominio | `IndicatorAnalysis`, `FodaQuadrant`, `ComplianceJudgment`; `PrimarySurveyBatch` de dominio si falta |
| Puertos | Extender `EvaluationMetricsPort` (`countFodaCompleteByProcessId`); `IndicatorAnalysisPort`; casos de uso upsert/get analysis, metrics, register/list survey |
| Servicios | Upsert FODA; Get metrics; Register/List survey; `evaluateSystematization`; ensure E3 deliverables |
| Persistencia | JPA + V20; reutilizar `primary_survey_batches` |
| REST / OpenAPI | API-WF-09…12; WF-07 sin cambio de ruta |
| Frontend | Métricas + form FODA + form encuesta; Orval |
| Docs | `api_contracts.md`, `DTP.md` changelog M8 |

### Out-of-Scope

- `CAE_MEMBER`, `IndicatorDelegation`.
- M9 informes / Plan de Mejoras, M10 freeze/[EE], M11 post-acreditación.
- SMTP UC-015, bitácora UC-017.
- Varios análisis históricos por indicador (un registro, upsert).

## 4. Restricciones

| ID | Regla |
|----|-------|
| R1 | Dominio sin Spring/JPA. |
| R2 | UNIQUE `indicator_id` en `indicator_analysis`. |
| R3 | FODA completo = quadrant + compliance + synthesis no vacío. |
| R4 | PUT analysis solo rol CC; GET CC/TD/JD. |
| R5 | Survey `responsesCount >= 1`; audience 1–32. |
| R6 | Asset opcional de encuesta debe pertenecer al mismo proceso. |
| R7 | E2 gate: evidencias 100% + ≥1 lote + entregables E2 APPROVED. |
| R8 | E3 gate: FODA 100% (si totalIndicators=0 → BLOCK) + entregables E3 APPROVED. |
| R9 | Frontend solo Orval; sin hex arbitrarios. |
| R10 | Tras código: `@dtp-sync` + `@save-prompt-mapping fsd=FSD-UC-029`. |

## 5. Contratos de salida (API)

Ver DD-UC-029 § API. Códigos error alineados a handlers existentes (`GlobalExceptionHandler` / `ErrorCode`).

## 6. Criterios de aceptación

- [ ] V20 aplicada; procesos existentes reciben entregables E3.
- [ ] PUT FODA upsert; GET 404 `ANALYSIS_NOT_FOUND` si no hay fila.
- [ ] GET metrics coherente con conteos.
- [ ] POST survey-batch 201; E2 gate pasa con evidencia+lote+entregables.
- [ ] E3 gate BLOCK si falta FODA o entregable E3.
- [ ] UI CC puede guardar FODA y registrar encuesta; TD ve métricas y gate.
- [ ] `./mvnw test` y `pnpm test:run` verdes en el alcance.
- [ ] Paso 3c Docker smoke + Paso 5 PM-016.

## 7. Fuera / no tocar

- `docs/baseline/**`
- M6/M7 salvo dependencias (gate E2, DocumentAsset para `documentAssetId` de encuesta).
