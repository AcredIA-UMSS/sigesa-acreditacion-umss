---
id: FSD-UC-036
nombre: Historial de accesos
estado: implementado
release: v1.0
actor_principal: "[JD]"
modulo: MOD-AUDIT
design_doc: DD-UC-036
ultima_actualizacion: "2026-11-11"
---

# FSD-UC-036 — Historial de accesos

## Contexto

[JD] consulta quién ingresó, cuándo cerró sesión y qué acciones de la bitácora `audit_log` quedaron asociadas a esa sesión. No reemplaza FSD-UC-017.

## Flujo

1. Un ingreso correcto abre `access_session` en `ACTIVE` y el JWT lleva `sid`.
2. Un intento fallido queda en `FAILED`.
3. El cierre voluntario pasa a `CLOSED`. Un JWT vencido, a `EXPIRED`.
4. Las acciones ya auditadas y las operaciones API de esa sesión quedan en `audit_log.session_id`.
5. [JD] filtra el historial y abre el detalle. No hay alta, edición ni borrado por API.

## Excepciones

| Condición | Respuesta |
| --- | --- |
| Rol distinto de [JD] | 403 |
| Sesión inexistente | 404 |
