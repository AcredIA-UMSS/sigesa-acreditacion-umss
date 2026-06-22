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
| Errores | `{ "code": "ERROR_CODE", "message": "...", "details": {} }` |
| Paginación | `?page=&size=`; respuesta `{ "items": [], "total": n }` |
| Idempotencia | `Idempotency-Key` en POST críticos (carga, importación) |

### Códigos HTTP frecuentes

| Código | Uso |
|--------|-----|
| 401 | Sin sesión / token inválido |
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
      required: [code, message]
      properties:
        code:
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

### API-AUTH-01 — `POST /auth/login`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-001 |
| **Roles** | — (público) |
| **Body** | `{ "email": "user@umss.edu.bo", "password": "***" }` |
| **201** | `{ "accessToken", "expiresIn", "role", "programScope" }` |
| **401** | `AUTH_INVALID_CREDENTIALS` (mensaje genérico) |

### API-USER-01 — `POST /admin/users`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-002 |
| **x-allowed-roles** | `[JD]` |
| **Body** | `{ "email", "role", "programId?" }` |
| **201** | `{ "userId", "status": "INACTIVE" }` |
| **422** | `INVALID_EMAIL_DOMAIN` si no es `@umss.edu.bo` |

### API-USER-02 — `PATCH /admin/users/{id}/deactivate`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-002 |
| **x-allowed-roles** | `[JD]` |
| **200** | Usuario desactivado; historial conservado |

---

## 4. MOD-PROCESS

### API-PROC-01 — `POST /processes`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-003 |
| **x-allowed-roles** | `[JD]` |
| **Body** | `{ "programId", "templateId", "managementYear" }` |
| **201** | `{ "processId", "status": "EN_PROCESO" }` |
| **409** | `PROCESS_ALREADY_ACTIVE` |

### API-PROC-02 — `POST /templates/{templateId}/activate`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-003 |
| **x-allowed-roles** | `[JD]` |
| **Body** | `{ "effectiveFrom": "2026-01-01" }` |
| **200** | Plantilla activa para nuevos procesos |

---

## 5. MOD-EVIDENCE

### API-EVD-01 — `POST /indicators/{indicatorId}/evidences`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-004 |
| **x-allowed-roles** | `[CC]` |
| **Content-Type** | `multipart/form-data` |
| **Body** | `evidenceBlob`, `criterionId`, `description` |
| **Prohibido en body** | `status` / `estado` (Audit Service deriva estado desde evento) |
| **201** | `{ "evidenceId", "version": 1, "contentHash", "event": "EvidenceUploaded" }` |
| **400** | `EVIDENCE_UNCLASSIFIED` |
| **403** | `FORBIDDEN_ROLE` si [TD] sin delegación |

### API-EVD-02 — `GET /evidences/search`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-007 |
| **x-allowed-roles** | `[CC]`, `[TD]` |
| **Query** | `programId`, `phaseId`, `indicatorId`, `q`, `managementYear` |
| **200** | Lista paginada; [CC] solo su carrera (FSD-BR-09) |

### API-EVD-03 — `GET /evidences/{id}/versions`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-005 |
| **x-allowed-roles** | `[CC]`, `[TD]` |
| **200** | `[{ "version", "supersedesId", "observationId", "createdAt", "createdBy" }]` |

### API-EVD-04 — `DELETE /evidences/{id}`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-005 |
| **Nota** | Endpoint existe para auditoría; **siempre 409** si aprobado |
| **409** | `EVIDENCE_IMMUTABLE` + `AUDIT_DELETE_DENIED` en log |

### API-EVD-05 — `POST /evidences/{id}/versions`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-006 |
| **x-allowed-roles** | `[CC]` |
| **Body** | `evidenceBlob`, `observationId`, `description` |
| **201** | `{ "version": 2, "observationId", "supersedesVersion": 1, "event": "EvidenceSubsanated" }` |

### API-IMP-01 — `POST /imports/evidences`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-018 |
| **x-allowed-roles** | `[CC]` |
| **Body** | `multipart` CSV |
| **200** | `{ "accepted": n, "rejected": [{ "row", "reason" }] }` |

---

## 6. MOD-WORKFLOW

> Usar endpoints **semánticos**; no `PATCH /indicators/{id}` con `{ "status": "APROBADO" }`. Todo cambio de estado se persiste como `INSERT` en `indicator_state_history`.

### API-WF-01 — `POST /indicators/{id}/reject`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-008 |
| **x-allowed-roles** | `[TD]` |
| **Body** | `{ "justification": "texto mínimo 20 chars" }` |
| **200** | `{ "newState": "OBSERVADO", "observationId", "stateHistoryId" }` |
| **422** | `JUSTIFICATION_REQUIRED` |

### API-WF-02 — `POST /indicators/{id}/approve`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-009 |
| **x-allowed-roles** | `[TD]` |
| **200** | `{ "newState": "APROBADO", "stateHistoryId", "event": "IndicatorApproved" }` |
| **403** | `FORBIDDEN_ROLE` si [CC] |

### API-WF-03 — `IndicatorApproved` → SQS FIFO → Orchestration Service

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-010 |
| **x-allowed-roles** | sistema |
| **Entrada** | Evento `IndicatorApproved` con `phaseId` y `correlationId` |
| **Salida** | Evento `PhaseCompleted` solo si `COUNT(APROBADO) == COUNT(TOTAL)` |
| **Sin cierre** | No emite evento; pendientes consultables desde dashboard |

---

## 7. MOD-DASH

### API-DASH-01 — `GET /dashboard/coordinator`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-011 |
| **x-allowed-roles** | `[CC]` |
| **200** | `{ "programId", "phases": [...], "openObservations": [...] }` |

### API-DASH-02 — `GET /dashboard/technician`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-012 |
| **x-allowed-roles** | `[TD]` |
| **Query** | `programId`, `phaseId`, `status` |
| **200** | Bandeja de Indicadores pendientes de revisión |

### API-DASH-03 — `GET /dashboard/executive`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-013 |
| **x-allowed-roles** | `[JD]` |
| **200** | `{ "faculties": [{ "programs": [{ "semaphore": "RED|YELLOW|GREEN" }] }] }` |

---

## 8. MOD-REPORT · MOD-NOTIFY · MOD-PUBLIC · MOD-AUDIT

### API-REP-01 — `POST /reports/executive/pdf`

| UC | FSD-UC-014 |
| **x-allowed-roles** | `[JD]` |
| **Body** | `{ "facultyId?", "programId?", "managementYear" }` |
| **202** | `{ "jobId" }` o **200** con `application/pdf` si síncrono |
| **SLA** | P95 ≤ 5 min (NFR-003) |

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
| POST /indicators/{id}/evidences | ✓ | | | |
| POST .../reject | | ✓ | | |
| POST .../approve | | ✓ | | |
| Evento IndicatorApproved → SQS FIFO | | sistema | | |
| GET /dashboard/coordinator | ✓ | | | |
| GET /dashboard/technician | | ✓ | | |
| GET /dashboard/executive | | | ✓ | |
| POST /reports/executive/pdf | | | ✓ | |
| GET /public/programs/* | | | | ✓ |
| POST /admin/users | | | ✓ | |

---

## 10. Anti-patrones (no implementar)

| Anti-patrón | Alternativa |
|-------------|-------------|
| `DELETE /evidences/{id}` que borre aprobados | 409 + append-only |
| `PUT/PATCH /indicators/{id}` con `status` en body | `POST /approve`, `POST /reject` + `indicator_state_history` |
| [CC] en `/approve` | 403 estricto |
| Exponer observaciones internas en `/public/*` | Filtro `published` |

---

## Registro de cambios

| Versión | Fecha | Cambio |
|---------|-------|--------|
| Dorada v1.0 | 2026-05-16 | Catálogo API desde FSD §8; RBAC y errores de estado |
