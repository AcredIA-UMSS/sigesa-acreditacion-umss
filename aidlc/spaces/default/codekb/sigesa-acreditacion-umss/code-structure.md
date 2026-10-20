# Code Structure

Raíz de aplicación: `backend/` (Maven) y `frontend/` (pnpm, ojeado). El paquete Java es `com.umss.sigesa`.

## Paquetes del camino de procesos

| Paquete | Clasificación | Papel en este camino |
| --- | --- | --- |
| `domain.model` | Model/Entity | `AccreditationProcess`, `OperationalMode`, `ProcessStatus`, `Role`. Sin Spring ni JPA. |
| `domain.exception` | Model/Entity | `ProcessNotFoundException` y errores de borrado o freeze (ojeados por nombre). |
| `application.port.in` | Service/UseCase | `ListProcessesUseCase`, `GetProcessDetailUseCase`, `CreateProcessUseCase`, `DeleteProcessUseCase`. |
| `application.port.out` | Repository/DAO | `ProcessQueryPort` y el resto de puertos que el listado enriquece. |
| `application.model.process` | Model/Entity | `ProcessQueryContext` (`role`, `programScope`). |
| `application.service.process` | Service/UseCase | `ListProcessesService`, `GetProcessDetailService`, `ProcessAccessPolicy`. |
| `application.usecase` | Service/UseCase | `CreateProcessUseCaseImpl`, cableado en el mismo config. |
| `config` | Configuration | `ProcessModuleConfig` instancia los casos de uso. |
| `adapter.in.web` | Controller/Handler | `ProcessController`, `ProcessResponsibleController`, `ProcessFreezeController`, DTO y `ProcessExceptionHandler`. |
| `adapter.in.security` | Middleware | Filtro JWT, cadena stateless, 401 y 403. |
| `adapter.out.auth` | Middleware | `JwtTokenAdapter` emite y parsea el token. |
| `adapter.out.persistance` | Repository/DAO | `ProcessQueryJpaAdapter`, entidad, repositorio Spring Data y mapper. El nombre del paquete conserva el typo `persistance`. |
| `src/main/resources` | Configuration / Migration | `application*.yaml` y Flyway `V17` para `operational_mode`. |
| `src/test/java/.../process` | Test | `ListProcessesServiceTest` cubre el recorte por rol. |

## Patrones observados

- Record de consulta inmutable: `ProcessQueryContext` y `ProcessListItem`.
- Política estática sin estado: `ProcessAccessPolicy`.
- Prioridad de rol en el controlador si hubiera varias autoridades: `JD`, luego `TD`, `CC`, `EE`, `CAE_MEMBER`. El JWT emite un solo rol.
- Mapeo manual controlador → DTO. El resumen de lista omite `operationalMode`; el detalle lo copia desde `EnrichedProcessDetail`.

La persistencia de la columna y de los perfiles de esquema está en `architecture.md`. El cableado de beans está en `ProcessModuleConfig` (un `@Bean` por caso de uso de proceso y de plantilla).
