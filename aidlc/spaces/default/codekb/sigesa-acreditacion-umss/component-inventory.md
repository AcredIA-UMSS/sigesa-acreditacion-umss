Inventario del camino leído en profundidad. Responsabilidades de persistencia y perfiles: `architecture.md`. Rutas: `api-documentation.md`.

## ProcessController

Adaptador HTTP de la colección de procesos (`/api/v1/processes`). Crea, lista, detalla y archiva. Traduce el `Authentication` a `ProcessQueryContext`: el rol sale de las autoridades y el alcance de carrera sale de `UserProgramAssignmentRepositoryPort`. Depende de los cuatro casos de uso de proceso, de ese puerto y de `NormativeStructureWebMapper`.

## ListProcessesService

Implementa `ListProcessesUseCase`. `JD` y `TD` piden todos los ítems; `CC`, `EE` y `CAE_MEMBER` piden los de sus carreras; alcance vacío devuelve lista vacía. Enriquece carrera, plantilla, conteos normativos y responsable. Depende de `ProcessQueryPort`, `ProgramCatalogPort`, `TemplatePort`, `ProcessResponsiblePort`, `UserRepositoryPort` y `NormativeHierarchyQueryPort`.

## GetProcessDetailService

Implementa `GetProcessDetailUseCase`. Carga el proceso, aplica `ProcessAccessPolicy` y devuelve el detalle enriquecido, incluido `operationalMode`. Mismos puertos de enriquecimiento que el listado. Un rechazo de política termina en 404.

## ProcessAccessPolicy

Función pura. `JD` y `TD` pasan siempre. `CC`, `EE` y `CAE_MEMBER` pasan si `programScope` contiene la carrera del proceso. Cualquier otro rol falla. El fallo lanza `ProcessNotFoundException` con el id del proceso.

## ProcessQueryJpaAdapter

Implementa `ProcessQueryPort`. Lista todos o por carreras, ordenados por `startDate` descendente, y quita `ARCHIVED`. El ítem de lista lleva id, carrera, plantilla, `status` y `startDate`. `findDetailById` devuelve el dominio completo, archivados incluidos, con `operationalMode`. Depende de `SpringDataAccreditationProcessRepository` y `ProcessPersistenceMapper`.

## JwtAuthenticationFilter

`OncePerRequestFilter`. Lee `Authorization: Bearer`, valida con `JwtTokenAdapter` y, si el token sirve, publica un `UsernamePasswordAuthenticationToken`: principal = userId, autoridad `ROLE_<rol>`, details = `programScope`. Token ausente, vacío o inválido: sigue la cadena sin autenticar.

## SecurityConfig

`SecurityFilterChain` stateless, CSRF desactivado, método de seguridad activado. Login, Swagger y `OPTIONS` son públicos. Reglas de ruta para admin, reportes, auditoría, bandeja de técnico y export de dashboard. El resto de `/api/v1/**` exige autenticación. 401 y 403 los escriben `RestAuthenticationEntryPoint` y `RestAccessDeniedHandler`. Codificador de contraseñas: Argon2.

## AccreditationProcess

Modelo de dominio del proceso: id, carrera, plantilla, `status` (texto alineado a `ProcessStatus`), `startDate` y `operationalMode` (default `ACTIVE`). `createFromTemplate` deja el proceso en `ACTIVE`. Lo persiste la entidad `AccreditationProcessJpaEntity` de la tabla `accreditation_processes`.
