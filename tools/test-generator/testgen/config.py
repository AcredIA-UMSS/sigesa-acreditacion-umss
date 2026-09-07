from __future__ import annotations

import os
from enum import Enum
from pathlib import Path

from dotenv import dotenv_values


class RuntimeMode(str, Enum):
    HOST = "host"
    DOCKER = "docker"

    @classmethod
    def resolve(cls, raw: str | None) -> RuntimeMode:
        if raw is None or raw.strip() == "" or raw.strip().lower() == "auto":
            if os.getenv("SIGESA_IN_DOCKER", "").lower() == "true":
                return cls.DOCKER
            if Path("/.dockerenv").exists():
                return cls.DOCKER
            return cls.HOST
        return cls.DOCKER if raw.strip().lower() == "docker" else cls.HOST


def find_repo_root() -> Path:
    explicit = os.getenv("SIGESA_REPO_ROOT")
    if explicit:
        return Path(explicit).resolve()
    cursor = Path.cwd().resolve()
    while cursor != cursor.parent:
        if (cursor / "backend").is_dir() and (cursor / "tools").is_dir():
            return cursor
        cursor = cursor.parent
    return Path.cwd().resolve()


class Env:
    def __init__(self, repo_root: Path) -> None:
        self.repo_root = repo_root
        self._file = dotenv_values(repo_root / ".env")

    def get(self, key: str, default: str | None = None) -> str | None:
        sys_val = os.getenv(key)
        if sys_val is not None and sys_val.strip():
            return sys_val.strip()
        file_val = self._file.get(key)
        if file_val is not None and str(file_val).strip():
            return str(file_val).strip()
        return default

    def get_int(self, key: str, default: int) -> int:
        raw = self.get(key)
        if raw is None:
            return default
        try:
            return int(raw)
        except ValueError:
            return default

    def get_float(self, key: str, default: float) -> float:
        raw = self.get(key)
        if raw is None:
            return default
        try:
            return float(raw)
        except ValueError:
            return default


def first_non_blank(*values: str | None) -> str | None:
    for value in values:
        if value is not None and value.strip():
            return value.strip()
    return None


def normalize_base_url(url: str | None) -> str | None:
    if url is None or not url.strip():
        return url
    trimmed = url.strip().rstrip("/")
    return trimmed


class LlmSettings:
    def __init__(
        self,
        provider: str,
        runtime_mode: RuntimeMode,
        base_url: str,
        api_key: str,
        model: str,
        temperature: float,
        max_tokens: int,
        timeout_seconds: float,
    ) -> None:
        self.provider = provider
        self.runtime_mode = runtime_mode
        self.base_url = base_url
        self.api_key = api_key
        self.model = model
        self.temperature = temperature
        self.max_tokens = max_tokens
        self.timeout_seconds = timeout_seconds

    @classmethod
    def resolve(cls, env: Env) -> LlmSettings:
        runtime = RuntimeMode.resolve(env.get("SIGESA_RUNTIME", "auto"))
        provider = (
            first_non_blank(
                env.get("SIGESA_TESTGEN_PROVIDER"),
                env.get("SIGESA_LLM_PROVIDER"),
                "local",
            )
            or "local"
        ).lower()

        base_url = cls._resolve_base_url(env, provider, runtime)
        api_key = cls._resolve_api_key(env, provider) or ""
        model = cls._resolve_model(env, provider) or ""
        temperature = env.get_float("SIGESA_TESTGEN_TEMPERATURE", 0.0)
        max_tokens = env.get_int("SIGESA_TESTGEN_MAX_TOKENS", 4096)
        timeout_seconds = env.get_float("SIGESA_TESTGEN_TIMEOUT_SECONDS", 900.0)

        return cls(
            provider, runtime, base_url, api_key, model, temperature, max_tokens, timeout_seconds
        )

    @staticmethod
    def _resolve_base_url(env: Env, provider: str, runtime: RuntimeMode) -> str:
        explicit = env.get("SIGESA_TESTGEN_BASE_URL")
        if explicit:
            return normalize_base_url(explicit) or explicit
        if provider == "groq":
            return normalize_base_url(env.get("GROQ_BASE_URL", "https://api.groq.com/openai/v1")) or ""
        host_url = env.get("SIGESA_LLM_BASE_URL_HOST", "http://localhost:11434/v1") or ""
        docker_url = env.get("SIGESA_LLM_BASE_URL_DOCKER", "http://ollama:11434/v1") or ""
        return normalize_base_url(docker_url if runtime == RuntimeMode.DOCKER else host_url) or ""

    @staticmethod
    def _resolve_api_key(env: Env, provider: str) -> str | None:
        explicit = env.get("SIGESA_TESTGEN_API_KEY")
        if explicit:
            return explicit
        if provider == "groq":
            return env.get("GROQ_API_KEY")
        return env.get("SIGESA_ASSISTANT_API_KEY")

    @staticmethod
    def _resolve_model(env: Env, provider: str) -> str | None:
        explicit = env.get("SIGESA_TESTGEN_MODEL")
        if explicit:
            return explicit
        if provider == "groq":
            return env.get("SIGESA_LLM_MODEL_GROQ", "openai/gpt-oss-20b")
        return first_non_blank(
            env.get("SIGESA_LLM_MODEL_LOCAL"),
            env.get("SIGESA_ASSISTANT_MODEL"),
            "qwen2.5:7b",
        )

    def validate(self) -> None:
        if self.provider == "groq" and not self.api_key.strip():
            raise ValueError("Provider groq requiere GROQ_API_KEY o SIGESA_TESTGEN_API_KEY en .env")
        if not self.model.strip():
            raise ValueError("Modelo LLM no configurado (SIGESA_TESTGEN_MODEL o SIGESA_LLM_MODEL_*)")
        if not self.base_url.strip():
            raise ValueError("Base URL LLM no configurada")

    def __str__(self) -> str:
        masked = "(vacía)" if not self.api_key else f"{self.api_key[:4]}***"
        return (
            f"LlmSettings(provider={self.provider}, runtime={self.runtime_mode.value}, "
            f"base_url={self.base_url}, api_key={masked}, model={self.model}, "
            f"temperature={self.temperature}, timeout={self.timeout_seconds}s)"
        )
