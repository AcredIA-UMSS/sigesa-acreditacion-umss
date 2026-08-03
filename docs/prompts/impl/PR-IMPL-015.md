# PR-IMPL-015 — Gestión de usuarios [JD]: UI modal, perfil extendido y credenciales (MOD-AUTH)

| Campo | Valor |
|-------|-------|
| **FSD-UC** | FSD-UC-002 |
| **DD** | DD-UC-002 |
| **PRD** | PRD-REQ-001, PRD-US-002 |
| **Sprint** | sprint_02 / PM-004 |
| **Release** | v1.1 |

## Objetivo

Mejorar la pantalla `/admin/users` para [JD]: listado con nombre completo, modal centralizado de alta según mockup, validaciones de campos, contraseña definida por JD con diálogo de confirmación para compartir credenciales (no recuperables post-alta).

## Restricciones

- Arquitectura hexagonal; DTOs en controladores; Lombok en entidades.
- No editar `docs/baseline/`.
- Contraseñas hasheadas (Argon2); nunca exponer hash ni recuperar contraseña existente.
- Frontend: Orval/React Query; tokens UMSS; sin `fetch` manual.
- Sin campo cédula de identidad.

## Backend

1. Migración `V3__app_user_profile_fields.sql` — columnas `first_name`, `last_name`, `phone_number` en `app_user`.
2. `UserProfile`, `InvalidUserProfileException`, `WeakPasswordException`.
3. `AppUser` / `AppUserEntity` — perfil + `getFullName()`.
4. `RegisterUserUseCase.RegisterUserCommand` — email, role, programId, firstName, lastName, phoneNumber, password.
5. `RegisterUserService` — validación perfil; password del request (no auto-generada).
6. `RegisterUserRequest`, `UserAdminSummaryResponse` — campos extendidos.
7. `ListUsersService`, `ListUsersUseCase.UserSummary` — firstName, lastName, fullName, phoneNumber.
8. `UserAdminController` — POST usa password del body.
9. `AuthExceptionHandler` — handlers perfil/contraseña débil.
10. `AuthDataLoader` — seed con nombres y celular demo.
11. Tests: `RegisterUserServiceTest`, `UserAdminControllerTest`, `ModAuthServiceIntegrationTest`.

## Frontend

1. `UsersAdminPage` — lista principal + modal + diálogo éxito.
2. `UsersTableUI` — tabla con nombre completo; botón Agregar usuario.
3. `AddUserModalUI` — modal mockup (Datos personales, 2 columnas, sin cédula).
4. `UserSaveSuccessDialog` — confirmación con email/contraseña y copiar.
5. `userFormValidation.ts` — nombres, celular Bolivia, email, contraseña.
6. `useRegisterUserForm`, `useUsersList` — hooks contenedor.
7. `TextInput` / `Select` — prop `requiredMark`.
8. Modelos Orval manuales: `registerUserRequest.ts`, `userAdminSummaryResponse.ts`.
9. Eliminar `RegisterUserFormUI.tsx` (formulario inline reemplazado por modal).

## Validaciones

| Campo | Regla |
|-------|--------|
| Nombre(s) / Apellido(s) | Obligatorio; al menos una letra; no solo dígitos/símbolos |
| Celular | 8 dígitos; rango 60000000–79999999 |
| Correo | Formato email + `@umss.edu.bo` |
| Contraseña | Mín. 8 chars, letras y números; coincide con repetir |
| Rol | Obligatorio; carrera si CC/EE |

## Criterios de cierre

1. [JD] ve listado con nombre completo, correo, celular, rol y estado.
2. Modal Agregar usuario con Guardar/Cerrar y validaciones en UI.
3. Tras alta exitosa, diálogo muestra credenciales para compartir (una sola vez).
4. POST/GET admin persisten y leen perfil en BD vía JPA/Flyway.
5. Registrar PM-004 en `docs/sprints/sprint_02/PROMPT_MAPPING.md`.
