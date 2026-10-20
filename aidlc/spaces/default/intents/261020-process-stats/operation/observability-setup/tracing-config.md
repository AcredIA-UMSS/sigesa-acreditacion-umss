# Trazas — resumen de procesos

El backend desplegado no instrumenta trazas. No hay OpenTelemetry ni X-Ray en `backend/pom.xml` ni en la configuración de Spring.

## Qué se puede seguir hoy

Una llamada a `GET /api/v1/processes/stats` se sigue por el código HTTP de la respuesta y por `docker logs sigesa-backend`. No hay un identificador de traza que cruce el filtro JWT, el controlador y la consulta a Postgres.

## Lo que no se instala

No se añade un agente de trazas ni un mapa de servicios. El alcance de este cambio no pidió instrumentación, y no hay un diseño de NFR que la exija.
