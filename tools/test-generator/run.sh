#!/usr/bin/env bash
# SIGESA Test Generator — ejecuta vía contenedor Docker (Python)
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

cd "${REPO_ROOT}"

# Construye la imagen si no existe (rápido si ya está cacheada)
docker compose --profile tools build test-generator >/dev/null

exec docker compose --profile tools run --rm test-generator "$@"
