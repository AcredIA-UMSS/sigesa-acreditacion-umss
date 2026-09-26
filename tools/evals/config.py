from __future__ import annotations

import importlib.util
import os
from pathlib import Path

_EVALS_DIR = Path(__file__).resolve().parent
_E2E_CONFIG_PATH = _EVALS_DIR.parent / "e2e-agent" / "config.py"


def _load_e2e_config():
    spec = importlib.util.spec_from_file_location("sigesa_e2e_agent_config", _E2E_CONFIG_PATH)
    if spec is None or spec.loader is None:
        raise RuntimeError(f"No se pudo cargar {_E2E_CONFIG_PATH}")
    mod = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(mod)
    return mod


e2e_config = _load_e2e_config()
REPO_ROOT = e2e_config.REPO_ROOT


def api_base() -> str:
    return (os.getenv("SIGESA_API_BASE") or "http://127.0.0.1:8080").rstrip("/")
