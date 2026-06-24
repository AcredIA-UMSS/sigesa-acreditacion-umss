# Modelo de datos funcional — SIGESA / AcredIA

## Control de versión

| Campo | Valor |
|-------|-------|
| **Diagrama ER (fuente)** | [`07_diagramas/er-002-modelo-funcional.mmd`](07_diagramas/er-002-modelo-funcional.mmd) |
| **Versión** | Dorada v1.0 |
| **Timestamp** | `2026-05-16T18:30:00-04:00` |
| **Vista** | Lógica / de dominio (FSD) |
| **Modelo físico (DTI)** | [`docs/05_dti/modelo_datos.md`](../05_dti/modelo_datos.md) |
| **DDL** | [`docs/05_dti/ddl_sigesa_append_only.sql`](../05_dti/ddl_sigesa_append_only.sql) |
| **Glosario** | [`glosario.md`](glosario.md) |

> Este documento describe entidades, relaciones y validaciones desde la **especificación funcional**. La implementación PostgreSQL append-only está en el DTI.

---

## 1. Principios

| Principio | Regla funcional |
|-----------|-----------------|
| Append-only | Sin borrado físico de Evidencia aprobada; subsanación = nueva `EvidenceVersion` |
| Trazabilidad | `version`, `supersedesVersion`, `observationId`, `createdBy`, `createdAt` |
| Taxonomía | CEUB/ARCU-SUR: Fase → Dimensión → Criterio → Indicador → Evidencia |
| Aislamiento [CC] | Datos acotados a `academicProgramId` del coordinador |
| Un Proceso activo | Por carrera + modalidad + periodo (FSD-BR-08) |

---

## 2. Diagrama ER lógico

```mermaid
erDiagram
  FACULTY ||--o{ ACADEMIC_PROGRAM : has
  ACADEMIC_PROGRAM ||--o{ ACCREDITATION_PROCESS : runs
  ACCREDITATION_TEMPLATE ||--o{ TEMPLATE_PHASE : defines
  ACCREDITATION_PROCESS ||--o{ PHASE : contains
  EVALUATION_DIMENSION ||--o{ EVALUATION_CRITERION : groups
  EVALUATION_CRITERION ||--o{ INDICATOR_CATALOG : defines
  TEMPLATE_PHASE ||--o{ INDICATOR_CATALOG : scopes
  PHASE ||--o{ INDICATOR : instantiates
  INDICATOR ||--o{ EVIDENCE : proves
  EVIDENCE ||--o{ EVIDENCE_VERSION : versions
  INDICATOR ||--o{ OBSERVATION : may_have
  OBSERVATION ||--o| EVIDENCE_VERSION : triggers_subsanation
  APP_USER ||--o{ USER_PROGRAM_ASSIGNMENT : assigned
  APP_USER ||--o{ AUDIT_LOG : performs
  APP_USER ||--o{ STATE_TRANSITION : triggers
```

---

## 3. Entidades y atributos core

### 3.1 Maestros institucionales

| Entidad (EN) | ES | Atributos clave | Notas |
|--------------|-----|-----------------|-------|
| `Faculty` | Facultad | `id`, `code`, `name` | Dato maestro UMSS |
| `AcademicProgram` | Carrera | `id`, `facultyId`, `code`, `name`, `status` | Unidad de acreditación |
| `AppUser` | Usuario | `id`, `email`, `role`, `status` | Rol único (`CC`/`TD`/`JD`); email `@umss.edu.bo`; estados `INACTIVE`→`ACTIVE`→`DEACTIVATED` |
| `UserProgramAssignment` | Asignación alcance | `id`, `userId`, `programId`, `assignedAt`, `revokedAt` | Rol en `AppUser`; alcance carrera aquí (FSD-BR-09); revocación soft (`revokedAt`) |

### 3.2 Plantilla normativa

| Entidad | Atributos clave | Notas |
|---------|-----------------|-------|
| `AccreditationTemplate` | `modality` (CEUB \| ARCU-SUR), `version`, `status` | Activada por [JD] |
| `TemplatePhase` | `templateId`, `order`, `name`, `normativeDeadline` | No editable por [CC] (BR-17) |
| `EvaluationDimension` | `templateId`, `code`, `name` | Agrupa criterios |
| `EvaluationCriterion` | `dimensionId`, `code`, `description` | |
| `IndicatorCatalog` | `templatePhaseId`, `criterionId`, `code` | Definición en plantilla |

### 3.3 Proceso en ejecución

| Entidad | Atributos clave | Notas |
|---------|-----------------|-------|
| `AccreditationProcess` | `programId`, `templateId`, `managementYear`, `status` | EN_PROCESO \| ACREDITADO \| VENCIDO |
| `Phase` | `processId`, `templatePhaseId` | Estado derivado por reglas agregadas |
| `Indicator` | `phaseId`, `catalogId` | Estado derivado desde `indicator_state_history` |

### 3.4 Evidencia y auditoría

| Entidad | Atributos clave | Notas |
|---------|-----------------|-------|
| `Evidence` | `indicatorId`, `latestVersionId` | Cabecera estable; no contiene estado mutable |
| `EvidenceVersion` | `evidenceId`, `versionNumber`, `contentHash`, `observationId`, `supersedesVersion` | Append-only |
| `Observation` | `indicatorId`, `justification`, `createdBy`, `createdAt` | Origen de subsanación |
| `IndicatorStateHistory` | `indicatorId`, `previousState`, `newState`, `actorId`, `role`, `createdAt` | Historial append-only de transiciones |
| `AuditLog` | `action`, `actorId`, `entityType`, `entityId`, `payload` | Login, DELETE denegado, etc. |
| `NotificationOutbox` | `eventType`, `recipientId`, `payload`, `sentAt` | Patrón outbox |
| `PublicationSnapshot` | `programId`, `publishedAt`, `publishedBy` | Portal [P] |

---

## 4. Máquina de estados — estado derivado de `Indicator`

| Estado | Descripción |
|--------|-------------|
| `PENDIENTE` | Sin Evidencia cargada |
| `SUBIDO` | Evidencia en revisión [TD] |
| `OBSERVADO` | Rechazado con observación activa |
| `SUBSANADO` | Nueva versión enviada; pendiente re-revisión |
| `APROBADO` | Validación [TD] completa |

Transiciones válidas: ver [`FSD.md`](FSD.md) §4.1 y `team/alexAlvarez/docs/context/04_state_machine.md`. La implementación física no actualiza `Indicator.status`; inserta una fila en `indicator_state_history` y expone el estado vigente mediante `indicator_current_view`.

---

## 5. Diccionario de validación (campos críticos)

| Entidad | Atributo | Tipo lógico | Obl. | Validación |
|---------|----------|-------------|------|------------|
| `Evidence` | `indicatorId` | UUID | sí | Existe; carrera ∈ alcance [CC] |
| `EvidenceVersion` | `contentHash` | string(64) | sí | SHA-256 del blob |
| `EvidenceVersion` | `observationId` | UUID | cond. | Obligatorio si subsanación |
| `Indicator` | `currentState` | enum derivado | sí | Valores §4; se obtiene desde `indicator_current_view` |
| `Observation` | `justification` | text | sí | min 20 caracteres (configurable) |
| `AppUser` | `email` | string | sí | Dominio `@umss.edu.bo`; login inválido → `401` (A1); alta inválida → `422` |
| `AppUser` | `role` | enum | sí | `CC`, `TD`, `JD` (un rol por usuario) |
| `AppUser` | `status` | enum | sí | `INACTIVE`, `ACTIVE`, `DEACTIVATED` |
| `UserProgramAssignment` | `revokedAt` | timestamp | no | `null` = asignación activa; índice único parcial activas |
| `AuditLog` | `action` | string | sí | Catálogo cerrado (`AUDIT_LOGIN`, `AUDIT_DELETE_DENIED`, …) |

**Prohibido:** `isDeleted` / `deletedAt` en `Evidence` o `EvidenceVersion` aprobados, y `UPDATE` destructivo para transiciones de estado de `Indicator`.

---

## 6. Mapeo lógico → físico (DTI)

| Entidad lógica | Tabla física |
|----------------|--------------|
| `Faculty` | `faculty` |
| `AcademicProgram` | `academic_program` |
| `AppUser` | `app_user` |
| `UserProgramAssignment` | `user_program_assignment` |
| `AccreditationProcess` | `accreditation_process` |
| `Phase` | `phase` |
| `Indicator` | `indicator` |
| `Evidence` | `evidence` |
| `EvidenceVersion` | `evidence_version` |
| `Observation` | `observation` |
| `IndicatorStateHistory` | `indicator_state_history` |
| `AuditLog` | `audit_log` |

Detalle de columnas, índices y FK: ver DTI §2–3.

---

## 7. Reglas de datos vinculadas

| Regla FSD | Impacto en modelo |
|-----------|-------------------|
| FSD-BR-02 | Sin DELETE en `evidence_version` aprobada |
| FSD-BR-06 | FK `observation_id` en versión subsanatoria |
| FSD-BR-08 | Índice único parcial `accreditation_process` activo |
| FSD-BR-09 | Filtro `program_id` en queries [CC]; alcance vía `user_program_assignment` |
| FSD-BR-12 | Dominio `@umss.edu.bo` en `app_user`; login A1 → `401` genérico |
| Máquina de estados | `indicator_state_history` append-only + `indicator_current_view` |

---

## Registro de cambios

| Versión | Fecha | Cambio |
|---------|-------|--------|
| v1.2 | 2026-06-23 | MOD-AUTH: atributos `AppUser`/`UserProgramAssignment` alineados a DD-UC-001 (sin `displayName`/`roleCode`) |
| v1.1 | 2026-06-22 | `@dtp-sync` DD-UC-001: tabla `user_program_assignment` en mapeo lógico→físico (MOD-AUTH) |
| Dorada v1.0 | 2026-05-16 | Vista funcional extraída de FSD.md; enlace a DTI |
