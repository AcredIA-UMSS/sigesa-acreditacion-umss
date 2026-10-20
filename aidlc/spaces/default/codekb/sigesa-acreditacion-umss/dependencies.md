# Dependencies

Las versiones están en `technology-stack.md`. Aquí solo el grafo que cruza el listado de procesos.

## Internas

`ProcessController` → casos de uso de proceso + `UserProgramAssignmentRepositoryPort`.

`ListProcessesService` y `GetProcessDetailService` → `ProcessQueryPort` y puertos de catálogo, plantilla, responsable, usuario y jerarquía normativa.

`GetProcessDetailService` → `ProcessAccessPolicy` → `ProcessNotFoundException`.

`ProcessModuleConfig` construye esas implementaciones y las publica como beans. Los servicios no se descubren por estereotipo.

`ProcessQueryJpaAdapter` → `SpringDataAccreditationProcessRepository` + `ProcessPersistenceMapper` → `AccreditationProcessJpaEntity` / `AccreditationProcess`.

`JwtAuthenticationFilter` → `JwtTokenAdapter` → `TokenPort`. `SecurityConfig` inserta el filtro delante de `UsernamePasswordAuthenticationFilter`.

`ProcessFreezeController` → casos de uso de freeze + `JwtProgramScopeSupport`. Ese apoyo no lo usa `ProcessController`.

El frontend no compila contra el backend. Orval genera el cliente a partir del OpenAPI.

## Externas directas del camino

- Spring Web y Spring Security sostienen el controlador y el 401/403.
- jjwt sostiene `JwtTokenAdapter`.
- Spring Data JPA sostiene el repositorio de `accreditation_processes`.
- Flyway sostiene el check de `operational_mode` en `prod`.
- PostgreSQL o H2, según el perfil descrito en `architecture.md`.
- Lombok en entidad, dominio y DTO.

POI, OpenPDF, fastcsv, S3 y Bouncy Castle están en el mismo `pom.xml` y quedan fuera del listado de procesos.
