---
id: PR-IMPL-033
feature_asociado: DD-SYS-002
modulo: MOD-ASSISTANT
fecha: "2026-08-21"
estado: Implementado
---

# PR-IMPL-033 — Encadenamiento multi-tool (Nivel 4)

## Objetivo

Cablear el **bucle multi-tool** en `SendChatMessageService`, exponer la **traza** (`steps[]`) en la API y reflejarla en **todos** los copilotos (general, phases, users, evidence) con escenarios demo Nivel 4.

## Alcance

### Backend

- Loop LLM en `SendChatMessageService` hasta `sigesa.assistant.max-tool-iterations` (default **5** vía `application.yaml`).
- Una tool por iteración; resultado JSON reenviado al LLM; respuesta final **siempre** formateada por `AssistantResponseFormatter`.
- Modelo `AssistantToolStep` + campo `steps` en `AssistantChatResult` / `SendChatMessageResponse`.
- Hints de encadenamiento en prompts de agentes `phases`, `users`, `evidence`.
- Escenario demo **#5** en `AssistantController` para los cuatro perfiles.

### Frontend

- `mapAssistantResponseMetadata()` + `recordToolTraceInAction()` compartidos.
- Componente `CopilotAssistantMetadata` (traza visible cuando `steps.length > 1`).
- Hooks: `usePhasesCopilot`, `useUsersCopilot`, `useEvidenceCopilot`, `useAssistantChat`.
- Paneles: `PhasesCopilotPanel`, `UsersCopilotPanel`, `EvidenceCopilotPanel`, `AssistantChatUI`.

### Tests

- `SendChatMessageServiceToolLoopTest.multiToolLoop_chainsTwoToolsAndReturnsTrace`
- `SendChatMessageServiceToolLoopTest.multiToolLoop_respectsMaxIterations`

## Contrato API ampliado

`POST /api/v1/assistant/chat` — respuesta:

```json
{
  "reply": "**Paso 1**\n…\n\n---\n\n**Paso 2**\n…",
  "toolId": "search_normative_docs",
  "sourceTables": ["phases", "normative_document"],
  "path": "LLM",
  "llmInvoked": true,
  "steps": [
    { "step": 1, "toolId": "list_process_structure", "sourceTables": ["phases"], "success": true },
    { "step": 2, "toolId": "search_normative_docs", "sourceTables": ["normative_document"], "success": true }
  ]
}
```

## Escenarios demo Nivel 4

| Agente | Pregunta demo |
|--------|---------------|
| general | Muestra la estructura de Ingeniería de Sistemas CEUB y busca normativa de Matriz de evidencias |
| phases | Muestra la estructura completa y busca normativa de la subfase Matriz de evidencias |
| users | Lista usuarios CC activos y muéstrame el detalle de cc@umss.edu.bo |
| evidence | Lista evidencias pendientes de revisión y busca normativa sobre matriz de evidencias CEUB |

## Niveles de madurez (referencia curso)

| Nivel | Descripción | Estado SIGESA |
|-------|-------------|---------------|
| 1 | FAQ / respuesta fija | KEYWORD router |
| 2 | LLM elige 1 tool; Java formatea | Implementado |
| 3 | RAG normativo (`search_normative_docs` + fallback) | PR-IMPL-032 |
| **4** | ≥2 tools encadenadas en un turno + traza visible | **PR-IMPL-033** |

## Archivos clave

| Capa | Ruta |
|------|------|
| Loop | `SendChatMessageService.java` |
| Modelo | `AssistantToolStep.java`, `AssistantChatResult.java` |
| API | `SendChatMessageResponse.java`, `AssistantToolStepResponse.java` |
| Config | `AssistantModuleConfig.java`, `application.yaml` |
| UI shared | `CopilotAssistantMetadata.tsx`, `mapAssistantResponseMetadata.ts` |
| Tests | `SendChatMessageServiceToolLoopTest.java` |

## Trazabilidad

| Artefacto | Referencia |
|-----------|------------|
| Design | [DD-SYS-002 §11.10](../../design/DD-SYS-002.md) |
| Catálogo | [TOOL-CATALOG.md §5](../../design/assistant/TOOL-CATALOG.md) |
| Agentes | [DD-AGENT-001](../../design/assistant/DD-AGENT-001.md), [002](../../design/assistant/DD-AGENT-002.md), [003](../../design/assistant/DD-AGENT-003.md) |
| DTP | [DTP.md §B.5](../../product/DTP.md) |
| Sprint | [PM-023](../../sprints/sprint_02/PROMPT_MAPPING.md) |
