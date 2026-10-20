# API Documentation

Superficie HTTP: Spring MVC, prefijo `/api/v1`, JSON, sesión stateless. Autenticación: `Authorization: Bearer`. Sin token válido, 401 `{ "error": "UNAUTHORIZED", "message": "No autenticado" }`. Rol rechazado por la cadena o por `@PreAuthorize`, 403 `{ "error": "ACCESS_DENIED", "message": "No autorizado para esta operación" }`.

No hay límite de tasa en los mapeos leídos.

## Procesos

`GET /api/v1/processes/stats` no está declarado. La búsqueda en el backend no encontró `processes/stats`, `GetMapping("/stats")` ni `ProcessStats`.

Controlador `ProcessController`, base `/api/v1/processes`. Orden de declaración:

| Método y ruta | Autorización | Éxito | Notas |
| --- | --- | --- | --- |
| `POST /api/v1/processes` | `JD` | 201 `ProcessResponseDto` | Cuerpo: carrera y plantilla. 409 si la carrera ya tiene proceso activo. |
| `GET /api/v1/processes` | `JD`, `TD`, `CC`, `EE`, `CAE_MEMBER` | 200 lista de `ProcessSummaryResponseDto` | Visibilidad en `ListProcessesService`. Alcance de carrera vacío: `[]`. |
| `GET /api/v1/processes/{processId}` | mismos cinco roles | 200 detalle | `processId` es `UUID`. Fuera de alcance o inexistente: 404 `PROCESS_NOT_FOUND`. El DTO de detalle incluye `operationalMode`. |
| `DELETE /api/v1/processes/{processId}` | `JD` | 204 | Archiva. 409 si hay evidencia o el estado no se puede borrar. |

`ProcessSummaryResponseDto` expone id, carrera, plantilla, `status`, fechas y conteos. No expone `operationalMode`.

Como `/{processId}` es el único `GET` de un segmento en ese controlador, la ruta literal `/stats` tiene que declararse aparte. Los hermanos ya ocupan sufijos más largos (`/freeze`, `/responsible`, `/stages`), así que no ocupan el segmento `stats`.

`ProcessResponsibleController` (misma base, solo `JD`):

- `PUT /api/v1/processes/{processId}/responsible`
- `DELETE /api/v1/processes/{processId}/responsible`
- `GET /api/v1/processes/{processId}/responsible/candidates`

`ProcessFreezeController`, base `/api/v1/processes/{processId}/freeze`:

- `POST` — `TD` — congela en `AUDIT_READONLY`. El alcance sale del claim JWT.
- `POST /lift` — `TD` — pasa a `ACTIVE` o `IMPROVEMENT_MONITORING`.
- `GET` — `CC`, `TD`, `JD`, `EE`, `CAE_MEMBER`.

Otras rutas bajo el mismo prefijo de proceso, vistas por el mapeo de clase (sin leer cada método): etapas, entregables, compuerta, checklist de sala de pares, lotes de encuesta, plan de mejora, delegaciones, métricas y nodos de nivel 1. Viven en controladores distintos de `ProcessController`.

## Resto de la superficie (base)

Auth `POST /api/v1/auth/login` (público). Usuarios ` /api/v1/admin/users` (`JD` en la cadena). Programas `GET /api/v1/programs`. Plantillas `/api/v1/templates` y árbol normativo de plantilla. Indicadores, evidencias y activos documentales bajo `/api/v1/indicators`, `/api/v1/evidences` y `/api/v1/document-assets`. Dashboards `/api/v1/dashboards`, bandeja `/api/v1/dashboards/technician/inbox`, reportes `/api/v1/reports/**` (`JD`), auditoría `/api/v1/audit/logs` (`JD`), asistente `/api/v1/assistant`.

Swagger: `/swagger-ui/**` y `/v3/api-docs/**`, públicos.
