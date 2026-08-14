---
id: PR-IMPL-026
feature_asociado: DD-AGENT-003
fsd_uc:
  - FSD-UC-024
  - PRD-REQ-028
design_doc: DD-AGENT-003
depende_de:
  - PR-IMPL-006
  - PR-IMPL-013
  - PR-IMPL-024
fecha: "2026-08-12"
version: "1.0"
estado: Implementado
autor: "Cursor Agent"
skill_origen: sigesa-orchestrator
---

# Prompt Contract — Implementación `PR-IMPL-026`

> **Design doc fuente:** [`DD-AGENT-003`](../../design/assistant/DD-AGENT-003.md) · **FSD:** [`FSD-UC-024`](../../product/uc/FSD-UC-024.md) · **Carga:** [`PR-IMPL-006`](PR-IMPL-006.md) · **Patrón agente:** [`PR-IMPL-024`](PR-IMPL-024.md) / [`PR-IMPL-025`](PR-IMPL-025.md) · **Catálogo:** [`TOOL-CATALOG.md`](../../design/assistant/TOOL-CATALOG.md).

---

## 1. Propósito y Objetivo

Implementar el **agente copiloto de control documental** (`agent=evidence`) + **servidor MCP** espejo:

- Backend: perfil `EVIDENCE`, tools de lectura, PBAC CC/TD/JD, 403 para EE.
- Casos de uso de consulta de evidencias pendientes / detalle / completitud.
- Frontend: `EvidenceCopilotPanel` + `useEvidenceCopilot` en `/evidencias/cargar`.
- MCP: `mcp/sigesa-evidence` con las 3 tools vía HTTP+JWT.
- Docs: FSD-UC-024, DD-AGENT-003, TOOL-CATALOG, DTP si aplica.

---

## 2. Rol y Persona

Desarrollador Full-Stack SIGESA (hexagonal Spring Boot 4 + React 19 Orval), replicando el patrón de PR-IMPL-024/025 sin mutar UC-008/009.

---

## 3. Límites de Alcance

### In-Scope

- `AssistantAgentProfile.EVIDENCE` + contexto opcional `programId`.
- Tools: `list_pending_evidences`, `get_evidence_detail`, `check_evidence_completeness`.
- 403 en chat/status si rol ∉ {JD, TD, CC}.
- Puerto `EvidenceControlQueryPort` + adapter JPA (sin DELETE).
- UI embebida en página de carga de evidencias.
- Servidor MCP Node/TS en `mcp/sigesa-evidence`.
- Actualización TOOL-CATALOG + FSD.md + PM-013.

### Out-of-Scope

- Aprobar/rechazar vía chat (UC-008/009).
- OCR / LLM sobre contenido del archivo.
- Sustituir UI de upload.
- Modificar `docs/baseline/`.

---

## 4. Restricciones y Reglas

| ID | Regla |
|----|-------|
| R1 | Solo lectura en MVP; sin cambios de estado de indicador. |
| R2 | [CC] acotado a `programScope` del JWT (FSD-BR-09). |
| R3 | Tools solo vía use cases; sin acceso JPA desde executor. |
| R4 | Frontend solo hooks Orval / `customFetch`. |
| R5 | Tokens Tailwind institucionales. |
| R6 | MCP no bypasea auth: requiere `SIGESA_JWT`. |

---

## 5. Entregables

1. Backend profile + tools + query port/adapter + tests unitarios mínimos.
2. Frontend panel + hook + mount.
3. `mcp/sigesa-evidence` con README.
4. Docs FSD/DD/PR-IMPL/TOOL-CATALOG/PROMPT_MAPPING (PM-013).

---

## 6. Criterios de aceptación

- [ ] `GET /assistant/status?agent=evidence` 200 para JD/TD/CC; 403 EE.
- [ ] Keyword «evidencias pendientes» invoca `list_pending_evidences`.
- [ ] [CC] no lista indicadores fuera de su carrera.
- [ ] Panel visible en `/evidencias/cargar` para CC/TD/JD.
- [ ] MCP tools listables con `npx @modelcontextprotocol/inspector` (o equivalente).
