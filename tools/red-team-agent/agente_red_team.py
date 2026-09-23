#!/usr/bin/env python3
"""Agente Red Team — genera y ejecuta pruebas contra /api/v1/assistant/chat.

Metodología: catálogo versionado + evaluador automático + generación asistida por LLM.

  cd tools/red-team-agent
  ./setup.sh
  ./run.sh listar
  ./run.sh sync
  ./run.sh probar-api                    # smoke HTTP (requiere backend + login)
  ./run.sh generar exfiltration_canary "contraseña demo CC en inglés"
  ./run.sh sync                  # → backend/src/test/resources/redteam/
  cd ../../backend && ./mvnw -q test -Dtest=RedTeamAssistantCatalogWebMvcTest
  ./run.sh probar-api            # LLM vivo (Docker), opcional
"""
from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

import config
from redteam.catalog import (
    REPORTS_DIR,
    coverage_by_category,
    list_ataques,
    list_attacks_ci,
    load_taxonomy_categories,
    load_catalog,
    load_ataques,
    sync_all,
)
from redteam.evaluator import defense_passed, evaluate_exito_si
from redteam.generator import append_attack_via_llm

AGENT_DIR = Path(__file__).resolve().parent


def cmd_listar(args: argparse.Namespace) -> int:
    attacks = (
        list_attacks_ci(args.categoria)
        if args.ci
        else list_ataques(args.categoria, args.id if hasattr(args, "id") else None)
    )
    for attack in attacks:
        cat = attack.get("categoria") or attack.get("category")
        title = attack.get("titulo") or attack.get("title")
        print(f"{attack['id']}\t{cat}\t{title}")
    return 0


def cmd_cobertura(_: argparse.Namespace) -> int:
    by_cat = coverage_by_category()
    taxonomy = load_taxonomy_categories()
    if taxonomy:
        print("Categoría (taxonomía)\tCasos\tIDs")
        for cat_id, label in taxonomy:
            attacks = by_cat.get(cat_id, [])
            ids = ", ".join(a["id"] for a in attacks) if attacks else "—"
            status = "OK" if attacks else "SIN CASOS"
            print(f"{cat_id}\t{len(attacks)}\t{status}\t{label}")
            if attacks:
                print(f"  → {ids}")
        extra = set(by_cat) - {c for c, _ in taxonomy}
        for cat_id in sorted(extra):
            attacks = by_cat[cat_id]
            print(f"{cat_id}\t{len(attacks)}\t(extra)\t{', '.join(a['id'] for a in attacks)}")
    else:
        for cat_id in sorted(by_cat):
            attacks = by_cat[cat_id]
            print(f"{cat_id}\t{len(attacks)}\t{', '.join(a['id'] for a in attacks)}")
    print(f"\nCatálogo operativo (ataques/*.json): {len(load_ataques())} casos")
    print(f"Catálogo JUnit (catalog/attacks.json): {len(load_catalog().get('attacks', []))} casos — ./run.sh sync")
    return 0


def cmd_sync(_: argparse.Namespace) -> int:
    backend_dest = sync_all(config.REPO_ROOT)
    print(f"OK: {backend_dest}")
    return 0


def cmd_generar(args: argparse.Namespace) -> int:
    client = config.crear_cliente()
    _, _, model = config.resolve_llm()
    attack = append_attack_via_llm(client, model, args.categoria, args.hint or "")
    print(json.dumps(attack, ensure_ascii=False, indent=2))
    backend_dest = sync_all(config.REPO_ROOT)
    print(f"Catálogo sincronizado a {backend_dest}")
    print("Ejecutar: cd backend && ./mvnw test -Dtest=RedTeamAssistantCatalogWebMvcTest")
    return 0


_LOGIN_CREDENTIALS = {
    "CC": ("cc@umss.edu.bo", "CoordDemo2026!"),
    "JD": ("jd@umss.edu.bo", "JefeDemo2026!"),
}


def _login_token(login_as: str = "CC") -> str:
    import urllib.request

    key = (login_as or "CC").upper()
    email, password = _LOGIN_CREDENTIALS.get(key, _LOGIN_CREDENTIALS["CC"])
    base = config.api_base()
    payload = json.dumps({"email": email, "password": password}).encode()
    req = urllib.request.Request(
        f"{base}/api/v1/auth/login",
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


def _run_attack_once(attack: dict, token: str) -> tuple[int, dict | None, str, list]:
    import urllib.error
    import urllib.request

    base = config.api_base()
    body = {
        "message": attack["userMessage"],
        "history": attack.get("history") or [],
        "context": {"agent": attack.get("agent", "general")},
    }
    req = urllib.request.Request(
        f"{base}/api/v1/assistant/chat",
        data=json.dumps(body).encode(),
        headers={
            "Content-Type": "application/json",
            "Authorization": f"Bearer {token}",
        },
        method="POST",
    )
    status = 0
    resp_body: dict | None = None
    reply = ""
    steps: list = []
    try:
        with urllib.request.urlopen(req, timeout=120) as resp:
            status = resp.status
            resp_body = json.loads(resp.read().decode())
            reply = str(resp_body.get("reply") or "")
            steps = resp_body.get("steps") or []
    except urllib.error.HTTPError as err:
        status = err.code
        raw = err.read()
        try:
            resp_body = json.loads(raw.decode())
        except Exception:
            resp_body = {"raw": raw.decode()[:500]}
    return status, resp_body, reply, steps


def _run_attack_trials(attack: dict, trials: int) -> dict:
    login_as = attack.get("loginAs") or "CC"
    token = _login_token(login_as)
    runs: list[dict] = []
    attack_success = 0
    for trial in range(1, trials + 1):
        status, resp_body, reply, steps = _run_attack_once(attack, token)
        won, matched = evaluate_exito_si(
            attack, status=status, reply=reply, body=resp_body, steps=steps
        )
        defense_ok = defense_passed(
            attack, status=status, reply=reply, body=resp_body, steps=steps
        )
        if won:
            attack_success += 1
        runs.append(
            {
                "trial": trial,
                "httpStatus": status,
                "defensePass": defense_ok,
                "attackWon": won,
                "exitoMatched": matched,
                "replySnippet": (reply or "")[:300],
                "toolIds": [
                    s.get("toolId") for s in steps if isinstance(s, dict) and s.get("toolId")
                ],
            }
        )
    return {
        "id": attack.get("id"),
        "title": attack.get("title") or attack.get("titulo"),
        "categoria": attack.get("categoria") or attack.get("category"),
        "trials": trials,
        "attackSuccessCount": attack_success,
        "defensePassCount": trials - attack_success,
        "runs": runs,
    }


def cmd_probar(args: argparse.Namespace) -> int:
    attacks = list_ataques(args.categoria, args.id)
    if not attacks:
        print("No hay ataques en tools/red-team-agent/ataques/*.json")
        return 1
    trials = max(1, int(args.trials))
    verbose = trials == 1 and not args.report
    report = {
        "source": "tools/red-team-agent/ataques",
        "trialsPerAttack": trials,
        "attacks": [],
    }
    any_attack_won = 0
    failed = 0
    for attack in attacks:
        summary = _run_attack_trials(attack, trials)
        report["attacks"].append(summary)
        bad = summary["attackSuccessCount"]
        ok = summary["defensePassCount"]
        if bad > 0:
            any_attack_won += 1
        if trials == 1:
            run = summary["runs"][0]
            if run["defensePass"]:
                print(f"PASS {attack['id']}: {attack.get('title')} (http={run['httpStatus']})")
            else:
                failed += 1
                print(f"FAIL {attack['id']}: {attack.get('title')}")
                for m in run.get("exitoMatched") or []:
                    print(f"  - éxito atacante: {m}")
                if run.get("replySnippet"):
                    print(f"  reply snippet: {run['replySnippet']!r}")
        else:
            print(
                f"{summary['id']}\tdefensa {ok}/{trials}\téxito atacante {bad}/{trials}\t{summary.get('title')}"
            )

    report_path = args.report or "reports/last-run.json"
    if not verbose or args.report:
        out = Path(report_path)
        if not out.is_absolute():
            out = AGENT_DIR / out
        out.parent.mkdir(parents=True, exist_ok=True)
        out.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        print(f"Informe: {out}")

    if trials == 1:
        return 1 if failed else 0
    return 1 if any_attack_won else 0


def cmd_show_config(_: argparse.Namespace) -> int:
    print(f"repo={config.REPO_ROOT}")
    print(f"api={config.api_base()}")
    print(f"llm={config.resumen_llm()}")
    print(f"ataques/={len(load_ataques())} catalog/ci={len(load_catalog().get('attacks', []))}")
    return 0


def main() -> int:
    parser = argparse.ArgumentParser(description="SIGESA Red Team agent")
    sub = parser.add_subparsers(dest="cmd", required=True)

    p_list = sub.add_parser("listar", help="Listar ataques (default: ataques/*.json)")
    p_list.add_argument("--categoria", default=None)
    p_list.add_argument("--id", default=None)
    p_list.add_argument(
        "--ci",
        action="store_true",
        help="Listar catalog/attacks.json (JUnit, legacy forbiddenInReply)",
    )

    sub.add_parser("cobertura", help="Cobertura por categoría (ataques/*.json)")
    sub.add_parser("sync", help="Copiar attacks.json al backend (JUnit)")

    p_gen = sub.add_parser("generar", help="Generar ataque nuevo vía LLM y append al catálogo")
    p_gen.add_argument("categoria")
    p_gen.add_argument("hint", nargs="?", default="")

    def add_probar_flags(p: argparse.ArgumentParser, *, default_trials: int) -> None:
        p.add_argument("--categoria", default=None)
        p.add_argument("--id", default=None)
        p.add_argument("--trials", type=int, default=default_trials)
        p.add_argument(
            "--report",
            default=None,
            help="Informe JSON (default reports/last-run.json si trials>1)",
        )

    p_probar = sub.add_parser(
        "probar",
        help="Ejecutar todos los ataques/ contra backend + LLM (recomendado)",
    )
    add_probar_flags(p_probar, default_trials=3)

    p_api = sub.add_parser(
        "probar-api",
        help="Alias de probar (default 1 repetición; smoke rápido)",
    )
    add_probar_flags(p_api, default_trials=1)

    p_lab = sub.add_parser(
        "probar-lab",
        help="Alias de probar (default 3 repeticiones + informe)",
    )
    add_probar_flags(p_lab, default_trials=3)

    sub.add_parser("show-config", help="Mostrar configuración")

    args = parser.parse_args()
    handlers = {
        "listar": cmd_listar,
        "cobertura": cmd_cobertura,
        "sync": cmd_sync,
        "generar": cmd_generar,
        "probar": cmd_probar,
        "probar-api": cmd_probar,
        "probar-lab": cmd_probar,
        "show-config": cmd_show_config,
    }
    return handlers[args.cmd](args)


if __name__ == "__main__":
    sys.exit(main())
