# MCP SIGESA — Control documental (`agent=evidence`)

Servidor [Model Context Protocol](https://modelcontextprotocol.io) que expone las tools de **FSD-UC-024 / DD-AGENT-003** contra la API del asistente SIGESA.

## Tools

| MCP tool | Equivalente asistente |
|----------|------------------------|
| `list_pending_evidences` | `list_pending_evidences` |
| `get_evidence_detail` | `get_evidence_detail` |
| `check_evidence_completeness` | `check_evidence_completeness` |

Cada tool invoca `POST /api/v1/assistant/chat` con `context.agent=evidence` y un mensaje que dispara el keyword router / tool calling del backend (mismas reglas PBAC).

## Variables de entorno

| Variable | Requerida | Descripción |
|----------|-----------|-------------|
| `SIGESA_API_URL` | sí | Base URL del backend (ej. `http://localhost:8080`) |
| `SIGESA_JWT` | sí | Bearer JWT de un usuario JD, TD o CC |
| `SIGESA_PROGRAM_ID` | no | UUID de carrera a incluir en el contexto |

## Uso local

```bash
cd mcp/sigesa-evidence
pnpm install   # o npm install
export SIGESA_API_URL=http://localhost:8080
export SIGESA_JWT=eyJ...
pnpm start
```

## Cursor (`mcp.json`)

```json
{
  "mcpServers": {
    "sigesa-evidence": {
      "command": "node",
      "args": ["/ruta/absoluta/sigesa-acreditacion-umss/mcp/sigesa-evidence/src/index.js"],
      "env": {
        "SIGESA_API_URL": "http://localhost:8080",
        "SIGESA_JWT": "<token-jd-td-cc>"
      }
    }
  }
}
```

## Notas

- No bypasea auth: sin JWT válido el backend responde 401/403.
- EE recibe 403 en `agent=evidence`.
- Fase 1: solo lectura (sin aprobar/rechazar).
