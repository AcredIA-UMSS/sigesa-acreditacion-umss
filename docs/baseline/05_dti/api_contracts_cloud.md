---
producto: SIGESA / AcredIA
version: Dorada v1.0 (borrador compilado)
timestamp: 2026-05-25T17:48:00-04:00
contrato: "[PC-SIG-14] Arquitecto Cloud Distribuido"
alcance: Evidence Service · Audit Service (endpoints criticos)
estado: En revisión — primera versión integrada
---

# Contratos de API — Evidence Service y Audit Service

## Control de versión

| Campo | Valor |
|-------|-------|
| **Versión** | Dorada v1.0 |
| **Timestamp** | `2026-05-25T17:48:00-04:00` |
| **Contrato origen** | [PC-SIG-14] Arquitecto Cloud Distribuido |
| **Arquitectura base** | [`hybrid_architecture.md`](hybrid_architecture.md) §2.2, §2.3 |
| **ADRs relacionados** | [`ADR_010`](adrs/ADR_010_event_driven_choreography.md) · [`ADR_011`](adrs/ADR_011_sqs_fifo_phase_closure.md) · [`ADR_012`](adrs/ADR_012_ddl_indicator_state_history.md) |
| **Estado** | En revisión |

> **Regla de oro:** si un endpoint no aparece en este documento o en [`docs/04_fsd/api_contracts.md`](../04_fsd/api_contracts.md), no existe para implementación v1.0.

---

## 1. Principios de diseño de la API

**RBAC obligatorio en todos los endpoints.** Cada endpoint declara el rol minimo requerido. API Gateway rechaza con `403 Forbidden` antes de llegar al servicio si el token JWT no contiene el rol adecuado. Roles validos segun glosario: `ProgramCoordinator` [CC], `DueaTechnician` [TD], `DueaAdministrator` [JD].

**Respuestas de error estandar.** Todos los errores siguen el esquema:

```
{
  "error": {
    "code": "EVIDENCE_UPLOAD_NOT_ALLOWED",
    "message": "El Indicator 1.2 no esta en estado PENDIENTE ni OBSERVADO.",
    "indicatorId": "uuid",
    "currentState": "APROBADO"
  }
}
```

**Idempotencia via cabecera `Idempotency-Key`.** Los endpoints de escritura (`POST`) aceptan la cabecera opcional `Idempotency-Key: <uuid>`. Si el servidor recibe una segunda solicitud con la misma clave dentro de 24h, retorna la respuesta original almacenada en cache sin volver a procesar.

---

## 2. Evidence Service

### 2.1 Cargar Evidence inicial o subsanacion

Sube una nueva Evidence normativa y la vincula a un Indicator. Si el estado derivado del Indicator es OBSERVADO, el campo `observationId` es obligatorio (subsanacion). Si es PENDIENTE, `observationId` es nulo (carga inicial).

```
POST /api/v1/indicators/{indicatorId}/evidences
```

**Autorizacion:** `ProgramCoordinator` [CC]

**Path parameters:**

| Parametro | Tipo | Descripcion |
|-----------|------|-------------|
| `indicatorId` | UUID | Identificador del Indicator destino |

**Request body (multipart/form-data):**

| Campo | Tipo | Requerido | Descripcion |
|-------|------|-----------|-------------|
| `evidenceBlob` | binary | Si | Blob de Evidence normativa (PDF, DOCX) |
| `observationId` | UUID | Condicional | Requerido si Indicator.state == OBSERVADO |
| `note` | string | No | Nota descriptiva del [CC] (max 500 chars) |

**Respuestas:**

| Codigo | Condicion | Cuerpo |
|--------|-----------|--------|
| `201 Created` | Evidence insertada, evento publicado | `{ "evidenceId": "uuid", "version": 2, "s3Key": "evidence/1.2/v2.pdf" }` |
| `400 Bad Request` | observationId ausente cuando estado es OBSERVADO | `{ "error": { "code": "OBSERVATION_ID_REQUIRED", ... } }` |
| `403 Forbidden` | Actor no tiene rol ProgramCoordinator | — |
| `409 Conflict` | Indicator.state no es PENDIENTE ni OBSERVADO | `{ "error": { "code": "EVIDENCE_UPLOAD_NOT_ALLOWED", "currentState": "APROBADO" } }` |
| `413 Payload Too Large` | Evidence supera limite institucional (50 MB) | `{ "error": { "code": "EVIDENCE_TOO_LARGE", ... } }` |

**Postcondicion:** Evidence insertada en RDS (Append-Only, `version = n+1`), blob en S3, evento `EvidenceUploaded` publicado en EventBridge.

**Invariante:** Este endpoint nunca ejecuta `UPDATE` ni `DELETE` en ninguna tabla. La transicion de estado del Indicator la realiza Audit Service al consumir el evento.

---

### 2.2 Obtener listado de versiones de Evidence

Retorna todas las versiones de Evidence de un Indicator, ordenadas de mas reciente a mas antigua, con metadatos de auditoria.

```
GET /api/v1/indicators/{indicatorId}/evidences
```

**Autorizacion:** `ProgramCoordinator` [CC] (solo su carrera) · `DueaTechnician` [TD] · `DueaAdministrator` [JD]

**Query parameters:**

| Parametro | Tipo | Default | Descripcion |
|-----------|------|---------|-------------|
| `page` | int | 1 | Pagina |
| `pageSize` | int | 20 | Registros por pagina (max 100) |

**Respuesta 200:**

```json
{
  "indicatorId": "uuid",
  "evidences": [
    {
      "evidenceId": "uuid",
      "version": 2,
      "supersedesId": "uuid-v1",
      "observationId": "uuid-obs-405",
      "s3PresignedUrl": "https://s3.../evidence/1.2/v2.pdf?X-Amz-Expires=900",
      "contentSha256": "abc123...",
      "createdByRole": "ProgramCoordinator",
      "createdAt": "2026-05-25T16:00:00-04:00"
    }
  ],
  "total": 2,
  "page": 1
}
```

**Nota:** La URL prefirmada de S3 tiene vigencia de 15 minutos (`X-Amz-Expires=900`). No se expone el `s3Key` directo al cliente.

---

### 2.3 Descargar una version especifica de Evidence

```
GET /api/v1/indicators/{indicatorId}/evidences/{evidenceId}
```

**Autorizacion:** `ProgramCoordinator` [CC] (solo su carrera) · `DueaTechnician` [TD] · `DueaAdministrator` [JD]

**Respuestas:**

| Codigo | Condicion | Cuerpo |
|--------|-----------|--------|
| `200 OK` | Evidence encontrada | `{ "evidenceId": "uuid", "version": 2, "s3PresignedUrl": "...", "contentSha256": "..." }` |
| `404 Not Found` | evidenceId no existe o no pertenece al indicatorId | `{ "error": { "code": "EVIDENCE_NOT_FOUND" } }` |
| `403 Forbidden` | [CC] intenta acceder a Evidence de otra carrera | — |

---

## 3. Audit Service

### 3.1 Aprobar un Indicator

El [TD] aprueba el Indicator. Solo es valido si el estado actual es `SUBIDO` o `SUBSANADO`. Genera una transicion a `APROBADO` en `indicator_state_history` y publica `IndicatorApproved`.

```
POST /api/v1/indicators/{indicatorId}/approve
```

**Autorizacion:** `DueaTechnician` [TD] exclusivamente. Este endpoint retorna `403` para cualquier otro rol, incluido `DueaAdministrator`.

**Path parameters:**

| Parametro | Tipo | Descripcion |
|-----------|------|-------------|
| `indicatorId` | UUID | Identifier del Indicator a aprobar |

**Request body (application/json):**

| Campo | Tipo | Requerido | Descripcion |
|-------|------|-----------|-------------|
| `justification` | string | No | Nota de aprobacion del [TD] (max 500 chars) |

**Respuestas:**

| Codigo | Condicion | Cuerpo |
|--------|-----------|--------|
| `200 OK` | Transicion exitosa | `{ "indicatorId": "uuid", "previousState": "SUBSANADO", "newState": "APROBADO", "stateHistoryId": "uuid" }` |
| `403 Forbidden` | Actor no tiene rol DueaTechnician | — |
| `409 Conflict` | Estado actual no es SUBIDO ni SUBSANADO | `{ "error": { "code": "ILLEGAL_STATE_TRANSITION", "currentState": "PENDIENTE", "attemptedTransition": "APROBADO" } }` |
| `404 Not Found` | indicatorId no existe | — |

**Postcondicion:** INSERT en `indicator_state_history` (previous=SUBIDO|SUBSANADO, new=APROBADO). Evento `IndicatorApproved` publicado. Orchestration Service evaluara cierre de Phase de forma asincrona.

**Invariante:** Este endpoint nunca ejecuta una actualización destructiva de estado del Indicator. El estado actual se lee de `indicator_current_view`.

---

### 3.2 Rechazar un Indicator y crear Observation

El [TD] rechaza el Indicator y registra una Observation obligatoria. Solo valido si el estado actual es `SUBIDO` o `SUBSANADO`. Genera transicion a `OBSERVADO` y crea un registro en `observation`.

```
POST /api/v1/indicators/{indicatorId}/reject
```

**Autorizacion:** `DueaTechnician` [TD] exclusivamente.

**Request body (application/json):**

| Campo | Tipo | Requerido | Descripcion |
|-------|------|-----------|-------------|
| `reason` | string | Si | Descripcion del defecto normativo (min 20, max 1000 chars) |
| `evidenceId` | UUID | Si | Version de Evidence que origina el rechazo |
| `linkedObservationId` | UUID | No | Observation previa que se reitera (iteracion de subsanacion) |

**Respuestas:**

| Codigo | Condicion | Cuerpo |
|--------|-----------|--------|
| `201 Created` | Rechazo registrado | `{ "observationId": "uuid", "indicatorId": "uuid", "newState": "OBSERVADO", "stateHistoryId": "uuid" }` |
| `400 Bad Request` | `reason` ausente o muy corto | `{ "error": { "code": "REASON_REQUIRED", "minLength": 20 } }` |
| `403 Forbidden` | Actor no tiene rol DueaTechnician | — |
| `409 Conflict` | Estado actual no es SUBIDO ni SUBSANADO | `{ "error": { "code": "ILLEGAL_STATE_TRANSITION", "currentState": "PENDIENTE" } }` |

**Postcondicion:** INSERT en `observation`. INSERT en `indicator_state_history` (previous=SUBIDO|SUBSANADO, new=OBSERVADO). Evento `IndicatorObserved` publicado. Notification Service alertara al [CC].

---

### 3.3 Consultar historial de estados de un Indicator

Retorna la secuencia completa de transiciones de estado, con actor y timestamp. Util para auditoria normativa.

```
GET /api/v1/indicators/{indicatorId}/state-history
```

**Autorizacion:** `DueaTechnician` [TD] · `DueaAdministrator` [JD]

**Respuesta 200:**

```json
{
  "indicatorId": "uuid",
  "currentState": "APROBADO",
  "history": [
    {
      "stateHistoryId": "uuid",
      "previousState": "SUBSANADO",
      "newState": "APROBADO",
      "createdByRole": "DueaTechnician",
      "createdAt": "2026-05-25T17:00:00-04:00",
      "correlationId": "uuid"
    },
    {
      "stateHistoryId": "uuid",
      "previousState": "OBSERVADO",
      "newState": "SUBSANADO",
      "createdByRole": "ProgramCoordinator",
      "createdAt": "2026-05-25T14:00:00-04:00",
      "correlationId": "uuid"
    }
  ]
}
```

---

### 3.4 Consultar Observations de un Indicator

```
GET /api/v1/indicators/{indicatorId}/observations
```

**Autorizacion:** `ProgramCoordinator` [CC] (solo su carrera) · `DueaTechnician` [TD] · `DueaAdministrator` [JD]

**Respuesta 200:**

```json
{
  "indicatorId": "uuid",
  "observations": [
    {
      "observationId": "obs-405",
      "evidenceId": "ev-001",
      "reason": "Falta la firma del Decano en la Resolución Facultativa 12/2025",
      "linkedObservationId": null,
      "createdByRole": "DueaTechnician",
      "createdAt": "2026-05-24T10:00:00-04:00"
    }
  ]
}
```

---

## 4. Codigos de error estandar

| Codigo | HTTP | Descripcion |
|--------|------|-------------|
| `EVIDENCE_UPLOAD_NOT_ALLOWED` | 409 | Indicator no esta en estado PENDIENTE ni OBSERVADO |
| `OBSERVATION_ID_REQUIRED` | 400 | Indicator en OBSERVADO pero no se provee observationId |
| `ILLEGAL_STATE_TRANSITION` | 409 | La transicion solicitada no existe en la maquina de estados |
| `EVIDENCE_IMMUTABLE` | 409 | Intento de borrado o reemplazo de Evidence aprobada (ADR_001) |
| `EVIDENCE_NOT_FOUND` | 404 | evidenceId no existe o no pertenece al indicatorId |
| `REASON_REQUIRED` | 400 | El campo reason del rechazo es obligatorio y tiene longitud minima |
| `EVIDENCE_TOO_LARGE` | 413 | Evidence supera limite de 50 MB |
| `PHASE_NOT_CLOSEABLE` | 409 | Se intenta cerrar una Phase con Indicators no APROBADO (Hard Constraint) |

---

## 5. Referencias

- [`docs/05_dti/hybrid_architecture.md`](hybrid_architecture.md) §2.2 Evidence Service, §2.3 Audit Service
- [`docs/05_dti/adrs/ADR_010_event_driven_choreography.md`](adrs/ADR_010_event_driven_choreography.md)
- [`docs/05_dti/adrs/ADR_012_ddl_indicator_state_history.md`](adrs/ADR_012_ddl_indicator_state_history.md)
- [`docs/04_fsd/api_contracts.md`](../04_fsd/api_contracts.md) (contratos funcionales cloud v1.0)
- [`api_contracts_mvp_runtime.md`](api_contracts_mvp_runtime.md) (gateway, auth, dashboard, Mode A local)
- [`context/03_domain_glossary.md`](../../context/03_domain_glossary.md) (mapeo de terminos ES → EN)
- [`team/alexAlvarez/docs/context/04_state_machine.md`](../../team/alexAlvarez/docs/context/04_state_machine.md) (transiciones validas)
