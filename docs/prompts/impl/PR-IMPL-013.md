---
id: PR-IMPL-013
feature_asociado: DD-SYS-002
fsd_uc:
  - PRD-REQ-028
  - FSD-UC-002
prd_refs:
  - PRD-REQ-028
modulo: MOD-ASSISTANT
fecha: "2026-07-31"
version: "1.0"
estado: Aprobado
autor: "Cursor Agent (@sigesa-prompt-contract-architect)"
skill_origen: sigesa-prompt-contract-architect
---

# Prompt Contract — Implementación `PR-IMPL-013`

> **Design doc fuente:** [`DD-SYS-002` §11](../../design/DD-SYS-002.md#11-tool-calling-fase-11--read-only) · **Catálogo tools:** [`TOOL-CATALOG.md`](../../design/assistant/TOOL-CATALOG.md) · **Contrato API:** [`API-USER-03`](../../product/api/API-USER-03.md) · **Requisito producto:** PRD-REQ-028.

---

## 1. Propósito y Objetivo

Implementar el **loop de tool calling read-only** en el asistente virtual SIGESA (MOD-ASSISTANT Fase 1.1):

- Orquestación multi-turno en el **backend** (`SendChatMessageService`).
- Registro dinámico de tools según rol JWT.
- Primera tool operativa: **`list_users`** (solo **JD**), delegando en `ListUsersUseCase`.
- Extensión de `ChatCompletionPort` / `OpenWebUiChatAdapter` para formato OpenAI function calling.
- **Sin cambios** en el contrato REST expuesto al frontend (`POST /api/v1/assistant/chat` sigue devolviendo `{ "reply": "..." }`).

---

## 2. Rol y Persona

- **Identidad:** Desarrollador Backend Senior experto en SIGESA.
- **Expertise:** Java 21, Spring Boot 4.x, arquitectura hexagonal, OpenAI-compatible tool calling, Spring Security (extracción de contexto JWT), JUnit 5 + Mockito.

---

## 3. Límites de Alcance

### In-Scope

**Dominio / aplicación**

- `AssistantAuthContext` — `userId`, `role`, `programScope` (record puro).
- `AssistantToolDefinition` — metadatos de tool (id, description, allowedRoles, sideEffect, parameterSchema).
- `AssistantToolRegistry` — catálogo en memoria; método `toolsForRole(String role)`.
- `AssistantToolExecutor` — dispatch por `toolId`; validación rol + parseo JSON args.
- `ToolExecutionResult` — envoltura `{ ok, data, error }` serializable a JSON.
- Refactor `SendChatMessageService` → loop con máx. **3** iteraciones tool-call.
- Extender `ChatCompletionPort`:
  - Entrada: `ChatCompletionRequest(messages, tools?)`
  - Salida: `ChatCompletionResult` — `content?` **o** `toolCalls?` (mutuamente excluyentes por turno).
- Extender `ChatRole` con valor **`TOOL`** para mensajes de resultado de tool en la conversación.
- Handler `list_users` → `ListUsersUseCase.list(roleFilter, statusFilter)`.

**Adaptadores**

- `OpenWebUiChatAdapter` — serializar `tools[]` en request; parsear `tool_calls` en response.
- `AssistantController` — extraer `AssistantAuthContext` desde `SecurityContext` (JWT ya validado por filtro) y pasarlo al use case.
- `AssistantModuleConfig` — wiring de registry, executor, use case extendido.

**Config**

- `AssistantProperties.maxToolIterations` (default `3`).

**Tests**

- Unit: `AssistantToolRegistryTest`, `AssistantToolExecutorTest`, `SendChatMessageServiceToolLoopTest`.
- Integración: `AssistantControllerToolCallingIT` — JD invoca listado; CC no registra tool.

**Documentación viva (post-implementación)**

- Actualizar `DD-SYS-002` §11.8 (estado → implementado).
- Actualizar `DTP.md` §B.5 con tool calling.
- Registrar en `docs/sprints/sprint_02/PROMPT_MAPPING.md` (PM-002).

### Out-of-Scope

- Tools de escritura (`create_process`, `deactivate_user`, etc.).
- Cambios en frontend `/ayuda` (misma API `{ message, history }` → `{ reply }`).
- Streaming SSE de tool calls.
- RAG / embeddings.
- Persistencia de conversaciones.
- Exponer tool calls al cliente (detalle interno del loop).
- Modificar `docs/baseline/`.
- Nuevos endpoints REST públicos para tools.

---

## 4. Restricciones y Reglas

| ID | Regla |
|----|-------|
| R1 | Arquitectura hexagonal: dominio/aplicación sin Spring/JPA; beans vía `AssistantModuleConfig`, no `@Service`. |
| R2 | Cada tool delega **exclusivamente** en un puerto de aplicación existente. Prohibido inyectar repositorios JPA en el executor. |
| R3 | Registro dinámico: `list_users` solo en `tools[]` si `authContext.role() == "JD"`. |
| R4 | Defensa en profundidad: `AssistantToolExecutor` revalida rol aunque el LLM alucine un `tool_call`. |
| R5 | Máximo **3** iteraciones tool-call por mensaje de usuario; si se excede → respuesta de fallback al usuario. |
| R6 | Formato de resultado tool hacia LLM: JSON `{ "ok", "data", "error" }` según [`TOOL-CATALOG.md`](../../design/assistant/TOOL-CATALOG.md). |
| R7 | `HttpClient` hacia Open WebUI sigue en **HTTP/1.1**. |
| R8 | System prompt del servidor; ignorar `system` del historial cliente (heredado PR-IMPL-012). |
| R9 | No exponer `passwordHash`, tokens ni datos fuera de [`API-USER-03`](../../product/api/API-USER-03.md). |
| R10 | Errores de dominio en tools (`InvalidRoleException`, `InvalidFilterException`) → mapear a `{ ok: false, error: { code, message } }`, no lanzar al controller. |
| R11 | JaCoCo ≥ 90% en `AssistantToolExecutor` y `SendChatMessageService` (clases tocadas). |

---

## 5. Especificaciones de Entrada

### 5.1 Documentos fuente

| Documento | Uso |
|-----------|-----|
| [`DD-SYS-002` §11](../../design/DD-SYS-002.md#11-tool-calling-fase-11--read-only) | Arquitectura del loop |
| [`TOOL-CATALOG.md`](../../design/assistant/TOOL-CATALOG.md) | Definición `list_users`, schemas, autorización |
| [`API-USER-03.md`](../../product/api/API-USER-03.md) | Contrato REST equivalente |
| [`PR-IMPL-012`](./PR-IMPL-012.md) | Baseline asistente MVP |
| `ListUsersUseCase.java` | Puerto existente a reutilizar |

### 5.2 Contexto de autenticación (desde JWT)

```java
public record AssistantAuthContext(
    UUID userId,
    String role,           // "JD" | "CC" | "TD"
    List<UUID> programScope
) {}
```

Extraído en `AssistantController` desde `SecurityContextHolder` + claims JWT (mismo origen que `JwtAuthenticationFilter`).

### 5.3 Tool `list_users` — parámetros

```json
{
  "role": "CC",
  "status": "ACTIVE"
}
```

Ambos opcionales. Ver schema en [`TOOL-CATALOG.md` §3.4](../../design/assistant/TOOL-CATALOG.md#34-json-schema-parámetros).

### 5.4 Request Open WebUI (referencia)

```json
{
  "model": "llama3.2:3b",
  "stream": false,
  "messages": [
    { "role": "system", "content": "..." },
    { "role": "user", "content": "¿Qué usuarios CC activos hay?" }
  ],
  "tools": [
    {
      "type": "function",
      "function": {
        "name": "list_users",
        "description": "Lista usuarios SIGESA (email, rol, estado). Solo JD.",
        "parameters": { "...": "..." }
      }
    }
  ]
}
```

### 5.5 Response Open WebUI con tool_call

```json
{
  "choices": [{
    "message": {
      "role": "assistant",
      "content": null,
      "tool_calls": [{
        "id": "call_abc123",
        "type": "function",
        "function": {
          "name": "list_users",
          "arguments": "{\"role\":\"CC\",\"status\":\"ACTIVE\"}"
        }
      }]
    }
  }]
}
```

---

## 6. Especificaciones de Salida

### 6.1 API REST (sin cambio de contrato)

`POST /api/v1/assistant/chat` — respuesta final tras completar el loop:

```json
{
  "reply": "Hay 2 coordinadores de carrera activos: cc@umss.edu.bo y cc2@umss.edu.bo."
}
```

### 6.2 Resultado interno de tool (hacia LLM)

```json
{
  "ok": true,
  "data": {
    "users": [
      {
        "userId": "550e8400-e29b-41d4-a716-446655440000",
        "email": "cc@umss.edu.bo",
        "role": "CC",
        "status": "ACTIVE",
        "programIds": ["550e8400-e29b-41d4-a716-446655440000"]
      }
    ],
    "total": 1
  },
  "error": null
}
```

### 6.3 Nuevos tipos aplicación (referencia)

```java
public record ChatCompletionRequest(List<ChatMessage> messages, List<AssistantToolDefinition> tools) {}

public record ChatCompletionResult(String content, List<ToolCall> toolCalls) {
    public boolean hasToolCalls() { return toolCalls != null && !toolCalls.isEmpty(); }
}

public record ToolCall(String id, String name, String argumentsJson) {}

public record ToolExecutionResult(boolean ok, Object data, ToolError error) {
    public record ToolError(String code, String message) {}
}
```

### 6.4 Firma extendida del use case

```java
public interface SendChatMessageUseCase {
    String send(String userMessage, List<ChatMessage> history, AssistantAuthContext authContext);
}
```

---

## 7. Diseño del loop (pseudocódigo obligatorio)

```java
public String send(String userMessage, List<ChatMessage> history, AssistantAuthContext auth) {
    List<ChatMessage> conversation = buildConversation(systemPrompt, history, userMessage);
    List<AssistantToolDefinition> tools = toolRegistry.toolsForRole(auth.role());

    for (int i = 0; i < maxToolIterations; i++) {
        ChatCompletionResult result = chatCompletionPort.complete(
            new ChatCompletionRequest(conversation, tools));

        if (!result.hasToolCalls()) {
            return requireNonBlank(result.content());
        }

        conversation.add(toAssistantMessage(result.toolCalls()));

        for (ToolCall call : result.toolCalls()) {
            String toolJson = toolExecutor.execute(call.name(), call.argumentsJson(), auth);
            conversation.add(new ChatMessage(ChatRole.TOOL, toolJson, call.id())); // tool_call_id
        }
        // siguiente iteración: LLM procesa resultados y responde o pide más tools
    }

    return "No pude completar la consulta en el número máximo de pasos. Intente reformular su pregunta.";
}
```

> **Nota:** `ChatMessage` debe soportar `toolCallId` opcional para mensajes `role=tool` (OpenAI exige `tool_call_id`).

---

## 8. Criterios de Aceptación

### Funcionales

- [ ] Usuario **JD** pregunta *"¿Qué usuarios tenemos registrados?"* → respuesta natural con datos reales obtenidos vía `list_users`.
- [ ] Usuario **JD** filtra *"coordinadores activos"* → tool invocada con `role=CC, status=ACTIVE`.
- [ ] Usuario **CC/TD** pregunta por listado de usuarios → **sin** tool en payload; respuesta textual sin datos administrativos.
- [ ] Args inválidos (`role=ADMIN`) → tool devuelve `{ ok: false, error: { code: "INVALID_ROLE" } }`; LLM explica el error al usuario.
- [ ] Loop respeta máximo 3 iteraciones.

### Técnicos

- [ ] `ChatCompletionPort` extendido sin romper tests existentes de chat simple (sin tools).
- [ ] `OpenWebUiChatAdapter` envía `tools` solo cuando la lista no está vacía.
- [ ] `AssistantController` pasa `AssistantAuthContext` desde JWT.
- [ ] Tests unitarios + integración verdes.
- [ ] JaCoCo ≥ 90% en clases nuevas/modificadas del módulo assistant.
- [ ] Sin cambios en contrato frontend Orval (`SendChatMessageRequest` / `SendChatMessageResponse`).

### Documentales

- [ ] `DD-SYS-002` §11.8 actualizado a **Implementado**.
- [ ] `DTP.md` §B.5 sincronizado (`@dtp-sync`).
- [ ] Entrada PM-002 en `PROMPT_MAPPING.md` sprint_02.

---

## 9. Archivos a crear / modificar

### Backend — nuevos

| Archivo | Responsabilidad |
|---------|-----------------|
| `application/model/assistant/AssistantAuthContext.java` | Contexto JWT |
| `application/model/assistant/AssistantToolDefinition.java` | Metadatos tool |
| `application/model/assistant/ChatCompletionRequest.java` | Request al puerto LLM |
| `application/model/assistant/ChatCompletionResult.java` | Response del puerto LLM |
| `application/model/assistant/ToolCall.java` | Tool call del LLM |
| `application/model/assistant/ToolExecutionResult.java` | Envoltura ok/error |
| `application/service/assistant/AssistantToolRegistry.java` | Catálogo + filtro por rol |
| `application/service/assistant/AssistantToolExecutor.java` | Dispatch + `list_users` |
| `application/service/assistant/ListUsersToolHandler.java` | (opcional) handler dedicado |

### Backend — modificar

| Archivo | Cambio |
|---------|--------|
| `domain/model/ChatRole.java` | Añadir `TOOL` |
| `domain/model/ChatMessage.java` | Campo opcional `toolCallId` |
| `application/port/out/ChatCompletionPort.java` | Firma con `ChatCompletionRequest` |
| `application/port/in/SendChatMessageUseCase.java` | Añadir `AssistantAuthContext` |
| `application/service/assistant/SendChatMessageService.java` | Loop tool calling |
| `adapter/out/assistant/OpenWebUiChatAdapter.java` | tools + tool_calls |
| `adapter/in/web/AssistantController.java` | Extraer auth context JWT |
| `config/AssistantModuleConfig.java` | Wiring registry + executor |
| `config/AssistantProperties.java` | `maxToolIterations` |
| `resources/application.yaml` | `sigesa.assistant.max-tool-iterations: 3` |

### Backend — tests

| Archivo | Escenarios |
|---------|------------|
| `AssistantToolRegistryTest.java` | JD ve `list_users`; CC/TD no |
| `AssistantToolExecutorTest.java` | JD ok; CC denied; invalid args |
| `SendChatMessageServiceToolLoopTest.java` | 1 tool call; 0 tool calls; max iterations |
| `AssistantControllerToolCallingIT.java` | E2E mock LLM con tool_call |

### Documentación

| Archivo | Acción |
|---------|--------|
| `docs/design/DD-SYS-002.md` | §11.8 estado implementado |
| `docs/product/DTP.md` | §B.5 tool calling |
| `docs/sprints/sprint_02/PROMPT_MAPPING.md` | PM-002 |

---

## 10. Plan de pruebas

| ID | Escenario | Rol | Mock LLM | Esperado |
|----|-----------|-----|----------|----------|
| T1 | Pregunta listado usuarios | JD | 1× tool_call `list_users` → texto | `reply` con datos |
| T2 | Chat FAQ sin tools | JD | respuesta directa | `reply` sin executor |
| T3 | Pregunta listado | CC | respuesta directa | tools=[] en adapter |
| T4 | Executor directo CC | CC | — | `ACCESS_DENIED` |
| T5 | Args `role=INVALID` | JD | — | `INVALID_ROLE` en tool result |
| T6 | 4 tool_calls seguidos | JD | loop | fallback max iterations |

---

## 11. Secuencia de implementación sugerida

1. Extender modelos dominio (`ChatRole.TOOL`, `ChatMessage.toolCallId`).
2. Crear DTOs aplicación (`AssistantAuthContext`, `ChatCompletionRequest/Result`).
3. Extender `ChatCompletionPort` + adaptar `OpenWebUiChatAdapter`.
4. Implementar `AssistantToolRegistry` con `list_users` hardcoded según TOOL-CATALOG.
5. Implementar `AssistantToolExecutor` + handler `list_users`.
6. Refactor `SendChatMessageService` con loop.
7. Actualizar `AssistantController` + `AssistantModuleConfig`.
8. Tests unitarios → integración.
9. Verificar chat simple sigue funcionando sin tools (regresión PR-IMPL-012).
10. `@dtp-sync` + `@save-prompt-mapping sprint=2 pr=PR-IMPL-013`.

---

## 12. Prompt de ejecución (copiar en Composer)

```text
Implementa PR-IMPL-013 — tool calling read-only MOD-ASSISTANT (Fase 1.1).

Fuentes obligatorias:
- docs/prompts/impl/PR-IMPL-013.md
- docs/design/DD-SYS-002.md §11
- docs/design/assistant/TOOL-CATALOG.md
- docs/product/api/API-USER-03.md
- AGENTS.md (hexagonal estricta)

Alcance:
1. Loop tool calling en SendChatMessageService (max 3 iteraciones).
2. AssistantToolRegistry + AssistantToolExecutor.
3. Tool list_users (solo JD) → ListUsersUseCase.
4. Extender ChatCompletionPort y OpenWebUiChatAdapter (tools + tool_calls).
5. AssistantController extrae AssistantAuthContext del JWT.
6. Tests unitarios + integración según §10 PR-IMPL-013.
7. Sin cambios frontend API.

Prohibido:
- Tools de escritura.
- Acceso JPA directo desde executor.
- Modificar docs/baseline/.
- @Service en capa aplicación (usar AssistantModuleConfig).

Al cerrar: actualizar DD-SYS-002 §11.8, DTP §B.5, PROMPT_MAPPING sprint_02 PM-002.
```

---

## 13. Trazabilidad

| Artefacto | Referencia |
|-----------|------------|
| Design doc | [DD-SYS-002 §11](../../design/DD-SYS-002.md#11-tool-calling-fase-11--read-only) |
| Catálogo tools | [TOOL-CATALOG.md](../../design/assistant/TOOL-CATALOG.md) |
| API list_users | [API-USER-03.md](../../product/api/API-USER-03.md) |
| FSD usuarios | [FSD-UC-002](../../product/uc/FSD-UC-002.md) |
| MVP asistente | [PR-IMPL-012](./PR-IMPL-012.md) |
| Sprint mapping | [PROMPT_MAPPING sprint_02 PM-002](../../sprints/sprint_02/PROMPT_MAPPING.md#pm-002) |
