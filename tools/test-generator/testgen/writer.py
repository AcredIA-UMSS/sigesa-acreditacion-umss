from __future__ import annotations

import re
from pathlib import Path

FENCE_PATTERN = re.compile(r"```(?:java)?\s*(.*?)```", re.DOTALL)
CLASS_DECL_PATTERN = re.compile(
    r"(\b(?:public\s+)?(?:final\s+)?class\s+)(\w+)(\b)",
)
PACKAGE_PATTERN = re.compile(r"^\s*package\s+[\w.]+;\s*$", re.MULTILINE)


def extract_java(raw: str) -> str:
    if not raw or not raw.strip():
        raise ValueError("Salida LLM vacía")
    match = FENCE_PATTERN.search(raw.strip())
    if match:
        return match.group(1).strip()
    trimmed = raw.strip()
    if trimmed.startswith("package ") or "class " in trimmed:
        return trimmed
    raise ValueError("No se encontró código Java en la respuesta LLM")


def detect_class_name(java_source: str) -> str | None:
    match = CLASS_DECL_PATTERN.search(java_source)
    return match.group(2) if match else None


def normalize_java_source(
    java_source: str,
    expected_class_name: str,
    package_name: str,
) -> tuple[str, list[str]]:
    """Ajusta package y nombre de clase cuando el LLM se desvía levemente."""
    notes: list[str] = []
    result = java_source

    if not PACKAGE_PATTERN.search(result):
        result = f"package {package_name};\n\n{result.lstrip()}"
        notes.append(f"package {package_name} insertado")
    elif f"package {package_name};" not in result:
        result = PACKAGE_PATTERN.sub(f"package {package_name};", result, count=1)
        notes.append(f"package corregido a {package_name}")

    actual = detect_class_name(result)
    if actual and actual != expected_class_name:
        result = CLASS_DECL_PATTERN.sub(
            rf"\g<1>{expected_class_name}\g<3>",
            result,
            count=1,
        )
        notes.append(f"clase renombrada {actual} → {expected_class_name}")

    return result, notes


def validate(java_source: str, expected_class_name: str) -> None:
    if f"class {expected_class_name}" not in java_source:
        actual = detect_class_name(java_source)
        hint = f" (LLM devolvió: {actual})" if actual else ""
        raise ValueError(f"La clase generada no se llama {expected_class_name}{hint}")
    if "@Test" not in java_source:
        raise ValueError("La clase generada no contiene @Test")
    if "TODO" in java_source or "// FIXME" in java_source:
        raise ValueError("La clase generada contiene TODO/FIXME")


def save_raw_output(repo_root: Path, batch_id: str, raw: str) -> Path:
    out_dir = repo_root / "tools/test-generator/.output"
    out_dir.mkdir(parents=True, exist_ok=True)
    target = out_dir / f"{batch_id}-last-raw.txt"
    target.write_text(raw, encoding="utf-8")
    return target


def write_test_file(
    repo_root: Path,
    package_name: str,
    class_name: str,
    llm_raw_output: str,
) -> tuple[Path, list[str]]:
    java_source = extract_java(llm_raw_output)
    java_source, notes = normalize_java_source(java_source, class_name, package_name)
    validate(java_source, class_name)

    output_dir = repo_root / "backend/src/test/java" / package_name.replace(".", "/")
    output_dir.mkdir(parents=True, exist_ok=True)
    target = output_dir / f"{class_name}.java"
    target.write_text(java_source, encoding="utf-8")
    return target, notes
