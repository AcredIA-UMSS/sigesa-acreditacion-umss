---
id: DD-AGENT-UI-SHELL
title: Shell UI unificado — Copilotos de dominio (MOD-ASSISTANT)
modulo: MOD-ASSISTANT
design_parent: DD-SYS-002
status: Implemented
ultima_actualizacion: "2026-09-02"
pr_impl: N/A (refactor frontend; patrón PR-IMPL-033)
---

# DD-AGENT-UI-SHELL — Ventana flotante compartida para copilotos

## 1. Propósito

Unificar la **experiencia visual** de los agentes embebidos por dominio (`phases`, `evidence`, `users`) en un **único shell flotante** que no consume espacio del layout principal.

**Fuera de alcance:** el asistente general de ayuda en `/ayuda` mantiene su diseño propio (`AssistantChatUI` — página completa con panel de demos y capacidades).

## 2. Agentes y superficies

| `agent` | Badge UI | Pantallas | Wrapper |
|---------|----------|-----------|---------|
| `phases` | **Fases** | `/procesos/{id}`, `/procesos/{id}/estructura` | `PhasesCopilotPanel` |
| `evidence` | **Evidencias** | `/evidencias/cargar` | `EvidenceCopilotPanel` |
| `users` | **Usuarios** | `/admin/users` | `UsersCopilotPanel` |
| `general` | — | `/ayuda` | `AssistantChatUI` (**diseño legacy, no migrar**) |

## 3. Comportamiento UX

### 3.1 FAB (cerrado)

- Posición fija: **inferior derecha** (`bottom-6 right-6`), `z-50`.
- Icono `Bot`; badge con etiqueta de contexto (Fases / Evidencias / Usuarios).
- **No reserva espacio** en el grid de la página (render vía `createPortal` → `document.body`).

### 3.2 Panel (abierto)

- Dimensiones: `min(100vw - 2rem, 24rem)` × `min(32rem, 100vh - 5rem)`.
- Cabecera: badge de contexto, título, subtítulo opcional, acciones:
  - **Historial** — alterna vista de historial de conversaciones.
  - **Limpiar** — archiva la sesión actual y reinicia el chat.
  - **Minimizar** — cierra el panel (conserva mensajes en memoria del hook).
- Cuerpo: lista de mensajes, empty state con preguntas demo, indicador de carga.
- Pie: textarea + botón Enviar (Enter envía; Shift+Enter nueva línea).

### 3.3 Historial de conversaciones

Vista accesible desde el botón **Historial**:

1. **Conversación actual** — todos los mensajes de la sesión activa.
2. **Conversaciones anteriores** — sesiones archivadas al pulsar **Limpiar**.

Persistencia: `sessionStorage`, clave `sigesa-copilot-archive:{kind}:{contextKey}` (máx. 20 entradas).

| Agente | `contextKey` típico |
|--------|-------------------|
| `phases` | `processId` |
| `evidence` | `programId` o `global` |
| `users` | `admin-users` |

> El backend **no** persiste chats; el historial archivado es solo UX local del navegador.

### 3.4 Historial de acciones (tools)

Separado del historial de conversaciones. En el agente `evidence` se expone enlace **Historial de acciones** (traza de tools → `AssistantCopilotActionDebugModal`). En `phases`/`users` solo si `VITE_*_COPILOT_DEBUG_ACTIONS=true`.

## 4. Arquitectura frontend

```
frontend/src/features/assistant/
├── components/domain-copilot/
│   ├── DomainCopilotFloatingChat.tsx   # Shell flotante (portal)
│   ├── CopilotConversationHistoryPanel.tsx
│   └── CopilotMessageBubble.tsx
├── lib/
│   ├── domainCopilotPresentation.ts    # Labels por kind
│   └── useCopilotConversationArchive.ts
└── types/domainCopilotKind.ts
```

Los wrappers de dominio (`PhasesCopilotPanel`, etc.) instancian su hook (`usePhasesCopilot`, …) y pasan el estado a `DomainCopilotFloatingChat`.

### Contrato de props (`DomainCopilotFloatingState`)

| Campo | Uso |
|-------|-----|
| `messages`, `draft`, `setDraft`, `sendMessage`, `clearConversation` | Chat |
| `messagesContainerRef` | Auto-scroll |
| `sampleQuestions` | Empty state |
| `isAssistantEnabled`, `isStatusLoading`, `isStatusError`, `isSending`, `errorMessage` | Estados |

## 5. Layout de páginas (post-refactor)

Las vistas host **eliminan** la columna lateral `340px` (`xl:grid-cols-[…_340px]`). El contenido principal usa ancho completo; el copiloto flota encima.

| Vista | Cambio |
|-------|--------|
| `ProcessDetailView` | Sin sidebar copiloto |
| `ProcessStructureView` | Sin sidebar copiloto |
| `EvidenceUploadPage` | Sin sidebar copiloto |
| `UsersAdminPage` | Sin sidebar copiloto |

## 6. Trazabilidad

| Artefacto | Referencia |
|-----------|------------|
| Design agentes | [DD-AGENT-001](DD-AGENT-001.md), [DD-AGENT-002](DD-AGENT-002.md), [DD-AGENT-003](DD-AGENT-003.md) |
| Motor / API | [DD-SYS-002](../DD-SYS-002.md) §11 |
| Sprint audit | [PM-007](../../sprints/sprint_03/PROMPT_MAPPING.md) |
| DTP | [DTP.md](../../product/DTP.md) §B.5 |

## 7. Validación

- [x] `pnpm exec tsc -b`
- [x] `pnpm lint` (oxlint)
- [ ] Smoke manual: FAB visible en fases/evidencias/usuarios; historial archiva al limpiar; `/ayuda` sin cambios
