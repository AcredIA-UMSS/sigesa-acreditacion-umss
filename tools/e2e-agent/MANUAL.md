# Manual — Agente E2E SIGESA (`tools/e2e-agent`)

Guía operativa para planificar y generar tests Playwright con **Python + SDK**, usando tu **IA local en Docker** (Ollama / Open WebUI del `docker-compose.yml`).

| Qué hace | Qué **no** hace |
|----------|-----------------|
| Escribe planes Markdown en `frontend/specs/` | No abre el navegador |
| Genera specs TypeScript en `frontend/tests/agente/` | No sustituye correr Playwright |
| Reporta tokens y tiempo por llamada | No audita solo — vos revisás el output |

**Camino alternativo con exploración en vivo:** agentes MCP Playwright en Cursor → `frontend/docs/MANUAL_E2E.md`.

---

## Índice

1. [Arquitectura](#1-arquitectura)
2. [Prerrequisitos](#2-prerrequisitos)
3. [IA local en Docker](#3-ia-local-en-docker)
4. [Instalación del agente](#4-instalación-del-agente)
5. [Configuración LLM](#5-configuración-llm)
6. [Flujo de trabajo](#6-flujo-de-trabajo)
7. [Comandos](#7-comandos)
8. [Ejecutar tests generados](#8-ejecutar-tests-generados)
9. [Auditoría obligatoria](#9-auditoría-obligatoria)
10. [Secciones disponibles](#10-secciones-disponibles)
11. [Añadir una sección nueva](#11-añadir-una-sección-nueva)
12. [Solución de problemas](#12-solución-de-problemas)
13. [Variables de entorno](#13-variables-de-entorno)

---

## 1. Arquitectura

```text
┌─────────────────────────────────────────────────────────────┐
│  Tu máquina (host)                                          │
│                                                             │
│  python agente_e2e.py plan|generar                          │
│       │                                                     │
│       ├── lee context/*.md + seed.spec.ts + patrón TS       │
│       ├── prompt → LLM                                      │
│       └── escribe frontend/specs/ y frontend/tests/agente/  │
│                    │                                        │
│                    ▼ HTTP :11434 o :3001                    │
│  ┌──────────────────────────────────────┐                   │
│  │  Docker Compose                      │                   │
│  │  sigesa-ollama (:11434)              │                   │
│  │  open-webui   (:3001)  [opcional]    │                   │
│  │  sigesa-backend (:8080)              │                   │
│  └──────────────────────────────────────┘                   │
│                                                             │
│  pnpm test:e2e  →  Vite :5173 + backend :8080               │
└─────────────────────────────────────────────────────────────┘
```

El agente Python corre **en el host** y habla con Ollama/Open WebUI por **puertos publicados** (`11434`, `3001`). No necesita entrar a la red Docker interna.

---

## 2. Prerrequisitos

| Componente | Versión / nota |
|------------|----------------|
| Python | 3.11+ |
| pip | `python -m pip install -r requirements.txt` |
| Docker + Compose | Stack SIGESA |
| Node + pnpm | Para ejecutar Playwright (`frontend/`) |
| Modelo Ollama | p. ej. `qwen2.5:7b` descargado en el contenedor |

---

## 3. IA local en Docker

### 3.1 Levantar Ollama (mínimo para el agente)

Desde la **raíz del monorepo**:

```bash
docker compose up -d ollama
```

Verificar:

```bash
docker ps --filter name=sigesa-ollama
curl -s http://localhost:11434/api/tags | head
```

### 3.2 Descargar el modelo

```bash
docker exec sigesa-ollama ollama pull qwen2.5:7b
docker exec sigesa-ollama ollama list
```

Usá el mismo nombre en `SIGESA_E2E_MODEL`.

### 3.3 Open WebUI (opcional)

Si preferís la UI y API de Open WebUI en lugar de Ollama directo:

```bash
docker compose up -d ollama open-webui
```

- UI: http://localhost:3001  
- API key: Settings → Account → API Keys en Open WebUI  
- Configurar `SIGESA_E2E_BACKEND=open-webui` y `SIGESA_ASSISTANT_API_KEY=sk-...`

### 3.4 Backend SIGESA (para correr tests E2E, no para planificar)

```bash
docker compose up -d backend
cd frontend && pnpm dev    # UI en :5173
```

---

## 4. Instalación del agente

```bash
cd tools/e2e-agent
./setup.sh
```

`setup.sh` crea **`tools/e2e-agent/.venv`**, instala `requirements.txt` y copia `.env.example` → `.env` si no existe.

Ejecutar siempre con el venv (sin activar manualmente):

```bash
./run.sh probar
./run.sh plan login
```

O activar el entorno:

```bash
source .venv/bin/activate
python agente_e2e.py probar
deactivate
```

Configurar LLM (siguiente sección), luego:

```bash
./run.sh probar
```

Deberías ver modelos de Ollama y una respuesta `OK` del chat.

---

## 5. Configuración LLM

### Opción A — Ollama directo (recomendada)

Copiá el ejemplo local:

```bash
cp .env.example .env
```

Contenido típico (`tools/e2e-agent/.env`):

```env
SIGESA_E2E_PROVIDER=local
SIGESA_E2E_BACKEND=ollama
SIGESA_E2E_MODEL=qwen2.5:7b
SIGESA_E2E_TIMEOUT_SECONDS=600
```

El agente usa `http://localhost:11434/v1` (puerto mapeado del contenedor).

### Opción B — Open WebUI

```env
SIGESA_E2E_PROVIDER=local
SIGESA_E2E_BACKEND=open-webui
SIGESA_ASSISTANT_API_KEY=sk-tu-clave-de-open-webui
SIGESA_E2E_MODEL=qwen2.5:7b
SIGESA_E2E_TIMEOUT_SECONDS=600
```

### Opción C — Variables en `.env` de la raíz

Podés poner las mismas claves `SIGESA_E2E_*` en `../../.env` junto al resto de SIGESA. Prioridad:

1. Variables de entorno del shell  
2. `tools/e2e-agent/.env`  
3. `.env` raíz del monorepo  

### Verificar conexión

```bash
python agente_e2e.py probar
```

Salida esperada:

```text
Config: local/ollama @ http://localhost:11434/v1 model=qwen2.5:7b timeout=600s
GET http://localhost:11434/api/tags
Ollama OK — modelos: qwen2.5:7b
Prueba chat (model=qwen2.5:7b)...
modelo: ... tokens: in=... out=... tiempo=...
Respuesta: OK
Conexión LLM OK (...)
```

---

## 6. Flujo de trabajo

```text
1. probar          → LLM local OK
2. plan <seccion>  → frontend/specs/plan_<seccion>.md
3. [REVISIÓN HUMANA del plan]
4. listar          → ver casos 1.1, 1.2, ...
5. generar N.N     → UN test por llamada (~3k tokens)
6. pnpm test:e2e   → verde en Chromium
7. AUDITORIA_E2E   → 5 preguntas
8. Repetir 5–7 por cada caso
```

**Regla de oro:** un caso por `generar`. No generes los 8 casos en un solo prompt.

### Ejemplo completo — sección `ayuda`

```bash
cd tools/e2e-agent
source .venv/bin/activate

python agente_e2e.py probar
python agente_e2e.py plan ayuda
# Abrí y revisá frontend/specs/plan_ayuda.md

python agente_e2e.py listar ayuda
python agente_e2e.py generar ayuda 1.2

cd ../../frontend
PW_SKIP_BACKEND=1 pnpm test:e2e tests/agente/ayuda-envio-vacio.spec.ts
```

---

## 7. Comandos

Todos se ejecutan desde `tools/e2e-agent/` con **`./run.sh`** (usa `.venv`).

| Comando | Descripción |
|---------|-------------|
| `./run.sh secciones` | Lista secciones registradas en `agente_e2e.py` |
| `./run.sh probar` | Test de conexión al LLM |
| `./run.sh plan <seccion>` | Genera/actualiza el plan Markdown |
| `./run.sh listar <seccion>` | Lista casos del plan con archivo destino |
| `./run.sh generar <seccion> <N.N>` | Genera un spec Playwright |
| `./run.sh --dry-run plan ayuda` | Muestra prompt sin llamar al LLM |

### Wrapper desde `tools/`

```bash
cd tools
python agente_e2e.py generar login 1.4
```

---

## 8. Ejecutar tests generados

Tres terminales:

```bash
# T1 — raíz monorepo
docker compose up -d backend ollama

# T2
cd frontend && pnpm dev

# T3
cd frontend
PW_SKIP_BACKEND=1 pnpm test:e2e tests/agente/<archivo>.spec.ts
```

Comandos útiles:

```bash
pnpm test:e2e:headed          # ver el navegador
pnpm test:e2e:ui              # UI interactiva Playwright
pnpm test:e2e:report          # informe HTML del último run
```

Credenciales dev:

| Rol | Email | Contraseña |
|-----|-------|------------|
| JD | jd@umss.edu.bo | JefeDemo2026! |
| CC | cc@umss.edu.bo | CoordDemo2026! |

---

## 9. Auditoría obligatoria

Antes de dar por bueno un spec generado, respondé **sí** a las 5 preguntas de `frontend/AUDITORIA_E2E.md`:

1. ¿Localizadores accesibles (`getByRole`, etc.)?
2. ¿Sin `waitForTimeout`?
3. ¿Sin assert de copy volátil del LLM?
4. ¿Test independiente con `page.goto`?
5. ¿Pasa solo en Chromium?

Corregí a mano lo que el modelo local no acierte (Ollama 7B es más propenso a olvidar reglas que Groq).

---

## 10. Secciones disponibles

| Sección | Plan | Patrón de referencia |
|---------|------|----------------------|
| `login` | `frontend/specs/plan_login.md` | `tests/tradicional/login.spec.ts` |
| `ayuda` | `frontend/specs/plan_ayuda.md` | `tests/agente/ayuda-funciones-usuario-jd.spec.ts` |
| `procesos` | `frontend/specs/plan_procesos_dimension_jd.md` | `tests/agente/procesos-jd-crear-dimension-prueba.spec.ts` |
| `evidencia` | `frontend/specs/plan_evidencia_cc.md` | `tests/agente/procesos-jd-crear-dimension-prueba.spec.ts` |
| `responsable` | `frontend/specs/plan_proceso_responsable_jd.md` | idem |
| `aprobacion` | `frontend/specs/plan_aprobacion_td.md` | idem |
| `plantilla` | `frontend/specs/plan_plantilla_jd.md` | idem |

Contexto que lee el planner/generator: `context/base.md` + `context/<archivo>.md` (ver columna en `SECCIONES` de `agente_e2e.py`).

Listar en terminal: `./run.sh secciones`.

---

## 10.1. Flujo recomendado (minimizar errores del LLM)

1. **Plan humano primero** — Usá los `frontend/specs/plan_*.md` hechos con MCP (o `./run.sh plan <seccion>` solo para refinar). No regeneres el plan entero si ya está auditado.
2. **Un caso por invocación** — `./run.sh generar <seccion> 1.1` (nunca pidas varios casos en un solo prompt).
3. **Patrón vivo** — El spec `procesos-jd-crear-dimension-prueba.spec.ts` es la referencia de estilo (helpers login, un solo `test()`, comentarios `// N.`).
4. **Precondiciones en contexto** — Si el caso depende de datos (evidencia antes de aprobar TD, CC de la misma carrera que el proceso), cumplilas o documentá `test.skip` en el spec tras la primera corrida fallida.
5. **Correr enseguida** — Con backend + `pnpm dev` up:  
   `cd frontend && PW_SKIP_BACKEND=1 pnpm test:e2e tests/agente/<archivo>.spec.ts`
6. **Auditoría** — `frontend/AUDITORIA_E2E.md` (5 preguntas). Corregí a mano localizadores y seed (Ollama 7B suele fallar en selects/modales).
7. **Orden sugerido de specs** — `login` → `plantilla` / `procesos` → `responsable` → `evidencia` → `aprobacion` (cadena de datos).

Variables útiles en `.env`: modelo con buen TypeScript (p. ej. `qwen2.5-coder:7b`), `temperature=0` ya va fijo en el agente.

---

## 11. Añadir una sección nueva

1. Crear `context/mi-seccion.md` (rutas, UI, casos típicos, credenciales).
2. Registrar en `SECCIONES` dentro de `agente_e2e.py`.
3. Tener al menos un spec manual como patrón en `frontend/tests/tradicional/` o `tests/agente/`.
4. `python agente_e2e.py plan mi-seccion` → revisar → `generar`.

---

## 12. Solución de problemas

### `Ollama no responde en localhost:11434`

```bash
docker compose up -d ollama
docker logs sigesa-ollama --tail 30
curl http://localhost:11434/api/tags
```

### `aviso: 'qwen2.5:7b' no aparece`

```bash
docker exec sigesa-ollama ollama pull qwen2.5:7b
```

### Timeout / respuesta muy lenta

- Subí `SIGESA_E2E_TIMEOUT_SECONDS=900`
- Usá un modelo más chico o más cuantizado
- `plan` consume más tokens que `generar`; probá primero con `generar` en un caso chico

### `ModuleNotFoundError: openai`

```bash
pip install -r requirements.txt
```

### El test generado falla en Playwright

1. Leé el error (`getByRole` strict, URL incorrecta, etc.)
2. Corregí a mano o regenerá con plan más detallado
3. Compará con el patrón en `tests/tradicional/login.spec.ts`

### Open WebUI devuelve 401

- Generá API key en http://localhost:3001 → Settings → API Keys
- Ponela en `SIGESA_ASSISTANT_API_KEY`

### Quiero usar Groq temporalmente

```env
SIGESA_E2E_PROVIDER=groq
GROQ_API_KEY=gsk_...
SIGESA_E2E_MODEL=openai/gpt-oss-20b
```

---

## 13. Variables de entorno

| Variable | Default | Descripción |
|----------|---------|-------------|
| `SIGESA_E2E_PROVIDER` | `local` | `local` \| `groq` |
| `SIGESA_E2E_BACKEND` | `ollama` | `ollama` \| `open-webui` (solo si provider=local) |
| `SIGESA_E2E_MODEL` | `qwen2.5:7b` | Modelo Ollama / Open WebUI |
| `SIGESA_E2E_BASE_URL` | auto | Override URL API |
| `SIGESA_E2E_API_KEY` | `ollama` | Clave (Open WebUI / Groq) |
| `SIGESA_E2E_TIMEOUT_SECONDS` | `600` | Timeout por llamada LLM |

Herencia desde raíz (si no hay override E2E):

- `SIGESA_LLM_PROVIDER`, `SIGESA_LLM_MODEL_LOCAL`
- `SIGESA_LLM_BASE_URL_HOST` → `http://localhost:11434/v1`
- `SIGESA_ASSISTANT_BASE_URL_HOST` → Open WebUI
- `GROQ_*` si provider=groq

---

## Referencias

| Documento | Contenido |
|-----------|-----------|
| `README.md` | Resumen rápido |
| `frontend/docs/MANUAL_E2E.md` | Playwright + MCP Cursor |
| `frontend/AUDITORIA_E2E.md` | Checklist 5 preguntas |
| `tools/PROMPTS_AGENTES.md` | Comparación Planner/Generator IDE vs script |
| `.env.example` (este dir.) | Plantilla IA local Docker |
