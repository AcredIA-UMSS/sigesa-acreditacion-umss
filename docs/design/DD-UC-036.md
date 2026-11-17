---
id: DD-UC-036
titulo: Historial de accesos
fsd_uc:
  - FSD-UC-036
status: implementado
ultima_actualizacion: "2026-11-11"
---

# DD-UC-036 — Historial de accesos

La bitácora `audit_log` sigue siendo el registro de acciones. `access_session` agrupa el ingreso, la salida y el identificador que el JWT expone como `sid`.

| Pieza | Rol |
| --- | --- |
| `access_session` | Ingreso, IP, agente, estado, fin |
| `audit_log.session_id` | Acciones de esa sesión, sin contraseñas ni tokens |
| `GET /api/v1/access-sessions` | Consulta [JD]: lista, resumen, detalle y acciones |
| `POST /api/v1/auth/logout` | Cierra la sesión del token |

No hay endpoints de modificación. Cerrar dos veces la misma sesión activa no cambia el primer cierre. Desactivar un usuario revoca sus sesiones `ACTIVE`.
