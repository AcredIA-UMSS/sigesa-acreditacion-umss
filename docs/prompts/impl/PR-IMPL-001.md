---
id: PR-IMPL-001
feature_asociado: DD-UC-001
fsd_uc:
  - "FSD-UC-001"
fecha: "2026-06-22"
version: "1.0"
estado: Aprobado
autor: "AI Prompt Architect (@sigesa-prompt-contract-architect)"
skill_origen: sigesa-prompt-contract-architect
derivado_de: PR-IMPL-004
---

# Prompt Contract — Implementación `PR-IMPL-001`

> **Generado vía** `@sigesa-prompt-contract-architect` (Paso 3). Derivado del contrato unificado histórico [`PR-IMPL-004`](./archive/PR-IMPL-004.md) (2026-06-22).  
> **Design doc fuente:** [`DD-UC-001`](../../design/DD-UC-001.md) · **FSD:** FSD-UC-001 · **ADR:** ADR-0003.

---

## 1. Propósito y Objetivo

Generar el código Java de **autenticación y sesión** (MOD-AUTH) implementando estrictamente [`DD-UC-001`](../../design/DD-UC-001.md):

- **FSD-UC-001:** login JWT con claims `role` y `programScope`; 401 genérico (A1); 403 sin rol (A2); perímetro Bearer (E3 / US-003).
- **ADR-0003:** patrón `AuthPort` + `LocalAuthAdapter` v1.0 (Argon2id, emails `@umss.edu.bo`).

**Dependencia:** consume modelo de identidad definido en [`DD-UC-002`](../../design/DD-UC-002.md) / [`PR-IMPL-002`](./PR-IMPL-002.md).

---

## 2. Rol y Persona

- **Identidad:** Desarrollador Backend Senior experto en SIGESA.
- **Expertise:** Java 21, Spring Boot 4.x, hexagonal, Spring Security (filtro JWT), JJWT 0.12.x, Argon2id, roles DUEA `[CC]`, `[TD]`, `[JD]`.

---

## 3. Límites de Alcance

### In-Scope

- Dominio (consumo): `Email`, `AuthenticatedIdentity`, excepciones login (`InvalidCredentialsException`, `RoleNotAssignedException`).
- Caso de uso: `AuthenticateUseCase` / `AuthenticateService` (Java puro, sin `@Service`).
- Puertos out: `AuthPort`, `TokenPort`, `UserRepositoryPort` (activación INACTIVE→ACTIVE), `AuditLogPort` (`logLogin`, stub).
- Adaptadores in: `AuthController`, `JwtAuthenticationFilter`, `SecurityConfig`, `RestAuthenticationEntryPoint`, `AuthExceptionHandler`.
- Adaptadores out: `LocalAuthAdapter`, `JwtTokenAdapter`, `NoOpAuditLogAdapter`.
- API: `POST /api/v1/auth/login` (público).
- Perímetro JWT: todo `/api/v1/**` excepto login.
- Tests derivados de §6 [`DD-UC-001`](../../design/DD-UC-001.md).
- Wiring vía `AuthModuleConfig`.

### Out-of-Scope

- Alta/revocación admin (→ [`PR-IMPL-002`](./PR-IMPL-002.md)).
- DDL `app_user` / `user_program_assignment` (dueño [`DD-UC-002`](../../design/DD-UC-002.md)).
- LDAP/SSO, frontend `/login`, recuperación de contraseña, `429 AUTH_LOCKED` v1.1.
- Modificar `docs/baseline/`.

---

## 4. Restricciones y Reglas

| ID | Regla |
|---|---|
| R1 | Dominio y casos de uso **sin** imports Spring/JPA/Hibernate. |
| R2 | Controladores exponen solo DTOs `record`; **nunca** entidades `@Entity`. |
| R3 | Credenciales verificadas **solo** detrás de `AuthPort` → `LocalAuthAdapter`. |
| R4 | Hash de contraseña con **Argon2id** (ADR-0003). |
| R5 | Emails solo `@umss.edu.bo` en login (FSD-BR-12). |
| R8 | Login fallido → mismo `401` `{ "error": "AUTH_INVALID_CREDENTIALS", "message": "Credenciales inválidas" }`. |
| R9 | Sin rol → `403 ACCESS_DENIED`. |
| R10 | Primer login exitoso de cuenta `INACTIVE` → `ACTIVE` (ciclo de vida en [`DD-UC-002`](../../design/DD-UC-002.md)). |
| R13 | JWT claims: `sub`, `email`, `role`, `programScope[]`, `exp`, `iat`. |

- JaCoCo ≥ 90% en `AuthenticateService`.
- Tras implementar: `@save-prompt-mapping PR-IMPL-001` → `@dtp-sync` → `@sigesa-architectural-code-reviewer`.

---

## 5. Especificaciones de Entrada

| Documento | Uso |
|---|---|
| `docs/design/DD-UC-001.md` | Contratos, API login, reglas, plan de pruebas |
| `docs/design/DD-UC-002.md` | Modelo `AppUser` / `UserStatus` (consumo) |
| `docs/product/uc/FSD-UC-001.md` | Gherkin login, A1/A2/E3 |
| `docs/baseline/05_dti/adrs/ADR_003_adapter_autenticacion.md` | AuthPort/LocalAuthAdapter |
| `docs/product/api_contracts.md` | `POST /auth/login` |

```java
public interface AuthPort {
    Optional<AuthenticatedIdentity> authenticate(Email email, char[] rawPassword);
}

public record LoginRequest(String email, String password) {}
public record LoginResponse(String accessToken, long expiresIn, String role, List<UUID> programScope) {}
```

**Ejemplo login:**

```json
{
  "email": "cc@umss.edu.bo",
  "password": "ChangeMe123!"
}
```

---

## 6. Especificaciones de Salida

**Paquetes (UC-001):**

```
com.umss.sigesa.domain.model          → Email, AuthenticatedIdentity (+ consumo AppUser vía DD-UC-002)
com.umss.sigesa.domain.exception      → InvalidCredentialsException, RoleNotAssignedException
com.umss.sigesa.application.port.in   → AuthenticateUseCase
com.umss.sigesa.application.port.out  → AuthPort, TokenPort, UserRepositoryPort, AuditLogPort
com.umss.sigesa.application.service.auth → AuthenticateService
com.umss.sigesa.adapter.in.web        → AuthController, LoginRequest, LoginResponse
com.umss.sigesa.adapter.in.security   → JwtAuthenticationFilter, SecurityConfig
com.umss.sigesa.adapter.out.auth      → LocalAuthAdapter, JwtTokenAdapter, NoOpAuditLogAdapter
com.umss.sigesa.config                → AuthModuleConfig (bean AuthenticateUseCase)
src/test/...                          → tests §6 DD-UC-001
```

| Escenario | HTTP | Body mínimo |
|---|---|---|
| Login OK | 200 | `{ accessToken, expiresIn, role, programScope }` |
| Credenciales inválidas (A1) | 401 | `{ "error": "AUTH_INVALID_CREDENTIALS", ... }` |
| Sin rol (A2) | 403 | `{ "error": "ACCESS_DENIED", ... }` |
| Sin Bearer en ruta protegida | 401 | `{ "error": "UNAUTHORIZED", ... }` |

---

## 7. Anti-patrones y Violaciones

- ❌ `@Service` en `AuthenticateService`.
- ❌ Verificar password fuera de `AuthPort`.
- ❌ Mensajes 401 distintos según causa (viola A1).
- ❌ Redefinir entidades `AppUser` / DDL (dueño [`PR-IMPL-002`](./PR-IMPL-002.md)).
- ❌ Implementar LDAP/SSO en v1.0.

---

## 8. Checklist de Validación del Contrato

- [x] Enlazado a `DD-UC-001` / FSD-UC-001.
- [x] Scope In/Out y reglas R1–R13 (subset UC-001) documentados.
- [x] Implementación histórica vía `PR-IMPL-004` (PM-002…PM-007); contrato vigente post-split: **PR-IMPL-001**.
