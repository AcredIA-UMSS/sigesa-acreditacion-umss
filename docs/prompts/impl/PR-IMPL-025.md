---
id: PR-IMPL-025
feature_asociado: DD-AGENT-002
fsd_uc:
  - FSD-UC-002
  - PRD-REQ-028
design_doc: DD-AGENT-002
depende_de:
  - PR-IMPL-002
  - PR-IMPL-013
  - PR-IMPL-024
fecha: "2026-08-11"
version: "1.0"
estado: Implementado
autor: "Cursor Agent"
skill_origen: sigesa-orchestrator
---

# Prompt Contract — Implementación `PR-IMPL-025`

> **Design doc fuente:** [`DD-AGENT-002`](../../design/assistant/DD-AGENT-002.md) · **CRUD usuarios:** [`PR-IMPL-002`](PR-IMPL-002.md) · **Tool calling base:** [`PR-IMPL-013`](PR-IMPL-013.md) · **Patrón agente:** [`PR-IMPL-024`](PR-IMPL-024.md) / [`DD-AGENT-001`](../../design/assistant/DD-AGENT-001.md) · **Catálogo:** [`TOOL-CATALOG.md`](../../design/assistant/TOOL-CATALOG.md).

---

## 1. Propósito y Objetivo

Implementar el **agente copiloto de usuarios** (`agent=users`) embebido en `/admin/users`:

- Backend: perfil `USERS`, tools de lectura/escritura JD-only, RBAC 403, `UserActionPlan` preview/confirm.
- Frontend: `UsersCopilotPanel` + `useUsersCopilot` (layout responsive análogo a phases).
- Docs: `DD-AGENT-002` + actualización `TOOL-CATALOG.md`.
- Regenerar Orval tras contrato OpenAPI.

---

## 2. Rol y Persona

Desarrollador Full-Stack SIGESA (hexagonal Spring Boot 4 + React 19 Orval), replicando el patrón de `PR-IMPL-024` sin refactorizar el CRUD existente de UC-002.

---

## 3. Límites de Alcance

### In-Scope

- `AssistantAgentProfile.USERS` + contexto opcional `userId` / `programId`.
- Tools: `list_users` (filtro programId), `get_user_detail`, `create_user`, `manage_user_status`, `manage_user_assignment`.
- 403 en `POST /assistant/chat` y `GET /assistant/status?agent=users` si rol ≠ JD.
- `ManageUserProgramAssignmentUseCase` (CREATE/UPDATE assignment, mínimo privilegio).
- UI embebida solo en ruta JD `/admin/users`.
- Tests unitarios executor/registry/controller + formatter preview.
- Actualización TOOL-CATALOG y DTP (si contratos API cambian).

### Out-of-Scope

- Sustituir modal CRUD de alta/baja tradicional.
- Variante read-only para CC/TD/EE.
- Eliminación física de usuarios.
- Agente en otras pantallas.
- Modificar `docs/baseline/`.

---

## 4. Restricciones y Reglas

| ID | Regla |
|----|-------|
| R1 | Solo rol JD; defensa en controller (403) + registry + executor. |
| R2 | Escritura con `confirmed=false` → preview; `confirmed=true` → ejecutar. |
| R3 | Correo solo `@umss.edu.bo` (`Email.of`). |
| R4 | Alta → `INACTIVE` hasta primer acceso (RegisterUserUseCase). |
| R5 | Desactivación soft; auditoría conservada (UC-017). |
| R6 | No exponer entidades JPA; DTOs + use cases. |
| R7 | Frontend solo hooks Orval / `customFetch`. |
| R8 | Tokens Tailwind institucionales; sin hex arbitrarios. |

---

## 5. Entregables

| Área | Artefactos |
|------|------------|
| API | `context.agent=users`; status `?agent=users` |
| Tools | 5 tools users + `UserActionPlan` |
| UI | `UsersCopilotPanel`, `useUsersCopilot`, integración `UsersAdminPage` |
| Docs | `DD-AGENT-002.md`, `TOOL-CATALOG.md`, este PR-IMPL |

---

## 6. Post-implementación

- `@dtp-sync fsd=FSD-UC-002` (contrato assistant context)
- `@save-prompt-mapping sprint=2 fsd=FSD-UC-002` → PM-NNN con `pr=PR-IMPL-025`
- Smoke Docker: login JD → `/admin/users` → chat listar / desactivar con confirmación

---

## 7. Criterios de aceptación (resumen)

1. JD crea usuario vía copiloto (correo UMSS, rol, carrera si CC/EE) → INACTIVE.
2. Assignment mínimo privilegio.
3. Desactivación: no login; auditoría intacta; confirmación obligatoria.
4. No-JD: 403 en agent=users; panel ausente en UI.
5. Orval regenerado; tests verdes.
