---
id: PR-IMPL-030
feature_asociado: DD-AGENT-003
fsd_uc:
  - FSD-UC-024
  - FSD-UC-004
design_doc: DD-AGENT-003
depende_de:
  - PR-IMPL-026
  - PR-IMPL-027
  - PR-IMPL-028
fecha: "2026-08-21"
version: "1.0"
estado: Implementado
autor: "Cursor Agent"
skill_origen: save-prompt-mapping
---

# Prompt Contract — Implementación `PR-IMPL-030`

> **Design doc:** [`DD-AGENT-003`](../../design/assistant/DD-AGENT-003.md) §10.

---

## 1. Objetivo

Alinear el copiloto de evidencias (`agent=evidence`) con fases/usuarios: UI unificada, modal dev de trazabilidad, seguridad chat documentada.

---

## 2. Entregables

| Área | Artefactos |
|------|------------|
| Frontend | `evidenceCopilotDebug.ts`, `EvidenceCopilotActionDebugModal`, `useEvidenceCopilot`, `EvidenceCopilotPanel` |
| Infra | `VITE_EVIDENCE_COPILOT_DEBUG_ACTIONS` en docker-compose / Dockerfile |
| Docs | `DD-AGENT-003` §10, `FSD-UC-024`, `FSD-UC-004`, PM-020 |

---

## 3. Prompt literal del usuario

```text
ahora modifica el agente de cargar evidencia, que la interfaz sea similar a los agentes actuales implementados, agrega lo del modal y seguridad
```
