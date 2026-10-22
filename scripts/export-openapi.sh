#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT/backend"
OPENAPI_WRITE=true ./mvnw -Dtest=OpenApiExportIT test
echo "Escrito $ROOT/openapi.json"
