"""LLM config — misma convención que tools/e2e-agent (lee .env de la raíz)."""

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
    raise RuntimeError("No encuentro la raíz del monorepo")


REPO_ROOT = find_repo_root()
AGENT_DIR = Path(__file__).resolve().parent


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
_AGENT_ENV = _load_dotenv(AGENT_DIR / ".env")


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


def resolve_llm() -> tuple[str, str, str]:
    provider = (_get("SIGESA_CONTRACT_PROVIDER") or _get("SIGESA_LLM_PROVIDER") or "local").lower()
    if provider == "groq":
        api_key = _get("GROQ_API_KEY") or ""
        base_url = _get("GROQ_BASE_URL") or "https://api.groq.com/openai/v1"
        model = _get("SIGESA_LLM_MODEL_GROQ") or "openai/gpt-oss-20b"
    else:
        api_key = _get("SIGESA_ASSISTANT_API_KEY") or "ollama"
        base_url = _get("SIGESA_LLM_BASE_URL_HOST") or "http://localhost:11434/v1"
        model = _get("SIGESA_LLM_MODEL_LOCAL") or "qwen2.5:7b"
    trimmed = base_url.rstrip("/")
    if not trimmed.endswith("/v1"):
        trimmed = f"{trimmed}/v1"
    return trimmed, api_key, model


def crear_cliente():
    from openai import OpenAI

    base_url, api_key, _ = resolve_llm()
    return OpenAI(base_url=base_url, api_key=api_key, timeout=120.0)


def resumen() -> str:
    base_url, _, model = resolve_llm()
    provider = (_get("SIGESA_LLM_PROVIDER") or "local").lower()
    return f"{provider} @ {base_url} model={model}"
