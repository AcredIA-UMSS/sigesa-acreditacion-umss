#!/usr/bin/env bash
# Verifica login CC + POST evidencia (API directa). Uso: backend en :8080, jq instalado.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../../.." && pwd)"
PDF="${ROOT}/frontend/tests/fixtures/evidencia-e2e.pdf"
BASE="${SIGESA_API_BASE:-http://127.0.0.1:8080}"
ENV_FILE="${ROOT}/tools/e2e-agent/.env"
# 1 = si no hay PENDIENTE/OBSERVADO, resetea un indicador del proceso ACTIVE del CC (solo dev Docker).
SIGESA_VERIFY_PREPARE_DEV="${SIGESA_VERIFY_PREPARE_DEV:-1}"
POSTGRES_CONTAINER="${SIGESA_POSTGRES_CONTAINER:-sigesa-postgres}"
POSTGRES_USER="${POSTGRES_USER:-sigesa_user}"
POSTGRES_DB="${POSTGRES_DB:-sigesa}"

if [[ -f "${ENV_FILE}" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "${ENV_FILE}" 2>/dev/null || true
  set +a
fi

pick_uploadable_from_api() {
  local json="$1"
  echo "${json}" | jq -r '.[0].indicatorId // empty'
}

pick_uploadable_from_process_tree() {
  local auth="$1"
  local processes_json
  processes_json=$(curl -s "${BASE}/api/v1/processes" -H "${auth}")
  local proc_id
  proc_id=$(echo "${processes_json}" | jq -r '[.[] | select(.status == "ACTIVE") | .id][0] // empty')
  if [[ -z "${proc_id}" ]]; then
    return 1
  fi
  local detail_json
  detail_json=$(curl -s "${BASE}/api/v1/processes/${proc_id}" -H "${auth}")
  echo "${detail_json}" | jq -r '
    [
      .level1Nodes[]?.level2Nodes[]?.level3Nodes[]?.indicators[]?
      | select(.status == "PENDIENTE" or .status == "OBSERVADO")
      | .id
    ][0] // empty'
}

prepare_dev_indicator() {
  local auth="$1"
  local proc_id ind_id
  proc_id=$(curl -s "${BASE}/api/v1/processes" -H "${auth}" \
    | jq -r '[.[] | select(.status == "ACTIVE") | .id][0] // empty')
  if [[ -z "${proc_id}" ]]; then
    return 1
  fi
  ind_id=$(curl -s "${BASE}/api/v1/processes/${proc_id}" -H "${auth}" \
    | jq -r '[.level1Nodes[]?.level2Nodes[]?.level3Nodes[]?.indicators[]? | .id][0] // empty')
  if [[ -z "${ind_id}" ]]; then
    return 1
  fi
  if ! docker ps --format '{{.Names}}' 2>/dev/null | grep -qx "${POSTGRES_CONTAINER}"; then
    echo "WARN: sin contenedor ${POSTGRES_CONTAINER}; no se puede preparar indicador en dev" >&2
    return 1
  fi
  docker exec "${POSTGRES_CONTAINER}" psql -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" -v ON_ERROR_STOP=1 -c "
    DELETE FROM evidence_version
    WHERE evidence_id IN (SELECT id FROM evidence WHERE normative_indicator_id = '${ind_id}');
    DELETE FROM evidence WHERE normative_indicator_id = '${ind_id}';
    UPDATE indicators SET status = 'PENDIENTE' WHERE id = '${ind_id}';
  " >/dev/null
  echo "prepared: indicator=${ind_id} status=PENDIENTE (evidencia previa eliminada)" >&2
  echo "${ind_id}"
}

TOKEN=$(curl -s -X POST "${BASE}/api/v1/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"email":"cc@umss.edu.bo","password":"CoordDemo2026!"}' | jq -r '.accessToken // empty' | tr -d '\n\r')
if [[ -z "${TOKEN}" ]]; then
  echo "FAIL: login sin accessToken"
  exit 1
fi

AUTH="Authorization: Bearer ${TOKEN}"
IND="${SIGESA_VERIFY_INDICATOR_ID:-}"

if [[ -z "${IND}" ]]; then
  UPLOADABLE_HTTP=$(curl -s -o /tmp/sigesa-uploadable.json -w '%{http_code}' \
    "${BASE}/api/v1/indicators/uploadable" -H "${AUTH}")
  if [[ "${UPLOADABLE_HTTP}" != "200" ]]; then
    echo "FAIL: GET uploadable http=${UPLOADABLE_HTTP} (token/JWT o rol CC)"
    cat /tmp/sigesa-uploadable.json
    echo
    exit 1
  fi
  UPLOADABLE_JSON=$(cat /tmp/sigesa-uploadable.json)
  IND=$(pick_uploadable_from_api "${UPLOADABLE_JSON}")
fi

if [[ -z "${IND}" ]]; then
  IND=$(pick_uploadable_from_process_tree "${AUTH}" || true)
fi

if [[ -z "${IND}" && "${SIGESA_VERIFY_PREPARE_DEV}" == "1" ]]; then
  IND=$(prepare_dev_indicator "${AUTH}" || true)
fi

if [[ -z "${IND}" ]]; then
  echo "FAIL: sin indicador PENDIENTE/OBSERVADO para el CC (proceso ACTIVE de su carrera)."
  echo "  Opciones:"
  echo "    SIGESA_VERIFY_INDICATOR_ID=<uuid> bash $0"
  echo "    SIGESA_VERIFY_PREPARE_DEV=1 (default) con ${POSTGRES_CONTAINER} en marcha"
  echo "  GET uploadable:"
  cat /tmp/sigesa-uploadable.json 2>/dev/null || echo "[]"
  echo
  exit 1
fi

HTTP=$(curl -s -o /tmp/sigesa-upload-resp.json -w '%{http_code}' --http1.1 -X POST \
  "${BASE}/api/v1/indicators/${IND}/evidences" \
  -H "${AUTH}" \
  -F "description=E2E curl $(date +%s)" \
  -F "file=@${PDF};type=application/pdf")
echo "indicator=${IND} http=${HTTP}"
cat /tmp/sigesa-upload-resp.json
echo
if [[ "${HTTP}" == "401" ]]; then
  echo "FAIL: POST 401 — revisar JWT/backend (no suele ser rol CC si GET uploadable fue 200)"
  exit 1
fi
if [[ "${HTTP}" == "403" ]]; then
  echo "FAIL: POST 403 (rol CC o @PreAuthorize) — verificar usuario cc@umss.edu.bo"
  exit 1
fi
if [[ "${HTTP}" == "409" ]]; then
  echo "FAIL: POST 409 integridad — reconstruir backend o preparar indicador (SIGESA_VERIFY_PREPARE_DEV=1)"
  exit 1
fi
[[ "${HTTP}" == "201" ]] || exit 1
echo "OK"
