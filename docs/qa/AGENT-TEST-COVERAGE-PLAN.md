---
id: AGENT-TEST-COVERAGE-PLAN
title: Plan de cobertura S2–S3 — MOD-ASSISTANT (Fase 2 AcredIA)
fecha: "2026-09-06"
proyecto: SIGESA — Acreditación UMSS
trazabilidad: AGENT-STRUCTURE-MAP · TEST-BASELINE-2026-09-06
alcance: MOD-ASSISTANT (agentes + chatbots backend)
---

# Plan de cobertura de pruebas — Fase 2

> **Objetivo:** definir qué probar (S2 unit + S3 integración/contrato), qué ya existe, qué generará el agente IA y qué auditará el equipo humano.  
> **Alcance acordado:** **MOD-ASSISTANT** (backend). Resto del backend fuera de alcance del generador (solo baseline documentado).

---

## 1. Decisiones de alcance

| Decisión | Valor |
|----------|-------|
| **Módulo** | MOD-ASSISTANT (`/api/v1/assistant/**`, servicios `application.service.assistant`) |
| **Pirámide** | S2 (unit) + S3 (integración/contrato) |
| **Excluido** | S4–S11 (E2E web, evals, red-teaming, monitoreo) |
| **Frontend copilotos** | Fuera de alcance Fase 3–4 (0 tests hoy) |
| **Carpeta staging generados** | `backend/src/test/java/com/umss/sigesa/generated/assistant/` |
| **Carpeta destino post-auditoría** | `backend/src/test/java/com/umss/sigesa/.../` (paquete definitivo) |
| **Max tests por lote generador** | **8 métodos** por clase fuente |
| **Modelo generador (Fase 3)** | Python 3.12 en Docker · `temperature: 0` · ver [TEST-GENERATOR-PROMPT.md](./TEST-GENERATOR-PROMPT.md) |

---

## 2. Matriz nodo → cobertura

| Nodo | Capa | Tests actuales | Gap | Acción Fase 3–5 | Prioridad |
|------|------|----------------|-----|-----------------|-----------|
| Guardrail entrada | S2 | 18 inv. | Casos borde historial vacío, Unicode | Ampliar `AssistantChatInputValidatorTest` | P1 |
| Control acceso agente | S2/S3 | 3 inv. | EE evidence, CC users 403 | `@WebMvcTest` status/chat | P0 |
| Clasificador KEYWORD | S2 | 13 inv. (vía SMS) | Sin tests aislados router | **Nuevo** `AssistantKeywordRouterTest` | P0 |
| Clasificador OOS | S2 | 2 inv. | Pocos sinónimos | Ampliar parametrizado | P2 |
| Caché | — | N/A | — | — | — |
| RAG normativo | S2 | 5 inv. | Sin integración FTS | Mock use case en executor | P1 |
| Tool registry | S2 | 11 inv. | **3 tests rotos** + evidence subset | **Fix manual Fase 5** + ampliar | P0 |
| Tool executor | S2 | 6 inv. | Sin evidence (3 tools) | **Nuevo** casos evidence | P0 |
| Tool RBAC | S2 | 7 inv. | **1 test roto** | **Fix manual Fase 5** | P0 |
| LLM loop | S2/S3 | 13 inv. | Solo mock | Mantener; opcional WireMock | P2 |
| HTTP LLM adapter | S3 | 0 | Sin tests | **Nuevo** `OpenWebUiChatAdapterTest` (WireMock) | P1 |
| Fallback LLM off | S2 | 2 inv. | Timeout, 503 | Ampliar adapter test | P1 |
| Guardrail salida | S2 | 0 | Formatter sin tests | **Nuevo** `AssistantResponseFormatterTest` | P0 |
| Contrato API chat | S3 | 0 | Sin MockMvc | **Nuevo** `AssistantControllerWebMvcTest` | P0 |
| Contrato API status | S3 | 0 | Shape JSON | Incluido en WebMvcTest | P1 |
| ChatContextFactory | S2 | 0 | Sin tests | **Nuevo** `AssistantChatContextFactoryTest` | P1 |
| ProcessQueryParser | S2 | 2 inv. | OK base | Ampliar casos borde | P2 |
| StructureLookup | S2 | 6 inv. | OK base | Ampliar | P2 |

---

## 3. Backlog priorizado (implementación)

### P0 — Crítico (antes o durante Fase 5)

| ID | Tipo | Clase / artefacto | Tests nuevos est. | Origen |
|----|------|-------------------|------------------:|--------|
| P0-1 | **Fix baseline** | `AssistantToolRegistryTest` | 0 (ajustar 2 asserts) | Manual auditoría |
| P0-2 | **Fix baseline** | `AssistantToolRbacGuardTest` | 0 (ajustar 1 assert) | Manual auditoría |
| P0-3 | **S2 nuevo** | `AssistantResponseFormatterTest` | 8–10 | Agente + auditoría |
| P0-4 | **S2 ampliar** | `AssistantToolExecutorTest` | +4 (evidence tools) | Agente + auditoría |
| P0-5 | **S2 nuevo** | `AssistantKeywordRouterTest` | 10–12 | Agente + auditoría |
| P0-6 | **S3 nuevo** | `AssistantControllerWebMvcTest` | 8–10 | Agente + auditoría |

### P1 — Importante (Fase 4–6)

| ID | Tipo | Clase | Tests est. |
|----|------|-------|----------:|
| P1-1 | S3 | `OpenWebUiChatAdapterTest` (WireMock) | 5–6 |
| P1-2 | S2 | `AssistantChatContextFactoryTest` | 4–5 |
| P1-3 | S2 | Ampliar `AssistantChatInputValidatorTest` | +3 |
| P1-4 | S3 | Contract assertions `SendChatMessageResponse` | 3–4 (dentro WebMvc) |

### P2 — Deseable (si hay tiempo)

| ID | Clase | Notas |
|----|-------|-------|
| P2-1 | `AssistantOutOfScopeDetectorTest` | +3 parametrizados |
| P2-2 | `AssistantStructureLookupTest` | +2 edge UUID |
| P2-3 | `SendChatMessageServiceToolLoopTest` | RAG fallback path explícito |

### Fuera alcance generador

| Item | Motivo |
|------|--------|
| Fix `TemplateStructureValidatorTest` | MOD-TEMPLATE, no assistant |
| Fix `UploadEvidenceServiceTest` | MOD-EVIDENCE legacy assert |
| Fix `TemplateControllerWebMvcTest` | MOD-TEMPLATE |

*(Opcional: corregir en paralelo para suite global verde.)*

---

## 4. Detalle por clase objetivo (agente generador)

### 4.1 `AssistantKeywordRouterTest` (S2 — nuevo)

**Fuente:** `AssistantKeywordRouter.java`  
**Contexto input:** `SendChatMessageServiceToolLoopTest`, `AssistantChatInputValidatorTest`  
**Restricciones prompt:** no duplicar escenarios ya cubiertos en SMS tool loop.

| # | Caso | Tipo |
|---|------|------|
| 1 | «Lista las fases…» → `list_process_phases` | positivo KEYWORD |
| 2 | «etapas del proceso» en agent phases → contextual | positivo |
| 3 | «confirmo» tras preview subfase → manage subphase confirmed | positivo |
| 4 | «Lista usuarios registrados» JD | positivo |
| 5 | «evidencias pendientes» CC | positivo |
| 6 | «normativa CEUB» sin estructura | RAG keyword |
| 7 | «y luego busca normativa» | vacío → multi-step (no keyword) |
| 8 | CC + list users | vacío o deny |

### 4.2 `AssistantResponseFormatterTest` (S2 — nuevo)

**Fuente:** `AssistantResponseFormatter.java` (package-private → test en mismo package o vía SMS; preferir `assistant` test package con reflection o test only through package `com.umss.sigesa.application.service.assistant`)

| # | Caso |
|---|------|
| 1 | `{ ok: false, error }` → mensaje error |
| 2 | `confirmationRequired: true` → incluye «confirmo» |
| 3 | `executed: true` → mensaje éxito |
| 4 | `phases[]` → formato listado fases |
| 5 | `users[]` → formato listado usuarios |
| 6 | `evidences[]` → formato pendientes |
| 7 | `documents[]` → formato normativa |
| 8 | data null / ok null → mensaje genérico |

### 4.3 `AssistantToolExecutorTest` — ampliación evidence (S2)

| # | Tool | Caso |
|---|------|------|
| 1 | `list_pending_evidences` | TD OK |
| 2 | `list_pending_evidences` | CC scope |
| 3 | `get_evidence_detail` | indicatorId válido |
| 4 | `check_evidence_completeness` | completo vs incompleto |

### 4.4 `AssistantControllerWebMvcTest` (S3 — nuevo)

**Setup:** `@WebMvcTest(AssistantController.class)`, `@MockitoBean` use cases, JWT mock o `@WithMockUser`.

| # | Endpoint | Caso | HTTP |
|---|----------|------|------|
| 1 | POST `/chat` | JD mensaje válido | 200 + body fields |
| 2 | POST `/chat` | Sin auth | 401 |
| 3 | POST `/chat` | SQL injection | 400 |
| 4 | POST `/chat` | EE + agent evidence | 403 |
| 5 | GET `/status` | agent=phases | 200 + scenarios |
| 6 | GET `/status` | agent=evidence EE | 403 |
| 7 | POST `/chat` | Contract: `reply`, `path`, `steps[]` tipos | 200 |
| 8 | POST `/chat` | assistant disabled | 503 |

**Contract testing:** aserciones AssertJ sobre record `SendChatMessageResponse` — campos obligatorios, `steps[].step` secuencial, `path` enum KEYWORD|LLM|RAG|OUT_OF_SCOPE.

---

## 5. Lotes de generación (Fase 4)

Ejecutar **un lote por sesión** del agente; auditoría antes del siguiente.

| Lote | Clase objetivo | Archivo staging | Max tests |
|------|----------------|-----------------|-----------:|
| L1 | `AssistantKeywordRouter` | `generated/assistant/AssistantKeywordRouterTest_AgentGenerated.java` | 8 |
| L2 | `AssistantResponseFormatter` | `generated/assistant/AssistantResponseFormatterTest_AgentGenerated.java` | 8 |
| L3 | `AssistantToolExecutor` (evidence) | ampliar staging o sección `@Nested Evidence` | 4 |
| L4 | `AssistantController` | `generated/assistant/AssistantControllerWebMvcTest_AgentGenerated.java` | 8 |
| L5 | `OpenWebUiChatAdapter` | `generated/assistant/OpenWebUiChatAdapterTest_AgentGenerated.java` | 6 |
| L6 | `AssistantChatContextFactory` | `generated/assistant/AssistantChatContextFactoryTest_AgentGenerated.java` | 5 |

**Total estimado post-auditoría:** +35…45 métodos · +40…50 invocaciones activas en MOD-ASSISTANT.

---

## 6. Proyección foto final (objetivo AcredIA)

| KPI | Baseline (Fase 0) | Objetivo post Fase 5–7 |
|-----|------------------:|-----------------------:|
| Invocaciones activas global | 231 | ~270–280 |
| Invocaciones MOD-ASSISTANT | 70 | ~110–120 |
| Clases test assistant | 11 | ~16–18 |
| Fallos baseline assistant | 3 | 0 |
| `@WebMvcTest` assistant | 0 | 1 |
| Contract tests API chat | 0 | ≥3 |
| Suite global verde | No (6 fail) | Sí (assistant); opcional 3 no-assistant |

---

## 7. Criterios de auditoría humana (Fase 5)

Rechazar test generado si:

1. Duplica assert ya presente en tests manuales listados como contexto.
2. Mock innecesario de use case ya cubierto en integración superior.
3. Assert excepción que el código no lanza (alucinación agente).
4. Nombre fuera convención `should*` / `method_scenario`.
5. No compila o falla `./mvnw test` en contenedor JDK.

**Bitácora:** `docs/qa/TEST-AUDIT-LOG.md` (crear en Fase 5).

---

## 8. Fixes manuales baseline (cola P0 — no generador)

| Test | Corrección esperada |
|------|---------------------|
| `toolsForRoleAndAgent_ccUsersProfile_isEmpty` | CC users → **empty** (sin rol JD) — mantener; CC no accede agent users |
| `toolsForRoleAndAgent_tdUsersProfile_isEmpty` | TD users → **empty** — mantener |
| `tdUsersAgentSubset_excludesListUsersForTdRole` | Assert `.isEmpty()` para TD+USERS (TD no gestiona users) |

> **Nota:** Los fallos actuales ocurren porque el código incluye `search_normative_docs` en `USERS_AGENT_TOOL_IDS` pero TD/CC no tienen rol en agent `users` — revisar si tests deben assert **empty para TD** (correcto) vs fallo por otro perfil. Auditoría Fase 5 validará contra `AssistantToolRegistry` líneas 252–258.

---

## 9. Prompt constraints (preview Fase 3)

El generador (`./tools/test-generator/run.sh`) **debe** operar con:

```text
- temperature: 0
- Stack: JUnit 5, Mockito, AssertJ, Spring Boot Test (@WebMvcTest where noted)
- Package: com.umss.sigesa.generated.assistant (staging)
- Naming: *Test_AgentGenerated or merge into standard *Test after audit
- Max 8 @Test methods per run
- Include: null, empty, invalid UUID, ACCESS_DENIED, 403, 400
- Exclude: duplicate of [list attached existing test methods]
- Do NOT modify production code
- Output: single Java file only
```

---

## 10. Trazabilidad

| Documento | Rol |
|-----------|-----|
| [AGENT-STRUCTURE-MAP.md](./AGENT-STRUCTURE-MAP.md) | Nodos Fase 1 |
| [TEST-BASELINE-2026-09-06.md](./TEST-BASELINE-2026-09-06.md) | Foto inicial |
| [test-inventory-baseline.json](./test-inventory-baseline.json) | Inventario clases |
| `docs/qa/TEST-AUDIT-LOG.md` | Pendiente Fase 5 |
| [TEST-GENERATOR-PROMPT.md](./TEST-GENERATOR-PROMPT.md) | Fase 3 ✅ |
| `tools/test-generator/` | CLI Java generador |

---

## 11. Estado Fase AcredIA

| Fase | Estado |
|------|--------|
| Fase 0 — Baseline | ✅ |
| Fase 1 — Mapeo nodos | ✅ |
| **Fase 2 — Diseño cobertura** | **✅ Completada** |
| **Fase 2b — JaCoCo + duplicados** | **✅** [PHASE-2B-COVERAGE-DUPLICATES.md](./PHASE-2B-COVERAGE-DUPLICATES.md) |
| **Fase 3 — Agente generador** | **✅** [TEST-GENERATOR-PROMPT.md](./TEST-GENERATOR-PROMPT.md) · `tools/test-generator/` |
| Fase 4 — Generación lotes | Pendiente |
| Fix 3 tests assistant rotos (P0-1/P0-2) | Pendiente Fase 5 |
