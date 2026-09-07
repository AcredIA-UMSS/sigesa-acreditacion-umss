from __future__ import annotations

import httpx

from testgen.config import LlmSettings


class LlmApiError(RuntimeError):
    def __init__(self, status_code: int, body: str) -> None:
        self.status_code = status_code
        self.body = body
        super().__init__(f"LLM HTTP {status_code}: {body[:500]}")


class OpenAiChatClient:
    def __init__(self, settings: LlmSettings) -> None:
        self.settings = settings

    def _headers(self) -> dict[str, str]:
        headers = {"Content-Type": "application/json"}
        if self.settings.api_key.strip():
            headers["Authorization"] = f"Bearer {self.settings.api_key}"
        return headers

    def _timeout(self) -> httpx.Timeout:
        return httpx.Timeout(
            connect=30.0,
            read=self.settings.timeout_seconds,
            write=30.0,
            pool=30.0,
        )

    def complete(self, system_prompt: str, user_prompt: str) -> str:
        payload = {
            "model": self.settings.model,
            "temperature": self.settings.temperature,
            "max_tokens": self.settings.max_tokens,
            "messages": [
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt},
            ],
        }
        url = f"{self.settings.base_url}/chat/completions"
        with httpx.Client(timeout=self._timeout()) as client:
            response = client.post(url, json=payload, headers=self._headers())

        if response.status_code >= 400:
            raise LlmApiError(response.status_code, response.text)

        data = response.json()
        content = (
            data.get("choices", [{}])[0]
            .get("message", {})
            .get("content", "")
        )
        if not content or not str(content).strip():
            raise RuntimeError(f"Respuesta LLM vacía: {response.text[:300]}")
        return str(content)

    def list_models(self) -> list[str]:
        url = f"{self.settings.base_url}/models"
        with httpx.Client(timeout=self._timeout()) as client:
            response = client.get(url, headers=self._headers())
        if response.status_code >= 400:
            raise LlmApiError(response.status_code, response.text)
        data = response.json()
        return sorted(item.get("id", "") for item in data.get("data", []) if item.get("id"))
