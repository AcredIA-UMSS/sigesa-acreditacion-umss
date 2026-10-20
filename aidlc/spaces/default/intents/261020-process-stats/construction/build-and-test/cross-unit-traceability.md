# Trazabilidad entre unidades — resumen de procesos

No hay unidades. El alcance express no generó historias de usuario, así que no hay criterios `AC` que cubrir. La fuente es `inception/requirements-analysis/requirements.md` y el mapa `construction/code-generation/traceability.json`.

## Veredicto

Pasa. Los 20 identificadores (`FR1`–`FR3.3` y `NFR1`–`NFR3`) están en `OK` y el archivo de destino existe.

## Cobertura

| ID | Estado | Destino |
| --- | --- | --- |
| FR1 | OK | `backend/src/main/java/com/umss/sigesa/adapter/in/web/ProcessController.java` |
| FR1.1 | OK | `backend/src/main/java/com/umss/sigesa/adapter/in/web/ProcessController.java` |
| FR1.2 | OK | `backend/src/main/java/com/umss/sigesa/application/service/process/GetProcessStatsService.java` |
| FR1.3 | OK | `backend/src/main/java/com/umss/sigesa/application/service/process/GetProcessStatsService.java` |
| FR1.4 | OK | `backend/src/main/java/com/umss/sigesa/application/service/process/GetProcessStatsService.java` |
| FR1.5 | OK | `backend/src/main/java/com/umss/sigesa/application/service/process/GetProcessStatsService.java` |
| FR1.6 | OK | `backend/src/main/java/com/umss/sigesa/application/service/process/GetProcessStatsService.java` |
| FR2 | OK | `backend/src/main/java/com/umss/sigesa/application/service/process/GetProcessStatsService.java` |
| FR2.1 | OK | `backend/src/main/java/com/umss/sigesa/adapter/out/persistance/ProcessQueryJpaAdapter.java` |
| FR2.2 | OK | `backend/src/main/java/com/umss/sigesa/application/service/process/GetProcessStatsService.java` |
| FR2.3 | OK | `backend/src/main/java/com/umss/sigesa/application/service/process/GetProcessStatsService.java` |
| FR2.4 | OK | `backend/src/main/java/com/umss/sigesa/application/service/process/GetProcessStatsService.java` |
| FR3 | OK | `backend/src/main/java/com/umss/sigesa/adapter/in/web/ProcessController.java` |
| FR3.1 | OK | `backend/src/test/java/com/umss/sigesa/adapter/in/web/ProcessStatsControllerWebMvcTest.java` |
| FR3.2 | OK | `backend/src/test/java/com/umss/sigesa/adapter/in/web/ProcessStatsControllerWebMvcTest.java` |
| FR3.3 | OK | `backend/src/main/java/com/umss/sigesa/application/service/process/GetProcessStatsService.java` |
| NFR1 | OK | `backend/src/main/java/com/umss/sigesa/adapter/in/web/ProcessController.java` |
| NFR2 | OK | `backend/src/main/java/com/umss/sigesa/application/service/process/GetProcessStatsService.java` |
| NFR3 | OK | `backend/src/test/java/com/umss/sigesa/application/service/process/GetProcessStatsServiceTest.java` |

## Sin cubrir

Ninguno.
