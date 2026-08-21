---
id: PR-IMPL-027
feature_asociado: DD-AGENT-001
fsd_uc:
  - FSD-UC-022
  - PRD-REQ-028
design_doc: DD-AGENT-001
depende_de:
  - PR-IMPL-024
fecha: "2026-08-21"
version: "1.0"
estado: Implementado
autor: "Cursor Agent"
skill_origen: save-prompt-mapping
---

# Prompt Contract — Implementación `PR-IMPL-027`

> **Design doc fuente:** [`DD-AGENT-001`](../../design/assistant/DD-AGENT-001.md) §10 · **Base:** [`PR-IMPL-024`](PR-IMPL-024.md).

---

## 1. Propósito y Objetivo

Endurecer el copiloto de fases (`agent=phases`) con:

1. **Validaciones de seguridad** en el chat (SQL injection, XSS, límites de entrada).
2. **Modal de trazabilidad** de acciones del agente, visible solo en desarrollo mediante interruptor explícito.

---

## 2. Entregables

| Área | Artefactos |
|------|------------|
| Backend | `AssistantChatInputValidator`, `AssistantInvalidInputException`, handler `ASSISTANT_INVALID_INPUT` |
| Frontend | `phasesCopilotDebug.ts`, `PhasesCopilotActionDebugModal`, `usePhasesCopilot` (actionHistory) |
| Docs | `DD-AGENT-001` §10, `PROMPT_MAPPING` PM-017 |

---

## 3. Criterios de aceptación

- [x] Mensajes con patrones SQL/XSS rechazados con HTTP 400.
- [x] Modal de acciones solo si `VITE_PHASES_COPILOT_DEBUG_ACTIONS=true` (build-time; docker-compose o `.env`).
- [x] Modal se abre al enviar mensaje y lista tools/camino/fuentes.
- [x] Documentación y trazabilidad sprint 02.

---

## 4. Prompt literal del usuario

```text
Quiero que para el agente de fases que disenamos hagas lo siguiente:
- hacer validaciones de seguridad para el chat para evitar problemas como el sqlinjection y demas
- que mientras le pregunte alguna accion al chat genere un modal de todas las acciones que hace el agente, para ver que esta realizando, esto tiene que tener un interruptor en el codigo para poder desactivar ya que solo se debe visualizar en modo desarrollo mas no en produccion, documenta todo esto, e igual en el prompt_mapping sprint 2
```
