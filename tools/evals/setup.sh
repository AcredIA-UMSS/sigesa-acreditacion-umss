#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"
python -m venv .venv
# shellcheck disable=SC1091
source .venv/bin/activate
pip install -q -r ../e2e-agent/requirements.txt 2>/dev/null || pip install -q openai httpx python-dotenv
chmod +x run.sh
echo "OK: .venv listo. Siguiente: ./run.sh show-config"
