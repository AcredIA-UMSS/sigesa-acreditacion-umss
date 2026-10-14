---
id: DD-UC-030
titulo: "Cierre 2.1: freeze / Sala de Pares / E7 / CAE"
producto: SIGESA / AcredIA
fsd_uc:
  - "FSD-UC-030"
  - "FSD-UC-020"
  - "FSD-UC-031"
  - "FSD-UC-028"
prd_refs:
  - "PRD-US-025"
  - "PRD-US-026"
adrs:
  - "ADR-0005"
prompts:
  - "PR-IMPL-M10-030"
release: "2.1.0"
status: aprobado
fecha: "13/10/2026"
modulo: MOD-WORKFLOW
---

# Design Doc `DD-UC-030` — M10 + M11 + CAE

Cierra el roadmap ADR-0005 **sin UC nuevos**. Anclas: UC-030 (freeze/pares), UC-020 (EE), UC-031 (E7), UC-004/029 (CAE escribe evidencia/FODA).

## 1. M10 — Freeze y Sala de Pares

### ProcessFreeze (V22)

Una fila por proceso: `process_id` UNIQUE, `mode` (`AUDIT_READONLY` \| `IMPROVEMENT_PARTIAL`), `frozen_at`, `frozen_by`, `lifted_at`, `lifted_by`.

- POST freeze `{ }` → mode `AUDIT_READONLY`, `operationalMode=AUDIT_READONLY`, `lifted_*` null. 409 `ALREADY_FROZEN` si ya activo (lifted_at IS NULL y mode AUDIT_READONLY).
- POST lift `{ "targetMode": "ACTIVE" \| "IMPROVEMENT_MONITORING" }` → set lifted_at; si MONITORING, freeze.mode=`IMPROVEMENT_PARTIAL` y process.operationalMode=`IMPROVEMENT_MONITORING`; si ACTIVE, operationalMode=`ACTIVE`.
- GET freeze 200 (mode + timestamps) o 404 `FREEZE_NOT_FOUND` si nunca hubo fila.

### Bloqueo de escritura

Guardia `ProcessFreezeGuard.assertWritable(processId)` en: upload/link evidence, PUT FODA, survey POST, plan POST/PUT/import, generate E4. Si freeze activo `AUDIT_READONLY` → `409 PROCESS_FROZEN`. En `IMPROVEMENT_MONITORING` permitir **solo** update de `progress`/`justification`/`progressEvidenceAssetId` del plan y generate E7.

### EE (UC-020)

- `ProcessAccessPolicy`: EE accede como CC (JWT `programScope` / assignment).
- GET stages, metrics, tree, evidences, freeze, checklist: añadir rol EE.
- Sin POST/PUT de evaluación.

### E5 / E6

`StageDeliverableCode`:

- PRESENTATION: `COMMUNITY_VALIDATION_MINUTES`, `FORMAL_SUBMISSION_PACKAGE` (APPROVED basta; PDF opcional).
- EXTERNAL_PREP: `SIMULATION_REPORT`, `PEER_ROOM_CHECKLIST`.

Bootstrap `ensureMissingDeliverables`.

### Compuertas

- **PRESENTATION (E5→E6):** entregables E5 APPROVED **y** freeze activo `AUDIT_READONLY`.
- **EXTERNAL_PREP (E6→E7):** freeze `AUDIT_READONLY`; E6 APPROVED; checklist 100% `checked=true`.

### Checklist

Tabla `peer_room_checklist_items`: `process_id`, `item_code`, `label`, `checked`, `checked_by`, `checked_at`. UNIQUE `(process_id, item_code)`.

Catálogo al ensure/freeze: `DOCUMENTATION_PACKAGE`, `EVIDENCE_SAMPLE`, `INTERVIEW_GUIDE`, `LOGISTICS`.

PUT body `{ "items": [ { "itemCode", "checked" } ] }` upsert checked.

## 2. M11 — Ciclo E7 (UC-031)

- Códigos E7: `IMPROVEMENT_PROGRESS_REPORT`. Generate PDF (reutilizar `DraftingDocumentRendererPort`) permitido en `POST_ACCREDITATION` o modo `IMPROVEMENT_MONITORING`.
- Extender `ImprovementPlanItem`: `justification` TEXT, `progress_evidence_asset_id` UUID nullable.
- PUT plan en monitoreo: CC puede cambiar progress/justification/asset; no borrar origen FODA.
- **Gate POST_ACCREDITATION:** para cada ítem con `deadline < today` y `progress < 100` exige justification no vacía; entregable E7 con asset + APPROVED.
- PUT plan en `AUDIT_READONLY` → 409 PROCESS_FROZEN.

## 3. CAE

- `Role.CAE_MEMBER`. Flyway: `app_user.role` VARCHAR **20**; CHECK incluye `CAE_MEMBER`.
- Alta JD: igual que CC (requiere `programId`). Frontend `BACKEND_ROLES` + `ROLE_REQUIRES_PROGRAM`.
- Tabla `indicator_delegations`: `id`, `process_id`, `user_id`, `level1_id` nullable, `level2_id` nullable, `indicator_id` nullable, `permission` VARCHAR default `EVIDENCE_AND_FODA`, `created_at`. CHECK: exactamente uno de los tres alcances no nulo. UNIQUE `(process_id, user_id, coalesce(level1,0), ...)`.
- API CC o JD: GET/POST/DELETE `/processes/{id}/delegations`.
- POST evidence y PUT analysis: `hasAnyRole('CC','CAE_MEMBER')`. Si CAE: `CaeDelegationGuard` — el indicador debe caer en una delegación del user (indicatorId exacto, o N2 padre, o N1 ancestro). Si no → 403 `DELEGATION_DENIED`.
- CAE no aprueba indicadores ni etapas ni freeze.

## 4. API nueva

| Código | Método | Ruta |
|--------|--------|------|
| API-WF-18 | POST | `/processes/{id}/freeze` |
| API-WF-19 | POST | `/processes/{id}/freeze/lift` |
| API-WF-20 | GET | `/processes/{id}/freeze` |
| API-WF-21 | GET/PUT | `/processes/{id}/peer-room-checklist` |
| API-WF-22 | GET/POST | `/processes/{id}/delegations` |
| API-WF-23 | DELETE | `/processes/{id}/delegations/{delegationId}` |

Generate E7: mismo API-WF-17, código `IMPROVEMENT_PROGRESS_REPORT`, etapa `POST_ACCREDITATION`.

## 5. Frontend

- Badge modo operativo en detalle.
- TD: Congelar / Levantar (select ACTIVE vs monitoreo).
- Checklist Sala de Pares (EE/TD) si hay freeze o etapa E6.
- JD: rol CAE en alta usuario.
- CC: panel delegaciones (user + alcance N1/N2/indicador).
- Plan: campos justificación / avance en monitoreo.
- EE: `/procesos/{id}` lectura (tree, métricas, checklist); sin botones de mutación.
- CAE: mismas rutas de evidencia/FODA que CC, limitadas por API.

## 6. Tests

Freeze 409 en PUT FODA; EE GET process 200 / PUT 403; gate E5 sin freeze BLOCK; checklist incompleto BLOCK E6; E7 ítem vencido sin justification BLOCK; CAE sin delegación 403; CAE con indicatorId 200; RegisterUser CAE_MEMBER requiere programId.

## 7. No hacer

`docs/baseline/`. SMTP. Portal público. Plantillas CNACU. No inventar UC-032.
