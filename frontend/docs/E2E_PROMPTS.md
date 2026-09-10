# Prompts E2E — SIGESA (adaptado del lab)

> Manual operativo completo: **[MANUAL_E2E.md](./MANUAL_E2E.md)**  
> MCP Cursor: §2.1 · Planner: §2.4 · Generator: §2.5 · Ejemplo real: §7.1

**Camino principal:** agentes nativos de Playwright (`npx playwright init-agents --loop=vscode`).  
**No uses** `tools/agente_e2e.py` como flujo principal: lee `index.html` estático (`<div id="root">`) y no ve la UI React montada.

Contexto de referencia en el repo: `tools/PROMPTS_AGENTES.md`, `tools/tests-E2E/tradicional/chat.spec.ts` (lab SoporteIA).

## Instalación agentes (una vez)

```bash
cd frontend
npx playwright init-agents --loop=vscode   # o --help para tu IDE
```

Genera `.github/agents/playwright-test-*.agent.md` y el servidor MCP.

## Planner — una sección por vez

Pedí explícitamente **solo login**, **solo listado de procesos**, etc. No planifiques toda la app de una vez (~10k tokens, mezcla ruido).

Contexto a adjuntar:

- `frontend/tests/seed.spec.ts`
- App viva en `http://127.0.0.1:5173` (con backend en `:8080`)
- Credenciales dev: README § Credenciales de acceso

Salida: `frontend/specs/plan_<seccion>.md`

### Prompt Planner (SIGESA)

```
Actúa como planificador de pruebas E2E para SIGESA (React + Spring Boot).
La app corre en http://127.0.0.1:5173 (frontend) con API en :8080.
Explora SOLO la sección: <login | listado procesos | …>.
Usa tests/seed.spec.ts como semilla de navegación.
Escribe specs/plan_<seccion>.md: resumen breve, grupos ## 1., ## 2., casos ### 1.1 con
**Pasos:** (acciones en orden) y **Resultado esperado:** (verificable en pantalla).
Máximo 8 casos: camino feliz, error, borde.
NO verifiques redacción exacta del asistente IA ni mensajes del LLM; verifica que haya respuesta,
estado de UI, URLs y datos tabulares.
No escribas código TypeScript todavía.
```

## Generator — un caso por vez (~3k tokens)

Contexto:

- El plan (`specs/plan_*.md`) — solo el caso N.N
- `tests/seed.spec.ts`
- `tests/tradicional/login.spec.ts` (patrón a imitar)

Salida: `tests/agente/<caso>.spec.ts` → correr → `AUDITORIA_E2E.md`.

### Prompt Generator (SIGESA)

```
Actúa como generador de tests Playwright TypeScript para SIGESA.
Toma el caso <N.N> de specs/plan_<seccion>.md y escribe tests/agente/<nombre>.spec.ts
con UN solo test en test.describe(nombre del grupo).
Imita tests/tradicional/login.spec.ts.
Reglas: getByRole, getByLabel, getByText, getByTestId; sin waitForTimeout; expect(...).toBeVisible/
toHaveURL/toHaveCount/toContainText; comentario antes de cada acción; page.goto('/login') o la ruta
inicial del flujo; no depende de otros tests.
Login dev JD: jd@umss.edu.bo / JefeDemo2026!
No verifiques textos volátiles del asistente LLM.
Ejecutá: pnpm test:e2e tests/agente/<archivo>.spec.ts y pegá la salida.
```

## Script Python del lab (opcional, secundario)

`tools/agente_e2e.py` sirve para entender el pipeline LLM sin navegador. Para SIGESA habría que adaptarlo a leer componentes TSX o usar MCP con app viva; no copies el flujo del lab tal cual.
