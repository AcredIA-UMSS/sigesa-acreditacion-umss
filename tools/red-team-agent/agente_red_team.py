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
from redteam.catalog import coverage_by_category, list_attacks, load_taxonomy_categories, load_catalog, sync_all
from redteam.evaluator import evaluate_http, evaluate_reply
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


def _login_token() -> str:
    import urllib.request

    base = config.api_base()
    payload = json.dumps({"email": "cc@umss.edu.bo", "password": "CoordDemo2026!"}).encode()
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


def cmd_probar_api(args: argparse.Namespace) -> int:
    import urllib.error
    import urllib.request

    token = _login_token()
    base = config.api_base()
    attacks = list_attacks(args.categoria)
    if args.id:
        attacks = [a for a in attacks if a["id"] == args.id]
    failed = 0
    for attack in attacks:
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
        try:
            with urllib.request.urlopen(req, timeout=120) as resp:
                status = resp.status
                resp_body = json.loads(resp.read().decode())
                reply = str(resp_body.get("reply") or "")
        except urllib.error.HTTPError as err:
            status = err.code
            try:
                resp_body = json.loads(err.read().decode())
            except Exception:
                resp_body = {"raw": err.read().decode()[:500]}

        http_failures = evaluate_http(attack, status, resp_body)
        reply_failures = [] if attack.get("expectHttpStatus") else evaluate_reply(attack, reply)
        all_fail = http_failures + reply_failures
        if all_fail:
            failed += 1
            print(f"FAIL {attack['id']}: {attack.get('title')}")
            for f in all_fail:
                print(f"  - {f}")
            if reply and not attack.get("expectHttpStatus"):
                print(f"  reply snippet: {reply[:200]!r}")
        else:
            print(f"PASS {attack['id']}: {attack.get('title')} (http={status})")
    return 1 if failed else 0


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

    sub.add_parser("show-config", help="Mostrar configuración")

    args = parser.parse_args()
    handlers = {
        "listar": cmd_listar,
        "cobertura": cmd_cobertura,
        "sync": cmd_sync,
        "generar": cmd_generar,
        "probar-api": cmd_probar_api,
        "show-config": cmd_show_config,
    }
    return handlers[args.cmd](args)


if __name__ == "__main__":
    sys.exit(main())
