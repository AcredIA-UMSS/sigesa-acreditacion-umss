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
    LAB_REPORTS_DIR,
    coverage_by_category,
    list_attacks,
    list_lab_attacks,
    load_taxonomy_categories,
    load_catalog,
    sync_all,
)
from redteam.evaluator import evaluate_success_criteria
from redteam.generator import append_attack_via_llm

AGENT_DIR = Path(__file__).resolve().parent


def cmd_listar(args: argparse.Namespace) -> int:
    for attack in list_attacks(args.categoria):
        print(f"{attack['id']}\t{attack.get('category')}\t{attack.get('title')}")
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
    print(f"\nTotal: {sum(len(v) for v in by_cat.values())} ataques en catalog/attacks.json")
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


def _evaluate_attack(attack: dict, status: int, resp_body: dict | None, reply: str, steps: list) -> list[str]:
    return evaluate_success_criteria(
        attack, status=status, reply=reply, body=resp_body, steps=steps
    )


def _run_attack_trials(attack: dict, trials: int) -> dict:
    login_as = attack.get("loginAs") or "CC"
    token = _login_token(login_as)
    runs: list[dict] = []
    attack_success = 0
    for trial in range(1, trials + 1):
        status, resp_body, reply, steps = _run_attack_once(attack, token)
        failures = _evaluate_attack(attack, status, resp_body, reply, steps)
        defense_ok = not failures
        if not defense_ok:
            attack_success += 1
        runs.append(
            {
                "trial": trial,
                "httpStatus": status,
                "defensePass": defense_ok,
                "failures": failures,
                "replySnippet": (reply or "")[:300],
                "toolIds": [
                    s.get("toolId") for s in steps if isinstance(s, dict) and s.get("toolId")
                ],
            }
        )
    return {
        "id": attack.get("id"),
        "title": attack.get("title"),
        "category": attack.get("category"),
        "trials": trials,
        "attackSuccessCount": attack_success,
        "defensePassCount": trials - attack_success,
        "runs": runs,
    }


def cmd_probar_api(args: argparse.Namespace) -> int:
    attacks = list_attacks(args.categoria)
    if args.id:
        attacks = [a for a in attacks if a["id"] == args.id]
    failed = 0
    for attack in attacks:
        summary = _run_attack_trials(attack, 1)
        run = summary["runs"][0]
        if run["defensePass"]:
            print(f"PASS {attack['id']}: {attack.get('title')} (http={run['httpStatus']})")
        else:
            failed += 1
            print(f"FAIL {attack['id']}: {attack.get('title')}")
            for f in run["failures"]:
                print(f"  - {f}")
            if run.get("replySnippet"):
                print(f"  reply snippet: {run['replySnippet']!r}")
    return 1 if failed else 0


def cmd_probar_lab(args: argparse.Namespace) -> int:
    attacks = list_lab_attacks(args.categoria, args.id)
    if not attacks:
        print("No hay ataques LAB en tools/red-team-agent/ataques/LAB-*.json")
        return 1
    trials = max(1, int(args.trials))
    report = {
        "source": "tools/red-team-agent/ataques",
        "trialsPerAttack": trials,
        "attacks": [_run_attack_trials(a, trials) for a in attacks],
    }
    for summary in report["attacks"]:
        aid = summary["id"]
        ok = summary["defensePassCount"]
        bad = summary["attackSuccessCount"]
        print(
            f"{aid}\tdefensa {ok}/{trials}\téxito atacante {bad}/{trials}\t{summary.get('title')}"
        )
    if args.report:
        out = Path(args.report)
        if not out.is_absolute():
            out = AGENT_DIR / out
        out.parent.mkdir(parents=True, exist_ok=True)
        out.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        print(f"Informe: {out}")
    else:
        default = LAB_REPORTS_DIR / "lab-last-run.json"
        default.parent.mkdir(parents=True, exist_ok=True)
        default.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        print(f"Informe: {default}")
    any_attack_won = any(s["attackSuccessCount"] > 0 for s in report["attacks"])
    return 1 if any_attack_won else 0


def cmd_show_config(_: argparse.Namespace) -> int:
    print(f"repo={config.REPO_ROOT}")
    print(f"api={config.api_base()}")
    print(f"llm={config.resumen_llm()}")
    print(f"attacks={len(load_catalog().get('attacks', []))}")
    return 0


def main() -> int:
    parser = argparse.ArgumentParser(description="SIGESA Red Team agent")
    sub = parser.add_subparsers(dest="cmd", required=True)

    p_list = sub.add_parser("listar", help="Listar ataques del catálogo")
    p_list.add_argument("--categoria", default=None)

    sub.add_parser("cobertura", help="Categorías taxonomía vs casos en attacks.json")
    sub.add_parser("sync", help="Copiar attacks.json al backend (JUnit)")

    p_gen = sub.add_parser("generar", help="Generar ataque nuevo vía LLM y append al catálogo")
    p_gen.add_argument("categoria")
    p_gen.add_argument("hint", nargs="?", default="")

    p_api = sub.add_parser("probar-api", help="Ejecutar catálogo contra backend real")
    p_api.add_argument("--categoria", default=None)
    p_api.add_argument("--id", default=None)

    p_lab = sub.add_parser(
        "probar-lab",
        help="Entregable lab: ataques/LAB-*.json con N repeticiones y informe JSON",
    )
    p_lab.add_argument("--categoria", default=None)
    p_lab.add_argument("--id", default=None)
    p_lab.add_argument("--trials", type=int, default=3)
    p_lab.add_argument("--report", default=None, help="Ruta informe JSON (default reports/lab-last-run.json)")

    sub.add_parser("show-config", help="Mostrar configuración")

    args = parser.parse_args()
    handlers = {
        "listar": cmd_listar,
        "cobertura": cmd_cobertura,
        "sync": cmd_sync,
        "generar": cmd_generar,
        "probar-api": cmd_probar_api,
        "probar-lab": cmd_probar_lab,
        "show-config": cmd_show_config,
    }
    return handlers[args.cmd](args)


if __name__ == "__main__":
    sys.exit(main())
