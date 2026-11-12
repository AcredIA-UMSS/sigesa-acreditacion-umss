# PROMPT_MAPPING — Sprint 03

> Registro PM del sprint 03. Trazabilidad: `Código → PR-IMPL → DD-UC-NNN → FSD-UC-NNN → DTP`.  
> **Índice:** PM-001…PM-018 (evidencia/subfases v1 → v2 normativo → M6 workflow → UX/docs → E2E Buscador Evidencias IA → M7 DocumentAsset → M8 FODA → M9 Plan Mejoras/E4 → M10 freeze/Pares + M11 E7 + CAE).

| ID Mapeo | PR-IMPL | Design Doc | FSD / PRD | Descripción de la Tarea |
| :--- | :--- | :--- | :--- | :--- |
| PM-001 | PR-IMPL-034 | DD-UC-022 | FSD-UC-022 / FSD-UC-004 / FSD-UC-021 | Subfases con `requirements`, evidencias múltiples por subfase, observaciones TD/JD, UI + docs |
| PM-002 | PR-IMPL-035 | DD-UC-005 | FSD-UC-005 | Historial de versiones + bloqueo DELETE append-only (API-EVD-03/04) |
| PM-003 | PR-IMPL-036 | DD-UC-006 | FSD-UC-006 | Subsanación por subfase, una vez por observación OPEN, historial liviano |
| PM-004 | PR-IMPL-037 | DD-UC-007 | FSD-UC-007 | Buscador de evidencias en vista fases/subfases del proceso |
| PM-005 | PR-IMPL-038 | DD-UC-008 / DD-UC-009 | FSD-UC-008 / FSD-UC-009 | Rechazo y aprobación de indicadores vía subfase (TD; requiere evidencia + indicatorId) |
| PM-006 | PR-IMPL-039 | DD-UC-010 | FSD-UC-010 | Cierre de fase TD cuando todas las subfases APROBADO (API-WF-03) |
| PM-007 | N/A | DD-AGENT-UI-SHELL | MOD-ASSISTANT (FSD-UC-024 / agentes 001–003) | Shell flotante unificado copilotos fases/evidencias/usuarios + historial conversaciones |
| PM-008 | N/A | N/A | Tests unitarios backend (JaCoCo gate) | Completar tests unitarios de servicios de aplicación y clases del check JaCoCo |
| PM-009 | N/A | N/A | Tests unitarios frontend (Vitest + RTL + MSW) | Infra de tests, cobertura de auth/dashboard/UI y features críticas |
| PM-008 | PR-IMPL-021 | DD-UC-021 | FSD-UC-021 | Full-Stack v2 plantillas: API-TPL-08, UI tabs, publish BR-24, duplicate v2 |
| PM-009 | PR-IMPL-003 | DD-UC-003 | FSD-UC-003 | Clonado árbol v2 al crear proceso (`ProcessNormativeTreeCloner`) |
| PM-010 | PR-IMPL-M6-025 | DD-UC-025 | FSD-UC-025…028 | M6 workflow metodológico (V17, API-WF-04…08, timeline UI) |
| PM-011 | N/A | DD-UC-019 / DD-UC-022 | FSD-UC-019 / FSD-UC-022 | Árbol normativo en capas desplegables N1→Indicador (UI) |
| PM-012 | N/A | DD-UC-001 | FSD-UC-001 | Fix login en bucle post-M6 (operational_mode, redirect, 401 global) |
| PM-013 | N/A | DD-UC-025 | FSD / DTP / ADR-0005 | Sincronización documental v2.0/v2.1 + README |
| PM-014 | PR-IMPL-037 | DD-UC-007 | FSD-UC-007 | Buscador de evidencias E2E Playwright + Modo IA MCP (header `X-AI-Enabled`, toggle UI, preset escenarios demo 1-3) |
| PM-015 | PR-IMPL-M7-004 | DD-UC-004-M7 | FSD-UC-004 / FSD-UC-005 | M7 DocumentAsset M:N (V19, link API, folio, UI vincular existente) |
| PM-016 | PR-IMPL-M8-029 | DD-UC-029 | FSD-UC-029 / FSD-UC-028 | M8 FODA + métricas + encuesta + compuerta E2→E3 / E3 |
| PM-017 | PR-IMPL-M9-031 | DD-UC-031 | FSD-UC-031 / FSD-UC-028 | M9 Plan de Mejoras + documentos E4 + compuerta E4→E5 |
| PM-018 | PR-IMPL-M10-030 | DD-UC-030 | FSD-UC-030 / 020 / 028 / 031 | Cierre 2.1: freeze/Sala de Pares, ciclo E7, CAE_MEMBER |
| PM-019 | PR-IMPL-040 | DD-UC-011 / DD-UC-014 | FSD-UC-011 / FSD-UC-014 | FEAT-01 #50: I/O reportes fuera de TX + Hikari explícito |
| PM-020 | PR-IMPL-041 | DD-UC-004-M7 | FSD-UC-004 / FSD-UC-005 | FEAT-02 #51: MinIO/S3 evidencias + lock BD |
| PM-021 | PR-IMPL-041 | DD-UC-004-M7 | FSD-UC-004 / FSD-UC-014 | FEAT-02 cierre: GET blob + reportes S3 + imagen quay.io MinIO |
| PM-022 | PR-IMPL-041 | DD-UC-004-M7 | FSD-UC-004 | Compose: Silo (`pgsty/silo`) reemplaza imagen MinIO Hub/Quay |
| PM-023 | PR-IMPL-042 | DD-SYS-002 | FSD-UC-024 | FEAT-03 #52 spec: timeout LLM 10 s + 504 + hilos virtuales |
| PM-024 | PR-IMPL-042 | DD-SYS-002 | FSD-UC-024 | FEAT-03 #52 código: sendAsync, timeout 10 s, 504 |
| PM-025 | PR-IMPL-043 | DD-EVALS-OFFLINE | Módulo 7 (Evals) | Marco de evaluación offline de la IA: dataset dorado (15 casos), métricas deterministas/Juez LLM (Kappa 1.0), compuerta exit 0 v3 e informe PDF |
| PM-026 | PR-IMPL-044 | DD-MOD8-ISSUES | FSD-UC-012/015/017 | Issues #53, #54, #55, #56: fail-fast env, outbox transaccional, bandeja dictamen y audit log append-only |
| PM-027 | PR-IMPL-045 | DD-FEAT-10 | FSD-UC-024 | FEAT-10 #57: FAB global de asistencia + modal a11y en el panel autenticado |
| PM-028 | PR-IMPL-035 | DD-UC-005 | FSD-UC-005 | DELETE evidencia no aprobada (CC/CAE 204) + UI quitar/reemplazar |
| PM-029 | N/A | DD-UC-005 | FSD-UC-004 / FSD-UC-005 | Demo 2: openapi.json + CI Spectral/drift + 8.2.0-aportes |
| PM-030 | PR-IMPL-041 | DD-UC-004-M7 | FSD-UC-004 | Restore blob MinIO si reuso de DocumentAsset huérfano |
| PM-031 | PR-IMPL-041 | DD-UC-004-M7 | FSD-UC-004 | Visor/descarga evidencias en frontend + registro Demo 2 OpenAPI |
| PM-032 | PR-IMPL-046 | FEAT-11 | FSD-UC-032 | FEAT-11 #73: Exportación Asíncrona del Dossier Digital (ZIP Jerárquico y Foliado) |
| PM-033 | PR-IMPL-047 | bench/e3/HIPOTESIS.md | E3 (laboratorio) | E3 PASO 0: inventario de stack, puertos, modulo outbox y rutas GET |
| PM-034 | PR-IMPL-047 | bench/e3/HIPOTESIS.md | E3 (laboratorio) | E3 infra: compose Toxiproxy, toxic.sh, verificar-infra, run-limpio |
| PM-035 | PR-IMPL-047 | bench/e3/HIPOTESIS.md | E3 (laboratorio) | E3 ejecucion: up HTTP/in_process, correccion de fallos, down -v |
| PM-036 | PR-IMPL-046 | FEAT-11 | FSD-UC-032 | Dossier ZIP: nombres de negocio para proceso/carrera, enlaces relativos locales en HTML y Content-Disposition |
| PM-037 | PR-IMPL-048 | DD-UC-033 | FSD-UC-033 | Corpus RAG Manual ARCU-SUR 2023 en el asistente existente (#76) |
| PM-038 | PR-IMPL-049 | DD-UC-034 | FSD-UC-034 | Envelope uniforme de errores REST (#67) |
| PM-039 | PR-IMPL-050 | DD-UC-016 | FSD-UC-016 | Portal público: `/api/v1/public/programs` listado+estado+certificado PDF |
| PM-040 | PR-IMPL-051 | DD-UC-035 | FSD-UC-035 | Noticias y destacados: `/api/v1/public/news` + admin JD + home `#noticias` |
| PM-041 | PR-IMPL-050 | DD-UC-016 | FSD-UC-016 | Certificado PDF B-lite: V29 agency/resolution, QR, A4 horizontal |
| PM-042 | PR-IMPL-052 | DD-UC-036 | FSD-UC-036 | Historial de ingresos: sesión, logout, intentos fallidos y actividad [JD] |


---

## PM-001

| Campo | Valor |
| --- | --- |
| **ID** | PM-001 |
| **Fecha** | 2026-08-27 |
| **Solicitante** | Boris Anthony Angulo Urquieta |
| **Agente/Entorno** | Cursor IDE — Agent |
| **Tarea** | Extensión subfases: requisitos, evidencias múltiples y observaciones |
| **Objetivo** | Cada subfase expone nombre, descripción y requisitos de completitud; [CC] sube 1..N evidencias; [TD]/[JD] registran observaciones |
| **Contexto** | Evolución de PM-016/PM-019 (carga por subfase sin FK). Flyway V9. API-SUB-01. |
| **PR-IMPL vinculado** | [PR-IMPL-034](../../prompts/impl/PR-IMPL-034.md) |
| **DD vinculado** | [DD-UC-022](../../design/DD-UC-022.md) |
| **FSD / PRD vinculado** | FSD-UC-022 · FSD-UC-004 · FSD-UC-021 |
| **Estado** | completado |

### Prompt usado exacto

```text
quiero que modifiques las subfases,
que tengan los siguientes datos:
nombre_subfase
descripcion_subfase
requisitos_subfase (hara referencia a los requisitos que debe cumplir para que se considere hecha)

ademas tambien que ajustes ciertas cosas si son necesarias como las evidencias, a una subfase se pueden subir 1 o mas evidencias,
tambien que cada subfase tenga un espacio de observacion, donde el tecnico o administrador pueda dar observaciones a la evidencia subida, realiza todo eso y tambien los cambios respectivos en el frontend

actualiza toda la documentacion necesaria y registralo en el prompt_mapping como sprint 3
```

---

## PM-013

| Campo | Valor |
| --- | --- |
| **ID** | PM-013 |
| **Fecha** | 2026-09-09 |
| **Hora** | 15:20 |
| **Solicitante** | Boris Anthony Angulo Urquieta |
| **Agente/Entorno** | Cursor IDE — Agent |
| **Modelo** | Composer |
| **Tarea** | Sincronización documental v2.0/v2.1 + README |
| **Objetivo** | Cerrar deuda FSD/DTP/ADR; crear FSD-UC-025 y PR-IMPL-M6-025; reconciliar índice Reespecificado→Implementado v2; actualizar README releases |
| **Contexto** | Post-implementación M6; pregunta usuario sobre estado «Reespecificado» vs «Implementado» |
| **PR-IMPL vinculado** | [PR-IMPL-M6-025](../../prompts/impl/PR-IMPL-M6-025.md) |
| **DD-UC vinculado** | [DD-UC-025](../../design/DD-UC-025.md) |
| **FSD-UC vinculado** | FSD-UC-025…028 · FSD-UC-004…010 · FSD-UC-019 |
| **Estado** | completado |

---

## PM-014

| Campo | Valor |
| --- | --- |
| **ID** | PM-014 |
| **Fecha** | 2026-09-11 |
| **Solicitante** | Tech Lead / User |
| **Agente/Entorno** | Antigravity AI — Agent |
| **Tarea** | FSD-UC-007 — Búsqueda de Evidencias E2E Playwright + Modo IA MCP (`X-AI-Enabled`) |
| **Objetivo** | Soportar búsqueda estándar sin IA y búsqueda asistida por IA (con expansión de sinónimos MCP / header `X-AI-Enabled`), conmutador en UI y botones preset de escenario demo (Escenarios 1, 2, 3), verificados 100% mediante suite Playwright E2E a través de componentes UI. |
| **PR-IMPL vinculado** | [PR-IMPL-037](../../prompts/impl/PR-IMPL-037.md) |
| **DD vinculado** | [DD-UC-007](../../design/DD-UC-007.md) |
| **FSD vinculado** | [FSD-UC-007](../../product/uc/FSD-UC-007.md) |
| **Estado** | completado |

### Prompt usado exacto

```text
you just change the way or the full feature to search evidences using AI mode to check similars, etc. pls review it and fix it with the actual UC available for this,take in mind all the scenarios described (without ai, using a toogle to select when to use or not use ai, and finally using synominc (call to function)), etc. pls provide me a good answer and then pls put it available for the users, and if reuqired pls fix the answer, tka ein mind since we are doing a E2E test for this the test cases must use the frontend, buttons and components to call to this feature
```

### Archivos generados o modificados

| Acción | Ruta |
| --- | --- |
| modificado | `frontend/playwright.config.ts` |
| modificado | `frontend/src/features/evidence/api/fetchEvidenceSearch.ts` |
| modificado | `frontend/src/features/evidence/hooks/useEvidenceSearch.ts` |
| modificado | `frontend/src/features/evidence/components/ProcessEvidenceSearchPanel.tsx` |
| modificado | `frontend/src/App.tsx` |
| generado | `frontend/tests/search_evidence/search_evidence.spec.ts` |
| generado | `frontend/src/features/evidence/EvidenceSearchPage.tsx` |

### Cambios realizados

1. **Configuración Playwright:** Configurados reporteros `list` y `html` (`open: 'never'`) y modo visual headed con retardo `slowMo`.
2. **API & Estado Frontend (`fetchEvidenceSearch.ts`, `useEvidenceSearch.ts`):** Normalización de respuestas API (formatos `subsets` / `items`) para prevenir pantalla blanca. Cabecera HTTP `X-AI-Enabled: true` enviada cuando el modo IA está activo.
3. **Panel de Búsqueda UI (`ProcessEvidenceSearchPanel.tsx`):** Checkbox `#evidence-search-ai-toggle`, badge "Modo IA MCP Activo", botones interactivos demo (Escenarios 1, 2, 3) y soporte híbrido para árbol normativo v2 y fases legacy.
4. **Suite E2E Playwright (`frontend/tests/search_evidence/search_evidence.spec.ts`):** 14 casos de prueba E2E interactuando en la ruta de producción `/evidencias/buscar` (y alias `/evidencias/search`).

### Validación ejecutada

- [x] `npx playwright test --reporter=list` — 14/14 tests PASADOS (100% éxito)
- [x] `pnpm exec tsc -b` — 0 errores de compilación
- [x] `docker compose build frontend && docker compose up -d frontend` — Servido en puerto 3000

### Resultado obtenido

Copilotos de dominio comparten UX flotante; páginas ganan ancho útil; trazabilidad en PM-007 y design doc dedicado.

### Próximos pasos

- [ ] Rebuild frontend Docker tras merge
- [ ] Smoke: historial archiva al limpiar; badge correcto por ruta

---

## PM-008

| Campo | Valor |
| --- | --- |
| **ID** | PM-008 |
| **Fecha** | 2026-09-16 |
| **Hora** | 17:10 |
| **Solicitante** | Aylen Gonzáles |
| **Agente/Entorno** | Cursor IDE — Agent (Grok 4.6) |
| **Modelo** | Cursor Grok 4.6 |
| **Tarea** | Completar tests unitarios del backend (arquitectura hexagonal, gate JaCoCo) |
| **Objetivo** | Cubrir servicios de aplicación con JUnit 5 / Mockito / AssertJ, sin PostgreSQL ni servicios externos, y registrar trazabilidad en Sprint 3 |
| **Contexto** | Inventario previo: `FaseServiceImpl` no existe en el código (solo include JaCoCo en `pom.xml`); `DownloadReportArtifactService` no tenía tests; varias clases JaCoCo tenían cobertura parcial |
| **PR-IMPL vinculado** | N/A (prompt de testing, no feature FSD) |
| **DD-UC vinculado** | N/A |
| **FSD-UC vinculado** | N/A (cubre UC-001, UC-002, UC-004, UC-014 y workflow/subfase de forma transversal) |
| **Estado** | completado |

### Prompt usado exacto

```text
Actúa como un ingeniero senior de testing Java/Spring Boot y trabaja sobre este repositorio:

AcredIA-UMSS/sigesa-acreditacion-umss

Objetivo: implementar y completar los tests unitarios del backend ubicado en backend/.

Antes de modificar archivos:

1. Lee obligatoriamente:
   - AGENTS.md
   - backend/pom.xml
   - README.md
   - .cursor/agents/sigesa-orchestrator.md
   - La estructura completa de backend/src/main/java
   - La estructura completa de backend/src/test/java
   - backend/src/test/resources, si contiene configuración
   - Las configuraciones application*.yaml o application*.properties

2. Identifica:
   - La arquitectura hexagonal del backend.
   - La separación entre dominio, aplicación y adaptadores.
   - Los servicios/casos de uso con mayor lógica de negocio.
   - Los tests existentes para no duplicarlos.
   - Las clases incluidas en las reglas de cobertura JaCoCo del pom.xml.
   - Las dependencias y patrones de testing ya usados en el proyecto.

Reglas obligatorias:

- Usa Java 21.
- Usa JUnit 5, Mockito, AssertJ y Spring Boot Test, según corresponda.
- Respeta la arquitectura hexagonal.
- Los tests unitarios de servicios no deben conectarse a PostgreSQL.
- Mockea puertos, repositorios, adaptadores, clientes HTTP, servicios externos y proveedores de autenticación.
- No expongas ni uses entidades JPA directamente cuando el código de producción trabaja con dominio y DTOs.
- No modifiques código productivo salvo que sea estrictamente necesario para hacer testeable una clase. Si es necesario, detente, explica el motivo y solicita confirmación.
- No modifiques archivos dentro de docs/baseline/.
- No borres ni sobrescribas tests existentes.
- No generes tests triviales que solo verifiquen que un objeto no es null.
- No pruebes detalles internos de implementación si puedes probar el comportamiento observable.
- No uses sleeps, llamadas reales a internet, PostgreSQL, Ollama, Groq ni servicios externos.
- Evita tests frágiles y tests que dependan del orden global de ejecución.
- Cada test debe tener nombres descriptivos usando una convención como:
  shouldAuthenticateActiveUser
  shouldRejectInactiveUser
  shouldThrowWhenProgramDoesNotExist

Prioridad de implementación:

1. Completa primero los tests unitarios de las clases de aplicación y servicios con lógica de negocio.
2. Da prioridad a las clases incluidas en JaCoCo:
   - FaseServiceImpl
   - AuthenticateService
   - RegisterUserService
   - DeactivateUserService
   - GenerateExecutiveReportService
   - GetReportJobStatusService
   - ProcessReportJobService
   - DownloadReportArtifactService
   - ReportExportJobService
   - DashboardSummaryAggregationService
   - UploadEvidenceService

3. Después completa los tests de adaptadores importantes.
4. Después completa los tests de controladores usando @WebMvcTest y MockMvc cuando sea apropiado.
5. Usa @DataJpaTest únicamente para pruebas específicas de repositorios JPA.
6. No confundas tests unitarios con tests E2E o de integración.

Para cada servicio, cubre como mínimo:

- Caso exitoso.
- Entrada inválida.
- Entidad o recurso inexistente.
- Regla de negocio incumplida.
- Usuario inactivo o sin permisos, cuando corresponda.
- Excepción del puerto o dependencia externa.
- Verificación de llamadas importantes con Mockito.verify().
- Verificación de que no se llamen dependencias cuando la validación falla.
- Respuestas vacías, listas vacías o valores opcionales vacíos cuando aplique.
- Casos límite relevantes.

Organización de archivos:

- Mantén los tests bajo:
  backend/src/test/java/com/umss/sigesa/

- Respeta los paquetes actuales:
  - application/
  - adapter/
  - e2e/
  - generated/
  - performance/

- Los tests unitarios nuevos deben ubicarse en el paquete equivalente al código productivo.
- No coloques tests unitarios nuevos en e2e/ ni performance/.
- No mezcles tests generados automáticamente con tests escritos manualmente.

Proceso de trabajo:

1. Primero presenta un inventario de:
   - Clases productivas detectadas.
   - Tests existentes.
   - Tests faltantes.
   - Tests duplicados o incompletos.
   - Clases prioritarias.
   No escribas código todavía.

2. Después de mostrar el inventario, comienza por las clases prioritarias y crea los tests en grupos pequeños.

3. Después de cada grupo ejecuta:

   cd backend
   ./mvnw test

4. Si falla un test:
   - Analiza la causa.
   - Corrige el test si el problema está en el test.
   - No cambies código productivo automáticamente.
   - Si el código productivo parece tener un error, repórtalo separadamente y solicita confirmación.

5. Cuando termines todos los tests unitarios, ejecuta:

   cd backend
   ./mvnw clean test
   ./mvnw verify

6. Revisa el reporte JaCoCo generado en:

   backend/target/site/jacoco/index.html

7. Informa:
   - Cuántos tests nuevos se agregaron.
   - Qué clases fueron cubiertas.
   - Qué escenarios se probaron.
   - Resultado de ./mvnw clean test.
   - Resultado de ./mvnw verify.
   - Cobertura obtenida.
   - Clases que todavía no cumplen el 90 %.
   - Problemas que no pudiste resolver.
   - Archivos modificados.
y registra este prompt en @docs/PROMPT_MAPPING.md de Aylen Gonzáles en Sprint 3
```

### Entradas auxiliares

- `backend/pom.xml` (reglas JaCoCo CLASS)
- `docs/sprints/sprint_03/PROMPT_MAPPING.md`

### Archivos generados o modificados

| Acción | Ruta |
| --- | --- |
| generado | `backend/src/test/java/com/umss/sigesa/application/service/report/DownloadReportArtifactServiceTest.java` |
| generado | `backend/src/test/java/com/umss/sigesa/application/service/auth/ListUsersServiceTest.java` |
| generado | `backend/src/test/java/com/umss/sigesa/application/service/catalog/ListProgramsServiceTest.java` |
| generado | `backend/src/test/java/com/umss/sigesa/application/service/evidence/SearchEvidencesServiceTest.java` |
| generado | `backend/src/test/java/com/umss/sigesa/application/service/template/ArchiveTemplateServiceTest.java` |
| generado | `backend/src/test/java/com/umss/sigesa/application/service/subphase/AddSubphaseObservationServiceTest.java` |
| generado | `backend/src/test/java/com/umss/sigesa/application/service/subphase/SubsanateSubphaseEvidenceServiceTest.java` |
| generado | `backend/src/test/java/com/umss/sigesa/application/service/workflow/ApproveSubphaseIndicatorServiceTest.java` |
| generado | `backend/src/test/java/com/umss/sigesa/application/service/workflow/RejectSubphaseIndicatorServiceTest.java` |
| generado | `backend/src/test/java/com/umss/sigesa/application/service/workflow/RejectIndicatorServiceTest.java` |
| modificado | `backend/src/test/java/com/umss/sigesa/application/service/auth/AuthenticateServiceTest.java` |
| modificado | `backend/src/test/java/com/umss/sigesa/application/service/auth/RegisterUserServiceTest.java` |
| modificado | `backend/src/test/java/com/umss/sigesa/application/service/auth/DeactivateUserServiceTest.java` |
| modificado | `backend/src/test/java/com/umss/sigesa/application/service/report/GenerateExecutiveReportServiceTest.java` |
| modificado | `backend/src/test/java/com/umss/sigesa/application/service/report/GetReportJobStatusServiceTest.java` |
| modificado | `backend/src/test/java/com/umss/sigesa/application/service/report/ProcessReportJobServiceTest.java` |
| modificado | `backend/src/test/java/com/umss/sigesa/application/service/report/ReportExportJobServiceTest.java` |
| modificado | `backend/src/test/java/com/umss/sigesa/application/service/dashboard/DashboardSummaryAggregationServiceTest.java` |
| modificado | `backend/src/test/java/com/umss/sigesa/application/service/evidence/UploadEvidenceServiceTest.java` |
| modificado | `backend/src/test/java/com/umss/sigesa/application/service/template/TemplateStructureValidatorTest.java` |
| modificado | `backend/src/test/java/com/umss/sigesa/adapter/in/web/TemplateControllerWebMvcTest.java` |
| modificado | `docs/sprints/sprint_03/PROMPT_MAPPING.md` (PM-008) |

### Cambios realizados

1. Tests unitarios nuevos/extendidos para el gate JaCoCo y servicios de aplicación (auth, reportes, evidencias, dashboard, workflow, subfase, catálogo, plantillas).
2. Ajuste mínimo de tests de plantilla existentes: campo `requirements` obligatorio (alineado a producción; no se tocó código productivo).
3. `FaseServiceImpl` no se testeó porque la clase no existe en el repositorio.

### Validación ejecutada

- [x] `mvnw.cmd clean test` — BUILD SUCCESS, Tests run: 340, Failures: 0, Errors: 0, Skipped: 3
- [x] `mvnw.cmd verify` — BUILD SUCCESS, `jacoco-check` OK
- [ ] `pnpm run lint` — N/A (solo backend)
- [ ] Smoke Docker — N/A

### Resultado obtenido

Gate JaCoCo de clases existentes en verde (≥90 % línea). Cobertura global de líneas ~50.8 %. Quedan servicios de aplicación (assistant, process CRUD, etc.) bajo 90 %.

### Riesgos/observaciones

- Gap de trazabilidad: no hay `PR-IMPL` formal; el usuario autorizó registro PM en Sprint 3.
- `com.umss.sigesa.service.impl.FaseServiceImpl` está en `pom.xml` pero no hay clase productiva.
- Suite global incluye tests E2E/IT/performance ya existentes (`ReportExportAsyncE2EIT`, `EvidenceUploadControllerIT`, `Dashboard1MPerformanceTest`).

### Próximos pasos

- [ ] Cubrir servicios de aplicación restantes (assistant, process structure, upload por subfase)
- [ ] Decidir si eliminar o implementar `FaseServiceImpl` en el include JaCoCo del `pom.xml`

## PM-009

| Campo | Valor |
| --- | --- |
| **ID** | PM-009 |
| **Fecha** | 2026-09-16 |
| **Hora** | 17:35 |
| **Solicitante** | Aylen Gonzáles |
| **Agente/Entorno** | Cursor IDE — Agent (Grok 4.6) |
| **Modelo** | Cursor Grok 4.6 |
| **Tarea** | Completar tests unitarios del frontend (Vitest, RTL, MSW) |
| **Objetivo** | Cubrir UI, auth, dashboard y features críticas sin llamadas reales a API ni backend |
| **Contexto** | Inventario previo: 0 tests; Vitest/RTL/MSW no instalados. Usuario confirmó inventario y pidió implementar. |
| **PR-IMPL vinculado** | N/A (prompt de testing, no feature FSD) |
| **DD-UC vinculado** | N/A |
| **FSD-UC vinculado** | N/A (cubre UC-001, UC-002, dashboard, WF y evidencias de forma transversal) |
| **Estado** | completado |

### Prompt usado exacto

```text
Actúa como un ingeniero senior de testing frontend especializado en React 19, TypeScript estricto y Vite.

Repositorio:
AcredIA-UMSS/sigesa-acreditacion-umss

Objetivo:
Implementar tests unitarios para el frontend ubicado en frontend/.

Antes de modificar archivos, lee obligatoriamente:

- AGENTS.md
- README.md
- frontend/package.json
- frontend/tsconfig.json
- frontend/vite.config.* 
- frontend/src/main.tsx
- frontend/src/App.tsx
- Toda la estructura de frontend/src/components/
- Toda la estructura de frontend/src/features/
- Toda la estructura de frontend/src/lib/
- Toda la estructura de frontend/src/api/
- Toda la estructura de frontend/src/mocks/
- Las reglas relevantes dentro de .cursor/rules/
- .cursor/skills/generate-frontend-feature/SKILL.md, si existe

Primero realiza un inventario y muéstrame:

1. Componentes principales.
2. Features existentes.
3. Hooks personalizados.
4. Providers, especialmente AuthProvider y React Query.
5. Cliente API generado por Orval.
6. Fixtures y mocks existentes.
7. Tests existentes, si los hay.
8. Archivos que necesitan tests.
9. Dependencias de testing que faltan.

No escribas tests hasta terminar este inventario.

Stack obligatorio para los tests:

- Vitest.
- React Testing Library.
- @testing-library/jest-dom.
- @testing-library/user-event.
- jsdom.
- MSW para simular peticiones HTTP.
- TypeScript estricto.

Actualmente el frontend usa React 19, TypeScript, Vite, React Query y Orval. Si Vitest, React Testing Library, jsdom o MSW no están configurados, instálalos y configura el proyecto correctamente.

Agrega o actualiza únicamente los scripts necesarios en frontend/package.json:

- test
- test:run
- test:coverage

Los scripts deben permitir ejecutar:

pnpm test
pnpm test:run
pnpm test:coverage

Reglas obligatorias:

- No uses Jest.
- No uses Cypress ni Playwright para estos tests unitarios.
- No hagas llamadas reales a localhost:8080.
- No hagas llamadas reales a internet.
- No dependas de PostgreSQL, Docker ni del backend para ejecutar los tests unitarios.
- Usa MSW para simular las respuestas de la API.
- No escribas nuevas llamadas fetch o axios manuales en la aplicación.
- Respeta el cliente API generado por Orval.
- No uses any.
- No desactives TypeScript estricto.
- No borres ni sobrescribas código existente.
- No modifiques backend/.
- No modifiques docs/baseline/.
- No cambies componentes productivos solo para hacerlos testeables sin explicarme primero el motivo.
- No hagas tests frágiles basados en clases CSS o estructura interna innecesaria.
- Prioriza queries accesibles: getByRole, getByLabelText, getByText y findByRole.
- No uses snapshots como sustituto de assertions de comportamiento.

Organización de los tests:

- Coloca los tests junto al código que prueban o en una carpeta frontend/src/test/.
- Usa nombres como:
  - ComponentName.test.tsx
  - hookName.test.ts
  - featureName.test.tsx

- Mantén la separación por features:
  - frontend/src/features/auth/
  - frontend/src/features/dashboard/
  - frontend/src/features/processes/
  - frontend/src/features/phases/
  - frontend/src/features/evidence/
  - frontend/src/features/admin/
  - frontend/src/features/assistant/
  - frontend/src/features/reports/

Crea la infraestructura común de testing si hace falta:

- frontend/src/test/setup.ts
- frontend/src/test/test-utils.tsx
- frontend/src/test/mocks/handlers.ts
- frontend/src/test/mocks/server.ts

La función de renderizado de tests debe incluir, cuando sea necesario:

- QueryClientProvider.
- AuthProvider.
- Router de pruebas.
- Configuración aislada de React Query.
- retry: false para queries y mutations.

No reutilices un QueryClient global entre tests si puede provocar contaminación entre casos.

Cubre los siguientes tipos de tests.

1. Componentes presentacionales

Para componentes de frontend/src/components/ y componentes UI de frontend/src/features/ prueba:

- Renderizado correcto.
- Texto visible.
- Props requeridas.
- Estado vacío.
- Estado de carga.
- Estado de error.
- Botones habilitados y deshabilitados.
- Interacciones del usuario.
- Formularios.
- Validaciones.
- Mensajes de error.
- Modales.
- Tablas.
- Paginación.
- Filtros.
- Accesibilidad básica.

Cada test debe verificar comportamiento observable por el usuario.

2. Autenticación

Para frontend/src/lib/auth/ y las pantallas de auth prueba:

- Usuario no autenticado.
- Usuario autenticado.
- Login exitoso.
- Login fallido.
- Token ausente.
- Token inválido o expirado.
- Logout.
- Redirección a login.
- Protección de rutas.
- Roles y permisos.

Usa MSW para simular el endpoint de autenticación.

3. Hooks y React Query

Para frontend/src/lib/hooks/ prueba:

- Estado inicial.
- Carga.
- Respuesta exitosa.
- Error HTTP.
- Mutaciones exitosas.
- Mutaciones fallidas.
- Invalidación de caché.
- Reintentos deshabilitados según la configuración del proyecto.
- Parámetros enviados a los hooks.
- Manejo de respuestas vacías.

Usa renderHook cuando sea apropiado y un QueryClient aislado por test.

4. Dashboard

Usa las fixtures existentes de:

frontend/src/mocks/dashboardFixtures.ts

Prueba:

- Renderizado de indicadores.
- Estados de carga.
- Estados vacíos.
- Errores de API.
- Diferencias de permisos por rol.
- Contenido para JD, TD y CC.
- Tablas y alertas.
- Filtros y paginación si existen.

5. Features

Revisa y crea tests para estas áreas cuando existan componentes o lógica suficiente:

- auth
- dashboard
- processes
- procesos
- phases
- subphases
- evidence
- admin
- assistant
- reports
- accreditation-process

No inventes funcionalidades que no existan en el código.

6. API y Orval

Para cada feature que use hooks generados por Orval:

- No pruebes el código generado internamente línea por línea.
- Prueba que el componente o hook reacciona correctamente a respuestas exitosas y fallidas.
- Intercepta las rutas HTTP mediante MSW.
- Verifica parámetros, método HTTP y datos relevantes enviados.
- Verifica que los errores se muestren correctamente.

7. App y routing

Prueba App.tsx y las rutas principales:

- Ruta pública de login.
- Rutas protegidas.
- Redirección de usuarios no autenticados.
- Rutas inexistentes.
- Renderizado según rol.
- Provider de autenticación.
- Integración con React Query.

Proceso de implementación:

1. Presenta primero el inventario solicitado.
2. Configura Vitest y la infraestructura común.
3. Implementa tests en grupos pequeños por feature.
4. Después de cada grupo ejecuta:

cd frontend
pnpm test:run

5. Después ejecuta:

pnpm lint
pnpm build
pnpm test:coverage

6. Corrige todos los errores encontrados.
7. No marques la tarea como terminada si fallan los tests, el lint o el build.

Criterios de calidad:

- Cada test debe tener un nombre descriptivo.
- Usa arrange, act, assert cuando sea útil.
- Verifica resultados y comportamiento, no detalles internos.
- Evita mocks innecesarios.
- Evita duplicación de configuración.
- Limpia handlers y mocks entre tests.
- Usa userEvent en lugar de fireEvent cuando sea apropiado.
- Usa waitFor o findBy... solo cuando exista una operación asíncrona real.
- No agregues esperas artificiales.
- No uses timeouts arbitrarios.
- Verifica errores de API y estados de carga.
- Mantén los tests independientes y deterministas.

Al finalizar informa:

- Dependencias instaladas.
- Archivos de configuración creados o modificados.
- Tests creados.
- Componentes, hooks y features cubiertos.
- Número total de tests.
- Resultado de pnpm test:run.
- Resultado de pnpm test:coverage.
- Resultado de pnpm lint.
- Resultado de pnpm build.
- Porcentaje de cobertura.
- Funcionalidades que todavía no tienen cobertura.
- Problemas encontrados.
- Si algún cambio productivo fue necesario, explica exactamente por qué.

Empieza únicamente con el inventario. Espera mi confirmación antes de crear o modificar archivos. y guarda este prompt en @docs/PROMPT_MAPPING.md
```

### Entradas auxiliares

- `frontend/package.json`
- `frontend/vite.config.ts`
- `docs/sprints/sprint_03/PROMPT_MAPPING.md`

### Archivos generados o modificados

| Acción | Ruta |
| --- | --- |
| generado | `frontend/src/test/setup.ts` |
| generado | `frontend/src/test/test-utils.tsx` |
| generado | `frontend/src/test/session.ts` |
| generado | `frontend/src/test/mocks/handlers.ts` |
| generado | `frontend/src/test/mocks/server.ts` |
| generado | `frontend/src/App.test.tsx` |
| generado | `frontend/src/components/auth/ProtectedRoute.test.tsx` |
| generado | `frontend/src/components/ui/Alert.test.tsx` |
| generado | `frontend/src/components/ui/Button.test.tsx` |
| generado | `frontend/src/components/ui/ConfirmDialog.test.tsx` |
| generado | `frontend/src/components/ui/Select.test.tsx` |
| generado | `frontend/src/components/ui/TextInput.test.tsx` |
| generado | `frontend/src/features/admin/templates/lib/templateFormValidation.test.ts` |
| generado | `frontend/src/features/admin/users/lib/userFormValidation.test.ts` |
| generado | `frontend/src/features/auth/components/LoginFormUI.test.tsx` |
| generado | `frontend/src/features/auth/pages/LoginPage.test.tsx` |
| generado | `frontend/src/features/dashboard/api/dashboardHooks.test.tsx` |
| generado | `frontend/src/features/dashboard/pages/DashboardPage.test.tsx` |
| generado | `frontend/src/features/evidence/components/EvidenceUploadUI.test.tsx` |
| generado | `frontend/src/features/phases/components/PhaseCloseAction.test.tsx` |
| generado | `frontend/src/features/processes/components/ProcessListTable.test.tsx` |
| generado | `frontend/src/features/processes/components/ProcessListView.test.tsx` |
| generado | `frontend/src/features/processes/hooks/useProcessList.test.tsx` |
| generado | `frontend/src/features/reports/components/ExecutiveReportUI.test.tsx` |
| generado | `frontend/src/features/reports/hooks/mapReportError.test.ts` |
| generado | `frontend/src/features/reports/lib/reportPreview.test.ts` |
| generado | `frontend/src/features/subphases/components/SubphaseReviewActions.test.tsx` |
| generado | `frontend/src/features/assistant/components/AssistantChatUI.test.tsx` |
| generado | `frontend/src/lib/api/mapApiError.test.ts` |
| generado | `frontend/src/lib/auth/AuthProvider.test.tsx` |
| generado | `frontend/src/lib/auth/getPostLoginPath.test.ts` |
| generado | `frontend/src/lib/auth/tokenStorage.test.ts` |
| generado | `frontend/src/lib/hooks/useLockBodyScroll.test.ts` |
| modificado | `frontend/package.json` |
| modificado | `frontend/pnpm-lock.yaml` |
| modificado | `frontend/pnpm-workspace.yaml` |
| modificado | `frontend/vite.config.ts` |
| modificado | `frontend/tsconfig.app.json` |
| modificado | `frontend/.oxlintrc.json` |
| modificado | `docs/sprints/sprint_03/PROMPT_MAPPING.md` (PM-009) |

### Cambios realizados

1. Infra Vitest + jsdom + RTL + user-event + MSW 2; scripts `test`, `test:run`, `test:coverage`.
2. Handlers MSW de login, dashboard, procesos, cierre de fase y review de subfase.
3. 86 tests unitarios colocados junto al código (auth, routing, UI, dashboard, procesos, evidencias, fases, reportes, assistant UI, validaciones admin).
4. `cleanup()` en setup para evitar contaminación de DOM entre tests (Vitest sin globals).
5. `allowBuilds.msw: true` en `pnpm-workspace.yaml` porque pnpm 10 ignora postinstall de MSW.
6. Override de oxlint `react/only-export-components` solo en `src/test/**`.
7. Sin cambios en componentes productivos.

### Validación ejecutada

- [x] `pnpm test:run` — 28 files, 86 tests, 0 failed
- [x] `pnpm test:coverage` — Statements 24.64%, Branches 24.76%, Functions 20.10%, Lines 25.76%
- [x] `pnpm lint` — oxlint exit 0
- [x] `pnpm build` — tsc + vite build OK
- [ ] Smoke Docker — N/A

### Resultado obtenido

Suite unitaria frontend en verde con MSW (sin backend/Postgres/internet). Cobertura global baja (~26 % líneas) porque quedan páginas admin, copilotos, editor de estructura y hooks de Orval sin tests.

### Riesgos/observaciones

- Gap de trazabilidad: no hay PR-IMPL formal; el usuario autorizó registro PM en Sprint 3.
- pnpm 10 exige `allowBuilds.msw` o el install falla con ERR_PNPM_IGNORED_BUILDS.
- `docker-compose.yml` tiene un cambio local de puerto Ollama (11435) no relacionado con esta tarea.

### Próximos pasos

- [ ] Tests de páginas admin (usuarios/plantillas) y editor de estructura de proceso
- [ ] Hooks de evidencia upload, reportes asíncronos y copilotos de dominio
- [ ] Subir cobertura de líneas hacia un umbral acordado
Buscador de evidencias integrado en la rama `release/2.0.0`, con suite E2E en la ubicación estándar `frontend/tests/search_evidence/search_evidence.spec.ts`.

## PM-015

| Campo | Valor |
| --- | --- |
| **ID** | PM-015 |
| **Fecha** | 2026-10-13 18:49 |
| **Solicitante** | Boris Anthony Angulo Urquieta |
| **Agente/Entorno** | Cursor Grok 4.6 — sigesa-orchestrator |
| **Tarea** | FSD-UC-004 / FSD-UC-005 — M7 DocumentAsset M:N (dedup blob, link, folio) |
| **Objetivo** | Separar el blob (`DocumentAsset`) de la evidencia 1:1 por indicador; reutilizar documento en el mismo proceso vía SHA-256; APIs EVD-01 ext / EVD-06 / EVD-07; UI modal «usar documento existente»; Flyway V19 + backfill Docker. |
| **PR-IMPL vinculado** | [PR-IMPL-M7-004](../../prompts/impl/PR-IMPL-M7-004.md) |
| **DD vinculado** | [DD-UC-004-M7](../../design/DD-UC-004-M7.md) |
| **FSD vinculado** | [FSD-UC-004](../../product/uc/FSD-UC-004.md) / [FSD-UC-005](../../product/uc/FSD-UC-005.md) |
| **Estado** | completado |

### Prompt usado exacto

```text
Implementa M7 DocumentAsset M:N en SIGESA. Los pasos 0–2 del pipeline YA ESTÁN HECHOS (no reescribas FSD/DD/PR-IMPL salvo sync menor de api_contracts/DTP al final). El usuario aprobó diseñar y orquestar: ejecuta Paso 3a → 3b → 3c → 4 review → 5 dtp-sync + save-prompt-mapping.

## Trazabilidad
FSD-UC-004 + FSD-UC-005 → DD-UC-004-M7 → PR-IMPL-M7-004
Lee ahora:
- docs/product/uc/FSD-UC-004.md
- docs/product/uc/FSD-UC-005.md
- docs/design/DD-UC-004-M7.md
- docs/prompts/impl/PR-IMPL-M7-004.md
- docs/adr/ADR-0005-workflow-metodologico-evaluacion-transversal.md
- AGENTS.md (hexagonal, Orval only, no docs/baseline/)

## Reglas duras
- Flyway **V19** (V18 YA EXISTE: V18__normative_evidence_legacy_nullable.sql). Archivo: backend/src/main/resources/db/migration/V19__document_assets_indicator_links.sql
- Dominio puro: sin Spring/JPA en domain.
- Un Evidence por indicador (uk_evidence_normative_indicator).
- Deduplicar blob por (process_id, content_hash).
- Asset de otro proceso → 403 PROGRAM_SCOPE_DENIED.
- Indicador con evidencia → 409 INDICATOR_ALREADY_HAS_EVIDENCE.
- DELETE evidencia aprobada sigue 409 EVIDENCE_IMMUTABLE; no borrar DocumentAsset compartido.
- Frontend: solo hooks Orval; tokens Tailwind institucionales; no hex arbitrarios.
- No implementes M8–M11, SMTP, ni N documentos por indicador.

## Qué construir
1. Tablas document_assets + indicator_evidence_links + backfill desde evidence/evidence_version normativas.
2. Domain DocumentAsset, IndicatorEvidenceLink + excepciones.
3. Extender UploadNormativeIndicatorEvidenceService (reuso hash, devolver documentAssetId, folioCode, reusedAsset).
4. LinkDocumentAssetToIndicatorService + GetDocumentAssetService.
5. REST: extender POST /indicators/{id}/evidences; POST /indicators/{id}/evidences/link; GET /document-assets/{id}. Actualizar OpenAPI del backend.
6. Tests unitarios + WebMvc.
7. Orval (pnpm --dir frontend generate:api) y UI modal de carga: vincular asset existente + mostrar folioCode.
8. Actualizar docs/product/api_contracts.md (API-EVD-01 ext, EVD-06, EVD-07) y DTP.md changelog.
9. ./mvnw test y pnpm --dir frontend test:run deben pasar.
10. Paso 3c Docker si compose está disponible (V19 en postgres).
11. Paso 5: @save-prompt-mapping fsd=FSD-UC-004 (PM-015 sprint_03) y dtp-sync.

## Código existente a extender (no duplicar controladores muertos)
- backend/.../UploadNormativeIndicatorEvidenceService.java
- Evidence blob/hash ports existentes
- frontend NormativeIndicatorEvidenceUploadModal.tsx y normativeIndicatorEvidenceApi.ts — migrar a Orval si aún usa wrapper; si el wrapper es el patrón del módulo, extiéndelo con tipos Orval.

folio_code: SIGESA-{yyyyMMdd}-{8 hex del contentHash}.

Devuelve al final: archivos creados/modificados, resultado de tests, PM-NNN, y huecos si los hay.
```

### Archivos generados o modificados

| Acción | Ruta |
| --- | --- |
| generado | `backend/src/main/resources/db/migration/V19__document_assets_indicator_links.sql` |
| generado | `backend/src/main/java/com/umss/sigesa/domain/model/DocumentAsset.java` |
| generado | `backend/src/main/java/com/umss/sigesa/domain/model/IndicatorEvidenceLink.java` |
| generado | `backend/src/main/java/com/umss/sigesa/domain/model/DocumentAssetView.java` |
| generado | `backend/src/main/java/com/umss/sigesa/domain/model/LinkDocumentAssetToIndicatorCommand.java` |
| generado | `backend/src/main/java/com/umss/sigesa/domain/exception/DocumentAssetNotFoundException.java` |
| generado | `backend/src/main/java/com/umss/sigesa/domain/exception/IndicatorAlreadyHasEvidenceException.java` |
| generado | `backend/src/main/java/com/umss/sigesa/application/port/out/DocumentAssetPort.java` |
| generado | `backend/src/main/java/com/umss/sigesa/application/port/in/LinkDocumentAssetToIndicatorUseCase.java` |
| generado | `backend/src/main/java/com/umss/sigesa/application/port/in/GetDocumentAssetUseCase.java` |
| generado | `backend/src/main/java/com/umss/sigesa/application/service/evidence/LinkDocumentAssetToIndicatorService.java` |
| generado | `backend/src/main/java/com/umss/sigesa/application/service/evidence/GetDocumentAssetService.java` |
| generado | `backend/src/main/java/com/umss/sigesa/adapter/out/persistance/entity/DocumentAssetEntity.java` |
| generado | `backend/src/main/java/com/umss/sigesa/adapter/out/persistance/entity/IndicatorEvidenceLinkEntity.java` |
| generado | `backend/src/main/java/com/umss/sigesa/adapter/out/persistance/repository/SpringDataDocumentAssetRepository.java` |
| generado | `backend/src/main/java/com/umss/sigesa/adapter/out/persistance/repository/SpringDataIndicatorEvidenceLinkRepository.java` |
| generado | `backend/src/main/java/com/umss/sigesa/adapter/out/persistance/DocumentAssetJpaAdapter.java` |
| generado | `backend/src/main/java/com/umss/sigesa/adapter/in/web/DocumentAssetController.java` |
| generado | `backend/src/main/java/com/umss/sigesa/adapter/in/web/dto/DocumentAssetResponseDto.java` |
| generado | `backend/src/main/java/com/umss/sigesa/adapter/in/web/dto/LinkDocumentAssetRequestDto.java` |
| generado | `backend/src/test/java/com/umss/sigesa/application/service/evidence/LinkDocumentAssetToIndicatorServiceTest.java` |
| generado | `backend/src/test/java/com/umss/sigesa/application/service/evidence/GetDocumentAssetServiceTest.java` |
| generado | `backend/src/test/java/com/umss/sigesa/adapter/in/web/DocumentAssetControllerWebMvcTest.java` |
| generado | `backend/src/test/java/com/umss/sigesa/adapter/in/web/EvidenceControllerWebMvcTest.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/application/service/evidence/UploadNormativeIndicatorEvidenceService.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/application/port/out/EvidenceUploadPersistencePort.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/adapter/out/persistance/EvidenceUploadJpaAdapter.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/adapter/out/persistance/NormativeIndicatorEvidenceJpaAdapter.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/adapter/in/web/EvidenceController.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/adapter/in/web/advice/EvidenceExceptionHandler.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/adapter/in/web/dto/UploadEvidenceResponse.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/adapter/in/web/dto/NormativeIndicatorEvidenceResponseDto.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/config/EvidenceModuleConfig.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/config/DevWorkflowSchemaAlignmentRunner.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/domain/model/NormativeIndicatorEvidenceItem.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/domain/model/NormativeIndicatorEvidenceUploadResult.java` |
| modificado | `backend/src/test/java/com/umss/sigesa/application/service/evidence/UploadNormativeIndicatorEvidenceServiceTest.java` |
| modificado | `backend/src/test/java/com/sigesa/app/contracts/evidence/EvidenceJsonContractTest.java` |
| generado | `frontend/src/api/endpoints/document-assets/` |
| generado | `frontend/src/api/model/documentAssetResponseDto.ts` |
| generado | `frontend/src/api/model/linkDocumentAssetRequestDto.ts` |
| generado | `frontend/src/api/model/listByProcessParams.ts` |
| generado | `frontend/src/api/model/uploadEvidenceParams.ts` |
| generado | `frontend/src/features/evidence/components/NormativeIndicatorEvidenceUploadModal.test.tsx` |
| modificado | `frontend/src/features/evidence/api/normativeIndicatorEvidenceApi.ts` |
| modificado | `frontend/src/features/evidence/components/NormativeIndicatorEvidenceUploadModal.tsx` |
| modificado | `frontend/src/features/evidence/hooks/mapUploadError.ts` |
| modificado | `frontend/src/features/processes/components/ProcessNormativeTree.tsx` |
| modificado | `frontend/src/features/processes/components/NormativeIndicatorCollaborationSection.tsx` |
| modificado | `frontend/src/features/processes/components/NormativeIndicatorEvidenceUploadSlot.tsx` |
| modificado | `frontend/src/api/endpoints/evidence/evidence.ts` (Orval) |
| modificado | `docs/product/api_contracts.md` |
| modificado | `docs/product/DTP.md` |
| modificado | `docs/product/modelo_datos.md` |
| modificado | `docs/product/FSD.md` |
| modificado | `docs/product/uc/FSD-UC-004.md` |
| modificado | `docs/product/uc/FSD-UC-005.md` |
| generado | `docs/design/DD-UC-004-M7.md` |
| generado | `docs/prompts/impl/PR-IMPL-M7-004.md` |
| modificado | `docs/sprints/sprint_03/PROMPT_MAPPING.md` |

### Cambios realizados

1. **Flyway V19 + backfill:** tablas `document_assets` / `indicator_evidence_links`; backfill desde evidencias normativas. Docker dev usa Hibernate `ddl-auto:update` + `DevWorkflowSchemaAlignmentRunner.backfillDocumentAssets()`.
2. **Dominio hexagonal:** `DocumentAsset`, `IndicatorEvidenceLink` sin Spring/JPA; puertos IN/OUT; excepciones `INDICATOR_ALREADY_HAS_EVIDENCE` / `DOCUMENT_ASSET_NOT_FOUND`.
3. **Upload extendido:** hash-first, reuso de blob por `(process_id, content_hash)`, persistencia de asset+link, respuesta `documentAssetId`/`folioCode`/`reusedAsset`.
4. **Link + GET:** `POST /indicators/{id}/evidences/link` (403 si asset de otro proceso; 409 si indicador ocupado); `GET /document-assets/{id}` y listado `?processId=` para el modal.
5. **Frontend:** wrapper Orval; checkbox «Usar documento ya cargado en este proceso»; folio en lista; códigos de error mapeados.
6. **Docs:** API-EVD-01 ext, EVD-06, EVD-07; DTP changelog 13/10/2026 + B.4.2.

### Validación ejecutada

- [x] `cd backend && ./mvnw test` — BUILD SUCCESS
- [x] `pnpm --dir frontend test:run` — 81 tests passed
- [x] `pnpm --dir frontend generate:api` — Orval OK
- [x] Smoke Docker `:8080` — POST upload 201; POST link `reusedAsset:true`; GET asset con dos `linkedIndicatorIds`; segundo upload al mismo indicador 409 `INDICATOR_ALREADY_HAS_EVIDENCE`
- [ ] DELETE evidencia normativa aprobada → 409 `EVIDENCE_IMMUTABLE` (observado 404 `EVIDENCE_NOT_FOUND` en lookup de ciclo de vida; no es M7)

### Resultado obtenido

M7 DocumentAsset M:N operativo en backend, OpenAPI, Orval y modal de carga. Un blob puede vincularse a varios indicadores del mismo proceso; cada indicador sigue con una sola `Evidence`. Folio `SIGESA-{yyyyMMdd}-{8 hex}`.

### Riesgos/observaciones

- DELETE de evidencia normativa no entra por el caso de uso de ciclo de vida (404). Fuera de alcance M7; no se borra `DocumentAsset` compartido.
- N documentos por indicador y M8 FODA quedan explícitamente fuera.
- Orval regeneró también modelos incidentales (`pageableObject`, dashboard, procesos).

### Próximos pasos

- [ ] M8 FODA
- [ ] Alinear DELETE evidencia normativa con 409 `EVIDENCE_IMMUTABLE`
- [ ] N documentos por el mismo indicador (post-M7)

---

## PM-016

| Campo | Valor |
|---|---|
| **ID** | PM-016 |
| **Fecha** | 2026-10-13 |
| **Hora** | 19:21 |
| **Solicitante** | Boris Anthony Angulo Urquieta |
| **Agente/Entorno** | sigesa-orchestrator · Cursor Grok 4.6 · Docker Compose `:3000`/`:8080` |
| **Modelo** | Cursor Grok 4.6 |
| **Tarea** | Implementar M8 full-stack: FODA por indicador, métricas, lotes de encuesta y compuertas E2→E3 / cierre E3 |
| **Objetivo** | Operativizar FSD-UC-029 + FSD-UC-028 (M8) sin reabrir M6/M7 salvo dependencias (gate E2, DocumentAsset) |
| **Contexto** | Pasos 0–2 ya aprobados (FSD, DD-UC-029, PR-IMPL-M8-029). Pipeline continuo 3a→3b→3c→4→5. Sin commits. |
| **PR-IMPL vinculado** | PR-IMPL-M8-029 |
| **DD-UC vinculado** | DD-UC-029 |
| **FSD-UC vinculado** | FSD-UC-029 · FSD-UC-028 |
| **Estado** | completado |

### Prompt usado exacto

```text
Implementa full-stack SIGESA 2.1 **M8** (FODA + compuerta E2→E3 + métricas). El usuario ya aprobó el pipeline completo (mismo criterio que M7). **No pauses** pidiendo confirmación de DD/PR-IMPL: ya existen y están listos. Ejecuta Pasos 3a → 3b → 3c → 4 → 5.

Trazabilidad resuelta: FSD-UC-029 → DD-UC-029 → PR-IMPL-M8-029 (también FSD-UC-028 M8).

Alcance IN: Flyway V20 indicator_analysis; dominio IndicatorAnalysis/FodaQuadrant/ComplianceJudgment; EvaluationMetricsPort.countFodaCompleteByProcessId; Upsert/Get analysis; GetEvaluationMetrics; Register/List PrimarySurveyBatch; StageDeliverableCode FODA_FORMS_COMPLETE + SYNTHESIS_MATRIX; MethodologicalStageBootstrapper ensure; StageGateEvaluator COLLECTION + SYSTEMATIZATION; REST API-WF-09…12; frontend Orval (métricas, FODA, encuesta); tests; docs post-código.

Alcance OUT: CAE_MEMBER, IndicatorDelegation, ImprovementPlanItem, M9–M11, docs/baseline/**.

Paso 5: @dtp-sync fsd=FSD-UC-029; @save-prompt-mapping sprint=03 fsd=FSD-UC-029 solicitante="Boris Anthony Angulo Urquieta" estado=completado. Siguiente PM es PM-016.
```

### Entradas auxiliares

- `docs/product/uc/FSD-UC-029.md`
- `docs/product/uc/FSD-UC-028.md`
- `docs/design/DD-UC-029.md`
- `docs/prompts/impl/PR-IMPL-M8-029.md`
- `docs/adr/ADR-0005-workflow-metodologico-evaluacion-transversal.md`

### Archivos generados o modificados

| Acción | Ruta |
|---|---|
| generado | `backend/src/main/resources/db/migration/V20__indicator_analysis_e3_deliverables.sql` |
| generado | `backend/src/main/java/com/umss/sigesa/domain/model/{IndicatorAnalysis,FodaQuadrant,ComplianceJudgment,PrimarySurveyBatch,EvaluationMetrics,UpsertIndicatorAnalysisCommand,RegisterPrimarySurveyBatchCommand}.java` |
| generado | `backend/src/main/java/com/umss/sigesa/domain/exception/{IndicatorAnalysisNotFoundException,ValidationException}.java` |
| generado | `backend/src/main/java/com/umss/sigesa/application/port/in/{UpsertIndicatorAnalysisUseCase,GetIndicatorAnalysisUseCase,GetEvaluationMetricsUseCase,RegisterPrimarySurveyBatchUseCase,ListPrimarySurveyBatchesUseCase}.java` |
| generado | `backend/src/main/java/com/umss/sigesa/application/port/out/{IndicatorAnalysisPort,PrimarySurveyBatchPort}.java` |
| generado | `backend/src/main/java/com/umss/sigesa/application/service/evaluation/` |
| generado | `backend/src/main/java/com/umss/sigesa/adapter/in/web/{EvaluationMetricsController,IndicatorAnalysisController,SurveyBatchController}.java` |
| generado | `backend/src/main/java/com/umss/sigesa/adapter/in/web/advice/EvaluationExceptionHandler.java` |
| generado | `backend/src/main/java/com/umss/sigesa/adapter/in/web/dto/{EvaluationMetricsResponseDto,IndicatorAnalysisResponseDto,UpsertIndicatorAnalysisRequestDto,PrimarySurveyBatchResponseDto,RegisterSurveyBatchRequestDto}.java` |
| generado | `backend/src/main/java/com/umss/sigesa/adapter/out/persistance/{IndicatorAnalysisJpaAdapter,PrimarySurveyBatchJpaAdapter}.java` |
| generado | `backend/src/main/java/com/umss/sigesa/adapter/out/persistance/entity/IndicatorAnalysisEntity.java` |
| generado | `backend/src/main/java/com/umss/sigesa/adapter/out/persistance/repository/SpringDataIndicatorAnalysisRepository.java` |
| generado | `backend/src/main/java/com/umss/sigesa/config/EvaluationModuleConfig.java` |
| generado | `backend/src/test/java/com/umss/sigesa/adapter/in/web/EvaluationControllerWebMvcTest.java` |
| generado | `backend/src/test/java/com/umss/sigesa/application/service/evaluation/` |
| generado | `backend/src/test/java/com/umss/sigesa/application/service/workflow/MethodologicalStageBootstrapperTest.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/domain/model/StageDeliverableCode.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/application/port/out/{EvaluationMetricsPort,MethodologicalStagePort}.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/application/service/workflow/{StageGateEvaluator,MethodologicalStageBootstrapper,ListProcessStagesService,EvaluateStageGateService,ApproveStageService}.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/adapter/out/persistance/{EvaluationMetricsJpaAdapter,MethodologicalStageJpaAdapter}.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/config/{WorkflowModuleConfig,DevWorkflowSchemaAlignmentRunner}.java` |
| modificado | `backend/src/test/java/com/umss/sigesa/application/service/workflow/StageGateEvaluatorTest.java` |
| generado | `frontend/src/api/endpoints/evaluation/` |
| generado | `frontend/src/api/model/{evaluationMetricsResponseDto,indicatorAnalysisResponseDto,primarySurveyBatchResponseDto,registerSurveyBatchRequestDto,upsertIndicatorAnalysisRequestDto*}.ts` |
| generado | `frontend/src/features/processes/components/{EvaluationMetricsPanel,EvaluationMetricsPanelContainer,IndicatorFodaForm,IndicatorFodaSection,CollectionSurveyBatchForm}.tsx` |
| generado | `frontend/src/features/processes/components/{EvaluationMetricsPanel,IndicatorFodaForm}.test.tsx` |
| modificado | `frontend/src/features/processes/components/{ProcessDetailView,ProcessNormativeTree,MethodologicalStageTimeline,MethodologicalStageTimelineContainer}.tsx` |
| modificado | `docs/product/{api_contracts.md,DTP.md,FSD.md}` |
| modificado | `docs/product/uc/{FSD-UC-028.md,FSD-UC-029.md}` |
| modificado | `docs/sprints/sprint_03/PROMPT_MAPPING.md` |

### Cambios realizados

1. **Flyway V20:** `indicator_analysis` (UNIQUE `indicator_id`, CHECKs FODA/compliance); drop CHECK enum V17 de `audience`; backfill entregables E3.
2. **Dominio hexagonal + servicios:** upsert/get FODA, métricas, register/list survey; `EvaluationAccessGuard` (scope CC); `countFodaCompleteByProcessId`.
3. **Compuertas:** E2 mantenida; E3 nueva (FODA 100% o BLOCK si 0 indicadores + E3 APPROVED). Bootstrapper `ensure` + `GET /stages` llama `bootstrapForProcess` (procesos seed sin etapas).
4. **REST:** API-WF-09…12; PUT FODA solo CC; asset de otro proceso → 403 `PROGRAM_SCOPE_DENIED`.
5. **Frontend Orval:** panel métricas, form FODA (edita CC), lote encuesta en E2 COLLECTION.
6. **Docs:** API-WF-09…12; DTP A.1/A.3/B.4.3; FSD-UC-028/029 **Hecho (M8)**.

### Validación ejecutada

- [x] `cd backend && ./mvnw test` — 427 tests, 0 failures, 3 skipped
- [x] `pnpm --dir frontend test:run` — 85 passed
- [x] `pnpm --dir frontend` oxlint + `tsc -b` — OK
- [x] `pnpm --dir frontend generate:api` — Orval OK (`evaluation`)
- [x] Smoke Docker `:8080` proceso `950e8400-e29b-41d4-a716-446655440020`:
  - GET metrics 200 (7 inds, 4 evidencia, 1 FODA, 2 lotes)
  - PUT FODA CC 200; PUT TD 403 `ACCESS_DENIED`
  - POST survey `DOCENTES`/8 → 201; GET surveys 200
  - GET stages 200 (7 etapas; E3 `FODA_FORMS_COMPLETE` + `SYNTHESIS_MATRIX`)
  - GET gate COLLECTION 200 BLOCK (4/7 evidencia + E2 PENDING)
  - GET gate SYSTEMATIZATION 200 BLOCK (1/7 FODA + E3 PENDING)
- [x] Smoke UI `:3000` CC: login → detalle proceso → panel «Completitud de evaluación»; FODA del indicador LEG-E20F9F1E con Fortaleza/Cumple + síntesis guardada. Form encuesta no visible (etapa activa = E1, no COLLECTION).

### Resultado obtenido

M8 operativo: FODA 1:1 por indicador, métricas, lotes de encuesta y preview de compuertas E2/E3. Procesos viejos sin etapas se bootstrapean al listar. `docs/baseline/` intacto.

### Riesgos/observaciones

- PUT no-CC responde 403 `ACCESS_DENIED` (`@PreAuthorize`), no `FORBIDDEN_ROLE`.
- `GET …/stages` tiene efecto de escritura (`bootstrapForProcess`) para no dejar procesos seed sin E1–E7/E3.
- `modelo_datos.md` §11.5 aún lista `IndicatorDelegation` como M8; está fuera de este corte (CAE).
- Working tree mezclado con archivos M7 (PM-015) aún sin commit.

### Próximos pasos

- [ ] M9 Plan de Mejoras (`ImprovementPlanItem`)
- [ ] N documentos por el mismo indicador
- [ ] Commit/PR M7+M8 cuando el usuario lo pida

---

## PM-017

| Campo | Valor |
|---|---|
| **ID** | PM-017 |
| **Fecha** | 2026-10-13 |
| **Hora** | 20:26 |
| **Solicitante** | Boris Anthony Angulo Urquieta |
| **Agente/Entorno** | sigesa-orchestrator · Cursor Grok 4.6 · Docker Compose `:3000`/`:8080` |
| **Modelo** | Cursor Grok 4.6 |
| **Tarea** | Implementar M9 full-stack: Plan de Mejoras, documentos E4 (PDF borrador) y compuerta E4→E5 |
| **Objetivo** | Operativizar FSD-UC-031 + FSD-UC-028 (M9 DRAFTING) sin reabrir M6–M8 salvo dependencias (FODA, DocumentAsset, ensure deliverables) |
| **Contexto** | Pasos 0–2 ya aprobados (FSD, DD-UC-031, PR-IMPL-M9-031). Pipeline continuo 3a→3b→3c→4→5. Sin commits. |
| **PR-IMPL vinculado** | PR-IMPL-M9-031 |
| **DD-UC vinculado** | DD-UC-031 |
| **FSD-UC vinculado** | FSD-UC-031 · FSD-UC-028 |
| **Estado** | completado |

### Prompt usado exacto

```text
Implementa full-stack SIGESA 2.1 **M9** (Plan de Mejoras + documentos E4 + compuerta E4→E5). El usuario aprobó el pipeline completo (igual que M7/M8). **No pauses** pidiendo confirmación DD/PR-IMPL: ya existen. Ejecuta Pasos 3a → 3b → 3c → 4 → 5.

Repo: `/home/boris/Projects/sigesa-acreditacion-umss`

## Trazabilidad
FSD-UC-031 → DD-UC-031 → PR-IMPL-M9-031 (también FSD-UC-028 M9 E4).

Pasos 0–2 ya hechos. No reescribas FSD salvo sync post-código (Hecho + api_contracts + DTP).

## Paso 5
@dtp-sync fsd=FSD-UC-031
@save-prompt-mapping sprint=03 fsd=FSD-UC-031 solicitante="Boris Anthony Angulo Urquieta" estado=completado
Siguiente PM = **PM-017** (PM-016 fue M8).
```

### Entradas auxiliares

- `docs/product/uc/FSD-UC-031.md`
- `docs/product/uc/FSD-UC-028.md`
- `docs/design/DD-UC-031.md`
- `docs/prompts/impl/PR-IMPL-M9-031.md`
- `docs/adr/ADR-0005-workflow-metodologico-evaluacion-transversal.md`

### Archivos generados o modificados

| Acción | Ruta |
|---|---|
| generado | `backend/src/main/resources/db/migration/V21__improvement_plan_e4_deliverables.sql` |
| generado | `backend/src/main/java/com/umss/sigesa/domain/model/ImprovementPlanItem.java` |
| generado | `backend/src/main/java/com/umss/sigesa/application/service/evaluation/ImportImprovementPlanFromFodaService.java` |
| generado | `backend/src/main/java/com/umss/sigesa/application/service/workflow/GenerateStageDeliverableDocumentService.java` |
| generado | `backend/src/main/java/com/umss/sigesa/adapter/out/report/OpenPdfDraftingDocumentRendererAdapter.java` |
| generado | `backend/src/main/java/com/umss/sigesa/adapter/in/web/ImprovementPlanController.java` |
| generado | `frontend/src/features/processes/components/ImprovementPlanPanel.tsx` |
| generado | `frontend/src/features/processes/components/ImprovementPlanPanelContainer.tsx` |
| modificado | `backend/src/main/java/com/umss/sigesa/domain/model/StageDeliverableCode.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/application/service/workflow/StageGateEvaluator.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/application/service/workflow/ApproveStageDeliverableService.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/adapter/in/web/StageWorkflowController.java` |
| modificado | `frontend/src/features/processes/components/MethodologicalStageTimeline.tsx` |
| modificado | `frontend/src/features/processes/components/ProcessDetailView.tsx` |
| modificado | `docs/product/api_contracts.md` |
| modificado | `docs/product/DTP.md` |
| modificado | `docs/product/FSD.md` |
| modificado | `docs/product/uc/FSD-UC-031.md` |
| modificado | `docs/product/uc/FSD-UC-028.md` |

(Lista completa en `git status --short` de esta sesión: dominio/puertos/servicios/DTOs/tests M9 + Orval `evaluation`/`stage-workflow` + `modelo_datos.md` V21.)

### Cambios realizados

1. **V21** `improvement_plan_items` + backfill E4; Hibernate/dev: `DevWorkflowSchemaAlignmentRunner` + UNIQUE parcial PostgreSQL.
2. **Dominio/servicios:** CRUD + import FODA solo `WEAKNESS` (idempotente); `GenerateStageDeliverableDocument` vía `DraftingDocumentRendererPort` (OpenPDF, no `PdfRendererPort`); `documentAssetId` en `StageDeliverable`/DTO timeline.
3. **Gate:** `evaluateDrafting` exige 4 E4 con asset + APPROVED; aprobar E4 sin PDF → `409 DELIVERABLE_WITHOUT_DOCUMENT`.
4. **REST:** API-WF-13…17; generate 201 `{deliverableId, documentAssetId, folioCode, title}`.
5. **Frontend Orval:** sección Plan de Mejoras (tabla, import, alta/edición CC); botón «Generar borrador» en E4 de DRAFTING.
6. **Docs:** API-WF-13…17; DTP A.1/A.3/B.4.4; FSD-UC-031/028 **Hecho (M9)**.

### Validación ejecutada

- [x] `cd backend && JAVA_HOME=<jdk21> ./mvnw test` — verde (suite completa post-fix PDF; 0 failures)
- [x] `pnpm --dir frontend test:run` — 89 passed (31 files)
- [x] `pnpm --dir frontend` oxlint + `tsc -b` — OK
- [x] `pnpm --dir frontend generate:api` — Orval OK (`listImprovementPlan`, `generateStageDeliverable`)
- [x] Smoke Docker `:8080` proceso `950e8400-e29b-41d4-a716-446655440020`:
  - GET `/improvement-plan` 200 `[]` luego 1 ítem
  - POST import-from-foda 200 `{importedCount:0, skippedCount:0}` (seed sin WEAKNESS)
  - POST item 201 `Smoke M9 mejorar laboratorios`
  - POST generate IMPROVEMENT_PLAN 201 folio `SIGESA-20261014-ea7f3a48`
  - GET gate DRAFTING 200 `pass:false` (BLOCK; faltan 3 PDF + 4 APPROVED)
- [x] Smoke UI `:3000` CC: detalle proceso → sección «Plan de Mejoras» + Importar/Editar/Añadir; timeline E1 activa (Generar borrador solo visible cuando DRAFTING es etapa activa).

### Resultado obtenido

M9 operativo: plan persistido, import FODA idempotente, PDF E4 institucional con folio, timeline con `documentAssetId`/`folioCode`, compuerta DRAFTING BLOCK/PASS según reglas. `docs/baseline/` intacto.

### Riesgos/observaciones

- Unicidad `(process_id, source_indicator_id)` parcial: índice PostgreSQL; H2/tests validan duplicado en servicio.
- Generate no exige `IN_PROGRESS` (solo `stage.code==DRAFTING` + código E4), alineado al contrato.
- Import 0 en seed: no hay análisis `WEAKNESS`; cubierto por test unitario de import.
- `StageWorkflowController` consulta `DocumentAssetPort` para folio en GET stages (enriquecimiento de presentación).

### Próximos pasos

- [ ] M10 freeze / Sala de Pares
- [ ] M11 ciclo E7 / `IMPROVEMENT_MONITORING`
- [ ] Commit/PR M9 cuando el usuario lo pida

---

## PM-018

| Campo | Valor |
|---|---|
| **ID** | PM-018 |
| **Fecha** | 2026-10-13 |
| **Hora** | 21:10 |
| **Solicitante** | Boris Anthony Angulo Urquieta |
| **Agente/Entorno** | Cursor IDE — sigesa-orchestrator |
| **Modelo** | Cursor Grok 4.6 |
| **Tarea** | Cierre SIGESA 2.1: M10 freeze/Sala de Pares, M11 ciclo E7, CAE_MEMBER |
| **Objetivo** | Congelar expediente AUDIT_READONLY, checklist Sala de Pares, gates E5–E7, justificación de plan, rol CAE con delegaciones, UI + Orval + Docker smoke |
| **Contexto** | Pipeline aprobado 3a→3b→3c→4→5 sin pausas. V21 existía; un solo Flyway `V22`. Ancla FSD-UC-030 / DD-UC-030 / PR-IMPL-M10-030 (también UC-020, UC-028, UC-031, CAE sobre UC-004/029). |
| **PR-IMPL vinculado** | [PR-IMPL-M10-030](../../prompts/impl/PR-IMPL-M10-030.md) |
| **DD-UC vinculado** | [DD-UC-030](../../design/DD-UC-030.md) |
| **FSD-UC vinculado** | FSD-UC-030 · FSD-UC-020 · FSD-UC-028 · FSD-UC-031 |
| **Estado** | completado |

### Prompt usado exacto

```text
Implementa full-stack el **cierre SIGESA 2.1**: M10 freeze/Sala de Pares, M11 ciclo E7, y CAE_MEMBER. Usuario aprobó pipeline completo. **No pauses** por DD/PR-IMPL. Pasos 3a→3b→3c→4→5.

Repo: `/home/boris/Projects/sigesa-acreditacion-umss`

## Trazabilidad
FSD-UC-030 → DD-UC-030 → PR-IMPL-M10-030 (también FSD-UC-020, FSD-UC-031 M11, FSD-UC-028 E5–E7, CAE sobre UC-004/029).

Lee: `docs/design/DD-UC-030.md`, `docs/prompts/impl/PR-IMPL-M10-030.md`, `docs/product/uc/FSD-UC-030.md`, `AGENTS.md`, skills generate-frontend-feature, code-reviewer, dtp-sync, save-prompt-mapping.
**Nunca** `docs/baseline/`.

Listar Flyway: siguiente **V22** (V21 existe). Un solo archivo `V22__freeze_peer_room_e7_cae.sql`.
…
Paso 5
@dtp-sync fsd=FSD-UC-030
@save-prompt-mapping sprint=03 fsd=FSD-UC-030 solicitante="Boris Anthony Angulo Urquieta" estado=completado
**PM-018** (017 fue M9). Marcar FSD UC-030/020/028/031 Hecho M10/M11. CAE ya no planificado.
**No commit.**
```

### Entradas auxiliares

- `docs/design/DD-UC-030.md`
- `docs/prompts/impl/PR-IMPL-M10-030.md`
- `docs/product/uc/FSD-UC-030.md` · `FSD-UC-020.md` · `FSD-UC-028.md` · `FSD-UC-031.md`
- `docs/adr/ADR-0005-workflow-metodologico-evaluacion-transversal.md`

### Archivos generados o modificados

| Acción | Ruta |
|---|---|
| generado | `backend/src/main/resources/db/migration/V22__freeze_peer_room_e7_cae.sql` |
| generado | `backend/src/main/java/com/umss/sigesa/adapter/in/web/ProcessFreezeController.java` |
| generado | `backend/src/main/java/com/umss/sigesa/adapter/in/web/PeerRoomChecklistController.java` |
| generado | `backend/src/main/java/com/umss/sigesa/adapter/in/web/IndicatorDelegationController.java` |
| generado | `backend/src/main/java/com/umss/sigesa/application/service/workflow/ProcessFreezeGuard.java` |
| generado | `backend/src/main/java/com/umss/sigesa/application/service/evaluation/CaeDelegationGuard.java` |
| generado | `frontend/src/features/processes/components/ProcessFreezePanel.tsx` |
| generado | `frontend/src/features/processes/components/PeerRoomChecklistPanel.tsx` |
| generado | `frontend/src/features/processes/components/DelegationsPanel.tsx` |
| modificado | `backend/src/main/java/com/umss/sigesa/domain/model/Role.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/application/service/workflow/StageGateEvaluator.java` |
| modificado | `frontend/src/lib/auth/roleLabels.ts` |
| modificado | `frontend/src/features/processes/components/ProcessDetailView.tsx` |
| modificado | `docs/product/FSD.md` · `DTP.md` · `api_contracts.md` |

(Lista completa: `git status --short` de esta sesión — ~208 rutas; incluye V21/M9 residual + Orval.)

### Cambios realizados

1. **V22** `process_freeze`, `peer_room_checklist_items`, `indicator_delegations`, `app_user.role` VARCHAR(20)+`CAE_MEMBER`, columnas M11 del plan, backfill E5–E7. Dev: Hibernate + `DevWorkflowSchemaAlignmentRunner`.
2. **Freeze:** POST TD upsert `AUDIT_READONLY`; lift `ACTIVE`/`IMPROVEMENT_MONITORING`; activo = `lifted_at IS NULL && mode==AUDIT_READONLY`. `ProcessFreezeGuard` 409 `PROCESS_FROZEN` en escrituras eval; monitoreo permite PUT plan + generate E7.
3. **Gates:** PRESENTATION = E5 APPROVED + freeze activo; EXTERNAL_PREP = freeze + E6 APPROVED + checklist; POST_ACCREDITATION = justificación de vencidos + E7 asset+APPROVED. Aprobar `PEER_ROOM_CHECKLIST` incompleto → 409.
4. **CAE:** `Role.CAE_MEMBER` con `programId`; delegaciones N1/N2/indicador; `CaeDelegationGuard` 403 `DELEGATION_DENIED`. EE/CAE leen proceso como CC; EE GET ok, PUT eval 403.
5. **Frontend Orval:** badge `operationalMode`; freeze/lift TD; checklist EE/TD; delegaciones CC; justificación de plan; mutate oculto en freeze salvo monitoreo.
6. **Docs:** UC-030/020/028/031 Hecho M10/M11; API-WF-18…23; CAE ya no planificado.

### Validación ejecutada

- [x] `cd backend && ./mvnw test` — verde (suite completa; fallos iniciales de stubbing/generate corregidos)
- [x] `pnpm --dir frontend test:run` — 98 passed (35 files)
- [x] `pnpm --dir frontend` oxlint + `tsc -b` — OK
- [x] `pnpm --dir frontend generate:api` — Orval OK (`useFreezeProcess`, `useGetPeerRoomChecklist`, `useListDelegations`, …)
- [x] Smoke Docker `:8080` proceso `950e8400-e29b-41d4-a716-446655440020`:
  - POST freeze TD → **201** `AUDIT_READONLY` `active:true`
  - GET freeze TD/EE → **200**
  - PUT FODA CC → **409 PROCESS_FROZEN**
  - GET analysis EE → **200**; PUT analysis EE → **403**
  - GET plan/checklist EE → **200** (4 ítems seed)
- [x] Smoke UI `:3000` CC: badge/estado «Freeze AUDIT_READONLY activo»; checklist 4 ítems disabled; panel Delegaciones CAE; Plan sin importar; timeline E1–E7.

### Resultado obtenido

Cierre 2.1 operativo: freeze bloquea escritura, EE lee, checklist se siembra al congelar, gates E5–E7 implementados, CAE con alcance delegado, UI y contratos alineados. `docs/baseline/` intacto.

### Riesgos/observaciones

- GET freeze sin fila → 404 `FREEZE_NOT_FOUND` (UI trata 404 como «sin freeze»).
- No hay usuario seed `CAE_MEMBER`; smoke CAE 403/200 por delegación no ejecutado en Docker (cubierto por `CaeDelegationGuardTest` / `CreateDelegationServiceTest` / `RegisterUserServiceTest`).
- Proceso INF-SIS quedó **congelado** tras el smoke; TD puede levantar con `POST .../freeze/lift`.
- Checklist GET no incluye CAE (403); panel oculto para ese rol.

### Próximos pasos

- [ ] Commit/PR cuando el usuario lo pida (no commit en esta sesión)
- [ ] Seed opcional `CAE_MEMBER` para demo UI de delegación

---

## PM-019

| Campo | Valor |
| --- | --- |
| **ID** | PM-019 |
| **Fecha** | 2026-10-17 |
| **Hora** | 20:46 |
| **Solicitante** | Boris Anthony Angulo Urquieta |
| **Agente/Entorno** | Cursor IDE — Agent |
| **Modelo** | Cursor Grok 4.6 |
| **Tarea** | Implementar GitHub #50 FEAT-01 (desacople I/O reportes + Hikari) en `feat/50-hikari-report-io` |
| **Objetivo** | Liberar conexiones Hikari antes de generar/escribir PDF/XLSX/CSV; declarar pool en YAML; tests de frontera TX |
| **Contexto** | Auditoría CON-03 FALLA; issue https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss/issues/50; release 8.1.0 Demo 1 |
| **PR-IMPL vinculado** | [PR-IMPL-040](../../prompts/impl/PR-IMPL-040.md) |
| **DD-UC vinculado** | [DD-UC-011](../../design/DD-UC-011.md) · [DD-UC-014](../../design/DD-UC-014.md) |
| **FSD-UC vinculado** | FSD-UC-011 · FSD-UC-014 |
| **Estado** | completado |

### Prompt usado exacto

```text
implementa #50 en feat/50-hikari-report-io” documenta todo el proceso donde sea necesario y que tenga coherencia con la documentacion actual, registra los prompts, agrega los cambios a sprint_03
```

### Entradas auxiliares

- `docs/prompts/impl/PR-IMPL-040.md`
- `docs/design/DD-UC-011.md`
- `docs/design/DD-UC-014.md`
- `docs/auditoria/concurrencia-boris.md` (CON-03)
- GitHub issue #50

### Archivos generados o modificados

| Acción | Ruta |
| --- | --- |
| generado | `backend/src/main/java/com/umss/sigesa/application/port/out/TransactionRunnerPort.java` |
| generado | `backend/src/main/java/com/umss/sigesa/adapter/out/persistance/SpringTransactionRunnerAdapter.java` |
| generado | `backend/src/test/java/com/umss/sigesa/application/port/out/DirectTransactionRunner.java` |
| generado | `docs/prompts/impl/PR-IMPL-040.md` |
| modificado | `backend/src/main/java/com/umss/sigesa/application/service/report/ReportExportJobService.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/application/service/report/ProcessReportJobService.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/config/ReportModuleConfig.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/adapter/out/persistance/JpaReportExportJobAdapter.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/adapter/out/persistance/ReportJobJpaAdapter.java` |
| modificado | `backend/src/main/resources/application.yaml` |
| modificado | `backend/src/main/resources/application-dev.yaml` |
| modificado | `backend/src/main/resources/application-prod.yaml` |
| modificado | `backend/src/test/resources/application-test.yaml` |
| modificado | `backend/src/test/java/com/umss/sigesa/application/service/report/ReportExportJobServiceTest.java` |
| modificado | `backend/src/test/java/com/umss/sigesa/application/service/report/ProcessReportJobServiceTest.java` |
| modificado | `backend/src/test/java/com/umss/sigesa/e2e/ReportExportAsyncE2EIT.java` |
| modificado | `docs/product/FSD.md` |
| modificado | `docs/product/uc/FSD-UC-011.md` |
| modificado | `docs/product/uc/FSD-UC-014.md` |
| modificado | `docs/product/DTP.md` |
| modificado | `docs/design/DD-UC-011.md` |
| modificado | `docs/design/DD-UC-014.md` |
| modificado | `docs/auditoria/concurrencia-boris.md` |
| modificado | `docs/sprints/sprint_03/PROMPT_MAPPING.md` |

### Cambios realizados

1. TX cortas vía `TransactionRunnerPort` / `SpringTransactionRunnerAdapter`; `generateReport`, `render` y `store` fuera de TX write.
2. Hikari explícito (pool 10, timeout 30s, leak 20s) en YAML default/dev/prod/test; log E2E muestra `sigesa-hikari - Starting`.
3. Tests de frontera TX + E2E sin `@Transactional` de clase; persistencia de job terminal.
4. Docs vivos + `PR-IMPL-040`; veredicto CON-03 histórico `FALLA` intacto, nota de remediación 8.1.0.

### Validación ejecutada

- [x] `cd backend && ./mvnw -Dtest=ReportExportJobServiceTest,ProcessReportJobServiceTest,ReportExportAsyncE2EIT test` — verde (exit 0)
- [ ] `pnpm run lint` — N/A (solo backend)

### Resultado obtenido

Issue #50 implementado en `feat/50-hikari-report-io`. Contratos REST sin cambio. Hexágono: I/O no retiene Hikari; stream JPA se consume en TX read-only.

### Riesgos/observaciones

- El stream de observaciones se materializa a `List` en TX read (trade-off vs 1M streaming de DD-UC-011; volumen demo 8.1.0).
- `@Async` permanece en `ReportExportJobService` (Spring en aplicación; mismo patrón previo).
- 503 por pool exhausted no implementado (fuera de #50).

### Próximos pasos

- [ ] Commit/PR `Fixes #50` cuando el usuario lo pida
- [ ] FEAT-02 (#51) MinIO y FEAT-03 (#52) LLM en ramas separadas

---

## PM-020

| Campo | Valor |
| --- | --- |
| **ID** | PM-020 |
| **Fecha** | 2026-10-17 |
| **Hora** | 21:10 |
| **Solicitante** | Boris Anthony Angulo Urquieta |
| **Agente/Entorno** | Cursor IDE — Agent |
| **Modelo** | Cursor Grok 4.6 |
| **Tarea** | Implementar GitHub #51 FEAT-02 (S3/MinIO + lock upload en BD) en `feat/51-s3-evidence-lock` |
| **Objetivo** | Blobs de evidencia fuera del disco del contenedor; lock de upload atómico y visible a N réplicas |
| **Contexto** | Auditoría F12-03 FALLA; issue https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss/issues/51; release 8.1.0 Demo 1 |
| **PR-IMPL vinculado** | [PR-IMPL-041](../../prompts/impl/PR-IMPL-041.md) |
| **DD-UC vinculado** | [DD-UC-004-M7](../../design/DD-UC-004-M7.md) |
| **FSD-UC vinculado** | FSD-UC-004 · FSD-UC-005 |
| **Estado** | completado |

### Prompt usado exacto

```text
implementemos la issue 51, crea la rama a partir de release/2.0.0 que ya esta actualiZada, y una vez hecho documenta todo los cambios donde sean necesarios
```

### Entradas auxiliares

- `docs/prompts/impl/PR-IMPL-041.md`
- `docs/auditoria/12factor-boris.md` (F12-03)
- GitHub issue #51

### Archivos generados o modificados

Ver `git status` de esta sesión (S3 adapter, lock JDBC, V23, Compose MinIO, docs vivos).

### Cambios realizados

1. `S3EvidenceBlobStorageAdapter` + MinIO en Compose; `SIGESA_S3_*` para endpoint remoto futuro.
2. `JdbcEvidenceUploadLockAdapter` + tabla `evidence_upload_lock` (V23 / Hibernate / runner dev).
3. `InMemoryEvidenceUploadLockAdapter` ya no es bean de Spring.

### Validación ejecutada

- [x] `./mvnw -Dtest=S3EvidenceBlobStorageAdapterTest,JdbcEvidenceUploadLockAdapterIT,UploadEvidenceServiceTest,UploadNormativeIndicatorEvidenceServiceTest test` — verde
- [ ] `pnpm run lint` — N/A (backend + compose)

### Resultado obtenido

Issue #51 implementado en `feat/51-s3-evidence-lock`. Veredicto F12-03 histórico `FALLA` intacto.

### Próximos pasos

- [ ] Commit/PR `Fixes #51` cuando el usuario lo pida
- [ ] FEAT-03 (#52) LLM

---

## PM-021

| Campo | Valor |
|---|---|
| **ID** | PM-021 |
| **Fecha** | 2026-10-17 |
| **Hora** | 21:28 |
| **Solicitante** | Boris Anthony Angulo Urquieta |
| **Agente/Entorno** | Cursor Grok 4.6 |
| **Modelo** | Cursor Grok 4.6 |
| **Tarea** | Cerrar huecos FEAT-02 #51: reportes en object storage + GET blob + imagen MinIO |
| **Objetivo** | PDF/Excel no viven en disco del API; descarga de evidencia desde MinIO/S3; pull de imagen viable |
| **Contexto** | Tras PM-020 el usuario pidió no dejar reportes en `LocalFileReportArtifactStorageAdapter` ni omitir GET de blob. Compose fallaba con `minio/minio` (Docker Hub deny). |
| **PR-IMPL vinculado** | PR-IMPL-041 |
| **DD-UC vinculado** | DD-UC-004-M7 / DD-UC-014 |
| **FSD-UC vinculado** | FSD-UC-004 / FSD-UC-014 |
| **Estado** | completado |

### Prompt usado exacto

```text
No es del issue (no lo dejes para “cerrar #51”)

Reportes PDF/Excel siguen en disco (LocalFileReportArtifactStorageAdapter) — lo excluimos a propósito.
No hay GET de descarga del blob: SIGESA ya solo guardaba la key; recrear el API no borra el archivo en MinIO ni la referencia en Postgres.

Esto corrigelo
```

### Entradas auxiliares

- `docs/prompts/impl/PR-IMPL-041.md`
- `docs/design/DD-UC-004-M7.md`
- `docs/design/DD-UC-014.md`

### Archivos generados o modificados

| Acción | Ruta |
|---|---|
| generado | `backend/src/main/java/com/umss/sigesa/adapter/out/report/S3ReportArtifactStorageAdapter.java` |
| generado | `backend/src/main/java/com/umss/sigesa/domain/exception/EvidenceBlobNotFoundException.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/adapter/in/web/DocumentAssetController.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/application/port/in/GetDocumentAssetUseCase.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/application/service/evidence/GetDocumentAssetService.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/application/port/out/EvidenceBlobStoragePort.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/application/service/report/ReportExportJobService.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/adapter/out/report/LocalFileReportArtifactStorageAdapter.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/config/S3EvidenceStorageConfig.java` |
| modificado | `docker-compose.yml` |
| modificado | `docs/product/api_contracts.md` |
| modificado | `docs/prompts/impl/PR-IMPL-041.md` |
| modificado | `docs/product/DTP.md` |
| modificado | `README.md` |

### Cambios realizados

1. `S3ReportArtifactStorageAdapter` + export dashboard vía `EvidenceBlobStoragePort.store/retrieve` (key opaca, no path absoluto).
2. `GET /api/v1/document-assets/{id}/content` (API-EVD-07b).
3. Imagen Compose `quay.io/minio/minio:latest`.

### Validación ejecutada

- [x] `./mvnw -Dtest=S3EvidenceBlobStorageAdapterTest,GetDocumentAssetServiceTest,DocumentAssetControllerWebMvcTest,ReportExportJobServiceTest,DownloadReportArtifactServiceTest test` — verde
- [ ] `pnpm run lint` — N/A (sin cambio FE)

### Resultado obtenido

Huecos de #51 cerrados en `feat/51-s3-evidence-lock`. Orval pendiente de `generate:api` con backend arriba.

### Próximos pasos

- [ ] Reintentar `docker compose up --build`
- [ ] Commit/PR cuando el usuario lo pida

---

## PM-022

| Campo | Valor |
|---|---|
| **ID** | PM-022 |
| **Fecha** | 2026-10-17 |
| **Hora** | 21:42 |
| **Solicitante** | Boris Anthony Angulo Urquieta |
| **Agente/Entorno** | Cursor Grok 4.6 |
| **Modelo** | Cursor Grok 4.6 |
| **Tarea** | Documentar y fijar imagen Compose tras 401 Quay MinIO |
| **Objetivo** | Compose pullable; docs vivos alineadas (no `docs/baseline/`) |
| **Contexto** | Hub `minio/minio` deny; `quay.io/minio/minio:latest` 401 UNAUTHORIZED (imágenes MinIO ya no públicas). |
| **PR-IMPL vinculado** | PR-IMPL-041 |
| **DD-UC vinculado** | DD-UC-004-M7 |
| **FSD-UC vinculado** | FSD-UC-004 |
| **Estado** | completado |

### Prompt usado exacto

```text
hiciste los cambios respectivos en la documentaicon del cambio?
```

(disparado por el 401 de `quay.io/minio/minio:latest` en `docker compose up --build`)

### Archivos generados o modificados

| Acción | Ruta |
|---|---|
| modificado | `docker-compose.yml` |
| modificado | `README.md` |
| modificado | `docs/prompts/impl/PR-IMPL-041.md` |
| modificado | `docs/product/DTP.md` |
| modificado | `docs/product/FSD.md` |
| modificado | `docs/product/uc/FSD-UC-004.md` |
| modificado | `docs/product/api_contracts.md` |
| modificado | `docs/design/DD-UC-004-M7.md` |
| modificado | `docs/auditoria/12factor-boris.md` |
| modificado | `docs/sprints/sprint_03/PROMPT_MAPPING.md` |

### Cambios realizados

- Imagen `docker.io/pgsty/silo:RELEASE.2026-10-15T00-00-00Z` (API S3/MinIO).
- Changelog DTP A.1 + B.4 object storage; FSD/DD/contratos/auditoría.

### Validación ejecutada

- [x] `docker pull docker.io/pgsty/silo:RELEASE.2026-10-15T00-00-00Z` — OK
- [ ] `pnpm run lint` — N/A

### Resultado obtenido

Documentación viva sincronizada con el reemplazo de imagen. `docs/baseline/` intacto.

### Próximos pasos

- [ ] Commit/PR cuando el usuario lo pida

---

## PM-023

| Campo | Valor |
|---|---|
| **ID** | PM-023 |
| **Fecha** | 2026-10-17 |
| **Hora** | 21:59 |
| **Solicitante** | Boris Anthony Angulo Urquieta |
| **Agente/Entorno** | Cursor Grok 4.6 |
| **Modelo** | Cursor Grok 4.6 |
| **Tarea** | Documentar FEAT-03 (#52) y crear rama desde release/2.0.0 |
| **Objetivo** | Contrato PR-IMPL-042 + FSD/DD/DTP/auditoría/API; mapping sprint 03. Sin código Java en este prompt. |
| **Contexto** | Auditoría CON-02/CON-05: `HttpClient.send` 120 s en worker Tomcat. Issue #52: timeout 10 s + no bloquear plataforma + 504. |
| **PR-IMPL vinculado** | PR-IMPL-042 |
| **DD-UC vinculado** | DD-SYS-002 |
| **FSD-UC vinculado** | FSD-UC-024 |
| **Estado** | completado |

### Prompt usado exacto

```text
Bien, ahora pasemos al issue #52, crea la rama a partir de release 2.0.0 y genera la documentacion necesaria guardando tambien el prompt mapping en sprint 3
```

### Entradas auxiliares

- `https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss/issues/52`
- `docs/auditoria/concurrencia-boris.md`
- `docs/design/DD-SYS-002.md`
- `docs/prompts/impl/PR-IMPL-040.md` (plantilla de contrato 8.1.0)

### Archivos generados o modificados

| Acción | Ruta |
|---|---|
| generado | `docs/prompts/impl/PR-IMPL-042.md` |
| modificado | `docs/product/DTP.md` |
| modificado | `docs/product/FSD.md` |
| modificado | `docs/product/uc/FSD-UC-024.md` |
| modificado | `docs/product/api_contracts.md` |
| modificado | `docs/design/DD-SYS-002.md` |
| modificado | `docs/auditoria/concurrencia-boris.md` |
| modificado | `docs/sprints/sprint_03/PROMPT_MAPPING.md` |

### Cambios realizados

1. Rama `feat/52-llm-timeout-async` desde `origin/release/2.0.0` (incluye #51 mergeado).
2. Contrato `PR-IMPL-042`: timeout 10 s, HTTP/1.1, 504, virtual threads/`sendAsync`; fuera MCP/retry/202.
3. Docs vivos alineados; veredictos de auditoría históricos intactos.

### Validación ejecutada

- [ ] `mvn test` — N/A (solo documentación)
- [ ] `pnpm run lint` — N/A

### Resultado obtenido

Spec FEAT-03 lista para implementación. Código Java **no** tocado.

### Próximos pasos

- [ ] Implementar `OpenWebUiChatAdapter` + handler 504 según `PR-IMPL-042`
- [ ] Commit/PR `Fixes #52` cuando el usuario lo pida

---

## PM-024

| Campo | Valor |
|---|---|
| **ID** | PM-024 |
| **Fecha** | 2026-10-17 |
| **Hora** | 22:03 |
| **Solicitante** | Boris Anthony Angulo Urquieta |
| **Agente/Entorno** | Cursor Grok 4.6 |
| **Modelo** | Cursor Grok 4.6 |
| **Tarea** | Implementar FEAT-03 (#52) y abrir PR |
| **Objetivo** | Timeout LLM 10 s, sendAsync + hilos virtuales, 504 amigable |
| **Contexto** | Spec PM-023 / PR-IMPL-042. |
| **PR-IMPL vinculado** | PR-IMPL-042 |
| **DD-UC vinculado** | DD-SYS-002 |
| **FSD-UC vinculado** | FSD-UC-024 |
| **Estado** | completado |

### Prompt usado exacto

```text
implementa y luego haz el commit y pr
```

### Archivos generados o modificados

| Acción | Ruta |
|---|---|
| generado | `backend/src/main/java/com/umss/sigesa/domain/exception/AssistantGatewayTimeoutException.java` |
| generado | `backend/src/test/java/com/umss/sigesa/adapter/out/assistant/OpenWebUiChatAdapterTest.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/adapter/out/assistant/OpenWebUiChatAdapter.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/adapter/in/web/advice/AssistantExceptionHandler.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/config/AssistantProperties.java` |
| modificado | `backend/src/main/resources/application.yaml` |
| modificado | `backend/src/main/resources/application-dev.yaml` |
| modificado | `backend/src/main/resources/application-prod.yaml` |
| modificado | `backend/src/test/java/com/umss/sigesa/adapter/in/web/AssistantControllerWebMvcTest.java` |

### Cambios realizados

1. `sendAsync` + `future.get(timeout)` con `cancel(true)` al vencer.
2. `ASSISTANT_GATEWAY_TIMEOUT` → HTTP 504.
3. `spring.threads.virtual.enabled=true`; `SIGESA_ASSISTANT_TIMEOUT_MS` default 10000.

### Validación ejecutada

- [x] tests adapter + WebMvc 504 (Maven)
- [ ] `pnpm run lint` — N/A

### Resultado obtenido

FEAT-03 implementado en `feat/52-llm-timeout-async`.

### Próximos pasos

- [ ] PR `Fixes #52` contra `release/2.0.0`

---

## PM-025

| Campo | Valor |
|---|---|
| **ID** | PM-025 |
| **Fecha** | 2026-10-18 |
| **Solicitante** | Alexander Alvarez |
| **Agente/Entorno** | Antigravity AI |
| **Tarea** | Marco de evaluación offline de la IA (Módulo 7) |
| **Objetivo** | Dataset dorado (15 casos), métricas deterministas/Juez LLM (Kappa 1.0), compuerta exit 0 v3 e informe PDF |
| **PR-IMPL vinculado** | PR-IMPL-043 |
| **DD-UC vinculado** | DD-EVALS-OFFLINE |
| **FSD-UC vinculado** | Módulo 7 (Evals) |
| **Estado** | completado |

---

## PM-026

| Campo | Valor |
|---|---|
| **ID** | PM-026 |
| **Fecha** | 2026-10-19 |
| **Solicitante** | Alexander Alvarez |
| **Agente/Entorno** | Antigravity AI |
| **Tarea** | Implementación integral Módulo 8 (Issues #53, #54, #55, #56) |
| **Objetivo** | Fail-fast de configuración por entorno, notificaciones outbox, bandeja dictamen técnico y bitácora inmutable append-only |
| **PR-IMPL vinculado** | PR-IMPL-044 |
| **DD-UC vinculado** | DD-MOD8-ISSUES |
| **FSD-UC vinculado** | FSD-UC-012 / FSD-UC-015 / FSD-UC-017 |
| **Estado** | completado |

---

## PM-027

| Campo | Valor |
|---|---|
| **ID** | PM-027 |
| **Fecha** | 2026-10-19 |
| **Hora** | 19:15 |
| **Solicitante** | Boris Anthony Angulo Urquieta |
| **Agente/Entorno** | Cursor IDE — Agent |
| **Modelo** | Cursor Grok 4.6 |
| **Tarea** | FEAT-10 botón flotante de asistencia (#57) |
| **Objetivo** | FAB inferior derecha en el dashboard/panel autenticado que abre un modal accesible con el copiloto general |
| **Contexto** | No hay `DD-UC-*`; se creó `DD-FEAT-10` bajo MOD-ASSISTANT. Rama `feat/57-floating-assistant` desde `origin/release/2.0.0`. |
| **PR-IMPL vinculado** | PR-IMPL-045 |
| **DD-UC vinculado** | DD-FEAT-10 (no DD-UC; GitHub #57) |
| **FSD-UC vinculado** | FSD-UC-024 |
| **Estado** | completado |

### Prompt usado exacto

```text
Actúa como un Senior Frontend Engineer con experiencia en React 19 + TypeScript + Vite, siguiendo la arquitectura del proyecto y respetando las restricciones de este repositorio.

Objetivo:
Implementar un botón flotante de asistencia visible en el dashboard, en la esquina inferior derecha, que al pulsarlo abra un modal accesible con el copiloto. El flujo debe ser funcional y no intrusivo.

Historia de usuario:
“Añadir botón flotante de asistencia que abre el copiloto en un modal desde cualquier página del dashboard. El botón debe ser accesible y no intrusivo.”

Importante:
No hagas cambios en archivos fuera del alcance de este feature. Prioriza la solución mínima viable.

haz ahora este que es el issue de la ventana flotante
crea la rama correspondiente a partir de release/2.0.0
```

### Entradas auxiliares

- `docs/design/assistant/DD-FEAT-10.md`
- `docs/design/assistant/DD-AGENT-UI-SHELL.md`
- `docs/prompts/impl/PR-IMPL-045.md`
- GitHub issue #57

### Archivos generados o modificados

| Acción | Ruta |
|---|---|
| generado | `docs/design/assistant/DD-FEAT-10.md` |
| generado | `docs/prompts/impl/PR-IMPL-045.md` |
| generado | `frontend/src/components/FloatingAssistantButton.tsx` |
| generado | `frontend/src/components/FloatingAssistantButton.test.tsx` |
| modificado | `frontend/src/components/auth/ProtectedRoute.tsx` |
| modificado | `frontend/src/features/assistant/components/AssistantChatUI.tsx` |
| modificado | `docs/design/assistant/DD-AGENT-UI-SHELL.md` |
| modificado | `docs/product/DTP.md` |
| modificado | `docs/product/FSD.md` |
| modificado | `docs/product/uc/FSD-UC-024.md` |
| modificado | `docs/sprints/sprint_03/PROMPT_MAPPING.md` |

### Cambios realizados

- FAB `CircleHelp` en `ProtectedRoute` (todas las páginas autenticadas excepto `/ayuda`).
- Modal a11y (`role=dialog`, ESC, overlay, foco de retorno, scroll lock).
- Chat via `useAssistantChat` + `AssistantChatUI compact` (sin `AssistantPage`/`Sidebar`).
- Offset `bottom-24` para no tapar el FAB de copilotos de dominio.

### Validación ejecutada

- [x] `pnpm exec vitest run` FloatingAssistantButton + ProtectedRoute — 8 passed
- [x] `pnpm exec tsc -b` — ok
- [x] `pnpm exec oxlint` archivos tocados — ok

### Resultado obtenido

FEAT-10 implementado en `feat/57-floating-assistant`.

### Próximos pasos

- [ ] PR `Fixes #57` contra `release/2.0.0`

---

## PM-028

| Campo | Valor |
|---|---|
| **ID** | PM-028 |
| **Fecha** | 2026-10-22 |
| **Hora** | 16:35 |
| **Solicitante** | Boris Anthony Angulo Urquieta |
| **Agente/Entorno** | Cursor Grok 4.6 |
| **Modelo** | grok-4.6 |
| **Tarea** | Permitir modificar/eliminar evidencia si el indicador no está aprobado |
| **Objetivo** | `DELETE /api/v1/evidences/{id}` 204 cuando el indicador no es `APROBADO`; 409 `EVIDENCE_IMMUTABLE` si está aprobado; UI de retiro/reemplazo para CC |
| **Contexto** | Tras cargar evidencia el coordinador no podía reemplazarla (`INDICATOR_ALREADY_HAS_EVIDENCE` + DELETE siempre 409) |
| **PR-IMPL vinculado** | PR-IMPL-035 |
| **DD-UC vinculado** | DD-UC-005 |
| **FSD-UC vinculado** | FSD-UC-005 |
| **Estado** | completado |

### Prompt usado exacto

```text
pero, me deberia permitir modificar/eliminar la evidencia si es que el indicador no esta aprobado
```

### Entradas auxiliares

- `docs/design/DD-UC-005.md`
- `docs/product/uc/FSD-UC-005.md`
- `docs/prompts/impl/PR-IMPL-035.md`

### Archivos generados o modificados

| Acción | Ruta |
|---|---|
| generado | `frontend/src/features/processes/components/NormativeIndicatorCollaborationSection.test.tsx` |
| modificado | `backend/src/main/java/com/umss/sigesa/application/service/evidence/AttemptDeleteEvidenceService.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/adapter/out/persistance/EvidenceUploadJpaAdapter.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/adapter/in/web/EvidenceLifecycleController.java` |
| modificado | `frontend/src/features/processes/components/NormativeIndicatorCollaborationSection.tsx` |
| modificado | `frontend/src/features/evidence/api/normativeIndicatorEvidenceApi.ts` |
| modificado | `docs/product/uc/FSD-UC-005.md` |
| modificado | `docs/design/DD-UC-005.md` |
| modificado | `docs/product/DTP.md` |
| modificado | `docs/product/FSD.md` |
| modificado | `docs/product/api_contracts.md` |
| modificado | `docs/sprints/sprint_03/PROMPT_MAPPING.md` |

### Cambios realizados

- DELETE retira evidencia normativa no `APROBADO` (CC/CAE), resetea indicador a `PENDIENTE`, borra blob/asset exclusivo.
- APROBADO y evidencia legacy siguen `409 EVIDENCE_IMMUTABLE` + `AUDIT_DELETE_DENIED`.
- UI: botón «Quitar / reemplazar» y volver a «Subir evidencia».

### Validación ejecutada

- [x] `./mvnw -Dtest=EvidenceLifecycleServiceTest,JpaAuditLogAdapterTest test` — OK
- [x] `pnpm test:run NormativeIndicatorCollaborationSection.test.tsx` — 2 passed

### Resultado obtenido

Coordinador puede quitar y volver a cargar evidencia mientras el indicador no esté aprobado.

### Próximos pasos

- [ ] Rebuild Docker (`docker compose up --build`) para que el API nuevo quede en el contenedor

---

## PM-029

| Campo | Valor |
|---|---|
| **ID** | PM-029 |
| **Fecha** | 2026-10-22 |
| **Hora** | 16:43 |
| **Solicitante** | Boris Anthony Angulo Urquieta |
| **Agente/Entorno** | Cursor Grok 4.6 |
| **Modelo** | grok-4.6 |
| **Tarea** | Registrar Demo 2: OpenAPI + CI y aportes 8.2.0 (cargar/eliminar evidencias) |
| **Objetivo** | Contrato `openapi.json` desde springdoc, workflow drift+Spectral, `docs/demos/demo-2.md` y `8.2.0-aportes.md` |
| **Contexto** | Entrega Classroom Demo 2 sobre `release/8.2.0` |
| **PR-IMPL vinculado** | N/A (registro Demo 2 + contrato) |
| **DD-UC vinculado** | DD-UC-005 |
| **FSD-UC vinculado** | FSD-UC-004 / FSD-UC-005 |
| **Estado** | completado |

### Prompt usado exacto

```text
2. Contrato OpenAPI y su flujo de validación
- Contrato generado desde el código: el backend genera su contrato con decoradores (NestJS), anotaciones (Spring) o modelos (FastAPI). Un comando lo exporta a openapi.json y el archivo se sube al repositorio. Debe incluir los endpoints de las funcionalidades que se presentan.
- Flujo de CI: un workflow en .github/workflows/ que, en cada pull request, compruebe que el archivo coincide con lo que genera el código (drift) y que pasa Spectral (lint). oasdiff y Schemathesis son opcionales y suman.

El grupo decide si esta parte la hace una persona o varias. Cuenta como una de las funcionalidades de quien la haga.

El ejemplo completo está en la sesión 5 (partes B y C) y en sus diapositivas.

3. Qué registrar
- En docs/demos/demo-2.md y aquí en Classroom: una tabla con cada integrante, sus funcionalidades. mas el link de su repositorio esto de forma individual.
- El plazo para acordar esto lo maneja cada grupo; lo registrado debe estar en el release antes de la demo.
- release/8.2.0 creado antes de la demo, con 8.2.0-aportes.md en la raíz. El release es el entregable del grupo: reúne las funcionalidades de todos sus integrantes.

Puedes registrar estas cosas para este feature que vendria a ser cargar/eliminar evidencias
```

### Entradas auxiliares

- `docs/design/DD-UC-005.md`
- `docs/product/uc/FSD-UC-004.md`
- `docs/product/uc/FSD-UC-005.md`

### Archivos generados o modificados

| Acción | Ruta |
|---|---|
| generado | `openapi.json` |
| generado | `.spectral.yml` |
| generado | `.github/workflows/openapi.yml` |
| generado | `scripts/export-openapi.sh` |
| generado | `docs/demos/demo-2.md` |
| generado | `8.2.0-aportes.md` |
| generado | `backend/src/test/java/com/umss/sigesa/adapter/in/web/OpenApiExportIT.java` |
| generado | `backend/src/main/java/com/umss/sigesa/config/OpenApiConfig.java` |
| modificado | `docs/product/DTP.md` |
| modificado | `docs/sprints/sprint_03/PROMPT_MAPPING.md` |

### Cambios realizados

- Exportación springdoc a `openapi.json` (incluye POST evidencias y DELETE).
- CI: drift + Spectral.
- Registro individual Demo 2 para Boris (cargar/eliminar evidencias + OpenAPI).

### Validación ejecutada

- [x] `OPENAPI_WRITE=true ./mvnw -Dtest=OpenApiExportIT test` — OK
- [x] Spectral lint `openapi.json` — sin errores

### Resultado obtenido

Artefactos Demo 2 listos en `release/8.2.0`.

### Próximos pasos

- [ ] Completar filas de los demás integrantes en `8.2.0-aportes.md`
- [ ] Push de `release/8.2.0` cuando el grupo lo pida

---

## PM-030

| Campo | Valor |
|---|---|
| **ID** | PM-030 |
| **Fecha** | 2026-10-22 |
| **Hora** | 17:12 |
| **Solicitante** | Boris Anthony Angulo Urquieta |
| **Agente/Entorno** | Cursor Grok 4.6 |
| **Modelo** | Cursor Grok 4.6 |
| **Tarea** | Restaurar blob S3/MinIO al reutilizar DocumentAsset huérfano |
| **Objetivo** | Que una evidencia visible en el frontend exista también en Silo cuando el SHA-256 coincide con un asset cuyo objeto ya fue borrado |
| **Contexto** | Upload inicial escribió Postgres y luego el catch de outbox borró el objeto. Re-subidas del mismo PDF (`00gjDPSka5p6EK8DSX9A_guia_bash_mouredev_pro.pdf`, folio `SIGESA-20261022-0a01fdbf`) reutilizaron `storageKey` sin PUT. |
| **PR-IMPL vinculado** | PR-IMPL-041 |
| **DD-UC vinculado** | DD-UC-004-M7 |
| **FSD-UC vinculado** | FSD-UC-004 |
| **Estado** | completado |

### Prompt usado exacto

```text
00gjDPSka5p6EK8DSX9A_guia_bash_mouredev_pro.pdf
subi esta evidencia pero no hay ene el silo, sale que esta subido en el fronten pero no hay en el servidor S3
```

### Entradas auxiliares

- `docs/design/DD-UC-004-M7.md`
- `docs/product/uc/FSD-UC-004.md`
- `docs/prompts/impl/PR-IMPL-041.md`

### Archivos generados o modificados

| Acción | Ruta |
|---|---|
| modificado | `backend/src/main/java/com/umss/sigesa/application/port/out/EvidenceBlobStoragePort.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/adapter/out/evidence/S3EvidenceBlobStorageAdapter.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/adapter/out/evidence/LocalFileEvidenceBlobStorageAdapter.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/application/service/evidence/UploadNormativeIndicatorEvidenceService.java` |
| modificado | `backend/src/test/java/com/umss/sigesa/adapter/out/evidence/S3EvidenceBlobStorageAdapterTest.java` |
| modificado | `backend/src/test/java/com/umss/sigesa/application/service/evidence/UploadNormativeIndicatorEvidenceServiceTest.java` |
| modificado | `docs/product/uc/FSD-UC-004.md` |
| modificado | `docs/product/api_contracts.md` |
| modificado | `docs/product/DTP.md` |
| modificado | `docs/sprints/sprint_03/PROMPT_MAPPING.md` |

### Cambios realizados

- Puerto `restore(storageKey, content)` + adaptadores S3 y filesystem local.
- Al reutilizar `DocumentAsset`, si `retrieve` está vacío se reescribe el objeto en la misma clave.
- Tests de reuso con blob presente/ausente.

### Validación ejecutada

- [x] `./mvnw -Dtest=UploadNormativeIndicatorEvidenceServiceTest,S3EvidenceBlobStorageAdapterTest test` — BUILD SUCCESS
- [ ] `pnpm run lint` — N/A (sin cambios FE)

### Resultado obtenido

Metadatos en BD ya no quedan huérfanos si se vuelve a subir el mismo archivo tras rebuild.

### Próximos pasos

- [ ] Rebuild del backend (`docker compose up --build`)
- [ ] Re-subir el mismo PDF a un indicador sin evidencia vigente para restaurar el objeto en Silo

---

## PM-031

| Campo | Valor |
|---|---|
| **ID** | PM-031 |
| **Fecha** | 2026-10-22 |
| **Hora** | 17:28 |
| **Solicitante** | Boris Anthony Angulo Urquieta |
| **Agente/Entorno** | Cursor Grok 4.6 |
| **Modelo** | Cursor Grok 4.6 |
| **Tarea** | Visualizar evidencias subidas + registrar Demo 2 (OpenAPI/CI) |
| **Objetivo** | Que [CC]/[TD] vean y descarguen el blob desde el detalle del indicador; documentar el parámetro 2 de Classroom |
| **Contexto** | FSD-UC-004 API-EVD-07b ya existía; el listado no abría el archivo. Demo 2 exige contrato generado + drift/Spectral. |
| **PR-IMPL vinculado** | PR-IMPL-041 |
| **DD-UC vinculado** | DD-UC-004-M7 |
| **FSD-UC vinculado** | FSD-UC-004 |
| **Estado** | completado |

### Prompt usado exacto

```text
Perfecto, ahora hagamos otro feature, donde pueda visualizar las evidencias subidas desde el frontend, marca el UC, 
y llena los parametros de la tarea:
2. Contrato OpenAPI y su flujo de validación
...
3. Qué registrar
- En docs/demos/demo-2.md y aquí en Classroom...
tambien llena los docs que se deben llenar en el repo como al sprint 3
```

### Entradas auxiliares

- `docs/design/DD-UC-004-M7.md`
- `docs/product/uc/FSD-UC-004.md`
- `docs/prompts/impl/PR-IMPL-041.md`

### Archivos generados o modificados

| Acción | Ruta |
|---|---|
| generado | `frontend/src/features/evidence/components/EvidencePreviewDialog.tsx` |
| generado | `frontend/src/features/evidence/components/EvidencePreviewDialog.test.tsx` |
| generado | `frontend/src/features/evidence/hooks/useEvidencePreview.ts` |
| modificado | `frontend/src/features/processes/components/NormativeIndicatorCollaborationSection.tsx` |
| modificado | `frontend/src/features/evidence/api/normativeIndicatorEvidenceApi.ts` |
| modificado | `frontend/src/api/endpoints/document-assets/document-assets.ts` |
| modificado | `frontend/src/lib/api/customFetch.ts` |
| modificado | `backend/src/main/java/com/umss/sigesa/adapter/in/web/DocumentAssetController.java` |
| modificado | `docs/demos/demo-2.md` |
| modificado | `8.2.0-aportes.md` |
| modificado | `docs/product/uc/FSD-UC-004.md` |
| modificado | `docs/product/FSD.md` |
| modificado | `docs/product/DTP.md` |
| modificado | `docs/sprints/sprint_03/PROMPT_MAPPING.md` |

### Cambios realizados

- Botón «Ver evidencia» + diálogo de preview/descarga (`GET /document-assets/{id}/content`).
- Registro Classroom: tabla integrantes, contrato OpenAPI (drift + Spectral), UC marcado.

### Validación ejecutada

- [x] `OPENAPI_WRITE=true ./mvnw -Dtest=OpenApiExportIT,DocumentAssetControllerWebMvcTest test` — OK
- [x] `vitest` CollaborationSection + EvidencePreviewDialog — 5 passed
- [x] oxlint sobre archivos del visor — OK

### Resultado obtenido

FSD-UC-004 cubre carga + visualización; Demo 2 documentada para Boris.

### Próximos pasos

- [ ] Rebuild frontend Docker para ver el visor
- [ ] Push a `release/8.2.0` cuando se pida

---

## PM-032

| Campo | Valor |
| --- | --- |
| **ID** | PM-032 |
| **Fecha** | 2026-10-23 |
| **Solicitante** | Alexander Alvarez (`Alex553-bot`) |
| **Rol** | Fullstack Dev / Tech Lead |
| **Sprint** | 03 |
| **PR-IMPL** | PR-IMPL-046 |
| **Design Doc** | `docs/features/FEAT-11-exportacion-dossier-digital-zip.md` |
| **FSD / PRD** | `FSD-UC-032` / Transversal §10 |
| **Rama** | `feat/73-dossier-zip-export` |
| **Issue GitHub** | [#73](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss/issues/73) |
| **Estado** | completado |

### Prompt usado exacto

```text
check the new feature implemented, there are some tests that are failing in the backend and the frontend as well
coudl u pls fil the database to showcase this new feature ?
commit all the changes
```

### Entradas auxiliares

- `docs/features/FEAT-11-exportacion-dossier-digital-zip.md`
- `docs/prompts/impl/PR-IMPL-046.md`

### Archivos generados o modificados

| Acción | Ruta |
|---|---|
| generado | `backend/src/main/java/com/umss/sigesa/adapter/out/dossier/ZipDossierExportStreamingAdapter.java` |
| generado | `backend/src/main/java/com/umss/sigesa/adapter/out/dossier/InMemoryDossierExportJobTrackerAdapter.java` |
| generado | `backend/src/main/java/com/umss/sigesa/application/port/in/ExportProcessDossierUseCase.java` |
| generado | `backend/src/main/java/com/umss/sigesa/application/port/in/GetProcessDossierExportJobStatusUseCase.java` |
| generado | `backend/src/main/java/com/umss/sigesa/application/port/out/DossierExportPort.java` |
| generado | `backend/src/main/java/com/umss/sigesa/application/service/dossier/DossierExportService.java` |
| generado | `backend/src/main/java/com/umss/sigesa/config/DossierDemoDataLoader.java` |
| generado | `backend/src/test/java/com/umss/sigesa/adapter/in/web/ProcessDossierExportControllerTest.java` |
| generado | `backend/src/test/java/com/umss/sigesa/adapter/out/dossier/ZipDossierExportStreamingAdapterTest.java` |
| generado | `backend/src/test/java/com/umss/sigesa/application/service/dossier/DossierExportServiceTest.java` |
| generado | `frontend/src/features/processes/hooks/useDossierExport.ts` |
| generado | `frontend/src/features/processes/hooks/useDossierExport.test.ts` |
| generado | `docs/features/FEAT-11-exportacion-dossier-digital-zip.md` |
| generado | `docs/prompts/impl/PR-IMPL-046.md` |
| modificado | `backend/src/main/java/com/umss/sigesa/adapter/in/web/ProcessController.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/adapter/out/persistance/DocumentAssetJpaAdapter.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/application/port/out/DocumentAssetPort.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/config/ReportModuleConfig.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/domain/model/ReportFormat.java` |
| modificado | `backend/src/test/java/com/umss/sigesa/adapter/in/security/JwtAuthenticationFilterTest.java` |
| modificado | `backend/src/test/java/com/umss/sigesa/adapter/in/web/ProcessControllerQueryTest.java` |
| modificado | `frontend/src/features/processes/components/ProcessDetailView.tsx` |
| modificado | `.gitignore` |
| modificado | `docs/sprints/sprint_03/PROMPT_MAPPING.md` |

### Cambios realizados

- Implementación del pipeline asíncrono para exportación de Dossier Digital (ZIP Jerárquico y Foliado).
- Corrección de pruebas en backend con `JwtTokenAdapter` mock y perfiles aislados.
- Tipado estricto y migración a `customFetch` en `useDossierExport.ts` con cobertura de pruebas unitarias Vitest.
- Seeder de demostración `DossierDemoDataLoader` con árbol CEUB de 4 niveles, 6 PDFs binarios reales y metadatos foliados.

### Validación ejecutada

- [x] Backend: `./mvnw test` — 564 pruebas pasadas, 0 fallos, 0 errores.
- [x] Frontend: `npm run test:run` — 44 suites, 119 pruebas pasadas.
- [x] Calidad Frontend: `npx oxlint` (0 errores) y `npx tsc -b` (0 errores).

---

## PM-033

| Campo | Valor |
|---|---|
| **ID** | PM-033 |
| **Fecha** | 2026-11-02 |
| **Hora** | 21:30 |
| **Solicitante** | Aylen Gonzáles |
| **Agente/Entorno** | Cursor IDE — Agent |
| **Modelo** | Cursor Grok 4.6 |
| **Tarea** | PASO 0 — inventario del repo para el experimento E3 |
| **Objetivo** | Reportar stack, arranque, Docker, DB, modulo extraible, puertos/adaptadores, cliente HTTP y rutas GET antes de escribir infra |
| **Contexto** | El enunciado asumia Python/uvicorn. El repo es Spring Boot 4 / Java 21. Integrante 1 ya dejo el adaptador dual. |
| **PR-IMPL vinculado** | [PR-IMPL-047](../../prompts/impl/PR-IMPL-047.md) |
| **DD-UC vinculado** | N/A (laboratorio `bench/e3/HIPOTESIS.md`) |
| **FSD-UC vinculado** | N/A — E3 laboratorio |
| **Estado** | completado |

### Prompt usado exacto

```text
Actúa como ingeniero DevOps. Necesito dejar listo el entorno de infraestructura para un experimento de "monolito modular vs servicio extraído" con inyección de fallas de red usando Toxiproxy. Solo infraestructura: NO modifiques la lógica de negocio ni los adaptadores.

## PASO 0: Investiga el repo antes de escribir nada
Revisa el proyecto y repórtame (en una lista corta) lo que encuentres sobre:
- Stack, versión de Python y gestor de dependencias (requirements.txt, pyproject.toml, etc.). Ignora el package.json: es solo de Playwright.
- Cómo se arranca la app hoy (entry point, puerto, comando de uvicorn).
- Si ya existe Dockerfile, docker-compose, .env o carpeta bench/.
- Base de datos que se usa y dónde vive (archivo SQLite, Postgres, etc.).
- Estructura de módulos y cuál es el candidato a extraer (frontera clara, tablas propias).
- Interfaz/puerto y adaptadores (in-process / HTTP), la variable de entorno que los selecciona y la que guarda la URL del módulo. Si no existen aún, dímelo y propón nombres, sin implementarlos.
- Cliente HTTP usado (httpx, requests, etc.) y si define timeouts.
- Rutas GET disponibles: elige una que use el módulo y una de control que no lo use.
Si algo es ambiguo o falta, pregúntame antes de asumir.
```

### Entradas auxiliares

- `bench/e3/HIPOTESIS.md`
- `docs/prompts/impl/PR-IMPL-047.md`

### Archivos generados o modificados

Ninguno (solo investigacion).

### Cambios realizados

- Inventario: Java 21 / Spring Boot 4 / Maven; `EXTRACTION_MODE` + `SIGESA_APP` + `SIGESA_NOTIFICATION_BASE_URL` ya existian; H2 default; rutas `GET /api/v1/e3/notifications` y `GET /api/health`.

### Validacion ejecutada

- [x] Lectura de `application.yaml`, `SigesaApplication`, adaptadores HTTP/in-process, compose de producto.

### Resultado obtenido

PASO 0 reportado. Compose E3 en `bench/e3/`; no se pisa el compose de producto.

---

## PM-034

| Campo | Valor |
|---|---|
| **ID** | PM-034 |
| **Fecha** | 2026-11-02 |
| **Hora** | 21:35 |
| **Solicitante** | DevOps E3 |
| **Agente/Entorno** | Cursor IDE — Agent |
| **Modelo** | Cursor Grok 4.6 |
| **Tarea** | Infraestructura Docker E3 (Toxiproxy, scripts, override monolito) |
| **Objetivo** | Compose HTTP + override in-process, toxic.sh, verificar-infra.sh, run-limpio.sh, README, `.env.example` |
| **Contexto** | Entregables del mismo prompt que PM-033. Dockerfile de producto reutilizado. |
| **PR-IMPL vinculado** | [PR-IMPL-047](../../prompts/impl/PR-IMPL-047.md) |
| **DD-UC vinculado** | N/A (laboratorio `bench/e3/HIPOTESIS.md`) |
| **FSD-UC vinculado** | N/A — E3 laboratorio |
| **Estado** | completado |

### Prompt usado exacto

```text
## Entregables (después del paso 0)
1. Dockerfile (si no existe), sin --reload y con el mismo número de workers para A y B.
2. docker-compose.yml con:
   - api (A): modo HTTP, URL del módulo apuntando al PROXY (toxiproxy:8666), nunca directo a modulo.
   - modulo (B): misma imagen, otro command; `expose`, NO `ports`.
   - toxiproxy: ghcr.io/shopify/toxiproxy:2.9.0 (versión fija), publica 8474.
   - toxiproxy-setup: curlimages/curl, crea el proxy `modulo` (listen 0.0.0.0:8666, upstream modulo:<puerto B>). Idempotente, corre en cada up.
   - k6: grafana/k6 con profiles: ["bench"], volumen ./bench/e3.
   - db solo si el paso 0 demuestra que hace falta.
   - Healthchecks sin curl en imágenes slim (usa python urllib); depends_on con condition: service_healthy.
   - Una sola red; comunicación por nombre de servicio.
3. docker-compose.monolito.yml (override): api en modo in-process, sin modulo/toxiproxy/toxiproxy-setup.
4. bench/e3/toxic.sh (bash, set -euo pipefail):
   - lento on|off → latency downstream 2000, jitter 0
   - colgado on|off → timeout downstream, timeout 0
   - caido on|off → POST /proxies/modulo {"enabled": false|true}
   - reset, status
   - TOXIPROXY_URL configurable (default http://localhost:8474). Idempotente, valida argumentos.
5. bench/e3/verificar-infra.sh: pasos OK/FALLA: ruta del módulo 200; lento ≥2 s; colgado termina con curl -m 5; caido falla al instante; reset vuelve a 200; ruta de control 200 en todos los casos.
6. bench/e3/run-limpio.sh: down -v, up -d --build --wait, y guarda tiempos de build y up en bench/e3/resultados/tiempos.csv (forma,fecha,build_s,up_s).
7. bench/e3/README-infra.md corto con puertos, nombres y uso.

## Reglas
- Versiones de imágenes fijas; variables configurables en .env.example.
- Puerto de modulo no publicado al host.
- Al terminar, lista los archivos creados y los comandos para validar.
```

### Entradas auxiliares

- `bench/e3/HIPOTESIS.md`
- `docs/prompts/impl/PR-IMPL-047.md`

### Archivos generados o modificados

| Accion | Ruta |
|---|---|
| generado | `bench/e3/docker-compose.yml` |
| generado | `bench/e3/docker-compose.monolito.yml` |
| generado | `bench/e3/toxic.sh` |
| generado | `bench/e3/verificar-infra.sh` |
| generado | `bench/e3/run-limpio.sh` |
| generado | `bench/e3/README-infra.md` |
| generado | `bench/e3/resultados/tiempos.csv` |
| modificado | `.env.example` |

### Cambios realizados

- Compose E3 aislado del de producto. API apunta a Toxiproxy:8666. Sin servicio `db` (H2). Healthcheck wget / toxiproxy-cli.

### Validacion ejecutada

- [x] `docker compose -f bench/e3/docker-compose.yml config` -- CONFIG_OK
- [x] Override monolito lista solo `api`

### Resultado obtenido

Infra escrita. Ejecucion real quedo en PM-035.

---

## PM-035

| Campo | Valor |
|---|---|
| **ID** | PM-035 |
| **Fecha** | 2026-11-02 |
| **Hora** | 21:44 |
| **Solicitante** | DevOps E3 |
| **Agente/Entorno** | Cursor IDE — Agent |
| **Modelo** | Cursor Grok 4.6 |
| **Tarea** | Ejecutar y corregir la infra E3 hasta verificar-infra OK |
| **Objetivo** | run-limpio http, verificar-infra (6 pasos), toxic status, run-limpio in_process, down -v; documentar timeouts y tiempos |
| **Contexto** | Docker Desktop 29.6.2. Fallos reales: CRLF de mvnw, /app/data y /app/uploads no escribibles, bean ObjectMapper ausente en Boot 4. |
| **PR-IMPL vinculado** | [PR-IMPL-047](../../prompts/impl/PR-IMPL-047.md) |
| **DD-UC vinculado** | N/A (laboratorio `bench/e3/HIPOTESIS.md`) |
| **FSD-UC vinculado** | N/A -- E3 laboratorio |
| **Estado** | completado |

### Prompt usado exacto

```text
Docker ya está corriendo. Ejecuta tú mismo todo en terminal y corrige lo que falle. Mantén la infra en bench/e3/ y no pises el docker-compose.yml de producto.

## 1. Revisa antes de ejecutar
- start_period del healthcheck de api y modulo (Spring Boot tarda en arrancar).
- Que el healthcheck use una herramienta que exista en la imagen Alpine (wget/busybox, no curl).
- Mismo límite de memoria/CPU y mismas JAVA_OPTS en api y modulo.

## 2. Ejecuta y valida (en este orden)
chmod +x bench/e3/*.sh
./bench/e3/run-limpio.sh http
./bench/e3/verificar-infra.sh
./bench/e3/toxic.sh status
./bench/e3/run-limpio.sh in_process

- Si algo falla: lee los logs (docker compose logs), diagnostica la causa raíz, corrige y vuelve a correr. Repite hasta que verificar-infra.sh pase sus 6 pasos.
- No marques nada como hecho sin haberlo ejecutado. Pégame la salida real de cada comando.
- Al terminar, deja el entorno limpio (docker compose down -v).

## 3. Documenta en bench/e3/README-infra.md
- Timeout y reintentos efectivos con E3_NOTIFICATION_* (valor, deadline, reintentos).
- Cuánto tarda en fallar cada tóxico (lento, colgado, caído) con esos valores.
- Tiempos medidos de build y up (de tiempos.csv).

## 4. Registra los prompts en promptimpl
- Busca primero la convención existente del repo (carpeta o archivo "promptimpl", plantillas PR-IMPL / PM-*). Síguela tal cual; no inventes formato.
- Guarda ahí los prompts usados en esta tarea: el del paso 0 (investigación del repo), el de implementación de infraestructura y este de ejecución/corrección.
- Para cada uno incluye: fecha, objetivo, texto del prompt, archivos creados o modificados y el resultado de la validación.
- Si la convención exige un ID (PM-*), asígnalo según el patrón existente. Si no queda claro cuál usar, pregúntame. Guarda el anterior prompt y este igual

## Al final
Lista los archivos creados o modificados y el resultado de cada paso de verificar-infra.sh (OK/FALLA).
```

### Entradas auxiliares

- `docs/prompts/impl/PR-IMPL-047.md`
- `bench/e3/HIPOTESIS.md`

### Archivos generados o modificados

| Accion | Ruta |
|---|---|
| generado | `docs/prompts/impl/PR-IMPL-047.md` |
| modificado | `bench/e3/docker-compose.yml` |
| modificado | `bench/e3/run-limpio.sh` |
| modificado | `bench/e3/README-infra.md` |
| modificado | `bench/e3/resultados/tiempos.csv` |
| modificado | `.env.example` |
| modificado | `.gitattributes` |
| modificado | `backend/Dockerfile` |
| modificado | `backend/src/main/java/com/umss/sigesa/config/NotificationExtractionConfig.java` |
| modificado | `docs/sprints/sprint_03/PROMPT_MAPPING.md` |

### Cambios realizados

- `start_period` 120 s; `--wait-timeout 300`; `JAVA_OPTS`/`JAVA_TOOL_OPTIONS` y limites 512m/1 CPU identicos en api y modulo.
- Storage local en `/tmp/sigesa-*` (usuario `sigesa` no puede crear `/app/data` ni `/app/uploads`).
- `sed` CRLF en `mvnw` (Alpine `./mvnw: not found`).
- `ObjectMapper` local en `NotificationExtractionConfig` (Boot 4 no expone el bean Jackson 2).

### Validacion ejecutada

- [x] `chmod +x bench/e3/*.sh`
- [x] `./bench/e3/run-limpio.sh http` -- api Healthy; `build_s=1` `up_s=45`
- [x] `./bench/e3/verificar-infra.sh` -- 10 OK, 0 FALLA (6 pasos + control en cada uno)
- [x] `./bench/e3/toxic.sh status` -- proxy `modulo` enabled, sin toxics
- [x] `./bench/e3/run-limpio.sh in_process` -- `build_s=2` `up_s=25`; GET modulo `origin=in_process` 200
- [x] `docker compose -f bench/e3/docker-compose.yml down -v` -- sin contenedores `sigesa-e3`

### Resultado obtenido

Infra E3 verificada. Entorno limpio. Timeouts efectivos 5000 ms / 1 intento. Toxicos: lento ~2 s / 200; colgado curl rc 28 a 5 s; caido 503 en 0 s.

---

## PM-036

| Campo | Valor |
|---|---|
| **ID** | PM-036 |
| **Fecha** | 2026-11-03 |
| **Hora** | 18:10 |
| **Solicitante** | Tech Lead / Alexander Alvarez |
| **Agente/Entorno** | Antigravity AI Pair Programming |
| **Modelo** | Claude 3.7 Sonnet |
| **Tarea** | Corrección de nombres de negocio y enlaces locales en exportación Dossier Digital |
| **Objetivo** | Reemplazar IDs crudos por nombres de carrera y proceso/plantilla en el índice HTML, resumen ejecutivo y manifiesto; asegurar enlaces relativos offline directos a evidencias foliadas; y asignar filename legible en Content-Disposition. |
| **Contexto** | El usuario detectó que el dossier generado mostraba UUIDs en lugar de nombres humanos para el proceso y carrera, y que los hipervínculos a los archivos dentro del HTML maestro no abrían las evidencias locales. |
| **PR-IMPL vinculado** | [PR-IMPL-046](file:///mnt/sda2/informatica/maestria/sigesa-acreditacion-umss/docs/prompts/impl/PR-IMPL-046.md) |
| **DD-UC vinculado** | [FEAT-11](file:///mnt/sda2/informatica/maestria/sigesa-acreditacion-umss/docs/features/FEAT-11-exportacion-dossier-digital-zip.md) |
| **FSD-UC vinculado** | FSD-UC-032 |
| **Estado** | completado |

### Prompt usado exacto

```text
the dosier export generation, is showing ids instead of the business names for processes, careers, and that the links for the files are not working in the export, pls review
```

### Entradas auxiliares

- `docs/features/FEAT-11-exportacion-dossier-digital-zip.md`
- `docs/prompts/impl/PR-IMPL-046.md`

### Archivos generados o modificados

| Acción | Ruta |
|---|---|
| modificado | `backend/src/main/java/com/umss/sigesa/adapter/out/dossier/ZipDossierExportStreamingAdapter.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/adapter/in/web/ProcessController.java` |
| modificado | `backend/src/test/java/com/umss/sigesa/adapter/out/dossier/ZipDossierExportStreamingAdapterTest.java` |
| modificado | `docs/sprints/sprint_03/PROMPT_MAPPING.md` |

### Cambios realizados

- Inyección de `ProgramCatalogPort` y `TemplatePort` en `ZipDossierExportStreamingAdapter` para resolver nombres legibles de carrera (`careerDisplayName`, `careerSlug`) y plantilla de acreditación (`processBusinessName`, `processSlug`).
- Sustitución de IDs UUID en `00_Indice_Maestro_Cumplimiento.html`, `00_Resumen_Ejecutivo.txt` y `00_Manifiest_Checksums.txt` por los nombres de negocio (manteniendo el UUID como identificador secundario).
- Generación de hipervínculos relativos funcionales (`<a class='file-link' href='...'>`) desde el índice HTML raíz hacia las carpetas y evidencias foliadas dentro del archivo ZIP, facilitando la navegación offline una vez descomprimido.
- Reutilización en memoria de los blobs de evidencias mediante mapa temporal en una sola pasada, previniendo lecturas redundantes del almacenamiento.
- Nombre descriptivo de descarga en `Content-Disposition` en `ProcessController.resolveDossierDownloadFilename` (`Dossier_<careerCode>.zip`).
- Ampliación de la suite de pruebas unitarias en `ZipDossierExportStreamingAdapterTest` cubriendo resolución de nombres de negocio y enlaces HTML relativos.

### Validación ejecutada

- [x] Backend: `./mvnw test -Dtest=ZipDossierExportStreamingAdapterTest,ProcessDossierExportControllerTest,DossierExportServiceTest` — 12 pruebas pasadas (0 fallos, 0 errores).
- [x] Frontend: `npx vitest run src/features/processes/hooks/useDossierExport.test.ts` — 4 pruebas pasadas (0 fallos).

---

## PM-037

| Campo | Valor |
|---|---|
| **ID** | PM-037 |
| **Fecha** | 2026-11-04 |
| **Hora** | 14:54 |
| **Solicitante** | Boris Anthony Angulo Urquieta |
| **Agente/Entorno** | Cursor IDE — Agent |
| **Modelo** | Cursor Grok 4.6 |
| **Tarea** | Corpus RAG Manual ARCU-SUR 2023 + trazabilidad FSD/sprint 03 |
| **Objetivo** | Que el asistente existente responda con el manual 2023 (Markdown indexado) y quede registrado en FSD, PR-IMPL y PROMPT_MAPPING |
| **Contexto** | Issue #76. Motor RAG ya existía (PR-IMPL-032). No se crea un quinto agent=. PDF origen en tmp/rag-inbox (gitignored). |
| **PR-IMPL vinculado** | [PR-IMPL-048](../../prompts/impl/PR-IMPL-048.md) |
| **DD-UC vinculado** | [DD-UC-033](../../design/DD-UC-033.md) |
| **FSD-UC vinculado** | [FSD-UC-033](../../product/uc/FSD-UC-033.md) |
| **Estado** | completado |

### Prompt usado exacto

```text
conectalo, a la vez quiero que tambien edites el issue de github, este esta usando el #76
quiero que agregues a ese issue todo lo que se esta haciendo y se hara

ya hiciste la documentacion? el fsd, prompt_mapping u registrarlo en sprint 3?
```

### Entradas auxiliares

- `docs/prompts/impl/PR-IMPL-032.md`
- `docs/design/assistant/TOOL-CATALOG.md`
- `https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss/issues/76`

### Archivos generados o modificados

| Acción | Ruta |
|---|---|
| modificado | `.gitignore` |
| modificado | `backend/src/main/java/com/umss/sigesa/adapter/out/persistance/repository/SpringDataNormativeDocumentRepository.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/application/service/assistant/AssistantNormativeRagService.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/config/NormativeDocumentSeedLoader.java` |
| modificado | `backend/src/test/java/com/umss/sigesa/application/service/assistant/AssistantNormativeRagServiceTest.java` |
| modificado | `docs/design/assistant/TOOL-CATALOG.md` |
| modificado | `docs/product/DTP.md` |
| modificado | `docs/product/FSD.md` |
| modificado | `docs/product/uc/FSD-UC-024.md` |
| modificado | `docs/sprints/sprint_03/PROMPT_MAPPING.md` |
| generado | `backend/src/main/java/com/umss/sigesa/application/service/normative/NormativeMarkdownDocumentParser.java` |
| generado | `backend/src/main/resources/normative-corpus/arcu-sur-2023/` |
| generado | `backend/src/test/java/com/umss/sigesa/application/service/normative/NormativeMarkdownDocumentParserTest.java` |
| generado | `backend/src/test/java/com/umss/sigesa/config/NormativeDocumentCorpusResourceTest.java` |
| generado | `docs/product/uc/FSD-UC-033.md` |
| generado | `docs/design/DD-UC-033.md` |
| generado | `docs/prompts/impl/PR-IMPL-048.md` |

### Cambios realizados

- Corpus Markdown del manual ARCU-SUR 2023 en classpath; loader upsert al boot; se retiran placeholders demo ARCU-SUR.
- Detector RAG ampliado (pares, autoevaluación, convocatoria, visita).
- Cadena documental FSD-UC-033 → DD-UC-033 → PR-IMPL-048 → PM-037; issue #76 actualizado.

### Validación ejecutada

- [x] `./mvnw -Dtest=NormativeMarkdownDocumentParserTest,NormativeDocumentCorpusResourceTest,AssistantNormativeRagServiceTest test` — OK
- [ ] Smoke Docker `RAG corpus ARCU-SUR 2023: 33 chunks upserted` — pendiente de rebuild

### Resultado obtenido

El asistente usa el mismo `POST /api/v1/assistant/chat`. El PDF no viaja al cliente. Otra máquina requiere pull + rebuild de la imagen.

### Próximos pasos

- [ ] Commit y push para que el corpus viaje con el repo
- [ ] Smoke autenticado (comité de pares / cronograma)
- [ ] Citas en UI y más documentos (issue #76)

---

## PM-038

| Campo | Valor |
|---|---|
| **ID** | PM-038 |
| **Fecha** | 2026-11-04 |
| **Hora** | 15:12 |
| **Solicitante** | Boris Anthony Angulo Urquieta |
| **Agente/Entorno** | Cursor IDE — Agent |
| **Modelo** | Cursor Grok 4.6 |
| **Tarea** | Manejo uniforme de errores REST (#67) |
| **Objetivo** | Envelope `{error,message,details?}`, 500 sin traza, validación con details, docs FSD-UC-034 |
| **Contexto** | Issue #67. El producto ya usaba `error`+`message` en advice de dominio; faltaba fallback 500 y details. |
| **PR-IMPL vinculado** | [PR-IMPL-049](../../prompts/impl/PR-IMPL-049.md) |
| **DD-UC vinculado** | [DD-UC-034](../../design/DD-UC-034.md) |
| **FSD-UC vinculado** | [FSD-UC-034](../../product/uc/FSD-UC-034.md) |
| **Estado** | completado |

### Prompt usado exacto

```text
Quiero hacer ahora el #67 issue,
```

### Archivos generados o modificados

| Acción | Ruta |
|---|---|
| generado | `backend/src/main/java/com/umss/sigesa/adapter/in/web/dto/ApiErrorResponse.java` |
| generado | `backend/src/main/java/com/umss/sigesa/adapter/in/web/advice/GlobalExceptionHandler.java` |
| generado | `backend/src/test/java/com/umss/sigesa/adapter/in/web/advice/ErrorContractProbeController.java` |
| generado | `backend/src/test/java/com/umss/sigesa/adapter/in/web/advice/GlobalExceptionHandlerWebMvcTest.java` |
| generado | `docs/product/uc/FSD-UC-034.md` |
| generado | `docs/design/DD-UC-034.md` |
| generado | `docs/prompts/impl/PR-IMPL-049.md` |
| modificado | `backend/src/main/java/com/umss/sigesa/adapter/in/web/advice/AuthExceptionHandler.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/adapter/in/security/RestAuthenticationEntryPoint.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/adapter/in/security/RestAccessDeniedHandler.java` |
| modificado | `backend/src/main/resources/application.yaml` |
| modificado | `docs/product/api_contracts.md` |
| modificado | `docs/product/FSD.md` |
| modificado | `docs/product/DTP.md` |
| modificado | `docs/sprints/sprint_03/PROMPT_MAPPING.md` |

### Validación ejecutada

- [x] `./mvnw -Dtest=GlobalExceptionHandlerWebMvcTest,AuthControllerTest test` — OK

### Resultado obtenido

API unifica 400/401/403/500 en el envelope documentado. Códigos de negocio 409 no se reescribieron.

### Próximos pasos

- [ ] Commit / PR si el usuario lo pide
- [ ] Cerrar #67 tras merge

---

## PM-039

| Campo | Valor |
|---|---|
| **ID** | PM-039 |
| **Fecha** | 2026-11-04 |
| **Hora** | 18:55 |
| **Solicitante** | Tech Lead (sesión) |
| **Agente/Entorno** | Cursor IDE — Agent |
| **Modelo** | Cursor Grok 4.6 |
| **Tarea** | Implementar FSD-UC-016 portal público |
| **Objetivo** | Listado publicado, estado por slug y certificado PDF anónimo bajo `/api/v1/public`, 404 genérico, UI `/public/status` |
| **Contexto** | Hueco de búsqueda por nombre resuelto con API-PUB-02 (adición UC-016). Prefijo `/api/v1/public` vs FSD original `/public/programs/{slug}`. Sin noticias, sin baseline. |
| **PR-IMPL vinculado** | [PR-IMPL-050](../../prompts/impl/PR-IMPL-050.md) |
| **DD-UC vinculado** | [DD-UC-016](../../design/DD-UC-016.md) |
| **FSD-UC vinculado** | [FSD-UC-016](../../product/uc/FSD-UC-016.md) |
| **Estado** | completado |

### Prompt usado exacto

```text
1. Opción 2: añade GET /api/v1/public/programs (listado de publicadas, solo
   nombre y slug). Documéntalo como adición al UC-016 y avísame del cambio de spec.
2. Usa /api/v1/public/... en todos los endpoints; anota la diferencia con el
   path del FSD.
3. Acepto GET /api/v1/public/programs/{slug}/certificate: PDF solo si está
   publicado, 404 genérico si no, sin exponer URLs directas del archivo.
```

### Entradas auxiliares

- `docs/design/DD-UC-016.md`
- `docs/prompts/impl/PR-IMPL-050.md`
- `docs/product/uc/FSD-UC-016.md`

### Archivos generados o modificados

| Acción | Ruta |
|---|---|
| generado | `docs/design/DD-UC-016.md` |
| generado | `docs/prompts/impl/PR-IMPL-050.md` |
| generado | `backend/src/main/java/com/umss/sigesa/adapter/in/web/PublicProgramController.java` |
| generado | `backend/src/main/java/com/umss/sigesa/application/service/publicportal/PublicPortalService.java` |
| generado | `backend/src/main/resources/db/migration/V27__publication_snapshot.sql` |
| generado | `backend/src/test/java/com/umss/sigesa/adapter/in/web/PublicPortalGherkinIT.java` |
| generado | `frontend/src/features/public-portal/` |
| generado | `frontend/src/api/endpoints/public-portal/public-portal.ts` |
| modificado | `docs/product/uc/FSD-UC-016.md` |
| modificado | `docs/product/FSD.md` |
| modificado | `docs/product/api_contracts.md` |
| modificado | `docs/product/DTP.md` |
| modificado | `docs/product/modelo_datos.md` |
| modificado | `backend/src/main/java/com/umss/sigesa/adapter/in/security/SecurityConfig.java` |
| modificado | `frontend/src/App.tsx` |
| modificado | `frontend/src/features/home/HomeAccessBanners.tsx` |
| modificado | `openapi.json` |
| modificado | `docs/sprints/sprint_03/PROMPT_MAPPING.md` |

### Validación ejecutada

- [x] `./mvnw -Dtest=PublicPortalGherkinIT,PublicPortalServiceTest,OpenPdfPublicCertificateRendererAdapterTest,OpenApiExportIT test` — OK
- [x] `pnpm run test:run` (App + public-portal + HomePage) — 13 passed
- [x] `pnpm run lint` — OK (warnings preexistentes en dashboard/RecentActivity)

### Resultado obtenido

Portal público anónimo: listado `{slug,name}`, estado vigente y PDF en memoria. 404 genérico para no publicados. Spec viva actualizada (path `/api/v1/public` vs FSD `/public`).

### Próximos pasos

- [ ] Commit / PR si el usuario lo pide
- [ ] UI de publicación [JD] (fuera de este UC)
- [ ] Noticias = UC-035 (no implementado)

---

## PM-040

| Campo | Valor |
|---|---|
| **ID** | PM-040 |
| **Fecha** | 2026-11-04 |
| **Hora** | 19:42 |
| **Solicitante** | Tech Lead (sesión) |
| **Agente/Entorno** | Cursor IDE — Agent |
| **Modelo** | Cursor Grok 4.6 |
| **Tarea** | Implementar FSD-UC-035 noticias y destacados |
| **Objetivo** | CRUD [JD], lectura anónima, una destacada, home `#noticias` real, 404 genérico |
| **Contexto** | Spec v1.1 aprobada + seis adiciones (unpublish featured=false, feature draft 409, fallback latest, home excluye slug). Sin imágenes/tags/schedule. Sin tocar UC-016/015/002 ni baseline. |
| **PR-IMPL vinculado** | [PR-IMPL-051](../../prompts/impl/PR-IMPL-051.md) |
| **DD-UC vinculado** | [DD-UC-035](../../design/DD-UC-035.md) |
| **FSD-UC vinculado** | [FSD-UC-035](../../product/uc/FSD-UC-035.md) |
| **Estado** | completado |

### Prompt usado exacto

```text
ok para implementar
```

### Entradas auxiliares

- `docs/design/DD-UC-035.md`
- `docs/prompts/impl/PR-IMPL-051.md`
- `docs/product/uc/FSD-UC-035.md`

### Archivos generados o modificados

| Acción | Ruta |
|---|---|
| generado | `docs/design/DD-UC-035.md` |
| generado | `docs/prompts/impl/PR-IMPL-051.md` |
| generado | `backend/src/main/resources/db/migration/V28__news.sql` |
| generado | `backend/src/main/java/com/umss/sigesa/application/service/news/NewsService.java` |
| generado | `backend/src/main/java/com/umss/sigesa/adapter/in/web/PublicNewsController.java` |
| generado | `backend/src/main/java/com/umss/sigesa/adapter/in/web/NewsAdminController.java` |
| generado | `backend/src/test/java/com/umss/sigesa/adapter/in/web/NewsGherkinIT.java` |
| generado | `frontend/src/features/news/` |
| generado | `frontend/src/api/endpoints/public-news/` |
| generado | `frontend/src/api/endpoints/admin-news/` |
| modificado | `docs/product/uc/FSD-UC-035.md` |
| modificado | `docs/product/FSD.md` |
| modificado | `docs/product/api_contracts.md` |
| modificado | `docs/product/DTP.md` |
| modificado | `docs/product/modelo_datos.md` |
| modificado | `backend/src/main/java/com/umss/sigesa/adapter/in/security/SecurityConfig.java` |
| modificado | `frontend/src/App.tsx` |
| modificado | `frontend/src/features/home/HomeNewsSection.tsx` |
| modificado | `openapi.json` |
| modificado | `docs/sprints/sprint_03/PROMPT_MAPPING.md` |

### Cambios realizados

- Tabla `news_article` (V28), servicio hexagonal, APIs públicas y admin JD.
- Seis tests Gherkin `@TC-NEWS`. Home `#noticias` consume Orval; «Ver mas Noticias» → `/public/news`.
- 404 genérico público; 409 `NEWS_NOT_PUBLISHED` al destacar borrador; unpublish fuerza `featured=false`.

### Validación ejecutada

- [x] `./mvnw -Dtest=NewsGherkinIT,NewsServiceTest,OpenApiExportIT test` — OK
- [x] `pnpm exec vitest run` (App + HomePage + useHomeNews + public-portal) — 15 passed
- [x] `oxlint` sobre news/home/App/Sidebar — OK

### Resultado obtenido

FSD-UC-035 Implementado. [JD] gestiona noticias; [P] lee solo publicadas. Home usa destacada o la más reciente y no duplica ese slug en el recorte.

### Próximos pasos

- [ ] Commit / PR si el usuario lo pide
- [ ] Imágenes / tags / programación (fuera de alcance)

---

## PM-041

| Campo | Valor |
|---|---|
| **ID** | PM-041 |
| **Fecha** | 2026-11-04 |
| **Hora** | 21:00 |
| **Solicitante** | Aylen Laura / Tech Lead (sesión) |
| **Agente/Entorno** | Cursor IDE — Agent |
| **Modelo** | Cursor Grok 4.6 |
| **Tarea** | Rediseñar presentación del certificado PDF (PRD-US-020 / FSD-UC-016) opción B-lite |
| **Objetivo** | A4 horizontal, frase con carrera/agencia/vigencia/resolución, QR y logos; sin cambiar endpoints ni 404 |
| **Contexto** | B-lite aprobada: V29 solo `agency` y `resolution_number` nullable. Sin `issued_at` (el snapshot no tiene fecha de publicación). Seed inf-sis sin valores reales → null. |
| **PR-IMPL vinculado** | [PR-IMPL-050](../../prompts/impl/PR-IMPL-050.md) |
| **DD-UC vinculado** | [DD-UC-016](../../design/DD-UC-016.md) |
| **FSD-UC vinculado** | [FSD-UC-016](../../product/uc/FSD-UC-016.md) |
| **Estado** | completado |

### Prompt usado exacto

```text
Opción B-lite, aprobada.
- Flyway V29 en publication_snapshot: solo agency y resolution_number
  (nullable). Sin issued_at: usa la fecha de publicación del snapshot.
  Sin signer_name/signer_title.
- Documenta la adición en FSD-UC-016 y modelo_datos (no baseline).
- Pie: línea de firma en blanco con "Firma y sello" y espacio para sello.
- Frase completa con carrera, agencia, vigencia y resolución. Si agency o
  resolution_number son null, omite esa parte sin romper el layout.
- Seed de inf-sis: agency = [PEGA AQUÍ], resolution_number = [PEGA AQUÍ].
  No inventes valores: si faltan, deja null y avísame.
- Reutiliza los logos ya existentes en el frontend (UMSS en el
  encabezado, DUEA en el pie), embebidos en alta resolución.
- Código de verificación = UUID del snapshot.
- QR aprobado: property de base URL por entorno, enlace a
  {baseUrl}/public/status?slug={slug}. Si la UI no lee ?slug=, añádelo
  como cambio mínimo.
- Mantén metadatos PDF, test de campos + application/pdf, y deja la
  muestra en backend/target/certificate-sample-inf-sis.pdf.
```

### Entradas auxiliares

- `docs/design/DD-UC-016.md`
- `docs/prompts/impl/PR-IMPL-050.md`
- `docs/product/uc/FSD-UC-016.md`

### Archivos generados o modificados

| Acción | Ruta |
|---|---|
| generado | `backend/src/main/resources/db/migration/V29__publication_snapshot_certificate_fields.sql` |
| generado | `backend/src/main/java/com/umss/sigesa/domain/model/PublicCertificateText.java` |
| generado | `backend/src/test/java/com/umss/sigesa/domain/model/PublicCertificateTextTest.java` |
| generado | `backend/src/main/resources/public-certificate/` |
| modificado | `backend/src/main/java/com/umss/sigesa/adapter/out/report/OpenPdfPublicCertificateRendererAdapter.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/domain/model/PublicProgramSnapshot.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/adapter/out/persistance/entity/PublicationSnapshotJpaEntity.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/adapter/out/persistance/PublicProgramQueryJpaAdapter.java` |
| modificado | `backend/pom.xml` |
| modificado | `frontend/src/features/public-portal/hooks/usePublicPortal.ts` |
| modificado | `docs/product/uc/FSD-UC-016.md` |
| modificado | `docs/product/modelo_datos.md` |
| modificado | `docs/product/DTP.md` |

### Cambios realizados

- V29 `agency` / `resolution_number` nullable. Frase omite cláusulas vacías.
- PDF A4 horizontal, borde doble, DejaVu embebida, logos UMSS/DUEA, QR ZXing, UUID de verificación.
- `/public/status?slug=` en el hook. Property `sigesa.public.base-url`.
- Seed inf-sis sin agencia ni resolución (valores no provistos).

### Validación ejecutada

- [x] `./mvnw -Dtest=PublicCertificateTextTest,OpenPdfPublicCertificateRendererAdapterTest,PublicPortalServiceTest,PublicPortalGherkinIT test` — OK
- [x] `pnpm exec vitest run` (public-portal + HomePage) — 10 passed
- [x] Muestra `backend/target/certificate-sample-inf-sis.pdf`

### Resultado obtenido

Certificado rediseñado. Endpoints y 404 intactos. Muestra lista para revisión visual.

### Próximos pasos

- [ ] Pegar `agency` y `resolution_number` reales del seed inf-sis
- [ ] Revisar la muestra PDF antes de cerrar

---

## PM-042

| Campo | Valor |
|---|---|
| **ID** | PM-042 |
| **Fecha** | 2026-11-11 |
| **Hora** | 23:42 |
| **Solicitante** | Marlene Velasquez |
| **Agente/Entorno** | Cursor |
| **Modelo** | Grok 4.7 |
| **Tarea** | Historial de ingresos y auditoría de acciones |
| **Objetivo** | Registrar ingresos, salidas, fallos y acciones de sesión, y consultarlos desde el panel [JD] |
| **Contexto** | La bitácora `audit_log` ya existía. Se añade `access_session` y `session_id` sin reemplazar FSD-UC-017 |
| **PR-IMPL vinculado** | PR-IMPL-052 |
| **DD-UC vinculado** | DD-UC-036 |
| **FSD-UC vinculado** | FSD-UC-036 |
| **Estado** | completado |

### Prompt usado exacto

```text
# Prompt: Implementar módulo de Historial de Ingresos y Auditoría de Acciones de Usuarios
```

El texto completo es el mensaje del chat del 2026-11-11 23:31. Alcance: menú HISTORIAL DE INGRESOS solo para el rol autorizado, registro de login, logout, expiración, fallo y acciones, consulta con filtros, paginación y detalle de sesión, sin edición ni borrado de la bitácora.

### Archivos generados o modificados

| Acción | Ruta |
|---|---|
| generado | `backend/src/main/resources/db/migration/V30__access_session.sql` |
| generado | `backend/src/main/java/com/umss/sigesa/domain/model/access/` |
| generado | `backend/src/main/java/com/umss/sigesa/adapter/in/web/AccessSessionController.java` |
| generado | `backend/src/main/java/com/umss/sigesa/adapter/in/security/AccessActivityFilter.java` |
| generado | `frontend/src/features/admin/access/` |
| modificado | `AuthenticateService`, `AuthController`, `JwtAuthenticationFilter`, `JwtTokenAdapter`, `SecurityConfig`, `audit_log` |
| modificado | `frontend/src/components/layout/Sidebar.tsx`, `App.tsx`, `AuthProvider.tsx` |
| generado | `docs/product/uc/FSD-UC-036.md`, `docs/design/DD-UC-036.md`, `docs/prompts/impl/PR-IMPL-052.md` |

### Validación ejecutada

- [x] `./mvnw -Dtest=AuthenticateAccessSessionTest,AccessSessionTest,ModAuthServiceIntegrationTest test` — 11 pruebas, 0 fallos
- [x] `oxlint` de la pantalla, el menú y la ruta — sin avisos

### Resultado obtenido

[JD] consulta `/admin/ingresos`. El ingreso abre una sesión, el fallo queda en `FAILED`, el logout cierra la sesión activa y un segundo cierre no la altera. Las acciones quedan en `audit_log.session_id`.

### Próximos pasos

- [ ] Reiniciar el backend que ya estaba en marcha para cargar V30 y los endpoints nuevos


