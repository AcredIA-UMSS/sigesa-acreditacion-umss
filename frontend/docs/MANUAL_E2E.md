# Manual E2E — SIGESA Frontend

Guía operativa para pruebas end-to-end con **Playwright** en el monorepo SIGESA.

| Capa | Herramienta | Ubicación |
|------|-------------|-----------|
| API / dominio | JUnit, Mockito | `backend/` |
| UI / flujos usuario | Playwright + TypeScript | `frontend/tests/` |

El **generador de tests** vive en el frontend React. El backend solo debe estar accesible en `:8080`.

### Índice

1. [Estructura del repo](#1-estructura-del-repo)
2. [Instalación](#2-instalación-una-vez) — incl. [MCP en Cursor](#21-cursor--revisar-y-habilitar-el-mcp-playwright), [Planner §2.4](#24-cursor--invocar-el-planner-paso-4), [Generator §2.5](#25-cursor--invocar-el-generator-paso-5)
3. [Entorno de prueba](#3-levantar-el-entorno-de-prueba)
4. [Comandos](#4-comandos-día-a-día)
5. [Tests tradicionales](#5-tests-tradicionales-a-mano)
6. [UI testeable](#6-ui-testeable-antes-de-generar-en-masa)
7. [Flujo Planner → Generator](#7-flujo-con-agentes-nativos-planner--generator) — incl. [§7.1 Ejemplo real login](#71-ejemplo-real-sigesa-plan_loginmd)
8. [Healer](#8-healer-tests-rotos)
9. [Solución de problemas](#9-solución-de-problemas)
10. [Checklist](#10-checklist-rápido-nueva-sección-de-la-app)
11. [Referencias](#11-referencias)

### Pipeline en 30 segundos

```text
pnpm dev + backend Docker
    → seed.spec.ts + login.spec.ts (verde)
    → Planner (MCP) → specs/plan_login.md
    → revisión humana del plan
    → Generator (MCP, 1 caso) → tests/agente/*.spec.ts
    → pnpm test:e2e (verde)
    → AUDITORIA_E2E.md (5 preguntas)
```

---

## 1. Estructura del repo

```
frontend/
├── playwright.config.ts       # baseURL, webServer, Chromium
├── AUDITORIA_E2E.md           # 5 preguntas antes de aceptar un spec generado
├── docs/
│   ├── MANUAL_E2E.md          # ← este documento
│   └── E2E_PROMPTS.md         # prompts listos para Planner / Generator
├── .github/agents/            # agentes nativos Playwright (Cursor / VS Code)
│   ├── playwright-test-planner.agent.md
│   ├── playwright-test-generator.agent.md
│   └── playwright-test-healer.agent.md
├── specs/                     # planes .md del Planner (uno por sección)
├── tests/
│   ├── seed.spec.ts           # semilla: cómo abrir la app
│   ├── tradicional/           # tests escritos a mano (patrón de referencia)
│   └── agente/                # tests generados por el agente
└── package.json               # scripts test:e2e*
```

**No usar como camino principal:** `tools/agente_e2e.py` (lab SoporteIA). Lee `index.html` estático (`<div id="root">` vacío) y no ve la UI React montada.

---

## 2. Instalación (una vez)

```bash
cd frontend
pnpm install
pnpm exec playwright install chromium
pnpm exec playwright init-agents --loop=vscode   # Cursor / VS Code
```

Tras `init-agents`, recargá Cursor (*Developer: Reload Window*) y verificá que el MCP **playwright-test** esté activo (ver §2.1).

En CachyOS/Arch puede aparecer:

```text
BEWARE: your OS is not officially supported by Playwright...
```

Es normal; Playwright usa binarios fallback de Ubuntu. Si el navegador falla al ejecutar:

```bash
export CHROMIUM_PATH=/usr/bin/chromium   # o google-chrome-stable
```

### 2.1 Cursor — revisar y habilitar el MCP Playwright

`init-agents` crea `frontend/.vscode/mcp.json`, pero **Cursor lee la config MCP del workspace** en:

```text
.cursor/mcp.json    ← raíz del monorepo (ya incluido en este repo)
```

#### Dónde verlo en la UI

1. Abrí **Cursor Settings** (engranaje abajo a la izquierda, o `Ctrl+Shift+J`).
2. Entrá a **MCP** (o buscá “MCP” en el buscador de settings).
3. Deberías ver el servidor **`playwright-test`**:
   - **Verde / Enabled** → conectado; el agente puede abrir el navegador.
   - **Amarillo / Loading** → arrancando; esperá unos segundos.
   - **Rojo / Error** → falló el comando; abrí el detalle del error.
   - **No aparece** → recargá ventana (*Developer: Reload Window*) o revisá que exista `.cursor/mcp.json`.

#### Qué tools deberían listarse (si está OK)

Al expandir `playwright-test`, tools como:

- `browser_navigate`, `browser_snapshot`, `browser_click`
- `planner_setup_page`, `planner_save_plan`
- `generator_setup_page`, `generator_write_test`

Si no ves ninguna tool, el MCP no está corriendo.

#### Probar conexión manual (terminal)

Desde la raíz del monorepo:

```bash
cd frontend && pnpm exec playwright run-test-mcp-server
```

Debe quedar esperando stdin (stdio). `Ctrl+C` para salir. Si falla aquí, Cursor tampoco lo levantará.

#### Contenido de `.cursor/mcp.json` (raíz monorepo)

El repo ya incluye esta config para que Cursor encuentre Playwright instalado en `frontend/`:

```json
{
  "mcpServers": {
    "playwright-test": {
      "command": "pnpm",
      "args": ["--dir", "frontend", "exec", "playwright", "run-test-mcp-server"]
    }
  }
}
```

Si el MCP falla con “command not found”, verificá que `pnpm` esté en el PATH de Cursor (abrir Cursor desde terminal a veces ayuda).

#### Errores MCP frecuentes

| Síntoma | Causa | Qué hacer |
|---------|-------|-----------|
| No aparece `playwright-test` | Cursor no recargó config | *Developer: Reload Window* |
| Rojo al iniciar | Playwright no instalado en `frontend/` | `cd frontend && pnpm install && pnpm exec playwright install chromium` |
| Agente responde solo texto, sin `browser_*` | MCP desconectado | Revisar pestaña MCP; probar § terminal arriba |
| Tools aparecen pero navegador no abre | App no levantada | `pnpm dev` en `:5173` |
| Nombre distinto en UI (`project-0-…-playwright-test`) | Normal en Cursor | Es el mismo servidor, namespace del workspace |

### 2.3 Qué genera `init-agents`

| Archivo | Para qué |
|---------|----------|
| `frontend/.github/agents/playwright-test-planner.agent.md` | Instrucciones del Planner |
| `frontend/.github/agents/playwright-test-generator.agent.md` | Instrucciones del Generator |
| `frontend/.github/agents/playwright-test-healer.agent.md` | Reparar tests rotos |
| `frontend/.vscode/mcp.json` | MCP para VS Code (referencia) |
| `.cursor/mcp.json` | MCP para Cursor (**usar este en el monorepo**) |

Los `.agent.md` definen **comportamiento**; el **MCP** (`run-test-mcp-server`) es lo que abre el navegador y expone `browser_*`, `planner_*`, `generator_*`.

### 2.4 Cursor — invocar el Planner (paso 4)

Los archivos en `frontend/.github/agents/` están pensados para **VS Code + Copilot**. En **Cursor** el flujo equivalente es:

#### Checklist antes de planificar

- [ ] `docker compose up -d backend` (API `:8080`)
- [ ] `cd frontend && pnpm dev` (UI `:5173`)
- [ ] MCP **playwright-test** verde (§2.1)
- [ ] Modo **Agent** (no Ask / no Edit-only)

#### Prompt listo para pegar (Planner — solo login)

Referenciá el agente con `@` si Cursor lo permite, o pegá el bloque completo:

```text
@frontend/.github/agents/playwright-test-planner.agent.md

Actuá como playwright-test-planner según ese agente.
Explora SOLO login en http://127.0.0.1:5173/login.
Contexto: frontend/tests/seed.spec.ts y frontend/tests/tradicional/login.spec.ts.
Guardá el plan en frontend/specs/plan_login.md.
JD: jd@umss.edu.bo / JefeDemo2026!
Máximo 8 casos. No verifiques redacción del asistente LLM.
```

#### Cómo saber que funcionó

1. En el chat aparecen **tool calls** (`planner_setup_page`, `browser_snapshot`, `planner_save_plan`).
2. Se crea **`frontend/specs/plan_login.md`** con casos `### 1.1`, `### 1.2`, …
3. El plan lista **pasos** y **expectativas verificables** (URL, headings, `role="alert"`, etc.).

Si el agente **solo escribe markdown en el chat** sin tools → MCP no activo (§2.1).

#### Revisar el plan antes del Generator

Preguntate:

- ¿Cada expectativa se ve en pantalla sin depender del LLM?
- ¿Hay casos duplicados de `tests/tradicional/`? (está bien; sirven de referencia cruzada)
- ¿Algún paso pide CSS o `#id`? → corregir el plan

### 2.5 Cursor — invocar el Generator (paso 5)

Un **solo caso** por invocación. Prompt de ejemplo (caso 1.4):

```text
@frontend/.github/agents/playwright-test-generator.agent.md

Generá el caso 1.4 de frontend/specs/plan_login.md.
Patrón: frontend/tests/tradicional/login.spec.ts
Salida: frontend/tests/agente/login-email-no-institucional.spec.ts
Seed: frontend/tests/seed.spec.ts
```

El Generator debería:

1. `generator_setup_page` con el plan del caso
2. Ejecutar pasos en vivo (`browser_type`, `browser_click`, …)
3. Escribir `tests/agente/<archivo>.spec.ts`

Luego corrés:

```bash
cd frontend
PW_SKIP_BACKEND=1 pnpm test:e2e tests/agente/login-email-no-institucional.spec.ts
```

Y auditás con `AUDITORIA_E2E.md`.

---

## 3. Levantar el entorno de prueba

### Opción recomendada — Docker + Vite

```bash
# Terminal 1 — raíz del monorepo
docker compose up -d backend          # API en :8080

# Terminal 2
cd frontend && pnpm dev               # UI en :5173, proxy /api → :8080
```

### Variables útiles

| Variable | Uso |
|----------|-----|
| `PW_SKIP_BACKEND=1` | Backend ya en Docker; Playwright no intenta `mvn spring-boot:run` |
| `PLAYWRIGHT_BASE_URL` | Frontend en otro puerto (ej. Docker nginx `:3000`) |
| `PW_HEADLESS=false` | Ver el navegador (`pnpm test:e2e:headed`) |
| `CHROMIUM_PATH` | Chromium del sistema en distros no soportadas |

### Credenciales dev (seed backend)

Ver también `README.md` raíz del monorepo.

| Rol | Email | Contraseña |
|-----|-------|------------|
| JD | `jd@umss.edu.bo` | `JefeDemo2026!` |
| TD | `td@umss.edu.bo` | `TecnicoDemo2026!` |
| CC | `cc@umss.edu.bo` | `CoordDemo2026!` |

---

## 4. Comandos día a día

```bash
cd frontend

# Todos los E2E
PW_SKIP_BACKEND=1 pnpm test:e2e

# Un archivo
PW_SKIP_BACKEND=1 pnpm test:e2e tests/tradicional/login.spec.ts

# UI interactiva de Playwright
pnpm test:e2e:ui

# Navegador visible
pnpm test:e2e:headed

# Informe HTML del último run
pnpm test:e2e:report
```

---

## 5. Tests tradicionales (a mano)

Los specs en `tests/tradicional/` son la **referencia** que el Generator debe imitar.

### Reglas obligatorias

1. **Localizadores:** `getByRole`, `getByLabel`, `getByText`, `getByTestId`. Prohibido CSS, `#id`, `nth-child`.
2. **Sin esperas fijas:** no `waitForTimeout`. Usar `expect(...).toBeVisible()` / `toHaveURL()` / `toHaveCount()`.
3. **Independencia:** cada test hace su propio `page.goto(...)`.
4. **Assert estable:** URLs, headings, roles, filas de tabla, `data-testid`. **No** texto exacto del copiloto LLM ni toasts variables.

### Patrón login (UC-001)

Archivo: `tests/tradicional/login.spec.ts`.

**Trampa conocida:** el botón «Mostrar contraseña» tiene `aria-label="Mostrar contraseña"`.  
`getByLabel('Contraseña')` matchea **dos** elementos. Usar:

```typescript
page.getByRole('textbox', { name: 'Contraseña' })
```

### Crear un test tradicional nuevo

1. Identificá el flujo (ej. listado de procesos).
2. Asegurate de que la UI tenga `<label>`, roles ARIA o `data-testid` (§6).
3. Creá `tests/tradicional/<feature>.spec.ts`.
4. Corré: `PW_SKIP_BACKEND=1 pnpm test:e2e tests/tradicional/<feature>.spec.ts`.

---

## 6. UI testeable (antes de generar en masa)

Sin esto, el Generator cae a selectores CSS y se rompe al primer cambio de Tailwind.

| Prioridad | Qué hacer |
|-----------|-----------|
| Alta | `<label htmlFor>` en inputs (ya en `TextInput`) |
| Alta | `role="alert"` en errores (ya en `Alert`) |
| Media | `data-testid` en tablas, filas, modales, copiloto `/ayuda` |
| Media | Headings semánticos (`h1`, `h2`) en títulos de página |

Ejemplos de `data-testid` sugeridos:

- `login-form` — formulario de login
- `process-row` — fila del listado de procesos
- `assistant-reply` — última respuesta del copiloto (sin assert de texto largo)

---

## 7. Flujo con agentes nativos (Planner → Generator)

### Reglas de consumo

- **Planner:** una **sección** por plan (login, listado procesos, …). No toda la app (~10k tokens).
- **Generator:** **un caso** por vez (~3k tokens por test).
- **Healer:** solo cuando un test ya existente se rompe (miércolo / mantenimiento).

### Paso A — Verificar base

```bash
PW_SKIP_BACKEND=1 pnpm test:e2e tests/seed.spec.ts
PW_SKIP_BACKEND=1 pnpm test:e2e tests/tradicional/login.spec.ts
```

Ambos deben estar en verde antes de planificar.

### Paso B — Planner

1. App viva: `pnpm dev` + backend Docker.
2. En Cursor, invocá el agente **`playwright-test-planner`**.
3. Prompt sugerido:

```
Explora SOLO la pantalla de login en http://127.0.0.1:5173/login.
Contexto: tests/seed.spec.ts y tests/tradicional/login.spec.ts.
Credenciales JD: jd@umss.edu.bo / JefeDemo2026!
Guarda el plan en specs/plan_login.md.
Máximo 8 casos: camino feliz, error, borde.
No incluyas verificación de redacción del asistente LLM.
Formato: grupos ## 1., casos ### 1.1 con **Pasos:** y **Resultado esperado:**
```

4. **Auditá el plan** antes de generar código: ¿cada resultado es visible en pantalla? ¿No pide assert de texto volátil?

Prompts alternativos: `docs/E2E_PROMPTS.md`.

### Paso C — Generator (un caso)

1. Invocá **`playwright-test-generator`** con el caso concreto, por ejemplo `1.1` de `specs/plan_login.md`.
2. Indicá:
   - **test-suite:** nombre del grupo (ej. `Autenticación UC-001`)
   - **test-name:** nombre del caso
   - **test-file:** `tests/agente/<nombre-fs-friendly>.spec.ts`
   - **seed-file:** `tests/seed.spec.ts`
   - **body:** pasos y expectativas del plan

3. El agente abre el navegador, ejecuta pasos en vivo y escribe el spec.

4. Corré solo ese archivo:

```bash
PW_SKIP_BACKEND=1 pnpm test:e2e tests/agente/<archivo>.spec.ts
```

### Paso D — Auditoría (obligatoria)

Respondé las **5 preguntas** de `AUDITORIA_E2E.md`:

1. ¿Localizadores resistentes a CSS?
2. ¿Sin `waitForTimeout`?
3. ¿No verifica copy volátil (LLM, i18n)?
4. ¿Test independiente con su `page.goto`?
5. ¿Pasa en Chromium solo?

Si falla alguna: corregí el spec o renombrá a `*.auditado.spec.ts` cuando quede limpio.

### Paso E — Repetir por caso

Plan con casos `1.1`, `1.2`, `1.3`… → generá **uno**, auditá, corrés, recién ahí el siguiente.

### 7.1 Ejemplo real SIGESA: `plan_login.md`

Flujo ya ejecutado en este proyecto (referencia):

| Paso | Artefacto | Estado |
|------|-----------|--------|
| Semilla | `tests/seed.spec.ts` | ✅ |
| Patrón manual | `tests/tradicional/login.spec.ts` (3 tests UC-001) | ✅ |
| Plan Planner | `specs/plan_login.md` (7 casos 1.1–1.7) | ✅ |
| Generator caso 1.4 | `tests/agente/login-email-no-institucional.spec.ts` | ✅ generado |

#### Casos del plan login

| Caso | Descripción | ¿Ya en tradicional? |
|------|-------------|---------------------|
| 1.1 | JD → `/admin/users` | Sí (`login.spec.ts`) |
| 1.2 | Credenciales inválidas → `role="alert"` | Sí |
| 1.3 | Formulario vacío → errores de campo | Parcial (solo URL/heading) |
| 1.4 | Email `@gmail.com` → validación cliente | **Generator** → `login-email-no-institucional.spec.ts` |
| 1.5 | Toggle mostrar/ocultar contraseña | Pendiente Generator |
| 1.6 | TD → `/dashboard` | Pendiente Generator |
| 1.7 | Estado de carga del botón submit | Pendiente Generator |

#### Comandos usados en el ejemplo

```bash
# Base
PW_SKIP_BACKEND=1 pnpm test:e2e tests/seed.spec.ts
PW_SKIP_BACKEND=1 pnpm test:e2e tests/tradicional/login.spec.ts

# Caso generado 1.4
PW_SKIP_BACKEND=1 pnpm test:e2e tests/agente/login-email-no-institucional.spec.ts
```

#### Formato esperado de `specs/plan_login.md`

```markdown
# SIGESA Login Test Plan

## Application Overview
(resumen de la pantalla /login)

## Test Scenarios

### 1. Autenticación UC-001 — Login
**Seed:** `tests/seed.spec.ts`

#### 1.4. Correo fuera de dominio UMSS es rechazado en cliente
**File:** `tests/agente/login-email-no-institucional.spec.ts`
**Steps:**
  1. Navegar a `/login`
    - expect: ...
```

#### Trampa: ruta duplicada al guardar el plan

Si `planner_save_plan` usa `fileName: frontend/specs/plan_login.md` **desde el cwd `frontend/`**, puede crear:

```text
frontend/frontend/specs/plan_login.md   ← incorrecto
```

**Corregí** moviendo a `frontend/specs/plan_login.md`. Al invocar el Planner, pedí explícitamente:

> Guardá en `specs/plan_login.md` (relativo a la carpeta `frontend/`).

#### Trampa: spec generado y campo contraseña

En login, **no** uses `getByLabel('Contraseña')` — choca con el botón «Mostrar contraseña». Patrón del repo:

```typescript
page.getByRole('textbox', { name: 'Contraseña' })
```

Ver helper `passwordField()` en `tests/tradicional/login.spec.ts`.

---

## 8. Healer (tests rotos)

Agente: **`playwright-test-healer`**.

Usalo cuando un spec que antes pasaba falla tras un cambio de UI. El healer explora el snapshot actual y propone localizadores nuevos respetando las mismas reglas (roles, labels, testid).

---

## 9. Solución de problemas

### Timeout `webServer` (60s / 120s)

**Causa típica:** puerto `5173` ocupado; Vite saltaba a `5174` y Playwright seguía esperando `5173`.

**Solución:**

```bash
ss -tlnp | grep 5173          # ver proceso
kill <pid>                    # matar vite huérfano
pnpm dev                      # un solo Vite en :5173
```

La config ya usa `strictPort: true` en Vite.

### `strict mode violation` en `getByLabel('Contraseña')`

Ver §5 — usar `getByRole('textbox', { name: 'Contraseña' })`.

### Backend no responde

```bash
curl -s -o /dev/null -w '%{http_code}\n' http://127.0.0.1:8080/v3/api-docs
docker compose up -d backend
```

### Frontend Docker (`:3000`) en lugar de Vite

```bash
PLAYWRIGHT_BASE_URL=http://127.0.0.1:3000 PW_SKIP_BACKEND=1 pnpm test:e2e
```

### Traza de un fallo

```bash
pnpm exec playwright show-trace test-results/.../trace.zip
```

### MCP conectado pero Planner no guarda archivo

- Revisá si el plan quedó solo en el chat → volvé a pedir `planner_save_plan` o guardá manualmente.
- Revisá `frontend/frontend/specs/` por rutas duplicadas (§7.1).
- Confirmá permisos de escritura en `frontend/specs/`.

### Agente Generator escribe spec pero test falla

1. Compará con `tests/tradicional/login.spec.ts` (localizadores, helper contraseña).
2. Corré un solo archivo con `-g` para depurar:

   ```bash
   PW_SKIP_BACKEND=1 pnpm test:e2e tests/agente/login-email-no-institucional.spec.ts --headed
   ```

3. Si falla `Executable doesn't exist` → `pnpm exec playwright install chromium` en `frontend/`.

### `pnpm test:e2e` vs MCP del chat

| | Terminal `pnpm test:e2e` | MCP en Cursor |
|--|--------------------------|---------------|
| Uso | CI, regresión, verificar specs | Planner/Generator exploran en vivo |
| Navegador | headless (o `--headed`) | controlado por MCP |
| Requiere | `PW_SKIP_BACKEND=1` + servers | `pnpm dev` + backend |

Ambos usan la misma `playwright.config.ts`.

---

## 10. Checklist rápido (nueva sección de la app)

- [ ] UI con labels / roles / `data-testid` donde haga falta
- [ ] Test tradicional de referencia en `tests/tradicional/` (opcional pero recomendado)
- [ ] Planner → `specs/plan_<seccion>.md` (una sección)
- [ ] Revisión humana del plan
- [ ] Generator → un caso → `tests/agente/*.spec.ts`
- [ ] `PW_SKIP_BACKEND=1 pnpm test:e2e tests/agente/<archivo>.spec.ts`
- [ ] Auditoría `AUDITORIA_E2E.md`
- [ ] Commit del spec auditado

---

## 11. Referencias

| Recurso | Ruta |
|---------|------|
| Patrón login | `tests/tradicional/login.spec.ts` |
| Semilla | `tests/seed.spec.ts` |
| Plan login (ejemplo Planner) | `specs/plan_login.md` |
| Spec generado 1.4 (ejemplo Generator) | `tests/agente/login-email-no-institucional.spec.ts` |
| MCP Cursor | `.cursor/mcp.json` (raíz monorepo) |
| Agentes Playwright | `.github/agents/playwright-test-*.agent.md` |
| Config Playwright | `playwright.config.ts` |
| Auditoría 5 preguntas | `AUDITORIA_E2E.md` |
| Prompts copiar/pegar | `docs/E2E_PROMPTS.md` |
| Lab original (contexto) | `tools/PROMPTS_AGENTES.md`, `tools/tests-E2E/` |
| Credenciales y datos seed | `README.md` (raíz monorepo) |
