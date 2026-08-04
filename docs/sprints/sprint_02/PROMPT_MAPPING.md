# PROMPT_MAPPING — Sprint 02

> Registro PM del sprint 02. Trazabilidad: `Código → PR-IMPL → DD-SYS-002 → PRD-REQ-028 → DTP`.

| ID Mapeo | PR-IMPL | Design Doc | FSD / PRD | Descripción de la Tarea |
| :--- | :--- | :--- | :--- | :--- |
| PM-001 | PR-IMPL-012 | DD-SYS-002 | PRD-REQ-028 | Asistente virtual SIGESA (MOD-ASSISTANT): backend proxy Open WebUI + frontend `/ayuda` + Docker Ollama |
| PM-002 | PR-IMPL-013 | DD-SYS-002 §11 | PRD-REQ-028 / FSD-UC-002 | Tool calling read-only: loop backend + tool `list_users` (solo JD) |
| PM-003 | PR-IMPL-019 | DD-UC-019 | FSD-UC-019 | Consulta de procesos de acreditación (GET listado + detalle, RBAC, UI `/procesos`) |

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

---

## PM-003

| Campo | Valor |
| --- | --- |
| **ID** | PM-003 |
| **Fecha** | 2026-08-03 |
| **Hora** | 19:26 |
| **Solicitante** | Boris Anthony Angulo Urquieta |
| **Agente/Entorno** | Cursor IDE — Agent |
| **Modelo** | Composer |
| **Tarea** | Consulta de procesos de acreditación |
| **Objetivo** | Exponer `GET /api/v1/processes` y `GET /api/v1/processes/{id}` con RBAC JD/TD/CC; UI listado + detalle con árbol fases/subfases |
| **Contexto** | FSD-UC-019 · DD-UC-019 · PR-IMPL-019 (contrato backend v1.1). Hexagonal estricta; [CC] cross-carrera → 404 `PROCESS_NOT_FOUND`. Branch `feature/FSD-019`. |
| **PR-IMPL vinculado** | [PR-IMPL-019](../../prompts/impl/PR-IMPL-019.md) |
| **DD-UC vinculado** | [DD-UC-019](../../design/DD-UC-019.md) |
| **FSD-UC vinculado** | [FSD-UC-019](../../product/uc/FSD-UC-019.md) |
| **Estado** | completado |

### Prompt usado exacto

```text
@PR-IMPL-019.md (1-359)
```

### Entradas auxiliares

```text
docs/design/DD-UC-019.md
docs/product/uc/FSD-UC-019.md
docs/prompts/impl/PR-IMPL-019.md
docs/product/api_contracts.md (API-PROC-03, API-PROC-04)
AGENTS.md
.cursor/rules/frontend-design.mdc
```

### Archivos generados o modificados

**Backend — generados (PR-IMPL-019 in-scope)**

| Acción | Ruta |
| --- | --- |
| generado | `backend/src/main/java/com/umss/sigesa/application/port/out/ProcessQueryPort.java` |
| generado | `backend/src/main/java/com/umss/sigesa/application/port/in/ListProcessesUseCase.java` |
| generado | `backend/src/main/java/com/umss/sigesa/application/port/in/GetProcessDetailUseCase.java` |
| generado | `backend/src/main/java/com/umss/sigesa/application/model/process/ProcessQueryContext.java` |
| generado | `backend/src/main/java/com/umss/sigesa/application/model/process/ProcessSummary.java` |
| generado | `backend/src/main/java/com/umss/sigesa/application/model/process/EnrichedProcessDetail.java` |
| generado | `backend/src/main/java/com/umss/sigesa/application/service/process/ListProcessesService.java` |
| generado | `backend/src/main/java/com/umss/sigesa/application/service/process/GetProcessDetailService.java` |
| generado | `backend/src/main/java/com/umss/sigesa/application/service/process/ProcessAccessPolicy.java` |
| generado | `backend/src/main/java/com/umss/sigesa/application/service/process/ProcessEnrichmentHelper.java` |
| generado | `backend/src/main/java/com/umss/sigesa/adapter/out/persistance/ProcessQueryJpaAdapter.java` |
| generado | `backend/src/main/java/com/umss/sigesa/adapter/in/web/dto/ProcessSummaryResponseDto.java` |
| generado | `backend/src/main/java/com/umss/sigesa/domain/exception/ProcessNotFoundException.java` |
| generado | `backend/src/test/java/com/umss/sigesa/application/service/process/ProcessAccessPolicyTest.java` |
| generado | `backend/src/test/java/com/umss/sigesa/application/service/process/ListProcessesServiceTest.java` |
| generado | `backend/src/test/java/com/umss/sigesa/application/service/process/GetProcessDetailServiceTest.java` |
| generado | `backend/src/test/java/com/umss/sigesa/adapter/in/web/ProcessControllerQueryTest.java` |

**Backend — modificados**

| Acción | Ruta |
| --- | --- |
| modificado | `backend/src/main/java/com/umss/sigesa/adapter/in/web/ProcessController.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/adapter/in/web/advice/ProcessExceptionHandler.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/adapter/in/web/dto/ProcessResponseDto.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/adapter/out/persistance/repository/SpringDataAccreditationProcessRepository.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/config/ProcessModuleConfig.java` |

**Frontend — generados (fuera de alcance PR-IMPL-019; misma sesión FSD-UC-019)**

| Acción | Ruta |
| --- | --- |
| generado | `frontend/src/features/processes/**` (10 archivos: pages, hooks, components) |
| generado | `frontend/src/api/model/processSummaryResponseDto.ts` |

**Frontend — modificados**

| Acción | Ruta |
| --- | --- |
| modificado | `frontend/src/App.tsx` (rutas `/procesos`, `/procesos/:processId`) |
| modificado | `frontend/src/components/layout/Sidebar.tsx` (desplegable Gestión procesos) |
| modificado | `frontend/src/api/endpoints/procesos-de-acreditación/procesos-de-acreditación.ts` (`useListProcesses`, `useGetProcess`) |
| modificado | `frontend/src/api/model/processResponseDto.ts`, `index.ts` |
| modificado | `frontend/orval.config.ts`, `frontend/vite.config.ts`, `frontend/src/lib/api/customFetch.ts` |
| generado | `frontend/src/vite-env.d.ts` |

**Documentación**

| Acción | Ruta |
| --- | --- |
| generado | `docs/design/DD-UC-019.md` |
| generado | `docs/product/uc/FSD-UC-019.md` |
| generado | `docs/prompts/impl/PR-IMPL-019.md` |
| modificado | `docs/product/api_contracts.md` |
| modificado | `docs/product/DTP.md`, `docs/product/FSD.md` |
| modificado | `docs/sprints/sprint_02/PROMPT_MAPPING.md` (este archivo) |

### Cambios realizados

1. **Backend hexagonal:** puerto read `ProcessQueryPort` + adaptador JPA; casos de uso `ListProcessesService` / `GetProcessDetailService` con `ProcessAccessPolicy` (JD/TD global; CC filtrado por `programScope`).
2. **REST:** `GET /api/v1/processes` (resumen) y `GET /api/v1/processes/{processId}` (detalle con fases/subfases ordenadas por `order`); OpenAPI annotations.
3. **RBAC:** [CC] acceso cross-carrera → **404** `PROCESS_NOT_FOUND` (no 403).
4. **Tests:** 15 tests verdes en subset PR-IMPL-019; WebMvc standalone (evita conflicto `entityManagerFactory`).
5. **Frontend FSD-UC-019:** feature `processes/` con listado, detalle, badges de estado, árbol fases/subfases; sidebar desplegable «Ver procesos» / «Nuevo proceso».
6. **Orval:** hooks GET añadidos manualmente hasta rebuild backend Docker exponga GET en OpenAPI.

### Validación ejecutada

- [x] `./mvnw test -Dtest=ProcessAccessPolicyTest,ListProcessesServiceTest,GetProcessDetailServiceTest,ProcessControllerQueryTest` — **OK** (15 tests)
- [x] `CreateProcessUseCaseImplTest` — regresión POST OK
- [ ] JaCoCo ≥ 90% servicios nuevos — no verificado en esta sesión
- [ ] `pnpm run lint` frontend — no ejecutado
- [ ] E2E manual Docker — pendiente (rebuild backend para OpenAPI GET)

### Resultado obtenido

Full-stack FSD-UC-019 operativo en código: backend GET con RBAC + UI `/procesos` integrada en sidebar. Cadena documental: `FSD-UC-019 → DD-UC-019 → PR-IMPL-019 → PM-003`. `@dtp-sync` aplicado (§A.1, §A.2 #12, §A.3, §B.2).

### Riesgos / observaciones

- **Alcance expandido:** PR-IMPL-019 declara *backend only*; frontend React se implementó en la misma sesión sin PR-IMPL separado. Considerar PR-IMPL frontend dedicado en sprint futuro.
- **Hooks Orval manuales:** regenerar con `pnpm run generate:api` tras `docker compose up -d --build backend`.
- **Rama mezclada:** el working tree incluye cambios UC-003 (catálogo carreras) y otros no listados arriba; este PM cubre exclusivamente FSD-UC-019 / PR-IMPL-019.

### Próximos pasos

- [ ] Rebuild backend Docker + `pnpm run generate:api`
- [ ] Verificación E2E con `jd@umss.edu.bo` y usuario CC seed
- [ ] JaCoCo ≥ 90% en servicios `process/*`
