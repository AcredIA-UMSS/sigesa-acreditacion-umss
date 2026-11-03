#!/usr/bin/env bash
# Medición E3 A/B/A/B. No inventa cifras: si un paso falla, el JSON queda en error.
set -u
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
COMPOSE="$ROOT/bench/e3/compose.yaml"
OUT="$ROOT/bench/e3/resultados"
mkdir -p "$OUT"
chmod 777 "$OUT"
LOG="$OUT/run.log"
exec > >(tee -a "$LOG") 2>&1

K6_IMAGE="grafana/k6:0.57.0"
API_A_PORT=18080
API_B_PORT=18082
NOTIFY_PORT=18081
TOXI_PORT=18474

echo "=== E3 start $(date -u +%Y-%m-%dT%H:%M:%SZ) ==="

docker pull "$K6_IMAGE"
docker pull ghcr.io/shopify/toxiproxy:2.9.0

wait_http() {
  local url="$1" tries="${2:-180}"
  local i
  for ((i = 1; i <= tries; i++)); do
    if curl -sf "$url" >/dev/null; then
      return 0
    fi
    sleep 1
  done
  return 1
}

ensure_proxy() {
  local port="$1"
  if curl -sf "http://127.0.0.1:${port}/proxies/notify" >/dev/null; then
    curl -sf -X POST "http://127.0.0.1:${port}/proxies/notify" \
      -H 'Content-Type: application/json' \
      -d '{"enabled":true,"listen":"0.0.0.0:8666","upstream":"notify:8081"}' >/dev/null
    return 0
  fi
  curl -sf -X POST "http://127.0.0.1:${port}/proxies" \
    -H 'Content-Type: application/json' \
    -d '{"name":"notify","listen":"0.0.0.0:8666","upstream":"notify:8081","enabled":true}' >/dev/null
}

k6_run() {
  local base="$1" dest="$2" duration="${3:-20s}"
  docker run --rm \
    --add-host=host.docker.internal:host-gateway \
    -v "$ROOT/bench/e3:/scripts" \
    -e BASE_URL="$base" \
    -e DURATION="$duration" \
    "$K6_IMAGE" run /scripts/k6-e3.js --summary-export "/scripts/resultados/$(basename "$dest")"
}

echo "--- smoke in_process ---"
docker compose -p e3a -f "$COMPOSE" down --remove-orphans -t 2 >/dev/null 2>&1 || true
API_PORT="$API_A_PORT" EXTRACTION_MODE=in_process \
  docker compose -p e3a -f "$COMPOSE" up -d api
if ! wait_http "http://127.0.0.1:${API_A_PORT}/api/health"; then
  echo "SMOKE_FAIL api health"
  docker compose -p e3a -f "$COMPOSE" logs api | tail -n 80
  exit 1
fi
curl -sS -D - "http://127.0.0.1:${API_A_PORT}/api/v1/e3/notifications" -o "$OUT/seed-in-process.json" | head -n 20
python3 - <<'PY' "$OUT/seed-in-process.json"
import json, sys
raw = open(sys.argv[1]).read()
print("SEED_IN_PROCESS", raw[:500])
try:
    json.loads(raw)
except Exception as exc:
    print("SEED_NOT_JSON", exc)
    raise SystemExit(1)
PY

echo "--- smoke http ---"
docker compose -p e3b -f "$COMPOSE" down --remove-orphans -t 2 >/dev/null 2>&1 || true
API_PORT="$API_B_PORT" NOTIFY_PORT="$NOTIFY_PORT" TOXIPROXY_PORT="$TOXI_PORT" \
  EXTRACTION_MODE=http SIGESA_NOTIFICATION_BASE_URL=http://toxiproxy:8666 \
  docker compose -p e3b -f "$COMPOSE" --profile http up -d
if ! wait_http "http://127.0.0.1:${TOXI_PORT}/version" 60; then
  echo "SMOKE_FAIL toxiproxy"
  docker compose -p e3b -f "$COMPOSE" logs toxiproxy | tail -n 40
  exit 1
fi
ensure_proxy "$TOXI_PORT"
if ! wait_http "http://127.0.0.1:${NOTIFY_PORT}/internal/notifications/status"; then
  echo "SMOKE_FAIL notify"
  docker compose -p e3b -f "$COMPOSE" logs notify | tail -n 80
  exit 1
fi
if ! wait_http "http://127.0.0.1:${API_B_PORT}/api/health"; then
  echo "SMOKE_FAIL api http health"
  docker compose -p e3b -f "$COMPOSE" logs api | tail -n 80
  exit 1
fi
curl -sS "http://127.0.0.1:${API_B_PORT}/api/v1/e3/notifications" -o "$OUT/seed-http.json"
python3 - <<'PY' "$OUT/seed-http.json"
import json, sys
raw = open(sys.argv[1]).read()
print("SEED_HTTP", raw[:500])
json.loads(raw)
PY
curl -sS "http://127.0.0.1:${TOXI_PORT}/proxies/notify" | python3 -m json.tool

echo "--- warmup descartado ---"
k6_run "http://host.docker.internal:${API_A_PORT}" "$OUT/warmup-a.json" "10s" || true
k6_run "http://host.docker.internal:${API_B_PORT}" "$OUT/warmup-b.json" "10s" || true

echo "--- H1 A/B/A/B ---"
for i in 1 2 3 4 5; do
  echo "RUN A $i"
  k6_run "http://host.docker.internal:${API_A_PORT}" "$OUT/h1-a-${i}.json" "20s"
  echo "RUN B $i"
  k6_run "http://host.docker.internal:${API_B_PORT}" "$OUT/h1-b-${i}.json" "20s"
done

echo "--- H2 corte ---"
curl -sf -X POST "http://127.0.0.1:${TOXI_PORT}/proxies/notify" \
  -H 'Content-Type: application/json' \
  -d '{"enabled":false}'
curl -sS "http://127.0.0.1:${TOXI_PORT}/proxies/notify" | python3 -m json.tool
k6_run "http://host.docker.internal:${API_B_PORT}" "$OUT/h2-cut.json" "20s"
echo "--- H2 control in_process durante el corte ---"
k6_run "http://host.docker.internal:${API_A_PORT}" "$OUT/h2-control-a.json" "20s"
curl -sf -X POST "http://127.0.0.1:${TOXI_PORT}/proxies/notify" \
  -H 'Content-Type: application/json' \
  -d '{"enabled":true}'
curl -sS -o "$OUT/seed-http-after-cut.json" -w "after_cut_status=%{http_code}\n" \
  "http://127.0.0.1:${API_B_PORT}/api/v1/e3/notifications"

echo "--- bajar bancos de latencia ---"
docker compose -p e3a -f "$COMPOSE" down --remove-orphans -t 2
docker compose -p e3b -f "$COMPOSE" --profile http down --remove-orphans -t 2

echo "--- H3 A/B/A/B compose up ---"
python3 - <<'PY' > "$OUT/h3.jsonl"
print("")
PY
: > "$OUT/h3.jsonl"

time_form() {
  local form="$1" n="$2"
  local project="e3h3"
  docker compose -p "$project" -f "$COMPOSE" --profile http down --remove-orphans -t 2 >/dev/null 2>&1 || true
  local start
  start="$(python3 -c 'import time; print(time.time())')"
  local status="ok" seconds=""
  if [[ "$form" == "in_process" ]]; then
    API_PORT=19080 EXTRACTION_MODE=in_process \
      docker compose -p "$project" -f "$COMPOSE" up -d api
    if ! wait_http "http://127.0.0.1:19080/api/health" 180; then
      status="timeout"
    fi
  else
    API_PORT=19080 NOTIFY_PORT=19081 TOXIPROXY_PORT=19474 \
      EXTRACTION_MODE=http SIGESA_NOTIFICATION_BASE_URL=http://toxiproxy:8666 \
      docker compose -p "$project" -f "$COMPOSE" --profile http up -d
    if ! wait_http "http://127.0.0.1:19474/version" 60; then
      status="timeout-toxiproxy"
    elif ! ensure_proxy 19474; then
      status="proxy-create-failed"
    elif ! wait_http "http://127.0.0.1:19081/internal/notifications/status" 180; then
      status="timeout-notify"
    elif ! wait_http "http://127.0.0.1:19080/api/health" 180; then
      status="timeout-api"
    fi
  fi
  seconds="$(python3 -c "import time; print(round(time.time()-float('$start'), 3))")"
  echo "{\"form\":\"$form\",\"n\":$n,\"seconds\":$seconds,\"status\":\"$status\"}" | tee -a "$OUT/h3.jsonl"
  docker compose -p "$project" -f "$COMPOSE" --profile http down --remove-orphans -t 2 >/dev/null 2>&1 || true
}

for i in 1 2 3 4 5; do
  time_form in_process "$i"
  time_form http "$i"
done

echo "=== E3 done $(date -u +%Y-%m-%dT%H:%M:%SZ) ==="
