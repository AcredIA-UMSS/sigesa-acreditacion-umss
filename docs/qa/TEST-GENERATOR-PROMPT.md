---
id: TEST-GENERATOR-PROMPT
title: Generador de tests — Fase 3 AcredIA
fecha: "2026-09-06"
herramienta: tools/test-generator (Python 3.12 + Docker)
---

# Test Generator — Fase 3

> Contenedor Python que genera tests JUnit en staging vía API OpenAI-compatible.  
> Soporta **Ollama local** y **Groq**. Configuración unificada en `.env` raíz.

---

## 1. Ubicación

```
tools/test-generator/
  Dockerfile
  requirements.txt
  batches.json
  run.sh              # wrapper → docker compose --profile tools
  testgen/            # módulo Python
    __main__.py
    config.py
    llm_client.py
    prompt.py
    writer.py
    batches.py
```

Salida staging: `backend/src/test/java/com/umss/sigesa/generated/assistant/`

---

## 2. Configuración (.env)

Copiar `.env.example` → `.env` y configurar:

| Variable | Uso |
|----------|-----|
| `SIGESA_RUNTIME` | `auto` \| `host` \| `docker` (contenedor fuerza `docker`) |
| `SIGESA_LLM_PROVIDER` | `local` \| `groq` |
| `SIGESA_TESTGEN_PROVIDER` | Override solo generador (opcional) |
| `GROQ_API_KEY` | API key Groq |
| `SIGESA_ASSISTANT_API_KEY` | API key Open WebUI (local) |
| `SIGESA_LLM_BASE_URL_DOCKER` | `http://ollama:11434/v1` (Ollama en compose) |
| `SIGESA_LLM_BASE_URL_HOST` | `http://localhost:11434/v1` |
| `SIGESA_LLM_MODEL_LOCAL` | ej. `qwen2.5:7b` |
| `SIGESA_LLM_MODEL_GROQ` | ej. `llama-3.3-70b-versatile` |
| `SIGESA_TESTGEN_TEMPERATURE` | **0** (obligatorio AcredIA) |
| `SIGESA_TESTGEN_MAX_TESTS` | 8 (default) |

### Ollama en host, generador en contenedor

Si Ollama corre fuera de Docker Compose:

```bash
SIGESA_LLM_BASE_URL_DOCKER=http://host.docker.internal:11434/v1
```

### Cambiar provider

**Local (Ollama en compose):**
```bash
SIGESA_LLM_PROVIDER=local
SIGESA_LLM_BASE_URL_DOCKER=http://ollama:11434/v1
SIGESA_LLM_MODEL_LOCAL=qwen2.5:7b
```

**Groq (sin Ollama):**
```bash
SIGESA_LLM_PROVIDER=groq
GROQ_API_KEY=gsk_...
SIGESA_LLM_MODEL_GROQ=openai/gpt-oss-20b
```

---

## 3. Comandos

Todos vía contenedor (única forma soportada):

```bash
./tools/test-generator/run.sh --show-config
./tools/test-generator/run.sh --list-batches
./tools/test-generator/run.sh --batch L1 --dry-run
./tools/test-generator/run.sh --batch L1
```

Equivalente manual:

```bash
docker compose --profile tools build test-generator
docker compose --profile tools run --rm test-generator --batch L1
```

El servicio `test-generator` usa `profiles: ["tools"]` — **no arranca** con `docker compose up` normal.

Backend en host:

```bash
eval "$(./scripts/sigesa-env.sh)"
cd backend && ./mvnw spring-boot:run
```

---

## 4. Lotes (batches.json)

| Lote | Clase fuente | Output staging | Max |
|------|--------------|----------------|----:|
| L1 | `AssistantKeywordRouter` | `AssistantKeywordRouterTest_AgentGenerated` | 8 |
| L2 | `AssistantResponseFormatter` | `AssistantResponseFormatterTest_AgentGenerated` | 8 |
| L3 | `AssistantToolExecutor` (evidence) | `AssistantToolExecutorEvidenceTest_AgentGenerated` | 4 |
| L4 | `AssistantController` | `AssistantControllerWebMvcTest_AgentGenerated` | 8 |
| L5 | `OpenWebUiChatAdapter` | `OpenWebUiChatAdapterTest_AgentGenerated` | 6 |
| L6 | `AssistantChatContextFactory` | `AssistantChatContextFactoryTest_AgentGenerated` | 5 |

---

## 5. Prompt constraints (system)

- JUnit 5, Mockito, AssertJ, Spring Boot Test
- Package `com.umss.sigesa.generated.assistant`
- Max N `@Test` por corrida · temperature = 0
- Código fuente + tests contexto (no duplicar)
- Salida: **un solo archivo Java**, sin markdown
- PROHIBIDO modificar producción

---

## 6. Post-generación (Fase 5)

1. `./mvnw test -Dtest=*AgentGenerated`
2. Checklist auditoría ([AGENT-TEST-COVERAGE-PLAN §7](./AGENT-TEST-COVERAGE-PLAN.md))
3. `TEST-AUDIT-LOG.md`
4. Mover aceptados fuera de `generated/assistant/`

---

## 7. Estado

| Fase | Estado |
|------|--------|
| Fase 3 — Agente generador | ✅ Python + Docker |
| Fase 4 — Generación lotes | Pendiente |
