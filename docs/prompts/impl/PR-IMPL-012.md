---
id: PR-IMPL-012
feature_asociado: DD-SYS-002
fsd_uc:
  - PRD-REQ-028
prd_refs:
  - PRD-REQ-028
  - BRD-REQ-024
modulo: MOD-ASSISTANT
fecha: "2026-07-27"
version: "1.0"
estado: Completado
autor: "Cursor Agent (feature/chatbot-boris)"
---

# Prompt Contract — Implementación `PR-IMPL-012`

> **Design doc fuente:** [`DD-SYS-002`](../../design/DD-SYS-002.md) · **Requisito producto:** PRD-REQ-028 (Chatbot FAQ normativo — MVP proxy).

---

## 1. Propósito y Objetivo

Implementar el **asistente virtual SIGESA** (MOD-ASSISTANT): chat in-app en `/ayuda` con backend como proxy autenticado hacia **Open WebUI** (API OpenAI-compatible) y **Ollama** (`llama3.2:3b`).

---

## 2. Rol y Persona

Desarrollador Full-Stack SIGESA — Java 21 / Spring Boot 4.x (hexagonal) + React 19 / Orval / React Query.

---

## 3. Límites de Alcance

### In-Scope

**Backend**

- Dominio: `ChatMessage`, `ChatRole`; excepciones `AssistantUnavailableException`, `AssistantCompletionException`.
- Puertos: `SendChatMessageUseCase`, `ChatCompletionPort`.
- Servicio: `SendChatMessageService` (system prompt + historial + mensaje usuario).
- Adaptador salida: `OpenWebUiChatAdapter` → `POST {baseUrl}/v1/chat/completions` (HTTP/1.1).
- Adaptador entrada: `AssistantController` — `GET /api/v1/assistant/status`, `POST /api/v1/assistant/chat`.
- Config: `AssistantProperties`, `AssistantModuleConfig`; bloque `sigesa.assistant` en `application.yaml`.
- Advice: `AssistantExceptionHandler` (503 / 502 + códigos JSON).

**Frontend**

- Feature `frontend/src/features/assistant/` (página, UI, hook).
- Cliente API Orval-style + tipos `assistantTypes.ts`.
- Ruta `/ayuda` y enlace Sidebar `AYUDA`.

**Infraestructura**

- Servicios Docker: `ollama`, `open-webui`.
- Variables `SIGESA_ASSISTANT_*` en `docker-compose.yml` y `.env` (gitignored).

### Out-of-Scope

- Persistencia de conversaciones en PostgreSQL.
- Streaming SSE de respuestas.
- RAG sobre documentos normativos aprobados.
- Rate limiting / auditoría de prompts.
- Modificar `docs/baseline/`.

---

## 4. Restricciones y Reglas

| ID | Regla |
|----|-------|
| R1 | Arquitectura hexagonal: dominio sin Spring/JPA; beans de aplicación vía `*ModuleConfig`, no `@Service`. |
| R2 | Frontend: consumo API solo vía hooks Orval / `customFetch`; prohibido `fetch`/`axios` manual en features. |
| R3 | API key Open WebUI **nunca** expuesta al navegador; solo JWT SIGESA en frontend. |
| R4 | System prompt definido en servidor (`sigesa.assistant.system-prompt`); ignorar `system` del historial cliente. |
| R5 | `HttpClient` debe usar **HTTP/1.1** hacia Open WebUI (uvicorn rechaza HTTP/2 de Java). |
| R6 | Cero credenciales en commits (`.env` gitignored). |

---

## 5. Criterios de Aceptación

- [x] Usuario autenticado envía mensaje en `/ayuda` y recibe respuesta del modelo.
- [x] `GET /api/v1/assistant/status` devuelve `enabled` y `model`.
- [x] Errores de config/auth → `ASSISTANT_UNAVAILABLE` (503); fallo inferencia → `ASSISTANT_COMPLETION_FAILED` (502).
- [x] Stack Docker Compose levanta `ollama` + `open-webui` + `backend` con healthchecks.
- [x] Documentación de diseño en `docs/design/DD-SYS-002.md`.

---

## 6. Archivos de Contexto

- [`docs/design/DD-SYS-002.md`](../../design/DD-SYS-002.md)
- [`docker-compose.yml`](../../../docker-compose.yml)
- [`AGENTS.md`](../../../AGENTS.md)

---

## 7. Entregables generados

Ver inventario completo en [`DD-SYS-002` §9](../../design/DD-SYS-002.md).
