# E3 — infraestructura (monolito vs servicio extraído)

Compose de **experimento**. No sustituye el `docker-compose.yml` de producto en la raíz.

## Puertos (host)

| Servicio | Host | Contenedor | Notas |
|---|---|---|---|
| `api` | `8080` (`E3_API_HOST_PORT`) | `8080` | Único puerto de aplicación publicado |
| `toxiproxy` API | `8474` (`E3_TOXIPROXY_API_PORT`) | `8474` | Control de toxics |
| `modulo` | — | `8080` (`E3_MODULE_PORT`) | Solo `expose`. La API habla con `toxiproxy:8666` |
| proxy `modulo` | no publicado | `8666` | Listen Toxiproxy → `modulo:8080` |

## Nombres

| Compose | Imagen | Rol |
|---|---|---|
| `api` | `sigesa-e3:local` (`backend/Dockerfile`) | `SIGESA_APP=api` |
| `modulo` | misma imagen | `SIGESA_APP=notification-service` |
| `toxiproxy` | `ghcr.io/shopify/toxiproxy:2.9.0` | Falla de red |
| `toxiproxy-setup` | `curlimages/curl:8.12.1` | Crea proxy `modulo` (idempotente) |
| `k6` | `grafana/k6:0.54.0` | Profile `bench` |

Red única: `sigesa-e3`. Proyecto Compose: `sigesa-e3`.

Modo: `EXTRACTION_MODE=http|in_process`. URL del módulo: `SIGESA_NOTIFICATION_BASE_URL` → **siempre** `http://toxiproxy:8666` en forma HTTP (nunca `http://modulo:...`).

Sin servicio `db`: perfil default de Spring usa H2 en memoria.

## Uso

```bash
# forma HTTP (API + sidecar + Toxiproxy)
./bench/e3/run-limpio.sh http

# forma monolito (solo API, in-process)
./bench/e3/run-limpio.sh in_process

# toxics
./bench/e3/toxic.sh lento on
./bench/e3/toxic.sh colgado on
./bench/e3/toxic.sh caido on
./bench/e3/toxic.sh reset
./bench/e3/toxic.sh status

# comprobar infra
./bench/e3/verificar-infra.sh
```

Rutas: módulo `GET /api/v1/e3/notifications` · control `GET /api/health`.

k6 (opcional): `docker compose -f bench/e3/docker-compose.yml --profile bench run --rm k6 run /scripts/<script.js>`

Variables: raíz `.env` / `.env.example` (`E3_*`). Timeouts E3 ≥ 5 s para que el toxic `lento` (2 s) sea medible.

Scripts: `chmod +x bench/e3/*.sh` (Git Bash / WSL / Linux). Healthchecks: `wget` en la JRE Alpine (no hay Python); Toxiproxy 2.9.0 es `scratch` → `/toxiproxy-cli list`. `start_period` api/modulo: **120 s**. `run-limpio.sh` usa `--wait-timeout 300`.

## Timeouts y reintentos efectivos (forma HTTP)

Valores inyectados en `api` (no los defaults 200/800 del adaptador):

| Variable | Valor | Efecto |
|---|---|---|
| `E3_NOTIFICATION_TIMEOUT_MS` → `SIGESA_NOTIFICATION_TIMEOUT_MS` | **5000** | timeout por intento (connect + request) |
| `E3_NOTIFICATION_DEADLINE_MS` → `SIGESA_NOTIFICATION_DEADLINE_MS` | **5000** | presupuesto total de `inspect()` |
| `E3_NOTIFICATION_RETRIES` → `SIGESA_NOTIFICATION_RETRIES` | **1** | `maxAttempts = 1` (un solo intento) |
| `E3_NOTIFICATION_RETRY_BACKOFF_MS` | **50** | no aplica con 1 intento |

`api` y `modulo` comparten `JAVA_OPTS`/`JAVA_TOOL_OPTIONS=-Xms256m -Xmx256m`, `mem_limit=512m`, `cpus=1.0`.

## Tiempos de fallo por tóxico (medidos con `verificar-infra.sh`)

Ruta: `GET /api/v1/e3/notifications`. Control `GET /api/health` = 200 en todos los casos.

| Tóxico | Qué hace Toxiproxy | Resultado medido |
|---|---|---|
| `lento` | latency downstream 2000 ms, jitter 0 | **~2 s**, HTTP **200** (timeout 5 s > latencia) |
| `colgado` | timeout downstream, timeout 0 (cuelga) | `curl -m 5` → **rc 28** a los **5006 ms** |
| `caido` | proxy `enabled: false` | HTTP **503** en **0 s** |

Tras `reset`: módulo vuelve a 200.

## Tiempos de build y up (`bench/e3/resultados/tiempos.csv`)

Corridas con cache de imagen (Maven ya empaquetado):

| forma | fecha (UTC) | build_s | up_s |
|---|---|---|---|
| http | 2026-11-03T01:52:27Z | 1 | 45 |
| in_process | 2026-11-03T01:53:18Z | 2 | 25 |

El `package` Maven en Docker (cuando cambió código) tardó **~10 s**. El primer `up` HTTP exitoso esperó health de modulo + api (~45 s).
