---
id: DD-UC-017
titulo: "Bitácora de auditoría (MOD-AUDIT) v1"
producto: "SIGESA"
grupo: "ACREDIA"
fsd_uc:
  - "FSD-UC-017"
prd_refs:
  - "PRD-REQ-018"
  - "PRD-US-022"
adrs:
  - "ADR-0005"
prompts: []
release: "v1.0"
status: aprobado
fecha: "29/10/2026"
autores:
  - "Cursor Agent"
---

# Design Doc `DD-UC-017` — Bitácora de auditoría v1

> **Qué es**: diseño de la primera versión de backend de **FSD-UC-017**. Persiste eventos de login y de carga de evidencia, y permite a [JD] consultarlos.
>
> **Relación con otros documentos**:
> - **Trazabilidad**: [`FSD-UC-017`](../product/uc/FSD-UC-017.md).
> - Sustituye el stub `NoOpAuditLogAdapter` citado en [`DD-UC-001`](./DD-UC-001.md) y [`DD-UC-002`](./DD-UC-002.md).
> - La tabla se llama `audit_log`, como en [`modelo_datos.md`](../product/modelo_datos.md). No usa el nombre `LOG_AUDITORIA` ni el rol `sigesa_app` del ADR histórico: esos nombres no existen en el código ni en la configuración de datasource (`sigesa` / `sigesa_user`).

## 1. Objetivo y alcance v1

| Incluido | Excluido |
|---|---|
| Login exitoso y fallido (`LOGIN` + `SUCCESS`/`FAILURE`) | Reemplazo, borrado, descarga, validación y rechazo de evidencia (no hay caso de uso) |
| Cierre de sesión (`POST /api/v1/auth/logout` → `LOGOUT`) y JWT vencido (`SESSION_EXPIRED`) | Cambio de rol posterior al alta (no hay caso de uso; el alta guarda el rol en `payload`) |
| Carga de evidencia con indicador, proceso, criterio y nombre de archivo | |
| Alta, activación, desactivación y solicitud de reporte | |
| Consulta con varias `action` y `GET /api/v1/audit/logs/export` (tope 1000, sin `payload`) | |
| `GET /api/v1/audit/logs` y `GET /api/v1/audit/logs/{id}` solo [JD] | |
| Tabla `audit_log` append-only en aplicación; trigger PostgreSQL contra `UPDATE`/`DELETE` | Inmutabilidad ante un superusuario de base de datos |

El catálogo reserva `EVIDENCE_REPLACED`, `EVIDENCE_DELETED` y `EVIDENCE_UPDATED` para cuando existan esas operaciones. Esta versión no las escribe.

## 2. Diseño

Arquitectura hexagonal existente. El dominio no depende de Spring ni JPA. El actor es el `userId` del caso de uso o el `subject` del JWT. No se acepta un id de actor enviado por el cliente.

| Capa | Componentes |
|---|---|
| Dominio | `AuditEvent`, `AuditLogQuery`, `AuditAction`, `AuditResult`, `AuditResourceType`, `AuditActorEmail` |
| Aplicación | `GetAuditLogsUseCase` / `GetAuditLogsService`; `AuditLogPort`, `AuditLogQueryPort` |
| Adaptador out | `AuditLogJpaAdapter`, entidad `audit_log` |
| Adaptador in | `AuditLogController`, `SecurityConfig` (`hasRole('JD')`) |

### Campos

`id` UUID, `actor_id` (nulo si el login fallido no identifica usuario), `actor_email` (snapshot, máx. 150), `action`, `resource_type`, `resource_id`, `description` (máx. 500), `occurred_at` UTC, `result`, `ip_address`, `payload` JSON.

No se guardan contraseñas, hashes, JWT ni el archivo. El correo de un intento fallido solo se conserva si es un `@umss.edu.bo` válido de hasta 150 caracteres; en caso contrario el actor queda vacío. La IP sale de `request.getRemoteAddr()` cuando hay petición HTTP. No se leen `X-Forwarded-For`.

La respuesta convierte `occurred_at` a `America/La_Paz`. El valor almacenado sigue en UTC.

### Transacciones

- Evento de éxito: el insert participa en la transacción de la operación (`REQUIRED`). Si no se puede persistir, la operación no queda confirmada. En la carga, `UploadEvidenceService.upload` es transaccional junto con `persistUpload`; si el audit falla, se borra el blob y se relanza.
- Login fallido: `logLoginFailure` usa `REQUIRES_NEW`, así la fila permanece aunque el caso de uso lance `InvalidCredentialsException`.

### Consulta

Filtros: `actorId`, `actorEmail`, `action` (una, varias repetidas o separadas por comas), `resourceType`, `resourceId`, `result`, `from`, `to`. Paginación Spring, tope 100, orden `occurredAt` descendente. `GET /export` usa los mismos filtros y corta en 1000 filas, sin `payload`. `from` posterior a `to`, o un valor de filtro desconocido, → 422. Sin token → 401. Otro rol → 403. No hay `PUT`, `PATCH` ni `DELETE`.

### Integridad

El puerto no ofrece actualización ni borrado. En PostgreSQL (Flyway `V8__audit_log.sql`, perfil prod) un trigger rechaza `UPDATE` y `DELETE`. En H2/dev la tabla la crea Hibernate y la protección es la de la aplicación. Eso no impide que un superusuario quite el trigger.

## 3. Pruebas

- Login exitoso: actor, acción y hora UTC.
- Login fallido: `FAILURE` y ningún secreto en la fila.
- Carga de evidencia: id de evidencia e indicador.
- [CC] recibe 403; [JD] filtra y pagina; no hay verbo de modificación.
- Un fallo dentro de la transacción de éxito revierte la fila; el fallo de login con `REQUIRES_NEW` se conserva.
