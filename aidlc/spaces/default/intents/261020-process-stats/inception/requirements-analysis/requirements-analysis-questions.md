# Requirements Analysis Questions

## Question 1

El listado `GET /api/v1/processes` arma la carrera del usuario con asignaciones activas en base de datos. El freeze lee el claim `programScope` del JWT. El pedido dice «procesos visibles al JWT» y, para `CC`, `EE` y `CAE_MEMBER`, «solo su carrera». Esas dos fuentes pueden divergir mientras el token siga vigente.

¿Qué alcance usa `GET /api/v1/processes/stats`?

A. El del listado vigente: asignaciones activas en base de datos, para que los conteos coincidan con `GET /api/v1/processes`
B. El claim `programScope` del JWT, igual que el freeze
X. Other (please specify)

[Answer]: A

## Question 2

Alcance vacío debe devolver ceros. Falta la forma de `byStatus` y `byOperationalMode` cuando un estado o un modo no tiene procesos. El listado vigente no incluye procesos `ARCHIVED`.

¿Qué JSON devuelve el 200, incluido el alcance vacío?

A. Siempre las mismas claves, con 0 si no hay: `ACTIVE` y `CLOSED` en `byStatus`; `ACTIVE`, `AUDIT_READONLY` e `IMPROVEMENT_MONITORING` en `byOperationalMode`. `total` y `frozenCount` en 0 si no hay procesos. `ARCHIVED` no entra en el conteo
B. Solo las claves con conteo mayor que cero. Alcance vacío: `byStatus` y `byOperationalMode` como objetos vacíos, `total` 0 y `frozenCount` 0
X. Other (please specify)

[Answer]: A
