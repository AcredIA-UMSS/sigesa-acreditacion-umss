# PROMPT_MAPPING — Sprint 03

> Registro PM del sprint 03. Trazabilidad: `Código → PR-IMPL → DD-UC-NNN → FSD-UC-NNN → DTP`.  
> **Índice:** PM-001…PM-014 (evidencia/subfases v1 → v2 normativo → M6 workflow → UX/docs → E2E Buscador Evidencias IA).

| ID Mapeo | PR-IMPL | Design Doc | FSD / PRD | Descripción de la Tarea |
| :--- | :--- | :--- | :--- | :--- |
| PM-001 | PR-IMPL-034 | DD-UC-022 | FSD-UC-022 / FSD-UC-004 / FSD-UC-021 | Subfases con `requirements`, evidencias múltiples por subfase, observaciones TD/JD, UI + docs |
| PM-002 | PR-IMPL-035 | DD-UC-005 | FSD-UC-005 | Historial de versiones + bloqueo DELETE append-only (API-EVD-03/04) |
| PM-003 | PR-IMPL-036 | DD-UC-006 | FSD-UC-006 | Subsanación por subfase, una vez por observación OPEN, historial liviano |
| PM-004 | PR-IMPL-037 | DD-UC-007 | FSD-UC-007 | Buscador de evidencias en vista fases/subfases del proceso |
| PM-005 | PR-IMPL-038 | DD-UC-008 / DD-UC-009 | FSD-UC-008 / FSD-UC-009 | Rechazo y aprobación de indicadores vía subfase (TD; requiere evidencia + indicatorId) |
| PM-006 | PR-IMPL-039 | DD-UC-010 | FSD-UC-010 | Cierre de fase TD cuando todas las subfases APROBADO (API-WF-03) |
| PM-007 | N/A | DD-AGENT-UI-SHELL | MOD-ASSISTANT (FSD-UC-024 / agentes 001–003) | Shell flotante unificado copilotos fases/evidencias/usuarios + historial conversaciones |
| PM-008 | PR-IMPL-021 | DD-UC-021 | FSD-UC-021 | Full-Stack v2 plantillas: API-TPL-08, UI tabs, publish BR-24, duplicate v2 |
| PM-009 | PR-IMPL-003 | DD-UC-003 | FSD-UC-003 | Clonado árbol v2 al crear proceso (`ProcessNormativeTreeCloner`) |
| PM-010 | PR-IMPL-M6-025 | DD-UC-025 | FSD-UC-025…028 | M6 workflow metodológico (V17, API-WF-04…08, timeline UI) |
| PM-011 | N/A | DD-UC-019 / DD-UC-022 | FSD-UC-019 / FSD-UC-022 | Árbol normativo en capas desplegables N1→Indicador (UI) |
| PM-012 | N/A | DD-UC-001 | FSD-UC-001 | Fix login en bucle post-M6 (operational_mode, redirect, 401 global) |
| PM-013 | N/A | DD-UC-025 | FSD / DTP / ADR-0005 | Sincronización documental v2.0/v2.1 + README |
| PM-014 | PR-IMPL-037 | DD-UC-007 | FSD-UC-007 | Buscador de evidencias E2E Playwright + Modo IA MCP (header `X-AI-Enabled`, toggle UI, preset escenarios demo 1-3) |

---

## PM-001

| Campo | Valor |
| --- | --- |
| **ID** | PM-001 |
| **Fecha** | 2026-08-27 |
| **Solicitante** | Boris Anthony Angulo Urquieta |
| **Agente/Entorno** | Cursor IDE — Agent |
| **Tarea** | Extensión subfases: requisitos, evidencias múltiples y observaciones |
| **Objetivo** | Cada subfase expone nombre, descripción y requisitos de completitud; [CC] sube 1..N evidencias; [TD]/[JD] registran observaciones |
| **Contexto** | Evolución de PM-016/PM-019 (carga por subfase sin FK). Flyway V9. API-SUB-01. |
| **PR-IMPL vinculado** | [PR-IMPL-034](../../prompts/impl/PR-IMPL-034.md) |
| **DD vinculado** | [DD-UC-022](../../design/DD-UC-022.md) |
| **FSD / PRD vinculado** | FSD-UC-022 · FSD-UC-004 · FSD-UC-021 |
| **Estado** | completado |

### Prompt usado exacto

```text
quiero que modifiques las subfases,
que tengan los siguientes datos:
nombre_subfase
descripcion_subfase
requisitos_subfase (hara referencia a los requisitos que debe cumplir para que se considere hecha)

ademas tambien que ajustes ciertas cosas si son necesarias como las evidencias, a una subfase se pueden subir 1 o mas evidencias,
tambien que cada subfase tenga un espacio de observacion, donde el tecnico o administrador pueda dar observaciones a la evidencia subida, realiza todo eso y tambien los cambios respectivos en el frontend

actualiza toda la documentacion necesaria y registralo en el prompt_mapping como sprint 3
```

---

## PM-013

| Campo | Valor |
| --- | --- |
| **ID** | PM-013 |
| **Fecha** | 2026-09-09 |
| **Hora** | 15:20 |
| **Solicitante** | Boris Anthony Angulo Urquieta |
| **Agente/Entorno** | Cursor IDE — Agent |
| **Modelo** | Composer |
| **Tarea** | Sincronización documental v2.0/v2.1 + README |
| **Objetivo** | Cerrar deuda FSD/DTP/ADR; crear FSD-UC-025 y PR-IMPL-M6-025; reconciliar índice Reespecificado→Implementado v2; actualizar README releases |
| **Contexto** | Post-implementación M6; pregunta usuario sobre estado «Reespecificado» vs «Implementado» |
| **PR-IMPL vinculado** | [PR-IMPL-M6-025](../../prompts/impl/PR-IMPL-M6-025.md) |
| **DD-UC vinculado** | [DD-UC-025](../../design/DD-UC-025.md) |
| **FSD-UC vinculado** | FSD-UC-025…028 · FSD-UC-004…010 · FSD-UC-019 |
| **Estado** | completado |

---

## PM-014

| Campo | Valor |
| --- | --- |
| **ID** | PM-014 |
| **Fecha** | 2026-09-11 |
| **Solicitante** | Tech Lead / User |
| **Agente/Entorno** | Antigravity AI — Agent |
| **Tarea** | FSD-UC-007 — Búsqueda de Evidencias E2E Playwright + Modo IA MCP (`X-AI-Enabled`) |
| **Objetivo** | Soportar búsqueda estándar sin IA y búsqueda asistida por IA (con expansión de sinónimos MCP / header `X-AI-Enabled`), conmutador en UI y botones preset de escenario demo (Escenarios 1, 2, 3), verificados 100% mediante suite Playwright E2E a través de componentes UI. |
| **PR-IMPL vinculado** | [PR-IMPL-037](../../prompts/impl/PR-IMPL-037.md) |
| **DD vinculado** | [DD-UC-007](../../design/DD-UC-007.md) |
| **FSD vinculado** | [FSD-UC-007](../../product/uc/FSD-UC-007.md) |
| **Estado** | completado |

### Prompt usado exacto

```text
you just change the way or the full feature to search evidences using AI mode to check similars, etc. pls review it and fix it with the actual UC available for this,take in mind all the scenarios described (without ai, using a toogle to select when to use or not use ai, and finally using synominc (call to function)), etc. pls provide me a good answer and then pls put it available for the users, and if reuqired pls fix the answer, tka ein mind since we are doing a E2E test for this the test cases must use the frontend, buttons and components to call to this feature
```

### Archivos generados o modificados

| Acción | Ruta |
| --- | --- |
| modificado | `frontend/playwright.config.ts` |
| modificado | `frontend/src/features/evidence/api/fetchEvidenceSearch.ts` |
| modificado | `frontend/src/features/evidence/hooks/useEvidenceSearch.ts` |
| modificado | `frontend/src/features/evidence/components/ProcessEvidenceSearchPanel.tsx` |
| modificado | `frontend/src/App.tsx` |
| generado | `frontend/tests/search_evidence/search_evidence.spec.ts` |
| generado | `frontend/src/features/evidence/EvidenceSearchPage.tsx` |

### Cambios realizados

1. **Configuración Playwright:** Configurados reporteros `list` y `html` (`open: 'never'`) y modo visual headed con retardo `slowMo`.
2. **API & Estado Frontend (`fetchEvidenceSearch.ts`, `useEvidenceSearch.ts`):** Normalización de respuestas API (formatos `subsets` / `items`) para prevenir pantalla blanca. Cabecera HTTP `X-AI-Enabled: true` enviada cuando el modo IA está activo.
3. **Panel de Búsqueda UI (`ProcessEvidenceSearchPanel.tsx`):** Checkbox `#evidence-search-ai-toggle`, badge "Modo IA MCP Activo", botones interactivos demo (Escenarios 1, 2, 3) y soporte híbrido para árbol normativo v2 y fases legacy.
4. **Suite E2E Playwright (`frontend/tests/search_evidence/search_evidence.spec.ts`):** 14 casos de prueba E2E interactuando en la ruta de producción `/evidencias/buscar` (y alias `/evidencias/search`).

### Validación ejecutada

- [x] `npx playwright test --reporter=list` — 14/14 tests PASADOS (100% éxito)
- [x] `pnpm exec tsc -b` — 0 errores de compilación
- [x] `docker compose build frontend && docker compose up -d frontend` — Servido en puerto 3000

### Resultado obtenido

Buscador de evidencias integrado en la rama `release/2.0.0`, con suite E2E en la ubicación estándar `frontend/tests/search_evidence/search_evidence.spec.ts`.
