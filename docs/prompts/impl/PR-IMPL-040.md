---
id: PR-IMPL-040
fsd_uc:
  - FSD-UC-010
feature_asociado: DD-UC-010
estado: Implementado
---

# PR-IMPL-040 — Consulta del progreso de fase (API-WF-04)

## Objetivo

Implementa el siguiente feature en este proyecto, incluyendo sus pruebas.

FEATURE: `GET /api/v1/processes/{processId}/phases/{phaseId}/progress` devuelve el progreso de una fase según el estado de sus subfases, usando las entidades existentes (`Phase`, `Subphase`, `PhaseState`, `SubphaseState`). Sin tablas ni dependencias nuevas.

Entrada: `processId` y `phaseId` (UUID) en la ruta. Respuesta 200: `phaseId`, `totalSubphases`, `approvedSubphases`, `pendingSubphases`, `progressPercent` y `state`. Error 400 cuando alguno de los UUID no es válido. Error 404 si el proceso/fase no existe o la fase no pertenece al proceso.

Solo `SubphaseState.APROBADO` cuenta como aprobada; `pendingSubphases` es el total menos las aprobadas; el porcentaje se redondea a 1 decimal; sin subfases el porcentaje es `0.0`.

Pruebas requeridas: progreso parcial, todas aprobadas, sin subfases, UUID inválido y proceso/fase inexistente o fase de otro proceso.

Seguir el estilo y arquitectura existentes (adapter/in/web, application/service, excepciones actuales), sin agregar tablas ni dependencias y manteniendo cambios mínimos.

## Alcance

- `GET /api/v1/processes/{processId}/phases/{phaseId}/progress`
- Respuesta con totales, aprobadas, pendientes, porcentaje con un decimal y estado.
- 400 para UUID inválidos; 404 si no existe el proceso/fase o no pertenecen juntos.
- Pruebas unitarias del cálculo y pruebas HTTP del contrato, validación y 404.
- Sin tablas, migraciones ni dependencias nuevas.

## Invariantes

- Arquitectura hexagonal estricta y uso exclusivo de `Phase` / `Subphase`.
- Solo `SubphaseState.APROBADO` se considera aprobado.
- Fase sin subfases retorna progreso `0.0`.
- No tocar `docs/baseline/`.

## Trazabilidad

`FSD-UC-010 → DD-UC-010 → PR-IMPL-040 → PM-008`
