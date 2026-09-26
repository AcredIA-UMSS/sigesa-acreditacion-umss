from __future__ import annotations

import json
from pathlib import Path
from typing import Any

EVALS_DIR = Path(__file__).resolve().parent.parent
DEFAULT_DATASET = EVALS_DIR / "datos" / "conocimiento" / "data_set_dorado.jsonl"
ARTIFACTS_DIR = EVALS_DIR / "artifacts"
LABELS_DIR = ARTIFACTS_DIR / "labels"
REPORTS_DIR = EVALS_DIR / "reports"
PROMPTS_DIR = EVALS_DIR / "prompts"
HUMAN_LABELS_PATH = LABELS_DIR / "human.jsonl"
JUDGE_LABELS_PATH = LABELS_DIR / "judge.jsonl"
KAPPA_MIN = 0.6


def load_dataset(path: Path | None = None) -> list[dict[str, Any]]:
    p = path or DEFAULT_DATASET
    cases: list[dict[str, Any]] = []
    for line in p.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#"):
            continue
        cases.append(json.loads(line))
    return cases


def list_cases(
    cases: list[dict[str, Any]],
    *,
    case_id: str | None = None,
    tipo: str | None = None,
) -> list[dict[str, Any]]:
    out = cases
    if case_id:
        out = [c for c in out if c.get("id") == case_id]
    if tipo:
        out = [c for c in out if c.get("tipo") == tipo]
    return out


def response_path(prompt_version: str, case_id: str) -> Path:
    return ARTIFACTS_DIR / "responses" / prompt_version / f"{case_id}.json"
