---
id: PR-IMPL-002
feature_asociado: DD-UC-002
fsd_uc:
  - "FSD-UC-002"
fecha: "2026-06-22"
version: "1.0"
estado: Aprobado
autor: "AI Prompt Architect (@sigesa-prompt-contract-architect)"
skill_origen: sigesa-prompt-contract-architect
derivado_de: PR-IMPL-004
---

# Prompt Contract — Implementación `PR-IMPL-002`

> **Generado vía** `@sigesa-prompt-contract-architect` (Paso 3). Derivado del contrato unificado histórico [`PR-IMPL-004`](./archive/PR-IMPL-004.md) (2026-06-22).  
> **Design doc fuente:** [`DD-UC-002`](../../design/DD-UC-002.md) · **FSD:** FSD-UC-002 · **ADR:** ADR-0003.

---

## 1. Propósito y Objetivo

Generar el código Java de **gestión de usuarios [JD]** (MOD-AUTH) implementando estrictamente [`DD-UC-002`](../../design/DD-UC-002.md):

- **FSD-UC-002:** alta **INACTIVE**; asignación `user_program_assignment`; desactivación soft con historial.
- Contraseña temporal generada en servidor; entrega **offline** v1.0.

**Dependencia:** endpoints admin exigen JWT y rol `[JD]` según [`PR-IMPL-001`](./PR-IMPL-001.md) / [`DD-UC-001`](../../design/DD-UC-001.md).

---

## 2. Rol y Persona

- **Identidad:** Desarrollador Backend Senior experto en SIGESA.
- **Expertise:** Java 21, Spring Boot 4.x, hexagonal, JPA en adaptadores, Argon2id, roles DUEA.

---

## 3. Límites de Alcance

### In-Scope

- Dominio: `AppUser`, `Role`, `UserStatus`, `UserProgramAssignment`, `Email`, excepciones admin.
- Casos de uso: `RegisterUserUseCase`, `DeactivateUserUseCase` (Java puro, sin `@Service`).
- Puertos out: `UserRepositoryPort`, `UserProgramAssignmentRepositoryPort`, `AuditLogPort` (`logUserRegistered`, `logUserDeactivated`, stub).
- Adaptadores in: `UserAdminController`.
- Adaptadores out: `UserJpaAdapter`, `UserProgramAssignmentJpaAdapter`.
- DDL / entidades JPA: `app_user`, `user_program_assignment`, índice `uk_upa_active`.
- API:
  - `POST /api/v1/admin/users` (`[JD]`)
  - `PATCH /api/v1/admin/users/{id}/deactivate` (`[JD]`)
- Tests derivados de §6 [`DD-UC-002`](../../design/DD-UC-002.md).
- Wiring vía `AuthModuleConfig`; Flyway prod / `AuthSchemaInitializer` dev.

### Out-of-Scope

- Login JWT / filtro (→ [`PR-IMPL-001`](./PR-IMPL-001.md)).
- Multi-rol, frontend `/admin/users`, recuperación de contraseña.
- `DELETE` físico de usuarios o historial.
- UC-017 completo (stub `AuditLogPort`).
- Modificar `docs/baseline/`.

---

## 4. Restricciones y Reglas

| ID | Regla |
|---|---|
| R1 | Dominio y casos de uso **sin** imports Spring/JPA/Hibernate. |
| R2 | Controladores exponen solo DTOs `record`; **nunca** entidades `@Entity`. |
| R4 | Hash de contraseña con **Argon2id** (ADR-0003). |
| R5 | Emails solo `@umss.edu.bo` en registro (FSD-BR-12). |
| R6 | Un usuario = un rol (`CC`, `TD`, `JD`). |
| R7 | Alcance en `user_program_assignment` (FSD-BR-09); **sin** `programId` plano en User. |
| R10 | Usuario registrado → `INACTIVE`; primer login → `ACTIVE` (aplicado en [`PR-IMPL-001`](./PR-IMPL-001.md)). |
| R11 | Desactivación → `DEACTIVATED` + `revoked_at`; conservar filas históricas. |
| R12 | `[CC]` requiere `programId` en registro; `[TD]`/`[JD]` no. |

- JaCoCo ≥ 90% en `RegisterUserService` y `DeactivateUserService`.
- Tras implementar: `@save-prompt-mapping PR-IMPL-002` → `@dtp-sync` → `@sigesa-architectural-code-reviewer`.

---

## 5. Especificaciones de Entrada

| Documento | Uso |
|---|---|
| `docs/design/DD-UC-002.md` | DDL, reglas, API admin, plan de pruebas |
| `docs/design/DD-UC-001.md` | JWT / rol JD en admin |
| `docs/product/uc/FSD-UC-002.md` | Gherkin alta/revocación |
| `docs/product/api_contracts.md` | Admin users |

```java
public record RegisterUserRequest(String email, String role, UUID programId) {}
public record RegisterUserResponse(UUID userId, String status) {}
```

**Ejemplo registro [JD]:**

```json
{
  "email": "nuevo.cc@umss.edu.bo",
  "role": "CC",
  "programId": "550e8400-e29b-41d4-a716-446655440000"
}
```

---

## 6. Especificaciones de Salida

**Paquetes (UC-002):**

```
com.umss.sigesa.domain.model          → AppUser, Role, UserStatus, UserProgramAssignment, Email
com.umss.sigesa.domain.exception      → DuplicateEmailException, InvalidEmailDomainException, ...
com.umss.sigesa.application.port.in   → RegisterUserUseCase, DeactivateUserUseCase
com.umss.sigesa.application.port.out  → UserRepositoryPort, UserProgramAssignmentRepositoryPort, AuditLogPort
com.umss.sigesa.application.service.auth → RegisterUserService, DeactivateUserService
com.umss.sigesa.adapter.in.web        → UserAdminController, RegisterUserRequest/Response
com.umss.sigesa.adapter.out.persistance → AppUserEntity, UserProgramAssignmentEntity, *JpaAdapter
com.umss.sigesa.config                → AuthModuleConfig, AuthSchemaInitializer, AuthDataLoader
src/test/...                          → tests §6 DD-UC-002
```

| Escenario | HTTP | Body mínimo |
|---|---|---|
| Registro OK | 201 | `{ userId, status: "INACTIVE" }` |
| Email duplicado | 409 | `{ "error": "EMAIL_ALREADY_REGISTERED", ... }` |
| Email inválido | 422 | `{ "error": "INVALID_EMAIL_DOMAIN", ... }` |
| Revocación OK | 204 | (sin body) |
| Admin sin rol JD | 403 | Spring Security |

---

## 7. Anti-patrones y Violaciones

- ❌ `@Service` en `RegisterUserService` / `DeactivateUserService`.
- ❌ Exponer `@Entity` en controladores.
- ❌ `DELETE` de `app_user` o asignaciones históricas.
- ❌ Campo `programId` plano en entidad User.
- ❌ Devolver password temporal en JSON response v1.0.

---

## 8. Checklist de Validación del Contrato

- [x] Enlazado a `DD-UC-002` / FSD-UC-002.
- [x] Scope In/Out y reglas documentados.
- [x] Implementación histórica vía `PR-IMPL-004` (PM-002…PM-007); contrato vigente post-split: **PR-IMPL-002**.
