from __future__ import annotations

import json
from dataclasses import dataclass
from pathlib import Path


@dataclass(frozen=True)
class BatchDefinition:
    source_relative_path: str
    output_class_name: str
    output_package: str
    context_test_relative_paths: list[str]
    max_tests: int
    notes: str


def batches_file() -> Path:
    return Path(__file__).resolve().parent.parent / "batches.json"


def load_batches() -> dict[str, BatchDefinition]:
    raw = json.loads(batches_file().read_text(encoding="utf-8"))
    result: dict[str, BatchDefinition] = {}
    for batch_id, data in raw.get("batches", {}).items():
        result[batch_id.upper()] = BatchDefinition(
            source_relative_path=data["sourceRelativePath"],
            output_class_name=data["outputClassName"],
            output_package=data["outputPackage"],
            context_test_relative_paths=list(data.get("contextTestRelativePaths", [])),
            max_tests=int(data.get("maxTests", 8)),
            notes=str(data.get("notes", "")),
        )
    return result


def print_catalog() -> None:
    batches = load_batches()
    print("Lotes disponibles (Fase 4):")
    for batch_id, batch in batches.items():
        print(
            f"  {batch_id} — {batch.source_relative_path} → "
            f"{batch.output_class_name} ({batch.max_tests} tests max)"
        )
