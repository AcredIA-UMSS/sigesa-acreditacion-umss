---
id: PR-IMPL-024
feature_asociado: DD-AGENT-001
fsd_uc:
  - FSD-UC-022
  - PRD-REQ-028
design_doc: DD-AGENT-001
depende_de:
  - PR-IMPL-013
  - PR-IMPL-022
fecha: "2026-08-11"
version: "1.0"
estado: Implementado
autor: "Cursor Agent"
skill_origen: save-prompt-mapping
---

# Prompt Contract — Implementación `PR-IMPL-024`

> **Design doc fuente:** [`DD-AGENT-001`](../../design/assistant/DD-AGENT-001.md) · **Tool calling base:** [`PR-IMPL-013`](PR-IMPL-013.md) · **Estructura proceso:** [`FSD-UC-022`](../../product/uc/FSD-UC-022.md) · **Catálogo:** [`TOOL-CATALOG.md`](../../design/assistant/TOOL-CATALOG.md).

---

## 1. Propósito y Objetivo

Implementar el **agente copiloto de fases** (`agent=phases`) embebido en pantallas de proceso:

- Backend: `AssistantChatContext`, tools `list_process_structure` / `manage_process_subphase`, RBAC CC solo lectura.
- Frontend: `PhasesCopilotPanel` + `usePhasesCopilot` en detalle y estructura (layout responsive).
- Resolución tolerante de fases/subfases (`AssistantStructureLookup`) y preview de orden al crear subfases (`SubphaseOrderPlan`).
- Documentación `DD-AGENT-001` + backlog de evolución.

---

## 2. Entregables

| Área | Artefactos |
|------|------------|
| API | `POST /assistant/chat` con `context.agent=phases`; `GET /assistant/status?agent=phases` |
| Tools | `list_process_structure`, `manage_process_subphase`; CC: 2 read-only |
| UI | `PhasesCopilotPanel`, integración `ProcessDetailView` / `ProcessStructureView` |
| Docs | `DD-AGENT-001.md`, `TOOL-CATALOG.md` §RBAC phases |

---

## 3. Post-implementación

- `@save-prompt-mapping sprint=2 pr=PR-IMPL-024` → PM-011
- Sugerido: `@dtp-sync` (MOD-ASSISTANT agente phases)

---

## 4. PR

- **PR #28** — `feat/agenteFases` → `main` (merge `60cb091`)
