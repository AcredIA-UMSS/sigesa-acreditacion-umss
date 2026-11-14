#!/usr/bin/env python3
"""Evals offline SIGESA — captura, evaluación determinista y compuerta."""
from __future__ import annotations

import argparse
import json
import os
import subprocess
import sys
from datetime import datetime, timezone
from pathlib import Path

import config
from evals.catalog import (
    ARTIFACTS_DIR,
    DEFAULT_DATASET,
    HUMAN_LABELS_PATH,
    JUDGE_LABELS_PATH,
    KAPPA_MIN,
    LABELS_DIR,
    PROMPTS_DIR,
    REPORTS_DIR,
    list_cases,
    load_dataset,
    response_path,
)
from evals.judge import call_judge_llm, judge_llm_base_url, judge_model
from evals.kappa import cohen_kappa, confusion_matrix
from evals.pass_evaluator import evaluate_criterios_pass

EVALS_DIR = Path(__file__).resolve().parent


def _resolve_prompt_version(cli_flag: str | None, positional: str | None = None) -> str:
    """Carpeta artifacts/responses/<version>/ — positional gana sobre flag; env EVALS_PROMPT_VERSION."""
    if positional and positional.strip():
        return positional.strip()
    if cli_flag and str(cli_flag).strip():
        return str(cli_flag).strip()
    return (os.getenv("EVALS_PROMPT_VERSION") or "v1").strip()


def _load_env_file(path: Path, *, override: bool = False) -> None:
    if not path.is_file():
        return
    for line in path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, _, value = line.partition("=")
        key = key.strip()
        value = value.strip().strip("'").strip('"')
        if not key:
            continue
        if override or key not in os.environ:
            os.environ[key] = value


_load_env_file(config.REPO_ROOT / ".env")
_load_env_file(EVALS_DIR / ".env", override=True)
try:
    from dotenv import load_dotenv

    load_dotenv(config.REPO_ROOT / ".env", override=True)
    load_dotenv(EVALS_DIR / ".env", override=True)
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
    version = _resolve_prompt_version(
        getattr(args, "prompt_version", None),
        getattr(args, "version", None),
    )
    out_dir = ARTIFACTS_DIR / "responses" / version
    out_dir.mkdir(parents=True, exist_ok=True)
    rel_out = out_dir.relative_to(EVALS_DIR)
    print(f"== capture → {rel_out}/ (promptVersion={version}; no confundir con snapshot-prompt) ==")
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
    manifest_path = out_dir / "manifest.json"
    merged: dict[str, dict] = {}
    if args.id and manifest_path.is_file():
        try:
            prev = json.loads(manifest_path.read_text(encoding="utf-8"))
            for entry in prev.get("cases") or []:
                if entry.get("id"):
                    merged[entry["id"]] = entry
        except Exception:
            pass
    elif not args.id:
        merged = {}

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
        record["promptVersion"] = version
        path = response_path(version, case["id"])
        path.write_text(json.dumps(record, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        merged[case["id"]] = {
            "id": case["id"],
            "file": path.name,
            "httpStatus": record.get("httpStatus"),
        }
        print(f"OK {case['id']} http={record.get('httpStatus')} -> {path.relative_to(EVALS_DIR)}")
    order = [c["id"] for c in load_dataset(Path(args.dataset) if args.dataset else None)]
    manifest["cases"] = [merged[cid] for cid in order if cid in merged]
    manifest_path.write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
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


def cmd_compare(args: argparse.Namespace) -> int:
    v1, v2 = args.v1, args.v2
    path1 = REPORTS_DIR / f"deterministic-{v1}.json"
    path2 = REPORTS_DIR / f"deterministic-{v2}.json"
    if not path1.is_file():
        print(f"Falta {path1} — ejecuta: ./run.sh eval --prompt-version {v1}")
        return 1
    if not path2.is_file():
        print(f"Falta {path2} — ejecuta: ./run.sh eval --prompt-version {v2}")
        return 1
    rep1 = json.loads(path1.read_text(encoding="utf-8"))
    rep2 = json.loads(path2.read_text(encoding="utf-8"))
    by1 = {r["id"]: r for r in rep1.get("results") or []}
    by2 = {r["id"]: r for r in rep2.get("results") or []}
    ids = sorted(set(by1) | set(by2))
    lines = [
        f"# Compare evals {v1} vs {v2}",
        "",
        f"- **{v1}:** {rep1.get('passCount')}/{rep1.get('total')} PASS, críticos fallidos: "
        f"{rep1.get('criticalFailures') or 'ninguno'}",
        f"- **{v2}:** {rep2.get('passCount')}/{rep2.get('total')} PASS, críticos fallidos: "
        f"{rep2.get('criticalFailures') or 'ninguno'}",
        "",
        "| Caso | Crítico | v1 | v2 | Δ |",
        "|------|---------|----|----|---|",
    ]
    for cid in ids:
        p1 = by1.get(cid, {})
        p2 = by2.get(cid, {})
        b1 = "PASS" if p1.get("pass") else "FAIL"
        b2 = "PASS" if p2.get("pass") else "FAIL"
        crit = "✓" if p1.get("critical") or p2.get("critical") else ""
        if b1 == b2:
            delta = "—"
        elif b1 == "FAIL" and b2 == "PASS":
            delta = "↑"
        else:
            delta = "↓"
        lines.append(f"| {cid} | {crit} | {b1} | {b2} | {delta} |")
    lines.append("")
    out = REPORTS_DIR / f"compare-{v1}-{v2}.md"
    out.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"OK: {out}")
    return 0


def cmd_clone_responses(args: argparse.Namespace) -> int:
    """Copia capturas FROM → TO (sin LLM); actualiza promptVersion en cada JSON."""
    src_dir = ARTIFACTS_DIR / "responses" / args.from_version
    dst_dir = ARTIFACTS_DIR / "responses" / args.to_version
    if not src_dir.is_dir():
        print(f"No existe {src_dir}")
        return 1
    if dst_dir.resolve() == src_dir.resolve():
        print("FROM y TO deben ser distintos")
        return 1
    dst_dir.mkdir(parents=True, exist_ok=True)
    count = 0
    for path in sorted(src_dir.glob("*.json")):
        if path.name == "manifest.json":
            continue
        cap = json.loads(path.read_text(encoding="utf-8"))
        cap["promptVersion"] = args.to_version
        cap["clonedFrom"] = args.from_version
        (dst_dir / path.name).write_text(
            json.dumps(cap, ensure_ascii=False, indent=2) + "\n",
            encoding="utf-8",
        )
        count += 1
    print(f"Clonados {count} archivos: {src_dir.name}/ → {dst_dir.name}/")
    mf_args = argparse.Namespace(prompt_version=args.to_version, dataset=None)
    return 0 if count and cmd_rebuild_manifest(mf_args) == 0 else 1


def cmd_rebuild_manifest(args: argparse.Namespace) -> int:
    """Reconstruye manifest.json desde JSONL + archivos existentes (sin POST)."""
    version = args.prompt_version
    dataset = load_dataset(Path(args.dataset) if args.dataset else None)
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
    cases_meta = []
    for case in dataset:
        cid = case["id"]
        path = response_path(version, cid)
        if not path.is_file():
            continue
        cap = json.loads(path.read_text(encoding="utf-8"))
        cases_meta.append({"id": cid, "file": path.name, "httpStatus": cap.get("httpStatus")})
    manifest = {
        "promptVersion": version,
        "dataset": str(DEFAULT_DATASET.relative_to(EVALS_DIR)),
        "apiBase": config.api_base(),
        "capturedAt": datetime.now(timezone.utc).isoformat(),
        "gitCommit": git_commit,
        "cases": cases_meta,
    }
    (out_dir / "manifest.json").write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"OK {len(cases_meta)} casos -> {out_dir / 'manifest.json'}")
    return 0 if cases_meta else 1


# Ajustes humanos tras revisión de capturas (semilla ≠ copia ciega del determinista).
_HUMAN_LABEL_OVERRIDES: dict[str, dict] = {
    "SIN-002": {
        "pass": False,
        "notes": "HTTP 401 / reply vacío — no cumple comportamiento esperado de fases.",
    },
}


def _load_jsonl(path: Path) -> list[dict]:
    if not path.is_file():
        return []
    rows: list[dict] = []
    for line in path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if line:
            rows.append(json.loads(line))
    return rows


def _write_jsonl(path: Path, rows: list[dict]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(
        "\n".join(json.dumps(r, ensure_ascii=False) for r in rows) + "\n",
        encoding="utf-8",
    )


def cmd_seed_human_labels(args: argparse.Namespace) -> int:
    version = args.prompt_version
    report_path = REPORTS_DIR / f"deterministic-{version}.json"
    if not report_path.is_file():
        print(f"Falta {report_path} — ejecuta: ./run.sh eval --prompt-version {version}")
        return 1
    report = json.loads(report_path.read_text(encoding="utf-8"))
    by_id = {r["id"]: r for r in report.get("results") or []}
    dataset = load_dataset(Path(args.dataset) if args.dataset else None)
    rows: list[dict] = []
    now = datetime.now(timezone.utc).isoformat()
    for case in dataset:
        cid = case["id"]
        det = by_id.get(cid, {})
        passed = bool(det.get("pass"))
        notes = "Semilla desde eval determinista; revisar en defensa."
        override = _HUMAN_LABEL_OVERRIDES.get(cid)
        if override:
            passed = bool(override.get("pass", passed))
            notes = str(override.get("notes") or notes)
        rows.append(
            {
                "id": cid,
                "pass": passed,
                "scale": None,
                "promptVersion": version,
                "labeledAt": now,
                "labeledBy": args.labeled_by,
                "notes": notes,
                "deterministicPass": det.get("pass"),
            }
        )
    _write_jsonl(HUMAN_LABELS_PATH, rows)
    print(f"OK {len(rows)} etiquetas -> {HUMAN_LABELS_PATH}")
    print("Edita el JSONL si discrepas; luego: ./run.sh judge-run --prompt-version", version)
    return 0


def cmd_judge_run(args: argparse.Namespace) -> int:
    version = args.prompt_version
    dataset = load_dataset(Path(args.dataset) if args.dataset else None)
    cases = list_cases(dataset, case_id=args.id)
    if not cases:
        print("No hay casos.")
        return 1
    existing = {r["id"]: r for r in _load_jsonl(JUDGE_LABELS_PATH)}
    updated: dict[str, dict] = {}
    now = datetime.now(timezone.utc).isoformat()
    print(f"Juez LLM: {judge_model()} @ {judge_llm_base_url()}")
    for case in cases:
        cid = case["id"]
        cap = _load_capture(version, cid)
        if not cap:
            print(f"SKIP {cid}: sin captura v{version}")
            continue
        verdict = call_judge_llm(case, cap, temperature=0.0)
        row = {
            "id": cid,
            "pass": verdict.get("pass"),
            "rationale": verdict.get("rationale"),
            "promptVersion": version,
            "judgedAt": now,
            "model": verdict.get("model"),
            "httpStatus": verdict.get("httpStatus"),
            "error": verdict.get("error"),
            "requiere_juez": bool(case.get("requiere_juez")),
        }
        updated[cid] = row
        if verdict.get("pass") is None:
            mark = "?"
            err_hint = verdict.get("httpStatus") or verdict.get("error")
            print(f"{mark} {cid} (juez) {err_hint}")
        else:
            mark = "PASS" if verdict.get("pass") else "FAIL"
            print(f"{mark} {cid} (juez)")
    if args.id or args.merge:
        merged = {**existing, **updated}
        rows = [merged[k] for k in sorted(merged)]
    else:
        rows = [updated[k] for k in sorted(updated)]
    _write_jsonl(JUDGE_LABELS_PATH, rows)
    ok_verdicts = sum(1 for r in rows if r.get("pass") is not None)
    print(f"OK -> {JUDGE_LABELS_PATH} ({ok_verdicts}/{len(rows)} verdictos PASS/FAIL)")
    if ok_verdicts == 0:
        print(
            "ERROR: ningún verdicto del juez — revisa SIGESA_JUDGE_BASE_URL + API key "
            "(Groq: https://api.groq.com/openai/v1 + GROQ_API_KEY; "
            "Open WebUI: http://127.0.0.1:3001/api + key de OWUI)."
        )
        return 1
    if ok_verdicts < len(rows):
        print("AVISO: algunos casos fallaron; reintenta con --id ... --merge")
    return 0


def cmd_judge_calibrate(args: argparse.Namespace) -> int:
    human_rows = _load_jsonl(HUMAN_LABELS_PATH)
    judge_rows = _load_jsonl(JUDGE_LABELS_PATH)
    if not human_rows:
        print(f"Falta {HUMAN_LABELS_PATH} — ./run.sh seed-human-labels --prompt-version v3")
        return 1
    if not judge_rows:
        print(f"Falta {JUDGE_LABELS_PATH} — ./run.sh judge-run --prompt-version v3")
        return 1
    h_map = {r["id"]: r for r in human_rows}
    j_map = {r["id"]: r for r in judge_rows}
    ids = sorted(set(h_map) & set(j_map))
    pairs: list[dict] = []
    human_b: list[bool] = []
    judge_b: list[bool] = []
    disagreements: list[dict] = []
    skipped_null_judge = 0
    skipped_null_human = 0
    for cid in ids:
        h_pass = h_map[cid].get("pass")
        j_pass = j_map[cid].get("pass")
        if h_pass is None:
            skipped_null_human += 1
            continue
        if j_pass is None:
            skipped_null_judge += 1
            continue
        hb, jb = bool(h_pass), bool(j_pass)
        human_b.append(hb)
        judge_b.append(jb)
        pairs.append({"id": cid, "human": hb, "judge": jb, "agree": hb == jb})
        if hb != jb:
            disagreements.append(
                {
                    "id": cid,
                    "human": hb,
                    "judge": jb,
                    "humanNotes": h_map[cid].get("notes"),
                    "judgeRationale": j_map[cid].get("rationale"),
                }
            )
    if not human_b:
        print(
            "ERROR: 0 pares humano↔juez — el juez no produjo PASS/FAIL "
            f"(null juez: {skipped_null_judge}/{len(ids)}). "
            "Corre ./run.sh judge-run --prompt-version v3 tras arreglar SIGESA_JUDGE_API_KEY."
        )
        sample = next((j_map[i] for i in ids if j_map[i].get("error")), None)
        if sample:
            print(f"  Ejemplo error juez: HTTP {sample.get('httpStatus')} {str(sample.get('error'))[:120]}")
        return 1
    kappa = cohen_kappa(human_b, judge_b)
    matrix = confusion_matrix(human_b, judge_b)
    req_judge_ids = [cid for cid in ids if j_map[cid].get("requiere_juez")]
    req_pairs = [p for p in pairs if p["id"] in req_judge_ids]
    req_human = [p["human"] for p in req_pairs]
    req_judge = [p["judge"] for p in req_pairs]
    kappa_req = cohen_kappa(req_human, req_judge) if len(req_pairs) >= 2 else None

    report = {
        "calibratedAt": datetime.now(timezone.utc).isoformat(),
        "casesCompared": len(pairs),
        "skippedNullJudge": skipped_null_judge,
        "skippedNullHuman": skipped_null_human,
        "cohenKappa": round(kappa, 4),
        "cohenKappaRequiereJuez": round(kappa_req, 4) if kappa_req is not None else None,
        "kappaMinRequired": KAPPA_MIN,
        "kappaOk": kappa >= KAPPA_MIN,
        "confusionMatrix": matrix,
        "disagreements": disagreements,
        "requiere_juez": {
            "ids": req_judge_ids,
            "agreement": sum(1 for p in req_pairs if p["agree"]),
            "total": len(req_pairs),
            "cohenKappa": round(kappa_req, 4) if kappa_req is not None else None,
        },
        "pairs": pairs,
    }
    out = REPORTS_DIR / "calibracion-juez.json"
    out.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"κ (15 casos) = {kappa:.4f} (requiere >={KAPPA_MIN}) -> {'OK' if report['kappaOk'] else 'FAIL'}")
    if kappa_req is not None:
        print(f"κ (solo requiere_juez, n={len(req_pairs)}) = {kappa_req:.4f}")
    print(f"Matriz: {matrix}")
    print(f"Desacuerdos: {len(disagreements)} — informe: {out}")
    if disagreements:
        for d in disagreements:
            print(f"  - {d['id']}: human={d['human']} juez={d['judge']}")
    return 0 if report["kappaOk"] else 1


def cmd_judge_gate(args: argparse.Namespace) -> int:
    path = REPORTS_DIR / "calibracion-juez.json"
    if not path.is_file():
        print(f"Falta {path} — ./run.sh judge-calibrate")
        return 1
    report = json.loads(path.read_text(encoding="utf-8"))
    kappa = float(report.get("cohenKappa", 0))
    ok = kappa >= KAPPA_MIN
    print(f"Calibración juez: κ={kappa:.4f} (>= {KAPPA_MIN}) -> {'APTO' if ok else 'NO APTO'}")
    return 0 if ok else 1


def cmd_show_config(_: argparse.Namespace) -> int:
    cases = load_dataset()
    print(f"repo={config.REPO_ROOT}")
    print(f"api={config.api_base()}")
    print(f"judge={judge_model()} @ {judge_llm_base_url()}")
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

    p_cap = sub.add_parser(
        "capture",
        help="POST chat → artifacts/responses/<VERSION>/ (independiente de snapshot-prompt)",
    )
    p_cap.add_argument(
        "--prompt-version",
        default=None,
        help="Carpeta destino (ej. v2). Default: v1 o EVALS_PROMPT_VERSION en .env",
    )
    p_cap.add_argument(
        "version",
        nargs="?",
        default=None,
        metavar="VERSION",
        help="Atajo: ./run.sh capture v2 --id HECH-001",
    )
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

    p_cmp = sub.add_parser("compare", help="Tabla v1 vs v2 desde informes deterministas (Fase 5)")
    p_cmp.add_argument("--v1", default="v1")
    p_cmp.add_argument("--v2", default="v2")

    p_mf = sub.add_parser("rebuild-manifest", help="Manifest desde capturas existentes (sin LLM)")
    p_mf.add_argument("--prompt-version", default="v1")
    p_mf.add_argument("--dataset", default=None)

    p_cl = sub.add_parser(
        "clone-responses",
        help="Copiar artifacts/responses/FROM → TO (p. ej. v2 v3 antes de re-capturar un caso)",
    )
    p_cl.add_argument("from_version", metavar="FROM")
    p_cl.add_argument("to_version", metavar="TO")

    p_hseed = sub.add_parser(
        "seed-human-labels",
        help="Fase 6: human.jsonl desde eval determinista + overrides",
    )
    p_hseed.add_argument("--prompt-version", default="v3")
    p_hseed.add_argument("--dataset", default=None)
    p_hseed.add_argument("--labeled-by", default="tech-lead")

    p_jrun = sub.add_parser("judge-run", help="Fase 6: LLM juez sobre capturas congeladas")
    p_jrun.add_argument("--prompt-version", default="v3")
    p_jrun.add_argument("--dataset", default=None)
    p_jrun.add_argument("--id", default=None)
    p_jrun.add_argument(
        "--merge",
        action="store_true",
        help="Conservar filas previas al correr un solo --id",
    )

    sub.add_parser("judge-calibrate", help="Fase 6: Cohen κ human vs judge → calibracion-juez.json")
    sub.add_parser("judge-gate", help="Fase 6: exit 0 si κ >= 0.6")

    sub.add_parser("show-config")

    args = parser.parse_args()
    handlers = {
        "listar": cmd_listar,
        "snapshot-prompt": cmd_snapshot_prompt,
        "capture": cmd_capture,
        "eval": cmd_eval,
        "gate": cmd_gate,
        "compare": cmd_compare,
        "rebuild-manifest": cmd_rebuild_manifest,
        "clone-responses": cmd_clone_responses,
        "seed-human-labels": cmd_seed_human_labels,
        "judge-run": cmd_judge_run,
        "judge-calibrate": cmd_judge_calibrate,
        "judge-gate": cmd_judge_gate,
        "show-config": cmd_show_config,
    }
    return handlers[args.cmd](args)


if __name__ == "__main__":
    sys.exit(main())
