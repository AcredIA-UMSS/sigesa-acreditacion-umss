#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
API_BASE_URL="${API_BASE_URL:-http://127.0.0.1:${E3_API_HOST_PORT:-8080}}"
TOXIPROXY_URL="${TOXIPROXY_URL:-http://localhost:${E3_TOXIPROXY_API_PORT:-8474}}"
MODULE_PATH="/api/v1/e3/notifications"
CONTROL_PATH="/api/health"
TOXIC="${SCRIPT_DIR}/toxic.sh"

ok=0
fail=0

pass() {
  ok=$((ok + 1))
  echo "OK    $1"
}

fail_step() {
  fail=$((fail + 1))
  echo "FALLA $1"
}

http_code() {
  local url="$1"
  local extra="${2:-}"
  # shellcheck disable=SC2086
  curl -sS -o /dev/null -w "%{http_code}" ${extra} "$url" || true
}

control_200() {
  local label="$1"
  local code
  code="$(http_code "${API_BASE_URL}${CONTROL_PATH}")"
  if [[ "$code" == "200" ]]; then
    pass "control ${CONTROL_PATH} = 200 (${label})"
  else
    fail_step "control ${CONTROL_PATH} = ${code} (${label})"
  fi
}

command -v curl >/dev/null || { echo "FALLA curl no esta en PATH" >&2; exit 1; }

echo "== verificar-infra E3 =="
echo "API=${API_BASE_URL}  TOXIPROXY=${TOXIPROXY_URL}"

# 1. Ruta del módulo 200 (sin toxics)
export TOXIPROXY_URL
"${TOXIC}" reset >/dev/null
code="$(http_code "${API_BASE_URL}${MODULE_PATH}")"
if [[ "$code" == "200" ]]; then
  pass "modulo ${MODULE_PATH} = 200 (baseline)"
else
  fail_step "modulo ${MODULE_PATH} = ${code} (baseline)"
fi
control_200 "baseline"

# 2. lento ≥ 2 s
"${TOXIC}" lento on >/dev/null
start="$(date +%s)"
code="$(http_code "${API_BASE_URL}${MODULE_PATH}")"
elapsed=$(( $(date +%s) - start ))
if [[ "$elapsed" -ge 2 ]]; then
  pass "lento tardo ${elapsed}s (>= 2s, http ${code})"
else
  fail_step "lento tardo ${elapsed}s (se esperaba >= 2s, http ${code})"
fi
control_200 "lento"
"${TOXIC}" lento off >/dev/null

# 3. colgado termina con curl -m 5
"${TOXIC}" colgado on >/dev/null
set +e
curl -sS -m 5 -o /dev/null "${API_BASE_URL}${MODULE_PATH}"
colgado_rc=$?
set -e
pass "colgado termino (curl -m 5, rc=${colgado_rc})"
control_200 "colgado"
"${TOXIC}" colgado off >/dev/null

# 4. caido falla al instante
"${TOXIC}" caido on >/dev/null
start="$(date +%s)"
code="$(http_code "${API_BASE_URL}${MODULE_PATH}")"
elapsed=$(( $(date +%s) - start ))
if [[ "$code" != "200" && "$elapsed" -le 1 ]]; then
  pass "caido fallo al instante (http ${code}, ${elapsed}s)"
else
  fail_step "caido http ${code} en ${elapsed}s (se esperaba !=200 y <=1s)"
fi
control_200 "caido"
"${TOXIC}" caido off >/dev/null

# 5. reset vuelve a 200
"${TOXIC}" reset >/dev/null
code="$(http_code "${API_BASE_URL}${MODULE_PATH}")"
if [[ "$code" == "200" ]]; then
  pass "reset -> modulo ${MODULE_PATH} = 200"
else
  fail_step "reset -> modulo ${MODULE_PATH} = ${code}"
fi
control_200 "reset"

echo
echo "Resumen: ${ok} OK, ${fail} FALLA"
if [[ "$fail" -gt 0 ]]; then
  exit 1
fi
