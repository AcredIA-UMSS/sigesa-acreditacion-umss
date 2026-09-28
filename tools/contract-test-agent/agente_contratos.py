#!/usr/bin/env python3
"""Planner / Generator de contract tests JSON (DTOs web). Token usage → reports/tokens.jsonl."""
from __future__ import annotations

import argparse
import json
import re
import sys
import time
from datetime import datetime, timezone
from pathlib import Path

import config

AGENT_DIR = Path(__file__).resolve().parent
MODULES = json.loads((AGENT_DIR / "config" / "modules.json").read_text(encoding="utf-8"))
PROMPTS = AGENT_DIR / "prompts"
REPORTS = AGENT_DIR / "reports"
BACKEND_JAVA = config.REPO_ROOT / "backend" / "src" / "main" / "java" / "com" / "umss" / "sigesa"


def sin_cerca(texto: str, lenguaje: str = "java") -> str:
    patron = rf"```(?:{lenguaje})?\n(.*?)```"
    m = re.search(patron, texto, re.S)
    return (m.group(1) if m else texto).strip() + "\n"


def registrar_tokens(rol: str, modulo: str, usage, segundos: float) -> None:
    REPORTS.mkdir(parents=True, exist_ok=True)
    row = {
        "ts": datetime.now(timezone.utc).isoformat(),
        "agent": rol,
        "module": modulo,
        "model": config.resolve_llm()[2],
        "prompt_tokens": getattr(usage, "prompt_tokens", None) if usage else None,
        "completion_tokens": getattr(usage, "completion_tokens", None) if usage else None,
        "total_tokens": getattr(usage, "total_tokens", None) if usage else None,
        "seconds": round(segundos, 2),
    }
    with (REPORTS / "tokens.jsonl").open("a", encoding="utf-8") as fh:
        fh.write(json.dumps(row, ensure_ascii=False) + "\n")


def llamar(system: str, user: str, rol: str, modulo: str, dry_run: bool) -> str:
    if dry_run:
        print("--- system ---\n", system[:600], "\n--- user ---\n", user[:1500])
        return ""
    t0 = time.time()
    model = config.resolve_llm()[2]
    r = config.crear_cliente().chat.completions.create(
        model=model,
        temperature=0,
        messages=[
            {"role": "system", "content": system},
            {"role": "user", "content": user},
        ],
    )
    elapsed = time.time() - t0
    u = r.usage
    registrar_tokens(rol, modulo, u, elapsed)
    print(
        f"modelo: {config.resumen()}  "
        f"tokens: in={u.prompt_tokens if u else '?'} out={u.completion_tokens if u else '?'}  "
        f"tiempo={elapsed:.1f}s"
    )
    return r.choices[0].message.content or ""


def leer_dtos(modulo: str) -> str:
    chunks: list[str] = []
    for rel in MODULES[modulo]["dtos"]:
        path = BACKEND_JAVA / rel
        if path.is_file():
            chunks.append(f"### {rel}\n```java\n{path.read_text(encoding='utf-8')}\n```")
    return "\n\n".join(chunks)


def ruta_plan(modulo: str) -> Path:
    return REPORTS / MODULES[modulo]["plan"]


def parsear_casos(texto: str) -> list[tuple[str, str, str]]:
    casos: list[tuple[str, str, str]] = []
    for m in re.finditer(r"^#### (\d+\.\d+)\.\s*(.+?)$", texto, re.M):
        caso_id, titulo = m.group(1), m.group(2).strip()
        start = m.end()
        nxt = re.search(r"^#### \d+\.\d+\.", texto[start:], re.M)
        end = start + nxt.start() if nxt else len(texto)
        casos.append((caso_id, titulo, texto[start:end].strip()))
    return casos


def cmd_modulos() -> None:
    for key, meta in MODULES.items():
        print(f"  {key:10} {meta['titulo']}")
        print(f"             plan -> tools/contract-test-agent/reports/{meta['plan']}")
        print(f"             tests -> {meta['package']}\n")


def cmd_plan(modulo: str, dry_run: bool) -> None:
    meta = MODULES[modulo]
    system = (PROMPTS / "plan.md").read_text(encoding="utf-8")
    user = (
        f"### Módulo\n{meta['titulo']}\n\n"
        f"### DTOs\n{leer_dtos(modulo)}\n\n"
        f"Escribe `reports/{meta['plan']}`."
    )
    salida = llamar(system, user, "planner", modulo, dry_run)
    if dry_run:
        return
    destino = ruta_plan(modulo)
    destino.write_text(sin_cerca(salida, "markdown"), encoding="utf-8")
    print(f"escrito: {destino.relative_to(config.REPO_ROOT)}")


def cmd_listar(modulo: str) -> None:
    plan = ruta_plan(modulo)
    if not plan.is_file():
        sys.exit(f"no existe {plan}")
    casos = parsear_casos(plan.read_text(encoding="utf-8"))
    print(f"{plan.name} — {len(casos)} casos\n")
    for caso_id, titulo, cuerpo in casos:
        m = re.search(r"\*\*File:\*\*\s*`([^`]+)`", cuerpo)
        print(f"  {caso_id}  {titulo}")
        if m:
            print(f"         -> {m.group(1)}")


def cmd_generar(modulo: str, caso_id: str, dry_run: bool) -> None:
    meta = MODULES[modulo]
    plan = ruta_plan(modulo)
    texto = plan.read_text(encoding="utf-8")
    match = next(((t, c) for cid, t, c in parsear_casos(texto) if cid == caso_id), None)
    if not match:
        sys.exit(f"no encuentro {caso_id} en {plan.name}")
    titulo, cuerpo = match
    patron_path = config.REPO_ROOT / meta["patron"]
    patron = patron_path.read_text(encoding="utf-8") if patron_path.is_file() else ""
    system = (PROMPTS / "generate.md").read_text(encoding="utf-8")
    user = (
        f"### Package\n{meta['package']}\n\n"
        f"### DTOs\n{leer_dtos(modulo)}\n\n"
        f"### Patrón\n```java\n{patron}\n```\n\n"
        f"### Caso {caso_id}: {titulo}\n{cuerpo}\n"
    )
    salida = llamar(system, user, "generator", modulo, dry_run)
    if dry_run:
        return
    slug = re.sub(r"[^A-Za-z0-9]+", "", titulo) or "Case"
    dest = (
        config.REPO_ROOT
        / "backend/src/test/java/com/sigesa/app/contracts"
        / modulo
        / f"{slug}_AgentGenerated.java"
    )
    dest.parent.mkdir(parents=True, exist_ok=True)
    dest.write_text(sin_cerca(salida), encoding="utf-8")
    print(f"escrito: {dest.relative_to(config.REPO_ROOT)}")
    print("auditar y luego: ./run.sh correr")


def cmd_correr() -> None:
    """Compila solo com.sigesa.app.contracts (tests legacy subfase no compilan) y corre Surefire."""
    import subprocess

    backend = config.REPO_ROOT / "backend"
    mvnw = backend / "mvnw"
    cp_file = backend / "target" / "test.cp"
    classes = backend / "target" / "classes"
    if not classes.is_dir():
        print("compilando main…")
        rc = subprocess.call([str(mvnw), "-q", "-DskipTests", "compile"], cwd=backend)
        if rc != 0:
            raise SystemExit(rc)
    subprocess.check_call(
        [str(mvnw), "-q", "dependency:build-classpath", "-DincludeScope=test", f"-Dmdep.outputFile={cp_file}"],
        cwd=backend,
    )
    cp = f"{classes}:{cp_file.read_text(encoding='utf-8').strip()}"
    sources = list((backend / "src/test/java/com/sigesa/app/contracts").rglob("*.java"))
    out = backend / "target" / "test-classes"
    out.mkdir(parents=True, exist_ok=True)
    javac = [
        "javac",
        "--release",
        "21",
        "-proc:none",
        "-cp",
        cp,
        "-d",
        str(out),
        *[str(s) for s in sources],
    ]
    print("javac", len(sources), "fuentes contract")
    rc = subprocess.call(javac)
    if rc != 0:
        raise SystemExit(rc)
    cmd = [
        str(mvnw),
        "surefire:test",
        "-Dtest=LoginJsonContractTest,EvidenceJsonContractTest,SendChatMessageJsonContractTest",
    ]
    print(" ".join(cmd))
    raise SystemExit(subprocess.call(cmd, cwd=backend))


def main() -> None:
    parser = argparse.ArgumentParser(description="Contract test agent SIGESA")
    parser.add_argument("--dry-run", action="store_true")
    sub = parser.add_subparsers(dest="cmd", required=True)
    sub.add_parser("modulos")
    p_plan = sub.add_parser("plan")
    p_plan.add_argument("modulo", choices=sorted(MODULES))
    p_list = sub.add_parser("listar")
    p_list.add_argument("modulo", choices=sorted(MODULES))
    p_gen = sub.add_parser("generar")
    p_gen.add_argument("modulo", choices=sorted(MODULES))
    p_gen.add_argument("caso")
    sub.add_parser("correr")
    args = parser.parse_args()
    if args.cmd == "modulos":
        cmd_modulos()
    elif args.cmd == "plan":
        cmd_plan(args.modulo, args.dry_run)
    elif args.cmd == "listar":
        cmd_listar(args.modulo)
    elif args.cmd == "generar":
        cmd_generar(args.modulo, args.caso, args.dry_run)
    elif args.cmd == "correr":
        cmd_correr()


if __name__ == "__main__":
    main()
