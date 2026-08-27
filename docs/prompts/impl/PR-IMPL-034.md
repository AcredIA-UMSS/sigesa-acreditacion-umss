---
id: PR-IMPL-034
titulo: "Subfases con requisitos, evidencias múltiples y observaciones"
fsd_uc:
  - FSD-UC-022
  - FSD-UC-004
  - FSD-UC-021
dd_refs:
  - DD-UC-022
  - DD-UC-004
  - DD-UC-021
sprint: 3
status: implementado
---

# PR-IMPL-034 — Subfases: requisitos, evidencias múltiples y observaciones

## Objetivo

Extender el modelo de **subfase** con `requirements` (requisitos_subfase), permitir **1..N evidencias** por subfase vía FK `evidence.subphase_id`, y habilitar **observaciones** de [TD]/[JD] sobre la evidencia cargada.

## Alcance backend

- Flyway `V9__subphase_requirements_evidence_observations.sql`
- Campo `requirements` en dominio/JPA/DTOs de subfase (proceso y plantilla)
- `SubphaseController`: `POST/GET .../evidences`, `GET/POST .../observations`
- Puertos y servicios: `UploadSubphaseEvidenceUseCase`, `ListSubphaseEvidencesUseCase`, `AddSubphaseObservationUseCase`, `ListSubphaseObservationsUseCase`
- `SubphaseWorkflowJpaAdapter`: bloqueo delete si hay evidencias
- Validación `ProcessStructureGuard.ensureRequirements()` y `TemplateStructureValidator`

## Alcance frontend

- Editor estructura proceso y plantillas: campo requisitos
- `ProcessPhaseTree`: requisitos, listado evidencias, upload modal vía API subfase, panel observaciones
- Feature `frontend/src/features/subphases/`

## API (resumen)

| Método | Ruta | Rol |
|--------|------|-----|
| POST | `/api/v1/subphases/{id}/evidences` | CC |
| GET | `/api/v1/subphases/{id}/evidences` | auth |
| GET | `/api/v1/subphases/{id}/observations` | auth |
| POST | `/api/v1/subphases/{id}/observations` | TD, JD |

Body subfase CRUD: `{ name, order, referenceUrl, description?, requirements }`.

## Criterios de cierre

1. Subfase creada/actualizada exige `requirements` no vacío.
2. [CC] puede subir ≥2 evidencias en la misma subfase.
3. [TD]/[JD] registran observaciones visibles en detalle del proceso.
4. Documentación viva (FSD, DD, DTP, api_contracts) + PM-001 sprint 3.
