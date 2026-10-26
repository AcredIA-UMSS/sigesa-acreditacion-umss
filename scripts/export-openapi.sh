#!/usr/bin/env bash
# Exporta el contrato OpenAPI desde Spring (springdoc GET /v3/api-docs) a openapi.json en la raíz.
# Reproducible: OPENAPI_WRITE=true ./mvnw -Dtest=OpenApiExportIT test  (cwd: backend/)
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT/backend"
OPENAPI_WRITE=true ./mvnw -Dtest=OpenApiExportIT test
echo "Escrito $ROOT/openapi.json"
