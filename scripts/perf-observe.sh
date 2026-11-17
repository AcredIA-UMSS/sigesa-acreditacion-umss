#!/usr/bin/env bash
# Near-real-time SIGESA API performance stack (Grafana + Prometheus + Tempo + OTEL + k6)
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
COMPOSE=(docker compose -f docker-compose.yml -f docker-compose.observability.yml)

usage() {
  cat <<EOF
Usage: $0 <up|down|rebuild|k6|status|urls>

  up       Start product stack + observability profile
  down     Stop observability services (keeps product stack unless --all)
  rebuild  Rebuild backend with OTEL deps and restart observability
  k6       Run k6 steady load against backend (generates live Grafana traffic)
  status   Show container health / key URLs
  urls     Print Grafana / Prometheus / Tempo / API URLs
EOF
}

cmd="${1:-}"
case "$cmd" in
  up)
    "${COMPOSE[@]}" --profile observability up -d --build
    "$0" urls
    ;;
  down)
    if [[ "${2:-}" == "--all" ]]; then
      "${COMPOSE[@]}" --profile observability down
    else
      "${COMPOSE[@]}" --profile observability stop grafana prometheus tempo otel-collector || true
    fi
    ;;
  rebuild)
    "${COMPOSE[@]}" --profile observability up -d --build backend otel-collector tempo prometheus grafana
    "$0" urls
    ;;
  k6)
    "${COMPOSE[@]}" --profile observability --profile bench run --rm k6 run /scripts/k6-api-perf.js
    ;;
  status)
    docker ps --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}' | grep -E 'sigesa|NAMES|grafana|prometheus|tempo|otel|k6' || true
    curl -sS -m 3 -o /dev/null -w 'API health: %{http_code}\n' http://localhost:8080/api/health || echo 'API health: down'
    curl -sS -m 3 -o /dev/null -w 'Actuator prometheus: %{http_code}\n' http://localhost:8080/actuator/prometheus || echo 'prometheus scrape: down'
    curl -sS -m 3 -o /dev/null -w 'Grafana: %{http_code}\n' http://localhost:3002/api/health || echo 'Grafana: down'
    ;;
  urls)
    cat <<EOF
Grafana:     http://localhost:3002  (admin/admin) — dashboard "SIGESA API — near real-time performance" (refresh 5s)
Prometheus:  http://localhost:9090
Tempo:       http://localhost:3200  (Grafana Explore → Tempo)
OTLP HTTP:   http://localhost:4318
API:         http://localhost:8080
Prometheus metrics: http://localhost:8080/actuator/prometheus

Generate load:
  ./scripts/perf-observe.sh k6
EOF
    ;;
  *)
    usage
    exit 1
    ;;
esac
