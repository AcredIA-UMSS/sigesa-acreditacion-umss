---
id: TEST-BASELINE-2026-09-06
title: Foto inicial — Suite de pruebas SIGESA (Fase 0 AcredIA)
fecha: "2026-09-06"
proyecto: SIGESA — Acreditación UMSS
alcance: Backend Java 21 · MOD-ASSISTANT (agentes/chatbots)
estado_ejecucion: ejecutado_2026-09-06_docker_jdk
---

# TEST-BASELINE — Foto inicial (Fase 0)

> **Objetivo:** registrar el estado del sistema **antes** de la tarea AcredIA (agente generador + auditoría humana).  
> **Próxima fase:** Fase 1 — Mapeo estructural de nodos del flujo assistant.

---

## 1. Resumen ejecutivo

| Métrica | Backend global | MOD-ASSISTANT / agentes |
|---------|---------------:|------------------------:|
| **Clases de prueba** (`*Test.java`, `*IT.java`) | **59** | **11** |
| **Métodos anotados** (`@Test` + `@ParameterizedTest`) | **225** | **62** |
| **Invocaciones totales** (expandido parameterized) | **233** | **70** |
| **Invocaciones activas** (excl. `@Disabled`) | **231** | **70** |
| **Clases `@Disabled`** | **2** | **0** |
| **Frontend** (`*.test.ts`, `*.spec.ts`) | **0** | **0** |

**Conteo activo AcredIA (referencia grupo):** **231 invocaciones** (233 − 2 placeholders legacy deshabilitados).

---

## 2. Ejecución de la suite (evidencia)

### 2.1 Ejecución completa vía Docker JDK (2026-09-06 18:38 -04)

| Campo | Valor |
|-------|-------|
| **Comando** | `docker run … eclipse-temurin:21-jdk-alpine ./mvnw clean test verify` |
| **Duración** | ~2 min 08 s |
| **Tests run** | **232** |
| **Failures** | **6** |
| **Errors** | **0** |
| **Skipped** | **1** (`UploadEvidenceServiceTest` legacy `@Disabled`) |
| **BUILD** | **FAILURE** (fallos en surefire; JaCoCo report no generado) |

### 2.2 Fallos detectados (baseline — deuda técnica pre-existente)

| # | Clase | Método | Causa probable |
|---|-------|--------|----------------|
| 1 | `TemplateStructureValidatorTest` | `shouldAcceptValidPublishStructure` | Fixture sin `requisitos_subfase` (regla V9+) |
| 2 | `TemplateControllerWebMvcTest` | `shouldCreateTemplate` | HTTP 400 vs 201 esperado (misma regla) |
| 3 | `UploadEvidenceServiceTest` | `upload_success` | Assert `IndicatorState` vs `SubphaseState.SUBIDO` (pivot v1.1) |
| 4 | `AssistantToolRegistryTest` | `toolsForRoleAndAgent_ccUsersProfile_isEmpty` | Perfil `users` CC ahora incluye `search_normative_docs` |
| 5 | `AssistantToolRegistryTest` | `toolsForRoleAndAgent_tdUsersProfile_isEmpty` | Idem TD perfil `users` |
| 6 | `AssistantToolRbacGuardTest` | `tdUsersAgentSubset_excludesListUsersForTdRole` | Idem — expectativa `.isEmpty()` desactualizada |

> Estos 6 fallos **no** provienen de la tarea AcredIA; documentar en bitácora Fase 5 si se corrigen antes o durante auditoría.

### 2.3 Intento host `./mvnw test` (anterior — bloqueado)

| Campo | Valor |
|-------|-------|
| **Resultado** | FALLIDO — compilación |
| **Causa** | `backend/target/classes/` con archivos propiedad `root` |

**Workaround usado:** contenedor JDK con volumen montado (`clean` como root en container).

### 2.4 JaCoCo

Pendiente: `./mvnw verify` con suite verde → `backend/target/site/jacoco/index.html`

---

## 3. Distribución por módulo (backend)

| Módulo | Clases | Métodos | Invocaciones |
|--------|-------:|--------:|-------------:|
| MOD-ASSISTANT | 9* | 54 | 62 |
| MOD-AUTH | 7 | 35 | 35 |
| MOD-PROCESS / workflow | 11 | 40 | 40 |
| MOD-EVIDENCE | 3 | 12 | 12 |
| MOD-TEMPLATE | 5 | 10 | 10 |
| MOD-DASHBOARD / REPORT | 8 | 23 | 23 |
| Adaptadores Web | 11 | 43 | 43 |
| Adaptadores out (auth, report, JPA) | 2 | 3 | 3 |
| E2E / IT | 1 | 1 | 1 |
| Performance | 1 | 1 | 1 |
| Bootstrap / other | 2 | 4 | 4 |

\* MOD-ASSISTANT en paquete `application.service.assistant`; **+2** clases colindantes contadas en fila agentes: `AssistantControllerToolCallingTest`, `NormativeSearchQueryNormalizerTest` → **11 clases agente**.

---

## 4. Pirámide S2–S3 (estado inicial)

| Capa pirámide | Sprint ref. | Clases aprox. | Estado |
|---------------|-------------|---------------|--------|
| **S2 — Unit tests** | S2 | ~50 | ✅ Mayoría de la suite |
| **S3 — Integración / contrato** | S3 | ~6–8 | ⚠️ Parcial (sin contract formal JSON Schema) |
| S4–S5 E2E web | — | 0 | ❌ |
| S6–S7 Evals IA | — | 0 | ❌ |
| S8 Red-teaming | — | 0 | ❌ (validación SQL/XSS en S2) |
| S11 Monitoreo | — | 0 | ❌ |

### Clasificación S3 existente (integración)

| Clase | Tipo |
|-------|------|
| `ModAuthServiceIntegrationTest` | Integración in-memory (sin Spring) |
| `AuthenticatedApiSmokeTest` | `@SpringBootTest` + MockMvc |
| `JwtAuthenticationFilterTest` | `@SpringBootTest` |
| `UserProgramAssignmentRepositoryTest` | `@DataJpaTest` + H2 |
| `ReportExportAsyncE2EIT` | `@SpringBootTest` E2E use cases |
| `SendChatMessageServiceToolLoopTest` | Unit/S3 borde — loop orquestador + LLM mock |

**Contract testing formal:** no hay validación JSON Schema / DTO contract dedicada en tests assistant.

---

## 5. MOD-ASSISTANT — inventario inicial (11 clases)

| Clase | Métodos | Invocaciones | Capa |
|-------|--------:|-------------:|------|
| `AssistantChatInputValidatorTest` | 5 | 13 | S2 |
| `AssistantControllerToolCallingTest` | 5 | 5 | S2 (controller unit) |
| `AssistantNormativeRagServiceTest` | 2 | 2 | S2 |
| `AssistantOutOfScopeDetectorTest` | 2 | 2 | S2 |
| `AssistantProcessQueryParserTest` | 2 | 2 | S2 |
| `AssistantStructureLookupTest` | 6 | 6 | S2 |
| `AssistantToolExecutorTest` | 6 | 6 | S2 |
| `AssistantToolRbacGuardTest` | 7 | 7 | S2 |
| `AssistantToolRegistryTest` | 11 | 11 | S2 |
| `SendChatMessageServiceToolLoopTest` | 13 | 13 | S2/S3 |
| `NormativeSearchQueryNormalizerTest` | 3 | 3 | S2 |

**Gaps identificados para fases posteriores:**
- Sin `@WebMvcTest(AssistantController)` + MockMvc HTTP
- Sin contract test schema respuesta `POST /assistant/chat`
- Sin tests executor para tools `evidence` (`list_pending_evidences`, etc.)
- Sin tests frontend copilotos (`DomainCopilotFloatingChat`)

---

## 6. Clases deshabilitadas (legacy)

| Clase | Motivo |
|-------|--------|
| `UploadEvidenceServiceTest` (`application.service`) | `@Disabled` — superseded MOD-EVIDENCE |
| `EvidenceUploadControllerIT` | `@Disabled` — superseded |

---

## 7. Stack y herramientas

| Herramienta | Versión / uso |
|-------------|---------------|
| JUnit 5 (Jupiter) | Runner principal |
| Mockito | `@ExtendWith(MockitoExtension.class)`, `@MockitoBean` |
| AssertJ | Aserciones |
| Spring Boot Test | `@SpringBootTest`, `@WebMvcTest`, `@DataJpaTest` |
| JaCoCo | Plugin Maven (`verify` → reporte) |
| Maven Surefire | 3.5.6 |
| Frontend tests | **Ninguno** |

---

## 8. Artefactos generados (Fase 0)

| Archivo | Descripción |
|---------|-------------|
| `docs/qa/TEST-BASELINE-2026-09-06.md` | Este documento |
| `docs/qa/test-inventory-baseline.json` | Inventario machine-readable (59 clases) |

---

## 9. Línea base para reporte comparativo final

Registrar al cierre de la tarea AcredIA:

| KPI | Foto inicial (2026-09-06) | Foto final (TBD) |
|-----|--------------------------:|-----------------:|
| Clases test backend | 59 | |
| Métodos anotados | 225 | |
| Invocaciones activas | 231 | |
| Clases MOD-ASSISTANT | 11 | |
| Invocaciones MOD-ASSISTANT | 70 | |
| Tests generados por agente | 0 | |
| Tests tras auditoría humana | 0 | |
| Cobertura JaCoCo (global) | TBD* | |
| Cobertura assistant package | TBD* | |

\* Pendiente ejecución `./mvnw clean test verify` tras remediación `target/`.

---

## 10. Trazabilidad tarea AcredIA

| Fase | Estado |
|------|--------|
| **Fase 0 — Foto inicial** | ✅ Completada (232 run · 6 fail · 1 skip · 2026-09-06) |
| **Fase 1 — Mapeo nodos** | ✅ Completada — [AGENT-STRUCTURE-MAP.md](./AGENT-STRUCTURE-MAP.md) |
| **Fase 2 — Diseño cobertura** | ✅ [AGENT-TEST-COVERAGE-PLAN.md](./AGENT-TEST-COVERAGE-PLAN.md) |
| **Fase 2b — JaCoCo + duplicados** | ✅ [PHASE-2B-COVERAGE-DUPLICATES.md](./PHASE-2B-COVERAGE-DUPLICATES.md) · **43,6 % línea** |
| **Fase 3 — Agente generador** | ✅ [TEST-GENERATOR-PROMPT.md](./TEST-GENERATOR-PROMPT.md) |
| Fase 4 — Generación | Pendiente |
| Fase 5 — Auditoría humana | Pendiente |
| Fase 6 — Integración + contratos | Pendiente |
| Fase 7 — Entregables finales | Pendiente |
