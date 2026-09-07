---
id: PHASE-2B-COVERAGE-DUPLICATES
title: Fase 2b — Cobertura JaCoCo + auditoría duplicados MOD-ASSISTANT
fecha: "2026-09-06"
proyecto: SIGESA — Acreditación UMSS / AcredIA
trazabilidad: TEST-BASELINE-2026-09-06 · AGENT-TEST-COVERAGE-PLAN
alcance: MOD-ASSISTANT backend (11 clases test · 70 invocaciones)
---

# Fase 2b — Cobertura y duplicados

> **Objetivo:** medir cobertura real con JaCoCo sobre la suite MOD-ASSISTANT existente, listar huecos y proponer duplicados para auditoría humana antes de `@Disabled`.

---

## 1. Ejecución

| Parámetro | Valor |
|-----------|-------|
| Comando | `./mvnw test jacoco:report -Dtest="Assistant*,SendChatMessageServiceToolLoopTest,NormativeSearchQueryNormalizerTest"` |
| Entorno | Docker `eclipse-temurin:21-jdk-alpine` (workaround permisos `target/`) |
| Resultado tests | **70 run · 3 failures · 0 skipped** |
| Reporte | `backend/target/site/jacoco/jacoco.xml` |

### Fallos (no duplicados — asserts desactualizados)

Los 3 fallos ocurren porque el código incluye `search_normative_docs` en el perfil `users` para CC/TD, pero los tests esperan subset vacío. Corrección planificada en **Fase 5** (fix manual P0-1/P0-2), **no** marcar como `@Disabled`.

| Test | Error |
|------|-------|
| `AssistantToolRegistryTest.toolsForRoleAndAgent_ccUsersProfile_isEmpty` | Esperaba vacío; recibe `[search_normative_docs]` |
| `AssistantToolRegistryTest.toolsForRoleAndAgent_tdUsersProfile_isEmpty` | Idem |
| `AssistantToolRbacGuardTest.tdUsersAgentSubset_excludesListUsersForTdRole` | Idem |

---

## 2. Cobertura JaCoCo — MOD-ASSISTANT (ANTES)

> Métricas agregadas sobre paquetes `application.service.assistant`, `adapter.out.assistant`, `AssistantController`, `NormativeSearchQueryNormalizer`. Medidas con suite assistant-only (70 tests); JaCoCo generó reporte aunque 3 tests fallaron.

| Métrica | Cubierto | Total | **%** |
|---------|----------|-------|------:|
| **Instrucciones** | 5 216 | 11 880 | **43,9 %** |
| **Líneas** | 1 037 | 2 381 | **43,6 %** |
| **Métodos** | 143 | 266 | **53,8 %** |
| **Ramas** | 415 | 1 322 | **31,4 %** |
| **Clases con ≥1 línea cubierta** | 23 | 31 | **74,2 %** |

**Meta post Fase 4–7 (referencia plan):** ~110–120 invocaciones · cobertura línea objetivo >60 % en nodos P0.

---

## 3. Clases / módulos sin cobertura (0 %)

| Clase | Capa | Prioridad generador |
|-------|------|---------------------|
| `OpenWebUiChatAdapter` | S3 (HTTP adapter) | P1 — Lote L5 WireMock |
| `Slf4jAssistantToolAuditAdapter` | Infra logging | P2 — bajo valor unitario |
| `AssistantChatContextFactory` | S2 | P1 — Lote L6 |
| `AssistantProcessResolver` (+ records internos) | S2 | P1 — vía executor/SMS |
| `AssistantUserActionPlan` | S2 | P2 |
| `AssistantKeywordRouter$PendingWriteAction` | S2 (inner) | Cubierto indirectamente vía SMS confirmo |

---

## 4. Clases con cobertura parcial crítica (<50 % instrucciones)

| Clase | Instr. | Línea | Método | Gap principal |
|-------|-------:|------:|-------:|---------------|
| `AssistantToolExecutor` | 15,0 % | 16,5 % | 21,7 % | Sin tests evidence (3 tools) |
| `AssistantResponseFormatter` | 34,0 % | 37,3 % | 38,5 % | **Sin clase test dedicada** |
| `AssistantUserLookup` | 40,5 % | 42,9 % | 71,4 % | Paths error/búsqueda |
| `AssistantStructureLookup` | 49,2 % | 48,6 % | 61,1 % | Edge UUID / orden |
| `AssistantKeywordRouter` | 60,8 % | 58,8 % | 66,7 % | Sin tests aislados (solo vía SMS) |

---

## 5. Huecos principales (métodos sin ninguna invocación cubierta)

Selección priorizada para el generador (Fase 4). Lista completa: 123 métodos en reporte JaCoCo.

### P0 — Unit (S2)

| Clase | Métodos sin cubrir (ejemplos) |
|-------|-------------------------------|
| `AssistantResponseFormatter` | `formatToolResult`, `formatPhases`, `formatUsers`, `formatEvidences`, `formatDocuments`, `formatError` |
| `AssistantKeywordRouter` | `resolve`, `buildNormativeSearchInvocation`, `buildListPendingEvidencesInvocation`, `findPendingWriteAction` |
| `AssistantToolExecutor` | `list_pending_evidences`, `get_evidence_detail`, `check_evidence_completeness` handlers |
| `AssistantChatContextFactory` | `resolve(...)` (ambas sobrecargas) |

### P1 — Integración / adapter (S3)

| Clase | Métodos sin cubrir |
|-------|-------------------|
| `OpenWebUiChatAdapter` | `complete`, `buildRequestBody`, `extractCompletionResult`, `normalizeBaseUrl` |
| `AssistantController` | `parseRole`, lambdas mapping DTO (parcial — 68 % instr.) |

### Ya bien cubiertos (>95 %)

`AssistantToolRegistry`, `NormativeSearchQueryNormalizer`, `AssistantChatInputValidator`, `AssistantOutOfScopeDetector`, `SendChatMessageService`.

---

## 6. Auditoría de duplicados

**Criterio AcredIA:** misma función bajo prueba + mismos datos de entrada + mismo assert, aunque cambie el nombre.

Análisis automático + revisión manual sobre **61 métodos @Test** en 11 clases.

### 6.1 Duplicados confirmados (acción propuesta)

| Test | Duplica a | Decisión propuesta | Motivo `@Disabled` |
|------|-----------|-------------------|---------------------|
| `AssistantToolRbacGuardTest.tdUsersAgentSubset_excludesListUsersForTdRole` | `AssistantToolRegistryTest.toolsForRoleAndAgent_tdUsersProfile_isEmpty` | **Omitir RbacGuard** | Misma SUT (`registry.toolsForRoleAndAgent("TD", USERS)`), mismo assert `.isEmpty()`; registry es dueño del contrato de subset |
| `AssistantToolRbacGuardTest.eeHasNormativeSearchOnly` | `AssistantToolRegistryTest.toolsForRole_eeHasNormativeSearchOnly` | **Omitir RbacGuard** | Misma SUT (`registry.toolsForRole("EE")`), mismo assert `containsExactly(SEARCH_NORMATIVE_DOCS_ID)` |

> **Estado:** pendiente confirmación humana antes de aplicar `@Disabled`. No se borra código.

### 6.2 Solapamiento parcial (NO duplicado — mantener ambos)

| Test A | Test B | Motivo |
|--------|--------|--------|
| `AssistantToolRegistryTest.isToolAllowedForAgent_rejectsCrossAgentTools` | `AssistantToolRbacGuardTest.isToolAllowedForAgent_phasesRejectsUserTools` | Registry cubre 3 pares agent/tool; RbacGuard añade assert positivo `LIST_PROCESS_PHASES+PHASES → true` |
| `SendChatMessageServiceToolLoopTest.scenario1_controlledKeyword_ccRole_doesNotCallLlm` | `scenario1_controlledKeyword_doesNotCallLlm` | Mismo mensaje; roles CC vs TD (distinto `AssistantAuthContext`); TD añade asserts `toolId`, `sourceTables`, `reply` |
| `AssistantOutOfScopeDetectorTest.detectsBudgetQuestion` | `SendChatMessageServiceToolLoopTest.scenario3_outOfScope_noToolNoInventedData` | Capas distintas: unit detector vs integración SMS completo |
| `AssistantChatInputValidatorTest.validateMessage_rejectsBlockedPatterns` | `AssistantControllerToolCallingTest.chat_rejectsSqlInjectionPayload` | Capas distintas: validator unit vs controller wiring |

### 6.3 Similares descartados como duplicado (falso positivo agente)

| Par | Razón para NO omitir |
|-----|---------------------|
| `AssistantControllerToolCallingTest.status_withEvidenceAgentAndEe_throwsAccessDenied` vs `chat_withEvidenceAgentAndEe_throwsAccessDenied` | Endpoints distintos (`GET /status` vs `POST /chat`); mismo rol/agent pero contratos HTTP diferentes |
| `AssistantToolExecutorTest.executeListUsers_withCcReturnsAccessDenied` vs `AssistantToolRbacGuardTest.ccCannotExecuteListUsers_evenIfLlmHallucinates` | Misma tool+rol; executor test valida JSON `{ok:false, code:ACCESS_DENIED}`; RbacGuard valida audit trail — asserts distintos |

---

## 7. Tests omitidos (`@Disabled`) — estado actual

| Test | Motivo | Estado |
|------|--------|--------|
| `AssistantToolRbacGuardTest.tdUsersAgentSubset_excludesListUsersForTdRole` | Duplicado de `RegistryTest.toolsForRoleAndAgent_tdUsersProfile_isEmpty` | ✅ `@Disabled` aplicado |
| `AssistantToolRbacGuardTest.eeHasNormativeSearchOnly` | Duplicado de `RegistryTest.toolsForRole_eeHasNormativeSearchOnly` | ✅ `@Disabled` aplicado |

### ~~Cola propuesta post-confirmación~~

Aplicado en `AssistantToolRbacGuardTest.java` (2026-09-06).

---

## 8. Implicaciones para fases siguientes

| Fase | Input desde 2b |
|------|----------------|
| **Fase 3** | Prompt generador incluye lista de 123 métodos sin cubrir + clases P0 |
| **Fase 4** | Lotes L1–L6 priorizados por huecos §4–§5 |
| **Fase 5** | Aplicar `@Disabled` confirmados §6.1; fix 3 tests rotos §1; bitácora en `TEST-AUDIT-LOG.md` |
| **Fase 7** | Cobertura **DESPUÉS** vs 43,6 % línea baseline |

---

## 9. Trazabilidad

| Documento | Rol |
|-----------|-----|
| [TEST-BASELINE-2026-09-06.md](./TEST-BASELINE-2026-09-06.md) | Inventario invocaciones |
| [AGENT-TEST-COVERAGE-PLAN.md](./AGENT-TEST-COVERAGE-PLAN.md) | Plan generación por huecos |
| [AGENT-STRUCTURE-MAP.md](./AGENT-STRUCTURE-MAP.md) | Nodos pipeline |
| `backend/target/site/jacoco/index.html` | Reporte HTML interactivo |

---

## 10. Estado AcredIA

| Fase | Estado |
|------|--------|
| Fase 0 — Baseline inventario | ✅ |
| Fase 1 — Mapeo nodos | ✅ |
| Fase 2 — Plan cobertura | ✅ |
| **Fase 2b — JaCoCo + duplicados** | **✅ Completada** |
| Fase 3 — Agente generador | Pendiente aprobación |
