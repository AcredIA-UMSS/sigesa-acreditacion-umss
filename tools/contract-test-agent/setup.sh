#!/usr/bin/env bash
set -euo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${DIR}"
if [[ ! -d .venv ]]; then
  python3 -m venv .venv
fi
.venv/bin/python -m pip install -U pip wheel
.venv/bin/pip install -r requirements.txt
if [[ ! -f .env ]] && [[ -f .env.example ]]; then
  cp .env.example .env
fi
echo "Listo. ./run.sh modulos | ./run.sh correr"
