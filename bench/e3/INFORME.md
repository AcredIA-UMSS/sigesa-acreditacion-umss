# Informe E3 — corrida limpia A/B/A/B

Medición de validez del 02/11/2026, 21:32–21:39 (UTC-4). Integrante: Marlene Velasquez. Host: `MacBook-Pro-de-Marlene.local`, macOS 26.5.2 (25F84), Darwin 25.5.0 arm64. Docker 29.6.2, Compose v5.3.1, k6 `grafana/k6:0.57.0`, Toxiproxy 2.9.0.

Esta corrida es la que respalda el veredicto. La anterior (`bench/e3/resultados/h1-*.json`, fuera de `limpio/`) se hizo con `spring-boot:run` y Open WebUI/Ollama en marcha y no se usa.

## Control de ejecución

Antes de cargar k6 se detuvo:

- `spring-boot:run` del repo (Maven PID 72731).
- Contenedores `open-webui` y `hardcore_brahmagupta` (Open WebUI + Ollama, ~1.8 GiB).

Siguieron en marcha, ociosos: `sigesa-postgres-db` (0.01 % CPU) y un nginx ajeno (`strange_cerf`, 0 % CPU). No se puede apagar Cursor, Chrome, WindowServer ni la VM de Docker: el banco corre dentro de esa VM.

Load average (1 / 5 / 15 min): 6.08 / 5.00 / 5.21 al empezar, 5.44 / 4.92 / 5.18 quince segundos después de parar esos procesos, 7.05 / 7.06 / 6.30 al terminar. El promedio de 1 minuto no llegó a bajar de 5: arrastra la carga previa y, al final, la de los tres JVM del banco. La secuencia A/B/A/B reparte ese ruido entre las dos formas.

Crudos: `bench/e3/resultados/limpio/`.

## Hipótesis (escritas antes de medir)

Copiadas de [HIPOTESIS.md](HIPOTESIS.md). No se reescribieron con el resultado.

| Id | Predicción | Falsación |
| --- | --- | --- |
| H1 | p95 HTTP − p95 in-process ≤ 15 ms, Toxiproxy sin toxic | mediana de esa diferencia > 15 ms |
| H2 | con el sidecar cortado, error de health ≤ 1 % y notificaciones > 50 % de error o p95 > 500 ms | health se degrada junto con notificaciones |
| H3 | mediana de `docker compose up` HTTP ≤ 2× in-process | HTTP > 2× |

Número de reversión del borrador: diferencia de p95 > 15 ms **o** error de health > 1 % durante el corte.

## Banco

Imagen `sigesa-backend:e3`. H2 en memoria, outbox vacío en las dos formas: `pendingCount: 0`, `totalCount: 0`. k6, 10 VU, 20 s, timeout de cliente 5 s. Una petición **colgada** es status 0: k6 no obtuvo respuesta HTTP (dial timeout o corte a los 5 s). Warmup de 10 s por forma, descartado. Orden A, B, A, B, cinco veces.

| Forma | Procesos |
| --- | --- |
| A in-process | API en `:18080` |
| B HTTP | API `:18082` + sidecar `:18081` + Toxiproxy `:18474`, sin toxics |

## Matriz — notificaciones

Percentiles en milisegundos, solo `GET /api/v1/e3/notifications`. Error = status distinto de 200. Colgadas = status 0.

| Orden | Forma | p50 | p95 | p99 | máx | errores | colgadas |
| --- | --- | ---: | ---: | ---: | ---: | ---: | ---: |
| 1 | in-process | 1.192 | 2.334 | 4.677 | 88.506 | 19/34063 | 19 |
| 2 | HTTP | 2.132 | 3.950 | 7.929 | 115.070 | 13/26015 | 13 |
| 3 | in-process | 1.062 | 1.639 | 2.486 | 47.233 | 10/39620 | 10 |
| 4 | HTTP | 2.133 | 3.848 | 7.356 | 96.352 | 8/42716 | 8 |
| 5 | in-process | 1.087 | 1.702 | 2.438 | 25.084 | 20/26770 | 20 |
| 6 | HTTP | 2.107 | 3.828 | 6.815 | 71.940 | 10/27095 | 10 |
| 7 | in-process | 1.097 | 1.848 | 3.502 | 77.885 | 10/39605 | 10 |
| 8 | HTTP | 2.082 | 3.611 | 6.494 | 72.804 | 11/30011 | 11 |
| 9 | in-process | 0.782 | 1.589 | 2.548 | 22.591 | 19/13520 | 19 |
| 10 | HTTP | 2.124 | 3.737 | 6.357 | 106.483 | 10/26861 | 10 |

| Percentil | mediana in-process | rango in-process | mediana HTTP | rango HTTP | HTTP − in-process |
| --- | ---: | --- | ---: | --- | ---: |
| p50 | 1.087 | 0.782 – 1.192 | 2.124 | 2.082 – 2.133 | 1.037 |
| p95 | 1.702 | 1.589 – 2.334 | 3.828 | 3.611 – 3.950 | **2.126** |
| p99 | 2.548 | 2.438 – 4.677 | 6.815 | 6.357 – 7.929 | 4.267 |

En régimen estable cada error de notificaciones fue una petición colgada. No hubo HTTP 4xx/5xx. Health, en la misma corrida, se quedó entre 0 y 12 colgadas (máximo 12/26770). El p99 de `http_req_blocked` estuvo entre 0.661 y 0.790 ms: no había una cola de conexiones esperando.

H1 se sostiene: 2.126 ms frente a 15 ms.

## H2 — corte

Proxy `notify` con `enabled: false`. La forma in-process corrió en paralelo y no usa ese proxy.

| Corrida | p50 | p95 | p99 | errores | colgadas |
| --- | ---: | ---: | ---: | ---: | ---: |
| HTTP, notificaciones | 1.970 | 5.106 | 11.128 | 2496/2496 (100 %) | 7 |
| HTTP, health | 1.964 | 4.805 | 9.474 | 33/2496 (1.322 %) | 33 |
| in-process, notificaciones | 1.151 | 1.825 | 3.011 | 10/57495 (0.017 %) | 10 |
| in-process, health | 1.104 | 1.717 | 2.777 | 8/57495 (0.014 %) | 8 |

De las 2496 notificaciones fallidas, 2489 recibieron respuesta HTTP (503 del adaptador) y 7 se colgaron. Las 33 de health no recibieron status: son colgadas, no un 500 del controller. Al reabrir el proxy, notificaciones volvió a 200 con `origin: "http"`.

H2 no se sostiene. El error de health quedó en 1.322 %, por encima del 1 %. El p95 de notificaciones (5.106 ms) no llegó a 500 ms; la cláusula que sí se cumple es el 100 % de error del módulo. La falla no se queda ahí: health del mismo proceso pierde peticiones.

## H3 — arranque

Reloj desde `docker compose up -d` hasta health 200. En HTTP, también hasta el status del sidecar y el alta del proxy. Las diez corridas `ok`.

| Orden | Forma | segundos |
| --- | --- | ---: |
| 1 | in-process | 7.678 |
| 2 | HTTP | 9.262 |
| 3 | in-process | 7.660 |
| 4 | HTTP | 9.168 |
| 5 | in-process | 7.629 |
| 6 | HTTP | 9.213 |
| 7 | in-process | 7.600 |
| 8 | HTTP | 10.378 |
| 9 | in-process | 7.661 |
| 10 | HTTP | 9.133 |

Mediana in-process 7.660 s (rango 7.600–7.678). Mediana HTTP 9.213 s (rango 9.133–10.378). Razón 1.20×. H3 se sostiene.

## Amenazas a la validez

- Load average de 1 minuto nunca bajó de 5. Cursor, WindowServer y la VM de Docker compartían la CPU. A/B/A/B reparte ese ruido; no lo elimina.
- k6 corre en otro contenedor y entra por `host.docker.internal`, no por la red de Compose. El hop medido incluye el bridge de Docker más ese salto al host.
- Las dos formas no comparten base: cada una tiene un H2 vacío. El p95 no dice nada de un outbox con filas en PostgreSQL.
- El corte es apagar el proxy, no un toxic de latencia. El circuit breaker (5 fallos, 2 s abierto) convierte la caída en 503 rápido, por eso el p95 del corte es 5 ms y no 500.
- Unas 8–20 peticiones colgadas por corrida aparecen también en régimen estable, en las dos formas. El piso de error del cliente no es cero.
- `gracefulStop` escrito en el script no lo acepta k6 0.57 (aviso `unknown field`). Aplicó el cierre por defecto de 30 s.

## Veredicto

H1 y H3 caben en el presupuesto. H2 no: con el sidecar cortado, health pierde 33/2496 (1.322 %). El número de reversión es un **o**. Se cruza. **El módulo se queda en el monolito** (`EXTRACTION_MODE=in_process`).

La frase del borrador que pide las dos falsaciones a la vez (H1 y H2) no se cumple, porque H1 aguanta. Manda el número: error de health > 1 % basta para no extraer. Decisión y condición para reabrirla: [ADR-001](../../docs/adr/ADR-001-extraer-modulo.md).
