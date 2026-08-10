# ENTREGA — Tool calling en SIGESA (Tarea semana)

**Proyecto:** SIGESA Acreditación UMSS  
**Repositorio:** `sigesa-acreditacion-umss`  
**Rama:** `feature/toolcalling`  
**Documento Word:** [`ENTREGA-TOOL-CALLING-SEMANA.docx`](ENTREGA-TOOL-CALLING-SEMANA.docx)  
**Fecha objetivo:** 8 ago 2026  

---

## 1. Objetivo cumplido

El asistente en `/ayuda` demuestra **tool calling sobre el sistema propio**:

- El **LLM solo elige la tool** cuando la pregunta no coincide con el catálogo de palabras clave.
- La **respuesta final la produce siempre el código** (`AssistantToolExecutor` + `AssistantResponseFormatter`).
- En pantalla se muestra: **herramienta**, **tablas fuente** y **camino** (`KEYWORD` / `LLM` / `OUT_OF_SCOPE`).

---

## 2. Feature flags (interruptor IA)

| Variable | Efecto |
|----------|--------|
| `SIGESA_ASSISTANT_ENABLED=true` | Módulo asistente activo |
| `SIGESA_ASSISTANT_LLM_ENABLED=true` | IA encendida: escenario 2 (sinónimos) funciona |
| `SIGESA_ASSISTANT_LLM_ENABLED=false` | IA apagada: escenario 1 y 4 funcionan; escenario 2 cae en «no sé» |

En `docker-compose.yml`:

```yaml
- SIGESA_ASSISTANT_ENABLED=true
- SIGESA_ASSISTANT_LLM_ENABLED=true   # false para escenario 4
- SIGESA_ASSISTANT_MODEL=qwen2.5:7b  # fijado, no usar latest
```

---

## 3. Modelo y stack

| Componente | Versión / nota |
|------------|----------------|
| Modelo LLM | `qwen2.5:7b` (Ollama, pin fijado en compose) |
| Proxy LLM | Open WebUI (`http://open-webui:8080/api`) |
| Backend | Spring Boot — `SendChatMessageService` |
| Catálogo tools | [`TOOL-CATALOG.md`](TOOL-CATALOG.md) |

**Qué no funcionó bien antes:** `llama3.2:3b` alucinaba y no invocaba tools de forma fiable; permisos Open WebUI causaban `Model not found` (mitigado con `BYPASS_MODEL_ACCESS_CONTROL`).

---

## 4. Cuatro escenarios demo (SIGESA)

| # | Pregunta ejemplo | Camino | Tool | Tablas fuente |
|---|------------------|--------|------|---------------|
| 1 | Lista las fases de Ingeniería de Sistemas CEUB | KEYWORD | `list_process_phases` | `phases`, `subphases`, `accreditation_processes`, `programs` |
| 2 | ¿Qué **etapas** tiene el proceso activo de Ingeniería de Sistemas CEUB? | LLM | `list_process_phases` | (mismas que 1) |
| 3 | ¿Cuál es el presupuesto de la universidad para 2027? | OUT_OF_SCOPE | — | — |
| 4 | Igual escenario 1 con `SIGESA_ASSISTANT_LLM_ENABLED=false` | KEYWORD | `list_process_phases` | (mismas que 1) |

**Demostración escenario 4 vs 2:** Con IA apagada, la pregunta del escenario 2 debe responder «no puedo» + capacidades. Eso prueba el valor aportado por el LLM.

---

## 5. Archivos clave del código

| Archivo | Rol |
|---------|-----|
| `AssistantKeywordRouter.java` | Escenarios 1 y 4 — catálogo palabras clave |
| `SendChatMessageService.java` | Orquestación: KEYWORD → LLM tool-pick → OUT_OF_SCOPE |
| `AssistantToolExecutor.java` | Ejecuta use cases (datos reales) |
| `AssistantResponseFormatter.java` | Redacta respuesta (nunca el LLM) |
| `AssistantToolSourceRegistry.java` | Mapeo tool → tablas PostgreSQL |
| `AssistantCapabilitiesCatalog.java` | Escenario 3 — mensaje + lista de capacidades |
| `AssistantController.java` | API + escenarios demo en `/status` |
| `AssistantChatUI.tsx` | UI: metadata visible en cada respuesta |

---

## 6. Cómo probar

```bash
docker compose up -d --build
# Login JD/TD: jd@umss.edu.bo / JefeDemo2026!
# Abrir http://localhost:3000/ayuda
```

Tests automatizados:

```bash
cd backend && ./mvnw test -Dtest=SendChatMessageServiceToolLoopTest
```

---

## 7. URL y documentación relacionada

- Diseño módulo: [`DD-SYS-002.md`](../DD-SYS-002.md) §11
- Catálogo tools: [`TOOL-CATALOG.md`](TOOL-CATALOG.md)
- Contrato vivo: [`DTP.md`](../../product/DTP.md) §B.5
