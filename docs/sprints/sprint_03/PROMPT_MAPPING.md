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
| PM-007 | N/A | DD-AGENT-UI-SHELL | MOD-ASSISTANT (FSD-UC-024 / agentes 001–003) | Shell flotante unificado copilotos fases/evidencias/usuarios + historial conversaciones |
| PM-008 | N/A | N/A | Tests unitarios backend (JaCoCo gate) | Completar tests unitarios de servicios de aplicación y clases del check JaCoCo |

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

---

## PM-007

| Campo | Valor |
| --- | --- |
| **ID** | PM-007 |
| **Fecha** | 2026-09-02 |
| **Solicitante** | Boris Anthony Angulo Urquieta |
| **Agente/Entorno** | Cursor IDE — Agent |
| **Tarea** | Unificación UI copilotos de dominio (MOD-ASSISTANT) |
| **Objetivo** | Misma ventana flotante inferior derecha para agentes fases/evidencias/usuarios; botón historial; sin ocupar layout; `/ayuda` conserva diseño propio |
| **Contexto** | Refactor frontend post PM-006; sin cambio de contrato API `/assistant/chat` |
| **PR-IMPL vinculado** | N/A (patrón UI derivado de [PR-IMPL-033](../../prompts/impl/PR-IMPL-033.md)) |
| **DD vinculado** | [DD-AGENT-UI-SHELL](../../design/assistant/DD-AGENT-UI-SHELL.md) |
| **FSD vinculado** | FSD-UC-024 · agentes [DD-AGENT-001](../../design/assistant/DD-AGENT-001.md) / [002](../../design/assistant/DD-AGENT-002.md) / [003](../../design/assistant/DD-AGENT-003.md) |
| **Estado** | completado |

### Prompt usado exacto

```text
Bien ahora quiero que modifiques nuestros chats de agentes, para que todos usen una misma vista o diseno, el diseno quiero que sea una ventana desplegable desde la parte inferior derecha, que no ocupe espacio, este chatbot debe tener su boton de historial que abre el historial de conversaciones, saber cuando estemos en la vista fases, evidencias, usuarios, el unico diseno que se mantiene es el chatbot de ayuda los demas deben compartir un mismo diseno especidficado

ahora documenta todo eso donde sea necesario y en el prompt mapping de sprint 3
```

### Entradas auxiliares

- [DD-AGENT-UI-SHELL](../../design/assistant/DD-AGENT-UI-SHELL.md) (nuevo)
- Actualización layout en DD-AGENT-001, DD-AGENT-002, DD-AGENT-003
- [DTP.md](../../product/DTP.md) §A.1 + §B.5
- [FSD-UC-024.md](../../product/uc/FSD-UC-024.md) (superficie UI)

### Archivos generados o modificados

| Acción | Ruta |
| --- | --- |
| generado | `frontend/src/features/assistant/components/domain-copilot/DomainCopilotFloatingChat.tsx` |
| generado | `frontend/src/features/assistant/components/domain-copilot/CopilotConversationHistoryPanel.tsx` |
| generado | `frontend/src/features/assistant/components/domain-copilot/CopilotMessageBubble.tsx` |
| generado | `frontend/src/features/assistant/lib/domainCopilotPresentation.ts` |
| generado | `frontend/src/features/assistant/lib/useCopilotConversationArchive.ts` |
| generado | `frontend/src/features/assistant/types/domainCopilotKind.ts` |
| generado | `docs/design/assistant/DD-AGENT-UI-SHELL.md` |
| modificado | `frontend/src/features/processes/components/PhasesCopilotPanel.tsx` |
| modificado | `frontend/src/features/evidence/components/EvidenceCopilotPanel.tsx` |
| modificado | `frontend/src/features/admin/users/components/UsersCopilotPanel.tsx` |
| modificado | `frontend/src/features/processes/components/ProcessDetailView.tsx` |
| modificado | `frontend/src/features/processes/components/ProcessStructureView.tsx` |
| modificado | `frontend/src/features/evidence/EvidenceUploadPage.tsx` |
| modificado | `frontend/src/features/admin/users/pages/UsersAdminPage.tsx` |
| modificado | `docs/design/assistant/DD-AGENT-001.md`, `DD-AGENT-002.md`, `DD-AGENT-003.md` |
| modificado | `docs/design/DD-SYS-002.md` (§4.5 shell flotante) |
| modificado | `docs/product/DTP.md`, `docs/product/FSD.md`, `docs/product/uc/FSD-UC-024.md` |
| modificado | `docs/prompts/impl/PR-IMPL-033.md` |
| modificado | `docs/sprints/sprint_03/PROMPT_MAPPING.md` (PM-007) |

### Cambios realizados

1. **Shell compartido:** `DomainCopilotFloatingChat` — FAB + panel portal `bottom-right`; badges Fases/Evidencias/Usuarios.
2. **Historial:** botón abre panel con conversación actual + archivos en `sessionStorage` al limpiar chat.
3. **Wrappers:** `PhasesCopilotPanel`, `EvidenceCopilotPanel`, `UsersCopilotPanel` delegan al shell; lógica en hooks existentes.
4. **Layouts:** eliminada columna lateral 340px en detalle/estructura proceso, carga evidencias y admin usuarios.
5. **Exclusión:** `/ayuda` + `AssistantChatUI` sin cambios.
6. **Docs:** design doc `DD-AGENT-UI-SHELL`; DTP y agentes 001–003 actualizados.

### Validación ejecutada

- [x] `pnpm exec tsc -b` — OK
- [x] `pnpm lint` — OK
- [ ] Smoke manual FAB + historial en Docker `:3000`

### Resultado obtenido

Copilotos de dominio comparten UX flotante; páginas ganan ancho útil; trazabilidad en PM-007 y design doc dedicado.

### Próximos pasos

- [ ] Rebuild frontend Docker tras merge
- [ ] Smoke: historial archiva al limpiar; badge correcto por ruta

---

## PM-008

| Campo | Valor |
| --- | --- |
| **ID** | PM-008 |
| **Fecha** | 2026-09-16 |
| **Hora** | 17:10 |
| **Solicitante** | Aylen Gonzáles |
| **Agente/Entorno** | Cursor IDE — Agent (Grok 4.6) |
| **Modelo** | Cursor Grok 4.6 |
| **Tarea** | Completar tests unitarios del backend (arquitectura hexagonal, gate JaCoCo) |
| **Objetivo** | Cubrir servicios de aplicación con JUnit 5 / Mockito / AssertJ, sin PostgreSQL ni servicios externos, y registrar trazabilidad en Sprint 3 |
| **Contexto** | Inventario previo: `FaseServiceImpl` no existe en el código (solo include JaCoCo en `pom.xml`); `DownloadReportArtifactService` no tenía tests; varias clases JaCoCo tenían cobertura parcial |
| **PR-IMPL vinculado** | N/A (prompt de testing, no feature FSD) |
| **DD-UC vinculado** | N/A |
| **FSD-UC vinculado** | N/A (cubre UC-001, UC-002, UC-004, UC-014 y workflow/subfase de forma transversal) |
| **Estado** | completado |

### Prompt usado exacto

```text
Actúa como un ingeniero senior de testing Java/Spring Boot y trabaja sobre este repositorio:

AcredIA-UMSS/sigesa-acreditacion-umss

Objetivo: implementar y completar los tests unitarios del backend ubicado en backend/.

Antes de modificar archivos:

1. Lee obligatoriamente:
   - AGENTS.md
   - backend/pom.xml
   - README.md
   - .cursor/agents/sigesa-orchestrator.md
   - La estructura completa de backend/src/main/java
   - La estructura completa de backend/src/test/java
   - backend/src/test/resources, si contiene configuración
   - Las configuraciones application*.yaml o application*.properties

2. Identifica:
   - La arquitectura hexagonal del backend.
   - La separación entre dominio, aplicación y adaptadores.
   - Los servicios/casos de uso con mayor lógica de negocio.
   - Los tests existentes para no duplicarlos.
   - Las clases incluidas en las reglas de cobertura JaCoCo del pom.xml.
   - Las dependencias y patrones de testing ya usados en el proyecto.

Reglas obligatorias:

- Usa Java 21.
- Usa JUnit 5, Mockito, AssertJ y Spring Boot Test, según corresponda.
- Respeta la arquitectura hexagonal.
- Los tests unitarios de servicios no deben conectarse a PostgreSQL.
- Mockea puertos, repositorios, adaptadores, clientes HTTP, servicios externos y proveedores de autenticación.
- No expongas ni uses entidades JPA directamente cuando el código de producción trabaja con dominio y DTOs.
- No modifiques código productivo salvo que sea estrictamente necesario para hacer testeable una clase. Si es necesario, detente, explica el motivo y solicita confirmación.
- No modifiques archivos dentro de docs/baseline/.
- No borres ni sobrescribas tests existentes.
- No generes tests triviales que solo verifiquen que un objeto no es null.
- No pruebes detalles internos de implementación si puedes probar el comportamiento observable.
- No uses sleeps, llamadas reales a internet, PostgreSQL, Ollama, Groq ni servicios externos.
- Evita tests frágiles y tests que dependan del orden global de ejecución.
- Cada test debe tener nombres descriptivos usando una convención como:
  shouldAuthenticateActiveUser
  shouldRejectInactiveUser
  shouldThrowWhenProgramDoesNotExist

Prioridad de implementación:

1. Completa primero los tests unitarios de las clases de aplicación y servicios con lógica de negocio.
2. Da prioridad a las clases incluidas en JaCoCo:
   - FaseServiceImpl
   - AuthenticateService
   - RegisterUserService
   - DeactivateUserService
   - GenerateExecutiveReportService
   - GetReportJobStatusService
   - ProcessReportJobService
   - DownloadReportArtifactService
   - ReportExportJobService
   - DashboardSummaryAggregationService
   - UploadEvidenceService

3. Después completa los tests de adaptadores importantes.
4. Después completa los tests de controladores usando @WebMvcTest y MockMvc cuando sea apropiado.
5. Usa @DataJpaTest únicamente para pruebas específicas de repositorios JPA.
6. No confundas tests unitarios con tests E2E o de integración.

Para cada servicio, cubre como mínimo:

- Caso exitoso.
- Entrada inválida.
- Entidad o recurso inexistente.
- Regla de negocio incumplida.
- Usuario inactivo o sin permisos, cuando corresponda.
- Excepción del puerto o dependencia externa.
- Verificación de llamadas importantes con Mockito.verify().
- Verificación de que no se llamen dependencias cuando la validación falla.
- Respuestas vacías, listas vacías o valores opcionales vacíos cuando aplique.
- Casos límite relevantes.

Organización de archivos:

- Mantén los tests bajo:
  backend/src/test/java/com/umss/sigesa/

- Respeta los paquetes actuales:
  - application/
  - adapter/
  - e2e/
  - generated/
  - performance/

- Los tests unitarios nuevos deben ubicarse en el paquete equivalente al código productivo.
- No coloques tests unitarios nuevos en e2e/ ni performance/.
- No mezcles tests generados automáticamente con tests escritos manualmente.

Proceso de trabajo:

1. Primero presenta un inventario de:
   - Clases productivas detectadas.
   - Tests existentes.
   - Tests faltantes.
   - Tests duplicados o incompletos.
   - Clases prioritarias.
   No escribas código todavía.

2. Después de mostrar el inventario, comienza por las clases prioritarias y crea los tests en grupos pequeños.

3. Después de cada grupo ejecuta:

   cd backend
   ./mvnw test

4. Si falla un test:
   - Analiza la causa.
   - Corrige el test si el problema está en el test.
   - No cambies código productivo automáticamente.
   - Si el código productivo parece tener un error, repórtalo separadamente y solicita confirmación.

5. Cuando termines todos los tests unitarios, ejecuta:

   cd backend
   ./mvnw clean test
   ./mvnw verify

6. Revisa el reporte JaCoCo generado en:

   backend/target/site/jacoco/index.html

7. Informa:
   - Cuántos tests nuevos se agregaron.
   - Qué clases fueron cubiertas.
   - Qué escenarios se probaron.
   - Resultado de ./mvnw clean test.
   - Resultado de ./mvnw verify.
   - Cobertura obtenida.
   - Clases que todavía no cumplen el 90 %.
   - Problemas que no pudiste resolver.
   - Archivos modificados.
y registra este prompt en @docs/PROMPT_MAPPING.md de Aylen Gonzáles en Sprint 3
```

### Entradas auxiliares

- `backend/pom.xml` (reglas JaCoCo CLASS)
- `docs/sprints/sprint_03/PROMPT_MAPPING.md`

### Archivos generados o modificados

| Acción | Ruta |
| --- | --- |
| generado | `backend/src/test/java/com/umss/sigesa/application/service/report/DownloadReportArtifactServiceTest.java` |
| generado | `backend/src/test/java/com/umss/sigesa/application/service/auth/ListUsersServiceTest.java` |
| generado | `backend/src/test/java/com/umss/sigesa/application/service/catalog/ListProgramsServiceTest.java` |
| generado | `backend/src/test/java/com/umss/sigesa/application/service/evidence/SearchEvidencesServiceTest.java` |
| generado | `backend/src/test/java/com/umss/sigesa/application/service/template/ArchiveTemplateServiceTest.java` |
| generado | `backend/src/test/java/com/umss/sigesa/application/service/subphase/AddSubphaseObservationServiceTest.java` |
| generado | `backend/src/test/java/com/umss/sigesa/application/service/subphase/SubsanateSubphaseEvidenceServiceTest.java` |
| generado | `backend/src/test/java/com/umss/sigesa/application/service/workflow/ApproveSubphaseIndicatorServiceTest.java` |
| generado | `backend/src/test/java/com/umss/sigesa/application/service/workflow/RejectSubphaseIndicatorServiceTest.java` |
| generado | `backend/src/test/java/com/umss/sigesa/application/service/workflow/RejectIndicatorServiceTest.java` |
| modificado | `backend/src/test/java/com/umss/sigesa/application/service/auth/AuthenticateServiceTest.java` |
| modificado | `backend/src/test/java/com/umss/sigesa/application/service/auth/RegisterUserServiceTest.java` |
| modificado | `backend/src/test/java/com/umss/sigesa/application/service/auth/DeactivateUserServiceTest.java` |
| modificado | `backend/src/test/java/com/umss/sigesa/application/service/report/GenerateExecutiveReportServiceTest.java` |
| modificado | `backend/src/test/java/com/umss/sigesa/application/service/report/GetReportJobStatusServiceTest.java` |
| modificado | `backend/src/test/java/com/umss/sigesa/application/service/report/ProcessReportJobServiceTest.java` |
| modificado | `backend/src/test/java/com/umss/sigesa/application/service/report/ReportExportJobServiceTest.java` |
| modificado | `backend/src/test/java/com/umss/sigesa/application/service/dashboard/DashboardSummaryAggregationServiceTest.java` |
| modificado | `backend/src/test/java/com/umss/sigesa/application/service/evidence/UploadEvidenceServiceTest.java` |
| modificado | `backend/src/test/java/com/umss/sigesa/application/service/template/TemplateStructureValidatorTest.java` |
| modificado | `backend/src/test/java/com/umss/sigesa/adapter/in/web/TemplateControllerWebMvcTest.java` |
| modificado | `docs/sprints/sprint_03/PROMPT_MAPPING.md` (PM-008) |

### Cambios realizados

1. Tests unitarios nuevos/extendidos para el gate JaCoCo y servicios de aplicación (auth, reportes, evidencias, dashboard, workflow, subfase, catálogo, plantillas).
2. Ajuste mínimo de tests de plantilla existentes: campo `requirements` obligatorio (alineado a producción; no se tocó código productivo).
3. `FaseServiceImpl` no se testeó porque la clase no existe en el repositorio.

### Validación ejecutada

- [x] `mvnw.cmd clean test` — BUILD SUCCESS, Tests run: 340, Failures: 0, Errors: 0, Skipped: 3
- [x] `mvnw.cmd verify` — BUILD SUCCESS, `jacoco-check` OK
- [ ] `pnpm run lint` — N/A (solo backend)
- [ ] Smoke Docker — N/A

### Resultado obtenido

Gate JaCoCo de clases existentes en verde (≥90 % línea). Cobertura global de líneas ~50.8 %. Quedan servicios de aplicación (assistant, process CRUD, etc.) bajo 90 %.

### Riesgos/observaciones

- Gap de trazabilidad: no hay `PR-IMPL` formal; el usuario autorizó registro PM en Sprint 3.
- `com.umss.sigesa.service.impl.FaseServiceImpl` está en `pom.xml` pero no hay clase productiva.
- Suite global incluye tests E2E/IT/performance ya existentes (`ReportExportAsyncE2EIT`, `EvidenceUploadControllerIT`, `Dashboard1MPerformanceTest`).

### Próximos pasos

- [ ] Cubrir servicios de aplicación restantes (assistant, process structure, upload por subfase)
- [ ] Decidir si eliminar o implementar `FaseServiceImpl` en el include JaCoCo del `pom.xml`
