# ADR-0005: Workflow metodológico y evaluación transversal (release 2.1+)

| Campo | Valor |
|-------|-------|
| **ID canónico** | ADR-0005 |
| **Estado** | **Aceptada** |
| **Fecha** | 2026-09-08 |
| **Release objetivo** | `2.1.0` (M6–M11) |
| **Alcance** | MOD-PROCESS · MOD-WORKFLOW · MOD-EVIDENCE · MOD-REPORT |
| **Trazabilidad** | FSD.md §8–§10 · [`modelo_datos.md`](../product/modelo_datos.md) §9–§11 · UC-003…010 (base v2.0) · UC-025…028 **implementados (M6)** · UC-029…031 planificados |
| **Relacionada con** | [ADR-0004](ADR-0004-normative-hierarchy-v2.md) (jerarquía normativa v2 — **no se revierte**) |

## Contexto

### Problema

[ADR-0004](ADR-0004-normative-hierarchy-v2.md) resolvió la **taxonomía evaluadora** (CEUB/ARCU-SUR: N1→N2→N3→Indicador→Evidencia) y retiró el cronograma legacy Fase/Subfase (M5). Sin embargo, la metodología institucional de acreditación UMSS no es solo un árbol de indicadores: es un **proceso de gestión** de 7 etapas metodológicas (+ post-acreditación) con entregables formales, compuertas de avance, roles ampliados y documentos de salida (Informe de Autoevaluación, Formulario CNACU, Plan de Mejoras, etc.).

**Error de modelado a evitar:** tratar el Módulo de Evaluación como “la Etapa 1” o como un repositorio estático. En la práctica:

- El **Workflow metodológico** marca el **tiempo** (Preparatoria → Recolección → … → Post-acreditación).
- El **Módulo de Evaluación** (árbol normativo + evidencias + FODA + métricas) es **transversal**: se crea al inicio, se alimenta en casi todas las etapas, se congela para evaluadores externos y se reabre parcialmente para el Plan de Mejoras.

### Drivers

- Metodología DUEA/ODE: convocatoria, CAE, reacreditaciones con informe de recomendaciones previas.
- CEUB (10 áreas) y ARCU-SUR (4 dimensiones, 203 indicadores) comparten el **mismo ciclo metodológico** pero distinta taxonomía (ya resuelta en ADR-0004).
- Requisitos transversales: redireccionamiento documental M:N, foliado/versionado, exportación por carpetas, modo auditoría (Sala de Pares).
- SIGESA v2.0 ya implementa el **núcleo evaluador** (UC-003…010, M5); falta el **orquestador metodológico**.

## Alternativas consideradas

| # | Alternativa | Pros | Contras | Veredicto |
|---|-------------|------|---------|-----------|
| **A** | **Dos módulos desacoplados** (Workflow + Evaluación) unidos por puertos de métricas y compuertas | Alineación metodológica; CEUB/ARCU comparten workflow; evaluación reutilizable | Más entidades y servicios | **Elegida** |
| **B** | Reintroducir “Fases metodológicas” como N1 del árbol normativo | Un solo árbol UI | Confunde dimensión normativa con etapa temporal; rompe ADR-0004 | Rechazada |
| **C** | Workflow como estados del `AccreditationProcess` sin entidades de etapa | Implementación mínima | Sin entregables, compuertas ni trazabilidad por hito | Rechazada |
| **D** | Un documento BPMN externo; SIGESA solo repositorio | Simple en código | No soporta compuertas alimentadas por métricas del árbol | Rechazada |

## Decisión

### 1. Arquitectura dual (bounded contexts)

```
┌─────────────────────────────────────────────────────────────┐
│  MOD-GESTIÓN / WORKFLOW (eje temporal)                       │
│  AccreditationProcess.currentStage                           │
│  MethodologicalStage (×7 + post-acreditación)                │
│  StageDeliverable, StageGateEvaluation, ProcessFreeze        │
│  ImprovementPlanItem (Etapa 7)                             │
└───────────────────────────┬─────────────────────────────────┘
                            │ consulta (puertos)
                            ▼
┌─────────────────────────────────────────────────────────────┐
│  MOD-EVALUACIÓN (eje transversal — ADR-0004, implementado)   │
│  N1→N2→N3→Indicator→Evidence                                 │
│  IndicatorAnalysis (FODA), DocumentAsset + links M:N         │
│  EvaluationMetricsPort → completitud, vacíos, FODA           │
└─────────────────────────────────────────────────────────────┘
```

**Regla de oro:** el dominio Workflow **no** conoce la semántica CEUB vs ARCU-SUR. Solo invoca `EvaluationMetricsPort` y `StageGateRule` configurables.

### 2. Siete etapas metodológicas (+ post-acreditación)

| Orden | Código | Nombre | Uso principal del Módulo de Evaluación |
|-------|--------|--------|----------------------------------------|
| 1 | `PREPARATORY` | Preparatoria y administrativa | Revisión cumplimiento recomendaciones **proceso anterior** |
| 2 | `COLLECTION` | Recolección de información | Carga evidencias secundaria/primaria en indicadores |
| 3 | `SYSTEMATIZATION` | Sistematización y análisis | FODA y juicios de valor por indicador/N1 |
| 4 | `DRAFTING` | Elaboración documentos clave | Consolidación → Informe, Formulario, Planes |
| 5 | `PRESENTATION` | Presentación y validación | Snapshot; actas de socialización |
| 6 | `EXTERNAL_PREP` | Preparación evaluación externa | **Congelamiento** + Sala de Pares + rol [EE] |
| 7 | `POST_ACCREDITATION` | Seguimiento post-acreditación | Plan de Mejoras cíclico (evidencias de avance) |

Estados de etapa: `PENDING` | `IN_PROGRESS` | `SUBMITTED_FOR_REVIEW` | `OBSERVED` | `APPROVED`.

### 3. Compuertas de avance (gateways)

El cierre de una etapa **no** es un botón libre; `StageGateEvaluator` consulta reglas:

| Transición | Regla mínima (resumen) | Código error propuesto |
|------------|------------------------|-------------------------|
| Cerrar E1 | Entregables `HCC_RESOLUTION`, `PRIOR_RECOMMENDATIONS_REPORT` aprobados por [TD] | `409 STAGE_GATE_BLOCKED` |
| E2 → E3 | 100% indicadores con ≥1 evidencia; encuestas primarias registradas | idem |
| Cerrar E3 | FODA completos en scope configurado (por N1 o global) | idem |
| E4 → E5 | Documentos oficiales generados y adjuntos como entregables | idem |
| Entrar E6 | `ProcessFreeze.mode = AUDIT_READONLY` | idem |
| E7 (ciclos) | Items Plan de Mejoras con evidencia de avance en periodo | idem |

**Analogía con lo existente:** UC-010 (cierre N1) es compuerta **local** dentro del módulo de evaluación. Las compuertas metodológicas son **globales** sobre el proceso.

### 4. Entidades nuevas (resumen lógico)

Ver [`modelo_datos.md`](../product/modelo_datos.md) §10 y [`MAR-ER-003-workflow-metodologico.mmd`](../product/diagramas/MAR-ER-003-workflow-metodologico.mmd).

| Entidad | Propósito |
|---------|-----------|
| `MethodologicalStage` | Instancia de etapa 1–7 por proceso |
| `StageDeliverable` | Hito documental requerido (Resolución HCC, Informe, Acta, etc.) |
| `StageGateEvaluation` | Registro append-only de evaluación de compuerta |
| `DocumentAsset` | Blob único institucional (folio, hash, storage) |
| `IndicatorEvidenceLink` | M:N indicador ↔ documento (redireccionamiento sin duplicar blob) |
| `IndicatorAnalysis` | FODA + juicio cumplimiento por indicador |
| `ImprovementPlanItem` | Fila Plan de Mejoras (origen: debilidad FODA) |
| `ProcessFreeze` | Modo `NONE` \| `AUDIT_READONLY` \| `IMPROVEMENT_PARTIAL` |
| `PrimarySurveyBatch` | Registro agregado encuestas (Etapa 2) |

**Evolución de evidencia:** `Evidence` actual (1:1 indicador) se mantiene en M6; en **M7** el blob físico migra a `DocumentAsset` y `Evidence` pasa a ser enlace lógico + metadatos de versión por indicador.

### 5. Roles institucionales → SIGESA

| Rol institucional | Rol SIGESA | Atribución ampliada (ADR-0005) |
|-------------------|------------|--------------------------------|
| Técnico DUEA/ODE | [TD] | Aprueba indicadores (UC-009/008), cierra N1 (UC-010), **aprueba entregables de etapa** y ejecuta compuertas |
| Director de Carrera | [JD] | Configuración, semáforo; **autoriza paso formal entre etapas** (firma metodológica) |
| Coordinador del Proceso | [CC] | Evidencias, subsanación; **envía etapa a revisión**; gestiona cronograma |
| Miembros CAE / auxiliares | `CAE_MEMBER` *(nuevo)* | Carga evidencias y FODA en ámbito delegado (N1/N2/indicador) |
| Par evaluador / auditor | [EE] | Solo lectura en `AUDIT_READONLY`; checklist Sala de Pares |

RBAC: permisos con alcance `{ proceso | stage | level1 | indicator }`.

### 6. Modos operativos del proceso

| Modo | Evaluación editable | Workflow |
|------|---------------------|----------|
| `ACTIVE` | Según etapa activa | Avance con compuertas |
| `AUDIT_READONLY` | Solo lectura ([EE], [TD]) | Etapa 6; sin retroceso destructivo |
| `IMPROVEMENT_MONITORING` | Parcial (items plan mejora) | Etapa 7 cíclica |

Campo propuesto: `AccreditationProcess.operationalMode` + `currentStageId`.

### 7. Roadmap de implementación

| Milestone | Entregable | Depende de |
|-----------|------------|------------|
| **M6** | `MethodologicalStage` + entregables E1–E2 + UI timeline | Proceso v2 ✅ (M5) |
| **M7** | `DocumentAsset` + `IndicatorEvidenceLink` (M:N) | UC-004/005 |
| **M8** | `IndicatorAnalysis` (FODA) + compuerta E2→E3 | M6, métricas |
| **M9** | Generación documentos E4 + `ImprovementPlanItem` | M8 |
| **M10** | `ProcessFreeze` + [EE] + checklist Sala de Pares | UC-020 |
| **M11** | Etapa 7 cíclica + archivo fotografía proceso | M9 |

### 8. Contratos API objetivo (planificados)

| Operación | Endpoint (borrador) | UC planificado |
|-----------|----------------------|----------------|
| Listar etapas del proceso | `GET /api/v1/processes/{id}/stages` | UC-025 |
| Enviar etapa a revisión | `POST /api/v1/processes/{id}/stages/{stageId}/submit` | UC-026 |
| Aprobar/rechazar etapa | `POST /api/v1/processes/{id}/stages/{stageId}/approve` | UC-027 |
| Evaluar compuerta (preview) | `GET /api/v1/processes/{id}/stages/{stageId}/gate` | UC-028 |
| Métricas completitud | `GET /api/v1/processes/{id}/evaluation-metrics` | UC-029 |
| Congelar / descongelar | `POST /api/v1/processes/{id}/freeze` | UC-030 |
| Plan de mejoras CRUD | `/api/v1/processes/{id}/improvement-plan` | UC-031 |

## Consecuencias

### Positivas

- Plataforma integral de aseguramiento de calidad (no solo repositorio de una fase).
- CEUB y ARCU-SUR comparten workflow metodológico; taxonomía ya unificada (ADR-0004).
- Compuertas auditable (`StageGateEvaluation` append-only).
- Redireccionamiento documental M:N sin duplicar almacenamiento.

### Negativas / Riesgos

- Incremento significativo de superficie (6 milestones, ~7 UC nuevos).
- Migración M7 de evidencias 1:1 → DocumentAsset requiere script Flyway cuidadoso.
- Roles `CAE_MEMBER` y ampliación [JD] implican cambios RBAC y UI de delegación.
- Generación documental (M9) depende de plantillas oficiales CNACU/CEUB externas.

### Neutras

- UC-004…010 y árbol normativo **permanecen** como están; ADR-0005 **extiende**, no reemplaza ADR-0004.
- Dashboards (UC-011…013) consumirán `EvaluationMetricsPort` cuando exista.

## Cumplimiento

| Artefacto | Acción |
|-----------|--------|
| [`modelo_datos.md`](../product/modelo_datos.md) | §9–§11 añadidos (v2.1 diseño) |
| [`FSD.md`](../product/FSD.md) | §8–§10; UC-025…028 **Implementado** (M6); UC-029…031 Planificado |
| [`MAR-ER-001`](../product/diagramas/MAR-ER-001-modelo-datos-nucleo.mmd) | Actualizado v2.0 normativo |
| [`MAR-ER-003`](../product/diagramas/MAR-ER-003-workflow-metodologico.mmd) | Nuevo ER integrado |
| `api_contracts.md`, `DTP.md` | ✅ Sincronizados M6 (API-WF-04…08, V17, §B.8 DTP) |
| DD-UC-025 | ✅ Creado e implementado (PR-IMPL-M6-025) |
| DD-UC-026…031 | Crear antes de M7–M11 |

## Referencias

- [`docs/product/FSD.md`](../product/FSD.md) — LFSD §8–§10
- [ADR-0004](ADR-0004-normative-hierarchy-v2.md) — Jerarquía normativa v2.0
- Metodología institucional CEUB/ARCU-SUR (convocatoria UMSS / CNACU)
