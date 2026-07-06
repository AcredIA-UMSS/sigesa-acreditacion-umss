---
id: PR-IMPL-005
feature_asociado: DD-UC-001, DD-UC-002
fecha: "2026-07-05"
autor: "Cursor Agent"
---

# Prompt de Implementación `PR-IMPL-005`

## Propósito
Implementar el frontend de **MOD-AUTH** (UC-001 Login + UC-002 Gestión de usuarios) en `/frontend`, consumiendo hooks Orval existentes, con guards JWT, mapeo centralizado de roles y manejo de errores según `api_contracts.md`.

## Alcance implementado
- `/login` — formulario email/password, validación `@umss.edu.bo`, mensaje genérico 401 (A1).
- Sesión — persistencia JWT, `role`/`programScope`, logout, `ProtectedRoute` + `JdOnlyRoute`.
- `/admin/users` — alta de usuario (JD), placeholder de tabla, hook `useDeactivateUser` preparado.
- `roleLabels.ts` — mapeo JD/CC/TD ↔ etiquetas UI.
- `customFetch` — Bearer token + parse JSON + `ApiError`.
- Proxy Vite `/api` → backend.

## Bloqueos backend documentados (no resueltos en frontend)
1. **`GET /api/v1/admin/users`** — requerido para listado y revocación en fila. Tabla = placeholder explícito.
2. **`GET /api/v1/programs`** — requerido para `<select>` de `programId` en rol Coordinador (CC). Alta CC bloqueada en UI.

## Redirect post-login
- `JD` → `/admin/users`
- `CC` / `TD` → `/procesos/nuevo` (ruta existente con UI estática MOD-PROCESS)

## Trazabilidad
- FSD-UC-001 · DD-UC-001
- FSD-UC-002 · DD-UC-002
- Skill: `@generate-frontend-feature`
- Reglas UI: `.cursor/rules/frontend-design.mdc`

## Pendiente operativo
- Ejecutar `@save-prompt-mapping` en sprint activo.
- Backend: exponer endpoints bloqueantes en próximo ciclo.
