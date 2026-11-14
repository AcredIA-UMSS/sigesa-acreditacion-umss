"""Config LLM para el agente E2E — lee `.env` de la raíz del monorepo."""

from __future__ import annotations

import os
from pathlib import Path


def find_repo_root() -> Path:
    explicit = os.getenv("SIGESA_REPO_ROOT")
    if explicit:
        return Path(explicit).resolve()
    cursor = Path(__file__).resolve().parent
    while cursor != cursor.parent:
        if (cursor / "backend").is_dir() and (cursor / "frontend").is_dir():
            return cursor
        cursor = cursor.parent
    raise RuntimeError("No encuentro la raíz del monorepo (backend/ + frontend/)")


REPO_ROOT = find_repo_root()
FRONTEND = REPO_ROOT / "frontend"


def _load_dotenv(path: Path) -> dict[str, str]:
    if not path.is_file():
        return {}
    out: dict[str, str] = {}
    for line in path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, _, value = line.partition("=")
        out[key.strip()] = value.strip().strip('"').strip("'")
    return out


_ENV_FILE = _load_dotenv(REPO_ROOT / ".env")
_AGENT_ENV = _load_dotenv(Path(__file__).resolve().parent / ".env")


def _get(key: str, default: str | None = None) -> str | None:
    env_val = os.getenv(key)
    if env_val is not None and env_val.strip():
        return env_val.strip()
    agent_val = _AGENT_ENV.get(key)
    if agent_val is not None and str(agent_val).strip():
        return str(agent_val).strip()
    file_val = _ENV_FILE.get(key)
    if file_val is not None and str(file_val).strip():
        return str(file_val).strip()
    return default


def _normalize_base_url(url: str) -> str:
    trimmed = url.rstrip("/")
    if trimmed.endswith("/v1"):
        return trimmed
    return f"{trimmed}/v1"


def resolve_llm() -> tuple[str, str, str]:
    """Devuelve (base_url, api_key, model)."""
    provider = (
        _get("SIGESA_E2E_PROVIDER")
        or _get("SIGESA_TESTGEN_PROVIDER")
        or _get("SIGESA_LLM_PROVIDER")
        or "local"
    ).lower()

    model = _get("SIGESA_E2E_MODEL") or _get("SIGESA_TESTGEN_MODEL")
    base_url = _get("SIGESA_E2E_BASE_URL") or _get("SIGESA_TESTGEN_BASE_URL")
    api_key = _get("SIGESA_E2E_API_KEY") or _get("SIGESA_TESTGEN_API_KEY")
    backend = (_get("SIGESA_E2E_BACKEND") or "ollama").lower()

    if provider == "groq":
        api_key = api_key or _get("GROQ_API_KEY")
        base_url = base_url or _get("GROQ_BASE_URL") or "https://api.groq.com/openai/v1"
        model = model or _get("SIGESA_LLM_MODEL_GROQ") or "openai/gpt-oss-20b"
    elif backend == "open-webui":
        # Open WebUI en Docker expone :3001 → API OpenAI-compatible en /api/v1
        base_url = (
            base_url
            or _get("SIGESA_ASSISTANT_BASE_URL_HOST")
            or _get("SIGESA_ASSISTANT_BASE_URL")
            or "http://localhost:3001/api"
        )
        api_key = api_key or _get("SIGESA_ASSISTANT_API_KEY") or "sk-local"
        model = model or _get("SIGESA_ASSISTANT_MODEL") or _get("SIGESA_LLM_MODEL_LOCAL") or "qwen2.5:7b"
    else:
        # Ollama directo — contenedor sigesa-ollama mapeado a localhost:11434
        base_url = base_url or _get("SIGESA_LLM_BASE_URL_HOST") or "http://localhost:11434/v1"
        api_key = api_key or "ollama"
        model = model or _get("SIGESA_LLM_MODEL_LOCAL") or _get("SIGESA_ASSISTANT_MODEL") or "qwen2.5:7b"

    if not model:
        raise ValueError("Modelo no configurado (SIGESA_E2E_MODEL o SIGESA_LLM_MODEL_LOCAL)")
    if provider == "groq" and not (api_key or "").strip():
        raise ValueError("Provider groq requiere GROQ_API_KEY en .env de la raíz")
    if backend == "open-webui" and not (api_key or "").strip().replace("sk-local", ""):
        pass  # sk-local funciona en dev; producción usa API key real

    return _normalize_base_url(base_url), api_key or "", model


def timeout_seconds() -> float:
    raw = _get("SIGESA_E2E_TIMEOUT_SECONDS") or _get("SIGESA_TESTGEN_TIMEOUT_SECONDS") or "600"
    try:
        return float(raw)
    except ValueError:
        return 600.0


def crear_cliente():
    try:
        from openai import OpenAI
    except ImportError as exc:
        raise SystemExit(
            "Falta el paquete openai. Instalá: pip install -r tools/e2e-agent/requirements.txt"
        ) from exc

    base_url, api_key, _ = resolve_llm()
    return OpenAI(base_url=base_url, api_key=api_key, timeout=timeout_seconds())


def resumen() -> str:
    base_url, _, model = resolve_llm()
    provider = (
        _get("SIGESA_E2E_PROVIDER") or _get("SIGESA_LLM_PROVIDER") or "local"
    ).lower()
    backend = _get("SIGESA_E2E_BACKEND") or "ollama"
    return f"{provider}/{backend} @ {base_url} model={model} timeout={timeout_seconds():.0f}s"
