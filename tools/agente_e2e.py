#!/usr/bin/env python3
"""Wrapper — delega en tools/e2e-agent/agente_e2e.py (SIGESA).

Uso:
    cd tools
    python agente_e2e.py plan login
    python agente_e2e.py generar ayuda 1.1

Documentación: tools/e2e-agent/README.md
"""
from __future__ import annotations

import runpy
import sys
from pathlib import Path

_AGENT_DIR = Path(__file__).resolve().parent / "e2e-agent"
_TARGET = _AGENT_DIR / "agente_e2e.py"

if not _TARGET.is_file():
    sys.exit(f"no encuentro {_TARGET}")

if str(_AGENT_DIR) not in sys.path:
    sys.path.insert(0, str(_AGENT_DIR))

sys.argv[0] = str(_TARGET)
runpy.run_path(str(_TARGET), run_name="__main__")
