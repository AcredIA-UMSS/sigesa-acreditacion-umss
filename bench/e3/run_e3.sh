#!/usr/bin/env bash
# =============================================================================
# Script de automatización de pruebas de estrés E3 (K6 + Toxiproxy)
# =============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
COMPOSE_FILE="${SCRIPT_DIR}/docker-compose.e3.yml"

echo "======================================================================"
echo "    SIGESA - Laboratorio E3: Carga K6 con Inyección Toxiproxy"
echo "======================================================================"

usage() {
    echo "Uso: $0 [opciones]"
    echo ""
    echo "Opciones:"
    echo "  --up                  Levanta los servicios (db, notification-service, toxiproxy, backend)"
    echo "  --seed                Aplica el seed de base de datos en notification_outbox"
    echo "  --run                 Ejecuta la prueba de estrés K6"
    echo "  --all                 Levanta servicios, aplica seed y ejecuta la prueba K6 (por defecto)"
    echo "  --mode <http|in_process>  Modo de extracción (por defecto: http)"
    echo "  --down                Detiene y elimina los contenedores del banco E3"
    echo "  --clean               Detiene y elimina volúmenes de datos"
    echo "  --help                Muestra esta ayuda"
    exit 1
}

ACTION="all"
MODE="${EXTRACTION_MODE:-http}"

while [[ $# -gt 0 ]]; do
    case "$1" in
        --up) ACTION="up"; shift ;;
        --seed) ACTION="seed"; shift ;;
        --run) ACTION="run"; shift ;;
        --all) ACTION="all"; shift ;;
        --down) ACTION="down"; shift ;;
        --clean) ACTION="clean"; shift ;;
        --mode)
            MODE="$2"
            shift 2
            ;;
        --help) usage ;;
        *)
            echo "Opción desconocida: $1"
            usage
            ;;
    esac
done

up_stack() {
    echo "[1/4] Levantando infraestructura E3 con Docker Compose (MODO: ${MODE})..."
    EXTRACTION_MODE="${MODE}" docker compose -f "${COMPOSE_FILE}" up -d --build db toxiproxy notification-service backend
    echo "Esperando a que los servicios completen sus healthchecks..."
    docker compose -f "${COMPOSE_FILE}" ps
}

seed_database() {
    echo "[2/4] Aplicando seed SQL en notification_outbox..."
    if docker compose -f "${COMPOSE_FILE}" ps -q db 2>/dev/null | grep -q .; then
        docker compose -f "${COMPOSE_FILE}" exec -T db psql -U sigesa_user -d sigesa < "${SCRIPT_DIR}/seed_e3_notifications.sql"
        echo "Seed completado. Verificando conteos en base de datos:"
        docker compose -f "${COMPOSE_FILE}" exec -T db psql -U sigesa_user -d sigesa -c "SELECT status, count(*) FROM notification_outbox GROUP BY status;"
    elif docker ps --format '{{.Names}}' | grep -q "^sigesa-postgres$"; then
        docker exec -i sigesa-postgres psql -U sigesa_user -d sigesa < "${SCRIPT_DIR}/seed_e3_notifications.sql"
        echo "Seed completado en sigesa-postgres. Verificando conteos en base de datos:"
        docker exec -i sigesa-postgres psql -U sigesa_user -d sigesa -c "SELECT status, count(*) FROM notification_outbox GROUP BY status;"
    else
        echo "Error: No se encontró ningún contenedor de PostgreSQL activo ('db' en compose o 'sigesa-postgres')."
        exit 1
    fi
}

verify_smoke() {
    local port="${BACKEND_PORT:-8080}"
    echo "[3/4] Verificando conectividad inicial (Smoke test) en puerto ${port}..."
    local max_retries=15
    local count=0
    until curl -sSf "http://localhost:${port}/api/health" > /dev/null 2>&1 || [ $count -ge $max_retries ]; do
        echo "Esperando a backend en http://localhost:${port}/api/health... ($((count+1))/$max_retries)"
        sleep 2
        count=$((count+1))
    done

    echo "Respuesta /api/health:"
    curl -s "http://localhost:${port}/api/health" || true
    echo ""

    echo "Respuesta /api/v1/e3/notifications:"
    curl -s "http://localhost:${port}/api/v1/e3/notifications" || true
    echo ""
}

run_k6() {
    local port="${BACKEND_PORT:-8080}"
    local target_rps="${TARGET_RPS:-50}"
    local fault_latency="${FAULT_LATENCY_MS:-2000}"
    echo "[4/4] Ejecutando escenario de estrés K6 (MODO: ${MODE}, TARGET_RPS: ${target_rps}, FAULT_LATENCY_MS: ${fault_latency}ms)..."
    if command -v k6 &> /dev/null; then
        echo "K6 nativo detectado en el host."
        API_BASE_URL="http://localhost:${port}" \
        TOXIPROXY_BASE_URL="http://localhost:8474" \
        PROXY_NAME="notification_proxy" \
        PROXY_LISTEN="0.0.0.0:8081" \
        TOXIPROXY_UPSTREAM="notification-service:8082" \
        EXTRACTION_MODE="${MODE}" \
        TARGET_RPS="${target_rps}" \
        FAULT_LATENCY_MS="${fault_latency}" \
        BENCH_DIR="${SCRIPT_DIR}" \
        SUMMARY_PATH="${SCRIPT_DIR}/k6_last_result.json" \
        k6 run "${SCRIPT_DIR}/stress_toxiproxy.js"
    else
        echo "K6 nativo no encontrado. Ejecutando mediante contenedor Docker (grafana/k6)..."
        EXTRACTION_MODE="${MODE}" TARGET_RPS="${target_rps}" FAULT_LATENCY_MS="${fault_latency}" \
        docker compose -f "${COMPOSE_FILE}" run --rm \
            -e EXTRACTION_MODE="${MODE}" \
            -e TARGET_RPS="${target_rps}" \
            -e FAULT_LATENCY_MS="${fault_latency}" \
            k6
    fi
}

down_stack() {
    echo "Deteniendo contenedores E3..."
    docker compose -f "${COMPOSE_FILE}" down
}

clean_stack() {
    echo "Limpiando contenedores y volúmenes E3..."
    docker compose -f "${COMPOSE_FILE}" down -v
}

case "$ACTION" in
    up)
        up_stack
        ;;
    seed)
        seed_database
        ;;
    run)
        verify_smoke
        run_k6
        ;;
    all)
        up_stack
        seed_database
        verify_smoke
        run_k6
        ;;
    down)
        down_stack
        ;;
    clean)
        clean_stack
        ;;
esac

echo ""
echo "Operación '$ACTION' finalizada exitosamente."
