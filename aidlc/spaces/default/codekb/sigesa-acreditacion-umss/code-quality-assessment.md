# Code Quality Assessment

Escala good / fair / poor, limitada al camino de procesos, JWT y listado. No es una auditoría del repositorio.

| Criterio | Nota | Evidencia |
| --- | --- | --- |
| Naming clarity | good | Casos de uso, puertos y roles coinciden con el vocabulario del dominio (`JD`, `TD`, `CC`, `EE`, `CAE_MEMBER`, `AUDIT_READONLY`). |
| Function size | fair | Los servicios de listado y detalle caben en una responsabilidad. `ProcessModuleConfig` concentra el cableado de procesos y plantillas en una sola clase. |
| Error handling | fair | 401 y 403 tienen cuerpo JSON estable. El detalle fuera de alcance es 404 a propósito. `ProcessController` sustituye un principal ilegible por un UUID aleatorio y convierte cualquier fallo de asignaciones en alcance vacío. |
| Test presence | fair | `ListProcessesServiceTest` cubre `JD`, `TD`, `CC` con una carrera y `CC` con alcance vacío. Existe `ProcessControllerQueryTest` (no leído línea a línea). No hay prueba de un agregado por modo porque el endpoint no existe. JaCoCo exige 90 % de línea solo en una lista de clases que no incluye este módulo. |
| Duplication | fair | La misma partición de roles está en `ProcessAccessPolicy.canAccess` y en `ListProcessesService.isCareerScoped`, y otra vez en `ProcessController.extractRole`. |
| Dead code | good | En los archivos leídos no hay ramas muertas ni bloques comentados en el flujo de listado. |

Señales concretas, una sola vez: paquete `persistance`; `ProcessListItem` sin `operationalMode`; `ARCHIVED` filtrado en memoria; secreto JWT de desarrollo con default en `application.yaml`; no hay workflows en `.github/`. El frontend declara OxLint y Vitest; no se ejecutó la suite.
