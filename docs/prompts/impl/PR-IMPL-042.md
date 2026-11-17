---
id: PR-IMPL-042
feature_asociado: DD-SYS-002
fsd_uc:
  - FSD-UC-024
fecha: "2026-10-17"
version: "1.0"
estado: Aprobado
autor: "Boris Anthony Angulo Urquieta"
github_issue: "https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss/issues/52"
release: "8.1.0"
---

# Prompt Contract — Implementación `PR-IMPL-042`

> **Issue:** [#52 FEAT-03](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss/issues/52) — Cliente HTTP resiliente (timeout 10 s) y ejecución no bloqueante hacia el LLM.  
> **Design docs:** [`DD-SYS-002`](../../design/DD-SYS-002.md) · [`DD-AGENT-003`](../../design/assistant/DD-AGENT-003.md)  
> **FSD:** [`FSD-UC-024`](../../product/uc/FSD-UC-024.md) (copilotos / `POST /assistant/chat`)  
> **Auditoría:** CON-02 / CON-05 en `docs/auditoria/concurrencia-boris.md` (veredicto histórico intacto; esta pieza es remediación 8.1.0).  
> **Rama:** `feat/52-llm-timeout-async`

---

## 1. Propósito y Objetivo

Evitar que `HttpClient.send` síncrono con timeout de **120 s** retenga workers Tomcat de plataforma y que un spike de chat deje el resto de la API en cola.

- Timeout de **conexión y lectura de 10 s** (configurable) hacia Open WebUI / Ollama.
- Desacoplar el wait LLM de hilos de plataforma Tomcat (hilos virtuales JDK 21 y/o `sendAsync`).
- Si se supera el timeout: **504** con código semántico y mensaje amigable; el hilo no queda colgado.

---

## 2. Rol y Persona

Desarrollador Backend Senior SIGESA — Java 21, Spring Boot 4.x, hexagonal, `java.net.http.HttpClient`.

---

## 3. Límites de Alcance

### In-Scope

- `OpenWebUiChatAdapter` (`ChatCompletionPort`): `connectTimeout` + timeout de request desde `sigesa.assistant.timeout` (default **10 s** / `SIGESA_ASSISTANT_TIMEOUT_MS=10000`).
- Llamada no bloqueante en hilo de plataforma: `HttpClient.sendAsync` **o** `spring.threads.virtual.enabled=true` (Tomcat sobre hilos virtuales). HTTP/1.1 se mantiene.
- Excepción de dominio de timeout → `AssistantExceptionHandler` **504** `{ error: ASSISTANT_GATEWAY_TIMEOUT, message }`.
- Tests del adapter/handler: timeout mapea a 504; HTTP/1.1 y contrato de body OpenAI no cambian.
- Docs vivos: FSD/DD-SYS-002/DTP/api_contracts + este contrato + PM sprint 03.

### Out-of-Scope

- Cambio de proveedor LLM, modelo, o contrato MCP.
- Cambio de ruta REST (`POST /api/v1/assistant/chat` sigue síncrono desde el cliente; **no** 202/polling).
- Retry/backoff, `Semaphore` bulkhead, `AbortSignal` en SPA/MCP (CON-04 residual).
- FEAT-01 (#50) Hikari/PDF y FEAT-02 (#51) MinIO/Silo.
- `docs/baseline/`.

---

## 4. Restricciones y Reglas

| ID | Regla |
|----|-------|
| R1 | Dominio sin Spring. Timeout no se hardcodea a 120 s; sale de `AssistantProperties` / YAML. |
| R2 | `HttpClient.Version.HTTP_1_1` obligatorio (uvicorn/Open WebUI rechaza HTTP/2 de Java). |
| R3 | Default **10 s** conexión y lectura, alineado al issue. Demo local con Ollama lento: subir `SIGESA_ASSISTANT_TIMEOUT_MS` (no reabrir 120 s en código). |
| R4 | `java.net.http.HttpTimeoutException` (y wrapping) → 504, no 502 `ASSISTANT_COMPLETION_FAILED`. |
| R5 | Mensaje 504 amigable (ES), p. ej. «El asistente tardó demasiado. Intente de nuevo.» Sin stack al cliente. |
| R6 | Tras timeout no queda `join()`/`send()` colgado: cancelación del request HTTP. |
| R7 | Loop de tools (`SendChatMessageService`) hereda el timeout **por cada** round-trip LLM, no un presupuesto global de 10 s × N tools (documentar; no alargar el default). |
| R8 | Cierre de implementación: `@save-prompt-mapping sprint=03 pr=PR-IMPL-042`. |

---

## 5. Criterio de aceptación (issue #52)

- Peticiones que excedan el timeout devuelven **504 Gateway Timeout** limpio con mensaje amigable.
- No quedan hilos colgados (request HTTP al LLM cancelado).
- Workers Tomcat de plataforma no se retienen 120 s por chat.

---

## 6. Trazabilidad

`FSD-UC-024` → `DD-SYS-002` → `PR-IMPL-042` → GitHub #52 → sprint 03 `PM-023`
