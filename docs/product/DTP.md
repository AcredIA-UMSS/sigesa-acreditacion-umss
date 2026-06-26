---
producto: "SIGESA"
grupo: "ACREDIA"
documento: DTP                 
version: v1.0                  
fecha: "22/06/2026"
status: vivo                   
audiencia: dual               
baseline_ref:                 
  dti: "docs/baseline/DTI_vFinal.md"
  tag: "release/2.0.0"
  commit: "HEAD"
release: "release/3.0.0"      
stack:
  - "Java 21"
  - "Spring Boot 3.x"
  - "Hibernate / Spring Data JPA"
  - "H2 (Local) / PostgreSQL (Prod)"
  - "React"
  - "AWS"
repo: "ruta/a/tu/repo/sigesa"
agents_md: "/AGENTS.md"
artefactos_vivos:
  prd: "docs/product/03_prd/PRD.md"          
  fsd: "docs/product/FSD.md"          
  prompt_mapping: "docs/PROMPT_MAPPING.md"
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
|-------|--------|--------------------------|-----|-------------|-------|
| 26/06/2026 | Implementación MOD-EVIDENCE: carga v1 multipart, SHA-256, `indicator_state_history`, outbox stub, seed CC. | FSD-UC-004 / DD-UC-004 | N/A | PM-012 / PR-IMPL-006 | Cursor Agent |
| 26/06/2026 | Implementación MOD-REPORT: jobs PDF asíncronos, OpenPDF, tabla `report_job`, endpoints polling/descarga; stub datos ejecutivos + puente `ExecutiveDashboardQueryPort` para UC-013. | FSD-UC-014 / DD-UC-014 | N/A | PM-010 / PR-IMPL-005 | Cursor Agent |
| 23/06/2026 | Sync inconsistencias MOD-AUTH: diagramas, modelo_datos, api_contracts, ADR-0003 vivo, FSD-BR-12. | FSD-UC-001, FSD-UC-002 / DD-UC-001 | ADR-0003 | docs sync | Cursor Agent |
| 22/06/2026 | `@dtp-sync` DD-UC-001: consolidación MOD-AUTH en DTP, FSD, api_contracts, modelo_datos. | FSD-UC-001, FSD-UC-002 / DD-UC-001 | ADR-0003 | `f38976b` / PM-007 | Cursor Agent |
| 22/06/2026 | Implementación MOD-AUTH (JWT, login, admin users, user_program_assignment, hardening code-review). | FSD-UC-001, FSD-UC-002 / DD-UC-001 | ADR-0003 | `5cd14df`…`f38976b` | Cursor Agent |
| 22/06/2026 | Implementación core de MOD-PROCESS (Dominio, Casos de Uso, Controladores y Stubs JPA para plantillas). | FSD-UC-003 / DD-UC-003 | N/A | Pendiente | Boris Angulo |
| 22/06/2026 | Inicialización de la arquitectura base Spring Boot y DTP vivo. | N/A | N/A | `init` | Boris Angulo |

### A.2 Deltas respecto al DTI vFinal

> Diferencias **deliberadas** entre lo diseñado y lo construido. 

| # | Sección del DTI afectada | Qué decía el DTI vFinal | Qué dice ahora el DTP | Motivo | ADR |
|---|--------------------------|-------------------------|-----------------------|--------|-----|
| 1 | Perímetro API | Endpoints legacy sin auth explícita en DTI piloto | Todo `/api/v1/**` excepto `POST /auth/login` exige JWT Bearer | MOD-AUTH v1.0 unifica seguridad antes de MOD-EVIDENCE | N/A (DD-UC-001) |
| 2 | Entrega password temporal | No especificado en API baseline | Alta genera password en servidor; entrega **offline** v1.0 (no en JSON response) | Evitar exposición en tránsito; capacitación [JD] | N/A |
| 3 | Migración DDL MOD-AUTH | Índice parcial en DTI | Flyway perfil `prod` + script `V1__mod_auth_uk_upa_active.sql`; H2 dev: `AuthSchemaInitializer` | Hibernate no genera índices parciales | N/A |
| 4 | Motor PDF | DTI piloto Node (PDFKit/ReportLab spike) | **OpenPDF 2.0.3** en backend Java (`OpenPdfRendererAdapter`) | Stack runtime = Java 21 / Spring Boot 4.x (ADR-009 plan B) | N/A |
| 5 | API reportes | Solo `POST /reports/executive/pdf` en catálogo baseline | Job asíncrono: `POST` 202 + `GET /{jobId}` + `GET /{jobId}/download` bajo `/api/v1` | Alineado a MAR-SEQ-005 y DD-UC-014 | DD-UC-014 |
| 6 | Fuente datos PDF | Proyección CQRS `proj_executive_semaphore` (DTI async) | v1.0: `ExecutiveDataStubAdapter`; v1.0+UC-013: `ExecutiveDashboardQueryPort` → `ExecutiveDataDashboardAdapter` | UC-013 pendiente | DD-UC-014 |
| 7 | Storage evidencias | S3 en DTI cloud | Filesystem local `sigesa.evidence.storage-path` v1.0 | Piloto local H2 | DD-UC-004 |

### A.3 Estado de implementación por FSD-UC

| FSD-UC | Design Doc | Estado | Release | Tests/Evals | Notas |
|--------|------------|--------|---------|-------------|-------|
| `FSD-UC-001` | `DD-UC-001` | hecho | `release/3.0.0` | Suite §6 DD-UC-001; JaCoCo pendiente `mvn verify` | JWT + LocalAuthAdapter; A1 estricto → 401 |
| `FSD-UC-002` | `DD-UC-001` | hecho | `release/3.0.0` | Suite §6 DD-UC-001; JaCoCo pendiente `mvn verify` | Alta INACTIVE; revoke soft; 409 email dup |
| `FSD-UC-003` | `DD-UC-003` | hecho (core) | `release/3.0.0` | Pendiente | Faltan queries SQL nativas en JPA Adapters |
| `FSD-UC-004` | `DD-UC-004` | en curso | `release/3.0.0` | Unit `UploadEvidenceService`; JaCoCo pendiente | v1 carga; UC-006 subsanación pendiente |
| `FSD-UC-014` | `DD-UC-014` | en curso | `release/3.0.0` | Unit `*Report*Service`; JaCoCo pendiente `mvn verify` | Stub datos; conectar UC-013 vía `ExecutiveDashboardQueryPort` |
| `FSD-UC-013` | pendiente | pendiente | `release/3.0.0` | — | Debe implementar `ExecutiveDashboardQueryPort` para alimentar PDF |

### A.4 Trazabilidad código ↔ DTP

`BRD/MRD (baseline)` → `PRD/FSD vivo (FSD-UC-NNN)` → `Design Doc (DD-UC-NNN)` → `Prompt (PR-IMPL-NNN)` → `PR/commit` → `Tests/Evals` → `ADR (si aplica)` → **DTP**.

---

## B. Contenido técnico vigente

> SIGESA utiliza arquitectura hexagonal. Cualquier desviación de los principios de Clean Architecture o del uso estricto de DTOs en controladores será documentada aquí.

| Sección (espejo del DTI) | ¿Cambió vs DTI vFinal? | Dónde está la versión vigente |
|--------------------------|------------------------|-------------------------------|
| §1 Visión del producto | no | DTI vFinal §1 |
| §2 Contexto del sistema (C4 N1) | no | DTI vFinal §2 |
| §3 Arquitectura de alto nivel (C4 N2/N3) | no | DTI vFinal §3 |
| §4 Modelo de dominio | no | DTI vFinal §4 |
| §5 Arquitectura hexagonal del core | no | DTI vFinal §5 |
| **MOD-AUTH (identidad)** | **sí** | Ver §B.1 abajo; design doc `DD-UC-001` |
| **MOD-REPORT (PDF ejecutivo)** | **sí** | Ver §B.2 abajo; design doc `DD-UC-014` |
| **MOD-EVIDENCE (carga v1)** | **sí** | Ver §B.3 abajo; design doc `DD-UC-004` |
| §8 Despliegue cloud (AWS) | no | DTI vFinal §8 |
| §10 Prompt mapping | **sí (crece)** | `docs/PROMPT_MAPPING.md` |
| §21 ADRs | **sí (crece)** | [`docs/adr/`](../adr/) (ADR-0003 MOD-AUTH; baseline en `docs/baseline/05_dti/adrs/`) |

### B.1 MOD-AUTH — contrato técnico vigente (DD-UC-001)

**Implementación:** `5cd14df` … `f38976b` · **Prompts:** `PR-IMPL-004` · **PM:** PM-001…PM-007

| Área | Detalle vigente |
|---|---|
| **Endpoints** | `POST /api/v1/auth/login` (público); `POST /api/v1/admin/users` ([JD]); `PATCH /api/v1/admin/users/{id}/deactivate` ([JD]) |
| **Perímetro JWT** | Todo `/api/v1/**` excepto login exige `Authorization: Bearer` (delta §A.2 #1) |
| **Tablas JPA** | `app_user`, `user_program_assignment` |
| **Índice parcial** | `uk_upa_active` — Flyway (`application-prod.yaml`) o `AuthSchemaInitializer` (dev/test H2) |
| **Password hashing** | Argon2id (`Argon2PasswordEncoder`) |
| **JWT** | HS256; claims `sub`, `email`, `role`, `programScope[]`; secret `SIGESA_JWT_SECRET`; TTL `SIGESA_JWT_EXPIRATION_SECONDS` (default 86400) |
| **Errores HTTP** | `401 AUTH_INVALID_CREDENTIALS` (A1 login); `403 ACCESS_DENIED` (A2); `409 EMAIL_ALREADY_REGISTERED`; `422 INVALID_EMAIL_DOMAIN` / `INVALID_SCOPE` / `INVALID_ROLE` |
| **Password temporal alta** | Generado en servidor; entrega **offline** v1.0 (delta §A.2 #2) |
| **Audit** | `AuditLogPort` → `NoOpAuditLogAdapter` (stub UC-017) |
| **Bloqueo por intentos** | Columnas `failed_attempts`/`locked_until` en DDL; lógica **diferida v1.1** (sin `429 AUTH_LOCKED` en v1.0) |
| **Seed dev** | `jd@umss.edu.bo` / `ChangeMe123!` (`AuthDataLoader`) |

### B.2 MOD-REPORT — contrato técnico vigente (DD-UC-014)

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

### B.3 MOD-EVIDENCE — contrato técnico vigente (DD-UC-004)

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

## C. Integraciones

### C.1 React Orval
El consumo de la API REST se realiza exclusivamente mediante hooks de React Query autogenerados por Orval (frontend/src/api/). Cualquier cambio en los DTOs del backend requiere ejecutar pnpm run generate:api en el frontend