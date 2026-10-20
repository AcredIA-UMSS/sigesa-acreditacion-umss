# Business Overview

SIGESA apoya la acreditación de carreras de la UMSS (plantillas CEUB / ARCU-SUR). Un proceso de acreditación pertenece a una carrera, nace desde una plantilla normativa y recorre estados `ACTIVE`, `CLOSED` y `ARCHIVED`.

El modo operativo del proceso es independiente del estado: `ACTIVE`, `AUDIT_READONLY` e `IMPROVEMENT_MONITORING`. `AUDIT_READONLY` es el congelamiento de auditoría. Lo escribe el flujo de freeze (`POST /api/v1/processes/{processId}/freeze`, rol `TD`) sobre la columna `operational_mode`.

## Quién ve procesos

Roles de dominio (`Role`): `CC`, `TD`, `JD`, `EE`, `CAE_MEMBER`.

| Rol | Alcance sobre procesos |
| --- | --- |
| `JD`, `TD` | Todas las carreras |
| `CC`, `EE`, `CAE_MEMBER` | Solo las carreras de su asignación activa |
| Alcance de carrera vacío | El listado queda vacío |
| Otro rol autenticado | El listado y el detalle responden 403 |

`JD` crea y archiva procesos y asigna al responsable `CC`. `TD` congela y levanta el freeze. El detalle de un proceso fuera del alcance de carrera responde 404 (`PROCESS_NOT_FOUND`), para no revelar que existe.

## Lo que esta etapa deja para construir

El resumen `GET /api/v1/processes/stats` todavía no está en el código. El listado actual es el precedente de visibilidad: mismos roles, mismos ceros cuando el alcance está vacío, mismos procesos no archivados. El resumen pedido (`total`, `byStatus`, `byOperationalMode`, `frozenCount`) necesita el modo operativo, que el listado de hoy no devuelve.
