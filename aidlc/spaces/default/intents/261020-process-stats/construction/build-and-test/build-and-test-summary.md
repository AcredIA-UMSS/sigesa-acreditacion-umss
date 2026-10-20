# Resumen de build y pruebas — resumen de procesos

## Estado

El backend compila y empaqueta. La suite Maven queda en verde (560 pruebas, 0 fallos). La estrategia es Minimal: no hay instrucciones de integración, rendimiento ni seguridad. Los cuatro objetivos del contrato quedan en Met, incluido `FR3.2`. El plan, las instrucciones de prueba y el resumen de código están en `construction/code-generation/`.

## Prerrequisitos

Java 21 y `backend/mvnw`. H2 en las pruebas. Sin frontend y sin migración nueva. Detalle en `build-instructions.md`.

## Tipos de prueba

| Tipo | Archivo | Por qué |
| --- | --- | --- |
| Unidad, adaptador y WebMvc | `construction/code-generation/unit-test-instructions.md` | Estrategia Minimal: ya los cubre la generación de código |
| Integración adicional | No generado | Minimal no añade este archivo |
| Rendimiento | No generado | No hay requisitos de rendimiento en `nfr-requirements/` ni en `nfr-design/` (esas etapas no corrieron) y la estrategia es Minimal |
| Seguridad adicional | No generado | Minimal no añade este archivo. El 401 y el 403 de este endpoint están en `ProcessStatsControllerWebMvcTest` |

## Cobertura esperada

No hay piso nuevo. El contrato express pide la suite en verde, un camino feliz por componente y una prueba comprobable por requisito. No se bajó el JaCoCo que ya tenía el `pom`.

## Target Verification Matrix

| Target ID | Source | Expected | Actual | Evidence | Owning Stage | Verdict |
| --- | --- | --- | --- | --- | --- | --- |
| QT-SUITE-GREEN | Testing Contract, `scope_floor` | La suite existente sigue en verde | 560 pruebas, 0 fallos, 3 omitidas, BUILD SUCCESS | `./mvnw test` el 2026-10-20 | build-and-test | Met |
| QT-HAPPY-PATH | Testing Contract, `strategy_volume` | Un camino feliz por componente nuevo | Caso de uso, adaptador JPA y `GET /stats` 200 | Comando acotado, 9 pruebas, BUILD SUCCESS | build-and-test | Met |
| QT-ONE-PER-REQ | Testing Contract, `strategy_volume` | Una prueba comprobable por requisito | `FR3.2` cubierto por `stats_withRoleOutsideAllowedSetReturns403AccessDenied` | `ProcessStatsControllerWebMvcTest` | build-and-test | Met |
| QT-NO-EXTRA-FLOOR | Testing Contract, `scope_floor` | Sin piso de cobertura nuevo | `GetProcessStatsService` no entra en `jacoco-check` | `backend/pom.xml` | build-and-test | Met |

## Preparación

- Compilar y empaquetar: listo (`./mvnw -DskipTests package`, BUILD SUCCESS).
- Pruebas del cambio (9) y suite existente (560): en verde.
- Listo para cerrar esta etapa: sí. Los objetivos aplicables están en Met.

## Pendientes

Ninguno. La vuelta 1 añadió la prueba del 403; esta pasada la volvió a ejecutar en verde.
