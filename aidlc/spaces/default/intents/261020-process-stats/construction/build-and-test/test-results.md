# Resultados — resumen de procesos

Fecha: 2026-10-20. Comandos desde `backend/` con `./mvnw`, porque `mvn` no está en el PATH. Es el mismo build que `mvn -f backend/pom.xml`.

## Build

Éxito. `./mvnw -DskipTests package` terminó en BUILD SUCCESS y dejó `backend/target/sigesa-0.0.1-SNAPSHOT.jar`.

## Pruebas

| Comando | Resultado |
| --- | --- |
| `./mvnw -Dtest=ListProcessesServiceTest test` | 4 pruebas, 0 fallos, BUILD SUCCESS |
| `./mvnw -Dtest=GetProcessStatsServiceTest,ProcessQueryStatsJpaTest,ProcessStatsControllerWebMvcTest test` | 9 pruebas (5 + 1 + 3), 0 fallos, BUILD SUCCESS |
| `./mvnw test` | 560 pruebas, 0 fallos, 0 errores, 3 omitidas, BUILD SUCCESS |

Las 3 omitidas ya estaban en otras clases (`AssistantToolRbacGuardTest`, `UploadEvidenceService`). No hay fallos nuevos.

No hay cobertura numérica que exigir: el contrato de este alcance no añade un piso, y el chequeo JaCoCo del `pom` no incluye `GetProcessStatsService`.

## Cierre tras la vuelta 1

Los comandos de esta pasada pasaron. `FR3.2` queda cubierto por `stats_withRoleOutsideAllowedSetReturns403AccessDenied`: 403 y `ACCESS_DENIED` en `GET /api/v1/processes/stats`.

## Matriz de objetivos

| Target ID | Source | Expected | Actual | Evidence | Owning Stage | Verdict |
| --- | --- | --- | --- | --- | --- | --- |
| QT-SUITE-GREEN | Testing Contract, `scope_floor` | La suite existente sigue en verde | 560 pruebas, 0 fallos, 3 omitidas, BUILD SUCCESS | `./mvnw test` el 2026-10-20 | build-and-test | Met |
| QT-HAPPY-PATH | Testing Contract, `strategy_volume` | Un camino feliz por componente nuevo | Caso de uso (`jdCountsStatusesModesAndFrozenCount`), adaptador (`ProcessQueryStatsJpaTest`) y `GET /stats` 200 | Comando acotado, 9 pruebas, BUILD SUCCESS | build-and-test | Met |
| QT-ONE-PER-REQ | Testing Contract, `strategy_volume` | Una prueba comprobable por requisito | `FR3.2` queda en `stats_withRoleOutsideAllowedSetReturns403AccessDenied` | `ProcessStatsControllerWebMvcTest`, 3 pruebas, BUILD SUCCESS | build-and-test | Met |
| QT-NO-EXTRA-FLOOR | Testing Contract, `scope_floor` | Este alcance no añade un piso de cobertura | El `jacoco-check` del `pom` no incluye la clase nueva | `backend/pom.xml`, ejecución `jacoco-check` | build-and-test | Met |

## Loop-Back Log

### Loop-back 1 — 2026-10-20T22:37:00-04:00

- Diagnosis: `FR3.2` no tiene una prueba que espere 403 y `ACCESS_DENIED` en `GET /api/v1/processes/stats`. Los comandos Maven pasaron.
- Root-cause stage: code-generation
- Planned fix: añadir en `ProcessStatsControllerWebMvcTest` un método con un rol distinto de `JD`, `TD`, `CC`, `EE` y `CAE_MEMBER` que espere 403 y `error` igual a `ACCESS_DENIED`.
- Estimated impact: esfuerzo de un método de prueba; coste ninguno; riesgo bajo.
