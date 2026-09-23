from __future__ import annotations

import json
from pathlib import Path
from typing import Any

AGENT_DIR = Path(__file__).resolve().parent.parent
CATALOG_PATH = AGENT_DIR / "catalog" / "attacks.json"
TAXONOMY_PATH = AGENT_DIR / "catalog" / "taxonomy.yaml"
ATAQUES_DIR = AGENT_DIR / "ataques"
DOCUMENTOS_DIR = ATAQUES_DIR / "documentos"
REPORTS_DIR = AGENT_DIR / "reports"

# Compat con imports antiguos
LAB_REPORTS_DIR = REPORTS_DIR


def load_taxonomy_categories() -> list[tuple[str, str]]:
    if not TAXONOMY_PATH.is_file():
        return []
    try:
        import yaml
    except ImportError:
        return []
    data = yaml.safe_load(TAXONOMY_PATH.read_text(encoding="utf-8")) or {}
    cats = data.get("categories") or {}
    lab = data.get("lab_file_categories") or {}
    out = [(key, (cats[key] or {}).get("label") or key) for key in cats]
    for key, meta in lab.items():
        if key not in cats:
            label = (meta or {}).get("label") or key
            out.append((key, label))
    return out


def load_catalog() -> dict[str, Any]:
    return json.loads(CATALOG_PATH.read_text(encoding="utf-8"))


def _parse_ataques_file(path: Path) -> list[dict[str, Any]]:
    raw = json.loads(path.read_text(encoding="utf-8"))
    if isinstance(raw, list):
        return [a for a in raw if isinstance(a, dict) and a.get("id")]
    if isinstance(raw, dict) and raw.get("id"):
        return [raw]
    if isinstance(raw, dict) and raw.get("attacks"):
        return [a for a in raw["attacks"] if isinstance(a, dict) and a.get("id")]
    return []


def build_user_message(attack: dict[str, Any]) -> str:
    if attack.get("userMessage"):
        return str(attack["userMessage"])
    msg = str(attack.get("mensaje") or "")
    doc = attack.get("documento")
    if doc:
        path = DOCUMENTOS_DIR / str(doc)
        if path.is_file():
            content = path.read_text(encoding="utf-8")
            msg = f"{msg}\n\n--- Contenido adjunto ({doc}) ---\n{content}"
        else:
            msg = f"{msg}\n\n[ADVERTENCIA: documento no encontrado: {doc}]"
    return msg


def normalize_attack(attack: dict[str, Any]) -> dict[str, Any]:
    out = dict(attack)
    out["userMessage"] = build_user_message(out)
    out["title"] = out.get("title") or out.get("titulo") or out.get("id")
    return out


def load_ataques() -> list[dict[str, Any]]:
    """Catálogo operativo completo: todos los *.json en ataques/."""
    if not ATAQUES_DIR.is_dir():
        return []
    attacks: list[dict[str, Any]] = []
    seen: set[str] = set()
    for path in sorted(ATAQUES_DIR.glob("*.json")):
        if path.name.startswith("_"):
            continue
        for item in _parse_ataques_file(path):
            aid = item.get("id")
            if not aid or aid in seen:
                continue
            seen.add(aid)
            attacks.append(normalize_attack(item))
    return attacks


def list_ataques(category: str | None = None, attack_id: str | None = None) -> list[dict[str, Any]]:
    attacks = load_ataques()
    if attack_id:
        attacks = [a for a in attacks if a.get("id") == attack_id]
    if category:
        attacks = [
            a
            for a in attacks
            if a.get("categoria") == category
            or a.get("category") == category
        ]
    return attacks


def list_attacks_ci(category: str | None = None) -> list[dict[str, Any]]:
    """Catálogo legacy para JUnit (catalog/attacks.json)."""
    data = load_catalog()
    attacks = data.get("attacks", [])
    if category:
        attacks = [
            a
            for a in attacks
            if a.get("category") == category or a.get("categoria") == category
        ]
    return attacks


# Alias históricos
def list_attacks(category: str | None = None) -> list[dict[str, Any]]:
    return list_attacks_ci(category)


def load_lab_attacks() -> list[dict[str, Any]]:
    return load_ataques()


def list_lab_attacks(
    category: str | None = None,
    attack_id: str | None = None,
    **_: Any,
) -> list[dict[str, Any]]:
    return list_ataques(category, attack_id)


def coverage_by_category() -> dict[str, list[dict[str, Any]]]:
    by_cat: dict[str, list[dict[str, Any]]] = {}
    for attack in load_ataques():
        cat = attack.get("categoria") or attack.get("category") or "unknown"
        by_cat.setdefault(cat, []).append(attack)
    return by_cat


def sync_to_backend(repo_root: Path) -> Path:
    dest = repo_root / "backend" / "src" / "test" / "resources" / "redteam" / "attacks.catalog.json"
    dest.parent.mkdir(parents=True, exist_ok=True)
    dest.write_text(CATALOG_PATH.read_text(encoding="utf-8"), encoding="utf-8")
    return dest


def sync_all(repo_root: Path) -> Path:
    return sync_to_backend(repo_root)
