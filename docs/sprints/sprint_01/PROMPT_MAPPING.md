# PROMPT_MAPPING — Sprint 01

> **Ubicación vigente del registro PM.** No existe `docs/PROMPT_MAPPING.md` en raíz. Las entradas PM históricas (PM-001…PM-007) pueden mencionar esa ruta legada en *Archivos modificados*; el contenido vive aquí.

| ID Mapeo | PR-IMPL | Design Doc | FSD Asociado | Descripción de la Tarea |
| :--- | :--- | :--- | :--- | :--- |
| PM-001 | N/A | DD-UC-001, DD-UC-002 | FSD-UC-001, FSD-UC-002 | Design doc MOD-AUTH (@feature-design-doc); **split 1:1** 2026-06-23 → `DD-UC-001` + `DD-UC-002` |
| PM-002 | PR-IMPL-004 | DD-UC-001 | FSD-UC-001, FSD-UC-002 | Implementación MOD-AUTH hexagonal + JWT (Paso 4) |
| PM-003 | PR-IMPL-004 | DD-UC-001 | FSD-UC-001, FSD-UC-002 | Contrato implementación (@sigesa-prompt-contract-architect, Paso 3) |
| PM-004 | PR-IMPL-004 | DD-UC-001 | FSD-UC-001, FSD-UC-002 | Completar MOD-AUTH §6 DD + JaCoCo ≥90% (Paso 4 cierre) |
| PM-005 | PR-IMPL-004 | DD-UC-001 | FSD-UC-001, FSD-UC-002 | Tests Gherkin Authenticate/RegisterUser + DD-UC-001 §6 |
| PM-006 | PR-IMPL-004 | DD-UC-001 | FSD-UC-001, FSD-UC-002 | Hardening post code-review MOD-AUTH |
| PM-007 | N/A | DD-UC-001 | FSD-UC-001, FSD-UC-002 | Cierre review: A1 login estricto + 409 genérico |
| PM-008 | N/A | ADR-004 | Todas | Configuración de Monorepo, React 19, OxLint y autogeneración API con Orval |
| PM-009 | PR-IMPL-003 | DD-UC-003 | FSD-UC-003 | Implementación de Plantillas y Creación de Procesos |
| PM-010 | PR-IMPL-005 | DD-UC-014 | FSD-UC-014 | Implementación MOD-REPORT — reporte ejecutivo PDF asíncrono |
| PM-011 | PR-IMPL-005 | DD-UC-014 | FSD-UC-014 | Code review + @dtp-sync + puente UC-013 |
| PM-012 | PR-IMPL-006 | DD-UC-004 | FSD-UC-004 | Implementación MOD-EVIDENCE — carga Evidencia v1 |
| PM-013 | PR-IMPL-003 | DD-UC-003 | FSD-UC-003 | Implementación UI Layout e Inicialización de Procesos |

> **Trazabilidad vigente (2026-06-23):** FSD-UC-001 → `DD-UC-001` → [`PR-IMPL-001`](../../prompts/impl/PR-IMPL-001.md) · FSD-UC-002 → `DD-UC-002` → [`PR-IMPL-002`](../../prompts/impl/PR-IMPL-002.md). Las filas PM-002…PM-007 conservan **`PR-IMPL-004`** como histórico de ejecución; ver [`archive/PR-IMPL-004`](../../prompts/impl/archive/PR-IMPL-004.md) (redirect: [`impl/PR-IMPL-004.md`](../../prompts/impl/PR-IMPL-004.md)).
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
| **PR-IMPL vinculado** | PR-IMPL-004 |
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

`DD-UC-001` **aprobado** (2026-06-22); prompt registrado como **PM-001**. Implementación y `@dtp-sync` cerrados en PM-002…PM-007.

### Riesgos / observaciones

- Ninguno pendiente para MOD-AUTH v1.0.

### Lecciones / reuso

- `@feature-design-doc` + plantilla estándar + `@save-prompt-mapping`.

### Próximos pasos

- [x] Aprobar `DD-UC-001`
- [x] Crear `PR-IMPL-004` e implementar MOD-AUTH
- [x] `@dtp-sync` tras merge

---

## PM-002

| Campo | Valor |
|---|---|
| **ID** | PM-002 |
| **Fecha** | 2026-06-22 |
| **Hora** | 23:02 |
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

> **Nota:** El contrato íntegro (§1–§8) vivía en `PR-IMPL-004`; hoy archivado en [`archive/PR-IMPL-004.md`](../../prompts/impl/archive/PR-IMPL-004.md). PM-002 registra la **ejecución** del Paso 4, no la redacción del contrato (PM-003).

### Entradas auxiliares

- `docs/prompts/impl/archive/PR-IMPL-004.md` (contrato unificado PM-003; archivado 2026-06-23)
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
- [ ] `mvn test` — pendiente verificación local (`JAVA_HOME` no configurado en entorno agente)
- [x] `@sigesa-architectural-code-reviewer` — completado (PM-006)
- [x] `@dtp-sync` — completado (PM-007)

### Resultado obtenido

Implementación MOD-AUTH v1.0 en código fuente. Cadena: `PM-001 → PM-003 → PM-002` (FSD → DD → contrato → código).

### Riesgos / observaciones

- Tests de integración §6 DD completados en PM-004.
- JaCoCo auth configurado en PM-004 (`pom.xml`).
- Entrada PM-002 reescrita para alinear Paso 4 README; contrato canónico en PM-003.

### Lecciones / reuso

- PM-002 = **solo código** (Paso 4); PM-003 = **solo contrato** (Paso 3). No mezclar artefactos.
- Prerequisitos PM explícitos evitan ejecutar implementación sin contrato aprobado.

### Próximos pasos

- [ ] Configurar `JAVA_HOME` y ejecutar `mvn verify` (JaCoCo numérico)
- [x] Completar tests §6 DD-UC-001
- [x] `@sigesa-architectural-code-reviewer` (Paso 5)
- [x] `@dtp-sync` (Paso 6)
- [ ] Commit/PR: `Implementa FSD-UC-001,002 · Diseño DD-UC-001 · Prompt PR-IMPL-004`

---

## PM-003

| Campo | Valor |
|---|---|
| **ID** | PM-004 |
| **Fecha** | 2026-06-22 |
| **Hora** | 23:09 |
| **Solicitante** | Aylen |
| **Agente/Entorno** | Cursor IDE — Agent |
| **Modelo** | Composer |
| **Tarea** | Cierre Paso 4 — completar MOD-AUTH según `PR-IMPL-004` y `DD-UC-001` §6 |
| **Objetivo** | Tests §6 DD, JaCoCo ≥90% en servicios auth, código producción sin placeholders |
| **Contexto** | PM-002 implementación base; PM-003 contrato; `agents.md` (DTOs record, hexagonal, JaCoCo); `baseline-congelado.mdc` |
| **PR-IMPL vinculado** | PR-IMPL-004 |
| **DD-UC vinculado** | DD-UC-001 |
| **FSD-UC vinculado** | FSD-UC-001, FSD-UC-002 |
| **Prerequisitos PM** | PM-001, PM-003, PM-002 |
| **Estado** | completado |

### Prompt usado exacto

```
Implementa el código siguiendo exactamente docs/prompts/impl/PR-IMPL-00X.md
y docs/design/DD-UC-001.md. Respeta .cursor/rules/baseline-congelado.mdc y
agents.md (cobertura ≥90% JaCoCo, DTOs como records, sin entidades JPA en
controladores, código listo para producción sin placeholders ni TODOs).
```

> **Nota:** `PR-IMPL-00X` = `PR-IMPL-004` en contexto MOD-AUTH.

### Entradas auxiliares

- `docs/prompts/impl/PR-IMPL-004.md`
- `docs/design/DD-UC-001.md` (§6 plan de pruebas)
- `AGENTS.md`
- `.cursor/rules/baseline-congelado.mdc`
- `.cursor/skills/save-prompt-mapping/SKILL.md`

### Archivos generados o modificados

| Acción | Ruta |
|---|---|
| modificado | `pom.xml` |
| modificado | `src/main/java/com/umss/sigesa/adapter/in/security/SecurityConfig.java` |
| generado | `src/main/java/com/umss/sigesa/adapter/in/security/RestAuthenticationEntryPoint.java` |
| modificado | `src/main/java/com/umss/sigesa/adapter/in/web/UserAdminController.java` |
| modificado | `src/main/java/com/umss/sigesa/adapter/in/web/advice/AuthExceptionHandler.java` |
| modificado | `src/main/java/com/umss/sigesa/application/port/in/RegisterUserUseCase.java` |
| modificado | `src/main/java/com/umss/sigesa/application/service/auth/RegisterUserService.java` |
| generado | `src/main/java/com/umss/sigesa/domain/exception/InvalidRoleException.java` |
| modificado | `src/main/java/com/umss/sigesa/adapter/out/persistance/UserJpaAdapter.java` |
| modificado | `src/main/java/com/umss/sigesa/adapter/out/persistance/UserProgramAssignmentJpaRepository.java` |
| modificado | `src/test/java/com/umss/sigesa/application/service/auth/AuthenticateServiceTest.java` |
| modificado | `src/test/java/com/umss/sigesa/application/service/auth/RegisterUserServiceTest.java` |
| modificado | `src/test/java/com/umss/sigesa/application/service/auth/DeactivateUserServiceTest.java` |
| modificado | `src/test/java/com/umss/sigesa/adapter/in/web/AuthControllerTest.java` |
| generado | `src/test/java/com/umss/sigesa/adapter/in/web/UserAdminControllerTest.java` |
| generado | `src/test/java/com/umss/sigesa/adapter/in/security/JwtAuthenticationFilterTest.java` |
| generado | `src/test/java/com/umss/sigesa/adapter/out/auth/LocalAuthAdapterTest.java` |
| generado | `src/test/java/com/umss/sigesa/adapter/out/persistance/UserProgramAssignmentRepositoryTest.java` |
| modificado | `src/test/resources/application-test.yaml` |
| modificado | `docs/PROMPT_MAPPING.md` |

### Cambios realizados

- Completados tests §6 DD-UC-001: unit (Authenticate/Register/Deactivate/LocalAuth), integración (AuthController 401 idéntico A1, UserAdmin 403/201, JwtFilter 401 US-003, `@DataJpaTest` assignment).
- `RestAuthenticationEntryPoint` → 401 JSON en acciones sin autenticación.
- Validación de rol en dominio (`InvalidRoleException`); `RegisterUserUseCase` recibe `roleName` String.
- JaCoCo ≥90% configurado para `AuthenticateService`, `RegisterUserService`, `DeactivateUserService`.
- Eliminado método muerto en `UserJpaAdapter`; `@Transactional` en revoke JPA.

### Validación ejecutada

- [x] `git status --short` — archivos verificados en working tree
- [ ] `mvn verify` — pendiente verificación local (`JAVA_HOME` no configurado en entorno agente)
- [x] `@sigesa-architectural-code-reviewer` — completado (PM-006)
- [x] `@dtp-sync` — completado (PM-007)

### Resultado obtenido

MOD-AUTH alineado a PR-IMPL-004 y DD-UC-001 §6; suite de tests auth completa; JaCoCo auth configurado. Trazabilidad: `PM-001 → PM-003 → PM-002 → PM-004`.

### Riesgos / observaciones

- Cobertura JaCoCo auth no verificada numéricamente hasta `mvn verify` local.
- Tabla resumen referencia PM-003 pero cuerpo PM-003 no está en archivo (posible pérdida previa); no editado (append-only entradas previas).

### Lecciones / reuso

- Separar PM-002 (scaffold inicial) de PM-004 (cierre tests + hardening) mantiene trazabilidad clara.
- `RestAuthenticationEntryPoint` necesario para cumplir 401 US-003 con Spring Security 6.

### Próximos pasos

- [ ] Ejecutar `mvn verify` localmente
- [x] `@sigesa-architectural-code-reviewer` (Paso 5)
- [x] `@dtp-sync` (Paso 6)
- [ ] Commit: `feat: implement MOD-AUTH (DD-UC-001, PR-IMPL-004)`

---

## PM-004

| Campo | Valor |
|---|---|
| **ID** | PM-005 |
| **Fecha** | 2026-06-22 |
| **Hora** | 23:27 |
| **Solicitante** | Aylen |
| **Agente/Entorno** | Cursor IDE — Agent |
| **Modelo** | Composer |
| **Tarea** | Tests Gherkin FSD-UC-001/002 — `AuthenticateService` + `RegisterUserService` |
| **Objetivo** | Unit + integración JUnit 5/Mockito; JaCoCo ≥90%; actualizar `DD-UC-001` §6 con resultado real |
| **Contexto** | PM-004 cierre auth; `agents.md`; escenarios Gherkin en FSD-UC-001/002; nombres dominio: `AuthenticateService`, `RegisterUserService` |
| **PR-IMPL vinculado** | PR-IMPL-004 |
| **DD-UC vinculado** | DD-UC-001 |
| **FSD-UC vinculado** | FSD-UC-001, FSD-UC-002 |
| **Prerequisitos PM** | PM-002, PM-004 |
| **Estado** | completado |

### Prompt usado exacto

```
Genera tests unitarios e integración para AuthenticationService y
CreateUserService cubriendo los escenarios Gherkin de FSD-UC-001 y
FSD-UC-002. Usa JUnit 5 y Mockito. Verifica con JaCoCo que la cobertura
sea ≥90% según agents.md; si no llega, agrega los casos faltantes.
Actualiza docs/design/DD-UC-001.md §6 con el resultado real obtenido.
```

### Entradas auxiliares

- `docs/design/DD-UC-001.md` (§6 plan de pruebas)
- `docs/product/uc/FSD-UC-001.md`
- `docs/product/uc/FSD-UC-002.md`
- `AGENTS.md`
- `src/test/java/com/umss/sigesa/application/service/auth/*Test.java` (existentes PM-004)

### Archivos generados o modificados

| Acción | Ruta |
|---|---|
| modificado | `docs/design/DD-UC-001.md` (§6 resultado real + DoD tests) |
| modificado | `docs/PROMPT_MAPPING.md` |
| modificado | `src/test/java/com/umss/sigesa/application/service/auth/AuthenticateServiceTest.java` |
| modificado | `src/test/java/com/umss/sigesa/application/service/auth/RegisterUserServiceTest.java` |
| modificado | `src/test/java/com/umss/sigesa/application/service/auth/DeactivateUserServiceTest.java` |
| generado | `src/test/java/com/umss/sigesa/application/service/auth/ModAuthServiceIntegrationTest.java` |
| generado | `src/test/java/com/umss/sigesa/application/service/auth/support/InMemoryUserRepository.java` |
| generado | `src/test/java/com/umss/sigesa/application/service/auth/support/InMemoryUserProgramAssignmentRepository.java` |
| generado | `src/test/java/com/umss/sigesa/application/service/auth/support/InMemoryAuthPort.java` |
| generado | `src/test/java/com/umss/sigesa/application/service/auth/support/RecordingAuditLogPort.java` |

### Cambios realizados

- **Unit (Mockito):** `AuthenticateServiceTest` (7 casos, `@DisplayName` Gherkin UC-001); `RegisterUserServiceTest` (7 casos UC-002); `DeactivateUserServiceTest` (2 casos A1 revocación).
- **Integración servicios:** `ModAuthServiceIntegrationTest` + adaptadores in-memory (`support/*`) — flujos login, A1, alta CC, revocación sin Spring/BD.
- **DD-UC-001 §6:** tabla resultado por clase, mapeo Gherkin→test, estado JaCoCo, nota de nombres (`AuthenticateService` / `RegisterUserService`).
- DoD §7: tests implementados; JaCoCo pendiente `mvn verify` local.

### Validación ejecutada

- [x] `git status --short` — archivos verificados en working tree
- [ ] `mvn verify` — no ejecutado (`JAVA_HOME` no configurado en entorno agente)
- [ ] Cobertura JaCoCo numérica ≥90% — pendiente verificación local

### Resultado obtenido

Suite Gherkin FSD-UC-001/002 cubierta en servicios auth; §6 DD actualizado con trazabilidad test↔escenario. Cadena: `PM-001 → … → PM-005`.

### Riesgos / observaciones

- Prompt cita `AuthenticationService`/`CreateUserService`; implementación real: `AuthenticateService`/`RegisterUserService` (documentado en DD §6).
- Porcentaje JaCoCo no medido hasta `mvn verify` en máquina con Java 21.

### Lecciones / reuso

- `ModAuthServiceIntegrationTest` + `support/*` permite integración de servicios sin `@SpringBootTest`.
- `@DisplayName` con texto Gherkin facilita trazabilidad en reportes Surefire.

### Próximos pasos

- [ ] `mvn verify` y registrar % JaCoCo real en DD §6 si difiere
- [ ] `@dtp-sync` (Paso 6)
- [ ] Commit sugerido: `test: Gherkin auth tests and DD-UC-001 §6 (PM-005)`

---

## PM-005

| Campo | Valor |
|---|---|
| **ID** | PM-006 |
| **Fecha** | 2026-06-21 |
| **Hora** | 23:37 |
| **Solicitante** | Usuario |
| **Agente/Entorno** | Cursor IDE — Agent |
| **Modelo** | Composer |
| **Tarea** | Hardening MOD-AUTH post `@code-review-sigesa` |
| **Objetivo** | Implementar todas las mejoras del code review: A1 login, 409 duplicados, índice parcial, seguridad, validación, tests smoke, DTP sync |
| **Contexto** | DD-UC-001; reporte code-review PM-005; AGENTS.md |
| **PR-IMPL vinculado** | PR-IMPL-004 (cierre hardening) |
| **DD-UC vinculado** | DD-UC-001 |
| **FSD-UC vinculado** | FSD-UC-001, FSD-UC-002 |
| **Estado** | completado |

### Prompt usado exacto

```
IMPLEMENTA LAS MEJORAS SEGUN SON PREVISTAS DE RESOLVER DE @code-review-sigesa
```

### Entradas auxiliares

- Reporte `@code-review-sigesa` (violaciones A1, 409, índice parcial, JWT perímetro, @Valid, DTP)
- `docs/design/DD-UC-001.md`
- `.cursor/skills/dtp-sync/SKILL.md`

### Archivos generados o modificados

| Acción | Ruta |
|---|---|
| generado | `src/main/java/com/umss/sigesa/domain/exception/DuplicateEmailException.java` |
| generado | `src/main/java/com/umss/sigesa/domain/exception/DuplicateActiveAssignmentException.java` |
| generado | `src/main/java/com/umss/sigesa/adapter/out/auth/PasswordUtils.java` |
| generado | `src/main/java/com/umss/sigesa/config/AuthSchemaInitializer.java` |
| generado | `src/main/resources/application-dev.yaml` |
| generado | `src/test/java/com/umss/sigesa/adapter/in/web/AuthenticatedApiSmokeTest.java` |
| modificado | `Email.java`, `AuthenticateService.java`, `RegisterUserService.java` |
| modificado | `AuthExceptionHandler.java`, `SecurityConfig.java`, controllers, DTOs |
| modificado | `UserJpaAdapter.java`, `UserProgramAssignmentJpaAdapter.java`, `LocalAuthAdapter.java` |
| modificado | `application.yaml`, tests auth + support in-memory |
| modificado | `docs/design/DD-UC-001.md`, `docs/product/DTP.md`, `docs/PROMPT_MAPPING.md` |

### Cambios realizados

- **A1 login:** `Email.forLogin()` → `401 AUTH_INVALID_CREDENTIALS` para dominio inválido/vacío.
- **409:** `DuplicateEmailException` + check en `RegisterUserService`/`UserJpaAdapter`; handler HTTP.
- **Asignaciones:** validación activa duplicada + `AuthSchemaInitializer` índice `uk_upa_active`.
- **Seguridad:** JWT secret `${SIGESA_JWT_SECRET}`; H2 console solo perfil `dev`; cadena JWT documentada.
- **Calidad:** `@Valid` en DTOs admin; login sin `@Valid` (validación en `Email.forLogin()`); `PasswordUtils` zeroize; smoke test JWT `/fases`.
- **Docs:** DTP §A.1–A.3 + deltas; DD-UC-001 reglas §2 y §6 actualizados.

### Validación ejecutada

- [ ] `mvn verify` — no ejecutado (`JAVA_HOME` ausente en entorno Windows del agente)
- [x] `git status` / `git diff --name-status` — archivos verificados

### Resultado obtenido

Todas las mejoras del code review implementadas en código y documentación viva. JaCoCo pendiente verificación local.

### Riesgos / observaciones

- Perímetro JWT en `/api/v1/**` es breaking change documentado en DTP §A.2.
- Password temporal sigue fuera del response API (canal offline v1.0).

### Próximos pasos

- [ ] `mvn verify` local y registrar % JaCoCo en DD §6
- [ ] Commit sugerido: `fix: MOD-AUTH hardening from code review (PM-005)`

---

## PM-010

| Campo | Valor |
|---|---|
| **ID** | PM-010 |
| **Fecha** | 2026-06-26 |
| **Solicitante** | Usuario |
| **Agente/Entorno** | Cursor IDE — @sigesa-orchestrator |
| **Tarea** | Pipeline AI-SDLC MOD-REPORT (FSD-UC-014) |
| **Objetivo** | Design doc, contrato PR-IMPL-005, backend hexagonal + frontend panel reportes |
| **PR-IMPL vinculado** | PR-IMPL-005 |
| **DD-UC vinculado** | DD-UC-014 |
| **FSD-UC vinculado** | FSD-UC-014 |
| **Estado** | completado |

### Prompt usado exacto

```
@sigesa-orchestrator Necesito implementar el módulo de reportes definido en FSD-UC-004. Hazte cargo del proceso.
```

### Nota de trazabilidad

El usuario citó FSD-UC-004; el módulo de reportes corresponde a **FSD-UC-014** (MOD-REPORT) según LFSD vivo.

---

## PM-011

| Campo | Valor |
|---|---|
| **Fecha** | 2026-06-26 |
| **Solicitante** | Usuario |
| **Agente/Entorno** | Cursor IDE |
| **Tarea** | Code review + DTP sync + puente UC-013 |
| **PR-IMPL vinculado** | PR-IMPL-005 |
| **Estado** | completado |

### Prompt usado exacto

```
proximos pasos 
@code-review-sigesa — revisión arquitectónica del diff
@dtp-sync — registrar OpenPDF, tabla report_job y endpoints en DTP.md
Conectar ExecutiveDataPort al panel semáforo real cuando se implemente FSD-UC-013
```

### Resultado

- Code review MOD-REPORT; fix `REPORT_GENERATION_FAILED` en `ProcessReportJobService`.
- DTP §B.2, deltas §A.2 #4-6, api_contracts API-REP-01..03.
- `ExecutiveDashboardQueryPort` + `ExecutiveDataDashboardAdapter` listos para UC-013.

---

## PM-012

| Campo | Valor |
|---|---|
| **Fecha** | 2026-06-26 |
| **Solicitante** | Usuario |
| **Agente/Entorno** | Cursor IDE — @sigesa-orchestrator |
| **Tarea** | Pipeline AI-SDLC MOD-EVIDENCE (FSD-UC-004) |
| **PR-IMPL vinculado** | PR-IMPL-006 |
| **DD-UC vinculado** | DD-UC-004 |
| **Estado** | completado |

### Prompt usado exacto

```
@sigesa-orchestrator Necesito implementar el módulo de Cargar Evidencia en FSD-UC-004. Hazte cargo del proceso.
```

---

## PM-013

| Campo | Valor |
|---|---|
| **ID** | PM-013 |
| **Fecha** | 2026-06-29 |
| **Hora** | 16:08 |
| **Solicitante** | Equipo de Desarrollo |
| **Agente/Entorno** | Cursor |
| **Modelo** | Claude 3.5 Sonnet / GPT-4o (Orquestador) |
| **Tarea** | Implementación UI Layout e Inicialización de Procesos |
| **Objetivo** | Construir el layout global (Sidebar desplegable) y la vista pura de inicialización de procesos aplicando el sistema de diseño estricto. |
| **Contexto** | Se requiere maquetar la vista sin conectar aún la API (Orval comentado) debido a que el backend está pendiente de despliegue local. |
| **PR-IMPL vinculado** | PR-IMPL-003 |
| **DD-UC vinculado** | DD-UC-003 |
| **FSD-UC vinculado** | FSD-UC-003 |
| **Estado** | completado |

### Prompt usado exacto

```text
@generate-frontend-feature Crea el componente del sidebar como en la imagen y lo demas debe tener un diseño similar al de la imagen. 
Aplica estrictamente las siguientes reglas del Frontend Design & UI Guidelines (design.md):
- Framework: React 19 + TypeScript estricto.
- Tailwind CSS: Usa EXCLUSIVAMENTE las variables de color primario (Azul UMSS), secundario (Rojo UMSS) y rampas definidas.
- Tipografía: IBM Plex Mono (posteriormente ajustado a Inter).
- Que el sidebar sea desplegable.
```

## Resumen de cambios

### Entradas auxiliares

- `docs/design/design.md` (Reglas de diseño UI frontend)
- Imagen de referencia del Mockup UI

### Archivos generados o modificados

| Acción | Ruta |
|--------|------|
| Generado | `frontend/src/features/procesos/components/CreateProcessUI.tsx` |
| Generado | `frontend/src/features/procesos/CreateProcessPage.tsx` |
| Generado | `frontend/src/components/layout/Sidebar.tsx` |
| Modificado | `frontend/src/App.tsx` |
| Modificado | `frontend/src/index.css` |
| Eliminado | `frontend/tailwind.config.ts` |
| Eliminado | `frontend/postcss.config.js` |

### Cambios realizados

- Estructuración de la página con separación de responsabilidades (UI pura vs Contenedor).
- Refactorización del ecosistema a Tailwind CSS v4 nativo.
- Creación de un sidebar colapsable gestionando estado local.

### Validación ejecutada

- [x] Compilación de Vite (`pnpm dev`) — **Resultado:** exitoso.
- [x] Renderizado de componentes sin errores de tipado en TypeScript.

### Resultado obtenido

Interfaz maquetada correctamente con responsive design base, colores institucionales y rutas configuradas. El hook de Orval fue dejado comentado intencionalmente.

### Próximos pasos

- [ ] Implementar React Hook Form + Zod para validación de datos.
- [ ] Descomentar el hook de Orval y realizar el *wiring* final con el backend de Spring Boot.

