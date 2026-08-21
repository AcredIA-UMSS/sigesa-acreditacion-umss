# ENTREGA — Tool calling en SIGESA (Tarea semana)

**Proyecto:** SIGESA Acreditación UMSS  
**Repositorio:** `sigesa-acreditacion-umss`  
**Rama:** `feature/toolcalling`  
**Documento Word:** [`ENTREGA-TOOL-CALLING-SEMANA.docx`](ENTREGA-TOOL-CALLING-SEMANA.docx)  
**Fecha objetivo:** 8 ago 2026  
**Última actualización:** 21 ago 2026 (Nivel 4 multi-tool)

---

## 1. Objetivo cumplido

El asistente en `/ayuda` y los **copilotos embebidos** (fases, usuarios, evidencias) demuestran **tool calling sobre el sistema propio**:

- El **LLM solo elige la tool** (o encadena varias en turnos sucesivos) cuando la pregunta no coincide con el catálogo de palabras clave.
- La **respuesta final la produce siempre el código** (`AssistantToolExecutor` + `AssistantResponseFormatter`).
- En pantalla se muestra: **herramienta**, **tablas fuente**, **camino** (`KEYWORD` / `LLM` / `RAG` / `OUT_OF_SCOPE`) y **traza multi-paso** cuando aplica.

---

## 2. Feature flags (interruptor IA)

| Variable | Efecto |
|----------|--------|
| `SIGESA_ASSISTANT_ENABLED=true` | Módulo asistente activo |
| `SIGESA_ASSISTANT_LLM_ENABLED=true` | IA encendida: escenario 2 (sinónimos) y **5 (multi-tool)** funcionan |
| `SIGESA_ASSISTANT_LLM_ENABLED=false` | IA apagada: escenario 1 y 4 funcionan; escenarios 2 y 5 caen en «no sé» |
| `SIGESA_ASSISTANT_MAX_TOOL_ITERATIONS=5` | Límite de tools encadenadas por mensaje (Nivel 4) |
| `SIGESA_ASSISTANT_RAG_ENABLED=true` | RAG normativo activo (`search_normative_docs`) |

En `docker-compose.yml`:

```yaml
- SIGESA_ASSISTANT_ENABLED=true
- SIGESA_ASSISTANT_LLM_ENABLED=true   # false para escenario 4
- SIGESA_ASSISTANT_MODEL=qwen2.5:7b  # fijado, no usar latest
- SIGESA_ASSISTANT_MAX_TOOL_ITERATIONS=5
```

---

## 3. Modelo y stack

| Componente | Versión / nota |
|------------|----------------|
| Modelo LLM | `qwen2.5:7b` (Ollama, pin fijado en compose) |
| Proxy LLM | Open WebUI (`http://open-webui:8080/api`) |
| Backend | Spring Boot — `SendChatMessageService` (loop multi-tool) |
| Catálogo tools | [`TOOL-CATALOG.md`](TOOL-CATALOG.md) |

**Qué no funcionó bien antes:** `llama3.2:3b` alucinaba y no invocaba tools de forma fiable; permisos Open WebUI causaban `Model not found` (mitigado con `BYPASS_MODEL_ACCESS_CONTROL`).

---

## 4. Escenarios demo (SIGESA)

### 4.1 Asistente general (`/ayuda`)

| # | Pregunta ejemplo | Camino | Tool(s) | Notas |
|---|------------------|--------|---------|-------|
| 1 | Lista las fases de Ingeniería de Sistemas CEUB | KEYWORD | `list_process_phases` | Sin LLM |
| 2 | ¿Qué **etapas** tiene el proceso activo de Ingeniería de Sistemas CEUB? | LLM | `list_process_phases` | LLM elige tool |
| 3 | ¿Cuál es el presupuesto de la universidad para 2027? | OUT_OF_SCOPE | — | Sin datos inventados |
| 4 | Igual escenario 1 con `SIGESA_ASSISTANT_LLM_ENABLED=false` | KEYWORD | `list_process_phases` | Demuestra valor del LLM vs apagado |
| **5** | Muestra estructura CEUB + normativa Matriz de evidencias | LLM | `list_process_structure` → `search_normative_docs` | **Nivel 4** — traza `steps[]` |

### 4.2 Copilotos embebidos

| Agente | Pantalla | Escenario Nivel 4 |
|--------|----------|-------------------|
| `phases` | `/procesos/{id}` | Estructura completa + normativa subfase |
| `users` | `/admin/users` | Lista CC activos + detalle cc@umss.edu.bo |
| `evidence` | `/evidencias/cargar` | Pendientes + normativa matriz CEUB |

**Demostración escenario 4 vs 2:** Con IA apagada, la pregunta del escenario 2 debe responder «no puedo» + capacidades. Eso prueba el valor aportado por el LLM.

---

## 5. Archivos clave del código

| Archivo | Rol |
|---------|-----|
| `AssistantKeywordRouter.java` | Escenarios 1 y 4 — catálogo palabras clave |
| `SendChatMessageService.java` | Orquestación: KEYWORD → loop LLM multi-tool → OUT_OF_SCOPE / RAG |
| `AssistantToolExecutor.java` | Ejecuta use cases (datos reales) |
| `AssistantResponseFormatter.java` | Redacta respuesta (nunca el LLM) |
| `AssistantNormativeRagService.java` | RAG normativo + fallback |
| `AssistantToolSourceRegistry.java` | Mapeo tool → tablas PostgreSQL |
| `AssistantController.java` | API + escenarios demo en `/status` |
| `CopilotAssistantMetadata.tsx` | UI: metadata + traza multi-paso (todos los copilotos) |
| `mapAssistantResponseMetadata.ts` | Mapeo `steps[]` API → metadata UI |

---

## 6. Cómo probar

```bash
docker compose up -d --build
# Login JD/TD: jd@umss.edu.bo / JefeDemo2026!
# Abrir http://localhost:3000/ayuda  (o copiloto en procesos/usuarios/evidencias)
# Usar escenario #5 (Multi-tool Nivel 4) del panel de samples
```

Tests automatizados:

```bash
cd backend && ./mvnw test -Dtest=SendChatMessageServiceToolLoopTest
```

---

## 7. URL y documentación relacionada

- Diseño módulo: [`DD-SYS-002.md`](../DD-SYS-002.md) §11.10
- Catálogo tools: [`TOOL-CATALOG.md`](TOOL-CATALOG.md)
- Contrato vivo: [`DTP.md`](../../product/DTP.md) §B.5
- Implementación Nivel 4: [`PR-IMPL-033.md`](../../prompts/impl/PR-IMPL-033.md)
