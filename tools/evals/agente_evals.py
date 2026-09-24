#!/usr/bin/env python3
"""Evals offline SIGESA — captura, evaluación determinista y compuerta."""
from __future__ import annotations

import argparse
import json
import subprocess
import sys
from datetime import datetime, timezone
from pathlib import Path

import config
from evals.catalog import (
    ARTIFACTS_DIR,
    DEFAULT_DATASET,
    PROMPTS_DIR,
    REPORTS_DIR,
    list_cases,
    load_dataset,
    response_path,
)
from evals.pass_evaluator import evaluate_criterios_pass

EVALS_DIR = Path(__file__).resolve().parent

try:
    from dotenv import load_dotenv

    load_dotenv(EVALS_DIR / ".env")
    load_dotenv(config.REPO_ROOT / ".env")
except ImportError:
    pass

_LOGIN = {
    "CC": ("cc@umss.edu.bo", "CoordDemo2026!"),
    "JD": ("jd@umss.edu.bo", "JefeDemo2026!"),
}

CRITICAL_IDS = frozenset({"HECH-CRIT-001", "SIN-CRIT-001", "FUE-CRIT-001", "SEC-CRIT-001"})
MIN_PASS_TOTAL = 12


def _login_token(login_as: str) -> str:
    import urllib.request

    key = (login_as or "CC").upper()
    email, password = _LOGIN.get(key, _LOGIN["CC"])
    payload = json.dumps({"email": email, "password": password}).encode()
    req = urllib.request.Request(
        f"{config.api_base()}/api/v1/auth/login",
        data=payload,
        headers={"Content-Type": "application/json"},
        method="POST",
    )
    with urllib.request.urlopen(req, timeout=30) as resp:
        data = json.loads(resp.read().decode())
    token = data.get("accessToken")
    if not token:
        raise SystemExit("FAIL: login sin accessToken")
    return token


def _chat_once(case: dict, token: str) -> dict:
    import urllib.error
    import urllib.request

    ctx = dict(case.get("context") or {})
    ctx.setdefault("agent", case.get("agent", "general"))
    body = {
        "message": case["mensaje"],
        "history": case.get("history") or [],
        "context": ctx,
    }
    req = urllib.request.Request(
        f"{config.api_base()}/api/v1/assistant/chat",
        data=json.dumps(body).encode(),
        headers={
            "Content-Type": "application/json",
            "Authorization": f"Bearer {token}",
        },
        method="POST",
    )
    record: dict = {
        "caseId": case.get("id"),
        "capturedAt": datetime.now(timezone.utc).isoformat(),
        "request": body,
    }
    try:
        with urllib.request.urlopen(req, timeout=120) as resp:
            record["httpStatus"] = resp.status
            resp_body = json.loads(resp.read().decode())
            record["response"] = resp_body
            record["reply"] = str(resp_body.get("reply") or "")
            record["steps"] = resp_body.get("steps") or []
    except urllib.error.HTTPError as err:
        record["httpStatus"] = err.code
        raw = err.read()
        try:
            record["response"] = json.loads(raw.decode())
        except Exception:
            record["response"] = {"raw": raw.decode()[:2000]}
        record["reply"] = ""
        record["steps"] = []
    return record


def cmd_listar(args: argparse.Namespace) -> int:
    cases = list_cases(load_dataset(Path(args.dataset) if args.dataset else None), tipo=args.tipo)
    if args.id:
        cases = [c for c in cases if c.get("id") == args.id]
    for c in cases:
        crit = "CRIT" if c.get("critical") else "    "
        print(f"{c['id']}\t{crit}\t{c.get('tipo')}\t{c.get('agent')}\t{c.get('loginAs', 'CC')}")
    print(f"\nTotal: {len(cases)}")
    return 0


def cmd_snapshot_prompt(args: argparse.Namespace) -> int:
    version = args.prompt_version
    dest_dir = PROMPTS_DIR / version
    dest_dir.mkdir(parents=True, exist_ok=True)
    yaml_path = config.REPO_ROOT / "backend" / "src" / "main" / "resources" / "application.yaml"
    text = yaml_path.read_text(encoding="utf-8")
    # Extracción simple del bloque system-prompt
    marker = "system-prompt: >-"
    if marker not in text:
        raise SystemExit("No se encontró system-prompt en application.yaml")
    chunk = text.split(marker, 1)[1].split("\n    max-tool-iterations:", 1)[0]
    lines = []
    for line in chunk.splitlines():
        if line.startswith("      "):
            lines.append(line[6:])
        elif line.strip():
            lines.append(line.strip())
    prompt = "\n".join(lines).strip() + "\n"
    out = dest_dir / "system_prompt.txt"
    out.write_text(prompt, encoding="utf-8")
    meta = {
        "promptVersion": version,
        "source": str(yaml_path.relative_to(config.REPO_ROOT)),
        "savedAt": datetime.now(timezone.utc).isoformat(),
    }
    (dest_dir / "meta.json").write_text(json.dumps(meta, indent=2) + "\n", encoding="utf-8")
    print(f"OK: {out}")
    return 0


def cmd_capture(args: argparse.Namespace) -> int:
    dataset = load_dataset(Path(args.dataset) if args.dataset else None)
    cases = list_cases(dataset, case_id=args.id)
    if not cases:
        print("No hay casos.")
        return 1
    version = args.prompt_version
    out_dir = ARTIFACTS_DIR / "responses" / version
    out_dir.mkdir(parents=True, exist_ok=True)
    git_commit = ""
    try:
        git_commit = subprocess.check_output(
            ["git", "rev-parse", "HEAD"],
            cwd=config.REPO_ROOT,
            text=True,
            stderr=subprocess.DEVNULL,
        ).strip()
    except Exception:
        pass
    manifest = {
        "promptVersion": version,
        "dataset": str(DEFAULT_DATASET.relative_to(EVALS_DIR)),
        "apiBase": config.api_base(),
        "capturedAt": datetime.now(timezone.utc).isoformat(),
        "gitCommit": git_commit,
        "cases": [],
    }
    for case in cases:
        login_as = case.get("loginAs") or "CC"
        token = _login_token(login_as)
        record = _chat_once(case, token)
        path = response_path(version, case["id"])
        path.write_text(json.dumps(record, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        manifest["cases"].append({"id": case["id"], "file": path.name, "httpStatus": record.get("httpStatus")})
        print(f"OK {case['id']} http={record.get('httpStatus')} -> {path.relative_to(EVALS_DIR)}")
    (out_dir / "manifest.json").write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"Manifest: {out_dir / 'manifest.json'}")
    return 0


def _load_capture(version: str, case_id: str) -> dict | None:
    path = response_path(version, case_id)
    if not path.is_file():
        return None
    return json.loads(path.read_text(encoding="utf-8"))


def cmd_eval(args: argparse.Namespace) -> int:
    dataset = load_dataset(Path(args.dataset) if args.dataset else None)
    cases = list_cases(dataset, case_id=args.id)
    version = args.prompt_version
    results: list[dict] = []
    for case in cases:
        cid = case["id"]
        cap = _load_capture(version, cid)
        if not cap:
            results.append(
                {
                    "id": cid,
                    "critical": bool(case.get("critical")),
                    "pass": False,
                    "error": f"sin captura en artifacts/responses/{version}/{cid}.json",
                }
            )
            continue
        status = int(cap.get("httpStatus") or 0)
        reply = str(cap.get("reply") or "")
        steps = cap.get("steps") or []
        body = cap.get("response") if isinstance(cap.get("response"), dict) else None
        passed, failures, matched = evaluate_criterios_pass(
            case, status=status, reply=reply, body=body, steps=steps
        )
        results.append(
            {
                "id": cid,
                "tipo": case.get("tipo"),
                "critical": bool(case.get("critical")),
                "requiere_juez": bool(case.get("requiere_juez")),
                "pass": passed,
                "failures": failures,
                "matched": matched,
                "httpStatus": status,
            }
        )
        mark = "PASS" if passed else "FAIL"
        print(f"{mark} {cid} (critical={case.get('critical')})")

    pass_count = sum(1 for r in results if r.get("pass"))
    crit_fail = [r["id"] for r in results if r.get("critical") and not r.get("pass")]
    report = {
        "promptVersion": version,
        "evaluatedAt": datetime.now(timezone.utc).isoformat(),
        "mode": "deterministic",
        "passCount": pass_count,
        "total": len(results),
        "criticalFailures": crit_fail,
        "results": results,
    }
    out = Path(args.report) if args.report else REPORTS_DIR / f"deterministic-{version}.json"
    if not out.is_absolute():
        out = EVALS_DIR / out
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"\n{pass_count}/{len(results)} PASS | críticos fallidos: {crit_fail or 'ninguno'}")
    print(f"Informe: {out}")
    return 0


def cmd_gate(args: argparse.Namespace) -> int:
    report_path = Path(args.report) if args.report else REPORTS_DIR / f"deterministic-{args.prompt_version}.json"
    if not report_path.is_absolute():
        report_path = EVALS_DIR / report_path
    if not report_path.is_file():
        print(f"Falta informe: {report_path} — ejecuta: ./run.sh eval --prompt-version {args.prompt_version}")
        return 1
    report = json.loads(report_path.read_text(encoding="utf-8"))
    pass_count = int(report.get("passCount", 0))
    total = int(report.get("total", 0))
    crit_fail = report.get("criticalFailures") or []
    ok_crit = len(crit_fail) == 0
    ok_total = pass_count >= MIN_PASS_TOTAL
    print(f"Críticos: {len(crit_fail)} fallos (requiere 0) -> {'OK' if ok_crit else 'FAIL'}")
    print(f"General: {pass_count}/{total} (requiere >={MIN_PASS_TOTAL}) -> {'OK' if ok_total else 'FAIL'}")
    if ok_crit and ok_total:
        print("COMPUERTA: APTO (determinista)")
        return 0
    print("COMPUERTA: NO APTO (exit 1)")
    return 1


def cmd_show_config(_: argparse.Namespace) -> int:
    cases = load_dataset()
    print(f"repo={config.REPO_ROOT}")
    print(f"api={config.api_base()}")
    print(f"dataset={len(cases)} casos")
    return 0


def main() -> int:
    parser = argparse.ArgumentParser(description="SIGESA offline evals")
    sub = parser.add_subparsers(dest="cmd", required=True)

    p_list = sub.add_parser("listar")
    p_list.add_argument("--dataset", default=None)
    p_list.add_argument("--id", default=None)
    p_list.add_argument("--tipo", default=None)

    p_snap = sub.add_parser("snapshot-prompt", help="Guardar system prompt desde application.yaml")
    p_snap.add_argument("prompt_version", nargs="?", default="v1")

    p_cap = sub.add_parser("capture", help="POST chat y guardar respuestas (Fase 3)")
    p_cap.add_argument("--prompt-version", default="v1")
    p_cap.add_argument("--dataset", default=None)
    p_cap.add_argument("--id", default=None)

    p_eval = sub.add_parser("eval", help="Eval determinista sobre capturas (Fase 4)")
    p_eval.add_argument("--prompt-version", default="v1")
    p_eval.add_argument("--dataset", default=None)
    p_eval.add_argument("--id", default=None)
    p_eval.add_argument("--report", default=None)

    p_gate = sub.add_parser("gate", help="Compuerta THRESHOLDS.md (Fase 8)")
    p_gate.add_argument("--prompt-version", default="v1")
    p_gate.add_argument("--report", default=None)

    sub.add_parser("show-config")

    args = parser.parse_args()
    handlers = {
        "listar": cmd_listar,
        "snapshot-prompt": cmd_snapshot_prompt,
        "capture": cmd_capture,
        "eval": cmd_eval,
        "gate": cmd_gate,
        "show-config": cmd_show_config,
    }
    return handlers[args.cmd](args)


if __name__ == "__main__":
    sys.exit(main())
