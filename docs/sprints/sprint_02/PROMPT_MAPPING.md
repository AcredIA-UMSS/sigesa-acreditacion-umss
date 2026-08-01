# PROMPT_MAPPING — Sprint 02

> Registro PM del sprint 02. Trazabilidad: `Código → PR-IMPL → DD-SYS-002 → PRD-REQ-028 → DTP`.

| ID Mapeo | PR-IMPL | Design Doc | FSD / PRD | Descripción de la Tarea |
| :--- | :--- | :--- | :--- | :--- |
| PM-001 | PR-IMPL-012 | DD-SYS-002 | PRD-REQ-028 | Asistente virtual SIGESA (MOD-ASSISTANT): backend proxy Open WebUI + frontend `/ayuda` + Docker Ollama |
| PM-002 | PR-IMPL-013 | DD-SYS-002 §11 | PRD-REQ-028 / FSD-UC-002 | Tool calling read-only: loop backend + tool `list_users` (solo JD) |

---

## PM-001

| Campo | Valor |
| --- | --- |
| **ID** | PM-001 |
| **Fecha** | 2026-07-27 |
| **Solicitante** | Boris Anthony Angulo Urquieta |
| **Agente/Entorno** | Cursor IDE — Agent |
| **Modelo** | Composer |
| **Tarea** | Implementación MOD-ASSISTANT — chatbot FAQ acreditación (MVP) |
| **Objetivo** | Integrar asistente virtual en SIGESA: UI `/ayuda`, API backend, proxy a Open WebUI/Ollama, stack Docker Compose |
| **Contexto** | PRD-REQ-028 / BRD-REQ-024. MVP sin RAG ni persistencia; alineado a arquitectura hexagonal y reglas frontend (Orval, tokens UMSS). Branch `feature/chatbot-boris`. |
| **PR-IMPL vinculado** | [PR-IMPL-012](../../prompts/impl/PR-IMPL-012.md) |
| **DD vinculado** | [DD-SYS-002](../../design/DD-SYS-002.md) |
| **PRD / FSD vinculado** | PRD-REQ-028 (Chatbot FAQ normativo) |
| **Estado** | completado |

### Prompt usado exacto

```text
Implementar asistente virtual SIGESA (MOD-ASSISTANT):
- Backend: AssistantController, SendChatMessageService, OpenWebUiChatAdapter,
  AssistantProperties, AssistantModuleConfig, manejo de errores.
- Frontend: feature assistant en /ayuda, useAssistantChat, AssistantChatUI, Sidebar.
- Docker: ollama + open-webui en docker-compose; SIGESA_ASSISTANT_* en backend.
- Proxy Open WebUI API OpenAI-compatible; modelo llama3.2:3b; system prompt SIGESA.
```

### Entradas auxiliares

```text
docs/product/03_prd/PRD.md (PRD-REQ-028)
AGENTS.md
.cursor/rules/frontend-design.mdc
docker-compose.yml
```

### Archivos generados o modificados

**Backend (nuevos)**

- `adapter/in/web/AssistantController.java`
- `adapter/in/web/advice/AssistantExceptionHandler.java`
- `adapter/in/web/dto/AssistantStatusResponse.java`
- `adapter/in/web/dto/SendChatMessageRequest.java`
- `adapter/in/web/dto/SendChatMessageResponse.java`
- `adapter/in/web/dto/ChatMessageDto.java`
- `adapter/out/assistant/OpenWebUiChatAdapter.java`
- `application/port/in/SendChatMessageUseCase.java`
- `application/port/out/ChatCompletionPort.java`
- `application/service/assistant/SendChatMessageService.java`
- `config/AssistantProperties.java`
- `config/AssistantModuleConfig.java`
- `domain/model/ChatMessage.java`, `ChatRole.java`
- `domain/exception/AssistantUnavailableException.java`, `AssistantCompletionException.java`

**Backend (modificados)**

- `src/main/resources/application.yaml` (bloque `sigesa.assistant`)

**Frontend (nuevos)**

- `features/assistant/AssistantPage.tsx`
- `features/assistant/components/AssistantChatUI.tsx`
- `features/assistant/hooks/useAssistantChat.ts`
- `features/assistant/hooks/mapAssistantError.ts`
- `api/endpoints/assistant-controller/assistant-controller.ts`
- `api/model/assistantTypes.ts`

**Frontend (modificados)**

- `App.tsx` (ruta `/ayuda`)
- `components/layout/Sidebar.tsx` (nav AYUDA)

**Infraestructura**

- `docker-compose.yml` (servicios `ollama`, `open-webui`; env assistant en `backend`)
- `.env` (raíz, gitignored — `SIGESA_ASSISTANT_API_KEY`)

**Documentación**

- `docs/design/DD-SYS-002.md`
- `docs/prompts/impl/PR-IMPL-012.md`
- `docs/sprints/sprint_02/PROMPT_MAPPING.md` (este archivo)
- `docs/product/DTP.md` (§MOD-ASSISTANT, changelog)

### Cambios realizados

1. **Backend hexagonal:** caso de uso `SendChatMessageService` antepone system prompt institucional; adaptador `OpenWebUiChatAdapter` consume API Open WebUI con HTTP/1.1 forzado (fix uvicorn 400).
2. **API REST:** `GET /api/v1/assistant/status`, `POST /api/v1/assistant/chat` bajo JWT; errores 503/502 con códigos `ASSISTANT_UNAVAILABLE` / `ASSISTANT_COMPLETION_FAILED`.
3. **Frontend:** chat en `/ayuda` con optimistic UI, historial en memoria, hooks React Query; UI con tokens institucionales UMSS.
4. **Docker:** Ollama + Open WebUI en red interna; backend espera healthcheck de Open WebUI antes de arrancar.
5. **Operación:** API key Open WebUI en `.env`; rotación documentada en DD-SYS-002 §6.3.

### Notas de cierre

- Primera inferencia puede tardar 5–10 s (carga del modelo en Ollama).
- No commitear `.env` con `SIGESA_ASSISTANT_API_KEY`.
- Evolución: streaming, RAG normativo, persistencia — ver DD-SYS-002 §8.

---

## PM-002

| Campo | Valor |
| --- | --- |
| **ID** | PM-002 |
| **Fecha** | 2026-07-31 |
| **Solicitante** | Boris Anthony Angulo Urquieta |
| **Agente/Entorno** | Cursor IDE — Agent |
| **Tarea** | Contrato implementación tool calling read-only (Fase 1.1 MOD-ASSISTANT) |
| **Objetivo** | Documentar e implementar loop de tool calling en backend con tool `list_users` exclusiva JD |
| **Contexto** | Extiende PR-IMPL-012 / DD-SYS-002 §11. Catálogo [`TOOL-CATALOG.md`](../../design/assistant/TOOL-CATALOG.md). API [`API-USER-03`](../../product/api/API-USER-03.md). |
| **PR-IMPL vinculado** | [PR-IMPL-013](../../prompts/impl/PR-IMPL-013.md) |
| **DD vinculado** | [DD-SYS-002 §11](../../design/DD-SYS-002.md#11-tool-calling-fase-11--read-only) |
| **PRD / FSD vinculado** | PRD-REQ-028 · FSD-UC-002 (listado usuarios vía `ListUsersUseCase`) |
| **Estado** | completado |

### Prompt usado exacto

```text
Crear PR-IMPL-013 con el loop de tool calling:
- SendChatMessageService multi-turno (max 3 iteraciones).
- AssistantToolRegistry + AssistantToolExecutor.
- Tool list_users (solo JD) → ListUsersUseCase.
- Extender ChatCompletionPort / OpenWebUiChatAdapter (tools + tool_calls).
- Sin cambios contrato frontend POST /assistant/chat.
```

### Entradas auxiliares

```text
docs/design/DD-SYS-002.md §11
docs/design/assistant/TOOL-CATALOG.md
docs/product/api/API-USER-03.md
docs/prompts/impl/PR-IMPL-012.md
AGENTS.md
```

### Entregables documentales (esta fase)

| Tipo | Ruta |
| --- | --- |
| Contrato prompt | `docs/prompts/impl/PR-IMPL-013.md` |
| Catálogo tools | `docs/design/assistant/TOOL-CATALOG.md` |
| Contrato API | `docs/product/api/API-USER-03.md` |
| DD §11 | `docs/design/DD-SYS-002.md` |

### Criterios de cierre implementación

1. Loop tool calling operativo con `list_users` para JD.
2. CC/TD no reciben tool en payload LLM.
3. Tests §10 PR-IMPL-013 verdes; JaCoCo ≥ 90% clases assistant tocadas.
4. `@dtp-sync` actualiza DTP §B.5.
5. Estado PM-002 → completado.
