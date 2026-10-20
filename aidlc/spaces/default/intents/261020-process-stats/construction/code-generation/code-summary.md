# Resumen de código — GET /api/v1/processes/stats

## Archivos

Creados:

- `backend/src/main/java/com/umss/sigesa/application/port/in/GetProcessStatsUseCase.java`
- `backend/src/main/java/com/umss/sigesa/application/model/process/ProcessStats.java`
- `backend/src/main/java/com/umss/sigesa/application/service/process/GetProcessStatsService.java`
- `backend/src/main/java/com/umss/sigesa/adapter/in/web/dto/ProcessStatsResponseDto.java`
- `backend/src/test/java/com/umss/sigesa/application/service/process/GetProcessStatsServiceTest.java`
- `backend/src/test/java/com/umss/sigesa/adapter/out/persistance/ProcessQueryStatsJpaTest.java`
- `backend/src/test/java/com/umss/sigesa/adapter/in/web/ProcessStatsControllerWebMvcTest.java`

Modificados:

- `backend/src/main/java/com/umss/sigesa/application/port/out/ProcessQueryPort.java`
- `backend/src/main/java/com/umss/sigesa/adapter/out/persistance/ProcessQueryJpaAdapter.java`
- `backend/src/main/java/com/umss/sigesa/config/ProcessModuleConfig.java`
- `backend/src/main/java/com/umss/sigesa/adapter/in/web/ProcessController.java`
- `backend/src/test/java/com/umss/sigesa/adapter/in/web/ProcessControllerQueryTest.java`

## Decisiones

El adaptador lee `status` y `operational_mode` y deja fuera `ARCHIVED`. `ProcessListItem` y el DTO del listado no cambian. El caso de uso, sin Spring ni JPA, arma los mapas con las claves fijas. Si `CC`, `EE` o `CAE_MEMBER` no tienen carreras, devuelve ceros y no llama al puerto. `GET /stats` está declarado antes de `GET /{processId}` y responde un DTO. OpenAPI queda en las anotaciones del método.

## Pruebas

`./mvnw -Dtest=ProcessStatsControllerWebMvcTest test` (desde `backend/`, porque `mvn` no está en el PATH) terminó en BUILD SUCCESS: 3 pruebas WebMvc (200, 401 y 403). Con las 5 del caso de uso y la del adaptador, el conjunto de este cambio queda en 9.

## Desviaciones

La primera pasada dejó 8 pruebas y no cubrió el 403. La vuelta 1 añadió `stats_withRoleOutsideAllowedSetReturns403AccessDenied` en `ProcessStatsControllerWebMvcTest`. El código de producción no cambió: `@PreAuthorize` ya rechazaba un rol fuera de `JD`, `TD`, `CC`, `EE` y `CAE_MEMBER`.
