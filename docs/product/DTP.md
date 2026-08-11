---
producto: "SIGESA"
grupo: "ACREDIA"
documento: DTP                 
version: v1.2                  
fecha: "2026-07-27"
status: vivo                   
audiencia: dual               
baseline_ref:                 
  dti: "docs/baseline/DTI_vFinal.md"
  tag: "release/2.0.0"
  commit: "HEAD"
release: "release/3.0.0"      
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
  prompt_mapping: "docs/sprints/sprint_02/PROMPT_MAPPING.md"
  design_docs_dir: "docs/design/"     
  adr_dir: "docs/adr/"
---

# Documento Técnico del Producto (DTP) – SIGESA

> **Qué es**: El contrato técnico vigente de SIGESA durante la fase de implementación.
> **Regla de oro**: Cero divergencia silenciosa. El baseline de la Fase de Diseño (`release/2.0.0`) permanece intacto en `docs/baseline/`.

---

## A. Control de cambios (Núcleo del DTP)

### A.1 Changelog de implementación

*(Este cuadro se llenará a medida que se ejecuten los prompts de implementación y se envíen los PRs)*

| Fecha | Cambio | Disparador (FSD-UC / DD) | ADR | PR / commit | Autor |
| ------- | -------- | -------------------------- | ----- | ------------- | ------- |
| 08/08/2026 | **MOD-EVIDENCE search:** Búsqueda inteligente de evidencias de punta a punta con enrutamiento híbrido de consultas, control de acceso carrera (FSD-BR-09) y toggle frontend (header X-AI-Enabled). | FSD-UC-007 / DD-UC-007 | N/A | PR-IMPL-007 / PM-006 | Antigravity Agent |
| 07/08/2026 | **MOD-PROCESS responsable (FSD-UC-023) — Full-Stack:** tabla `process_responsible_assignment` (Flyway V7); API-PROC-09…11; `ProcessResponsiblePort`; extensión UC-019 con `responsible`; UI sección/modal en detalle y listado. | FSD-UC-023 / DD-UC-023 | N/A | PM-010 / PR-IMPL-023 | Boris Anthony Angulo Urquieta |
| 07/08/2026 | **MOD-PROCESS estructura (FSD-UC-022) — Full-Stack:** `ProcessStructureController` API-PROC-05…08; `ProcessStructurePort` + `SubphaseWorkflowPort` (stub); guard `ProcessStructureGuard`; UI `/procesos/{processId}/estructura`; extensión GET detalle con `description`/`referenceUrl`. | FSD-UC-022 / DD-UC-022 | N/A | PM-009 / PR-IMPL-022 | Boris Anthony Angulo Urquieta |
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

### A.3 Estado de implementación por FSD-UC

| FSD-UC | Design Doc | Estado | Release | Tests/Evals | PR-IMPL | Notas |
| -------- | ------------ | -------- | --------- | ------------- | --------- | ------- |
| `FSD-UC-001` | `DD-UC-001` | hecho | `release/3.0.0` | Suite §6 DD-UC-001; JaCoCo pendiente `mvn verify` | `PR-IMPL-001` | JWT + LocalAuthAdapter; A1 estricto → 401 |
| `FSD-UC-002` | `DD-UC-002` | hecho | `release/3.0.0` | Suite §6 DD-UC-002; JaCoCo pendiente `mvn verify` | `PR-IMPL-002` | Alta INACTIVE; revoke soft; 409 email dup |
| `FSD-UC-003` | `DD-UC-003` | **hecho (Full-Stack)** | `release/3.0.0` | Suite unitaria (Mockito); React Hooks | `PR-IMPL-003V3` | Arquitectura Hexagonal (Backend) + UI React c/ Orval |
| `FSD-UC-019` | `DD-UC-019` | **hecho (Full-Stack)** | `v1.0` | Unit `ListProcessesService`, `GetProcessDetailService`, `ProcessAccessPolicy`; WebMvc standalone | `PR-IMPL-019` / PM-003 | GET listado + detalle; [CC] 404 cross-carrera |
| `FSD-UC-021` | `DD-UC-021` | **hecho (backend)** | `v1.0` | Unit validator + services template; WebMvc `TemplateControllerWebMvcTest`; JaCoCo pendiente `mvn verify` | `PR-IMPL-021` / PM-006 | API-TPL-01…08 solo [JD]; FE pendiente `PR-IMPL-021-FE` |
| `FSD-UC-022` | `DD-UC-022` | **hecho (Full-Stack)** | `v1.0` | Unit `ProcessStructureGuard`, `Add/Delete/Reorder*Service`; WebMvc `ProcessStructureControllerWebMvcTest`; `./mvnw test` | `PR-IMPL-022` / PM-009 | API-PROC-05…08 [JD]; UI `/procesos/{id}/estructura`; `SubphaseWorkflowPort` stub v1.0 |
| `FSD-UC-023` | `DD-UC-023` | **hecho (Full-Stack)** | `v1.0` | Unit `Assign/RemoveProcessResponsibleService`; WebMvc `ProcessResponsibleControllerWebMvcTest`; `./mvnw test` 157 tests | `PR-IMPL-023` / PM-010 | API-PROC-09…11 [JD]; UI responsable en `/procesos/{id}` + columna listado |
| `FSD-UC-004` | `DD-UC-004` | en curso | `release/3.0.0` | Unit `UploadEvidenceService`; JaCoCo pendiente | `PR-IMPL-006` | v1 carga; UC-006 subsanación pendiente |
| `FSD-UC-011` | `DD-UC-011` | hecho | `release/3.0.0` | Suite §6 DD-UC-011 (Gherkin TC-09a/c) | `PR-IMPL-011` | Suite Híbrida Compuesta PBAC (`/me/summary`, `/details`, `/export`) conectado a DB real sin stubs |
| `FSD-UC-014` | `DD-UC-014` | en curso | `release/3.0.0` | Unit `*Report*Service`; JaCoCo pendiente `mvn verify` | `PR-IMPL-005` | Stub datos; conectar UC-013 vía `ExecutiveDashboardQueryPort` |
| `FSD-UC-013` | pendiente | pendiente | `release/3.0.0` | — | — | Debe implementar `ExecutiveDashboardQueryPort` para alimentar PDF |
| `FSD-SYS-001` | `DD-SYS-001` | **hecho** | `release/3.0.0` | Tests de conexión locales (Flyway) | `PR-IMPL-004` | Integración con PostgreSQL (Driver, HikariCP, YML) |
| `PRD-REQ-028` | `DD-SYS-002` | **hecho (MVP)** | `release/3.0.0` | Manual E2E `/ayuda`; sin tests automatizados aún | `PR-IMPL-012` | Chat proxy Open WebUI; modelo `llama3.2:3b`; ver §B.5 |
| `FSD-UC-019` | `DD-UC-019` | **hecho** | `release/3.0.0` | `RegisterUserServiceTest` EE; manual E2E dashboard | `PR-IMPL-014` | Rol EE solo lectura; scope carrera; seed `ee@umss.edu.bo` |
| `FSD-UC-007` | `DD-UC-007` | **hecho (Full-Stack)** | `release/3.0.0` | Suite unitaria (Mockito); React Hooks; OxLint | `PR-IMPL-007` | Búsqueda inteligente de evidencias con enrutador híbrido y control de acceso |

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
| §4 Modelo de dominio | no | DTI vFinal §4 |
| §5 Arquitectura hexagonal del core | **sí** | Confirmada su exigencia estricta en FSD-UC-003. Dominio encapsulado. |
| **MOD-AUTH (identidad)** | **sí** | Ver §B.1 abajo; design docs `DD-UC-001`, `DD-UC-002` |
| **MOD-PROCESS (acreditación)** | **sí** | Ver §B.2 abajo; design docs `DD-UC-003`, `DD-UC-019`, **`DD-UC-022` (§B.2.1)**, **`DD-UC-023` (§B.2.2)** |
| **MOD-REPORT (PDF ejecutivo)** | **sí** | Ver §B.3 abajo; design doc `DD-UC-014` |
| **MOD-EVIDENCE (carga v1)** | **sí** | Ver §B.4 abajo; design doc `DD-UC-004` |
| **MOD-ASSISTANT (chatbot MVP)** | **sí** | Ver §B.5 abajo; design doc `DD-SYS-002` |
| **MOD-REVIEW (evaluador externo [EE])** | **sí** | Ver §B.6 abajo; design doc `DD-UC-019` |
| **MOD-TEMPLATE (plantillas normativas)** | **sí** | Ver §B.7 abajo; design doc `DD-UC-021` |
| §8 Despliegue cloud (AWS) | no | DTI vFinal §8 |
| §10 Prompt mapping | **sí (crece)** | `docs/sprints/sprint_02/PROMPT_MAPPING.md` (Sprint 02 — MOD-ASSISTANT PM-001) |
| §21 ADRs | **sí (crece)** | [`docs/adr/`](../adr/) (ADR-0003 MOD-AUTH, **ADR-0002 PostgreSQL**; baseline en `docs/baseline/05_dti/adrs/`) |

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

**Implementación creación:** PM-001 · **Prompts vigentes:** `PR-IMPL-003V3`  
**Implementación consulta:** `PR-IMPL-019` · **Design doc consulta:** `DD-UC-019`

| Área | Detalle vigente |
| --- | --- |
| **Endpoints (Web Adapter)** | `POST /api/v1/processes` ([JD]); `GET /api/v1/processes` ([JD], [TD], [CC]); `GET /api/v1/processes/{processId}` ([JD], [TD], [CC]); catálogo `GET /api/v1/programs?q=` |
| **DTOs (Web Adapter)** | `CreateProcessRequestDto`, `ProcessResponseDto` (enriquecido), `ProcessSummaryResponseDto`, `ProgramSummaryResponse` |
| **Puertos aplicación (consulta)** | `ListProcessesUseCase`, `GetProcessDetailUseCase`; OUT: `ProcessQueryPort` (separado de `AccreditationProcessPort`) |
| **RBAC consulta** | JD/TD: todos los procesos; CC: filtro `career_id ∈ JWT.programScope`; acceso ajeno → **404 `PROCESS_NOT_FOUND`** (no 403) |
| **Lógica de Negocio (Use Case)** | Clonación profunda Plantilla → Proceso (POST); enriquecimiento carrera/plantilla vía `ProgramCatalogPort` + `TemplatePort` (GET) |
| **Tablas JPA (Persistencia)** | `programs`, `templates`, `template_phases`, `template_subphases`, `accreditation_processes`, `phases`, `subphases` |
| **Seed dev carreras** | `ProgramSeedDataLoader` — 25 carreras UMSS (`DevSeedData.PROGRAM_*`) |
| **Regla Unicidad (BD + App)** | Error HTTP 409 `PROCESS_ALREADY_ACTIVE`. Índice parcial `career_id` WHERE `status = 'ACTIVE'` |
| **Errores proceso** | 404 `PROGRAM_NOT_FOUND`, 404 `TEMPLATE_NOT_FOUND`, 404 `PROCESS_NOT_FOUND`, 409 `PROCESS_ALREADY_ACTIVE` |
| **Frontend** | `/procesos` (listado); `/procesos/{processId}` (detalle árbol fases/subfases); `/procesos/nuevo` ([JD]); sidebar desplegable «Ver procesos» / «Nuevo proceso» |
| **Hooks Orval** | `useListProcesses`, `useGetProcess`, `useCreateProcess` en `procesos-de-acreditación.ts` |
| **Modelos de Dominio** | Puros (sin `@Entity`); JPA vía mappers/adapters hexagonales |

#### B.2.1 MOD-PROCESS — estructura en proceso ACTIVE (`DD-UC-022`)

**Implementación:** Sprint 02 PM-009 · **Prompt:** `PR-IMPL-022` · **FSD:** FSD-UC-022 · **Estado:** **hecho (Full-Stack)**

| Área | Detalle vigente |
| --- | --- |
| **Endpoints** | `POST/PUT/DELETE /api/v1/processes/{processId}/phases[/{phaseId}]`; CRUD subfases bajo fase; `PUT /processes/{processId}/structure/reorder` |
| **RBAC** | Solo **[JD]** — `@PreAuthorize("hasRole('JD')")` en `ProcessStructureController` |
| **Puertos aplicación** | IN: `AddProcessPhaseUseCase` … `ReorderProcessStructureUseCase`; OUT: **`ProcessStructurePort`**, **`SubphaseWorkflowPort`** (stub v1.0 → `false`) |
| **Adaptadores** | `ProcessStructureJpaAdapter` (operaciones granulares; evita orphanRemoval en agregado); `SubphaseWorkflowStubAdapter` |
| **Guard** | `ProcessStructureGuard` — solo `process.status == ACTIVE` → else `409 PROCESS_NOT_EDITABLE` |
| **Errores clave** | 400 `SUBPHASE_LINK_REQUIRED`, `PROCESS_STRUCTURE_ORDER_CONFLICT`; 409 `SUBPHASE_HAS_EVIDENCE`, `PROCESS_NOT_EDITABLE` |
| **Extensión UC-019** | GET detalle incluye `description` + `referenceUrl` en subfases |
| **Dependencia** | `PR-IMPL-021` (V5 + clonación `referenceUrl` desde plantilla) |
| **Frontend** | `/procesos/{processId}/estructura` — `ProcessStructurePage`, `useProcessStructureEditor`, Orval `estructura-de-proceso/` |
| **Anti-patrón JPA** | No persistir agregado `AccreditationProcess` completo con colecciones reemplazadas; no preasignar UUID con `@GeneratedValue` |

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

**Implementación:** PM-010 · **Prompts:** `PR-IMPL-005` · **FSD:** FSD-UC-014

| Área | Detalle vigente |
|---|---|
| **Dependencia Maven** | `com.github.librepdf:openpdf:2.0.3` |
| **Endpoints** | `POST /api/v1/reports/executive/pdf` → **202** `{ jobId }`; `GET .../pdf/{jobId}` → estado; `GET .../pdf/{jobId}/download` → `application/pdf` |
| **RBAC** | Solo `[JD]` — `SecurityConfig` + FSD-BR-14 |
| **Tabla JPA** | `report_job` (`id`, `requester_id`, filtros, `status`, `artifact_key`, `error_code`, timestamps) |
| **Storage artefactos** | Filesystem local `sigesa.report.storage-path` (default `./data/reports`) |
| **Job async** | `@Async("reportJobExecutor")` — `ReportJobAsyncDispatcher` |
| **Estados job** | `PENDING` → `IN_PROGRESS` → `COMPLETED` \| `FAILED` |
| **Errores job** | `REPORT_TEMPLATE`, `REPORT_GENERATION_FAILED` |
| **Datos PDF** | v1.0: `ExecutiveDataStubAdapter`; post UC-013: `ExecutiveDashboardQueryPort` + `ExecutiveDataDashboardAdapter` (@Primary) |
| **Integración UC-013** | MOD-DASH implementa `ExecutiveDashboardQueryPort.fetchExecutiveSnapshot()` leyendo la misma proyección que `GET /dashboard/executive` |

### B.4 MOD-EVIDENCE — contrato técnico vigente (DD-UC-004)

**Implementación:** PM-012 · **Prompts:** `PR-IMPL-006` · **FSD:** FSD-UC-004

| Área | Detalle vigente |
|---|---|
| **Endpoint** | `POST /api/v1/indicators/{indicatorId}/evidences` (multipart) |
| **RBAC** | Solo `[CC]`; alcance carrera vía `user_program_assignment` (FSD-BR-09) |
| **Tablas JPA** | `indicator`, `indicator_state_history`, `evidence`, `evidence_version` |
| **Estado Indicador** | Append-only history; transición upload: `PENDIENTE → SUBIDO` |
| **Hash** | SHA-256 hex (`Sha256ContentHashAdapter`) |
| **Storage** | `./data/evidences` (local v1.0) |
| **MIME** | pdf, doc/docx, xls/xlsx, png, jpeg — max 50MB |
| **Lock upload** | `InMemoryEvidenceUploadLockAdapter` (FSD-BR-18 anti-doble-envío) |
| **Notificaciones** | `NoOpNotificationOutboxAdapter` → `EvidenceUploaded` (UC-015 stub) |
| **Seed dev** | `cc@umss.edu.bo` / indicador `550e8400-…-440003` PENDIENTE |

### B.5 MOD-ASSISTANT — contrato técnico vigente (`DD-SYS-002`)

**Implementación:** Sprint 02 PM-001 + PM-002 · **Prompts:** `PR-IMPL-012`, `PR-IMPL-013` · **PRD:** PRD-REQ-028

| Área | Detalle vigente |
| --- | --- |
| **Endpoints** | `GET /api/v1/assistant/status`; `POST /api/v1/assistant/chat` |
| **RBAC chat** | Todo usuario autenticado con JWT válido |
| **RBAC tools** | Registro dinámico por rol JWT — matriz [`TOOL-CATALOG.md`](../design/assistant/TOOL-CATALOG.md) §2.0 |
| **RBAC tools (resumen)** | Usuarios → **solo JD**; fases de proceso → **JD + TD**; CC/EE sin tools |
| **Proxy LLM** | Backend → Open WebUI `POST {baseUrl}/v1/chat/completions` (HTTP/1.1) |
| **Tool calling** | Loop en `SendChatMessageService` (max 5 iter. default); `AssistantToolRegistry` + `AssistantToolExecutor`; payload OpenAI `tools[]` + `tool_calls` |
| **Tools vigentes** | Catálogo [`TOOL-CATALOG.md`](../design/assistant/TOOL-CATALOG.md) — read + write con confirmación en chat |
| **Auth hacia Open WebUI** | `Authorization: Bearer {SIGESA_ASSISTANT_API_KEY}` — solo servidor |
| **Modelo default** | `llama3.2:3b` (`SIGESA_ASSISTANT_MODEL`) |
| **Config YAML** | `sigesa.assistant.*` — incluye `max-tool-iterations` (default 5) |
| **Variables entorno** | `SIGESA_ASSISTANT_ENABLED`, `SIGESA_ASSISTANT_BASE_URL`, `SIGESA_ASSISTANT_API_KEY`, `SIGESA_ASSISTANT_MODEL`, `SIGESA_ASSISTANT_MAX_TOOL_ITERATIONS` |
| **Docker Compose (dev)** | Servicios `ollama` (:11434), `open-webui` (:3001→8080); backend `depends_on` open-webui healthy |
| **Frontend** | Ruta `/ayuda`; feature `frontend/src/features/assistant/`; contrato REST sin cambios (`{ reply }`) |
| **Errores API** | 503 `ASSISTANT_UNAVAILABLE`; 502 `ASSISTANT_COMPLETION_FAILED` |
| **Persistencia chats** | Ninguna (historial en memoria del navegador) |
| **Streaming** | No (`stream: false`) |
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

### B.7 MOD-TEMPLATE — plantillas normativas (`DD-UC-021`)

**Implementación:** Sprint 02 PM-006 · **Prompt:** `PR-IMPL-021` · **FSD:** FSD-UC-021

| Área | Detalle vigente |
| --- | --- |
| **Endpoints** | `GET/POST /api/v1/templates`; `GET/PUT/DELETE /api/v1/templates/{templateId}`; `POST …/publish`, `…/duplicate`, `…/archive` |
| **RBAC** | Solo **[JD]** — `@PreAuthorize("hasRole('JD')")` en `TemplateController` |
| **Puertos aplicación** | IN: `CreateTemplateUseCase` … `DeleteTemplateUseCase`; OUT write: **`TemplateManagementPort`**; OUT read: **`TemplatePort`** (CQRS) |
| **Validador dominio** | `TemplateStructureValidator` — URL HTTPS obligatoria, órdenes únicos, estructura mínima al publicar |
| **Ciclo de vida** | `DRAFT` → `PUBLISHED` → `ARCHIVED`; duplicar crea copia `DRAFT` |
| **Errores clave** | 400 `TEMPLATE_SUBPHASE_LINK_REQUIRED`, `TEMPLATE_STRUCTURE_INCOMPLETE`, `TEMPLATE_ORDER_CONFLICT`; 409 `TEMPLATE_IN_USE`; 400 `TEMPLATE_NOT_PUBLISHED` en `POST /processes` |
| **Hook UC-003** | `CreateProcessUseCaseImpl` rechaza plantilla no `PUBLISHED`; clona `referenceUrl`/`description` a subfases del proceso |
| **Migración Flyway** | `V5__template_management.sql` (columnas `description`, `status`, `reference_url`, timestamps) — perfil **prod** |
| **Dev Docker** | `application-dev.yaml`: Hibernate `ddl-auto: update`; Flyway deshabilitado (delta §A.2 #13) |
| **Tablas JPA** | `templates`, `template_phases`, `template_subphases` |
| **Frontend** | Pendiente `PR-IMPL-021-FE` — rutas `/admin/plantillas/**` |
| **Hooks Orval (post-FE)** | `useListTemplates`, `useGetTemplate`, `useCreateTemplate`, … en `plantillas-normativas.ts` |

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
