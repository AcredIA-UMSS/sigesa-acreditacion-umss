#!/usr/bin/env bash
# Resuelve URLs y credenciales del assistant según SIGESA_LLM_PROVIDER (local|groq).
# Uso host:  eval "$(./scripts/sigesa-env.sh)" && cd backend && ./mvnw spring-boot:run
# Docker Compose lee .env directamente — configure las mismas variables allí.
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

if [[ -f "${REPO_ROOT}/.env" ]]; then
  set -a
  # shellcheck disable=SC1091
  source "${REPO_ROOT}/.env"
  set +a
fi

provider="${SIGESA_LLM_PROVIDER:-local}"
runtime="${SIGESA_RUNTIME:-auto}"

if [[ "${runtime}" == "auto" ]]; then
  if [[ -f /.dockerenv ]] || [[ "${SIGESA_IN_DOCKER:-false}" == "true" ]]; then
    runtime="docker"
  else
    runtime="host"
  fi
fi

# --- Perfil Groq (respuestas rápidas, sin Ollama) ---
if [[ "${provider}" == "groq" ]]; then
  # Backend append /v1/chat/completions → base SIN /v1 final
  export SIGESA_ASSISTANT_BASE_URL="${SIGESA_ASSISTANT_BASE_URL:-https://api.groq.com/openai}"
  export SIGESA_ASSISTANT_API_KEY="${SIGESA_ASSISTANT_API_KEY:-${GROQ_API_KEY:-}}"
  export SIGESA_ASSISTANT_MODEL="${SIGESA_ASSISTANT_MODEL:-${SIGESA_LLM_MODEL_GROQ:-openai/gpt-oss-20b}}"
  export SIGESA_LLM_BASE_URL="${SIGESA_LLM_BASE_URL:-${GROQ_BASE_URL:-https://api.groq.com/openai/v1}}"

# --- Perfil local (Open WebUI u Ollama) ---
elif [[ "${runtime}" == "docker" ]]; then
  export SIGESA_ASSISTANT_BASE_URL="${SIGESA_ASSISTANT_BASE_URL:-${SIGESA_ASSISTANT_BASE_URL_DOCKER:-http://open-webui:8080/api}}"
  export SIGESA_LLM_BASE_URL="${SIGESA_LLM_BASE_URL:-${SIGESA_LLM_BASE_URL_DOCKER:-http://ollama:11434/v1}}"
  export SIGESA_ASSISTANT_MODEL="${SIGESA_ASSISTANT_MODEL:-${SIGESA_LLM_MODEL_LOCAL:-qwen2.5:7b}}"
else
  export SIGESA_ASSISTANT_BASE_URL="${SIGESA_ASSISTANT_BASE_URL:-${SIGESA_ASSISTANT_BASE_URL_HOST:-http://localhost:3001/api}}"
  export SIGESA_LLM_BASE_URL="${SIGESA_LLM_BASE_URL:-${SIGESA_LLM_BASE_URL_HOST:-http://localhost:11434/v1}}"
  export SIGESA_ASSISTANT_MODEL="${SIGESA_ASSISTANT_MODEL:-${SIGESA_LLM_MODEL_LOCAL:-qwen2.5:7b}}"
fi

echo "export SIGESA_RUNTIME=${runtime}"
echo "export SIGESA_LLM_PROVIDER=${provider}"
echo "export SIGESA_ASSISTANT_BASE_URL=${SIGESA_ASSISTANT_BASE_URL}"
echo "export SIGESA_ASSISTANT_API_KEY=${SIGESA_ASSISTANT_API_KEY:-}"
echo "export SIGESA_ASSISTANT_MODEL=${SIGESA_ASSISTANT_MODEL}"
echo "export SIGESA_LLM_BASE_URL=${SIGESA_LLM_BASE_URL}"
