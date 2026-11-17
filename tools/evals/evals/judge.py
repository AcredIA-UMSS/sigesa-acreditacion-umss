from __future__ import annotations

import json
import os
import re
import urllib.error
import urllib.request
from pathlib import Path
from typing import Any

EVALS_DIR = Path(__file__).resolve().parent.parent
LABELS_DIR = EVALS_DIR / "artifacts" / "labels"
JUDGE_PROMPT_PATH = EVALS_DIR / "prompts" / "juez_system_prompt.txt"
RUBRIC_PATH = EVALS_DIR / "prompts" / "juez_rubrica.md"


def _normalize_openai_base(raw: str) -> str:
    """Base URL sin /chat/completions — compatible Open WebUI, Groq u OpenAI."""
    base = raw.strip().rstrip("/")
    if not base:
        return "http://127.0.0.1:3001/api"
    if "groq.com" in base:
        if base.endswith("/v1"):
            return base
        if base.endswith("/openai"):
            return base + "/v1"
        return "https://api.groq.com/openai/v1"
    if base.endswith("/v1"):
        return base
    # Open WebUI / Ollama vía proxy local: …/api
    if base.endswith("/api"):
        return base
    return base + "/api"


def _chat_completions_url(base: str) -> str:
    root = base.rstrip("/")
    if root.endswith("/v1"):
        return f"{root}/chat/completions"
    return f"{root}/v1/chat/completions"


def judge_llm_base_url() -> str:
    explicit = os.getenv("SIGESA_JUDGE_BASE_URL")
    if explicit:
        return _normalize_openai_base(explicit)
    assistant = os.getenv("SIGESA_ASSISTANT_BASE_URL")
    if assistant:
        return _normalize_openai_base(assistant)
    groq_base = os.getenv("GROQ_BASE_URL")
    if groq_base and (os.getenv("SIGESA_LLM_PROVIDER") or "").strip().lower() == "groq":
        return _normalize_openai_base(groq_base)
    if os.getenv("GROQ_API_KEY"):
        return _normalize_openai_base(groq_base or "https://api.groq.com/openai/v1")
    return _normalize_openai_base("http://127.0.0.1:3001/api")


def judge_model() -> str:
    return (
        os.getenv("SIGESA_JUDGE_MODEL")
        or os.getenv("SIGESA_ASSISTANT_MODEL")
        or os.getenv("SIGESA_LLM_MODEL_GROQ")
        or "llama3.2:3b"
    )


def judge_api_key() -> str:
    return (
        os.getenv("SIGESA_JUDGE_API_KEY")
        or os.getenv("SIGESA_ASSISTANT_API_KEY")
        or os.getenv("GROQ_API_KEY")
        or ""
    )


def _load_system_prompt() -> str:
    if JUDGE_PROMPT_PATH.is_file():
        return JUDGE_PROMPT_PATH.read_text(encoding="utf-8").strip()
    return "Responde JSON: {\"pass\": bool, \"rationale\": str}"


def build_user_prompt(case: dict[str, Any], capture: dict[str, Any]) -> str:
    reply = str(capture.get("reply") or "")
    steps = capture.get("steps") or []
    steps_short = [
        {
            "toolId": s.get("toolId"),
            "success": s.get("success"),
        }
        for s in steps
        if isinstance(s, dict)
    ]
    reply_display = reply if reply else "(vacío)"
    return (
        f"Caso: {case.get('id')}\n"
        f"Tipo: {case.get('tipo')}\n"
        f"Crítico: {case.get('critical')}\n"
        f"Pregunta usuario: {case.get('mensaje')}\n"
        f"Comportamiento esperado: {case.get('comportamiento_esperado')}\n"
        f"HTTP captura: {capture.get('httpStatus')}\n"
        f"Respuesta asistente:\n{reply_display}\n"
        f"Steps tools (success): {json.dumps(steps_short, ensure_ascii=False)}\n"
        f"Tool ids ejecutados: {[s.get('toolId') for s in steps_short]}\n"
    )


def parse_judge_json(text: str) -> tuple[bool | None, str]:
    text = text.strip()
    try:
        obj = json.loads(text)
        return bool(obj.get("pass")), str(obj.get("rationale") or "")
    except json.JSONDecodeError:
        pass
    match = re.search(r"\{[^{}]*\"pass\"\s*:\s*(true|false)[^{}]*\}", text, re.IGNORECASE | re.DOTALL)
    if match:
        try:
            obj = json.loads(match.group(0))
            return bool(obj.get("pass")), str(obj.get("rationale") or "")
        except json.JSONDecodeError:
            pass
    low = text.lower()
    if "pass" in low and "true" in low:
        return True, text[:200]
    if "pass" in low and "false" in low:
        return False, text[:200]
    return None, text[:300]


def call_judge_llm(case: dict[str, Any], capture: dict[str, Any], *, temperature: float = 0.0) -> dict[str, Any]:
    base = judge_llm_base_url()
    url = _chat_completions_url(base)
    system = _load_system_prompt()
    user = build_user_prompt(case, capture)
    body = {
        "model": judge_model(),
        "stream": False,
        "temperature": temperature,
        "messages": [
            {"role": "system", "content": system},
            {"role": "user", "content": user},
        ],
    }
    headers = {
        "Content-Type": "application/json",
        "Accept": "application/json",
        "User-Agent": "SIGESA-Evals-Judge/1.0 (compatible; Groq/OpenAI)",
    }
    key = judge_api_key()
    if key:
        headers["Authorization"] = f"Bearer {key}"
    req = urllib.request.Request(
        url,
        data=json.dumps(body).encode(),
        headers=headers,
        method="POST",
    )
    record: dict[str, Any] = {"caseId": case.get("id"), "model": judge_model()}
    try:
        with urllib.request.urlopen(req, timeout=120) as resp:
            record["httpStatus"] = resp.status
            payload = json.loads(resp.read().decode())
        content = (
            payload.get("choices", [{}])[0]
            .get("message", {})
            .get("content", "")
        )
        record["raw"] = content
        passed, rationale = parse_judge_json(str(content))
        record["pass"] = passed
        record["rationale"] = rationale
        if passed is None:
            record["error"] = "judge_parse_failed"
    except urllib.error.HTTPError as err:
        record["httpStatus"] = err.code
        record["pass"] = None
        record["error"] = err.read().decode()[:500]
    except Exception as ex:
        record["pass"] = None
        record["error"] = str(ex)[:500]
    return record
