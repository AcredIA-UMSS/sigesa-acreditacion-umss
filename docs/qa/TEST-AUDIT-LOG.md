---
id: TEST-AUDIT-LOG
title: Bitácora de auditoría — tests MOD-ASSISTANT (AcredIA)
fecha: "2026-09-06"
proyecto: SIGESA — Acreditación UMSS
trazabilidad: AGENT-TEST-COVERAGE-PLAN · TEST-BASELINE-2026-09-06
---

# Bitácora de auditoría de tests

> Registro humano de tests generados, aceptados y rechazados por capa y lote.

---

## Resumen

| Capa | Generados | Aceptados | Rechazados | Pendiente auditoría |
|------|----------:|----------:|-----------:|------------------:|
| S2 unit | 20 | 20 | 2 | 0 |
| S3 integración/contrato | 8 | 8 | 0 | 0 |

---

## L1 — `AssistantKeywordRouterTest` (S2)

| Origen | Archivo | Generados | Aceptados | Rechazados | Notas |
|--------|---------|----------:|----------:|-----------:|-------|
| Ollama full | `generated/assistant/AssistantKeywordRouterTest_AgentGenerated.java` | 0 | 0 | 1 | Prosa + código producción inventado |
| Groq compact | `generated/assistant/AssistantKeywordRouterTest_AgentGenerated.java` | 8 | 0 | 1 | API inventada (`router.route`, package wrong) |
| IDE agent | `application/service/assistant/AssistantKeywordRouterTest.java` | 8 | **8** | 0 | Aceptados 2026-09-06 — API real `resolve(message, history, auth)` |

### Tests aceptados L1

| Método | Caso |
|--------|------|
| `listPhasesQuestion_resolvesToListProcessPhases` | «Lista las fases…» → `list_process_phases` |
| `phasesAgent_contextualEtapas_usesCareerFromContext` | «etapas del proceso» en copiloto fases |
| `phasesAgent_confirmoAfterSubphasePreview_resolvesManageSubphaseConfirmed` | «confirmo» tras preview subfase |
| `jdListUsers_resolvesToListUsers` | JD list users |
| `ccPendingEvidences_resolvesToListPendingEvidences` | CC evidencias pendientes |
| `normativaCeub_resolvesToSearchNormativeDocs` | normativa CEUB → RAG keyword |
| `multiStepNormativeIntent_returnsEmpty` | multi-step → vacío |
| `ccListUsers_returnsEmpty` | CC list users → vacío |

---

## L2 — `AssistantResponseFormatterTest` (S2)

| Origen | Archivo | Generados | Aceptados | Rechazados | Notas |
|--------|---------|----------:|----------:|-----------:|-------|
| IDE agent | `application/service/assistant/AssistantResponseFormatterTest.java` | 8 | **8** | 0 | Package `assistant` — acceso a formatter package-private |

### Tests aceptados L2

| Método | Caso |
|--------|------|
| `format_whenNotOk_includesErrorMessage` | `{ ok: false, error }` |
| `format_whenConfirmationRequired_includesConfirmoPrompt` | preview + «confirmo» |
| `format_whenExecuted_includesSuccessMessage` | `executed: true` |
| `format_phasesData_listsPhasesWithCareerHeader` | `phases[]` |
| `format_usersData_listsUsersWithTotal` | `users[]` |
| `format_pendingEvidencesData_listsPendingItems` | `evidences[]` |
| `format_normativeDocumentsData_listsDocumentsWithQuery` | `documents[]` |
| `format_nullOrNonMapData_returnsGenericMessage` | null / data no map |

---

## L3 — `AssistantToolExecutorTest` ampliación evidence (S2)

| Origen | Archivo | Generados | Aceptados | Rechazados | Notas |
|--------|---------|----------:|----------:|-----------:|-------|
| IDE agent | `AssistantToolExecutorTest.java` (+4 métodos) | 5 inv. | **5** | 0 | `@ParameterizedTest` completo/incompleto |

### Tests aceptados L3

| Método | Caso |
|--------|------|
| `executeListPendingEvidences_withTdReturnsEvidences` | TD OK |
| `executeListPendingEvidences_withCcScopedProgram` | CC scope + programId |
| `executeGetEvidenceDetail_withValidIndicator` | indicatorId válido |
| `executeCheckEvidenceCompleteness_reportsCompleteFlag` | completo vs incompleto (parametrizado) |

---

## Duplicados auditados (`@Disabled`)

| Test omitido | Mantener | Motivo |
|--------------|----------|--------|
| `AssistantToolRbacGuardTest.tdUsersAgentSubset_excludesListUsersForTdRole` | `AssistantToolRegistryTest.toolsForRoleAndAgent_tdUsersProfile_isEmpty` | Duplicado subset users TD |
| `AssistantToolRbacGuardTest.eeHasNormativeSearchOnly` | `AssistantToolRegistryTest.toolsForRole_eeHasNormativeSearchOnly` | Duplicado EE normative |

---

## Pendiente

| ID | Capa | Clase | Estado |
|----|------|-------|--------|
| P1-1 | S3 | `OpenWebUiChatAdapterTest` | Pendiente |
| P0-1/P0-2 | Fix | `AssistantToolRegistryTest` / RBAC | Fase 5 |

---

## L4 — `AssistantControllerWebMvcTest` (S3)

| Origen | Archivo | Generados | Aceptados | Rechazados | Notas |
|--------|---------|----------:|----------:|-----------:|-------|
| IDE agent | `adapter/in/web/AssistantControllerWebMvcTest.java` | 8 | **8** | 0 | `@WebMvcTest` + JWT mock + `AssistantExceptionHandler` |

### Tests aceptados L4

| Método | Endpoint | Caso | HTTP |
|--------|----------|------|------|
| `chat_withJdValidMessage_returns200AndResponseFields` | POST `/chat` | JD mensaje válido | 200 |
| `chat_withoutAuth_returns401` | POST `/chat` | Sin auth | 401 |
| `chat_withSqlInjectionPayload_returns400` | POST `/chat` | SQL injection | 400 |
| `chat_withEvidenceAgentAndEe_returns403` | POST `/chat` | EE + agent evidence | 403 |
| `status_withPhasesAgent_returns200AndScenarios` | GET `/status` | agent=phases | 200 |
| `status_withEvidenceAgentAndEe_returns403` | GET `/status` | agent=evidence EE | 403 |
| `chat_responseContract_includesReplyPathAndSequentialSteps` | POST `/chat` | Contrato reply/path/steps | 200 |
| `chat_whenAssistantDisabled_returns503` | POST `/chat` | assistant disabled | 503 |

---

## Resumen actualizado

| Capa | Generados | Aceptados | Rechazados | Pendiente auditoría |
|------|----------:|----------:|-----------:|------------------:|
| S2 unit | 20 | 20 | 2 | 0 |
| S3 integración/contrato | 8 | 8 | 0 | 0 |

---

## Pendiente (actualizado)

| ID | Capa | Clase | Estado |
|----|------|-------|--------|
| L5 | S3 | `OpenWebUiChatAdapterTest` | Pendiente |
| L6 | S2 | `AssistantChatContextFactoryTest` | Pendiente |

---

## Fix baseline P0-1 (2026-09-06)

| Cambio | Archivo | Resultado |
|--------|---------|-----------|
| Users agent solo JD en registry | `AssistantToolRegistry.toolsForRoleAndAgent` | 2 tests registry verdes · suite **99/99** (2 `@Disabled`) |

---

## Contrato API — JSON Schema

| Schema | Ruta |
|--------|------|
| Request | `docs/qa/schemas/send-chat-message-request.schema.json` |
| Response | `docs/qa/schemas/send-chat-message-response.schema.json` |

---

## Cobertura JaCoCo DESPUÉS

Ver [PHASE-5-COVERAGE-AFTER.md](./PHASE-5-COVERAGE-AFTER.md): línea **48,8 %** (+5,2 pp vs Fase 2b).
