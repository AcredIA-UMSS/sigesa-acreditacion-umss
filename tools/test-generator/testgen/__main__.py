from __future__ import annotations

import argparse
import sys
from pathlib import Path

import httpx

from testgen.batches import load_batches, print_catalog
from testgen.config import Env, LlmSettings, find_repo_root
from testgen.llm_client import LlmApiError, OpenAiChatClient
from testgen.prompt import build_user_prompt
from testgen.writer import save_raw_output, write_test_file

HELP_TEXT = """
SIGESA Test Generator (AcredIA Fase 3 — Python)

Uso:
  ./run.sh --batch L1              Genera tests del lote L1
  ./run.sh --batch L1 --dry-run    Solo muestra prompt (sin LLM)
  ./run.sh --list-batches          Catálogo de lotes Fase 4
  ./run.sh --show-config           Muestra LLM resuelto desde .env

Variables (.env raíz del repo):
  SIGESA_RUNTIME=auto|host|docker
  SIGESA_LLM_PROVIDER=local|groq
  SIGESA_TESTGEN_PROVIDER=         (override, opcional)
  GROQ_API_KEY=                    (provider groq)
  SIGESA_ASSISTANT_API_KEY=        (provider local / Open WebUI)
  SIGESA_LLM_BASE_URL_HOST=http://localhost:11434/v1
  SIGESA_LLM_BASE_URL_DOCKER=http://ollama:11434/v1
  SIGESA_LLM_MODEL_LOCAL=qwen2.5:7b
  SIGESA_LLM_MODEL_GROQ=openai/gpt-oss-20b
  SIGESA_TESTGEN_TEMPERATURE=0
  SIGESA_TESTGEN_MAX_TESTS=8
  SIGESA_TESTGEN_MAX_RETRIES=1
""".strip()

RETRY_SUFFIX = """
CORRECCIÓN OBLIGATORIA — intento anterior inválido:
{error}

Regenera el archivo Java COMPLETO desde cero.
- public class {class_name}  (nombre exacto, incluye _AgentGenerated)
- package {package_name};
- Sin markdown, sin texto extra, solo código Java.
"""


def parse_args(argv: list[str] | None = None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="SIGESA Test Generator", add_help=False)
    parser.add_argument("-h", "--help", action="store_true")
    parser.add_argument("--list-batches", action="store_true")
    parser.add_argument("--list-groq-models", action="store_true")
    parser.add_argument("--show-config", action="store_true")
    parser.add_argument("--dry-run", action="store_true")
    parser.add_argument("--batch", type=str, default=None)
    return parser.parse_args(argv)


def show_config() -> None:
    repo_root = find_repo_root()
    env = Env(repo_root)
    settings = LlmSettings.resolve(env)
    print("SIGESA Test Generator — configuración resuelta")
    print(f"Repo root: {repo_root}")
    print(settings)
    print(f"SIGESA_TESTGEN_MAX_TESTS={env.get('SIGESA_TESTGEN_MAX_TESTS', '8')}")
    print(f"SIGESA_TESTGEN_MAX_RETRIES={env.get('SIGESA_TESTGEN_MAX_RETRIES', '1')}")
    env_path = repo_root / ".env"
    print(f".env encontrado: {'sí' if env_path.is_file() else 'no (solo env del sistema)'}")


def generate_with_retries(
    client: OpenAiChatClient,
    repo_root: Path,
    batch_id: str,
    batch,
    system_prompt: str,
    user_prompt: str,
    max_retries: int,
) -> Path:
    llm_output = client.complete(system_prompt, user_prompt)
    last_error: ValueError | None = None

    for attempt in range(max_retries + 1):
        try:
            target, notes = write_test_file(
                repo_root,
                batch.output_package,
                batch.output_class_name,
                llm_output,
            )
            for note in notes:
                print(f"  ↳ normalizado: {note}")
            return target
        except ValueError as exc:
            last_error = exc
            raw_path = save_raw_output(repo_root, batch_id, llm_output)
            print(f"  ⚠ Validación fallida (intento {attempt + 1}): {exc}", file=sys.stderr)
            print(f"  ↳ respuesta cruda guardada en: {raw_path}", file=sys.stderr)
            if attempt >= max_retries:
                break
            correction = RETRY_SUFFIX.format(
                error=exc,
                class_name=batch.output_class_name,
                package_name=batch.output_package,
            )
            print("  ↳ reintentando con prompt de corrección...")
            llm_output = client.complete(system_prompt, user_prompt + correction)

    assert last_error is not None
    raise last_error


def main(argv: list[str] | None = None) -> int:
    args = parse_args(argv)

    if args.help or len(sys.argv) == 1:
        print(HELP_TEXT)
        return 0

    if args.list_batches:
        print_catalog()
        return 0

    if args.list_groq_models:
        repo_root = find_repo_root()
        env = Env(repo_root)
        settings = LlmSettings.resolve(env)
        if settings.provider != "groq":
            print("Forzando provider=groq para listar modelos...")
            settings = LlmSettings(
                "groq",
                settings.runtime_mode,
                settings.base_url if "groq.com" in settings.base_url else "https://api.groq.com/openai/v1",
                settings.api_key or env.get("GROQ_API_KEY", "") or "",
                settings.model,
                settings.temperature,
                settings.max_tokens,
                settings.timeout_seconds,
            )
        settings.validate()
        client = OpenAiChatClient(settings)
        print("Modelos Groq disponibles para tu API key:")
        for model_id in client.list_models():
            print(f"  - {model_id}")
        return 0

    if args.show_config:
        show_config()
        return 0

    if not args.batch:
        print("Error: indique --batch L1..L6 (use --list-batches)", file=sys.stderr)
        return 1

    repo_root = find_repo_root()
    env = Env(repo_root)
    settings = LlmSettings.resolve(env)

    try:
        settings.validate()
    except ValueError as exc:
        print(f"Error de configuración: {exc}", file=sys.stderr)
        return 1

    batches = load_batches()
    batch_id = args.batch.upper()
    batch = batches.get(batch_id)
    if batch is None:
        print(f"Lote desconocido: {args.batch}", file=sys.stderr)
        print_catalog()
        return 1

    max_tests = env.get_int("SIGESA_TESTGEN_MAX_TESTS", batch.max_tests)
    max_retries = env.get_int("SIGESA_TESTGEN_MAX_RETRIES", 1)
    prompt_mode = env.get("SIGESA_TESTGEN_PROMPT_MODE")
    system_prompt, user_prompt = build_user_prompt(
        repo_root, batch, max_tests, settings.provider, prompt_mode
    )
    mode_label = prompt_mode or ("compact" if settings.provider == "groq" else "full")

    print(f"Repo:     {repo_root}")
    print(f"Settings: {settings}")
    print(f"Lote:     {batch_id} → {batch.output_class_name}")
    print(f"Max @Test: {max_tests}")
    print(f"Prompt:   {mode_label} (~{len(user_prompt)} chars user)")

    if args.dry_run:
        print("\n=== SYSTEM PROMPT ===\n" + system_prompt)
        truncated = user_prompt[:4000] + ("\n... [truncado]" if len(user_prompt) > 4000 else "")
        print("\n=== USER PROMPT (truncado) ===\n" + truncated)
        return 0

    client = OpenAiChatClient(settings)
    print(f"\nLlamando LLM ({settings.provider}, timeout {int(settings.timeout_seconds)}s)...")

    try:
        target = generate_with_retries(
            client,
            repo_root,
            batch_id,
            batch,
            system_prompt,
            user_prompt,
            max_retries,
        )
    except httpx.ReadTimeout:
        print(
            f"\n✗ Timeout: el LLM no respondió en {int(settings.timeout_seconds)}s.",
            file=sys.stderr,
        )
        print(
            "  Opciones: (1) SIGESA_TESTGEN_TIMEOUT_SECONDS=1800 en .env",
            file=sys.stderr,
        )
        print(
            "            (2) SIGESA_TESTGEN_PROVIDER=groq + GROQ_API_KEY",
            file=sys.stderr,
        )
        print(
            "            (3) modelo más pequeño: SIGESA_LLM_MODEL_LOCAL=llama3.2:3b",
            file=sys.stderr,
        )
        return 1
    except LlmApiError as exc:
        print(f"\n✗ Error LLM: {exc}", file=sys.stderr)
        if exc.status_code in (413, 429) and "token" in exc.body.lower():
            print(
                "  Prompt demasiado grande para tier free Groq (8000 TPM).",
                file=sys.stderr,
            )
            print(
                "  Ya se usa modo compact automático; prueba SIGESA_TESTGEN_PROMPT_MODE=compact",
                file=sys.stderr,
            )
            print(
                "  o sube de tier en console.groq.com",
                file=sys.stderr,
            )
        elif exc.status_code == 404 and "model" in exc.body.lower():
            print(
                "  Groq retiró llama-3.3-70b-versatile (ago 2026). "
                "Use en .env:",
                file=sys.stderr,
            )
            print(
                "    SIGESA_LLM_MODEL_GROQ=openai/gpt-oss-20b",
                file=sys.stderr,
            )
            print(
                "  Listar modelos: ./tools/test-generator/run.sh --list-groq-models",
                file=sys.stderr,
            )
        return 1
    except ValueError as exc:
        print(f"\n✗ Generación fallida: {exc}", file=sys.stderr)
        print("  Revise tools/test-generator/.output/ para ver la respuesta del modelo.", file=sys.stderr)
        return 1

    print(f"\n✓ Test generado: {target}")
    print("  Auditar con checklist Fase 5 antes de mover fuera de generated/assistant/")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
