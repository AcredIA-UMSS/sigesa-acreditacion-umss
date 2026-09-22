# PROMPT_MAPPING — Sprint 03

> Registro PM del sprint 03. Trazabilidad: `Código → PR-IMPL → DD-UC-NNN → FSD-UC-NNN → DTP`.  
> **Índice:** PM-001…PM-014 (evidencia/subfases v1 → v2 normativo → M6 workflow → UX/docs → E2E Buscador Evidencias IA).

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
