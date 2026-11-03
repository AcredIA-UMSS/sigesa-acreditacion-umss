#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
COMPOSE_BASE="${SCRIPT_DIR}/docker-compose.yml"
COMPOSE_MONO="${SCRIPT_DIR}/docker-compose.monolito.yml"
RESULTADOS="${SCRIPT_DIR}/resultados"
CSV="${RESULTADOS}/tiempos.csv"

forma="${1:-http}"
case "$forma" in
  http|extracted) forma="http" ;;
  in_process|monolito|in-process) forma="in_process" ;;
  *)
    echo "Uso: run-limpio.sh [http|in_process]" >&2
    exit 1
    ;;
esac

compose_args=(-f "$COMPOSE_BASE")
if [[ "$forma" == "in_process" ]]; then
  compose_args+=(-f "$COMPOSE_MONO")
fi

mkdir -p "$RESULTADOS"
if [[ ! -f "$CSV" ]]; then
  echo "forma,fecha,build_s,up_s" > "$CSV"
fi

if [[ -f "${ROOT}/.env" ]]; then
  compose_args+=(--env-file "${ROOT}/.env")
fi

echo "== E3 run-limpio forma=${forma} =="
docker compose "${compose_args[@]}" down -v --remove-orphans

echo "-- build --"
t0="$(date +%s)"
docker compose "${compose_args[@]}" build
t1="$(date +%s)"
build_s=$((t1 - t0))

echo "-- up --"
t2="$(date +%s)"
docker compose "${compose_args[@]}" up -d --build --wait
t3="$(date +%s)"
up_s=$((t3 - t2))

fecha="$(date -u +"%Y-%m-%dT%H:%M:%SZ")"
echo "${forma},${fecha},${build_s},${up_s}" >> "$CSV"

echo "build_s=${build_s}  up_s=${up_s}"
echo "registrado en ${CSV}"
