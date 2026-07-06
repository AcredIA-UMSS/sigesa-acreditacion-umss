---
id: PR-IMPL-006
feature_asociado: DD-UC-001, DD-UC-002
fecha: "2026-07-06"
autor: "Cursor Agent"
---

# Prompt de Implementación `PR-IMPL-006`

## Propósito
Cerrar brechas MOD-AUTH UC-001/UC-002: endpoints `GET /admin/users` y `GET /programs`, UI admin completa (listado, revocación, alta CC con catálogo).

## Alcance backend
- `ListUsersUseCase` + filtros opcionales `role`/`status`
- `ProgramCatalogController` + catálogo estático v1.0
- Tests HTTP `UserAdminControllerTest`, `ProgramCatalogControllerTest`
- Actualización `api_contracts.md` y `DTP.md`

## Alcance frontend
- Hooks `useListUsers`, `useListPrograms`
- Tabla usuarios + desactivar con confirmación
- Alta CC con `<select>` de carreras
- Invalidación React Query tras alta/revocación

## Trazabilidad
- FSD-UC-001 · DD-UC-001
- FSD-UC-002 · DD-UC-002
- Continúa PR-IMPL-005
