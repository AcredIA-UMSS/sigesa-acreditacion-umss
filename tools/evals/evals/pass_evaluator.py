from __future__ import annotations

import re
from typing import Any


def _text_values(cond: dict[str, Any]) -> list[str]:
    if cond.get("valores"):
        return [str(v) for v in cond["valores"] if v]
    single = cond.get("patron") or cond.get("value") or cond.get("valor")
    return [str(single)] if single else []


def _step_tool_ids(steps: list[Any] | None) -> list[str]:
    ids: list[str] = []
    for step in steps or []:
        if isinstance(step, dict):
            tid = step.get("toolId") or step.get("tool_id")
            if tid:
                ids.append(str(tid))
    return ids


def _condition_passes(
    cond: dict[str, Any],
    *,
    status: int,
    reply: str,
    body: dict[str, Any] | None,
    tool_ids: list[str],
) -> tuple[bool, str]:
    tipo = (cond.get("tipo") or cond.get("type") or "").strip()
    text = reply or ""
    lower = text.lower()
    err_body = str(body) if body else ""

    if tipo == "texto_contiene":
        vals = _text_values(cond)
        ci = cond.get("caseInsensitive", True)
        for val in vals:
            hay = val.lower() in lower if ci else val in text
            if hay:
                return True, f"texto_contiene: {val!r}"
        return False, f"texto_contiene: ninguno de {vals!r}"

    if tipo == "texto_no_contiene":
        vals = _text_values(cond)
        ci = cond.get("caseInsensitive", True)
        for val in vals:
            hay = val.lower() in lower if ci else val in text
            if hay:
                return False, f"texto_no_contiene violado: {val!r}"
        return True, "texto_no_contiene: ok"

    if tipo in ("regex_en_texto", "regex_contiene"):
        patron = str(cond.get("patron") or cond.get("pattern") or "")
        if patron and re.search(patron, text, re.IGNORECASE | re.DOTALL):
            return True, f"regex_en_texto: {patron!r}"
        return False, f"regex_en_texto: no match {patron!r}"

    if tipo == "regex_no_coincide":
        patron = str(cond.get("patron") or cond.get("pattern") or "")
        if patron and re.search(patron, text, re.IGNORECASE | re.DOTALL):
            return False, f"regex_no_coincide violado: {patron!r}"
        return True, "regex_no_coincide: ok"

    if tipo == "herramienta_no_ejecutada":
        name = str(cond.get("nombre") or cond.get("toolId") or cond.get("tool_id") or "")
        if name and name in tool_ids:
            return False, f"herramienta_no_ejecutada violado: {name!r}"
        return True, f"herramienta_no_ejecutada: {name or 'ok'}"

    if tipo == "herramienta_ejecutada":
        name = str(cond.get("nombre") or cond.get("toolId") or cond.get("tool_id") or "")
        if name and name in tool_ids:
            return True, f"herramienta_ejecutada: {name!r}"
        return False, f"herramienta_ejecutada: falta {name!r}"

    if tipo == "http_status":
        want = int(cond.get("valor") or cond.get("value") or 0)
        if status == want:
            return True, f"http_status: {want}"
        return False, f"http_status: esperado {want}, got {status}"

    return False, f"tipo desconocido: {tipo!r}"


def evaluate_criterios_pass(
    case: dict[str, Any],
    *,
    status: int,
    reply: str,
    body: dict[str, Any] | None,
    steps: list[Any] | None = None,
) -> tuple[bool, list[str], list[str]]:
    """
    PASS del asistente según criterios_pass del dataset dorado.
    Retorna (passed, failures, matched_ok).
    """
    block = case.get("criterios_pass") or {}
    agg = (block.get("tipo") or "todas").lower()
    condiciones = block.get("condiciones") or []
    if not condiciones:
        return False, ["sin criterios_pass"], []

    tool_ids = _step_tool_ids(steps or ((body or {}).get("steps") if body else None))
    results: list[tuple[bool, str]] = [
        _condition_passes(c, status=status, reply=reply, body=body, tool_ids=tool_ids)
        for c in condiciones
    ]
    matched = [msg for ok, msg in results if ok]
    failures = [msg for ok, msg in results if not ok]

    if agg == "cualquiera":
        passed = any(ok for ok, _ in results)
        if passed:
            return True, [], matched
        return False, failures or ["ninguna condición cumplida (cualquiera)"], []

    passed = all(ok for ok, _ in results)
    if passed:
        return True, [], matched
    return False, failures, matched
