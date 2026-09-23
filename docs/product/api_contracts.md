# Contratos API — SIGESA / AcredIA

## Control de versión

| Campo | Valor |
|-------|-------|
| **Versión** | v2.0 (jerarquía normativa multinivel) |
| **Release** | `2.0.0` |
| **Timestamp** | `2026-09-08T00:00:00-04:00` |
| **Fuente** | [`FSD.md`](FSD.md) · [`reglas_negocio.md`](reglas_negocio.md) · [`ADR-0004`](../adr/ADR-0004-normative-hierarchy-v2.md) |
| **OpenAPI (futuro)** | `docs/05_dti/openapi.yaml` (pendiente sync v2) |

> Contratos **lógicos** REST bajo `/api/v1`. El cliente **no** envía `status` de workflow en payloads; el backend deriva la máquina de estados del **Indicador**.  
> **Estado implementación (2026-09-08):** endpoints **v2.0** documentados como objetivo; código en `main` aún expone **legacy** Fase/Subfase (§12). Fase **M3** ADR-0004: coexistencia dual → retiro legacy.

---

## 1. Convenciones globales

| Aspecto | Valor |
|---------|-------|
| Base URL | `/api/v1` |
| Formato | `application/json` (salvo upload: `multipart/form-data`) |
| Auth | `Authorization: Bearer {token}` |
| Errores | `{ "error": "ERROR_CODE", "message": "...", "details": {} }` |
| Paginación | `?page=&size=`; respuesta `{ "items": [], "total": n, "page", "size" }` |
| Idempotencia | `Idempotency-Key` en POST críticos (carga, importación) |
| Nomenclatura UI | Alias CEUB/ARCU-SUR en respuestas: `label` por nodo (`Área`, `Dimensión`, …) |

### Códigos HTTP frecuentes

| Código | Uso |
|--------|-----|
| 401 | Sin sesión / token inválido |
| 403 | Rol o alcance insuficiente |
| 409 | Conflicto de estado (`EVIDENCE_IMMUTABLE`, `NIVEL1_CIERRE_BLOQUEADO`, `INDICATOR_HAS_EVIDENCE`, `PROCESS_ALREADY_ACTIVE`) |
| 410 | Endpoint legacy retirado (post-M5) |
| 422 | Validación (`JUSTIFICATION_REQUIRED`, `EVIDENCE_UNCLASSIFIED`, `INDICATOR_INCOMPLETE`) |

### Códigos de error v2.0 (dominio)

| Código | Sustituye legacy | Regla |
|--------|----------------|-------|
| `NIVEL1_CIERRE_BLOQUEADO` | `FASE_CIERRE_BLOQUEADO` | FSD-BR-07 |
| `INDICATOR_HAS_EVIDENCE` | `SUBPHASE_HAS_EVIDENCE` | FSD-BR-22 |
| `TEMPLATE_INDICATOR_LINK_REQUIRED` | `TEMPLATE_SUBPHASE_LINK_REQUIRED` | FSD-BR-24 |
| `TEMPLATE_INDICATOR_INCOMPLETE` | — | FSD-BR-24, BR-25 |
| `INDICATOR_INCOMPLETE` | `SUBPHASE_LINK_REQUIRED` | FSD-BR-25 |

---

## 2. Seguridad (OpenAPI fragment)

```yaml
openapi: 3.0.3
info:
  title: SIGESA API
  version: "2.0.0"
  description: Sistema de automatización de acreditación UMSS — jerarquía normativa CEUB/ARCU-SUR

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
        error: { type: string }
        message: { type: string }
        details: { type: object }

    IndicatorSummary:
      type: object
      required: [id, code, description, weight, order, status]
      properties:
        id: { type: string, format: uuid }
        code: { type: string }
        description: { type: string }
        weight: { type: number, minimum: 0 }
        order: { type: integer }
        referenceUrl: { type: string, format: uri }
        status: { enum: [PENDIENTE, SUBIDO, OBSERVADO, SUBSANADO, APROBADO] }

security:
  - bearerAuth: []
```

---

## 3. MOD-AUTH

> Sin cambios v2.0. Rutas relativas a `/api/v1`.

### API-AUTH-01 — `POST /auth/login`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-001 |
| **Roles** | — (público) |
| **Body** | `{ "email": "user@umss.edu.bo", "password": "***" }` |
| **200** | `{ "accessToken", "expiresIn", "role", "programScope" }` |
| **401** | `AUTH_INVALID_CREDENTIALS` |

### API-USER-01 — `POST /admin/users`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-002 |
| **x-allowed-roles** | `[JD]` |
| **Body** | `{ "email", "role", "programId?" }` |
| **201** | `{ "userId", "status": "INACTIVE" }` |
| **409** | `EMAIL_ALREADY_REGISTERED` |
| **422** | `INVALID_EMAIL_DOMAIN` |

### API-USER-02 — `PATCH /admin/users/{id}/deactivate`

| **UC** | FSD-UC-002 · **x-allowed-roles** | `[JD]` · **204** |

### API-USER-03 — `GET /admin/users`

> Contrato completo: [`api/API-USER-03.md`](api/API-USER-03.md)

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-002 |
| **x-allowed-roles** | `[JD]` |
| **Query** | `role?`, `status?` |
| **200** | `[{ "userId", "email", "role", "status", "programIds" }]` |

### API-CAT-01 — `GET /programs`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-002, FSD-UC-003 |
| **Query** | `q?` — autocomplete nombre/código |
| **200** | `[{ "id", "code", "name" }]` |

---

## 4. MOD-PROCESS (v2.0)

### API-PROC-01 — `POST /processes`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-003 |
| **x-allowed-roles** | `[JD]` |
| **Body** | `{ "career_id": "uuid", "template_id": "uuid" }` |
| **201** | Proceso creado; árbol **N1→N2→N3→Indicador** clonado (`ProcessResponseDto` v2) |
| **404** | `PROGRAM_NOT_FOUND` / `TEMPLATE_NOT_FOUND` |
| **409** | `PROCESS_ALREADY_ACTIVE` |
| **400** | `TEMPLATE_NOT_PUBLISHED` |

### API-PROC-02 — `POST /templates/{templateId}/activate`

| **UC** | FSD-UC-003 · **x-allowed-roles** | `[JD]` · **200** plantilla activa |

### API-PROC-03 — `GET /processes`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-019 |
| **x-allowed-roles** | `[JD]`, `[TD]`, `[CC]` |
| **200** | `[ProcessSummaryResponseDto]` — `evaluatorModel`, `level1Count`, `indicatorCount`, estado |
| **Filtrado [CC]** | Solo `career_id ∈ JWT.programScope` |

### API-PROC-04 — `GET /processes/{processId}`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-019 |
| **x-allowed-roles** | `[JD]`, `[TD]`, `[CC]` |
| **200** | `ProcessResponseDto` v2 — metadatos + `level1Nodes[]` anidado (N2→N3→`indicators[]`) + `responsibleUser?` |
| **404** | `PROCESS_NOT_FOUND` |

**Fragmento respuesta v2.0:**

```json
{
  "id": "uuid",
  "careerId": "uuid",
  "evaluatorModel": "CEUB",
  "templateName": "CEUB 2026",
  "status": "ACTIVE",
  "level1Nodes": [{
    "id": "uuid",
    "name": "Área académica",
    "label": "Área",
    "order": 1,
    "status": "ABIERTA",
    "level2Nodes": [{
      "name": "Variable docente",
      "level3Nodes": [{
        "name": "Sub-variable formación",
        "indicators": [{
          "id": "uuid",
          "code": "IND-01",
          "description": "Plan de formación",
          "weight": 0.15,
          "order": 1,
          "status": "PENDIENTE",
          "referenceUrl": "https://..."
        }]
      }]
    }]
  }]
}
```

### API-PROC-09…11 — Responsable [CC]

> Sin cambios v2.0. Ver legacy implementado: `PUT/DELETE /processes/{id}/responsible`, `GET …/candidates` (FSD-UC-023).

---

## 4.1 MOD-PROCESS — estructura normativa en proceso (v2.0)

> Sustituye API-PROC-05…08 legacy (fases/subfases). **UC:** FSD-UC-022 · **Roles:** `[JD]`, `[TD]`.

### API-STR-01 — Nivel 1

| Método | Ruta | Body | Respuesta |
|--------|------|------|-----------|
| POST | `/processes/{processId}/level1-nodes` | `{ name, order, description? }` | **201** N1 creado |
| PUT | `/processes/{processId}/level1-nodes/{level1Id}` | `{ name?, order?, description? }` | **200** |
| DELETE | `/processes/{processId}/level1-nodes/{level1Id}` | — | **204** o **409** `INDICATOR_HAS_EVIDENCE` |

### API-STR-02 — Nivel 2

| Método | Ruta | Body |
|--------|------|------|
| POST | `/level1-nodes/{level1Id}/level2-nodes` | `{ name, order, description? }` |
| PUT | `/level2-nodes/{level2Id}` | `{ name?, order?, description? }` |
| DELETE | `/level2-nodes/{level2Id}` | — |

### API-STR-03 — Nivel 3

| Método | Ruta | Body |
|--------|------|------|
| POST | `/level2-nodes/{level2Id}/level3-nodes` | `{ name, order, description? }` |
| PUT | `/level3-nodes/{level3Id}` | `{ name?, order?, description? }` |
| DELETE | `/level3-nodes/{level3Id}` | — |

### API-STR-04 — Indicador (estructura)

| Método | Ruta | Body |
|--------|------|------|
| POST | `/level3-nodes/{level3Id}/indicators` | `{ code, description, weight, order, referenceUrl }` |
| PUT | `/indicators/{indicatorId}` | campos parciales |
| DELETE | `/indicators/{indicatorId}` | — |

| Error | Condición |
|-------|-----------|
| `409 PROCESS_NOT_EDITABLE` | Proceso ≠ ACTIVE |
| `409 INDICATOR_HAS_EVIDENCE` | Workflow/evidencias iniciados |
| `400 PROCESS_STRUCTURE_ORDER_CONFLICT` | `order` duplicado |
| `400 INDICATOR_INCOMPLETE` | Falta `code`, `weight` o `referenceUrl` |

### API-STR-05 — Reordenar

| Método | Ruta | Body |
|--------|------|------|
| PUT | `/processes/{processId}/structure/reorder` | `{ nodes: [{ id, type, order, parentId? }] }` |

**Tools asistente (v2.0 objetivo):** `list_process_indicators`, `manage_process_level1` — ver [`TOOL-CATALOG`](../design/assistant/TOOL-CATALOG.md).

---

## 4.2 MOD-TEMPLATE — plantillas normativas (v2.0)

> **UC:** FSD-UC-021 · **Roles:** `[JD]`

### API-TPL-01 — `GET /templates`

| Query | `status?`, `evaluatorModel?` (`CEUB` \| `ARCU-SUR`) |
| **200** | `[{ id, name, evaluatorModel, status, level1Count, indicatorCount }]` |

### API-TPL-02 — `POST /templates`

| Body | `{ name, description?, evaluatorModel, level1Nodes: [{ name, order, level2Nodes: [{ … level3Nodes: [{ … indicators: [{ code, description, weight, order, referenceUrl }] }] }] }] }` |
| **201** | `DRAFT` |
| **400** | `TEMPLATE_INDICATOR_LINK_REQUIRED`, `TEMPLATE_STRUCTURE_INCOMPLETE`, `TEMPLATE_INDICATOR_INCOMPLETE` |

### API-TPL-03 — `GET /templates/{templateId}`

| **200** | Árbol completo N1→N2→N3→Indicador |

### API-TPL-04 — `PUT /templates/{templateId}`

Metadatos y/o árbol (misma forma que POST). FSD-BR-21: no migra procesos ACTIVE.

### API-TPL-05 — `DELETE /templates/{templateId}`

| **204** | Archivar · **409** `TEMPLATE_IN_USE` |

### API-TPL-06 — `POST /templates/{templateId}/publish`

| **200** | `PUBLISHED` — exige ≥1 indicador válido (FSD-BR-24) |

### API-TPL-07 — `POST /templates/{templateId}/duplicate`

| **201** | Copia `DRAFT` |

### API-TPL-08 — CRUD granular en plantilla `DRAFT`

Rutas análogas a API-STR-01…04 bajo prefijo `/templates/{templateId}/…`.

---

## 5. MOD-EVIDENCE (v2.0)

> Evidencias **siempre** ligadas a **Indicador** (`evidence.indicator_id` NOT NULL). FSD-BR-01.

### API-EVD-01 — `POST /indicators/{indicatorId}/evidences`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-004 |
| **x-allowed-roles** | `[CC]` |
| **Content-Type** | `multipart/form-data` |
| **Body** | `file?`, `externalUrl?`, `description` (al menos file o externalUrl) |
| **201** | `{ evidenceId, version: 1, contentHash?, event: "EvidenceUploaded", indicatorState: "SUBIDO" }` |
| **400** | `EVIDENCE_UNCLASSIFIED` |
| **403** | `PROGRAM_SCOPE_DENIED` |
| **409** | `SUBSANATION_NOT_ALLOWED` |

### API-EVD-02 — `GET /evidences/search`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-007 |
| **x-allowed-roles** | `[CC]`, `[TD]`, `[JD]` |
| **Query** | `processId?`, `level1Id?`, `level3Id?`, `indicatorId?`, `programId?`, `q?`, `page`, `size` |
| **200** | `{ items: [{ evidenceId, indicatorId, indicatorCode, normativePath[], processId, version, description, … }], total, page, size }` |

### API-EVD-03 — `GET /evidences/{id}/versions`

| **UC** | FSD-UC-005 · **200** historial append-only |

### API-EVD-04 — `DELETE /evidences/{id}`

| **UC** | FSD-UC-005 · **409** `EVIDENCE_IMMUTABLE` siempre si aprobado |

### API-EVD-05 — Subsanación por indicador

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-006 |
| **Rutas** | `GET /indicators/{indicatorId}/subsanation-eligibility`; `POST /indicators/{indicatorId}/evidences/{evidenceId}/subsanate` |
| **x-allowed-roles** | `[CC]` |
| **POST** | multipart: `file?`, `externalUrl?`, `description`, `observationId` |
| **201** | `{ version, observationId, supersedesVersion, event: "EvidenceSubsanated" }` |

### API-IND-OBS-01 — Observaciones por indicador

| Método | Ruta | Rol | Body |
|--------|------|-----|------|
| GET | `/indicators/{indicatorId}/observations` | TD, JD, CC (lectura) | — |
| POST | `/indicators/{indicatorId}/observations` | TD, JD | `{ body }` (rechazo formal vía API-WF-01 preferido) |

---

## 6. MOD-WORKFLOW (v2.0)

> Workflow centrado en **Indicador**. Cierre agregado en **Nivel 1**.

### API-WF-01 — `POST /indicators/{indicatorId}/reject`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-008 |
| **x-allowed-roles** | `[TD]` |
| **Body** | `{ "justification": "≥20 chars" }` |
| **200** | `{ observationId, indicatorId, newState: "OBSERVADO" }` |
| **409** | `EVIDENCE_REQUIRED`, `INVALID_STATE` |
| **422** | `JUSTIFICATION_REQUIRED` |

### API-WF-02 — `POST /indicators/{indicatorId}/approve`

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-009 |
| **x-allowed-roles** | `[TD]` |
| **200** | `{ indicatorId, newState: "APROBADO", event: "IndicatorApproved" }` |
| **409** | `EVIDENCE_REQUIRED`, `SUBSANATION_NOT_ALLOWED`, `INVALID_STATE` |

### API-WF-03 — Cierre de Nivel 1

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-010 |
| **Ruta** | `POST /processes/{processId}/level1-nodes/{level1Id}/complete` |
| **x-allowed-roles** | `[TD]` |
| **Precondición** | Todos los indicadores del subárbol = `APROBADO` |
| **200** | `{ level1Id, previousState, newState: "COMPLETADA", event: "Level1Completed" }` |
| **409** | `NIVEL1_CIERRE_BLOQUEADO` + `pendingIndicators[]` |

### API-WF-04 — Timeline etapas metodológicas (M6 / v2.1)

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-025 |
| **Ruta** | `GET /processes/{processId}/stages` |
| **x-allowed-roles** | `[CC, TD, JD]` |
| **200** | `MethodologicalStageResponseDto[]` (7 etapas + entregables E1–E2) |

### API-WF-05 — Enviar etapa a revisión

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-026 |
| **Ruta** | `POST /processes/{processId}/stages/{stageId}/submit` |
| **x-allowed-roles** | `[CC]` |
| **200** | `{ status: "SUBMITTED_FOR_REVIEW" }` |
| **409** | `INVALID_STATE` |

### API-WF-06 — Aprobar / observar etapa

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-027 |
| **Rutas** | `POST …/approve` · `POST …/observe` |
| **x-allowed-roles** | `[TD, JD]` |
| **409 approve** | `STAGE_GATE_BLOCKED` + `failedRules[]` |
| **409 observe** | `INVALID_STATE` |

### API-WF-07 — Preview compuerta

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-028 |
| **Ruta** | `GET /processes/{processId}/stages/{stageId}/gate` |
| **x-allowed-roles** | `[TD]` |
| **200** | `{ pass, failedRules[], summary }` |

### API-WF-08 — Aprobar entregable

| Campo | Valor |
|-------|-------|
| **UC** | FSD-UC-025 ext. |
| **Ruta** | `POST /processes/{processId}/stages/{stageId}/deliverables/{deliverableId}/approve` |
| **x-allowed-roles** | `[TD]` |

---

## 7. MOD-DASH

### API-DASH-01 — Suite Híbrida PBAC

#### API-DASH-01a — `GET /dashboards/me/summary`

| **UC** | FSD-UC-011, 012, 013 · PBAC por JWT |

#### API-DASH-01b — `GET /dashboards/coordinator/details`

| Query v2.0 | `level1Id?`, `indicatorStatus?`, `page`, `size`, `sort` |
| **200** | Observaciones/indicadores del [CC] |

#### API-DASH-01c — `GET /dashboards/coordinator/export`

| Query v2.0 | `format`, `level1Id?`, `indicatorStatus?` |

### API-DASH-02 — Bandeja [TD]

| Query v2.0 | `programId`, `level1Id`, `indicatorStatus` |
| **200** | Indicadores pendientes de revisión |

### API-DASH-03 — Semáforo [JD]

| **200** | `{ faculties: [{ programs: [{ semaphore, indicatorCompletionPct }] }] }` |

---

## 8. MOD-REPORT · MOD-NOTIFY · MOD-PUBLIC · MOD-AUDIT

> Sin cambios de rutas v2.0. Reportes PDF consumirán agregados por **Indicador** / Nivel 1 en v2.1.

| ID | Resumen |
|----|---------|
| API-REP-01…03 | PDF ejecutivo [JD] — FSD-UC-014 |
| API-NOTIF-01 | Outbox interno — FSD-UC-015 |
| API-PUB-01 | Portal público — FSD-UC-016 |
| API-AUDIT-01 | Bitácora [JD] — FSD-UC-017 |
| API-IMP-01 | `POST /imports/evidences` — filas con `indicatorCode` — FSD-UC-018 |

---

## 9. Matriz endpoint × rol (v2.0 objetivo)

| Endpoint | CC | TD | JD | EE | P |
|----------|:--:|:--:|:--:|:--:|:--:|
| POST /indicators/{id}/evidences | ✓ | | | | |
| POST /indicators/{id}/reject | | ✓ | | | |
| POST /indicators/{id}/approve | | ✓ | | | |
| POST /level1-nodes/{id}/complete | | ✓ | | | |
| GET /dashboards/coordinator/* | ✓ | | | ✓* | |
| GET /dashboards/me/summary (TD) | | ✓ | | | |
| POST /templates | | | ✓ | | |
| POST /processes | | | ✓ | | |
| POST /admin/users | | | ✓ | | |
| GET /public/programs/* | | | | | ✓ |

\* [EE] solo lectura; sin mutaciones (FSD-BR-19).

---

## 10. Anti-patrones (no implementar)

| Anti-patrón | Alternativa |
|-------------|-------------|
| `DELETE /evidences/{id}` que borre aprobados | 409 + append-only |
| `PATCH /indicators/{id}` con `status` en body | `POST /reject`, `POST /approve` |
| Evidencia sin `indicatorId` | 400 `EVIDENCE_UNCLASSIFIED` |
| Cierre de N2/N3 directamente | Solo cierre **Nivel 1** agregado |
| [CC] en `/approve` | 403 estricto |

---

## 11. MOD-ASSISTANT (referencia)

Sin cambio de rutas (`/assistant/status`, `/assistant/chat`). Tools v2.0 objetivo: referencias a **indicador** y ruta normativa — migración en PR post-M3.

---

## 12. Legacy v1.x — Fase/Subfase *(implementado en `main`; deprecación M3–M5)*

> **No usar en clientes nuevos.** Mantener hasta migración Orval/UI. Sucesor: §4–§6 v2.0.

| ID legacy | Ruta implementada | Sucesor v2.0 |
|-----------|-------------------|--------------|
| API-PROC-05…08 | `/processes/{id}/phases`, `/subphases` | API-STR-01…05 |
| API-SUB-01 | `/subphases/{id}/evidences`, `/observations` | API-EVD-01, API-IND-OBS-01 |
| API-SUB-02 | `/subphases/{id}/subsanate` | API-EVD-05 |
| API-SUB-03/04 | `/subphases/{id}/reject\|approve` | API-WF-01/02 |
| API-WF-03 legacy | `/phases/{id}/complete` | API-WF-03 |
| API-TPL-01…08 legacy | plantillas `phases/subphases` | API-TPL-01…08 v2 |
| API-EVD-LEGACY | `POST /indicators/{id}/evidences` → **410** (v1.1) | **Revive** como API-EVD-01 en v2.0 |

### API-EVD-LEGACY — estado transitorio

| Campo | Valor |
|-------|-------|
| **Estado 2026-09** | `POST /indicators/{id}/evidences` retorna **410 Gone** (decisión v1.1) |
| **v2.0** | Mismo path **reactivado** como contrato canónico (API-EVD-01) tras migración M3 |
| **Headers deprecación** | Retirar `Deprecation: true` al activar v2 |

---

## Registro de cambios

| Versión | Fecha | Cambio |
|---------|-------|--------|
| v2.0 | 2026-09-08 | Release 2.0.0: contratos N1→N2→N3→Indicador→Evidencia; API-STR/WF/EVD/TPL v2; §12 legacy; ADR-0004 |
| v1.8 | 2026-08-27 | API-EVD-LEGACY 410 Gone; pivot subfase |
| v1.5 | 2026-08-03 | API-CAT-01 programs |
| Dorada v1.0 | 2026-05-16 | Catálogo API desde FSD §8 |
