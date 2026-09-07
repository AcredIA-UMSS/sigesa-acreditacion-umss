---
id: PHASE-5-COVERAGE-AFTER
title: Cobertura JaCoCo DESPUÉS — MOD-ASSISTANT (AcredIA Fase 5)
fecha: "2026-09-06"
proyecto: SIGESA — Acreditación UMSS
trazabilidad: PHASE-2B-COVERAGE-DUPLICATES · TEST-AUDIT-LOG · AGENT-TEST-COVERAGE-PLAN
alcance: MOD-ASSISTANT backend post L1–L4 + fix baseline P0-1
---

# Cobertura JaCoCo — DESPUÉS (Fase 5)

> Medición tras aceptar tests IDE (L1–L4) y fix `AssistantToolRegistry.toolsForRoleAndAgent` (users agent solo JD).

---

## 1. Ejecución

| Parámetro | Valor |
|-----------|-------|
| Comando | `./mvnw test jacoco:report -Dtest="Assistant*,SendChatMessageServiceToolLoopTest,NormativeSearchQueryNormalizerTest,AssistantControllerWebMvcTest"` |
| Entorno | Docker `eclipse-temurin:21-jdk-alpine` |
| Resultado tests | **99 run · 0 failures · 2 skipped** (`@Disabled` duplicados) |
| Reporte | `backend/target/site/jacoco/jacoco.xml` |

---

## 2. Comparativa agregada MOD-ASSISTANT

| Métrica | ANTES (Fase 2b) | DESPUÉS (Fase 5) | Δ |
|---------|----------------:|-----------------:|--:|
| **Instrucciones** | 43,9 % | **49,6 %** | +5,7 pp |
| **Líneas** | 43,6 % | **48,8 %** | +5,2 pp |
| **Métodos** | 53,8 % | **57,5 %** | +3,7 pp |
| **Ramas** | 31,4 % | **36,5 %** | +5,1 pp |
| **Clases con ≥1 línea** | 74,2 % | **74,2 %** | — |
| Invocaciones test | 70 | **99** | +29 |
| Fallos | 3 | **0** | ✅ |

---

## 3. Clases P0 — evolución

| Clase | Línea ANTES | Línea DESPUÉS | Notas |
|-------|------------:|--------------:|-------|
| `AssistantKeywordRouter` | ~58,8 % | **65,7 %** | +8 tests aislados L1 |
| `AssistantResponseFormatter` | ~37,3 % | **55,6 %** | +8 tests L2 |
| `AssistantToolExecutor` | ~16,5 % | **23,2 %** | +4 evidence L3; hueco grande persiste |
| `AssistantController` | parcial vía unit | **77,1 %** | +8 WebMvc L4 |
| `OpenWebUiChatAdapter` | 0 % | **0 %** | Pendiente L5 WireMock |
| `AssistantChatContextFactory` | 0 % | **0 %** | Pendiente L6 |

---

## 4. Fix baseline P0-1 (producción)

**Archivo:** `AssistantToolRegistry.toolsForRoleAndAgent`

**Cambio:** perfil `USERS` devuelve lista vacía si rol ≠ `JD` (CC/TD no gestionan usuarios; alineado con `AssistantController.assertUsersAgentAccess`).

**Tests verdes:**

- `AssistantToolRegistryTest.toolsForRoleAndAgent_ccUsersProfile_isEmpty`
- `AssistantToolRegistryTest.toolsForRoleAndAgent_tdUsersProfile_isEmpty`

---

## 5. Contrato API — JSON Schema

| Artefacto | Ruta |
|-----------|------|
| Request `POST /api/v1/assistant/chat` | [`docs/qa/schemas/send-chat-message-request.schema.json`](./schemas/send-chat-message-request.schema.json) |
| Response `SendChatMessageResponse` | [`docs/qa/schemas/send-chat-message-response.schema.json`](./schemas/send-chat-message-response.schema.json) |

**Validación manual:** respuesta de `AssistantControllerWebMvcTest.chat_responseContract_includesReplyPathAndSequentialSteps` cumple el schema de response (`path` enum, `steps[].step` secuencial).

---

## 6. Pendiente post-Fase 5

| ID | Item |
|----|------|
| L5 | `OpenWebUiChatAdapterTest` (WireMock) |
| L6 | `AssistantChatContextFactoryTest` |
| — | Validador automático schema en CI (opcional: `networknt/json-schema-validator`) |
| — | Meta >60 % línea global assistant (requiere más coverage executor/RAG) |

---

## 7. Trazabilidad

| Documento | Rol |
|-----------|-----|
| [TEST-AUDIT-LOG.md](./TEST-AUDIT-LOG.md) | Tests aceptados L1–L4 |
| [PHASE-2B-COVERAGE-DUPLICATES.md](./PHASE-2B-COVERAGE-DUPLICATES.md) | Baseline ANTES |
| [AGENT-TEST-COVERAGE-PLAN.md](./AGENT-TEST-COVERAGE-PLAN.md) | Plan original |
