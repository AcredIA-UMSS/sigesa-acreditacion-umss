# PR-IMPL-014 — Rol evaluador externo [EE] (MOD-REVIEW)

| Campo | Valor |
|-------|-------|
| **FSD-UC** | FSD-UC-019 |
| **DD** | DD-UC-019 |
| **PRD** | PRD-REQ-029, PRD-US-026 |
| **Sprint** | sprint_02 / PM-003 |
| **Release** | v1.1 |

## Objetivo

Habilitar el rol **[EE]** (External Evaluator) para revisión documental **solo lectura** de la carrera asignada, con trazabilidad documental y cambios en backend + frontend.

## Restricciones

- Arquitectura hexagonal; DTOs en controladores.
- No editar `docs/baseline/`.
- [EE] requiere `programId` al alta (igual que [CC]).
- Correo `@umss.edu.bo` (FSD-BR-12).
- Sin mutaciones: evidencias, indicadores, fases, admin, reportes, export dashboard.

## Backend

1. `Role.java` — agregar `EE`.
2. `RegisterUserService` — scope obligatorio para EE; assignment en `user_program_assignment`.
3. `DashboardSummaryAggregationService` — mapear `ROLE_EE` a `coordinatorSection`.
4. `SecurityConfig` — `POST .../export-jobs` solo CC/TD/JD.
5. `AuthDataLoader` — seed `ee@umss.edu.bo` / `EvalDemo2026!` con carrera demo.
6. `AssistantToolRegistry` — enum filtro roles incluye `EE` (sin tools para EE).
7. Tests en `RegisterUserServiceTest` + dashboard si aplica.

## Frontend

1. `roleLabels.ts` — EE en BACKEND_ROLES, ASSIGNABLE_ROLES, ROLE_REQUIRES_PROGRAM.
2. `getPostLoginPath` — EE → `/dashboard`.
3. `CcOnlyRoute` — proteger `/evidencias/cargar`.
4. `Sidebar` — navegación acotada para EE.
5. `CoordinatorDashboardSection` — prop `readOnly`.
6. `DashboardPage` — detectar rol EE, vista solo lectura.
7. README — credenciales seed EE.

## Documentación

- Actualizar: `glosario.md`, `FSD.md`, `PRD.md`, `reglas_negocio.md`, `FSD-UC-002.md`, `DTP.md`.
- Crear: `FSD-UC-019.md`, `DD-UC-019.md` (hecho).
- Registrar: `docs/sprints/sprint_02/PROMPT_MAPPING.md` PM-003.

## Criterios de cierre

1. [JD] puede registrar usuario [EE] con carrera.
2. [EE] login OK; JWT con programScope.
3. Dashboard muestra KPIs de carrera asignada.
4. POST evidencias / export / admin → 403 para EE.
5. Frontend oculta acciones de escritura para EE.
