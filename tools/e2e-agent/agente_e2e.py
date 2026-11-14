#!/usr/bin/env python3
"""agente_e2e.py — Planner y Generator E2E SIGESA por SDK (sin IDE ni navegador).

    cd tools/e2e-agent
    pip install -r requirements.txt
    cp .env.example .env         # IA local Docker (ver MANUAL.md)

    python agente_e2e.py probar                  # verifica Ollama/Open WebUI
    python agente_e2e.py plan login              -> frontend/specs/plan_login.md
    python agente_e2e.py listar responsable      -> casos del plan
    python agente_e2e.py generar plantilla 1.1     -> frontend/tests/agente/<archivo>.spec.ts
    cd ../../frontend && PW_SKIP_BACKEND=1 pnpm test:e2e tests/agente/<archivo>.spec.ts

Diferencia con agentes nativos Playwright: NO abre el navegador; lee contexto Markdown +
seed/patrón TypeScript. Pierde exploración en vivo, pero muestra el pipeline LLM (tokens, tiempo).
"""
from __future__ import annotations

import argparse
import re
import sys
import time
from pathlib import Path

import config

AGENT_DIR = Path(__file__).resolve().parent
CONTEXT_DIR = AGENT_DIR / "context"
SPECS_DIR = config.FRONTEND / "specs"
TESTS_AGENTE = config.FRONTEND / "tests" / "agente"

SECCIONES: dict[str, dict[str, str]] = {
    "login": {
        "plan": "plan_login.md",
        "context": "login.md",
        "patron": "tests/tradicional/login.spec.ts",
        "titulo": "Autenticación UC-001 — Login",
    },
    "ayuda": {
        "plan": "plan_ayuda.md",
        "context": "ayuda.md",
        "patron": "tests/agente/ayuda-funciones-usuario-jd.spec.ts",
        "titulo": "Asistente /ayuda — agent=general",
    },
    "procesos": {
        "plan": "plan_procesos_dimension_jd.md",
        "context": "procesos_dimension.md",
        "patron": "tests/agente/procesos-jd-crear-dimension-prueba.spec.ts",
        "titulo": "Procesos — JD crea dimensión normativa",
    },
    "evidencia": {
        "plan": "plan_evidencia_cc.md",
        "context": "evidencia_cc.md",
        "patron": "tests/agente/procesos-jd-crear-dimension-prueba.spec.ts",
        "titulo": "Evidencia — CC carga en proceso",
    },
    "responsable": {
        "plan": "plan_proceso_responsable_jd.md",
        "context": "proceso_responsable_jd.md",
        "patron": "tests/agente/procesos-jd-crear-dimension-prueba.spec.ts",
        "titulo": "Proceso — JD asigna coordinador (CC)",
    },
    "aprobacion": {
        "plan": "plan_aprobacion_td.md",
        "context": "aprobacion_td.md",
        "patron": "tests/agente/procesos-jd-crear-dimension-prueba.spec.ts",
        "titulo": "Revisión — TD aprueba indicador / cierra dimensión",
    },
    "plantilla": {
        "plan": "plan_plantilla_jd.md",
        "context": "plantilla_jd.md",
        "patron": "tests/agente/procesos-jd-crear-dimension-prueba.spec.ts",
        "titulo": "Plantillas — JD crea plantilla en borrador",
    },
}

PLAN_FORMATO_REF = """# SIGESA <Sección> Test Plan

## Application Overview

<resumen breve: ruta, roles, UI clave, reglas de no verificar copy LLM>

## Test Scenarios

### 1. <Nombre del grupo>

**Seed:** `tests/seed.spec.ts`

#### 1.1. <Título del caso>

**File:** `tests/agente/<nombre-kebab>.spec.ts`

**Steps:**
  1. <acción>
    - expect: <verificación accesible>
  2. ...

#### 1.2. ...
"""

PROMPT_PLAN = """Actúa como planificador de pruebas E2E para SIGESA (React + Spring Boot).
NO escribas código TypeScript. Devuelve SOLO Markdown con el formato exacto del ejemplo.

Reglas:
- Máximo 8 casos en total (camino feliz, error, borde).
- Grupos como `### 1. Nombre`; casos como `#### 1.1. Título`.
- Cada caso incluye `**File:**` con ruta bajo `tests/agente/` (kebab-case).
- Pasos numerados con sub-bullets `- expect:` verificables en pantalla (roles, URLs, dialog, tablas).
- NO verifiques redacción exacta del asistente LLM; sí respuesta presente, modal historial, Camino KEYWORD/LLM/OUT_OF_SCOPE.
- Localizadores implícitos: getByRole, getByLabel, getByText, getByTestId (nunca CSS).
- Credenciales dev en contexto; login JD va a `/admin/users`.

Formato de referencia:
```markdown
{formato}
```"""

PROMPT_GENERAR = """Actúa como generador de tests Playwright TypeScript para SIGESA.
Escribe UN solo `test(...)` dentro de un `test.describe` con el nombre del grupo.
Imita el estilo del archivo patrón (helpers, comentarios por paso, credenciales).

Reglas estrictas:
- Localizadores SOLO: getByRole, getByLabel, getByText, getByTestId.
- Campo contraseña login: `page.getByRole('textbox', {{ name: 'Contraseña' }})`.
- Prohibido waitForTimeout, CSS, #id, nth-child.
- expect(...).toBeVisible / toHaveURL / toContainText / toHaveCount.
- Comentario `// N. ...` antes de cada acción según los pasos del plan.
- Test independiente: empieza con page.goto de la ruta inicial del flujo.
- NO verifiques textos volátiles del LLM; sí URLs, dialog, Camino:, badge OK.
- Incluye `import {{ test, expect }} from '@playwright/test';` y `import type {{ Page }}` si usás helpers.
- Header: `// spec: specs/plan_<seccion>.md — caso X.Y`

Devuelve SOLO código TypeScript, sin explicaciones."""


def llamar(system: str, user: str, dry_run: bool = False) -> str:
    if dry_run:
        print("--- system ---\n", system[:800], "...\n--- user (truncado) ---\n", user[:2000], "...")
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
    u = r.usage
    print(
        f"modelo: {config.resumen()}  "
        f"tokens: in={u.prompt_tokens if u else '?'} out={u.completion_tokens if u else '?'}  "
        f"tiempo={time.time() - t0:.1f}s"
    )
    return r.choices[0].message.content or ""


def sin_cerca(texto: str, lenguaje: str = "") -> str:
    patron = rf"```(?:{lenguaje}|markdown|md)?\n(.*?)```" if lenguaje else r"```(?:\w+)?\n(.*?)```"
    m = re.search(patron, texto, re.S)
    return (m.group(1) if m else texto).strip() + "\n"


def leer_contexto(seccion: str) -> str:
    base = (CONTEXT_DIR / "base.md").read_text(encoding="utf-8")
    extra = (CONTEXT_DIR / SECCIONES[seccion]["context"]).read_text(encoding="utf-8")
    return f"{base}\n\n{extra}"


def leer_archivo_frontend(rel: str) -> str:
    path = config.FRONTEND / rel
    if not path.is_file():
        sys.exit(f"no existe: {path}")
    return path.read_text(encoding="utf-8")


def ruta_plan(seccion: str) -> Path:
    return SPECS_DIR / SECCIONES[seccion]["plan"]


def cmd_plan(seccion: str, dry_run: bool) -> None:
    if seccion not in SECCIONES:
        sys.exit(f"sección desconocida: {seccion}. Válidas: {', '.join(SECCIONES)}")

    meta = SECCIONES[seccion]
    seed = leer_archivo_frontend("tests/seed.spec.ts")
    contexto = leer_contexto(seccion)
    plan_existente = ""
    destino = ruta_plan(seccion)
    if destino.is_file():
        plan_existente = destino.read_text(encoding="utf-8")[:4000]
        nota = f"(Plan actual truncado como referencia de formato; podés mejorarlo, no copies casos sin revisar.)\n```markdown\n{plan_existente}\n```"
    else:
        nota = "(No hay plan previo; creá uno nuevo.)"

    user = (
        f"### Sección a planificar\n{meta['titulo']}\n\n"
        f"### Contexto SIGESA\n{contexto}\n\n"
        f"### tests/seed.spec.ts\n```ts\n{seed}\n```\n\n"
        f"### Plan previo\n{nota}\n\n"
        f"Escribe `specs/{meta['plan']}`."
    )
    system = PROMPT_PLAN.format(formato=PLAN_FORMATO_REF)
    salida = llamar(system, user, dry_run=dry_run)
    if dry_run:
        return

    destino.parent.mkdir(parents=True, exist_ok=True)
    destino.write_text(sin_cerca(salida), encoding="utf-8")
    print(f"escrito: {destino.relative_to(config.REPO_ROOT)}")
    print("-> revisá el plan ANTES de generar (frontend/AUDITORIA_E2E.md)")


def parsear_casos(texto: str) -> list[tuple[str, str, str]]:
    """Devuelve [(id, titulo, cuerpo), ...] ej. ('1.1', 'JD login', '...')."""
    casos: list[tuple[str, str, str]] = []
    for m in re.finditer(r"^#### (\d+\.\d+)\.\s*(.+?)$", texto, re.M):
        caso_id, titulo = m.group(1), m.group(2).strip()
        start = m.end()
        nxt = re.search(r"^#### \d+\.\d+\.", texto[start:], re.M)
        end = start + nxt.start() if nxt else len(texto)
        casos.append((caso_id, titulo, texto[start:end].strip()))
    return casos


def parsear_grupos(texto: str) -> dict[str, str]:
    return {m.group(1): m.group(2).strip() for m in re.finditer(r"^### (\d+)\.\s*(.+?)$", texto, re.M)}


def slug_archivo(cuerpo: str, caso_id: str, titulo: str) -> str:
    m = re.search(r"\*\*File:\*\*\s*`([^`]+)`", cuerpo)
    if m:
        return Path(m.group(1).strip()).name
    slug = re.sub(r"[^a-z0-9]+", "-", titulo.lower()).strip("-")[:48]
    return f"{slug or 'caso'}.spec.ts" if slug else f"caso_{caso_id.replace('.', '_')}.spec.ts"


def cmd_listar(seccion: str) -> None:
    plan = ruta_plan(seccion)
    if not plan.is_file():
        sys.exit(f"no existe {plan}; corré: python agente_e2e.py plan {seccion}")
    texto = plan.read_text(encoding="utf-8")
    casos = parsear_casos(texto)
    if not casos:
        sys.exit(f"no encontré casos #### N.N en {plan.name}")
    print(f"{plan.relative_to(config.REPO_ROOT)} — {len(casos)} casos:\n")
    for caso_id, titulo, cuerpo in casos:
        archivo = slug_archivo(cuerpo, caso_id, titulo)
        print(f"  {caso_id}  {titulo}")
        print(f"         -> tests/agente/{archivo}")


def cmd_generar(seccion: str, caso_id: str, dry_run: bool) -> None:
    if seccion not in SECCIONES:
        sys.exit(f"sección desconocida: {seccion}")

    plan_path = ruta_plan(seccion)
    if not plan_path.is_file():
        sys.exit(f"no existe {plan_path}; corré: python agente_e2e.py plan {seccion}")

    texto = plan_path.read_text(encoding="utf-8")
    grupos = parsear_grupos(texto)
    match = next(((t, c) for cid, t, c in parsear_casos(texto) if cid == caso_id), None)
    if not match:
        sys.exit(f"no encuentro el caso {caso_id} en {plan_path.name}. Usá: listar {seccion}")

    titulo, cuerpo = match
    grupo_num = caso_id.split(".")[0]
    grupo_nombre = grupos.get(grupo_num, SECCIONES[seccion]["titulo"])
    meta = SECCIONES[seccion]
    patron = leer_archivo_frontend(meta["patron"])
    seed = leer_archivo_frontend("tests/seed.spec.ts")
    contexto = leer_contexto(seccion)
    nombre_archivo = slug_archivo(cuerpo, caso_id, titulo)
    destino = TESTS_AGENTE / nombre_archivo

    user = (
        f"### Contexto\n{contexto}\n\n"
        f"### Patrón ({meta['patron']})\n```ts\n{patron}\n```\n\n"
        f"### Seed\n```ts\n{seed}\n```\n\n"
        f"### Grupo: {grupo_nombre}\n"
        f"### Caso {caso_id}: {titulo}\n{cuerpo}\n\n"
        f"### Salida\n`tests/agente/{nombre_archivo}`"
    )
    salida = llamar(PROMPT_GENERAR, user, dry_run=dry_run)
    if dry_run:
        return

    codigo = sin_cerca(salida, "(?:ts|typescript)")
    header = f"// spec: specs/{meta['plan']} — caso {caso_id}\n"
    if "import { test, expect }" not in codigo:
        codigo = "import { test, expect } from '@playwright/test';\n\n" + codigo
        print("aviso: el modelo olvidó el import; se agregó")
    if not codigo.lstrip().startswith("// spec:"):
        codigo = header + codigo

    if "waitForTimeout" in codigo:
        print("aviso: el código contiene waitForTimeout — revisá AUDITORIA_E2E.md")

    destino.parent.mkdir(parents=True, exist_ok=True)
    destino.write_text(codigo, encoding="utf-8")
    rel = destino.relative_to(config.FRONTEND)
    print(f"escrito: frontend/{rel}")
    print(f"ahora:\n  cd frontend\n  PW_SKIP_BACKEND=1 pnpm test:e2e {rel}")
    print("después: auditoría (frontend/AUDITORIA_E2E.md)")


def cmd_secciones() -> None:
    print("Secciones disponibles:\n")
    for key, meta in SECCIONES.items():
        print(f"  {key:8}  {meta['titulo']}")
        print(f"           plan -> frontend/specs/{meta['plan']}")
        print(f"           patrón -> frontend/{meta['patron']}\n")


def cmd_probar(dry_run: bool) -> None:
    """Verifica conectividad con el LLM configurado (Ollama/Open WebUI/Groq)."""
    print(f"Config: {config.resumen()}\n")

    base_url, api_key, model = config.resolve_llm()
    root = base_url.removesuffix("/v1")

    # Ollama nativo: listar modelos sin openai
    if "11434" in base_url or base_url.endswith(":11434"):
        import json
        import urllib.error
        import urllib.request

        url = f"{root}/api/tags"
        print(f"GET {url}")
        if dry_run:
            return
        try:
            with urllib.request.urlopen(url, timeout=15) as resp:
                data = json.loads(resp.read().decode())
            names = [m.get("name", "?") for m in data.get("models", [])]
            print(f"Ollama OK — modelos: {', '.join(names) or '(ninguno)'}")
            if model not in names and not any(model in n for n in names):
                print(f"aviso: '{model}' no aparece. Descargá: docker exec sigesa-ollama ollama pull {model}")
        except urllib.error.URLError as exc:
            sys.exit(f"Ollama no responde en {url}. ¿docker compose up -d ollama?\n  {exc}")

    print(f"\nPrueba chat (model={model})...")
    if dry_run:
        return
    t0 = time.time()
    try:
        reply = llamar(
            "Respondé en una sola palabra: OK",
            "Di solo OK si recibiste este mensaje.",
            dry_run=False,
        )
        print(f"Respuesta: {reply.strip()[:120]}")
        print(f"Conexión LLM OK ({time.time() - t0:.1f}s)")
    except Exception as exc:
        sys.exit(f"Falló la llamada al LLM: {exc}")


def main() -> None:
    parser = argparse.ArgumentParser(description="Planner / Generator E2E SIGESA (SDK, sin navegador)")
    parser.add_argument("--dry-run", action="store_true", help="muestra prompt truncado, no llama al LLM")
    sub = parser.add_subparsers(dest="cmd", required=True)

    sub.add_parser("secciones", help="lista secciones soportadas")
    sub.add_parser("probar", help="verifica conexión al LLM (Ollama Docker, Open WebUI o Groq)")

    p_plan = sub.add_parser("plan", help="genera specs/plan_<seccion>.md")
    p_plan.add_argument("seccion", choices=sorted(SECCIONES))

    p_list = sub.add_parser("listar", help="lista casos del plan")
    p_list.add_argument("seccion", choices=sorted(SECCIONES))

    p_gen = sub.add_parser("generar", help="genera un test desde un caso del plan")
    p_gen.add_argument("seccion", choices=sorted(SECCIONES))
    p_gen.add_argument("caso", help="id del caso, ej. 1.1")

    args = parser.parse_args()

    if args.cmd == "secciones":
        cmd_secciones()
    elif args.cmd == "probar":
        cmd_probar(args.dry_run)
    elif args.cmd == "plan":
        cmd_plan(args.seccion, args.dry_run)
    elif args.cmd == "listar":
        cmd_listar(args.seccion)
    elif args.cmd == "generar":
        cmd_generar(args.seccion, args.caso, args.dry_run)


if __name__ == "__main__":
    main()
