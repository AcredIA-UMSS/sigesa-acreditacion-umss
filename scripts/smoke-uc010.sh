#!/usr/bin/env bash
# Smoke FSD-UC-010 — credentials loaded from AuthDataLoader constants (dev seed only).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
LOADER="$ROOT/backend/src/main/java/com/umss/sigesa/config/AuthDataLoader.java"
API="${API_BASE:-http://localhost:8080}"
FE="${FE_BASE:-http://localhost:3000}"

extract_const() {
  grep "public static final String $1" "$LOADER" | sed -E 's/.*= "([^"]+)".*/\1/'
}

TD_EMAIL="$(extract_const SEED_TD_EMAIL)"
TD_PASS="$(extract_const SEED_TD_PASSWORD)"
CC_EMAIL="$(extract_const SEED_CC_EMAIL)"
CC_PASS="$(extract_const SEED_CC_PASSWORD)"

pass=0
fail=0
ok() { echo "✅ $1"; pass=$((pass + 1)); }
bad() { echo "❌ $1"; fail=$((fail + 1)); }

http_code() {
  local out="$1"
  shift
  curl -s -o "$out" -w '%{http_code}' "$@"
}

echo "=== FSD-UC-010 smoke (API=$API) ==="

code="$(http_code /dev/null "$API/v3/api-docs")"
[ "$code" = "200" ] && ok "OpenAPI $code" || bad "OpenAPI expected 200 got $code"

code="$(http_code /dev/null "$FE/")"
[ "$code" = "200" ] && ok "Frontend $code" || bad "Frontend expected 200 got $code"

login() {
  curl -s -X POST "$API/api/v1/auth/login" \
    -H 'Content-Type: application/json' \
    -d "{\"email\":\"$1\",\"password\":\"$2\"}"
}

TD_JSON="$(login "$TD_EMAIL" "$TD_PASS")"
TD_TOKEN="$(echo "$TD_JSON" | python3 -c "import sys,json; print(json.load(sys.stdin).get('accessToken',''))")"
[ -n "$TD_TOKEN" ] && ok "TD login" || { bad "TD login"; exit 1; }

CC_JSON="$(login "$CC_EMAIL" "$CC_PASS")"
CC_TOKEN="$(echo "$CC_JSON" | python3 -c "import sys,json; print(json.load(sys.stdin).get('accessToken',''))")"
[ -n "$CC_TOKEN" ] && ok "CC login" || bad "CC login"

AUTH_TD=( -H "Authorization: Bearer $TD_TOKEN" )
AUTH_CC=( -H "Authorization: Bearer $CC_TOKEN" )

LIST_FILE="$(mktemp)"
code="$(http_code "$LIST_FILE" "$API/api/v1/processes" "${AUTH_TD[@]}")"
[ "$code" = "200" ] && ok "List processes $code" || bad "List processes expected 200 got $code"

PROCESS_ID=""
PHASE_ID=""
SUBPHASE_COUNT=0
DETAIL_FILE="$(mktemp)"

while read -r pid; do
  [ -n "$pid" ] || continue
  dcode="$(http_code "$DETAIL_FILE" "$API/api/v1/processes/$pid" "${AUTH_TD[@]}")"
  if [ "$dcode" != "200" ]; then
    err="$(python3 -c "import json; d=json.load(open('$DETAIL_FILE')); print(d.get('error','?'))" 2>/dev/null || echo '?')"
    echo "   detail $pid → HTTP $dcode ($err)"
    # Retry via frontend proxy (nginx)
    dcode_fe="$(http_code "$DETAIL_FILE" "$FE/api/v1/processes/$pid" "${AUTH_TD[@]}")"
    if [ "$dcode_fe" = "200" ]; then
      dcode=200
    else
      continue
    fi
  fi
  read -r phase_id sub_count <<EOF
$(python3 - "$DETAIL_FILE" <<'PY'
import json, sys
d = json.load(open(sys.argv[1]))
phases = d.get("phases") or []
if not phases:
    print("", 0)
else:
    print(phases[0]["id"], sum(len(ph.get("subphases") or []) for ph in phases))
PY
)
EOF
  if [ -n "$phase_id" ]; then
    PROCESS_ID="$pid"
    PHASE_ID="$phase_id"
    SUBPHASE_COUNT="$sub_count"
    break
  fi
done < <(python3 - "$LIST_FILE" <<'PY'
import json, sys
items = json.load(open(sys.argv[1]))
# Prefer process with most subphases (richer smoke)
items = sorted(items, key=lambda p: p.get("subphaseCount") or 0, reverse=True)
for p in items:
    print(p["id"])
PY
)

if [ -z "$PROCESS_ID" ] || [ -z "$PHASE_ID" ]; then
  bad "No process with phases found for smoke"
else
  ok "Process $PROCESS_ID phase $PHASE_ID ($SUBPHASE_COUNT subphases)"

  code="$(http_code /dev/null -X POST "$API/api/v1/processes/$PROCESS_ID/phases/$PHASE_ID/complete" \
    "${AUTH_CC[@]}")"
  [ "$code" = "403" ] && ok "CC complete forbidden $code" || bad "CC complete expected 403 got $code"

  BODY_FILE="$(mktemp)"
  code="$(http_code "$BODY_FILE" -X POST "$API/api/v1/processes/$PROCESS_ID/phases/$PHASE_ID/complete" \
    "${AUTH_TD[@]}")"
  if [ "$code" = "409" ]; then
    ok "TD complete blocked $code (FASE_CIERRE_BLOQUEADO)"
    grep -q pendingSubphases "$BODY_FILE" && ok "Response includes pendingSubphases" || bad "Missing pendingSubphases in 409"
  elif [ "$code" = "200" ]; then
    ok "TD complete success $code"
    grep -q COMPLETADA "$BODY_FILE" && ok "Phase state COMPLETADA" || bad "Missing COMPLETADA in 200"
  else
    bad "TD complete unexpected HTTP $code"
  fi
fi

code="$(http_code /dev/null "$FE/api/v1/processes" "${AUTH_TD[@]}")"
[ "$code" = "200" ] && ok "Frontend proxy $code" || bad "Frontend proxy expected 200 got $code"

ORVAL="$ROOT/frontend/src/api/endpoints/phase-workflow/phase-workflow.ts"
[ -f "$ORVAL" ] && grep -q completePhase "$ORVAL" && ok "Orval phase-workflow hook" || bad "Orval phase-workflow missing"

echo ""
echo "=== Result: $pass passed, $fail failed ==="
[ "$fail" -eq 0 ]
