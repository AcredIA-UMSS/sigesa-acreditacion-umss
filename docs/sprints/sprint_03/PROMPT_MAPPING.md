# PROMPT_MAPPING — Sprint 03

> Registro PM del sprint 03. Trazabilidad: `Código → PR-IMPL → DD-UC-022 → FSD-UC-022/004 → DTP`.

| ID Mapeo | PR-IMPL | Design Doc | FSD / PRD | Descripción de la Tarea |
| :--- | :--- | :--- | :--- | :--- |
| PM-001 | PR-IMPL-034 | DD-UC-022 | FSD-UC-022 / FSD-UC-004 / FSD-UC-021 | Subfases con `requirements`, evidencias múltiples por subfase, observaciones TD/JD, UI + docs |
| PM-002 | PR-IMPL-035 | DD-UC-005 | FSD-UC-005 | Historial de versiones + bloqueo DELETE append-only (API-EVD-03/04) |
| PM-003 | PR-IMPL-036 | DD-UC-006 | FSD-UC-006 | Subsanación por subfase, una vez por observación OPEN, historial liviano |
| PM-004 | PR-IMPL-037 | DD-UC-007 | FSD-UC-007 | Buscador de evidencias en vista fases/subfases del proceso |
| PM-005 | PR-IMPL-038 | DD-UC-008 / DD-UC-009 | FSD-UC-008 / FSD-UC-009 | Rechazo y aprobación de indicadores vía subfase (TD; requiere evidencia + indicatorId) |
| PM-006 | PR-IMPL-039 | DD-UC-010 | FSD-UC-010 | Cierre de fase TD cuando todas las subfases APROBADO (API-WF-03) |

---

## PM-001

| Campo | Valor |
| --- | --- |
| **ID** | PM-001 |
| **Fecha** | 2026-08-27 |
| **Solicitante** | Boris Anthony Angulo Urquieta |
| **Agente/Entorno** | Cursor IDE — Agent |
| **Tarea** | Extensión subfases: requisitos, evidencias múltiples y observaciones |
| **Objetivo** | Cada subfase expone nombre, descripción y requisitos de completitud; [CC] sube 1..N evidencias; [TD]/[JD] registran observaciones |
| **Contexto** | Evolución de PM-016/PM-019 (carga por subfase sin FK). Flyway V9. API-SUB-01. |
| **PR-IMPL vinculado** | [PR-IMPL-034](../../prompts/impl/PR-IMPL-034.md) |
| **DD vinculado** | [DD-UC-022](../../design/DD-UC-022.md) |
| **FSD / PRD vinculado** | FSD-UC-022 · FSD-UC-004 · FSD-UC-021 |
| **Estado** | completado |

### Prompt usado exacto

```text
quiero que modifiques las subfases,
que tengan los siguientes datos:
nombre_subfase
descripcion_subfase
requisitos_subfase (hara referencia a los requisitos que debe cumplir para que se considere hecha)

ademas tambien que ajustes ciertas cosas si son necesarias como las evidencias, a una subfase se pueden subir 1 o mas evidencias,
tambien que cada subfase tenga un espacio de observacion, donde el tecnico o administrador pueda dar observaciones a la evidencia subida, realiza todo eso y tambien los cambios respectivos en el frontend

actualiza toda la documentacion necesaria y registralo en el prompt_mapping como sprint 3
```

### Archivos generados o modificados

**Backend**

- `db/migration/V9__subphase_requirements_evidence_observations.sql`
- Dominio/JPA: `Subphase`, `TemplateSubphase`, `Evidence`, `SubphaseObservation`
- `SubphaseController`, `SubphaseModuleConfig`, `SubphaseCollaborationJpaAdapter`
- Use cases/servicios subfase evidencias y observaciones
- `ProcessStructureGuard.ensureRequirements()`, `TemplateStructureValidator`
- DTOs CRUD subfase + plantilla con `requirements`

**Frontend**

- `features/subphases/` (api, hooks, `SubphaseCollaborationSection`, `SubphaseObservationPanel`)
- `ProcessPhaseTree.tsx`, `ProcessDetailView.tsx`, `SubphaseEvidenceUploadModal.tsx`
- `ProcessStructureEditorUI.tsx`, `ProcessStructureView.tsx`
- `admin/templates/*` (requisitos en editor plantillas)
- Orval models: `subphaseDto`, `createSubphaseRequestDto`, `updateSubphaseRequestDto`, `templateSubphase*`

**Documentación**

- `docs/design/DD-UC-022.md`
- `docs/product/uc/FSD-UC-022.md`, `FSD-UC-004.md`
- `docs/product/api_contracts.md`, `FSD.md`, `DTP.md`
- `docs/prompts/impl/PR-IMPL-034.md`
- `docs/sprints/sprint_03/PROMPT_MAPPING.md` (este archivo)

### Cambios realizados

1. **Modelo:** `requirements` en subfases de proceso y plantilla; backfill V9 desde descripción existente.
2. **Evidencias:** FK `evidence.subphase_id`; indicador opcional en upload por subfase; listado por subfase.
3. **Observaciones:** tabla `subphase_observation`; POST solo TD/JD; lectura para roles autenticados con scope de carrera.
4. **UI:** árbol de proceso muestra requisitos, evidencias cargadas, modal de carga y panel de observaciones.
5. **Docs:** API-SUB-01, DTP §A.1, FSD changelog 2026-08-27.

### Validación ejecutada

- [ ] `./mvnw test` — pendiente entorno Java
- [ ] `pnpm lint` + `tsc -b` — verificar en CI/local
- [ ] Smoke: crear subfase con requisitos → subir 2 evidencias → TD registra observación

---

## PM-002

| Campo | Valor |
| --- | --- |
| **ID** | PM-002 |
| **Fecha** | 2026-08-27 |
| **Solicitante** | Boris Anthony Angulo Urquieta |
| **Agente/Entorno** | Cursor IDE — @sigesa-orchestrator |
| **Tarea** | FSD-UC-005 — Versionado y bloqueo de borrado |
| **Objetivo** | GET historial de versiones; DELETE siempre rechazado (append-only) con auditoría |
| **PR-IMPL vinculado** | [PR-IMPL-035](../../prompts/impl/PR-IMPL-035.md) |
| **DD vinculado** | [DD-UC-005](../../design/DD-UC-005.md) |
| **FSD vinculado** | [FSD-UC-005](../../product/uc/FSD-UC-005.md) |
| **Estado** | completado |

### Prompt usado exacto

```text
@sigesa-orchestrator fsd=FSD-UC-005 FSD sprint=3 solicitante="Boris Anthony Angulo Urquieta"
```

### Archivos generados o modificados

**Backend:** `EvidenceLifecycleController`, `ListEvidenceVersionsService`, `AttemptDeleteEvidenceService`, `EvidenceLifecycleJpaAdapter`, excepciones, `EvidenceModuleConfig`, tests

**Frontend:** `fetchEvidenceVersions.ts`, `EvidenceVersionHistoryPanel.tsx`, integración en `SubphaseObservationPanel`

**Docs:** `DD-UC-005.md`, `PR-IMPL-035.md`, `FSD-UC-005.md`, `FSD.md`, `DTP.md`, este PM-002

### Validación ejecutada

- [ ] `./mvnw test` — pendiente entorno Java
- [x] `pnpm tsc -b` — OK
- [ ] Smoke Docker paso 3c

---

## PM-003

| Campo | Valor |
| --- | --- |
| **ID** | PM-003 |
| **Fecha** | 2026-08-27 |
| **Solicitante** | Boris Anthony Angulo Urquieta |
| **Agente/Entorno** | Cursor IDE — @sigesa-orchestrator |
| **Tarea** | FSD-UC-006 — Subsanación de evidencia en subfase |
| **Objetivo** | [CC] subsana una vez tras observación TD/JD; historial liviano (metadatos sin blob en versiones anteriores) |
| **PR-IMPL vinculado** | [PR-IMPL-036](../../prompts/impl/PR-IMPL-036.md) |
| **DD vinculado** | [DD-UC-006](../../design/DD-UC-006.md) |
| **FSD vinculado** | [FSD-UC-006](../../product/uc/FSD-UC-006.md) |
| **Estado** | completado |

### Prompt usado exacto

```text
@sigesa-orchestrator fsd=FSD-UC-006 FSD sprint=3 solicitante="Boris Anthony Angulo Urquieta"
aplica sobre las subfases, tambien implementa la funciona que solo se pueda subsanar una vez el tecnico o jefe haya rechazado una subfase y subida su respectiva observacion, y ademas implementa una funcion que haga que el historial de las versiones sea mas liviano, y no asi un pedf con peso solo si es posible, actualiza luego la documentacion necesario, si tienes que cambiar algo importante consultamelo
```

### Archivos generados o modificados

**Backend:** Flyway V10; `SubsanateSubphaseEvidenceService`, `GetSubphaseSubsanationEligibilityService`; endpoints en `SubphaseController`; `EvidenceUploadJpaAdapter.persistSubphaseSubsanation`; observación OPEN/RESOLVED; `blob_purged` en `evidence_version`

**Frontend:** `SubphaseSubsanationModal`, `subphaseApi` (eligibility + subsanate), `SubphaseCollaborationSection`, badges observación, `EvidenceVersionHistoryPanel` (`blobAvailable`)

**Docs:** `DD-UC-006`, `PR-IMPL-036`, `FSD-UC-006`, `FSD.md`, `DTP.md`, `api_contracts.md` (API-SUB-02), este PM-003

### Validación ejecutada

- [ ] `./mvnw test` — pendiente entorno Java
- [ ] `pnpm tsc -b` — verificar en CI/local
- [ ] Smoke: TD observación → CC subsana → historial muestra v1 solo metadatos

---

## PM-004

| Campo | Valor |
| --- | --- |
| **ID** | PM-004 |
| **Fecha** | 2026-08-27 |
| **Solicitante** | Boris Anthony Angulo Urquieta |
| **Agente/Entorno** | Cursor IDE — @sigesa-orchestrator |
| **Tarea** | FSD-UC-007 — Buscar evidencias en vista de proceso |
| **Objetivo** | Panel buscador en fases/subfases con filtros texto, fase y subfase; API paginada |
| **PR-IMPL vinculado** | [PR-IMPL-037](../../prompts/impl/PR-IMPL-037.md) |
| **DD vinculado** | [DD-UC-007](../../design/DD-UC-007.md) |
| **FSD vinculado** | [FSD-UC-007](../../product/uc/FSD-UC-007.md) |
| **Estado** | completado |

### Prompt usado exacto

```text
@sigesa-orchestrator fsd=FSD-UC-007 FSD sprint=3 solicitante="Boris Anthony Angulo Urquieta"
en la parte de frontend en la vista de las subfases y subfases debe estar este buscador para buscar evidencias
```

### Archivos generados o modificados

**Backend:** `EvidenceSearchController`, `SearchEvidencesService`, `EvidenceSearchJpaAdapter` (FTS GIN + LIKE fallback), Flyway V11, DTOs, `EvidenceModuleConfig`

**Frontend:** `ProcessEvidenceSearchPanel`, `fetchEvidenceSearch`, `useEvidenceSearch`, integración en `ProcessDetailView`, anclas en `ProcessPhaseTree`

**Docs:** `DD-UC-007`, `PR-IMPL-037`, `FSD-UC-007`, `FSD.md`, `DTP.md`, `api_contracts.md`, este PM-004

### Validación ejecutada

- [ ] `./mvnw test` — pendiente entorno Java
- [ ] `pnpm tsc -b` — verificar en CI/local
- [ ] Smoke: buscar evidencia en `/procesos/{id}` → Ir a subfase

---

## PM-005

| Campo | Valor |
| --- | --- |
| **ID** | PM-005 |
| **Fecha** | 2026-08-27 |
| **Solicitante** | Boris Anthony Angulo Urquieta |
| **Agente/Entorno** | Cursor IDE — @sigesa-orchestrator |
| **Tarea** | FSD-UC-008 + FSD-UC-009 — Rechazo y aprobación de indicadores vía subfase |
| **Objetivo** | [TD] rechaza/aprueba **subfases** solo si tienen evidencia cargada; rechazo crea observación OPEN |
| **PR-IMPL vinculado** | [PR-IMPL-038](../../prompts/impl/PR-IMPL-038.md) |
| **DD vinculado** | [DD-UC-008](../../design/DD-UC-008.md) · [DD-UC-009](../../design/DD-UC-009.md) |
| **FSD vinculado** | [FSD-UC-008](../../product/uc/FSD-UC-008.md) · [FSD-UC-009](../../product/uc/FSD-UC-009.md) |
| **Estado** | completado |

### Prompt usado exacto

```text
@sigesa-orchestrator fsd=FSD-UC-008 y FSD-UC-009 FSD sprint=3 solicitante="Boris Anthony Angulo Urquieta"
Esto con las condiciones de las subfases que te comente en un inicio, estos indicadores hacen referencia a las subfases, se puede rechazar un inidicador solo si tiene su evidencia subida, y lo mismo con aprobar
```

### Archivos generados o modificados

**Backend:** `RejectSubphaseIndicatorService`, `ApproveSubphaseIndicatorService`, `RejectIndicatorService`, `ApproveIndicatorService`, `IndicatorTransitionHelper`, `IndicatorWorkflowController`, endpoints reject/approve en `SubphaseController`, excepciones (`EvidenceRequiredException`, `IndicatorNotLinkedException`, `JustificationRequiredException`, `InvalidIndicatorStateException`), `WorkflowModuleConfig`, extensión `SubphaseEvidenceQueryPort`, `SubsanateSubphaseEvidenceService` (transición SUBSANADO post-subsanación)

**Frontend:** `subphaseWorkflowApi.ts`, `SubphaseReviewActions.tsx`, integración en `SubphaseCollaborationSection`, `canReviewEvidence` en `ProcessDetailView` / `ProcessPhaseTree`

**Docs:** `DD-UC-008`, `DD-UC-009`, `PR-IMPL-038`, `FSD-UC-008`, `FSD-UC-009`, `FSD.md`, `api_contracts.md` (API-SUB-03/04, WF-01/02), este PM-005

### Cambios realizados

1. **Precondición evidencia:** reject/approve bloqueados con `409 EVIDENCE_REQUIRED` si la subfase no tiene evidencias.
2. **Vínculo subfase:** workflow resuelve desde `subphaseId`; evidencias FK `subphase_id`.
3. **Rechazo TD:** justificación ≥20 chars, observación OPEN, subfase → OBSERVADO.
4. **Aprobación TD:** sin observación OPEN, subfase → APROBADO.
5. **UI:** panel «Revisión técnica» visible solo para [TD] en detalle de proceso.

### Validación ejecutada

- [ ] `./mvnw test` — bloqueado permisos en `backend/target/` (entorno local)
- [x] `pnpm tsc -b` — OK
- [ ] Smoke: TD rechaza subfase con evidencia+indicador → CC subsana → TD aprueba

---

## PM-006

| Campo | Valor |
| --- | --- |
| **ID** | PM-006 |
| **Fecha** | 2026-08-27 |
| **Solicitante** | Boris Anthony Angulo Urquieta |
| **Agente/Entorno** | Cursor IDE — @sigesa-orchestrator |
| **Tarea** | FSD-UC-010 — Avanzar/cerrar Fase |
| **Objetivo** | [TD] cierra fase cuando todas las subfases están APROBADO; evento PhaseCompleted; UI con manejo FASE_CIERRE_BLOQUEADO |
| **PR-IMPL vinculado** | [PR-IMPL-039](../../prompts/impl/PR-IMPL-039.md) |
| **DD vinculado** | [DD-UC-010](../../design/DD-UC-010.md) |
| **FSD vinculado** | [FSD-UC-010](../../product/uc/FSD-UC-010.md) |
| **Estado** | completado |

### Prompt usado exacto

```text
Implement FSD-UC-010 end-to-end for sprint 3 following `.cursor/agents/sigesa-orchestrator.md` pipeline (all steps including 3c Docker smoke if possible, code review, @dtp-sync, @save-prompt-mapping).

**Invocation parameters:**
- fsd=FSD-UC-010
- sprint=3
- solicitante="Boris Anthony Angulo Urquieta"
- Run all steps without pausing unless blocked

**FSD-UC-010 — Avanzar/cerrar Fase:**
- Actor: [TD]
- Precondición: todas las subfases de la fase en `APROBADO`
- Flujo: TD cierra fase → verificar COUNT(subfases)=COUNT(APROBADO) → fase `COMPLETADA` → evento `PhaseCompleted`
- Errores: `409 FASE_CIERRE_BLOQUEADO` + lista subfases pendientes; `403 FORBIDDEN_ROLE` si CC
- Regla: FSD-BR-07
```

### Archivos generados o modificados (UC-010)

**Documentación**

- `docs/design/DD-UC-010.md`
- `docs/prompts/impl/PR-IMPL-039.md`
- `docs/product/uc/FSD-UC-010.md`, `FSD.md`, `api_contracts.md`, `DTP.md`
- `docs/sprints/sprint_03/PROMPT_MAPPING.md` (PM-006)

**Backend**

- `db/migration/V13__phase_workflow_status.sql`
- Dominio: `PhaseState`, `PhaseCompleteResult`, `PendingSubphase`, excepciones
- `ClosePhaseUseCase`, `ClosePhaseService`, `PhaseWorkflowPort`, `PhaseWorkflowJpaAdapter`
- `PhaseWorkflowController` — `POST .../phases/{phaseId}/complete`
- `ProcessResponseDto` + mappers: `status` en fase/subfase
- `WorkflowModuleConfig`, `ProcessExceptionHandler` (FASE_CIERRE_BLOQUEADO)
- `ClosePhaseServiceTest` (7 escenarios)

**Frontend**

- `features/phases/api/phaseWorkflowApi.ts`
- `features/phases/components/PhaseCloseAction.tsx`
- `ProcessPhaseTree.tsx`, `ProcessDetailView.tsx`
- Orval manual: `phaseDto.status`, `subphaseDto.status`

### Cambios realizados

1. **Modelo:** `phases.status` (`ABIERTA`/`COMPLETADA`), default ABIERTA.
2. **Regla BR-07:** cierre bloqueado si alguna subfase ≠ APROBADO; respuesta incluye `pendingSubphases`.
3. **API-WF-03:** endpoint REST síncrono (outbox `PhaseCompleted`).
4. **UI TD:** botón «Cerrar fase» por acordeón; lista subfases pendientes con enlace scroll.
5. **Docs:** FSD-UC-010 → Implementado; T-004 completo.

### Validación ejecutada

- [x] Compilación backend → `target-orval/classes` (519 fuentes OK)
- [x] `ClosePhaseServiceTest` — compila con javac (7 tests; `./mvnw test` bloqueado por `target/` root-owned)
- [x] `pnpm tsc -b` — OK
- [x] `pnpm run generate:api` con backend `:8080` — OK (Orval v8.23.0; `phase-workflow/completePhase`, `evidence-legacy`)
- [x] `pnpm exec tsc -b` — OK (eliminado stub obsoleto `evidence-controller/`)
- [x] Paso 3c Docker smoke — OK vía `scripts/smoke-uc010.sh` (TD login, list/detail, `POST .../complete` → **409** + `pendingSubphases[]`, proxy nginx `:3000`)
- [x] Fix esquema dev Docker: columnas `phases.status` / `subphases.status` (V12/V13) aplicadas manualmente en Postgres (Flyway off en dev)
- [ ] CC en `POST .../complete` devuelve **401** en lugar de **403** esperado — revisar `AccessDeniedHandler` (no bloquea UC-010)
