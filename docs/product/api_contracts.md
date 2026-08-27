# Contratos API — SIGESA / AcredIA

## Control de versión

| Campo | Valor |
|-------|-------|
| **Versión** | Dorada v1.0 |
| **Timestamp** | `2026-05-16T18:30:00-04:00` |
| **Fuente** | [`FSD.md`](FSD.md) §8 · [`reglas_negocio.md`](reglas_negocio.md) |
| **OpenAPI (futuro)** | `docs/05_dti/openapi.yaml` (pendiente DTI) |

> Contratos **lógicos** REST v1. El cliente **no** envía `estado` en payloads; el backend aplica la máquina de estados. Autenticación: JWT Bearer (sesión UMSS).

---

## 1. Convenciones globales

| Aspecto | Valor |
|---------|-------|
| Base URL | `/api/v1` |
| Formato | `application/json` (salvo upload: `multipart/form-data`) |
| Auth | `Authorization: Bearer {token}` |
| Errores | `{ "error": "ERROR_CODE", "message": "...", "details": {} }` (campo `error` en v1.0; specs legacy pueden decir `code`) |
| Paginación | `?page=&size=`; respuesta `{ "items": [], "total": n }` |
| Idempotencia | `Idempotency-Key` en POST críticos (carga, importación) |

### Códigos HTTP frecuentes

| Código | Uso |
|--------|-----|
| 401 | Sin sesión / token inválido (`UNAUTHORIZED` en perímetro JWT); login A1 → `AUTH_INVALID_CREDENTIALS` |
| 403 | Rol o alcance insuficiente |
| 409 | Conflicto de estado (`EVIDENCE_IMMUTABLE`, `FASE_CIERRE_BLOQUEADO`, `PROCESS_ALREADY_ACTIVE`) |
| 422 | Validación (`JUSTIFICATION_REQUIRED`, `EVIDENCE_UNCLASSIFIED`) |

---

## 2. Seguridad (OpenAPI fragment)

```yaml
openapi: 3.0.3
info:
  title: SIGESA API
  version: "1.0.0"
  description: Sistema de automatización de acreditación UMSS

components:
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT

  schemas:
    Error:
      type: object
      required: [error, message]
      properties:
        error:
          type: string
        message:
          type: string
        details:
          type: object

security:
  - bearerAuth: []
```

---

## 3. MOD-AUTH

> Rutas relativas a Base URL `/api/v1`. Errores MOD-AUTH usan campo `error` (no `code`).

### API-AUTH-01 — `POST /auth/login`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-001 |
| **Roles** | — (público) |
| **Body** | `{ "email": "user@umss.edu.bo", "password": "***" }` |
| **200** | `{ "accessToken", "expiresIn", "role", "programScope" }` |
| **401** | `AUTH_INVALID_CREDENTIALS` (mensaje genérico; A1: dominio inválido, vacío, user/password incorrecto) |

### API-USER-01 — `POST /admin/users`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-002 |
| **x-allowed-roles** | `[JD]` |
| **Body** | `{ "email", "role", "programId?" }` |
| **201** | `{ "userId", "status": "INACTIVE" }` |
| **409** | `EMAIL_ALREADY_REGISTERED` (mensaje genérico) |
| **422** | `INVALID_EMAIL_DOMAIN` si no es `@umss.edu.bo` |

### API-USER-02 — `PATCH /admin/users/{id}/deactivate`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-002 |
| **x-allowed-roles** | `[JD]` |
| **204** | Usuario desactivado; historial conservado |

### API-USER-03 — `GET /admin/users`

> **Contrato completo:** [`docs/product/api/API-USER-03.md`](api/API-USER-03.md)

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-002 |
| **x-allowed-roles** | `[JD]` |
| **Query** | `role?` (`CC`/`TD`/`JD`), `status?` (`INACTIVE`/`ACTIVE`/`DEACTIVATED`) |
| **200** | `[{ "userId", "email", "role", "status", "programIds" }]` |
| **401** | `UNAUTHORIZED` — sin JWT o token inválido |
| **403** | Rol distinto de JD |
| **422** | `INVALID_ROLE` / `INVALID_FILTER` si filtro inválido |
| **Tool asistente** | `list_users`, `set_user_status` (solo JD) — ver [`TOOL-CATALOG`](../design/assistant/TOOL-CATALOG.md) |

### API-CAT-01 — `GET /programs`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-002 (alta CC), FSD-UC-003 (selección carrera al crear proceso) |
| **Auth** | JWT Bearer (cualquier rol autenticado) |
| **Query** | `q?` — búsqueda parcial por nombre o código (autocomplete) |
| **200** | `[{ "id", "code", "name" }]` |
| **Persistencia** | Tabla `programs` (PostgreSQL); seed dev vía `ProgramSeedDataLoader` (25 carreras UMSS) |
| **Migración** | Flyway `V3__programs_catalog.sql` |
| **Adapter** | `ProgramCatalogJpaAdapter` → `ProgramCatalogPort` |
| **Frontend** | `CareerAutocomplete` en `/procesos/nuevo` (debounce 300 ms) |

---

## 4. MOD-PROCESS

### API-PROC-01 — `POST /processes`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-003 |
| **x-allowed-roles** | `[JD]` |
| **Body** | `{ "career_id": "uuid", "template_id": "uuid" }` |
| **Plantillas permitidas** | Solo tipos **CEUB** y **ARCU-SUR** (validación en use case) |
| **201** | Proceso creado con fases/subfases clonadas (`ProcessResponseDto`) |
| **404** | `PROGRAM_NOT_FOUND` / `TEMPLATE_NOT_FOUND` |
| **409** | `PROCESS_ALREADY_ACTIVE` |

### API-PROC-02 — `POST /templates/{templateId}/activate`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-003 |
| **x-allowed-roles** | `[JD]` |
| **Body** | `{ "effectiveFrom": "2026-01-01" }` |
| **200** | Plantilla activa para nuevos procesos |

### API-PROC-03 — `GET /processes`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-019 |
| **x-allowed-roles** | `[JD]`, `[TD]`, `[CC]` |
| **200** | `[ProcessSummaryResponseDto]` — carrera, plantilla, estado, conteos fase/subfase |
| **Filtrado [CC]** | Solo procesos con `career_id ∈ JWT.programScope` |
| **200 vacío** | `[]` si [CC] sin carreras asignadas o sin procesos en alcance |

### API-PROC-04 — `GET /processes/{processId}`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-019 |
| **x-allowed-roles** | `[JD]`, `[TD]`, `[CC]` |
| **200** | `ProcessResponseDto` enriquecido con árbol Fase → Subfase ordenado por `order` (incluye `referenceUrl` por subfase) y `responsibleUser` opcional (UC-023) |
| **404** | `PROCESS_NOT_FOUND` — ID inexistente o [CC] fuera de `programScope` |

### API-PROC-05 — `POST /processes/{processId}/phases`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-022 |
| **x-allowed-roles** | `[JD]`, `[TD]` |
| **Body** | `{ "name", "order", "description?" }` |
| **201** | Fase creada en proceso ACTIVE |
| **409** | `PROCESS_NOT_EDITABLE` |
| **Tool asistente** | `manage_process_phase` (JD, TD) — ver [`TOOL-CATALOG`](../design/assistant/TOOL-CATALOG.md) |

### API-PROC-06 — `PUT /processes/{processId}/phases/{phaseId}`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-022 |
| **x-allowed-roles** | `[JD]`, `[TD]` |
| **Body** | `{ "name?", "order?", "description?" }` |
| **200** | Fase actualizada |

### API-PROC-07 — `DELETE /processes/{processId}/phases/{phaseId}`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-022 |
| **x-allowed-roles** | `[JD]`, `[TD]` |
| **204** | Fase eliminada si subfases elegibles |
| **409** | `SUBPHASE_HAS_EVIDENCE` |

### API-PROC-08 — CRUD subfases bajo fase

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-022 |
| **Rutas** | `POST/PUT/DELETE /processes/{processId}/phases/{phaseId}/subphases[/{subphaseId}]` |
| **x-allowed-roles** | `[JD]`, `[TD]` |
| **Body subfase** | `{ "name", "order", "referenceUrl", "description?", "requirements" }` |
| **400** | `SUBPHASE_LINK_REQUIRED` (URL HTTPS o requisitos vacíos) |
| **409** | `SUBPHASE_HAS_EVIDENCE` / `PROCESS_NOT_EDITABLE` |

### API-SUB-01 — Evidencias y observaciones por subfase

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-004 / FSD-UC-022 |
| **Rutas** | `POST/GET /subphases/{subphaseId}/evidences`; `GET/POST /subphases/{subphaseId}/observations` |
| **POST evidencias** | multipart: `file`, `description`. Rol `[CC]` |
| **POST observaciones** | `{ "body": "texto" }`. Roles `[TD]`, `[JD]` |
| **201 evidencia** | `{ evidenceId, version, contentHash, event, currentState? }` |
| **200 listado** | evidencias u observaciones ordenadas por fecha descendente |
| **Observación** | `{ id, body, status: OPEN\|RESOLVED, resolvedAt?, resolvedVersionId? }` |
| **409 upload** | `SUBSANATION_NOT_ALLOWED` si hay observación OPEN pendiente |
| **409 observación** | `INVALID_STATE` si ya existe observación OPEN |

### API-SUB-02 — Subsanación de evidencia en subfase

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-006 |
| **Rutas** | `GET /subphases/{subphaseId}/subsanation-eligibility`; `POST /subphases/{subphaseId}/evidences/{evidenceId}/subsanate` |
| **GET elegibilidad** | `{ canSubsanate, openObservationId?, reason? }`. Rol `[CC]` |
| **POST subsanate** | multipart: `file`, `description`, `observationId`. Rol `[CC]` |
| **201** | `{ evidenceId, version, observationId, supersedesVersion, contentHash, event: "EvidenceSubsanated" }` |
| **409** | `SUBSANATION_NOT_ALLOWED` — sin observación OPEN, ya subsanada, o upload bloqueado |
| **Nota historial** | Versiones anteriores exponen `blobAvailable: false` en API-EVD-03 |

### API-SUB-03 — Rechazar subfase

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-008 |
| **Ruta** | `POST /api/v1/subphases/{subphaseId}/reject` |
| **x-allowed-roles** | `[TD]` |
| **Body** | `{ "justification": "texto mínimo 20 chars" }` |
| **Precondición** | ≥1 evidencia en subfase |
| **200** | `{ observationId, subphaseId, newState: "OBSERVADO" }` |
| **409** | `EVIDENCE_REQUIRED`, `INVALID_STATE` (observación OPEN) |
| **422** | `JUSTIFICATION_REQUIRED` |

### API-SUB-04 — Aprobar subfase

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-009 |
| **Ruta** | `POST /api/v1/subphases/{subphaseId}/approve` |
| **x-allowed-roles** | `[TD]` |
| **Precondición** | ≥1 evidencia; sin observación OPEN |
| **200** | `{ subphaseId, newState: "APROBADO" }` |
| **409** | `EVIDENCE_REQUIRED`, `SUBSANATION_NOT_ALLOWED` (observación OPEN), `INVALID_STATE` |

### API-PROC-09 — `PUT /processes/{processId}/responsible`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-023 |
| **x-allowed-roles** | `[JD]` |
| **Body** | `{ "userId": "uuid" }` — [CC] activo, misma carrera, sin otro proceso ACTIVE |
| **200** | Responsable asignado |
| **409** | `CC_ALREADY_ASSIGNED_TO_PROCESS` / `CAREER_SCOPE_MISMATCH` |

### API-PROC-10 — `DELETE /processes/{processId}/responsible`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-023 |
| **x-allowed-roles** | `[JD]` |
| **204** | Responsable removido; [CC] disponible para otro proceso |

### API-PROC-11 — `GET /processes/{processId}/responsible/candidates`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-023 |
| **x-allowed-roles** | `[JD]` |
| **200** | `[{ userId, fullName, email }]` — [CC] activos de la carrera del proceso sin otro proceso ACTIVE como responsable |
| **404** | `PROCESS_NOT_FOUND` |

---

## 4.1 MOD-TEMPLATE (plantillas normativas)

### API-TPL-01 — `GET /templates`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-021 |
| **x-allowed-roles** | `[JD]` |
| **Query** | `status?`, `type?` (`CEUB` \| `ARCU-SUR`) |
| **200** | `[{ id, name, description, type, status, phaseCount, subphaseCount }]` |

### API-TPL-02 — `POST /templates`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-021 |
| **x-allowed-roles** | `[JD]` |
| **Body** | `{ "name", "description?", "type", "phases": [{ "name", "order", "description?", "subphases": [{ "name", "order", "referenceUrl", "description?", "requirements" }] }] }` |
| **201** | Plantilla `DRAFT` creada |
| **400** | `TEMPLATE_SUBPHASE_LINK_REQUIRED` / `TEMPLATE_STRUCTURE_INCOMPLETE` |

### API-TPL-03 — `GET /templates/{templateId}`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-021 |
| **x-allowed-roles** | `[JD]` |
| **200** | Plantilla con árbol completo fases/subfases y enlaces |

### API-TPL-04 — `PUT /templates/{templateId}`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-021 |
| **x-allowed-roles** | `[JD]` |
| **Body** | Metadatos y/o árbol (misma forma que POST) |
| **200** | Plantilla actualizada (FSD-BR-21) |

### API-TPL-05 — `DELETE /templates/{templateId}`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-021 |
| **x-allowed-roles** | `[JD]` |
| **204** | Eliminación lógica o archivado |
| **409** | `TEMPLATE_IN_USE` — usar archivar |

### API-TPL-06 — `POST /templates/{templateId}/publish`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-021 |
| **x-allowed-roles** | `[JD]` |
| **200** | `status = PUBLISHED`; disponible en UC-003 |

### API-TPL-07 — `POST /templates/{templateId}/duplicate`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-021 |
| **x-allowed-roles** | `[JD]` |
| **201** | Copia `DRAFT` con misma estructura |

### API-TPL-08 — CRUD fases/subfases en plantilla `DRAFT`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-021 |
| **Rutas** | `POST/PUT/DELETE /templates/{templateId}/phases[/{phaseId}/subphases[/{subphaseId}]]` |
| **x-allowed-roles** | `[JD]` |
| **400** | `TEMPLATE_ORDER_CONFLICT` / `TEMPLATE_SUBPHASE_LINK_REQUIRED` |

---

## 5. MOD-EVIDENCE

> Evidencias **siempre** ligadas a subfase (`evidence.subphase_id`). Sin taxonomía Indicador/Criterio en v1.1.

### API-EVD-01 — `POST /api/v1/subphases/{subphaseId}/evidences`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-004 |
| **Alias** | API-SUB-01 (upload) |
| **x-allowed-roles** | `[CC]` |
| **Content-Type** | `multipart/form-data` |
| **Body** | `file`, `description` |
| **201** | `{ "evidenceId", "version": 1, "contentHash", "event": "EvidenceUploaded" }` |
| **400** | `EVIDENCE_UNCLASSIFIED` |
| **403** | `PROGRAM_SCOPE_DENIED` |
| **409** | `SUBSANATION_NOT_ALLOWED`, `UPLOAD_IN_PROGRESS` |
| **413** | `PAYLOAD_TOO_LARGE` |
| **422** | `INVALID_EVIDENCE_FORMAT` |

### API-EVD-LEGACY — `POST /api/v1/indicators/{indicatorId}/evidences` (deprecado)

| Campo | Valor |
|-------|-------|
| **Estado** | **Retirado** desde modelo v1.1 (2026-08-27) |
| **Sucesor** | `POST /api/v1/subphases/{subphaseId}/evidences` (API-EVD-01) |
| **x-allowed-roles** | `[CC]` (sigue protegido; respuesta siempre error) |
| **410** | `{ "error": "ENDPOINT_DEPRECATED", "message": "…", "indicatorId": "…" }` |
| **Headers** | `Deprecation: true`; `Link: </api/v1/subphases/{subphaseId}/evidences>; rel="successor-version"` |
| **Nota** | No acepta carga multipart; clientes Orval deben migrar a API-SUB-01 |

### API-EVD-02 — `GET /api/v1/evidences/search`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-007 |
| **x-allowed-roles** | `[CC]`, `[TD]`, `[JD]` |
| **Query** | `processId?`, `phaseId?`, `subphaseId?`, `programId?`, `q?`, `managementYear?`, `page=0`, `size=20` |
| **200** | `{ items: [{ evidenceId, subphaseId, subphaseName, phaseId, phaseName, processId, version, description, originalFilename, uploadedAt, uploadedBy, blobAvailable }], total, page, size }` |
| **403** | `PROGRAM_SCOPE_DENIED` ([CC] sin carrera) |
| **Nota FTS** | Flyway `V11__evidence_version_fts.sql`: GIN `search_vector`; dev/H2 → fallback LIKE |

### API-EVD-03 — `GET /evidences/{id}/versions`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-005 |
| **x-allowed-roles** | `[CC]`, `[TD]` |
| **200** | `[{ "versionId", "version", "supersedesVersion", "observationId", "description", "contentHash", "originalFilename", "createdAt", "createdBy", "current", "blobAvailable" }]` |

### API-EVD-04 — `DELETE /evidences/{id}`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-005 |
| **Nota** | Endpoint existe para auditoría; **siempre 409** si aprobado |
| **409** | `EVIDENCE_IMMUTABLE` + `AUDIT_DELETE_DENIED` en log |

### API-EVD-05 — Subsanación por subfase

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-006 |
| **Alias** | API-SUB-02 |
| **Rutas** | `GET /subphases/{subphaseId}/subsanation-eligibility`; `POST /subphases/{subphaseId}/evidences/{evidenceId}/subsanate` |
| **x-allowed-roles** | `[CC]` |
| **201** | `{ "version": n+1, "observationId", "supersedesVersion", "event": "EvidenceSubsanated" }` |

### API-IMP-01 — `POST /imports/evidences`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-018 |
| **x-allowed-roles** | `[CC]` |
| **Body** | `multipart` CSV |
| **200** | `{ "accepted": n, "rejected": [{ "row", "reason" }] }` |

---

## 6. MOD-WORKFLOW

> Workflow centrado en **Subfase**. Rechazo/aprobación vía API-SUB-03/04. Cierre de fase UC-010 cuando todas las subfases = APROBADO.

### API-WF-01 — `POST /subphases/{subphaseId}/reject`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-008 |
| **Alias** | API-SUB-03 |
| **x-allowed-roles** | `[TD]` |
| **Body** | `{ "justification": "texto mínimo 20 chars" }` |
| **Precondición** | ≥1 evidencia en subfase |
| **200** | `{ "observationId", "subphaseId", "newState": "OBSERVADO" }` |
| **409** | `EVIDENCE_REQUIRED`, `INVALID_STATE` |
| **422** | `JUSTIFICATION_REQUIRED` |

### API-WF-02 — `POST /subphases/{subphaseId}/approve`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-009 |
| **Alias** | API-SUB-04 |
| **x-allowed-roles** | `[TD]` |
| **Precondición** | ≥1 evidencia; sin observación OPEN |
| **200** | `{ "subphaseId", "newState": "APROBADO", "event": "SubphaseApproved" }` |
| **409** | `EVIDENCE_REQUIRED`, `SUBSANATION_NOT_ALLOWED`, `INVALID_STATE` |
| **403** | `FORBIDDEN_ROLE` si [CC] |

### API-WF-03 — Cierre de fase

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-010 |
| **Método / Ruta** | `POST /api/v1/processes/{processId}/phases/{phaseId}/complete` |
| **x-allowed-roles** | `[TD]` |
| **Precondición** | Todas las subfases de la fase en `APROBADO` |
| **200** | `{ "phaseId", "previousState", "newState": "COMPLETADA", "event": "PhaseCompleted" }` |
| **409** | `FASE_CIERRE_BLOQUEADO` + `pendingSubphases[]` |
| **403** | `FORBIDDEN_ROLE` si [CC] |

---

## 7. MOD-DASH

### API-DASH-01 — Suite Híbrida Compuesta Dashboard (`FSD-UC-011` / `DD-UC-011`)

#### API-DASH-01a — `GET /api/v1/dashboards/me/summary` (Composite PBAC Summary)
| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-011, FSD-UC-012, FSD-UC-013 (`DD-UC-011`) |
| **x-allowed-roles** | `[CC]`, `[TD]`, `[JD]` (Evaluación dinámica por permisos PBAC) |
| **Filtro Scope** | `academic_program_id` y autorizaciones extraídas del JWT |
| **200 OK** | `{ "userId", "grantedPermissions": [...], "coordinatorSection": {...}, "technicianSection": {...}, "executiveSection": {...} }` |


#### API-DASH-01b — `GET /api/v1/dashboards/coordinator/details`
| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-011 (`DD-UC-011`) |
| **x-allowed-roles** | `[CC]` |
| **Query Params** | `page` (default 0), `size` (default 10), `sort` (default `fechaLimite,asc`), `faseId`, `estado` |
| **200 OK** | Page JSON Object (`content`: listado de observaciones/subfases, `totalElements`, `totalPages`, etc.) |

#### API-DASH-01c — `GET /api/v1/dashboards/coordinator/export`
| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-011 (`DD-UC-011`) |
| **x-allowed-roles** | `[CC]` |
| **Query Params** | `format` (`xlsx` \| `csv` \| `pdf`), `faseId`, `estado` |
| **Headers** | `Content-Disposition: attachment; filename="reporte_dashboard_coordinator_{timestamp}.xlsx"` |
| **200 OK** | Binary File Stream (StreamingResponseBody) filtrado por rol y programa |


### API-DASH-02 — `GET /dashboard/technician`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-012 |
| **x-allowed-roles** | `[TD]` |
| **Query** | `programId`, `phaseId`, `status` |
| **200** | Bandeja de subfases pendientes de revisión |

### API-DASH-03 — `GET /dashboard/executive`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-013 |
| **x-allowed-roles** | `[JD]` |
| **200** | `{ "faculties": [{ "programs": [{ "semaphore": "RED|YELLOW|GREEN" }] }] }` |

---

## 8. MOD-REPORT · MOD-NOTIFY · MOD-PUBLIC · MOD-AUDIT

### API-REP-01 — `POST /api/v1/reports/executive/pdf`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-014 |
| **x-allowed-roles** | `[JD]` |
| **Body** | `{ "facultyId?", "programId?", "managementYear" }` |
| **202** | `{ "jobId" }` |
| **SLA** | P95 ≤ 5 min (NFR-003) |

### API-REP-02 — `GET /api/v1/reports/executive/pdf/{jobId}`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-014 |
| **x-allowed-roles** | `[JD]` (solo solicitante del job) |
| **200** | `{ "jobId", "status", "downloadUrl?", "errorCode?" }` |
| **404** | Job inexistente |

### API-REP-03 — `GET /api/v1/reports/executive/pdf/{jobId}/download`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-014 |
| **x-allowed-roles** | `[JD]` (solo solicitante; job `COMPLETED`) |
| **200** | `application/pdf` |
| **409** | `REPORT_NOT_READY` |

### API-NOTIF-01 — Outbox interno

| UC | FSD-UC-015 |
| Tipo | Eventos internos → worker SMTP; no expuesto a cliente |

### API-PUB-01 — `GET /public/programs/{slug}`

| UC | FSD-UC-016 |
| Auth | — |
| **200** | Solo `published=true` |
| **404** | Borradores no publicados |

### API-AUDIT-01 — `GET /audit/logs`

| UC | FSD-UC-017 |
| **x-allowed-roles** | `[JD]` |
| **Query** | `actorId`, `action`, `from`, `to` |
| **200** | Log paginado; export `Accept: text/csv` |

---

## 9. Matriz endpoint × rol (resumen)

| Endpoint | CC | TD | JD | P |
|----------|:--:|:--:|:--:|:--:|
| POST /subphases/{id}/evidences | ✓ | | | |
| POST /indicators/{id}/evidences (legacy) | ✓ | | | | **410 deprecado** |
| POST /subphases/{id}/reject | | ✓ | | |
| POST /subphases/{id}/approve | | ✓ | | |
| GET /dashboard/coordinator | ✓ | | | |
| GET /dashboard/technician | | ✓ | | |
| GET /dashboard/executive | | | ✓ | |
| POST /reports/executive/pdf | | | ✓ | |
| GET /public/programs/* | | | | ✓ |
| POST /admin/users | | | ✓ | |
| GET /admin/users | | | ✓ | |

---

## 10. Anti-patrones (no implementar)

| Anti-patrón | Alternativa |
|-------------|-------------|
| `DELETE /evidences/{id}` que borre aprobados | 409 + append-only |
| `PUT/PATCH /subphases/{id}` con `status` en body | `POST /reject`, `POST /approve` + observaciones/historial |
| [CC] en `/approve` | 403 estricto |
| Exponer observaciones internas en `/public/*` | Filtro `published` |

---

## Registro de cambios

| Versión | Fecha | Cambio |
|---------|-------|--------|
| v1.8 | 2026-08-27 | API-EVD-LEGACY: `POST /indicators/{id}/evidences` retorna **410 Gone**; sucesor API-EVD-01; Orval `DeprecatedEndpointResponseDto` |
| v1.7 | 2026-08-27 | Pivot v1.1: Proceso→Fase→Subfase→Evidencia; retiro Indicador/Criterio; WF-01/02 por subfase |
| v1.5 | 2026-08-03 | API-CAT-01: catálogo `programs` en BD + query `q`; FSD-UC-003 autocomplete carreras; plantillas proceso solo CEUB/ARCU-SUR |
| v1.4 | 2026-07-31 | API-USER-03: contrato formal `docs/product/api/API-USER-03.md`; GET `/admin/users`; tool `list_users` |
| v1.1 | 2026-06-23 | MOD-AUTH: campo `error` canónico; nota perímetro `UNAUTHORIZED`; rutas bajo `/api/v1` |
| Dorada v1.0 | 2026-05-16 | Catálogo API desde FSD §8; RBAC y errores de estado |
