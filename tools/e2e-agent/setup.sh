#!/usr/bin/env bash
# Crea o actualiza el entorno virtual en tools/e2e-agent/.venv
set -euo pipefail

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${DIR}"

if [[ ! -d .venv ]]; then
  echo "Creando .venv con $(command -v python3)..."
  python3 -m venv .venv
fi

echo "Instalando dependencias..."
.venv/bin/python -m pip install -U pip wheel
.venv/bin/pip install -r requirements.txt

if [[ ! -f .env ]] && [[ -f .env.example ]]; then
  cp .env.example .env
  echo "Creado .env desde .env.example (revisá SIGESA_E2E_MODEL)"
fi

echo ""
echo "Listo. Probá:"
echo "  ./run.sh probar"
echo "  ./run.sh secciones"
