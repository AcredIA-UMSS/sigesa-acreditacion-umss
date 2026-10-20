## Developer Code Scan Results

### Scan Coverage
- **Analyzed deeply**:
  - backend/pom.xml
  - backend/src/main/java/com/umss/sigesa/adapter/in/security/JwtAuthenticationFilter.java
  - backend/src/main/java/com/umss/sigesa/adapter/in/security/JwtProgramScopeSupport.java
  - backend/src/main/java/com/umss/sigesa/adapter/in/security/RestAccessDeniedHandler.java
  - backend/src/main/java/com/umss/sigesa/adapter/in/security/RestAuthenticationEntryPoint.java
  - backend/src/main/java/com/umss/sigesa/adapter/in/security/SecurityConfig.java
  - backend/src/main/java/com/umss/sigesa/adapter/in/web/ProcessController.java
  - backend/src/main/java/com/umss/sigesa/adapter/in/web/ProcessFreezeController.java
  - backend/src/main/java/com/umss/sigesa/adapter/in/web/ProcessResponsibleController.java
  - backend/src/main/java/com/umss/sigesa/adapter/in/web/dto/ProcessSummaryResponseDto.java
  - backend/src/main/java/com/umss/sigesa/adapter/out/auth/JwtTokenAdapter.java
  - backend/src/main/java/com/umss/sigesa/adapter/out/persistance/ProcessQueryJpaAdapter.java
  - backend/src/main/java/com/umss/sigesa/adapter/out/persistance/entity/AccreditationProcessJpaEntity.java
  - backend/src/main/java/com/umss/sigesa/adapter/out/persistance/mapper/ProcessPersistenceMapper.java
  - backend/src/main/java/com/umss/sigesa/adapter/out/persistance/repository/SpringDataAccreditationProcessRepository.java
  - backend/src/main/java/com/umss/sigesa/application/model/process/ProcessQueryContext.java
  - backend/src/main/java/com/umss/sigesa/application/port/out/ProcessQueryPort.java
  - backend/src/main/java/com/umss/sigesa/application/service/process/GetProcessDetailService.java
  - backend/src/main/java/com/umss/sigesa/application/service/process/ListProcessesService.java
  - backend/src/main/java/com/umss/sigesa/application/service/process/ProcessAccessPolicy.java
  - backend/src/main/java/com/umss/sigesa/config/ProcessModuleConfig.java
  - backend/src/main/java/com/umss/sigesa/domain/model/AccreditationProcess.java
  - backend/src/main/java/com/umss/sigesa/domain/model/OperationalMode.java
  - backend/src/main/java/com/umss/sigesa/domain/model/ProcessStatus.java
  - backend/src/main/java/com/umss/sigesa/domain/model/Role.java
  - backend/src/main/resources/application-dev.yaml
  - backend/src/main/resources/application-prod.yaml
  - backend/src/main/resources/application.yaml
  - backend/src/main/resources/db/migration/V17__methodological_workflow_stages.sql
  - backend/src/test/java/com/umss/sigesa/application/service/process/ListProcessesServiceTest.java
- **Skimmed only**:
  - frontend/
  - docs/
  - tools/
  - backend/src/main/java/com/umss/sigesa/adapter/in/web/
  - backend/src/main/resources/db/migration/
  - backend/src/test/java/com/umss/sigesa/
  - backend/src/main/java/com/umss/sigesa/application/service/workflow/
  - backend/src/main/java/com/umss/sigesa/config/
- **Left out**:
  - backend/target/
  - frontend/node_modules/
  - frontend/dist/
  - instalación del harness y el árbol de workflow `aidlc/` (fuera del sistema de acreditación)

### Packages Found
- `com.umss:sigesa` (`backend/`) — aplicación — Java 21 — API hexagonal de acreditación universitaria
- `sigesa-frontend` (`frontend/`) — aplicación — TypeScript — interfaz React (ojeada de `package.json` y clientes generados)

### Build System
- **Type**: Maven en el backend; pnpm en el frontend. Módulos separados, sin un build raíz que los enlace.
- **Config Files**: `backend/pom.xml`, `frontend/package.json` (este último solo ojeado).
- **Build Dependencies**: el frontend consume el contrato HTTP; no importa código Java. El backend no depende del frontend.

### APIs Discovered
- REST Spring MVC — `backend/src/main/java/com/umss/sigesa/adapter/in/web/` — 30 controladores bajo `/api/v1`. El inventario vive en `api-documentation.md`.
- Búsqueda en el backend de `processes/stats`, `GetMapping("/stats")` y `ProcessStats`: sin coincidencias. `GET /api/v1/processes/stats` no está mapeado.

### Frameworks & Libraries
- Java 21, Spring Boot 4.1.0, Spring Security, Spring Data JPA, Flyway, jjwt 0.12.6, springdoc 2.8.8, Lombok, JaCoCo 0.8.13.
- Frontend ojeado: React 19, Vite 8, TypeScript 6, Orval, OxLint, Vitest, Playwright.
- Versiones y propósito: `technology-stack.md`. Relaciones: `dependencies.md`.

### Test Coverage
- **Test Directories**: `backend/src/test/java/com/umss/sigesa/` (ojeado, salvo `ListProcessesServiceTest.java`).
- **Test Frameworks**: JUnit 5, Mockito, `spring-boot-starter-test`, `spring-boot-starter-webmvc-test`.
- **Coverage Config**: JaCoCo 0.8.13 presente. El check de línea al 0.90 aplica a una lista cerrada de clases (auth, reportes, dashboard, carga de evidencia). `ListProcessesService` y `ProcessController` quedan fuera de ese umbral.

### Code Quality Indicators
- **Linting**: OxLint declarado en `frontend/package.json`. En el backend no apareció un linter aparte del compilador de Maven.
- **CI/CD**: no existe el directorio `.github/` en la raíz del repositorio.
- **Documentation**: los controladores de procesos llevan anotaciones OpenAPI. `docs/product/` se ojeó (FSD-UC-019 cita `GET /api/v1/processes` y `GET /api/v1/processes/{processId}`). `docs/baseline/` no se modificó.

### Technical Debt Signals
- El paquete de persistencia se llama `persistance`.
- `ProcessController.extractUserId` fabrica un `UUID` aleatorio cuando el principal no es un UUID. `extractProgramScopes` captura cualquier excepción y devuelve lista vacía.
- El listado arma el alcance con `UserProgramAssignmentRepositoryPort`. El congelamiento (`ProcessFreezeController`) lee `Authentication.details` vía `JwtProgramScopeSupport`. El claim `programScope` del JWT y las asignaciones vigentes pueden divergir durante la vida del token (86400 s por defecto).
- `ProcessQueryPort.ProcessListItem` trae `status` y omite `operationalMode`. `ProcessSummaryResponseDto` tampoco lo expone. El detalle sí lo mapea.
- `ProcessQueryJpaAdapter` excluye `ARCHIVED` en memoria después de cargar las filas.
- `ProcessPersistenceMapper.toJpaEntity` no copia `operationalMode` (la entidad nueva queda en el default `ACTIVE`).
- No hay `CREATE TABLE accreditation_processes` en las migraciones Flyway V1–V25. `V7__process_responsible.sql` ya referencia la tabla. El perfil `prod` valida con Hibernate y migra con Flyway `baseline-on-migrate`.
- `application.yaml` incluye un secreto JWT de desarrollo por defecto.

## Handoff Summary
- **Intent-relevant finding**: `GET /api/v1/processes/stats` no existe. En `ProcessController` el orden de mapeo es `POST` colección, `GET` colección (línea 71), `GET /{processId}` (línea 87) y `DELETE /{processId}` (línea 102). Un segmento literal `stats` hoy cae en `/{processId}` (`UUID`). La visibilidad del listado ya está resuelta: `JD` y `TD` ven todos los no archivados; `CC`, `EE` y `CAE_MEMBER` ven solo su carrera; alcance vacío devuelve lista vacía (`ListProcessesService` líneas 43–45, test `ccWithEmptyScopeReturnsEmptyList`). `401` sale de `RestAuthenticationEntryPoint` si no hay autenticación. `403` sale de `@PreAuthorize` más `RestAccessDeniedHandler`. `frozenCount` deberá contar `operationalMode == AUDIT_READONLY` (`OperationalMode` y columna `accreditation_processes.operational_mode`, check en `V17`). Ese campo no viaja en el ítem de listado.
- **Risks / follow-up**: declarar el literal `/stats` en el mismo controlador y antes de `/{processId}`. Reutilizar la regla de visibilidad del listado (ceros si el alcance de carrera está vacío), no el `404` de `ProcessAccessPolicy` en el detalle. Extender el puerto de consulta con `status` y `operationalMode`, o el agregado no puede calcular `byStatus`, `byOperationalMode` ni `frozenCount`. El modo de la columna es la fuente que pide el intent; el registro `ProcessFreeze` es otro agregado y usa el claim JWT, no las asignaciones de BD.
