---
producto: "SIGESA"
grupo: "ACREDIA"
documento: DTP                 
version: v2.1
fecha: "2026-09-09"
status: vivo
audiencia: dual
baseline_ref:
  dti: "docs/baseline/DTI_vFinal.md"
  tag: "release/2.0.0"
  commit: "HEAD"
release: "2.1.0"
stack:
  - "Java 21"
  - "Spring Boot 4.x"
  - "Hibernate / Spring Data JPA"
  - "PostgreSQL (Principal) / H2 (Pruebas)"
  - "React 19"
  - "Ollama (inferencia LLM local — MOD-ASSISTANT dev)"
  - "Open WebUI (API OpenAI-compatible — MOD-ASSISTANT dev)"
  - "AWS"
repo: "ruta/a/tu/repo/sigesa"
agents_md: "/AGENTS.md"
artefactos_vivos:
  prd: "docs/product/03_prd/PRD.md"          
  fsd: "docs/product/FSD.md"          
  prompt_mapping: "docs/sprints/sprint_03/PROMPT_MAPPING.md"
  design_docs_dir: "docs/design/"     
  adr_dir: "docs/adr/"
---

# Documento Técnico del Producto (DTP) – SIGESA

> **Qué es**: El contrato técnico vigente de SIGESA durante la fase de implementación.  
> **Release 2.0.0** (cerrado): jerarquía normativa **N1→N2→N3→Indicador→Evidencia** ([ADR-0004](../adr/ADR-0004-normative-hierarchy-v2.md)); legacy Fase/Subfase retirado (M5).  
> **Release 2.1.0** (en curso): workflow metodológico 7 etapas ([ADR-0005](../adr/ADR-0005-workflow-metodologico-evaluacion-transversal.md)); **M6** implementado (UC-025…028, Flyway V17).  
> **Regla de oro**: Cero divergencia silenciosa. El baseline de la Fase de Diseño permanece intacto en `docs/baseline/`.

---

## A. Control de cambios (Núcleo del DTP)

### A.1 Changelog de implementación

*(Este cuadro se llenará a medida que se ejecuten los prompts de implementación y se envíen los PRs)*

| Fecha | Cambio | Disparador (FSD-UC / DD) | ADR | PR / commit | Autor |
| ------- | -------- | -------------------------- | ----- | ------------- | ------- |
| 22/10/2026 | **Demo 2:** `openapi.json` generado desde springdoc; CI drift + Spectral (`.github/workflows/openapi.yml`). Registro `docs/demos/demo-2.md` y `8.2.0-aportes.md`. | FSD-UC-004 / FSD-UC-005 | — | PM-029 | Boris Anthony Angulo Urquieta |
| 22/10/2026 | **MOD-EVIDENCE UC-005:** `DELETE /evidences/{id}` retira evidencia si el indicador no está `APROBADO` (CC/CAE, 204 + blob/asset exclusivo); `409 EVIDENCE_IMMUTABLE` si aprobado. UI «Quitar / reemplazar». | FSD-UC-005 / DD-UC-005 | ADR-0001 | PM-028 | Boris Anthony Angulo Urquieta |
| 19/10/2026 | **MOD-ASSISTANT FEAT-10 (#57):** FAB global en panel autenticado (`FloatingAssistantButton`) abre modal a11y con copiloto general; `/ayuda` y copilotos de dominio sin cambios. Offset `bottom-24` para no solapar el FAB de dominio. | FSD-UC-024 / DD-FEAT-10 | — | PM-027 / PR-IMPL-045 / #57 | Boris Anthony Angulo Urquieta |
| 17/10/2026 | **MOD-ASSISTANT FEAT-03 (#52):** timeout LLM 10 s (`SIGESA_ASSISTANT_TIMEOUT_MS`); `sendAsync` + hilos virtuales; 504 `ASSISTANT_GATEWAY_TIMEOUT`. Contrato `PR-IMPL-042`. | FSD-UC-024 / DD-SYS-002 | — | PM-023 / PM-024 / PR-IMPL-042 / #52 | Boris Anthony Angulo Urquieta |
| 17/10/2026 | **Compose object storage:** imagen `docker.io/pgsty/silo:RELEASE.2026-10-15T00-00-00Z` (API S3/MinIO). `minio/minio` y `quay.io/minio/minio` ya no son pullables anónimamente. Servicio Compose sigue `minio` + `MINIO_*` + volumen `minio_data`. | FSD-UC-004 / DD-UC-004-M7 | — | PM-022 / PR-IMPL-041 / #51 | Boris Anthony Angulo Urquieta |
| 17/10/2026 | **MOD-EVIDENCE FEAT-02 (#51):** blobs S3/MinIO (`EvidenceBlobStoragePort`); lock `evidence_upload_lock` (V23); Compose servicio `minio` + volumen. Endpoint/credenciales por env (S3 remoto futuro). | FSD-UC-004 / DD-UC-004-M7 | — | PM-020 / PR-IMPL-041 / #51 | Boris Anthony Angulo Urquieta |
| 17/10/2026 | **MOD-REPORT FEAT-01 (#50):** I/O de PDF/XLSX/CSV fuera de TX write (`TransactionRunnerPort`); Hikari explícito (pool 10, acquire 30s, leak 20s). Remedia CON-03 (veredicto de auditoría histórico intacto). | FSD-UC-011 / FSD-UC-014 / DD-UC-011 / DD-UC-014 | — | PM-019 / PR-IMPL-040 / #50 | Boris Anthony Angulo Urquieta |
| 13/10/2026 | **Cierre SIGESA 2.1 M10/M11 + CAE:** Flyway **V22** (`process_freeze`, `peer_room_checklist_items`, `indicator_delegations`, `app_user.role` VARCHAR(20)+`CAE_MEMBER`, columnas plan M11, backfill E5–E7); API-WF-18…23; gates PRESENTATION/EXTERNAL_PREP/POST_ACCREDITATION; UI freeze/checklist/delegaciones/justificación; Orval. | FSD-UC-030 / 020 / 028 / 031 / DD-UC-030 | ADR-0005 | PM-018 / PR-IMPL-M10-030 | Boris Anthony Angulo Urquieta |
| 13/10/2026 | **MOD-EVALUATION M9 Plan de Mejoras + E4:** Flyway **V21** (`improvement_plan_items` + UNIQUE parcial source + backfill E4); API-WF-13…17; `evaluateDrafting`; generate PDF borrador (`DraftingDocumentRendererPort`); UI plan + botón generar; Orval. | FSD-UC-031 / FSD-UC-028 / DD-UC-031 | ADR-0005 | PM-017 / PR-IMPL-M9-031 | Boris Anthony Angulo Urquieta |
| 13/10/2026 | **MOD-EVALUATION M8 FODA + métricas + E2→E3:** Flyway **V20** (`indicator_analysis` + CHECKs + backfill E3 `FODA_FORMS_COMPLETE`/`SYNTHESIS_MATRIX`); API-WF-09…12; gate SYSTEMATIZATION; bootstrap ensure en procesos viejos; panel métricas + form FODA + lote encuesta; Orval. | FSD-UC-029 / FSD-UC-028 / DD-UC-029 | ADR-0005 | PM-016 / PR-IMPL-M8-029 | Boris Anthony Angulo Urquieta |
| 13/10/2026 | **MOD-EVIDENCE M7 DocumentAsset M:N:** Flyway **V19** (`document_assets`, `indicator_evidence_links` + backfill); deduplicación SHA-256 por proceso; API-EVD-01 ext + EVD-06 link + EVD-07 GET asset; UI modal «usar documento existente» + folio; Orval regenerado. | FSD-UC-004 / FSD-UC-005 / DD-UC-004-M7 | ADR-0005 | PM-015 / PR-IMPL-M7-004 | Boris Anthony Angulo Urquieta |
| 23/09/2026 | **QA contract tests JSON:** `backend/src/test/java/com/sigesa/app/contracts/{auth,evidence,assistant}` (Lic. Marlene; `unit/` Aylen, `integration/` Alex). Forma Jackson de DTOs web; agente `tools/contract-test-agent/`. | QA AcredIA / FSD-UC-001+004+assistant | — | local | Marlen |
| 09/09/2026 | **MOD-WORKFLOW metodológico (M6):** Flyway V17 (`methodological_stages`, `stage_deliverables`, `stage_gate_evaluations`, `primary_survey_batches`; `operational_mode`, `current_stage_id` en procesos); bootstrap 7 etapas + E1/E2 en `CreateProcessUseCaseImpl`; API-WF-04…08 (`StageWorkflowController`); `EvaluationMetricsPort`; UI `MethodologicalStageTimeline`; docs sync FSD/DTP/ADR. | FSD-UC-025…028 / DD-UC-025 | ADR-0005 | PM-010 / PR-IMPL-M6-025 | Boris Anthony Angulo Urquieta |
| 09/09/2026 | **Reconciliación documental v2.0:** índice FSD UC-004…010, UC-019 → Implementado v2; T-003/T-004 cerrados. | FSD sync | ADR-0004 | docs sync | Cursor Agent |
| 08/09/2026 | **MOD-PROCESS (FSD-UC-003 v2):** `CreateProcessUseCaseImpl` clona árbol normativo N1→N2→N3→Indicador vía `ProcessNormativeTreeCloner`; validación A4 `TEMPLATE_STRUCTURE_INCOMPLETE`; coexistencia clonado legacy Fase/Subfase. | FSD-UC-003 / DD-UC-003 | ADR-0004 | PM-009 / PR-IMPL-003 | Boris Anthony Angulo Urquieta |
| 08/09/2026 | **MOD-TEMPLATE (FSD-UC-021 v2 Full-Stack):** API-TPL-08 `TemplateNormativeStructureController`; publicación BR-24 (`PublishTemplateService` + árbol v2); UI tabs v2/legacy `/admin/plantillas`; Orval `jerarquía-normativa-plantilla-v2`; `DuplicateTemplateService` deep-copy v2 (`TemplateNormativeTreeCloner`). | FSD-UC-021 / DD-UC-021 | ADR-0004 | PM-008 / PR-IMPL-021 | Boris Anthony Angulo Urquieta |
| 08/09/2026 | **Release 2.0.0 (documental):** jerarquía normativa N1→N2→N3→Indicador→Evidencia; FSD/glosario/modelo/reglas/api v2; workflow en Indicador; cierre Nivel 1. Código legacy Fase/Subfase sin migrar. | FSD v2.0 | **ADR-0004** | docs sync | Boris Anthony Angulo Urquieta |
| 02/09/2026 | **MOD-ASSISTANT UI shell:** copilotos `phases`/`evidence`/`users` unificados en ventana flotante inferior derecha (`DomainCopilotFloatingChat`); historial de conversaciones en `sessionStorage`; layouts sin columna 340px; `/ayuda` sin cambios. | DD-AGENT-UI-SHELL / DD-AGENT-001…003 | N/A | PM-007 | Boris Anthony Angulo Urquieta |
| 27/08/2026 | **MOD-WORKFLOW UC-010:** cierre de fase TD (`PhaseState` ABIERTA/COMPLETADA, Flyway V13); API-WF-03 `POST /processes/{id}/phases/{id}/complete`; `409 FASE_CIERRE_BLOQUEADO` + `pendingSubphases[]`; evento outbox `PhaseCompleted`; UI «Cerrar fase» en detalle proceso. | FSD-UC-010 / DD-UC-010 | N/A | PM-006 / PR-IMPL-039 | Boris Anthony Angulo Urquieta |
| 27/08/2026 | **MOD-EVIDENCE:** deprecación `POST /indicators/{id}/evidences` → **410 Gone** (`ENDPOINT_DEPRECATED`); sucesor `POST /subphases/{id}/evidences`; Orval `DeprecatedEndpointResponseDto`; UI UC-004 migrada a selector proceso/subfase. | FSD-UC-004 / api_contracts API-EVD-LEGACY | N/A | docs + Orval sync | Cursor Agent |
| 27/08/2026 | **Pivot modelo v1.1:** Proceso→Fase→Subfase→Evidencia; retiro Dimensión/Criterio/Indicador del alcance piloto; docs FSD/glosario/modelo/api alineados | FSD v1.1 | N/A | docs sync | Boris Anthony Angulo Urquieta |
| 27/08/2026 | **MOD-WORKFLOW:** rechazo/aprobación de subfase (API-SUB-03/04); observación OPEN en rechazo TD | FSD-UC-008 / FSD-UC-009 | N/A | PM-005 / PR-IMPL-038 | Boris Anthony Angulo Urquieta |
| 27/08/2026 | **MOD-EVIDENCE UC-005:** historial versiones + DELETE rechazado (`EVIDENCE_IMMUTABLE`, `AUDIT_DELETE_DENIED`); API-EVD-03/04. | FSD-UC-005 / DD-UC-005 | N/A | PM-002 / PR-IMPL-035 | Boris Anthony Angulo Urquieta |
| 27/08/2026 | **MOD-EVIDENCE:** FTS GIN en `evidence_version.search_vector` (Flyway V11); ranking `ts_rank` en API-EVD-02. | FSD-UC-007 / DD-UC-007 | N/A | PM-004 / PR-IMPL-037 | Cursor Agent |
| 27/08/2026 | **MOD-EVIDENCE:** búsqueda multifiltro API-EVD-02; panel buscador en detalle de proceso (fases/subfases). | FSD-UC-007 / DD-UC-007 | N/A | PM-004 / PR-IMPL-037 | Cursor Agent |
| 27/08/2026 | **MOD-EVIDENCE:** subsanación por subfase (API-SUB-02); observación OPEN/RESOLVED; historial liviano (`blob_purged`, Flyway V10); bloqueo upload con observación pendiente. | FSD-UC-006 / DD-UC-006 | N/A | PM-003 / PR-IMPL-036 | Cursor Agent |
| 27/08/2026 | **MOD-PROCESS / MOD-EVIDENCE:** subfases con `requirements`; evidencias múltiples por subfase (`evidence.subphase_id`, Flyway V9); observaciones TD/JD (`subphase_observation`); API `/api/v1/subphases/{id}/evidences|observations`; UI detalle + editor estructura + plantillas. | FSD-UC-022 / FSD-UC-004 / DD-UC-022 | N/A | PM-001 / PR-IMPL-034 | Cursor Agent |
| 21/08/2026 | **MOD-ASSISTANT Nivel 4 (multi-tool):** loop encadenado en `SendChatMessageService`; API `steps[]`; traza UI en `/ayuda` + copilotos phases/users/evidence; escenarios demo #5 por agente. | DD-SYS-002 §11.10 / TOOL-CATALOG §5 | N/A | PM-023 / PR-IMPL-033 | Cursor Agent |
| 21/08/2026 | **MOD-ASSISTANT RAG normativo:** tabla `normative_document` (V8 FTS); tool `search_normative_docs`; `AssistantNormativeRagService`; config `rag-enabled`, `rag-max-chunks`. | DD-SYS-002 / PR-IMPL-032 | N/A | PM-022 / PR-IMPL-032 | Cursor Agent |
| 21/08/2026 | **MOD-ASSISTANT agente evidence (DD-AGENT-003):** UI copiloto alineada con fases/usuarios; modal dev `VITE_EVIDENCE_COPILOT_DEBUG_ACTIONS`; seguridad chat documentada. | FSD-UC-024 / DD-AGENT-003 | N/A | PM-020 / PR-IMPL-030 | Cursor Agent |
| 21/08/2026 | **MOD-EVIDENCE / MOD-PROCESS:** carga de evidencia en subfases vía enlace subrayado + modal (`SubphaseEvidenceUploadModal`); [CC] upload inline; [JD/TD] redirect UC-004. | FSD-UC-004 / FSD-UC-019 / DD-UC-004 | N/A | PM-019 / PR-IMPL-029 | Cursor Agent |
| 07/08/2026 | **MOD-PROCESS responsable (FSD-UC-023) — Full-Stack:** tabla `process_responsible_assignment` (Flyway V7); API-PROC-09…11; `ProcessResponsiblePort`; extensión UC-019 con `responsible`; UI sección/modal en detalle y listado. | FSD-UC-023 / DD-UC-023 | N/A | PM-010 / PR-IMPL-023 | Boris Anthony Angulo Urquieta |
| 07/08/2026 | **MOD-PROCESS estructura (FSD-UC-022) — Full-Stack:** `ProcessStructureController` API-PROC-05…08; `ProcessStructurePort` + `SubphaseWorkflowPort` (stub); guard `ProcessStructureGuard`; UI `/procesos/{processId}/estructura`; extensión GET detalle con `description`/`referenceUrl`. | FSD-UC-022 / DD-UC-022 | N/A | PM-009 / PR-IMPL-022 | Boris Anthony Angulo Urquieta |
| 12/08/2026 | **MOD-ASSISTANT agente evidence (DD-AGENT-003):** copiloto control documental (`agent=evidence`); tools `list_pending_evidences`, `get_evidence_detail`, `check_evidence_completeness`; PBAC JD/TD/CC (403 EE); UI `EvidenceCopilotPanel`; MCP `mcp/sigesa-evidence`. | FSD-UC-024 / DD-AGENT-003 | N/A | PM-013 / PR-IMPL-026 | Cursor Agent |
| 11/08/2026 | **MOD-ASSISTANT agente users (DD-AGENT-002):** copiloto embebido en `/admin/users` (`agent=users`); tools `get_user_detail`, `create_user`, `manage_user_status`, `manage_user_assignment`; RBAC HTTP 403 no-JD; `UserActionPlan` preview/confirm; UI `UsersCopilotPanel`. | FSD-UC-002 / DD-AGENT-002 | N/A | PM-012 / PR-IMPL-025 | Cursor Agent |
| 07/08/2026 | **MOD-PROCESS estructura (FSD-UC-022) — contrato:** `PR-IMPL-022` aprobado; `DD-UC-022` + API-PROC-05…08 en `api_contracts.md`; puertos planificados `ProcessStructurePort`, `SubphaseWorkflowPort` (stub). | FSD-UC-022 / DD-UC-022 | N/A | PM-008 / PR-IMPL-022 | Boris Anthony Angulo Urquieta |
| 07/08/2026 | **MOD-TEMPLATE (FSD-UC-021):** CRUD plantillas normativas `GET/POST/PUT/DELETE /api/v1/templates` + publish/duplicate/archive; Flyway `V5__template_management.sql`; `TemplateManagementPort` CQRS; validación `PUBLISHED` en `POST /processes`; clonación `referenceUrl` a subfases de proceso. | FSD-UC-021 / DD-UC-021 | N/A | PM-006 / PR-IMPL-021 | Boris Anthony Angulo Urquieta |
| 03/08/2026 | **MOD-DASH:** Conexión completa a Postgres/H2, eliminación de fallbacks mock/hardcoded en el backend y frontend. Creación de DashboardDataLoader para H2/dev. | FSD-UC-011 / DD-UC-011 | N/A | PR-IMPL-011 | Antigravity Agent |
| 03/08/2026 | **MOD-REVIEW:** Rol evaluador externo [EE] — login JWT, alta [JD] con carrera, dashboard solo lectura, FSD-BR-19. | FSD-UC-019 / DD-UC-019 | N/A | PM-003 / PR-IMPL-014 | Cursor Agent |
| 27/07/2026 | **MOD-ASSISTANT:** Asistente virtual en `/ayuda`; backend proxy Open WebUI/Ollama; Docker Compose `ollama` + `open-webui`; API `GET/POST /api/v1/assistant/*`. | PRD-REQ-028 / DD-SYS-002 | N/A | PM-001 / PR-IMPL-012 | Cursor Agent |
| 03/08/2026 | **MOD-PROCESS consulta (FSD-UC-019):** `GET /processes` + `GET /processes/{id}`; `ProcessQueryPort` CQRS read; RBAC JD/TD/CC + `programScope`; frontend `/procesos`, `/procesos/{id}`; sidebar desplegable. | FSD-UC-019 / DD-UC-019 | N/A | PR-IMPL-019 / PM-003 | Cursor Agent |
| 03/08/2026 | **MOD-PROCESS catálogo carreras:** tabla `programs` persistida; `GET /programs?q=`; autocomplete frontend; validación `career_id` y plantillas CEUB/ARCU-SUR únicamente. | FSD-UC-003 / DD-UC-003 | N/A | docs sync | Cursor Agent |
| 09/08/2026 | **MOD-ASSISTANT tools Fase 1.2:** `set_user_status` (solo JD), `list_programs` / `list_process_phases` / `manage_process_phase` (JD+TD); confirmación en chat; `ActivateUserUseCase`. | PRD-REQ-028 / DD-SYS-002 §11 | N/A | — | Cursor Agent |
| 31/07/2026 | **MOD-ASSISTANT tool calling Fase 1.1:** loop backend read-only; tool `list_users` (solo JD) vía `ListUsersUseCase`; max 3 iteraciones. | PRD-REQ-028 / DD-SYS-002 §11 | N/A | PM-002 / PR-IMPL-013 | Cursor Agent |
| 26/07/2026 | **PostgreSQL:** Configuración del motor transaccional, Flyway y propiedades de persistencia. | FSD-SYS-001 / DD-SYS-001 | ADR-0002 | Pendiente | AI Agent |
| 23/07/2026 | **Full-Stack MOD-PROCESS:** Arquitectura Hexagonal estricta Backend y UI Frontend (React/Orval) para clonación de Plantillas (`PROCESS_ALREADY_ACTIVE`). | FSD-UC-003 / DD-UC-003 | N/A | PM-001 | AI Agent |
| 26/06/2026 | Implementación MOD-EVIDENCE: carga v1 multipart, SHA-256, `indicator_state_history`, outbox stub, seed CC. | FSD-UC-004 / DD-UC-004 | N/A | PM-012 / PR-IMPL-006 | Cursor Agent |
| 26/06/2026 | Implementación MOD-REPORT: jobs PDF asíncronos, OpenPDF, tabla `report_job`, endpoints polling/descarga; stub datos ejecutivos + puente `ExecutiveDashboardQueryPort` para UC-013. | FSD-UC-014 / DD-UC-014 | N/A | PM-010 / PR-IMPL-005 | Cursor Agent |
| 23/06/2026 | Trazabilidad 1:1 MOD-AUTH: split `DD-UC-001`/`DD-UC-002`; prompts `PR-IMPL-001`/`PR-IMPL-002`; `PR-IMPL-004` → `archive/`. | FSD-UC-001 / DD-UC-001 · FSD-UC-002 / DD-UC-002 | ADR-0003 | docs sync | Cursor Agent |
| 23/06/2026 | Sync inconsistencias MOD-AUTH: diagramas, modelo_datos, api_contracts, ADR-0003 vivo, FSD-BR-12. | FSD-UC-001, FSD-UC-002 / DD-UC-001, DD-UC-002 | ADR-0003 | docs sync | Cursor Agent |
| 22/06/2026 | `@dtp-sync` DD-UC-001: consolidación MOD-AUTH en DTP, FSD, api_contracts, modelo_datos. | FSD-UC-001, FSD-UC-002 / DD-UC-001 | ADR-0003 | `f38976b` / PM-007 | Cursor Agent |
| 22/06/2026 | Implementación MOD-AUTH (JWT, login, admin users, user_program_assignment, hardening code-review). | FSD-UC-001, FSD-UC-002 / DD-UC-001 | ADR-0003 | `5cd14df`…`f38976b` | Cursor Agent |
| 22/06/2026 | Inicialización de la arquitectura base Spring Boot y DTP vivo. | N/A | N/A | `init` | Boris Angulo |

## [2026-06-29] - Actualización de Arquitectura Frontend (FSD-UC-003)

### Dependencias Añadidas

- `lucide-react`: Adoptado como estándar para la iconografía de la interfaz.
- `react-router-dom`: Configurado para la gestión de rutas del lado del cliente.
- `orval`: Utilizado para la autogeneración de clientes y hooks de React Query a partir de OpenAPI (Swagger).

### Decisiones Técnicas y Refactorización

- **Migración a Tailwind CSS v4:** Se eliminaron los archivos de configuración legados (`tailwind.config.ts`, `postcss.config.js`). El sistema de diseño institucional y los tokens de color ahora se gestionan nativamente mediante la directiva `@theme` en `src/index.css`.
- **Tipografía:** Se estableció `Inter` como la tipografía principal sin serifa (`sans`), manteniendo `IBM Plex Mono` para contextos específicos.
- **Enrutamiento y Vistas:** Se registró la ruta `/procesos/nuevo` conectada al contenedor lógico `CreateProcessView`. Se implementaron mocks temporales a la espera de los endpoints `GET /careers` and `GET /templates`.

### A.2 Deltas respecto al DTI vFinal

> Diferencias **deliberadas** entre lo diseñado y lo construido.

| # | Sección del DTI afectada | Qué decía el DTI vFinal | Qué dice ahora el DTP | Motivo | ADR |
| --- | -------------------------- | ------------------------- | ----------------------- | -------- | ----- |
| 1 | Perímetro API | Endpoints legacy sin auth explícita en DTI piloto | Todo `/api/v1/**` excepto `POST /auth/login` exige JWT Bearer | MOD-AUTH v1.0 unifica seguridad antes de MOD-EVIDENCE | N/A (DD-UC-001) |
| 2 | Entrega password temporal | No especificado en API baseline | Alta genera password en servidor; entrega **offline** v1.0 (no en JSON response) | Evitar exposición en tránsito; capacitación [JD] | N/A |
| 3 | Migración DDL MOD-AUTH | Índice parcial en DTI | Flyway perfil `prod` + script `V1__mod_auth_uk_upa_active.sql`; H2 dev: `AuthSchemaInitializer` | Hibernate no genera índices parciales | N/A |
| 4 | Motor PDF | DTI piloto Node (PDFKit/ReportLab spike) | **OpenPDF 2.0.3** en backend Java (`OpenPdfRendererAdapter`) | Stack runtime = Java 21 / Spring Boot 4.x (ADR-009 plan B) | N/A |
| 5 | API reportes | Solo `POST /reports/executive/pdf` en catálogo baseline | Job asíncrono: `POST` 202 + `GET /{jobId}` + `GET /{jobId}/download` bajo `/api/v1` | Alineado a MAR-SEQ-005 y DD-UC-014 | DD-UC-014 |
| 6 | Fuente datos PDF | Proyección CQRS `proj_executive_semaphore` (DTI async) | v1.0: `ExecutiveDataStubAdapter`; v1.0+UC-013: `ExecutiveDashboardQueryPort` → `ExecutiveDataDashboardAdapter` | UC-013 pendiente | DD-UC-014 |
| 7 | Storage evidencias | S3 en DTI cloud | Filesystem local `sigesa.evidence.storage-path` v1.0 | Piloto local H2 | DD-UC-004 |
| 8 | Dashboard Architecture | `GET /dashboard/coordinator` aislado por rol | Arquitectura Híbrida Compuesta PBAC (`GET /api/v1/dashboards/me/summary`) + endpoints modulares (`/details`, `/export`) | Optimizar peticiones HTTP para usuarios multi-rol y renderizado dinámico en UI | N/A (DD-UC-011) |
| 9 | Stack Persistencia | H2 (Memoria/Archivo) para todo | **PostgreSQL** (Principal) + H2 (Test) | Las reglas de negocio (índices únicos activos) requieren motor transaccional robusto | ADR-0002 |
| 10 | Acreditación (MOD-PROCESS) | Arquitectura por capas implícita | **Arquitectura Hexagonal Estricta** (Puertos In/Out, Dominio Puro, Adaptadores) | Escalabilidad del modelo normativo y desacoplamiento de frameworks (JPA) | N/A (DD-UC-003) |
| 11 | Asistente / IA | Chatbot FAQ normativo (Could, v2.0 PRD) | **MOD-ASSISTANT MVP:** proxy backend → Open WebUI → Ollama; sin RAG ni persistencia de chats | Piloto self-hosted local; API key Open WebUI solo en servidor | DD-SYS-002 |
| 12 | RBAC consulta proceso [CC] | 403 Forbidden cross-scope (patrón REST típico) | **404 `PROCESS_NOT_FOUND`** cuando `career_id ∉ programScope` | No revelar existencia de procesos ajenos al coordinador | N/A (DD-UC-019) |
| 13 | Migraciones dev Docker | Flyway habilitado en perfil `dev` | **`ddl-auto: update`** + **`flyway.enabled: false`** en `application-dev.yaml`; scripts `V5…V6` aplican solo en **prod** | Migraciones incrementales asumen tablas base Hibernate; BD Docker fresca fallaba en `V4` (`app_user` inexistente) | N/A (operacional dev) |
| 14 | Modelo acreditación (MOD-PROCESS/EVIDENCE) | Piloto v1.x: Proceso→Fase→Subfase→Evidencia; `POST /indicators/...` deprecado (410) | **v2.0 objetivo:** Modelo evaluador→N1→N2→N3→Indicador→Evidencia; API-STR/API-WF/API-EVD v2 en [`api_contracts.md`](api_contracts.md); legacy Fase/Subfase hasta M3 | Alineación normativa CEUB/ARCU-SUR; ponderación por indicador | **ADR-0004** |

### A.3 Estado de implementación por FSD-UC

| FSD-UC | Design Doc | Estado | Release | Tests/Evals | PR-IMPL | Notas |
| -------- | ------------ | -------- | --------- | ------------- | --------- | ------- |
| `FSD-UC-001` | `DD-UC-001` | hecho | `release/3.0.0` | Suite §6 DD-UC-001; JaCoCo pendiente `mvn verify` | `PR-IMPL-001` | JWT + LocalAuthAdapter; A1 estricto → 401 |
| `FSD-UC-002` | `DD-UC-002` | hecho | `release/3.0.0` | Suite §6 DD-UC-002; JaCoCo pendiente `mvn verify` | `PR-IMPL-002` | Alta INACTIVE; revoke soft; 409 email dup |
| `FSD-UC-003` | `DD-UC-003` | **hecho v2** *(legacy coexist.)* | `2.0.0` | `ProcessNormativeTreeClonerTest`, `CreateProcessUseCaseImplTest` | `PR-IMPL-003` | Clona N1→Indicador; legacy Fase/Subfase paralelo |
| `FSD-UC-019` | `DD-UC-019` | **hecho v2** | `2.0.0` | GET devuelve `level1Nodes[]` + timeline M6 | `PR-IMPL-019` | |
| `FSD-UC-021` | `DD-UC-021` | **hecho (Full-Stack v2)** | `2.0.0` | API-TPL-08 + WebMvc; UI `/admin/plantillas`; publish BR-24 | `PR-IMPL-021` | Duplicate v2; legacy tabs coexisten |
| `FSD-UC-022` | `DD-UC-022` | **hecho v2** | `2.0.0` | API-STR-01…05 + UI estructura v2 | `PR-IMPL-022` | |
| `FSD-UC-004` | `DD-UC-004-M7` | **hecho (M7)** | `2.1.0` | EVD-01 ext + EVD-06 link + EVD-07 asset | `PR-IMPL-M7-004` | PM-015 |
| `FSD-UC-005` | `DD-UC-005` / `DD-UC-004-M7` | **hecho (M7)** | `2.1.0` | Append-only si `APROBADO`; CC puede retirar si no aprobado | `PR-IMPL-035` / `PR-IMPL-M7-004` | PM-015 / PM-028 |
| `FSD-UC-006` | `DD-UC-006` | **hecho v2** | `2.0.0` | Subsanación por indicador API-EVD-05 | `PR-IMPL-036` | |
| `FSD-UC-007` | `DD-UC-007` | **hecho v2** | `2.0.0` | API-EVD-02 filtros `level1Id`, `indicatorId` | `PR-IMPL-037` | |
| `FSD-UC-008` | `DD-UC-008` | **hecho v2** | `2.0.0` | API-WF-01 reject indicador | `PR-IMPL-038` | |
| `FSD-UC-009` | `DD-UC-009` | **hecho v2** | `2.0.0` | API-WF-02 approve indicador | `PR-IMPL-038` | |
| `FSD-UC-010` | `DD-UC-010` | **hecho v2** | `2.0.0` | API-WF-03 cierre N1 | `PR-IMPL-039` | |
| `FSD-UC-025` | `DD-UC-025` | **hecho (M6)** | `2.1.0` | API-WF-04…08; bootstrap etapas | `PR-IMPL-M6-025` | UC-026…028 mismo contrato |
| `FSD-UC-026` | `DD-UC-025` | **hecho (M6)** | `2.1.0` | POST submit etapa | `PR-IMPL-M6-025` | |
| `FSD-UC-027` | `DD-UC-025` | **hecho (M6)** | `2.1.0` | POST approve/observe etapa | `PR-IMPL-M6-025` | |
| `FSD-UC-028` | `DD-UC-025` / `DD-UC-029` / `DD-UC-031` / `DD-UC-030` | **hecho (M11)** | `2.1.0` | Gates E1–E7 (freeze, checklist, justificación) | `PR-IMPL-M6-025` / `PR-IMPL-M8-029` / `PR-IMPL-M9-031` / `PR-IMPL-M10-030` | PM-018 |
| `FSD-UC-029` | `DD-UC-029` | **hecho (M8)** | `2.1.0` | API-WF-09…12; V20; UI FODA/métricas/encuesta | `PR-IMPL-M8-029` | PM-016 |
| `FSD-UC-030` | `DD-UC-030` | **hecho (M10)** | `2.1.0` | Freeze + Sala de Pares + EE; API-WF-18…21 | `PR-IMPL-M10-030` | PM-018 |
| `FSD-UC-031` | `DD-UC-031` / `DD-UC-030` | **hecho (M9+M11)** | `2.1.0` | Plan + E4 + justificación/E7 | `PR-IMPL-M9-031` / `PR-IMPL-M10-030` | PM-018 |
| `FSD-UC-023` | `DD-UC-023` | **hecho (Full-Stack)** | `2.0.0` | Sin cambio contrato | `PR-IMPL-023` | Responsable [CC] — independiente del árbol |
| `FSD-UC-011` | `DD-UC-011` | hecho / **v2.0 filtros pendientes** | `2.0.0` | PBAC summary/details | `PR-IMPL-011` | Migrar filtros a level1Id/indicatorStatus |
| `FSD-UC-014` | `DD-UC-014` | en curso | `release/3.0.0` | Unit `*Report*Service`; JaCoCo pendiente `mvn verify` | `PR-IMPL-005` | Stub datos; conectar UC-013 vía `ExecutiveDashboardQueryPort` |
| `FSD-UC-013` | pendiente | pendiente | `release/3.0.0` | — | — | Debe implementar `ExecutiveDashboardQueryPort` para alimentar PDF |
| `FSD-SYS-001` | `DD-SYS-001` | **hecho** | `release/3.0.0` | Tests de conexión locales (Flyway) | `PR-IMPL-004` | Integración con PostgreSQL (Driver, HikariCP, YML) |
| `PRD-REQ-028` | `DD-SYS-002` | **hecho (MVP)** | `release/3.0.0` | Manual E2E `/ayuda`; sin tests automatizados aún | `PR-IMPL-012` | Chat proxy Open WebUI; modelo `llama3.2:3b`; ver §B.5 |
| `FSD-UC-020` | `DD-UC-030` | **hecho (M10)** | `2.1.0` | EE lectura proceso + checklist Sala de Pares | `PR-IMPL-M10-030` | PM-018 |

### A.4 Trazabilidad código ↔ DTP

`BRD/MRD (baseline)` → `PRD/FSD vivo (FSD-UC-NNN)` → `Design Doc (DD-UC-NNN)` → `Prompt (PR-IMPL-NNN)` → `PR/commit` → `Tests/Evals` → `ADR (si aplica)` → **DTP**.

---

## B. Contenido técnico vigente

> SIGESA utiliza **Arquitectura Hexagonal (Puertos y Adaptadores)** de manera estricta para los nuevos módulos core (ej. MOD-PROCESS). Cualquier desviación de los principios de Clean Architecture o del uso estricto de DTOs en adaptadores web será documentada aquí.

| Sección (espejo del DTI) | ¿Cambió vs DTI vFinal? | Dónde está la versión vigente |
| -------------------------- | ------------------------ | ------------------------------- |
| §1 Visión del producto | no | DTI vFinal §1 |
| §2 Contexto del sistema (C4 N1) | no | DTI vFinal §2 |
| §3 Arquitectura de alto nivel (C4 N2/N3) | **sí** | PostgreSQL reemplaza H2 en la persistencia principal (C4 N2 Container Diagram) |
| §4 Modelo de dominio | **sí** | v2.0: [`modelo_datos.md`](modelo_datos.md) — N1→N2→N3→Indicador; legacy Fase/Subfase en JPA hasta Flyway V14+ |
| §5 Arquitectura hexagonal del core | **sí** | Confirmada su exigencia estricta en FSD-UC-003. Dominio encapsulado. |
| **MOD-AUTH (identidad)** | **sí** | Ver §B.1 abajo; design docs `DD-UC-001`, `DD-UC-002` |
| **MOD-PROCESS (acreditación)** | **sí** | Ver §B.2 abajo; design docs `DD-UC-003`, `DD-UC-019`, **`DD-UC-022` (§B.2.1)**, **`DD-UC-023` (§B.2.2)** |
| **MOD-REPORT (PDF ejecutivo)** | **sí** | Ver §B.3 abajo; design doc `DD-UC-014` |
| **MOD-EVIDENCE (carga v1)** | **sí** | Ver §B.4 abajo; design doc `DD-UC-004` |
| **MOD-WORKFLOW (indicador / N1)** | **sí** | Ver §B.4.1 abajo; DD-UC-008, 009, 010 |
| **MOD-WORKFLOW (metodológico 7 etapas)** | **sí** | Ver §B.8 abajo; DD-UC-025; ADR-0005 |
| **MOD-ASSISTANT (chatbot MVP)** | **sí** | Ver §B.5 abajo; design doc `DD-SYS-002` |
| **MOD-REVIEW (evaluador externo [EE])** | **sí** | Ver §B.6 abajo; design doc `DD-UC-019` |
| **MOD-TEMPLATE (plantillas normativas)** | **sí** | Ver §B.7 abajo; design doc `DD-UC-021` |
| §8 Despliegue cloud (AWS) | no | DTI vFinal §8 |
| §10 Prompt mapping | **sí (crece)** | `docs/sprints/sprint_02/PROMPT_MAPPING.md` (Sprint 02 — MOD-ASSISTANT PM-001) |
| §21 ADRs | **sí (crece)** | [`docs/adr/`](../adr/) (ADR-0003 MOD-AUTH, **ADR-0002 PostgreSQL**, **ADR-0004 jerarquía v2.0**; baseline en `docs/baseline/05_dti/adrs/`) |

### B.1 MOD-AUTH — contrato técnico vigente (`DD-UC-001` + `DD-UC-002`)

*(Sin cambios respecto a la actualización del 22/06/2026)*

| Área | Detalle vigente |
| --- | --- |
| **Endpoints** | `POST /api/v1/auth/login` (público); `POST /api/v1/admin/users` ([JD]); `PATCH /api/v1/admin/users/{id}/deactivate` ([JD]) |
| **Perímetro JWT** | Todo `/api/v1/**` excepto login exige `Authorization: Bearer` (delta §A.2 #1) |
| **Tablas JPA** | `app_user`, `user_program_assignment` |
| **Índice parcial** | `uk_upa_active` — Flyway (`application-prod.yaml`) |
| **Password hashing** | Argon2id (`Argon2PasswordEncoder`) |
| **JWT** | HS256; claims `sub`, `email`, `role`, `programScope[]`; secret `SIGESA_JWT_SECRET` |

### B.2 MOD-PROCESS — contrato técnico vigente (`DD-UC-003` + `DD-UC-019`)

**Release 2.0.0 objetivo:** clonación y consulta del árbol **N1→N2→N3→Indicador** — ver [`api_contracts.md`](api_contracts.md) §4.  
**Implementación actual (legacy):** Fase/Subfase — mantener hasta ADR-0004 M2–M3.

**Implementación creación:** PM-001 · **Prompts vigentes:** `PR-IMPL-003V3`  
**Implementación consulta:** `PR-IMPL-019` · **Design doc consulta:** `DD-UC-019`

| Área | Detalle vigente (legacy en prod) | Objetivo v2.0 |
| --- | --- | --- |
| **Endpoints creación/consulta** | `POST/GET /processes`, `GET /processes/{id}` | Igual; payload con `level1Nodes[]` |
| **DTOs** | `ProcessResponseDto` con `phases[].subphases[]` | `evaluatorModel`, `level1Nodes` anidado |
| **Puertos consulta** | `ProcessQueryPort` | + proyección árbol normativo |
| **Tablas JPA (prod)** | `phases`, `subphases`, `template_phases`, `template_subphases` | `level1_nodes`, `level2_nodes`, `level3_nodes`, `indicators`, `template_*` equivalentes (V14+) |
| **Clonación** | Fase→Subfase desde plantilla | N1→N2→N3→Indicador |
| **Frontend** | `/procesos/{id}` árbol fases/subfases | Árbol multinivel + badges indicador |
| **Hooks Orval** | `useListProcesses`, `useGetProcess` | Regenerar post-OpenAPI v2 |

#### B.2.1 MOD-PROCESS — estructura en proceso ACTIVE (`DD-UC-022`)

**Implementación legacy:** Sprint 02 PM-009 · **Objetivo v2.0:** API-STR-01…05 · **Estado:** legacy hecho; v2.0 pendiente M3

| Área | Legacy (prod) | Objetivo v2.0 |
| --- | --- | --- |
| **Endpoints** | API-PROC-05…08 `/phases`, `/subphases` | API-STR-01…05 `/level1-nodes`, `/level2-nodes`, `/level3-nodes`, `/indicators` |
| **RBAC** | `[JD]`, `[TD]` en controller (verificar vs FSD-BR-23) | Igual |
| **Puertos** | `ProcessStructurePort`, `SubphaseWorkflowPort` | `NormativeStructurePort`, `IndicatorWorkflowPort` |
| **Errores** | `SUBPHASE_HAS_EVIDENCE` | `INDICATOR_HAS_EVIDENCE` |
| **Frontend** | `/procesos/{processId}/estructura` (2 niveles) | Editor árbol 4 niveles |

#### B.2.2 MOD-PROCESS — responsable [CC] (`DD-UC-023`)

**Implementación:** Sprint 02 PM-010 · **Prompt:** `PR-IMPL-023` · **FSD:** FSD-UC-023 · **Estado:** **hecho (Full-Stack)**

| Área | Detalle vigente |
| --- | --- |
| **Endpoints** | `PUT/DELETE /api/v1/processes/{processId}/responsible`; `GET …/responsible/candidates` |
| **RBAC** | Solo **[JD]** mutaciones; lectura responsable en GET list/detail (UC-019) |
| **Puertos aplicación** | IN: `AssignProcessResponsibleUseCase`, `RemoveProcessResponsibleUseCase`, `ListEligibleResponsiblesUseCase`; OUT: **`ProcessResponsiblePort`** |
| **Tabla JPA** | `process_responsible_assignment` — soft revoke (`revoked_at`); índices únicos parciales (BR-20) |
| **Migración Flyway** | `V7__process_responsible.sql` (prod); dev Docker: Hibernate `ddl-auto: update` |
| **Errores clave** | 409 `CC_ALREADY_ASSIGNED_TO_PROCESS`, `CAREER_SCOPE_MISMATCH`, `PROCESS_NOT_EDITABLE`; 400 `INVALID_RESPONSIBLE_USER` |
| **Extensión UC-019** | `ProcessSummaryResponseDto` / `ProcessResponseDto` → campo `responsible` (`ProcessResponsibleDto`) |
| **Frontend** | Sección «Responsable del proceso» en detalle; modal asignación; columna en listado `/procesos` |
| **Hooks Orval** | `useAssignResponsible`, `useRemoveResponsible`, `useListCandidates` en `procesos-de-acreditación.ts` |

---

### B.3 MOD-REPORT — contrato técnico vigente (DD-UC-014)

**Implementación:** PM-010 · **Prompts:** `PR-IMPL-005` · **Hardening 8.1.0:** `PR-IMPL-040` (issue #50) · **FSD:** FSD-UC-014

| Área | Detalle vigente |
|---|---|
| **Dependencia Maven** | `com.github.librepdf:openpdf:2.0.3` |
| **Endpoints** | `POST /api/v1/reports/executive/pdf` → **202** `{ jobId }`; `GET .../pdf/{jobId}` → estado; `GET .../pdf/{jobId}/download` → `application/pdf` |
| **RBAC** | Solo `[JD]` — `SecurityConfig` + FSD-BR-14 |
| **Tabla JPA** | `report_job` (`id`, `requester_id`, filtros, `status`, `artifact_key`, `error_code`, timestamps) |
| **Storage artefactos** | `ReportArtifactStoragePort`: local si `SIGESA_EVIDENCE_STORAGE=local`; S3/MinIO keys `reports/{jobId}.pdf` si `s3` |
| **Job async** | `@Async("reportJobExecutor")` — `ReportJobAsyncDispatcher` |
| **TX vs I/O (FEAT-01)** | `TransactionRunnerPort`: persistir estado en TX cortas; `render`/`store`/`generateReport` fuera de TX write |
| **HikariCP** | `spring.datasource.hikari` en YAML — `maximum-pool-size=10`, `connection-timeout=30000`, `leak-detection-threshold=20000` (`SIGESA_HIKARI_*`) |
| **Estados job** | `PENDING` → `IN_PROGRESS` → `COMPLETED` \| `FAILED` |
| **Errores job** | `REPORT_TEMPLATE`, `REPORT_GENERATION_FAILED` |
| **Datos PDF** | v1.0: `ExecutiveDataStubAdapter`; post UC-013: `ExecutiveDashboardQueryPort` + `ExecutiveDataDashboardAdapter` (@Primary) |
| **Integración UC-013** | MOD-DASH implementa `ExecutiveDashboardQueryPort.fetchExecutiveSnapshot()` leyendo la misma proyección que `GET /dashboard/executive` |

### B.4 MOD-EVIDENCE — contrato técnico vigente (DD-UC-004)

**Release 2.0.0 objetivo:** evidencia en **Indicador** — API-EVD-01 `POST /indicators/{id}/evidences`.  
**Legacy en prod:** upload por **Subfase**; path `/indicators/...` retorna **410 Gone** (decisión v1.1, revertir en M3).

| Área | Legacy (prod) | Objetivo v2.0 |
|---|---|---|
| **Upload** | `POST /subphases/{subphaseId}/evidences` | `POST /indicators/{indicatorId}/evidences` |
| **Subsanación** | API-SUB-02 por subfase | API-EVD-05 por indicador |
| **Tablas** | `evidence.subphase_id`, `subphase_observation` | `evidence.indicator_id` NOT NULL, `indicator_observation` |
| **Estado workflow** | Derivado en `subphases.status` | Derivado en `indicators.status` |
| **Búsqueda** | API-EVD-02 filtros `phaseId`, `subphaseId` | `level1Id`, `indicatorId`, `normativePath` |
| **RBAC** | `[CC]` alcance carrera | Sin cambio |
| **Hash / storage** | SHA-256, `./data/evidences` | M7: blob en `DocumentAsset`; unique `(process_id, content_hash)`. **8.1.0 FEAT-02:** `SIGESA_EVIDENCE_STORAGE=s3` (MinIO/S3) o `local` (tests); lock `evidence_upload_lock`; `GET /document-assets/{id}/content` (API-EVD-07b) |

#### B.4.2 MOD-EVIDENCE — DocumentAsset M:N (`DD-UC-004-M7`, M7 / v2.1)

**Implementación:** Sprint 03 PM-015 · **Prompt:** `PR-IMPL-M7-004` · **Hardening 8.1.0:** `PR-IMPL-041` (#51) · **FSD:** UC-004/005 · **ADR:** ADR-0005

| Área | Detalle vigente (M7) |
| --- | --- |
| **Endpoints** | API-EVD-01 201 + `documentAssetId`/`folioCode`/`reusedAsset`; EVD-06 `POST .../evidences/link`; EVD-07 `GET /document-assets/{id}` (+ list `?processId=`) |
| **Dominio** | `DocumentAsset`, `IndicatorEvidenceLink`; `Evidence` sigue 1:1 indicador (`uk_evidence_normative_indicator`) |
| **Puertos** | OUT `DocumentAssetPort`; IN `LinkDocumentAssetToIndicatorUseCase`, `GetDocumentAssetUseCase` |
| **Migración Flyway** | `V19` DocumentAsset; **`V23__evidence_upload_lock.sql`** (FEAT-02); Docker: Hibernate + `DevWorkflowSchemaAlignmentRunner` |
| **Object storage** | `S3EvidenceBlobStorageAdapter` si `sigesa.evidence.storage=s3`; Compose `minio` = `pgsty/silo:RELEASE.2026-10-15T00-00-00Z` (`:9000` API / `:9001` consola); keys `evidences/{id}/v{n}/…` |
| **Upload lock** | `JdbcEvidenceUploadLockAdapter` — PK `indicator_id`; sin `ConcurrentHashMap` |
| **Errores clave** | 409 `INDICATOR_ALREADY_HAS_EVIDENCE`; 404 `DOCUMENT_ASSET_NOT_FOUND`; 403 `PROGRAM_SCOPE_DENIED` (asset de otro proceso) |
| **Frontend** | Modal carga: checkbox vincular asset del proceso; folio en lista de evidencias; wrapper Orval |
| **Pendiente** | N documentos por el mismo indicador (post-M7) |

#### B.4.3 MOD-EVALUATION — FODA, métricas y encuestas (`DD-UC-029`, M8 / v2.1)

**Implementación:** Sprint 03 PM-016 · **Prompt:** `PR-IMPL-M8-029` · **FSD:** UC-029 / UC-028 · **ADR:** ADR-0005

| Área | Detalle vigente (M8) |
| --- | --- |
| **Endpoints** | API-WF-09 `GET …/evaluation-metrics`; WF-10 GET/PUT `…/indicators/{id}/analysis`; WF-11/12 POST/GET `…/survey-batches`; WF-07 preview E2/E3 sin cambio de ruta |
| **Dominio** | `IndicatorAnalysis`, `FodaQuadrant`, `ComplianceJudgment`, `PrimarySurveyBatch`, `EvaluationMetrics` (puro; sin Spring/JPA) |
| **Puertos** | OUT `IndicatorAnalysisPort`, `PrimarySurveyBatchPort`; `EvaluationMetricsPort.countFodaCompleteByProcessId`; IN upsert/get analysis, metrics, register/list survey |
| **Compuertas** | E2 `COLLECTION`: 100% evidencia + ≥1 lote + entregables E2 APPROVED. E3 `SYSTEMATIZATION`: 100% FODA (`totalIndicators==0` → BLOCK) + `FODA_FORMS_COMPLETE`/`SYNTHESIS_MATRIX` APPROVED. E4 `DRAFTING` (M9): 4 códigos E4 con `documentAssetId` + APPROVED |
| **Bootstrap** | `MethodologicalStageBootstrapper` asegura entregables faltantes; `GET …/stages` llama `bootstrapForProcess` (crea E1–E7 si el proceso no tenía etapas) |
| **Migración Flyway** | `V20__indicator_analysis_e3_deliverables.sql` (prod); Docker dev: Hibernate + `DevWorkflowSchemaAlignmentRunner` (audience 1–32 + backfill E3) |
| **Errores clave** | 404 `ANALYSIS_NOT_FOUND` / `INDICATOR_NOT_FOUND`; 400 `VALIDATION_ERROR`; 403 `PROGRAM_SCOPE_DENIED` (asset de otro proceso); PUT FODA no-CC → 403 `ACCESS_DENIED` |
| **Frontend** | Panel métricas en detalle; form FODA en árbol (edita CC, lectura TD/JD); registro lote en timeline E2; hooks Orval `evaluation` |
| **Pendiente** | CAE_MEMBER / `IndicatorDelegation`; M10 freeze; M11 ciclo E7 |

#### B.4.4 MOD-EVALUATION — Plan de Mejoras y documentos E4 (`DD-UC-031`, M9 / v2.1)

**Implementación:** Sprint 03 PM-017 · **Prompt:** `PR-IMPL-M9-031` · **FSD:** UC-031 / UC-028 · **ADR:** ADR-0005

| Área | Detalle vigente (M9) |
| --- | --- |
| **Endpoints** | API-WF-13 GET plan; WF-14 POST ítem; WF-15 POST import-from-foda; WF-16 PUT ítem; WF-17 POST generate E4 |
| **Dominio** | `ImprovementPlanItem` (puro); `StageDeliverable.documentAssetId`; catálogo E4 `SELF_ASSESSMENT_REPORT` / `OFFICIAL_DATA_FORM` / `IMPROVEMENT_PLAN` / `ACADEMIC_DEV_PLAN` |
| **Puertos** | OUT `ImprovementPlanItemPort`, `DraftingDocumentRendererPort` (OpenPDF, **no** `PdfRendererPort`); IN list/create/update/import + `GenerateStageDeliverableDocumentUseCase` |
| **PDF** | Leyenda «Borrador institucional SIGESA — no es plantilla CNACU/CEUB oficial»; SHA-256 + `DocumentAsset.create` + `EvidenceBlobStoragePort` |
| **Compuerta E4** | `evaluateDrafting`: 4 entregables con asset y APPROVED. Aprobar E4 sin asset → `409 DELIVERABLE_WITHOUT_DOCUMENT` |
| **Migración Flyway** | `V21__improvement_plan_e4_deliverables.sql` (prod); Docker dev: Hibernate + `DevWorkflowSchemaAlignmentRunner` (backfill E4 + índice único parcial) |
| **Unicidad source** | UNIQUE parcial PostgreSQL `(process_id, source_indicator_id) WHERE source_indicator_id IS NOT NULL`; H2: unicidad en servicio |
| **Errores clave** | 400 `VALIDATION_ERROR`; 404 `IMPROVEMENT_ITEM_NOT_FOUND`; 409 `INVALID_STAGE_STATE` / `DELIVERABLE_IMMUTABLE` / `DELIVERABLE_WITHOUT_DOCUMENT` |
| **Frontend** | Sección Plan de Mejoras en detalle; botón «Generar borrador» en timeline DRAFTING (CC); folio si hay asset; Orval `evaluation` + `stage-workflow` |
| **Pendiente** | M10 freeze; M11 ciclo E7; plantillas CNACU reales |

#### B.4.1 MOD-WORKFLOW — indicador y cierre Nivel 1 (DD-UC-008, 009, 010)

| Área | Legacy (prod) | Objetivo v2.0 |
|---|---|---|
| **Rechazo** | `POST /subphases/{id}/reject` (API-SUB-03) | `POST /indicators/{id}/reject` (API-WF-01) |
| **Aprobación** | `POST /subphases/{id}/approve` (API-SUB-04) | `POST /indicators/{id}/approve` (API-WF-02) |
| **Cierre agregado** | `POST /phases/{id}/complete` — `PhaseCompleted` | `POST /level1-nodes/{id}/complete` — `Level1Completed` |
| **Flyway** | V12 subphase status, V13 phase status | V14+ indicator + level1 status |
| **Eventos outbox** | `SubphaseApproved`, `PhaseCompleted` | `IndicatorApproved`, `Level1Completed` |

### B.5 MOD-ASSISTANT — contrato técnico vigente (`DD-SYS-002`)

**Implementación:** Sprint 02 PM-001…PM-023 · **Prompts:** `PR-IMPL-012`, `PR-IMPL-013`, `PR-IMPL-033`, `PR-IMPL-032` · **PRD:** PRD-REQ-028

| Área | Detalle vigente |
| --- | --- |
| **Endpoints** | `GET /api/v1/assistant/status`; `POST /api/v1/assistant/chat` |
| **RBAC chat** | Todo usuario autenticado con JWT válido |
| **RBAC tools** | Registro dinámico por rol JWT + subset agente en executor — matriz [`TOOL-CATALOG.md`](../design/assistant/TOOL-CATALOG.md) §1.2.1 |
| **Auditoría tools** | `AssistantToolAuditPort` → SLF4J `AUDIT_ASSISTANT_TOOL` (userId, role, agent, tool, outcome) |
| **RBAC tools (resumen)** | Usuarios → **solo JD**; fases de proceso → **JD + TD**; CC/EE sin tools administrativas |
| **Proxy LLM** | Backend → Open WebUI `POST {baseUrl}/v1/chat/completions` (HTTP/1.1). **FEAT-03:** timeout 10 s; hilos virtuales/`sendAsync`; 504 `ASSISTANT_GATEWAY_TIMEOUT` |
| **Tool calling** | Loop multi-tool en `SendChatMessageService` (max **5** iter. default); una tool/iteración; `AssistantToolRegistry` + `AssistantToolExecutor`; payload OpenAI `tools[]` + `tool_calls` |
| **Nivel 4 (multi-tool)** | LLM encadena tools secuenciales; respuesta combinada por `AssistantResponseFormatter`; traza `steps[]` en API y UI |
| **RAG normativo** | `search_normative_docs` + `AssistantNormativeRagService`; FTS PostgreSQL; config `rag-enabled`, `rag-max-chunks` |
| **Tools vigentes** | Catálogo [`TOOL-CATALOG.md`](../design/assistant/TOOL-CATALOG.md) — read + write con confirmación en chat |
| **Auth hacia Open WebUI** | `Authorization: Bearer {SIGESA_ASSISTANT_API_KEY}` — solo servidor |
| **Modelo default** | `llama3.2:3b` (`SIGESA_ASSISTANT_MODEL`) |
| **Config YAML** | `sigesa.assistant.*` — `max-tool-iterations` (default 5), `rag-enabled`, `rag-max-chunks`; **8.1.0 FEAT-03:** `timeout` default 10 s |
| **Variables entorno** | `SIGESA_ASSISTANT_*`, `SIGESA_ASSISTANT_MAX_TOOL_ITERATIONS`, `SIGESA_ASSISTANT_RAG_ENABLED`, `SIGESA_ASSISTANT_RAG_MAX_CHUNKS`; **FEAT-03:** `SIGESA_ASSISTANT_TIMEOUT_MS` (default `10000`) |
| **Docker Compose (dev)** | Servicios `ollama` (:11434), `open-webui` (:3001→8080); backend `depends_on` open-webui healthy |
| **Frontend** | `/ayuda` (`AssistantChatUI` — diseño página completa); copilotos dominio vía **`DomainCopilotFloatingChat`** ([DD-AGENT-UI-SHELL](../design/assistant/DD-AGENT-UI-SHELL.md)): `PhasesCopilotPanel`, `UsersCopilotPanel`, `EvidenceCopilotPanel`; historial conversaciones `sessionStorage`; metadata + traza vía `CopilotAssistantMetadata` |
| **Agentes embebidos** | `agent=phases` ([DD-AGENT-001](../design/assistant/DD-AGENT-001.md)); `agent=users` ([DD-AGENT-002](../design/assistant/DD-AGENT-002.md)) — JD-only + 403; `agent=evidence` ([DD-AGENT-003](../design/assistant/DD-AGENT-003.md)) — JD/TD/CC + MCP `mcp/sigesa-evidence`; **shell UI compartido** [DD-AGENT-UI-SHELL](../design/assistant/DD-AGENT-UI-SHELL.md) |
| **Respuesta chat** | `{ reply, toolId, sourceTables, path, llmInvoked, steps[] }` |
| **Errores API** | 503 `ASSISTANT_UNAVAILABLE`; 502 `ASSISTANT_COMPLETION_FAILED`; **504 `ASSISTANT_GATEWAY_TIMEOUT` (FEAT-03 / #52)**; 403 `ACCESS_DENIED` (agente users sin rol JD) |
| **Persistencia chats** | Ninguna (historial en memoria del navegador) |
| **Streaming** | No (`stream: false`) |
| **Salida asistente** | `AssistantReplyOutputGuard` — refuerzo lexical cuando tool falla y el usuario pide confirmar hechos (evals v3, `SIN-CRIT-001`) |
| **Evals offline (QA)** | [`tools/evals/`](../../tools/evals/) — dataset dorado 15 casos; compuerta `./run.sh gate --prompt-version v3` (0 críticos, ≥12/15); juez LLM calibrado (κ ≥ 0.6); informe [`INFORME_EVALS.md`](../../tools/evals/reports/INFORME_EVALS.md) |
| **Design doc** | [`docs/design/DD-SYS-002.md`](../design/DD-SYS-002.md) §11 |

### B.6 MOD-REVIEW — evaluador externo [EE] (`DD-UC-019`)

**Implementación:** Sprint 02 PM-003 · **Prompt:** `PR-IMPL-014` · **FSD:** FSD-UC-019

| Área | Detalle vigente |
| --- | --- |
| **Rol dominio** | `Role.EE` en enum JWT |
| **Alta usuario** | [JD] vía `POST /api/v1/admin/users`; `programId` obligatorio |
| **Scope** | `user_program_assignment` — una carrera (FSD-BR-09) |
| **Lectura** | `GET /api/v1/dashboards/me/summary`, `GET /api/v1/dashboards/coordinator/details` |
| **Prohibido** | Carga evidencias, export dashboard, admin, reportes PDF (FSD-BR-19) |
| **Seed dev** | `ee@umss.edu.bo` / `EvalDemo2026!` → carrera INF-SIS |

### B.8 MOD-WORKFLOW — etapas metodológicas (`DD-UC-025`, M6 / v2.1)

**Implementación:** Sprint 03 PM-010 · **Prompt:** `PR-IMPL-M6-025` · **FSD:** UC-025…028 · **ADR:** ADR-0005

| Área | Detalle vigente (M6) |
| --- | --- |
| **Endpoints** | API-WF-04 `GET /processes/{id}/stages`; WF-05 submit; WF-06 approve/observe; WF-07 gate; WF-08 approve deliverable |
| **RBAC** | CC submit; TD/JD approve/observe; TD gate + deliverables |
| **Puertos aplicación** | IN: `ListProcessStagesUseCase`, `SubmitStageForReviewUseCase`, `ApproveStageUseCase`, `ObserveStageUseCase`, `EvaluateStageGateUseCase`, `ApproveStageDeliverableUseCase`; OUT: **`MethodologicalStagePort`**, **`EvaluationMetricsPort`** |
| **Bootstrap** | `MethodologicalStageBootstrapper` en `CreateProcessUseCaseImpl` — 7 etapas + catálogo E1/E2 |
| **Compuertas** | `StageGateEvaluator` — E1 entregables; E2 100% indicadores con evidencia + encuesta primaria |
| **Tablas JPA** | `methodological_stages`, `stage_deliverables`, `stage_gate_evaluations`, `primary_survey_batches` |
| **Columnas proceso** | `accreditation_processes.operational_mode`, `current_stage_id` |
| **Migración Flyway** | `V17__methodological_workflow_stages.sql` (prod); dev Docker: Hibernate `ddl-auto: update` + `DevWorkflowSchemaAlignmentRunner` |
| **Errores clave** | 409 `STAGE_GATE_BLOCKED`, `INVALID_STAGE_STATE`; 404 `STAGE_NOT_FOUND` |
| **Frontend** | `MethodologicalStageTimeline` + container en `ProcessDetailView`; Orval `stage-workflow` |
| **M10–M11 + CAE** | Freeze AUDIT_READONLY, checklist E6, generate E7, `CAE_MEMBER` + delegaciones (V22, API-WF-18…23) |

### B.7 MOD-TEMPLATE — plantillas normativas (`DD-UC-021`)

**Objetivo v2.0:** plantilla con árbol **N1→N2→N3→Indicador** (`evaluatorModel`, `code`, `weight`).  
**Legacy en prod:** `template_phases`, `template_subphases`.

| Área | Legacy (prod) | Objetivo v2.0 |
| --- | --- | --- |
| **Endpoints** | API-TPL-01…08 con `phases/subphases` | Misma base `/templates`; payload con `level1Nodes` |
| **Validador** | URL + requisitos subfase | `code`, `weight`, `referenceUrl` por indicador (BR-24, BR-25) |
| **Publicación** | ≥1 fase + 1 subfase | ≥1 indicador en árbol completo |
| **Flyway** | `V5__template_management.sql` | `V14+` tablas normativas v2 |
| **Hook UC-003** | Clona subfases | Clona indicadores con ponderación |

## C. Integraciones

### C.2 Open WebUI + Ollama (MOD-ASSISTANT, dev local)

Stack opcional para desarrollo/demo self-hosted:

| Servicio | Imagen | Puerto host | Rol |
| --- | --- | --- | --- |
| Ollama | `ollama/ollama:latest` | 11434 | Runtime de modelos |
| Open WebUI | `ghcr.io/open-webui/open-webui:main` | 3001 | UI admin + API compatible OpenAI |

El backend SIGESA en Docker usa `SIGESA_ASSISTANT_BASE_URL=http://open-webui:8080/api`. La API key se genera en Open WebUI (Settings → Account → API Keys) y se define en `.env` raíz (gitignored).

**Nota operativa:** forzar HTTP/1.1 en `OpenWebUiChatAdapter` — Java `HttpClient` con HTTP/2 provoca `400 Invalid HTTP request` en uvicorn.


### C.1 React + Orval (Frontend)

El consumo de la API REST se realiza **exclusivamente** mediante hooks de React Query autogenerados por Orval (ubicados en `frontend/src/api/`). Queda prohibida la escritura de clientes HTTP (fetch/axios) manuales en la capa de UI.

- **Ciclo de vida:** Cualquier cambio en los contratos de la API (DTOs del backend) requiere ejecutar el comando `pnpm run generate:api` en el entorno frontend antes de ser consumido por las vistas (ej. `CreateProcessView`).
