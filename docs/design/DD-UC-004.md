---
id: DD-UC-004
fsd_ref: FSD-UC-004
titulo: "Diseño: Cargar Evidencia (MOD-EVIDENCE)"
modulo: MOD-EVIDENCE
arquitectura: Hexagonal
tecnologia: Java 21, Spring Boot 4.x
estado: Aprobado
autor: AI Architect (@feature-design-doc)
fecha: "2026-06-26"
fsd_uc:
  - FSD-UC-004
prd_refs:
  - PRD-US-005
  - PRD-US-025
prompts:
  - PR-IMPL-006
release: v1.0
---

# DD-UC-004: Cargar Evidencia (MOD-EVIDENCE)

## 1. Propósito

Permitir al **[CC]** cargar la **Evidencia v1** de un Indicador en estado `PENDIENTE`, validando clasificación (FSD-BR-01), alcance de carrera (FSD-BR-09), MIME/tamaño, SHA-256, transición `PENDIENTE → SUBIDO` vía `indicator_state_history`, y evento `EvidenceUploaded` (outbox UC-015).

---

## 2. Dominio

| Concepto | Descripción |
|----------|-------------|
| `IndicatorState` | `PENDIENTE`, `SUBIDO`, `OBSERVADO`, `SUBSANADO`, `APROBADO` |
| `Evidence` | Cabecera estable (`indicatorId`, `latestVersionId`) |
| `EvidenceVersion` | v1 append-only: `contentHash`, `criterionId`, `description`, `storageKey` |
| `IndicatorStateHistory` | Append-only; estado vigente = última fila |

### Excepciones

| Excepción | HTTP | Código |
|-----------|------|--------|
| `EvidenceUnclassifiedException` | 400 | `EVIDENCE_UNCLASSIFIED` |
| `InvalidEvidenceFormatException` | 422 | `INVALID_EVIDENCE_FORMAT` |
| `IndicatorNotFoundException` | 404 | `INDICATOR_NOT_FOUND` |
| `IndicatorNotUploadableException` | 409 | `INDICATOR_NOT_UPLOADABLE` |
| `ProgramScopeDeniedException` | 403 | `PROGRAM_SCOPE_DENIED` |
| `UploadInProgressException` | 409 | `UPLOAD_IN_PROGRESS` |
| `EvidencePayloadTooLargeException` | 413 | `PAYLOAD_TOO_LARGE` |

---

## 3. Puertos

### Inbound

* `UploadEvidenceUseCase`

### Outbound

* `IndicatorRepositoryPort` — findById, getCurrentState, appendStateHistory
* `EvidenceRepositoryPort` — saveEvidenceWithVersion, existsByIndicatorId
* `EvidenceBlobStoragePort` — store bytes, key by evidenceId/version
* `ContentHashPort` — SHA-256 hex
* `EvidenceUploadLockPort` — bloqueo doble envío (FSD-BR-18)
* `NotificationOutboxPort` — `EvidenceUploaded` (stub UC-015)
* `AuditLogPort` — log carga
* `UserProgramAssignmentRepositoryPort` — alcance [CC]

---

## 4. API

| Método | Ruta | Rol | Respuesta |
|--------|------|-----|-----------|
| POST | `/api/v1/indicators/{indicatorId}/evidences` | CC | **201** JSON |

**Content-Type:** `multipart/form-data`

| Part | Obligatorio |
|------|-------------|
| `file` | sí |
| `criterionId` | sí |
| `description` | sí |

**201 body:** `{ evidenceId, version, contentHash, event, currentState }`

---

## 5. Reglas de negocio

| ID | Implementación |
|----|----------------|
| FSD-BR-01 | Rechazar sin `criterionId`/`description`/archivo |
| FSD-BR-03 | Solo rol `CC` en SecurityConfig |
| FSD-BR-09 | `indicator.programId ∈ programScope` del [CC] |
| FSD-BR-18 | `EvidenceUploadLockPort` + frontend progress (US-025) |

**Transición:** solo desde `PENDIENTE` sin evidencia previa → `SUBIDO`.

---

## 6. Persistencia

| Tabla | Notas |
|-------|-------|
| `indicator` | `id`, `program_id`, `criterion_id`, `phase_id` |
| `indicator_state_history` | append-only |
| `evidence` | cabecera |
| `evidence_version` | v1, SHA-256, storage_key |

Blobs: `sigesa.evidence.storage-path` (default `./data/evidences`).

---

## 7. Plan de pruebas

* Unit: `UploadEvidenceService` (mock ports)
* Gherkin FSD-UC-004: carga OK, sin clasificación, scope denegado
