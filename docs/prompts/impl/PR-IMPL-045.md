---
id: PR-IMPL-045
feature_asociado: DD-FEAT-10
fsd_uc:
  - FSD-UC-024
fecha: "2026-10-19"
version: "1.0"
estado: Aprobado
autor: "Boris Anthony Angulo Urquieta"
github_issue: "https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss/issues/57"
release: "2.0.0"
---

# Prompt Contract — Implementación `PR-IMPL-045`

> **Issue:** [#57 FEAT-10](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss/issues/57) — Botón flotante de asistencia (copiloto).  
> **Design:** [`DD-FEAT-10`](../../design/assistant/DD-FEAT-10.md)  
> **FSD:** [`FSD-UC-024`](../../product/uc/FSD-UC-024.md) (asistente general; no nuevo UC)  
> **Rama:** `feat/57-floating-assistant` desde `origin/release/2.0.0`

---

## 1. Propósito

FAB inferior derecha en el panel autenticado que abre un modal accesible con el copiloto general funcional (`useAssistantChat` + `AssistantChatUI`).

## 2. In-Scope

- `frontend/src/components/FloatingAssistantButton.tsx` (+ test).
- Montaje en `ProtectedRoute` (todas las páginas del panel).
- Modal a11y: `aria-modal`, foco, ESC, cierre visible, scroll lock, retorno de foco.
- Variant compacto de `AssistantChatUI` para caber en el diálogo.
- Docs vivos: DD-FEAT-10, DTP, FSD changelog, DD-AGENT-UI-SHELL nota, PM sprint 03.

## 3. Out-of-Scope

- Iframe a `/ayuda`, cambios de API, npm, `docs/baseline/`, landing #59, Quick Actions #58, copilotos de dominio.

## 4. Reglas

| ID | Regla |
|----|-------|
| R1 | Solo hooks Orval; sin `fetch`/`axios`. |
| R2 | No incrustar `AssistantPage` (traería `Sidebar` anidado). |
| R3 | Tokens Tailwind institucionales; sin hex arbitrario. |
| R4 | TypeScript estricto; sin `any`. FAB no en `/ayuda`. |
| R5 | Offset `bottom-24` para no tapar el FAB de dominio. |
| R6 | Cierre: `@save-prompt-mapping sprint=03 pr=PR-IMPL-045`. |
