from __future__ import annotations

import re
from pathlib import Path

from testgen.batches import BatchDefinition

SYSTEM_PROMPT = """
Eres un generador de tests JUnit 5 para SIGESA (Java 21, Spring Boot 4, Mockito, AssertJ).
Reglas estrictas:
- temperature efectiva 0: código determinista, sin creatividad innecesaria.
- Devuelve UN SOLO archivo Java completo, sin markdown ni explicaciones fuera del código.
- La declaración DEBE ser exactamente: public class <NombreIndicadoEnElPrompt>;
- Package staging: com.umss.sigesa.generated.assistant
- Naming métodos: should* o method_scenario (convención existente del proyecto).
- Máximo N métodos @Test (el usuario indica N).
- Incluir casos: null, empty, invalid UUID, ACCESS_DENIED, 403, 400 cuando aplique.
- NO modificar código de producción.
- NO duplicar escenarios ya cubiertos en tests de contexto adjuntos.
- Usar dobles (Mockito) en fronteras: DB, red, disco, use cases.
- Para @WebMvcTest: @MockitoBean use cases, JWT mock o auth simplificado.
- PROHIBIDO @Disabled, TODO, placeholders vacíos.
""".strip()

SYSTEM_PROMPT_COMPACT = """
Generador JUnit 5 SIGESA (Java 21, Mockito, AssertJ). Un solo archivo Java, sin markdown.
public class <nombre exacto del prompt>. Package indicado. Max N @Test. temperature 0.
No duplicar métodos listados en inventario. Sin TODO ni @Disabled.
""".strip()

DISABLED_TESTS = [
    "AssistantToolRbacGuardTest.tdUsersAgentSubset_excludesListUsersForTdRole",
    "AssistantToolRbacGuardTest.eeHasNormativeSearchOnly",
]


def resolve_prompt_mode(provider: str, env_mode: str | None) -> str:
    if env_mode and env_mode.strip().lower() in ("compact", "full"):
        return env_mode.strip().lower()
    return "compact" if provider == "groq" else "full"


def _truncate_lines(content: str, max_lines: int, label: str) -> str:
    lines = content.splitlines()
    if len(lines) <= max_lines:
        return content
    head = "\n".join(lines[:max_lines])
    return (
        f"{head}\n\n"
        f"// ... [{label}: {len(lines) - max_lines} líneas omitidas] ..."
    )


def extract_test_method_names(java_test: str) -> list[str]:
    names: list[str] = []
    for block in re.split(r"@Test\b", java_test):
        match = re.search(r"void\s+(\w+)\s*\(", block)
        if match:
            names.append(match.group(1))
    return names


def compact_java_source(source: str, max_lines: int = 200) -> str:
    """Reduce cuerpos de métodos; conserva package, patterns y firmas."""
    lines = source.splitlines()
    result: list[str] = []
    skip_depth = 0

    for line in lines:
        if skip_depth > 0:
            skip_depth += line.count("{") - line.count("}")
            continue

        stripped = line.strip()
        is_signature = (
            re.match(r"(public|private|protected)", stripped)
            and "(" in line
            and ("{" in line or stripped.endswith(")"))
        )

        if (
            stripped.startswith("package ")
            or stripped.startswith("import ")
            or re.search(r"\bclass\s+\w+", line)
            or "Pattern" in line
            or stripped.startswith("@")
            or stripped.startswith("/**")
            or stripped.startswith("*")
            or stripped.startswith("//")
        ):
            result.append(line)
        elif is_signature:
            if "{" in line:
                prefix = line.split("{", 1)[0].rstrip()
                result.append(f"{prefix} {{ /* cuerpo omitido */ }}")
            else:
                result.append(line.rstrip() + " { /* cuerpo omitido */ }")
                skip_depth = 1

        if len(result) >= max_lines:
            break

    omitted = max(0, len(lines) - len(result))
    if omitted:
        result.append(f"\n// ... [{omitted} líneas omitidas — probar API pública arriba] ...")
    return "\n".join(result)


def build_user_prompt(
    repo_root: Path,
    batch: BatchDefinition,
    max_tests: int,
    provider: str = "local",
    prompt_mode: str | None = None,
) -> tuple[str, str]:
    mode = resolve_prompt_mode(provider, prompt_mode)
    system = SYSTEM_PROMPT_COMPACT if mode == "compact" else SYSTEM_PROMPT

    source_path = repo_root / "backend/src/main/java" / batch.source_relative_path
    if not source_path.is_file():
        raise FileNotFoundError(f"Fuente no encontrada: {source_path}")

    raw_source = source_path.read_text(encoding="utf-8")
    if mode == "compact":
        source_code = compact_java_source(raw_source, max_lines=200)
    else:
        source_code = _truncate_lines(raw_source, 350, batch.source_relative_path)

    context_parts: list[str] = []
    for rel in batch.context_test_relative_paths:
        test_path = repo_root / "backend/src/test/java" / rel
        if not test_path.is_file():
            continue
        raw = test_path.read_text(encoding="utf-8")
        if mode == "compact":
            methods = extract_test_method_names(raw)
            inventory = "\n".join(f"- {name}" for name in methods) or "- (ninguno detectado)"
            context_parts.append(
                f"\n--- {rel} — inventario @Test (NO duplicar) ---\n{inventory}\n"
            )
        else:
            context_parts.append(
                f"\n--- {rel} ---\n{_truncate_lines(raw, 80, rel)}\n"
            )

    disabled_list = "\n".join(f"- {item}" for item in DISABLED_TESTS)

    user = f"""
Genera la clase de test: {batch.output_class_name}
Package: {batch.output_package}
Fuente bajo prueba: {batch.source_relative_path}
Máximo @Test: {max_tests}
Notas: {batch.notes}

=== CÓDIGO FUENTE ===
{source_code}

=== CONTEXTO TESTS EXISTENTES ===
{''.join(context_parts)}

=== @Disabled (no regenerar) ===
{disabled_list}

public class {batch.output_class_name}
package {batch.output_package};
""".strip()

    return system, user
