---
id: FSD-UC-036
nombre: Historial de accesos
estado: implementado
release: v1.0
actor_principal: "[JD]"
modulo: MOD-AUDIT
design_doc: DD-UC-036
ultima_actualizacion: "2026-11-13"
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

## Criterios de aceptación

```gherkin
# language: es
Característica: Historial de accesos para jefatura

  Escenario: El jefe de departamento entra al historial
    Dado un usuario con rol JD
    Cuando abre el panel
    Entonces ve «Historial de accesos»
    Y al elegirlo llega a /admin/ingresos

  Escenario: Otro rol no usa la pantalla
    Dado un usuario con rol TD, CC o EE
    Cuando abre /admin/ingresos
    Entonces no permanece en esa pantalla

  Escenario: Un ingreso correcto abre la sesión
    Dado un usuario con credenciales válidas
    Cuando inicia sesión
    Entonces queda una access_session en ACTIVE
    Y el JWT lleva el sid de esa sesión

  Escenario: Un ingreso fallido queda registrado
    Dado un intento de ingreso con credenciales inválidas
    Cuando el sistema rechaza el ingreso
    Entonces la sesión queda en FAILED

  Escenario: El cierre voluntario cierra la sesión
    Dado una sesión ACTIVE
    Cuando el usuario cierra sesión
    Entonces la sesión queda en CLOSED
    Y un segundo cierre no cambia ese primer cierre

  Escenario: Un JWT vencido expira la sesión
    Dado una sesión ACTIVE cuyo JWT venció
    Cuando el sistema detecta el vencimiento
    Entonces la sesión queda en EXPIRED

  Escenario: Jefatura filtra el historial
    Dado sesiones de distintos usuarios, roles, estados, IP y fechas
    Cuando [JD] aplica esos filtros en /admin/ingresos
    Entonces la lista muestra solo las sesiones que cumplen el filtro
    Y ve los totales de ingresos, sesiones activas, ingresos de hoy e intentos fallidos

  Escenario: La lista muestra las acciones de la sesión
    Dado una sesión con acciones de crear, modificar o eliminar en la bitácora
    Cuando [JD] ve esa fila
    Entonces la columna de acciones muestra esas etiquetas, sin repetirlas
    Y no usa esa columna para el ingreso ni la salida

  Escenario: El detalle muestra la bitácora de la sesión
    Dado una sesión con acciones en audit_log
    Cuando [JD] abre el detalle
    Entonces ve esas acciones asociadas por session_id
    Y el registro no incluye contraseñas ni tokens

  Escenario: La impresión respeta el filtro
    Dado un filtro aplicado en el historial
    Cuando [JD] imprime el reporte
    Entonces el reporte contiene las sesiones de ese filtro, con su columna de acciones

  Escenario: El historial no se modifica
    Dado un usuario con rol JD
    Cuando consulta el historial
    Entonces no puede crear, editar ni eliminar sesiones desde la pantalla ni por API

  Escenario: Un rol distinto no consulta el historial
    Dado un usuario que no es JD
    Cuando llama a /api/v1/access-sessions
    Entonces el sistema responde 403

  Escenario: La sesión no existe
    Dado un identificador de sesión que no está registrado
    Cuando [JD] pide su detalle o sus acciones
    Entonces el sistema responde 404
```

## Excepciones

| Condición | Respuesta |
| --- | --- |
| Rol distinto de [JD] | 403 |
| Sesión inexistente | 404 |
