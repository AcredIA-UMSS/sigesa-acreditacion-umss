---
id: PR-IMPL-028
feature_asociado: DD-AGENT-002
fsd_uc:
  - FSD-UC-002
  - PRD-REQ-028
design_doc: DD-AGENT-002
depende_de:
  - PR-IMPL-025
  - PR-IMPL-027
fecha: "2026-08-21"
version: "1.0"
estado: Implementado
autor: "Cursor Agent"
skill_origen: save-prompt-mapping
---

# Prompt Contract — Implementación `PR-IMPL-028`

> **Design doc fuente:** [`DD-AGENT-002`](../../design/assistant/DD-AGENT-002.md) §10 · **Base:** [`PR-IMPL-025`](PR-IMPL-025.md), [`PR-IMPL-027`](PR-IMPL-027.md).

---

## 1. Propósito y Objetivo

Replicar para el copiloto de usuarios (`agent=users`):

1. Modal de trazabilidad de acciones (dev-only, build-time flag).
2. Documentación de seguridad compartida (`AssistantChatInputValidator` global).
3. Componente modal compartido `AssistantCopilotActionDebugModal`.

---

## 2. Entregables

| Área | Artefactos |
|------|------------|
| Frontend | `usersCopilotDebug.ts`, `UsersCopilotActionDebugModal`, `useUsersCopilot`, `UsersCopilotPanel` |
| Shared | `AssistantCopilotActionDebugModal`, `copilotAgentAction.ts` |
| Infra | `VITE_USERS_COPILOT_DEBUG_ACTIONS` en docker-compose / Dockerfile |
| Docs | `DD-AGENT-002` §10, `PROMPT_MAPPING` PM-018 |

---

## 3. Criterios de aceptación

- [x] Modal usuarios activable vía `VITE_USERS_COPILOT_DEBUG_ACTIONS=true` (Docker o Vite).
- [x] Trazabilidad: tool, camino, fuentes, pasos por turno.
- [x] Scroll del chat contenido; modal con portal + lock de `<main>`.
- [x] Seguridad documentada (validador global ya en PR-IMPL-027).

---

## 4. Prompt literal del usuario

```text
ahora haz las mismas funciones implementadas, del modal y seguridad para el agente de usaurios
```
