# Requisitos — resumen de procesos

## Análisis de la intención

Quien ya puede ver procesos de acreditación necesita un conteo, no la lista. `GET /api/v1/processes/stats` resume los procesos no archivados que ese usuario vería en `GET /api/v1/processes`: cuántos hay, cómo se reparten por estado y por modo operativo, y cuántos están congelados en auditoría (`AUDIT_READONLY`).

El pedido original está en `project-description.json`. Las dos decisiones de esta etapa están en `requirements-analysis-questions.md`: alcance por asignaciones activas en base de datos (pregunta 1, A) y mapas siempre con las mismas claves, en cero si no hay procesos (pregunta 2, A).

## Requisitos funcionales

### FR1 — Consulta del resumen

El sistema debe exponer `GET /api/v1/processes/stats` y responder 200 con un JSON de cuatro campos: `total`, `byStatus`, `byOperationalMode` y `frozenCount`.

- **FR1.1** La ruta literal `/api/v1/processes/stats` se declara antes de `GET /api/v1/processes/{processId}`, para que `stats` no se interprete como UUID.
- **FR1.2** `total` es un entero ≥ 0: la cantidad de procesos de la población (FR2).
- **FR1.3** `byStatus` es un objeto. Siempre incluye las claves `ACTIVE` y `CLOSED`. El valor de cada clave es un entero ≥ 0. Si no hay procesos de ese estado, el valor es 0. No se omite la clave.
- **FR1.4** `byOperationalMode` es un objeto. Siempre incluye las claves `ACTIVE`, `AUDIT_READONLY` e `IMPROVEMENT_MONITORING`. El valor de cada clave es un entero ≥ 0. Si no hay procesos de ese modo, el valor es 0. No se omite la clave.
- **FR1.5** `frozenCount` es un entero ≥ 0 e igual al valor de `byOperationalMode.AUDIT_READONLY`.
- **FR1.6** Sobre la misma población: `total` = suma de `byStatus` = suma de `byOperationalMode`.

Given un usuario `JD` y dos procesos no archivados, uno `ACTIVE`/`ACTIVE` y otro `CLOSED`/`AUDIT_READONLY`
When llama a `GET /api/v1/processes/stats` con un token válido
Then la respuesta es 200
And `total` es 2
And `byStatus` es `{ "ACTIVE": 1, "CLOSED": 1 }`
And `byOperationalMode` es `{ "ACTIVE": 1, "AUDIT_READONLY": 1, "IMPROVEMENT_MONITORING": 0 }`
And `frozenCount` es 1

### FR2 — Población y alcance

El sistema debe contar solo los procesos no archivados que el usuario vería en el listado vigente.

- **FR2.1** Un proceso con estado `ARCHIVED` no entra en `total`, ni en `byStatus`, ni en `byOperationalMode`, ni en `frozenCount`. `byStatus` no tiene clave `ARCHIVED`.
- **FR2.2** `JD` y `TD` cuentan los procesos no archivados de todas las carreras.
- **FR2.3** `CC`, `EE` y `CAE_MEMBER` cuentan solo los procesos no archivados de las carreras de su asignación activa en base de datos. La fuente es la misma que `GET /api/v1/processes`. No se usa el claim `programScope` del JWT.
- **FR2.4** Si `CC`, `EE` o `CAE_MEMBER` no tiene carreras en esa asignación (nula o vacía), la respuesta es 200 con ceros: `total` 0, `frozenCount` 0, y los objetos de FR1.3 y FR1.4 con todas las claves en 0. No es 404.

Given un `CC` cuya asignación activa solo incluye la carrera A, y existen un proceso no archivado en A y otro en B
When pide el resumen
Then `total` es 1 y el proceso de B no cuenta

Given un `CC` sin asignaciones activas de carrera
When pide el resumen
Then la respuesta es 200 y todos los conteos son 0, con las claves de FR1.3 y FR1.4 presentes

### FR3 — Autenticación y rol

El sistema debe rechazar la llamada antes de armar el resumen cuando no hay sesión válida o el rol no está autorizado. El cuerpo de error es el de la cadena actual.

- **FR3.1** Sin token, o con un token que el filtro JWT no acepta, la respuesta es 401 `{ "error": "UNAUTHORIZED", "message": "No autenticado" }`.
- **FR3.2** Con token válido y un rol distinto de `JD`, `TD`, `CC`, `EE` y `CAE_MEMBER`, la respuesta es 403 `{ "error": "ACCESS_DENIED", "message": "No autorizado para esta operación" }`.
- **FR3.3** Un proceso fuera de la carrera del usuario no produce 404. Se excluye del conteo (FR2.3, FR2.4).

Given una llamada a `GET /api/v1/processes/stats` sin cabecera `Authorization`
When la cadena de seguridad la evalúa
Then la respuesta es 401 con `error` igual a `UNAUTHORIZED`

## Requisitos no funcionales

- **NFR1** El endpoint no es público. Usa la cadena JWT stateless ya aplicada a `/api/v1/**`. No abre otro mecanismo de sesión.
- **NFR2** La población contada es la del listado vigente: no archivados, y la carrera sale de las asignaciones activas en base de datos. El resumen no debe contar un proceso que ese mismo usuario no recibiría en `GET /api/v1/processes`.
- **NFR3** Hay pruebas Maven del caso de uso (visibilidad por rol, exclusión de `ARCHIVED`, `frozenCount` y el cuerpo en ceros) y una prueba WebMvc del 200 y del 401.

## Restricciones

- Java 21 y Spring Boot. Arquitectura hexagonal: el caso de uso entra por un puerto; el controlador no recibe ni devuelve la entidad JPA. La respuesta HTTP es un DTO.
- La ruta literal queda declarada antes de `/{processId}` en el controlador de procesos.
- No hay frontend. No se modifica `docs/baseline/`.

## Supuestos

- Un token inválido recibe el mismo 401 que la ausencia de token, porque hoy ambos caen en `RestAuthenticationEntryPoint`.
- El JWT sigue emitiendo un solo rol. Si llegaran varias autoridades, la prioridad es la del controlador actual: `JD`, luego `TD`, `CC`, `EE`, `CAE_MEMBER`.
- «Asignación activa» es la misma lectura que ya usa `ProcessController` para armar el listado.
- `byStatus` y `byOperationalMode` son objetos JSON, no listas.
- Estado y modo operativo son independientes: un proceso `CLOSED` y `AUDIT_READONLY` suma uno en `CLOSED` y uno en `AUDIT_READONLY`, y uno en `total`.

## Fuera de alcance

- Pantallas, cliente Orval y cualquier cambio de frontend.
- Cualquier archivo bajo `docs/baseline/`.
- Incluir `operationalMode` en el ítem del listado.
- Unificar el alcance del freeze (claim `programScope` del JWT) con el del listado.
- Contar procesos `ARCHIVED`.
- Filtros por query, rango de fechas o paginación.
- Roles nuevos.

## Preguntas abiertas

Ninguna. El alcance y la forma del 200 quedaron cerrados en las preguntas 1 y 2.
