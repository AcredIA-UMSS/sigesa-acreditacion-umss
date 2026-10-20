# Objetivo de servicio — resumen de procesos

La pregunta 1 quedó en A: no hay un objetivo numérico para `GET /api/v1/processes/stats`. Los requisitos no fijan disponibilidad ni latencia, y no hay una línea base medida.

## Indicador que sí existe

La comprobación es binaria y manual, no un porcentaje en una ventana:

- Sin token, la respuesta es 401 con `error` igual a `UNAUTHORIZED`.
- Con un rol `JD`, `TD`, `CC`, `EE` o `CAE_MEMBER`, la respuesta es 200 y el JSON trae `total`, `byStatus`, `byOperationalMode` y `frozenCount`.

Eso es lo que ya ejecutó `operation/deployment-execution/smoke-test-results.md`. No se convierte en un 99,9 % ni en un presupuesto de error.

## Lo que no se fija

No hay SLO, SLA ni ventana de 30 días. No hay umbral de latencia. Inventar uno contradice la respuesta A.
