---
id: PR-IMPL-004
feature_asociado: DD-UC-001
fsd_uc:
  - FSD-UC-001
  - FSD-UC-002
fecha: "2026-06-22"
version: "1.0"
estado: Aprobado
autor: "AI Prompt Architect (@sigesa-prompt-contract-architect)"
skill_origen: sigesa-prompt-contract-architect
---

# Prompt Contract — Implementación `PR-IMPL-004`

> **Generado vía** `@sigesa-prompt-contract-architect` (Paso 3, [`docs/design/README.md`](../../design/README.md)).  
> **Design doc fuente:** [`DD-UC-001`](../../design/DD-UC-001.md) · **FSD:** FSD-UC-001, FSD-UC-002 · **ADR:** ADR-0003, ADR-007.

---

## 1. Propósito y Objetivo

Generar el código Java del módulo **MOD-AUTH** (autenticación JWT y gestión de usuarios [JD]) implementando estrictamente [`DD-UC-001`](../../design/DD-UC-001.md):

- **FSD-UC-001:** login JWT con claims `role` y `programScope`; 401 genérico (A1); 403 sin rol (A2).
- **FSD-UC-002:** alta de usuario **INACTIVE**; activación en primer login; desactivación soft con historial.
- **ADR-0003:** patrón `AuthPort` + `LocalAuthAdapter` v1.0 (Argon2id, emails `@umss.edu.bo`).

Este contrato es la **única fuente autorizada** para la implementación del Paso 4 del AI-SDLC. No improvisar arquitectura fuera de `DD-UC-001`.

---

## 2. Rol y Persona

- **Identidad:** Desarrollador Backend Senior experto en SIGESA.
- **Tono:** Técnico, preciso, orientado a producción.
- **Expertise requerida:**
  - Java 21, Spring Boot 4.x
  - Arquitectura hexagonal (puertos/adaptadores)
  - Spring Security en perímetro (filtro JWT, `@PreAuthorize`)
  - JPA/Hibernate solo en adaptadores de salida
  - JWT (JJWT 0.12.x), Argon2id
  - Roles DUEA: `[CC]`, `[TD]`, `[JD]` (nunca «Admin» genérico)

---

## 3. Límites de Alcance

### In-Scope

- Dominio puro: `AppUser`, `Role`, `UserStatus`, `UserProgramAssignment`, `Email`, `AuthenticatedIdentity`, excepciones.
- Casos de uso (Java puro, sin `@Service`): `AuthenticateUseCase`, `RegisterUserUseCase`, `DeactivateUserUseCase`.
- Puertos out: `AuthPort`, `UserRepositoryPort`, `UserProgramAssignmentRepositoryPort`, `TokenPort`, `AuditLogPort` (stub UC-017).
- Adaptadores in: `AuthController`, `UserAdminController`, `JwtAuthenticationFilter`, `SecurityConfig`, `AuthExceptionHandler`.
- Adaptadores out: `LocalAuthAdapter`, `UserJpaAdapter`, `UserProgramAssignmentJpaAdapter`, `JwtTokenAdapter`.
- DDL vía entidades JPA: `app_user`, `user_program_assignment`.
- API REST:
  - `POST /api/v1/auth/login` (público)
  - `POST /api/v1/admin/users` (`[JD]`)
  - `PATCH /api/v1/admin/users/{id}/deactivate` (`[JD]`)
- Tests derivados de §6 `DD-UC-001` (unit + integración mínima).
- Dependencias: `spring-boot-starter-security`, JJWT 0.12.x.
- Wiring de casos de uso vía `@Configuration` (`AuthModuleConfig`), no `@Service` en aplicación.

### Out-of-Scope

- `LdapAuthAdapter`, SSO/OIDC (v1.1).
- Multi-rol por usuario.
- Frontend `/login`, `/admin/users`.
- Recuperación de contraseña, blocklist refresh token.
- `DELETE` físico de usuarios o historial de auditoría.
- Modificar `docs/baseline/`.
- Código fuera del módulo MOD-AUTH salvo dependencias Maven y config JWT en `application.yaml`.

---

## 4. Restricciones y Reglas

### Restricciones duras

| ID | Regla |
|---|---|
| R1 | Dominio y casos de uso **sin** imports Spring/JPA/Hibernate. |
| R2 | Controladores exponen solo DTOs `record`; **nunca** entidades `@Entity`. |
| R3 | Credenciales verificadas **solo** detrás de `AuthPort` → `LocalAuthAdapter`. |
| R4 | Hash de contraseña con **Argon2id** (ADR-0003). |
| R5 | Emails solo `@umss.edu.bo` (FSD-BR-12). |
| R6 | Un usuario = un rol (`CC`, `TD`, `JD`). |
| R7 | Alcance de carrera en `user_program_assignment` (FSD-BR-09); **sin** `programId` plano en User. |
| R8 | Login fallido (inexistente, password incorrecto, `DEACTIVATED`) → mismo `401` `{ "error": "AUTH_INVALID_CREDENTIALS", "message": "Credenciales inválidas" }`. |
| R9 | Sin rol → `403 ACCESS_DENIED`. |
| R10 | Usuario registrado → `INACTIVE`; primer login exitoso → `ACTIVE`. |
| R11 | Desactivación → `DEACTIVATED` + `revoked_at` en asignaciones activas; conservar filas históricas. |
| R12 | `[CC]` requiere `programId` en registro; `[TD]`/`[JD]` no. |
| R13 | JWT claims: `sub`, `email`, `role`, `programScope[]`, `exp`, `iat`. |

### Límites funcionales

- JaCoCo ≥ 90% en clases `*UseCase*` / servicios auth de aplicación (objetivo §6 DD).
- Tras implementar: `@save-prompt-mapping PR-IMPL-004` → `@dtp-sync` → `@sigesa-architectural-code-reviewer`.

---

## 5. Especificaciones de Entrada

**Formato:** Design doc aprobado + specs vivas referenciadas.

**Documentos obligatorios (leer antes de codificar):**

| Documento | Uso |
|---|---|
| `docs/design/DD-UC-001.md` | Contratos, DDL, API, reglas, plan de pruebas |
| `docs/product/uc/FSD-UC-001.md` | Gherkin login, A1/A2 |
| `docs/product/uc/FSD-UC-002.md` | Gherkin alta/revocación |
| `docs/baseline/05_dti/adrs/ADR_003_adapter_autenticacion.md` | AuthPort/LocalAuthAdapter |
| `docs/product/api_contracts.md` | Rutas y códigos HTTP |

**Contratos de dominio (copiar de DD-UC-001 §2):**

```java
public interface AuthPort {
    Optional<AuthenticatedIdentity> authenticate(Email email, char[] rawPassword);
}

public record LoginRequest(String email, String password) {}
public record LoginResponse(String accessToken, long expiresIn, String role, List<UUID> programScope) {}
public record RegisterUserRequest(String email, String role, UUID programId) {}
public record RegisterUserResponse(UUID userId, String status) {}
```

**Ejemplo de request login válido:**

```json
{
  "email": "cc@umss.edu.bo",
  "password": "ChangeMe123!"
}
```

**Ejemplo de request registro [JD] válido:**

```json
{
  "email": "nuevo.cc@umss.edu.bo",
  "role": "CC",
  "programId": "550e8400-e29b-41d4-a716-446655440000"
}
```

---

## 6. Especificaciones de Salida

**Formato:** Código Java + tests en el repositorio, organizado por capas hexagonales.

**Estructura obligatoria de paquetes:**

```
com.umss.sigesa.domain.model          → Role, UserStatus, Email, AppUser, UserProgramAssignment, AuthenticatedIdentity
com.umss.sigesa.domain.exception      → InvalidCredentialsException, RoleNotAssignedException, ...
com.umss.sigesa.application.port.in   → AuthenticateUseCase, RegisterUserUseCase, DeactivateUserUseCase
com.umss.sigesa.application.port.out  → AuthPort, UserRepositoryPort, TokenPort, ...
com.umss.sigesa.application.service.auth → AuthenticateService, RegisterUserService, DeactivateUserService (Java puro)
com.umss.sigesa.adapter.in.web        → AuthController, UserAdminController, DTOs record
com.umss.sigesa.adapter.in.security   → JwtAuthenticationFilter, SecurityConfig
com.umss.sigesa.adapter.out.auth      → LocalAuthAdapter, JwtTokenAdapter, NoOpAuditLogAdapter
com.umss.sigesa.adapter.out.persistance → entidades JPA, repos, UserJpaAdapter, UserProgramAssignmentJpaAdapter
com.umss.sigesa.config                → AuthModuleConfig, AuthDataLoader (seed dev opcional)
src/test/...                          → tests §6 DD-UC-001
```

**Respuestas HTTP esperadas:**

| Escenario | HTTP | Body mínimo |
|---|---|---|
| Login OK | 200 | `{ accessToken, expiresIn, role, programScope }` |
| Credenciales inválidas (A1) | 401 | `{ "error": "AUTH_INVALID_CREDENTIALS", "message": "Credenciales inválidas" }` |
| Sin rol (A2) | 403 | `{ "error": "ACCESS_DENIED", ... }` |
| Registro OK | 201 | `{ userId, status: "INACTIVE" }` |
| Email inválido | 422 | `{ "error": "INVALID_EMAIL_DOMAIN", ... }` |
| Admin sin rol JD | 403 | Spring Security |

**Ejemplo login exitoso:**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "expiresIn": 86400,
  "role": "CC",
  "programScope": ["550e8400-e29b-41d4-a716-446655440000"]
}
```

---

## 7. Anti-patrones y Violaciones

- ❌ Crear `PR-IMPL-004` dentro del flujo `@feature-design-doc` (debe ser Paso 3 separado con `@sigesa-prompt-contract-architect`).
- ❌ `@Service` / `@Autowired` en dominio o casos de uso de aplicación.
- ❌ Exponer `@Entity` JPA en controladores.
- ❌ Verificar password en el controlador o use case sin pasar por `AuthPort`.
- ❌ Mensajes 401 distintos para «usuario no existe» vs «password incorrecto» (viola A1).
- ❌ `DELETE` de `app_user` o asignaciones históricas.
- ❌ Campo `programId` plano en entidad User.
- ❌ Roles genéricos («Admin», «Super User») en lugar de `[CC]`, `[TD]`, `[JD]`.
- ❌ Modificar `docs/baseline/`.
- ❌ Implementar LDAP/SSO en v1.0.

---

## 8. Checklist de Validación del Contrato

- [x] Propósito definido sin ambigüedad y enlazado a `DD-UC-001`.
- [x] Rol específico SIGESA (no genérico).
- [x] Scope In/Out explícito y exhaustivo.
- [x] Restricciones duras con IDs trazables a FSD/ADR.
- [x] Entrada: documentos + contratos + ejemplos JSON.
- [x] Salida: estructura de paquetes + respuestas HTTP + ejemplo.
- [x] Sin suposiciones fuera de `DD-UC-001` aprobado.
- [x] No viola invariantes SIGESA (append-only auditoría, roles DUEA, hexagonal).
- [x] Generado por `@sigesa-prompt-contract-architect` (Paso 3 README).
- [x] Implementación ejecutada (Paso 4) y registrada en `PROMPT_MAPPING.md` (`@save-prompt-mapping`).
- [x] Revisión arquitectónica (Paso 5) y `@dtp-sync` (Paso 6) completados (PM-006, PM-007).
