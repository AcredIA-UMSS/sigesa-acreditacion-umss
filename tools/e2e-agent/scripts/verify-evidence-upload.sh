#!/usr/bin/env bash
# Verifica login CC + POST evidencia (API directa). Uso: backend en :8080, jq instalado.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../../.." && pwd)"
PDF="${ROOT}/frontend/tests/fixtures/evidencia-e2e.pdf"
BASE="${SIGESA_API_BASE:-http://127.0.0.1:8080}"

TOKEN=$(curl -s -X POST "${BASE}/api/v1/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"email":"cc@umss.edu.bo","password":"CoordDemo2026!"}' | jq -r '.accessToken // empty')
if [[ -z "${TOKEN}" ]]; then
  echo "FAIL: login sin accessToken"
  exit 1
fi

IND=$(curl -s "${BASE}/api/v1/indicators/uploadable" -H "Authorization: Bearer ${TOKEN}" | jq -r '.[0].indicatorId // empty')
if [[ -z "${IND}" ]]; then
  echo "FAIL: sin indicadores uploadable (normativa v2 / seed)"
  exit 1
fi

HTTP=$(curl -s -o /tmp/sigesa-upload-resp.json -w '%{http_code}' -X POST \
  "${BASE}/api/v1/indicators/${IND}/evidences" \
  -H "Authorization: Bearer ${TOKEN}" \
  -F "description=E2E curl $(date +%s)" \
  -F "file=@${PDF};type=application/pdf")
echo "indicator=${IND} http=${HTTP}"
cat /tmp/sigesa-upload-resp.json
echo
[[ "${HTTP}" == "201" ]] || exit 1
echo "OK"
