# SIGESA — near-real-time API performance (Grafana + k6 + OpenTelemetry)

Stack for **watching live endpoint performance** of the product API (not E3 extraction A/B).

```
k6 ──► backend (Micrometer + OTLP)
              │
              ├── scrape /actuator/prometheus ──► Prometheus (5s) ──► Grafana (refresh 5s)
              └── OTLP traces ──► Collector ──► Tempo ──► Grafana Explore
```

## Quick start

```bash
# 1) Start product + observability (rebuilds backend with actuator/OTEL)
./scripts/perf-observe.sh up

# 2) Open Grafana (dashboard auto-loads, 5s refresh)
#    http://localhost:3002  admin/admin

# 3) Generate steady load so panels move in near real-time
./scripts/perf-observe.sh k6
```

## What you see live

| Panel | Source | Cadence |
|---|---|---|
| Request rate by endpoint | Prometheus `http_server_requests_*` | scrape 5s / UI 5s |
| Latency p95 by endpoint | Prometheus histograms | scrape 5s / UI 5s |
| Error rate (5xx) | Prometheus | scrape 5s |
| JVM / Hikari | Prometheus | scrape 5s |
| Slow request waterfall | Tempo via Grafana Explore | OTLP batch ~1s |

## Endpoints exercised by k6

- `GET /api/health`
- `GET /api/v1/public/news`
- `GET /api/v1/public/news/featured`
- `GET /api/v1/public/programs`
- `POST /api/v1/auth/login` (setup) + `GET /api/v1/processes` (JWT)

Override load: `K6_VUS=20 K6_DURATION=5m ./scripts/perf-observe.sh k6`

## Ports

| Service | Host |
|---|---|
| Grafana | 3002 |
| Prometheus | 9090 |
| Tempo | 3200 |
| OTLP gRPC/HTTP | 4317 / 4318 |
| API | 8080 |

## Product stack + observability

`./scripts/perf-observe.sh up` starts the product stack (including `ollama` / `open-webui`) plus observability:

- Product: `db`, `backend`, `minio`, `smtp`, `ollama`, `open-webui` (+ `frontend` if in base compose)
- Observability: `otel-collector`, `tempo`, `prometheus`, `grafana`

The observability overlay still forces `SIGESA_ASSISTANT_ENABLED=false` during API perf runs so latency panels are not skewed by LLM traffic; AI containers remain part of the environment for normal product use.

## Backend wiring

- Deps: `spring-boot-starter-actuator`, `spring-boot-starter-opentelemetry`, `micrometer-registry-prometheus`
- Security: `/actuator/health`, `/actuator/prometheus`, `/actuator/info` permitAll
- Compose overlay enables OTLP export to the collector when the `observability` profile is used
- Overlay forces assistant OFF (`SIGESA_ASSISTANT_ENABLED=false`) so the API does not wait on LLM containers

## Stop

```bash
./scripts/perf-observe.sh down        # stop obs containers
./scripts/perf-observe.sh down --all  # tear down obs volumes/services
```

> Port conflicts: if something else binds `:3002` / `:4318` / `:3200`, stop it before `perf-observe.sh up`.
