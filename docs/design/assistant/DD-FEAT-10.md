---
id: DD-FEAT-10
title: Botón flotante de asistencia general (dashboard)
modulo: MOD-ASSISTANT
design_parent: DD-SYS-002
status: In progress
github_issue: "https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss/issues/57"
pr_impl: PR-IMPL-045
ultima_actualizacion: "2026-10-19"
---

# DD-FEAT-10 — FAB global del asistente en el panel autenticado

## 1. Propósito

Dar acceso al copiloto **general** (`agent=general`, misma API que `/ayuda`) desde **cualquier página autenticada del panel**, sin navegar a `/ayuda` y sin ocupar el layout.

**No es un caso de uso de dominio nuevo.** Complementa [DD-AGENT-UI-SHELL](DD-AGENT-UI-SHELL.md) y [FSD-UC-024](../../product/uc/FSD-UC-024.md) / [DD-SYS-002](../DD-SYS-002.md). No existe `DD-UC-*` porque no hay UC de negocio adicional.

## 2. Relación con el shell de dominio

| Superficie | Componente | Posición |
|------------|------------|----------|
| Copilotos `phases` / `evidence` / `users` | `DomainCopilotFloatingChat` | FAB `bottom-6 right-6` `z-50` |
| Asistente general (este FEAT) | `FloatingAssistantButton` | FAB `bottom-24 right-6` `z-40` |
| Página `/ayuda` | `AssistantPage` | Sin cambios (sin FAB global) |

El FAB global se coloca **encima** del FAB de dominio para no solaparse. `/ayuda` no muestra el FAB (evitar duplicar el chat de página completa).

**Fuera de alcance:** migrar `/ayuda` al shell de dominio; iframe a `/ayuda` (incrustaría el `Sidebar`); copilotos de dominio; contratos API; dependencias npm.

## 3. UX

### 3.1 FAB (cerrado)

- Circular, `CircleHelp`, `aria-label="Abrir asistente"`.
- No reserva espacio en el grid (portal a `document.body`).
- Visible en todas las rutas envueltas por `ProtectedRoute` excepto `/ayuda`.
- No aparece en landing pública ni login.

### 3.2 Modal (abierto)

- `role="dialog"` `aria-modal="true"` `aria-labelledby` título «Asistente virtual».
- Overlay semitransparente; el diálogo no cubre el sidebar de forma permanente (se cierra y se sigue navegando).
- Cierre: botón visible «Cerrar», tecla **Escape**, clic en overlay.
- Bloqueo de scroll del `body` mientras está abierto.
- Foco inicial en el diálogo; al cerrar, foco de vuelta al FAB.
- Trampa de Tab dentro del diálogo.
- Contenido: `AssistantChatUI` compacto + `useAssistantChat` (Orval). **No** `AssistantPage` (incluye `Sidebar`).

## 4. Arquitectura

```
ProtectedRoute
  └── FloatingAssistantButton          # FAB + chrome a11y del modal
        └── (si abierto) useAssistantChat + AssistantChatUI compact
```

Presentación en `frontend/src/components/`. Lógica de chat reutiliza el hook existente (sin `fetch`/`axios`).

## 5. Validación

- [ ] FAB visible en dashboard, procesos, evidencias, admin (excepto `/ayuda`)
- [ ] ESC, botón Cerrar y overlay cierran; foco vuelve al FAB
- [ ] Envío de mensaje usa `POST /api/v1/assistant/chat` vía Orval
- [ ] `pnpm exec tsc -b` y OxLint sin advertencias
