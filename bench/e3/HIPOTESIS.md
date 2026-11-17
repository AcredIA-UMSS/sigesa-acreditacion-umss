# Hipótesis E3 — notificaciones extraídas vs en proceso

Escritas **antes** de medir. Módulo: outbox de notificaciones (`notification_outbox`).

Rutas (GET):

- Usa el módulo: `GET /api/v1/e3/notifications`
- Control (no usa el módulo): `GET /api/health`

## H1 — overhead de red en régimen estable

Extraer el módulo (`EXTRACTION_MODE=http` vía Toxiproxy, sin toxic) sube el **p95** de `GET /api/v1/e3/notifications` en **≤ 15 ms** respecto a `EXTRACTION_MODE=in_process`, en la misma máquina y el mismo seed.

Falsación: mediana del p95 extraído − mediana del p95 in-process **> 15 ms**.

## H2 — aislamiento de falla

Durante la fase en que Toxiproxy corta o atrasa el sidecar, el **% de errores** de `GET /api/health` permanece **≤ 1 %**, mientras `GET /api/v1/e3/notifications` supera **50 %** de errores o su p95 supera **500 ms**.

Falsación: health se degrada al mismo tiempo que notificaciones (la falla se arrastra al monolito).

## H3 — coste operativo

El tiempo de `docker compose up` de la forma http (API + sidecar + Toxiproxy) es **≤ 2×** el de la forma in-process (solo API), mediana de ≥ 5 corridas.

Falsación: up http > 2× up in-process.

## Reversión (borrador para el ADR)

Si H1 se falsifica **y** H2 no se sostiene (health se cae con el sidecar), **no extraemos**. El número de reversión propuesto: p95 extraído − p95 in-process **> 15 ms** en mediana, o error rate de health **> 1 %** durante el corte.
