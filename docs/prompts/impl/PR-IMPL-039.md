---
id: PR-IMPL-039
fsd_uc:
  - FSD-UC-010
feature_asociado: DD-UC-010
estado: Aprobado
---

# PR-IMPL-039 — Cierre de fase (API-WF-03)

## Objetivo

Implementar cierre de fase por [TD] cuando todas las subfases están `APROBADO` (FSD-BR-07).

## Alcance

- `PhaseState` + columna `phases.status` (Flyway V13)
- `ClosePhaseUseCase` / `ClosePhaseService` / `PhaseWorkflowController`
- DTOs + excepción `PhaseClosureBlockedException` con subfases pendientes
- Evento outbox `PhaseCompleted`
- UI TD: botón «Cerrar fase» + manejo 409
- Exponer `status` en `PhaseDto` (y `SubphaseDto` en detalle proceso)
- Tests unitarios `ClosePhaseService` (>90% cobertura servicio)
- PM-006 sprint 3

## Fuera de alcance

- Cierre de proceso completo (dictamen JD)
- SQS/async orchestration real (NoOp outbox existente)
- Indicador/criterio legacy

## Invariantes

- Arquitectura hexagonal estricta
- RBAC: `@PreAuthorize("hasRole('TD')")` en endpoint
- No tocar `docs/baseline/`

## Trazabilidad

`FSD-UC-010 → DD-UC-010 → PR-IMPL-039 → PM-006`
