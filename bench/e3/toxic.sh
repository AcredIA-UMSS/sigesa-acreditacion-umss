#!/usr/bin/env bash
set -euo pipefail

TOXIPROXY_URL="${TOXIPROXY_URL:-http://localhost:8474}"
PROXY="modulo"

usage() {
  cat <<'EOF'
Uso: toxic.sh <comando> [on|off]

  lento on|off      latency downstream 2000 ms, jitter 0
  colgado on|off    timeout downstream, timeout 0
  caido on|off      POST /proxies/modulo {"enabled": false|true}
  reset             quita toxics y rehabilita el proxy
  status            GET /proxies/modulo

TOXIPROXY_URL (default http://localhost:8474)
EOF
}

die() {
  echo "ERROR: $*" >&2
  exit 1
}

need_on_off() {
  local value="${1:-}"
  if [[ "$value" != "on" && "$value" != "off" ]]; then
    die "se espera on|off, recibido '${value:-<vacio>}'"
  fi
}

api() {
  local method="$1"
  local path="$2"
  local data="${3:-}"
  if [[ -n "$data" ]]; then
    curl -sS -X "$method" "${TOXIPROXY_URL}${path}" \
      -H "Content-Type: application/json" \
      -d "$data"
  else
    curl -sS -X "$method" "${TOXIPROXY_URL}${path}"
  fi
}

toxic_delete() {
  local name="$1"
  curl -sS -o /dev/null -w "%{http_code}" -X DELETE \
    "${TOXIPROXY_URL}/proxies/${PROXY}/toxics/${name}" || true
}

ensure_proxy() {
  if ! curl -sf "${TOXIPROXY_URL}/proxies/${PROXY}" >/dev/null; then
    die "no existe el proxy '${PROXY}' en ${TOXIPROXY_URL}"
  fi
}

lento_on() {
  ensure_proxy
  toxic_delete "lento" >/dev/null
  api POST "/proxies/${PROXY}/toxics" \
    '{"name":"lento","type":"latency","stream":"downstream","toxicity":1.0,"attributes":{"latency":2000,"jitter":0}}'
  echo
}

lento_off() {
  ensure_proxy
  toxic_delete "lento" >/dev/null
  echo "lento off"
}

colgado_on() {
  ensure_proxy
  toxic_delete "colgado" >/dev/null
  api POST "/proxies/${PROXY}/toxics" \
    '{"name":"colgado","type":"timeout","stream":"downstream","toxicity":1.0,"attributes":{"timeout":0}}'
  echo
}

colgado_off() {
  ensure_proxy
  toxic_delete "colgado" >/dev/null
  echo "colgado off"
}

caido_on() {
  ensure_proxy
  api POST "/proxies/${PROXY}" '{"enabled":false}'
  echo
}

caido_off() {
  ensure_proxy
  api POST "/proxies/${PROXY}" '{"enabled":true}'
  echo
}

reset_all() {
  ensure_proxy
  api POST "/reset"
  echo
  api POST "/proxies/${PROXY}" '{"enabled":true}'
  echo
}

status_proxy() {
  api GET "/proxies/${PROXY}"
  echo
}

cmd="${1:-}"
arg="${2:-}"

case "$cmd" in
  lento)
    need_on_off "$arg"
    if [[ "$arg" == "on" ]]; then lento_on; else lento_off; fi
    ;;
  colgado)
    need_on_off "$arg"
    if [[ "$arg" == "on" ]]; then colgado_on; else colgado_off; fi
    ;;
  caido)
    need_on_off "$arg"
    if [[ "$arg" == "on" ]]; then caido_on; else caido_off; fi
    ;;
  reset)
    [[ -z "$arg" ]] || die "reset no acepta argumentos"
    reset_all
    ;;
  status)
    [[ -z "$arg" ]] || die "status no acepta argumentos"
    status_proxy
    ;;
  -h|--help|help|"")
    usage
    [[ -n "$cmd" ]] || exit 1
    ;;
  *)
    usage
    die "comando desconocido: $cmd"
    ;;
esac
