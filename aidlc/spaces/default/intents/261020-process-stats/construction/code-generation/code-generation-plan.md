# Plan de generación — GET /api/v1/processes/stats

## Summary

- Builds: `GET /api/v1/processes/stats` con total, byStatus, byOperationalMode y frozenCount
- Touches: `ProcessController`, caso de uso nuevo, `ProcessQueryPort` y su adaptador JPA
- Tests: 9 pruebas Maven (caso de uso, adaptador JPA y WebMvc 200/401/403)

No hay historias de usuario en este flujo. Cada paso cita el requisito (`FR` / `NFR`) que cubre.

Orden: test-after. Se implementa la capa y después se escriben y corren sus pruebas. No hay migración: `operational_mode` ya está en `V17` y los enums `ProcessStatus` y `OperationalMode` ya existen. No hay frontend. No se toca `docs/baseline/`. No se cambia el DTO del listado ni `ProcessListItem`.

## Testing Contract

```json
{
  "version": 1,
  "methodology": "test-after",
  "source": "org",
  "ordering": "implement each applicable testable layer, then write and run that layer's tests.",
  "scope": "express",
  "test_strategy": "minimal",
  "project_type": "brownfield",
  "applicable_notes": [
    {
      "layer": "org",
      "text": "We treat tests as a first-class deliverable in every Bolt. The specific\nmethodology (TDD, BDD, ATDD, or classic test-after) is affirmed at\npractices-discovery and recorded in `team.md` under this heading with explicit\n`Methodology` and `Ordering` fields; Code Generation resolves those fields\nindependently from coverage, tooling, and scope notes.\n\nWhen no posture has been affirmed, our default per scope is:\n- **Methodology**: test-after\n- **Ordering**: implement each applicable testable layer, then write and run\n  that layer's tests.\n- `mvp`, `enterprise`, `feature`, `infra`, `classic` add an 80% line-coverage\n  floor and CI execution before merge.\n- `bugfix`, `security-patch` add a targeted regression for the specific\n  bug/vulnerability and require the existing suite to remain green.\n- `express` uses the Minimal strategy: requirement-driven unit tests (one per\n  requirement, with a happy-path floor per component); existing tests remain\n  green.\n- `poc`, `refactor`, `workshop` add no extra new-test floor and require the\n  existing suite to remain green.\n\nThe active `Test Strategy` still applies in every scope and determines test\nvolume/types. Scope floors are additive; they never reduce or replace the\nselected strategy.\n\nBuild and Test verifies defined coverage floors and affirmed quality targets;\nthey may not be weakened to make a step pass.\n\nAffirm a stricter posture in `team.md` if the team commits to one."
    }
  ],
  "obligations": {
    "strategy": "minimal",
    "strategy_volume": [
      "One verifiable test per requirement at the narrowest effective level.",
      "At least one happy-path unit test per component.",
      "Unit tests are the default; a bugfix/security scope floor may require an integration or E2E regression when that is the narrowest level that reproduces the defect."
    ],
    "scope_floor": [
      "Keep the existing test suite green.",
      "This scope adds no extra new-test floor beyond the selected test strategy."
    ],
    "combination_rule": "Apply every selected-strategy obligation and every scope-floor obligation; neither replaces the other, and a targeted scope regression may add the narrowest necessary test type beyond the strategy default."
  },
  "plan_profile": {
    "methodology": "test-after",
    "runner_step": "Verify the existing test runner/configuration and record the exact unit-scoped command.",
    "runner_ready_before_first_test": true,
    "testable_layers": [
      "Data model / database behavior",
      "Repository / data access",
      "Business logic",
      "API / endpoint",
      "Frontend behavior"
    ],
    "steps": [
      "Project structure and production configuration skeleton.",
      "Verify the existing test runner/configuration and record the exact unit-scoped command.",
      "Data model / database behavior - implement.",
      "Data model / database behavior - write and run its tests after implementation.",
      "Repository / data access - implement.",
      "Repository / data access - write and run its tests after implementation.",
      "Business logic - implement.",
      "Business logic - write and run its tests after implementation.",
      "API / endpoint - implement.",
      "API / endpoint - write and run its tests after implementation.",
      "Frontend behavior - implement.",
      "Frontend behavior - write and run its tests after implementation.",
      "Environment/build configuration.",
      "Documentation and traceability."
    ]
  },
  "input_sha256": "sha256:c8f84e35190e007d9863fad236154d0269ab9bf633fc2a3ff106ed965ba4a9b9",
  "contract_sha256": "sha256:bb76c1b22f8307be6e5780d50cabab891684449d5b164bd0e825e9e4e5c61e5b"
}
```

## Pasos

El runner ya existe (`backend/pom.xml`, JUnit vía `spring-boot-starter-test`). No hay esqueleto nuevo de proyecto ni cambio de configuración. La capa de modelo de datos no se modifica. La capa de frontend no aplica.

- [x] Paso 1. Verificar el runner con una prueba ya existente y acotada: `mvn -f backend/pom.xml -Dtest=ListProcessesServiceTest test`. El comando de este cambio queda en `unit-test-instructions.md`. (NFR3)

- [x] Paso 2. Repositorio. Añadir en `ProcessQueryPort` un método que devuelva filas con `status` y modo operativo, de todas las carreras o de una lista de carreras, sin procesos `ARCHIVED`. Implementarlo en `ProcessQueryJpaAdapter` leyendo `operational_mode`. No ampliar `ProcessListItem` ni el DTO del listado. (FR2.1, NFR2)

- [x] Paso 3. Prueba del adaptador, después del paso 2. Un `@DataJpaTest` al estilo de `UserProgramAssignmentRepositoryTest`: una fila no archivada en `AUDIT_READONLY` entra; una `ARCHIVED` no entra. Comando en `unit-test-instructions.md`. (FR2.1)

- [x] Paso 4. Caso de uso. Puerto de entrada `GetProcessStatsUseCase`, modelo de aplicación con `total`, `byStatus`, `byOperationalMode` y `frozenCount`, y `GetProcessStatsService` sin Spring ni JPA. Reglas: `JD`/`TD` ven todos los no archivados; `CC`/`EE`/`CAE_MEMBER` solo las carreras de `ProcessQueryContext.programScope` (asignaciones en base de datos, no el claim JWT); alcance nulo o vacío devuelve ceros sin consultar el puerto; claves fijas de estado (`ACTIVE`, `CLOSED`) y de modo (`ACTIVE`, `AUDIT_READONLY`, `IMPROVEMENT_MONITORING`) siempre presentes; `frozenCount` igual a `AUDIT_READONLY`; `total` igual a ambas sumas. Registrar el bean en `ProcessModuleConfig`. (FR1.2–FR1.6, FR2.2–FR2.4)

- [x] Paso 5. Pruebas del caso de uso, después del paso 4. `GetProcessStatsServiceTest` con el puerto mockeado: camino feliz `JD` (conteos y `frozenCount`), `CC` solo su carrera, alcance vacío en ceros, `ARCHIVED` ausente de la población que el puerto ya filtró no se reintroduce, y las claves en cero cuando falta un estado o un modo. (FR1, FR2, NFR3)

- [x] Paso 6. API. En `ProcessController`, `GET /stats` declarado antes de `GET /{processId}`, mismos roles que el listado (`JD`, `TD`, `CC`, `EE`, `CAE_MEMBER`), reutilizando `buildQueryContext()`. El controlador devuelve un DTO, nunca la entidad JPA. 401 y 403 los sigue resolviendo la cadena JWT actual. (FR1.1, FR3, NFR1)

- [x] Paso 7. Pruebas WebMvc, después del paso 6. `ProcessStatsControllerWebMvcTest` al estilo de `ProcessFreezeControllerWebMvcTest`: 200 con el JSON del caso de uso, 401 sin token (`UNAUTHORIZED`), y 403 con un rol distinto de `JD`, `TD`, `CC`, `EE` y `CAE_MEMBER` (`ACCESS_DENIED`). (FR1, FR3.1, FR3.2, NFR3)

- [x] Paso 8. Documentación de cierre: anotar el mapeo en OpenAPI solo con las anotaciones del método, sin editar `docs/baseline/`. Escribir `code-summary.md` y `traceability.json` de esta etapa. (NFR1)
