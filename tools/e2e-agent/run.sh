#!/usr/bin/env bash
# Ejecuta agente_e2e.py con el Python del venv local
set -euo pipefail

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${DIR}"

if [[ ! -x .venv/bin/python ]]; then
  echo "No hay .venv. Ejecutá primero: ./setup.sh" >&2
  exit 1
fi

exec .venv/bin/python agente_e2e.py "$@"
