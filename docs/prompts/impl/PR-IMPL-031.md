---
id: PR-IMPL-031
feature_asociado: DD-SYS-002
modulo: MOD-ASSISTANT
fecha: "2026-08-21"
estado: Implementado
---

# PR-IMPL-031 — RBAC reforzado y auditoría de tools

## Objetivo

Endurecer RBAC en copilotos `phases` / `users` / `evidence` y registrar trazabilidad de invocaciones de tools.

## In-Scope

- `AssistantToolRbacGuard` — revalidación rol + subset agente en executor.
- `AssistantToolAuditPort` + `Slf4jAssistantToolAuditAdapter` — log `AUDIT_ASSISTANT_TOOL`.
- `AssistantToolRegistry.isToolAllowedForAgent`.
- `SendChatMessageService` pasa `agentProfile` al executor.
- Tests: `AssistantToolRbacGuardTest`, ampliación `AssistantToolRegistryTest`.
- Docs: `TOOL-CATALOG` §1.2.1, `DD-AGENT-001/002/003`, `DTP.md` §B.5.

## Out-of-Scope

- Persistencia en tabla `audit_log` (solo SLF4J en MVP).
- Rate limiting por rol.
