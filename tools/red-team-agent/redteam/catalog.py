from __future__ import annotations

import json
from pathlib import Path
from typing import Any

AGENT_DIR = Path(__file__).resolve().parent.parent
CATALOG_PATH = AGENT_DIR / "catalog" / "attacks.json"
TAXONOMY_PATH = AGENT_DIR / "catalog" / "taxonomy.yaml"


def load_taxonomy_categories() -> list[tuple[str, str]]:
    """Devuelve (id_categoria, label) en orden del YAML."""
    if not TAXONOMY_PATH.is_file():
        return []
    try:
        import yaml
    except ImportError:
        return []
    data = yaml.safe_load(TAXONOMY_PATH.read_text(encoding="utf-8")) or {}
    cats = data.get("categories") or {}
    return [(key, (cats[key] or {}).get("label") or key) for key in cats]


def coverage_by_category() -> dict[str, list[dict[str, Any]]]:
    by_cat: dict[str, list[dict[str, Any]]] = {}
    for attack in load_catalog().get("attacks", []):
        cat = attack.get("category") or "unknown"
        by_cat.setdefault(cat, []).append(attack)
    return by_cat


def load_catalog() -> dict[str, Any]:
    return json.loads(CATALOG_PATH.read_text(encoding="utf-8"))


def list_attacks(category: str | None = None) -> list[dict[str, Any]]:
    data = load_catalog()
    attacks = data.get("attacks", [])
    if category:
        attacks = [a for a in attacks if a.get("category") == category]
    return attacks


def sync_to_backend(repo_root: Path) -> Path:
    dest = repo_root / "backend" / "src" / "test" / "resources" / "redteam" / "attacks.catalog.json"
    dest.parent.mkdir(parents=True, exist_ok=True)
    dest.write_text(CATALOG_PATH.read_text(encoding="utf-8"), encoding="utf-8")
    return dest


def sync_all(repo_root: Path) -> Path:
    return sync_to_backend(repo_root)
