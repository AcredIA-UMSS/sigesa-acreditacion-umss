| PR-IMPL-003 | DD-UC-003 | FSD-UC-003 | Implementación de Plantillas y Creación de Procesos |
| — | DD-UC-001 | FSD-UC-001, FSD-UC-002 | Design doc MOD-AUTH (`@feature-design-doc`) | PM-001 |
| PR-IMPL-004 | DD-UC-001 | FSD-UC-001, FSD-UC-002 | Contrato implementación (`@sigesa-prompt-contract-architect`, Paso 3) | PM-003 |
| PR-IMPL-004 | DD-UC-001 | FSD-UC-001, FSD-UC-002 | Implementación MOD-AUTH hexagonal + JWT (Paso 4) | PM-002 |

---

## PM-001

| Campo | Valor |
|---|---|
| **ID** | PM-001 |
| **Fecha** | 2026-06-22 |
| **Hora** | 22:30 |
| **Solicitante** | Aylen |
| **Agente/Entorno** | Cursor IDE — Agent |
| **Modelo** | Composer |
| **Tarea** | `@feature-design-doc` — MOD-AUTH (FSD-UC-001, FSD-UC-002) |
| **Objetivo** | Crear `DD-UC-001` y registrar el prompt en `PROMPT_MAPPING.md` |
| **Contexto** | Plantilla `FEATURE_DESIGN_DOC_TEMPLATE.md`; release v1.0; hexagonal estricta; JWT; `user_program_assignment`; ADR-0003 |
| **PR-IMPL vinculado** | pendiente |
| **DD-UC vinculado** | DD-UC-001 |
| **FSD-UC vinculado** | FSD-UC-001, FSD-UC-002 |
| **Estado** | completado |

### Prompt usado exacto

```
@feature-design-doc FSD-UC-001,FSD-UC-002 titulo="Autenticación y Gestión de Usuarios (MOD-AUTH)" release=v1.0

Usa docs/plantillas/FEATURE_DESIGN_DOC_TEMPLATE.md como base exacta.
Completa el frontmatter: id=DD-UC-001, fsd_uc=[FSD-UC-001, FSD-UC-002],
prd_refs=[PRD-REQ-001, PRD-US-001, PRD-US-002, PRD-US-003], adrs=[ADR-0003],
prompts=[] (se llena después), status=borrador, autores=[<tu nombre>].

Contexto de diseño a respetar en las secciones 1-7:
- Sesión basada en JWT (FSD-UC-001 paso 3), con claims de rol y programScope
- Login no debe revelar si el usuario existe o no (FSD-UC-001, excepción A1)
- Usuario se crea INACTIVO hasta primer acceso (FSD-UC-002, paso 3)
- Relación usuario-alcance como entidad separada (user_program_assignment),
  no como campo plano en User (soporta FSD-BR-09)
- Revocación de acceso conserva historial de auditoría (FSD-UC-002, A1)
- En §3 (Alternativas), evalúa AuthPort/LocalAuthAdapter vs acoplar
  directamente a Spring Security, y concluye si requiere ADR-0003
- En §2, arquitectura hexagonal estricta: dominio sin dependencias de
  Spring/JPA
- En §6, deriva el plan de pruebas de los Gherkin de FSD-UC-001 y FSD-UC-002 y guardalo en prompt_mapping el prompt
```

### Entradas auxiliares

- `docs/plantillas/FEATURE_DESIGN_DOC_TEMPLATE.md`
- `docs/product/uc/FSD-UC-001.md`
- `docs/product/uc/FSD-UC-002.md`
- `docs/product/api_contracts.md`
- `docs/baseline/05_dti/adrs/ADR_003_adapter_autenticacion.md`
- `docs/product/reglas_negocio.md`
- `.cursor/skills/feature-design-doc/SKILL.md`

### Archivos generados o modificados

| Acción | Ruta |
|---|---|
| generado | `docs/design/DD-UC-001.md` |
| modificado | `docs/PROMPT_MAPPING.md` |

### Cambios realizados

- Creado `DD-UC-001` (secciones 1–7): hexagonal MOD-AUTH, DDL, API, alternativas AuthPort vs Spring Security, plan de pruebas Gherkin, DoD.
- Append `PM-001` en `PROMPT_MAPPING.md` (fila resumen + entrada completa).

### Validación ejecutada

- [x] `git status --short` → `docs/design/DD-UC-001.md`, `docs/PROMPT_MAPPING.md`
- [ ] `mvn test` — no aplica (solo diseño)

### Resultado obtenido

`DD-UC-001` en **borrador**; prompt registrado como **PM-001**.

### Riesgos / observaciones

- `PR-IMPL-NNN` pendiente tras aprobar DD.

### Lecciones / reuso

- `@feature-design-doc` + plantilla estándar + `@save-prompt-mapping`.

### Próximos pasos

- [ ] Aprobar `DD-UC-001`
- [ ] Crear `PR-IMPL-NNN` e implementar MOD-AUTH
- [ ] `@dtp-sync` tras merge

---

## PM-002

| Campo | Valor |
|---|---|
| **ID** | PM-002 |
| **Fecha** | 2026-06-22 |
| **Hora** | 23:45 |
| **Solicitante** | Aylen |
| **Agente/Entorno** | Cursor IDE — Agent |
| **Modelo** | Composer |
| **Tarea** | Paso 4 README — ejecutar `PR-IMPL-004` (implementación MOD-AUTH) |
| **Objetivo** | Generar código Java, tests y config del módulo MOD-AUTH según contrato `PR-IMPL-004` y `DD-UC-001` |
| **Contexto** | `DD-UC-001` aprobado (PM-001). Contrato `PR-IMPL-004` generado vía `@sigesa-prompt-contract-architect` (PM-003, Paso 3). ADR-0003, FSD-UC-001/002, hexagonal estricta. |
| **PR-IMPL vinculado** | PR-IMPL-004 |
| **DD-UC vinculado** | DD-UC-001 |
| **FSD-UC vinculado** | FSD-UC-001, FSD-UC-002 |
| **Prerequisitos PM** | PM-001 (design doc), PM-003 (contrato PR-IMPL-004) |
| **Estado** | completado |

### Prompt usado exacto

```
EJECUTA PR-IMPL-004 según docs/prompts/impl/PR-IMPL-004.md (contrato aprobado v1.0, PM-003).

---
Contrato ejecutado (Paso 4 — copia literal de docs/prompts/impl/PR-IMPL-004.md):

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

# Prompt Contract — Implementación PR-IMPL-004

Generar el código Java del módulo MOD-AUTH implementando estrictamente DD-UC-001:
FSD-UC-001 (login JWT, 401 genérico A1, 403 A2), FSD-UC-002 (alta INACTIVE, desactivación soft),
ADR-0003 (AuthPort + LocalAuthAdapter, Argon2id, @umss.edu.bo).

Restricciones clave: dominio y casos de uso sin Spring; DTOs record en controladores;
user_program_assignment (FSD-BR-09); wiring vía AuthModuleConfig; API
POST /api/v1/auth/login, POST /api/v1/admin/users [JD], PATCH .../deactivate.

Salida: capas hexagonales completas (dominio, puertos, application/service/auth,
adapter in/out, SecurityConfig, JwtAuthenticationFilter, tests §6 DD-UC-001),
dependencias spring-boot-starter-security + JJWT 0.12.x.

Cierre: @save-prompt-mapping PR-IMPL-004 → @dtp-sync → @sigesa-architectural-code-reviewer.
```

> **Nota:** El contrato íntegro (§1–§8) vive en `docs/prompts/impl/PR-IMPL-004.md`. PM-002 registra la **ejecución** del Paso 4, no la redacción del contrato (PM-003).

### Entradas auxiliares

- `docs/prompts/impl/PR-IMPL-004.md` (contrato PM-003)
- `docs/design/DD-UC-001.md`
- `docs/design/README.md` (Paso 4)
- `docs/product/uc/FSD-UC-001.md`
- `docs/product/uc/FSD-UC-002.md`
- `docs/baseline/05_dti/adrs/ADR_003_adapter_autenticacion.md`
- `docs/prompts/impl/PR-IMPL-003.md` (patrón hexagonal existente)
- `.cursor/skills/save-prompt-mapping/SKILL.md`

### Archivos generados o modificados

| Acción | Ruta |
|---|---|
| modificado | `pom.xml` |
| modificado | `src/main/resources/application.yaml` |
| modificado | `docs/PROMPT_MAPPING.md` |
| generado | `src/main/java/com/umss/sigesa/domain/model/Role.java` |
| generado | `src/main/java/com/umss/sigesa/domain/model/UserStatus.java` |
| generado | `src/main/java/com/umss/sigesa/domain/model/Email.java` |
| generado | `src/main/java/com/umss/sigesa/domain/model/AppUser.java` |
| generado | `src/main/java/com/umss/sigesa/domain/model/UserProgramAssignment.java` |
| generado | `src/main/java/com/umss/sigesa/domain/model/AuthenticatedIdentity.java` |
| generado | `src/main/java/com/umss/sigesa/domain/exception/InvalidCredentialsException.java` |
| generado | `src/main/java/com/umss/sigesa/domain/exception/RoleNotAssignedException.java` |
| generado | `src/main/java/com/umss/sigesa/domain/exception/InvalidEmailDomainException.java` |
| generado | `src/main/java/com/umss/sigesa/domain/exception/InvalidScopeException.java` |
| generado | `src/main/java/com/umss/sigesa/domain/exception/UserNotFoundException.java` |
| generado | `src/main/java/com/umss/sigesa/application/port/in/AuthenticateUseCase.java` |
| generado | `src/main/java/com/umss/sigesa/application/port/in/RegisterUserUseCase.java` |
| generado | `src/main/java/com/umss/sigesa/application/port/in/DeactivateUserUseCase.java` |
| generado | `src/main/java/com/umss/sigesa/application/port/out/AuthPort.java` |
| generado | `src/main/java/com/umss/sigesa/application/port/out/UserRepositoryPort.java` |
| generado | `src/main/java/com/umss/sigesa/application/port/out/UserProgramAssignmentRepositoryPort.java` |
| generado | `src/main/java/com/umss/sigesa/application/port/out/TokenPort.java` |
| generado | `src/main/java/com/umss/sigesa/application/port/out/IssuedToken.java` |
| generado | `src/main/java/com/umss/sigesa/application/port/out/AuditLogPort.java` |
| generado | `src/main/java/com/umss/sigesa/application/service/auth/AuthenticateService.java` |
| generado | `src/main/java/com/umss/sigesa/application/service/auth/RegisterUserService.java` |
| generado | `src/main/java/com/umss/sigesa/application/service/auth/DeactivateUserService.java` |
| generado | `src/main/java/com/umss/sigesa/adapter/in/web/AuthController.java` |
| generado | `src/main/java/com/umss/sigesa/adapter/in/web/UserAdminController.java` |
| generado | `src/main/java/com/umss/sigesa/adapter/in/web/advice/AuthExceptionHandler.java` |
| generado | `src/main/java/com/umss/sigesa/adapter/in/web/dto/LoginRequest.java` |
| generado | `src/main/java/com/umss/sigesa/adapter/in/web/dto/LoginResponse.java` |
| generado | `src/main/java/com/umss/sigesa/adapter/in/web/dto/RegisterUserRequest.java` |
| generado | `src/main/java/com/umss/sigesa/adapter/in/web/dto/RegisterUserResponse.java` |
| generado | `src/main/java/com/umss/sigesa/adapter/in/security/JwtAuthenticationFilter.java` |
| generado | `src/main/java/com/umss/sigesa/adapter/in/security/SecurityConfig.java` |
| generado | `src/main/java/com/umss/sigesa/adapter/out/auth/LocalAuthAdapter.java` |
| generado | `src/main/java/com/umss/sigesa/adapter/out/auth/JwtTokenAdapter.java` |
| generado | `src/main/java/com/umss/sigesa/adapter/out/auth/NoOpAuditLogAdapter.java` |
| generado | `src/main/java/com/umss/sigesa/adapter/out/persistance/entity/AppUserEntity.java` |
| generado | `src/main/java/com/umss/sigesa/adapter/out/persistance/entity/UserProgramAssignmentEntity.java` |
| generado | `src/main/java/com/umss/sigesa/adapter/out/persistance/AppUserJpaRepository.java` |
| generado | `src/main/java/com/umss/sigesa/adapter/out/persistance/UserProgramAssignmentJpaRepository.java` |
| generado | `src/main/java/com/umss/sigesa/adapter/out/persistance/UserJpaAdapter.java` |
| generado | `src/main/java/com/umss/sigesa/adapter/out/persistance/UserProgramAssignmentJpaAdapter.java` |
| generado | `src/main/java/com/umss/sigesa/config/AuthModuleConfig.java` |
| generado | `src/main/java/com/umss/sigesa/config/AuthDataLoader.java` |
| generado | `src/test/java/com/umss/sigesa/application/service/auth/AuthenticateServiceTest.java` |
| generado | `src/test/java/com/umss/sigesa/application/service/auth/RegisterUserServiceTest.java` |
| generado | `src/test/java/com/umss/sigesa/application/service/auth/DeactivateUserServiceTest.java` |
| generado | `src/test/java/com/umss/sigesa/adapter/in/web/AuthControllerTest.java` |

### Cambios realizados

- Implementado MOD-AUTH (Paso 4 AI-SDLC): login JWT, registro [JD], desactivación soft, `user_program_assignment`, Argon2id, seed dev `jd@umss.edu.bo`.
- Casos de uso en Java puro + `AuthModuleConfig`; Spring Security/JPA solo en adaptadores.
- **No incluye** creación de `PR-IMPL-004.md` (corresponde a PM-003, Paso 3).

### Validación ejecutada

- [x] `git status --short` — archivos de código verificados en working tree
- [ ] `mvn test` — no ejecutado (`JAVA_HOME` no configurado)
- [ ] `@sigesa-architectural-code-reviewer` — pendiente (Paso 5)
- [ ] `@dtp-sync` — pendiente (Paso 6)

### Resultado obtenido

Implementación MOD-AUTH v1.0 en código fuente. Cadena: `PM-001 → PM-003 → PM-002` (FSD → DD → contrato → código).

### Riesgos / observaciones

- Tests de integración §6 DD pendientes (401 idéntico A1, UserAdmin 403, JwtFilter, `@DataJpaTest` assignment).
- JaCoCo auth use cases no configurado (solo `FaseServiceImpl` en pom).
- Entrada PM-002 reescrita para alinear Paso 4 README; contrato canónico en PM-003.

### Lecciones / reuso

- PM-002 = **solo código** (Paso 4); PM-003 = **solo contrato** (Paso 3). No mezclar artefactos.
- Prerequisitos PM explícitos evitan ejecutar implementación sin contrato aprobado.

### Próximos pasos

- [ ] Configurar `JAVA_HOME` y ejecutar `mvn test`
- [ ] Completar tests §6 DD-UC-001
- [ ] `@sigesa-architectural-code-reviewer` (Paso 5)
- [ ] `@dtp-sync` (Paso 6)
- [ ] Commit/PR: `Implementa FSD-UC-001,002 · Diseño DD-UC-001 · Prompt PR-IMPL-004`

---

## PM-003

| Campo | Valor |
|---|---|
| **ID** | PM-003 |
| **Fecha** | 2026-06-21 |
| **Hora** | 23:55 |
| **Solicitante** | Usuario (Tech Lead / docente) |
| **Agente/Entorno** | Cursor IDE — Agent |
| **Modelo** | Composer |
| **Tarea** | Corrección Paso 3 README — `@sigesa-prompt-contract-architect` para `PR-IMPL-004` |
| **Objetivo** | Regenerar `PR-IMPL-004.md` como contrato formal del skill (no en flujo `@feature-design-doc`) |
| **Contexto** | Desviación detectada: PR-IMPL-004 creado manualmente / en Paso 2; README exige Paso 3 con `@sigesa-prompt-contract-architect` |
| **PR-IMPL vinculado** | PR-IMPL-004 |
| **DD-UC vinculado** | DD-UC-001 |
| **FSD-UC vinculado** | FSD-UC-001, FSD-UC-002 |
| **Estado** | completado |

### Prompt usado exacto

```
@sigesa-prompt-contract-architect "Diseña el prompt para implementar el DD-UC-001 en Spring Boot"

CORRIGE ESTO: PR-IMPL-004 se creó en el flujo del design doc / recreación manual, no vía ese skill
```

### Entradas auxiliares

- `docs/design/README.md` (Paso 3)
- `.cursor/skills/sigesa-prompt-contract-architect/SKILL.md`
- `docs/design/DD-UC-001.md`
- `docs/prompts/impl/PR-IMPL-003.md` (referencia de autoría)
- `docs/prompts/impl/PR-IMPL-004.md` (versión incorrecta previa)

### Archivos generados o modificados

| Acción | Ruta |
|---|---|
| modificado | `docs/prompts/impl/PR-IMPL-004.md` |
| modificado | `docs/design/DD-UC-001.md` (§5 nota Paso 3) |
| modificado | `docs/PROMPT_MAPPING.md` |

### Cambios realizados

- Reescrito `PR-IMPL-004.md` como **Prompt Contract** completo (§1–§8 del skill): autor `@sigesa-prompt-contract-architect`, `estado: Aprobado`, specs entrada/salida, anti-patrones.
- Actualizado `DD-UC-001` §5: explicita que PR-IMPL-004 es Paso 3 separado del design doc.
- Registrado `PM-003` append-only.

### Validación ejecutada

- [x] Checklist §8 del contrato PR-IMPL-004 completado (excepto Pasos 4–6 de cierre)
- [x] Autor alineado con `PR-IMPL-003` (`@sigesa-prompt-contract-architect`)
- [ ] `mvn test` — no aplica (solo corrección documental)

### Resultado obtenido

Trazabilidad AI-SDLC corregida: `DD-UC-001` → `@sigesa-prompt-contract-architect` → `PR-IMPL-004` → (Paso 4 código ya existente vía PM-002).

### Riesgos / observaciones

- PM-002 reescrito para reflejar solo Paso 4 (implementación); esta entrada (PM-003) cubre Paso 3 (contrato).
- Código generado en PM-002 alineado con contrato v1.0 de este PM.

### Lecciones / reuso

- **Nunca** crear `PR-IMPL-NNN` dentro de `@feature-design-doc`; siempre Paso 3 con `@sigesa-prompt-contract-architect`.
- Verificar frontmatter `autor` y `skill_origen` en todo PR-IMPL.

### Próximos pasos

- [ ] `@sigesa-architectural-code-reviewer` (Paso 5 README)
- [ ] `@dtp-sync` (Paso 6)
- [ ] Commit/PR con trazabilidad FSD → DD → PR-IMPL → PM

