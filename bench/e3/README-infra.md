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

Scripts: `chmod +x bench/e3/*.sh` (Git Bash / WSL / Linux). Healthchecks: `wget` en la JRE Alpine (no hay Python); Toxiproxy 2.9.0 es `scratch` → `/toxiproxy-cli list`.
