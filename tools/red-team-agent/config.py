"""Config Red Team — LLM vía e2e-agent (import por ruta, sin colisión con este `config`)."""

from __future__ import annotations

import importlib.util
import os
from pathlib import Path

_AGENT_DIR = Path(__file__).resolve().parent
_E2E_CONFIG_PATH = _AGENT_DIR.parent / "e2e-agent" / "config.py"


def _load_e2e_config():
    spec = importlib.util.spec_from_file_location("sigesa_e2e_agent_config", _E2E_CONFIG_PATH)
    if spec is None or spec.loader is None:
        raise RuntimeError(f"No se pudo cargar {_E2E_CONFIG_PATH}")
    mod = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(mod)
    return mod


e2e_config = _load_e2e_config()

REPO_ROOT = e2e_config.REPO_ROOT
FRONTEND = e2e_config.FRONTEND


def api_base() -> str:
    return (os.getenv("SIGESA_API_BASE") or "http://127.0.0.1:8080").rstrip("/")


def crear_cliente():
    return e2e_config.crear_cliente()


def resolve_llm():
    return e2e_config.resolve_llm()


def resumen_llm() -> str:
    return e2e_config.resumen()
