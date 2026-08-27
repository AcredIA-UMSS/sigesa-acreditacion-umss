---
id: DD-UC-010
fsd_ref: FSD-UC-010
titulo: "Cierre de fase (workflow subfase)"
modulo: MOD-WORKFLOW
estado: Aprobado
fecha: "2026-08-27"
prompts:
  - PR-IMPL-039
---

# DD-UC-010 — Avanzar/cerrar Fase

## Contexto

[FSD-UC-010](../product/uc/FSD-UC-010.md) · Regla [FSD-BR-07](../product/reglas_negocio.md) · Contrato [API-WF-03](../product/api_contracts.md).

Modelo **v1.1**: agregación por **Subfase** (`SubphaseState`), no por indicador/criterio.

## Reglas

| ID | Regla |
|----|-------|
| BR-10.1 | Solo [TD] puede cerrar fase (`403 FORBIDDEN_ROLE` para [CC]) |
| BR-10.2 | Precondición: `COUNT(subfases) = COUNT(subfases WHERE status = APROBADO)` |
| BR-10.3 | Fase sin subfases (0=0) puede cerrarse |
| BR-10.4 | Transición fase `ABIERTA` → `COMPLETADA`; idempotencia: fase ya `COMPLETADA` → `409 INVALID_STATE` |
| BR-10.5 | Violación BR-10.2 → `409 FASE_CIERRE_BLOQUEADO` + lista subfases pendientes |
| BR-10.6 | Publicar evento outbox `PhaseCompleted` |

## Modelo de datos

| Entidad | Campo | Tipo | Default |
|---------|-------|------|---------|
| `phases` | `status` | `VARCHAR(32)` | `ABIERTA` |

Enum dominio `PhaseState`: `ABIERTA`, `COMPLETADA`.

Migración Flyway: `V13__phase_workflow_status.sql` (prod). Dev Docker: Hibernate `ddl-auto: update`.

## API

| Método | Ruta | Rol |
|--------|------|-----|
| POST | `/api/v1/processes/{processId}/phases/{phaseId}/complete` | TD |

**200 OK**

```json
{
  "phaseId": "uuid",
  "previousState": "ABIERTA",
  "newState": "COMPLETADA",
  "event": "PhaseCompleted"
}
```

**409 FASE_CIERRE_BLOQUEADO**

```json
{
  "error": "FASE_CIERRE_BLOQUEADO",
  "message": "...",
  "pendingSubphases": [
    { "subphaseId": "uuid", "name": "...", "status": "PENDIENTE", "order": 1 }
  ]
}
```

## Capas (hexagonal)

| Capa | Artefacto |
|------|-----------|
| Port in | `ClosePhaseUseCase` |
| Port out | `PhaseWorkflowPort` |
| Service | `ClosePhaseService` |
| Adapter in | `PhaseWorkflowController` |
| Adapter out | `PhaseWorkflowJpaAdapter` |

## Frontend

- Acción «Cerrar fase» en acordeón de fase (`ProcessPhaseTree`) visible solo [TD].
- Badge estado fase (`ABIERTA` / `COMPLETADA`).
- Manejo `FASE_CIERRE_BLOQUEADO`: listar subfases pendientes con enlace scroll.

## Impacto specs vivas

- `docs/product/FSD.md` — UC-010 → Implementado
- `docs/product/api_contracts.md` — ruta API-WF-03
- `docs/product/DTP.md` — columna `phases.status`, endpoint WF-03
