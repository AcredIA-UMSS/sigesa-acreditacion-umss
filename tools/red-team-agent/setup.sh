#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"
python3 -m venv .venv
# shellcheck disable=SC1091
source .venv/bin/activate
pip install -q -r requirements.txt
pip install -q -r ../e2e-agent/requirements.txt
if [[ ! -f .env ]]; then
  cp .env.example .env 2>/dev/null || true
fi
echo "OK: .venv listo. source .venv/bin/activate"
