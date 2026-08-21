# PROMPT_MAPPING — Sprint 02

> Registro PM del sprint 02. Trazabilidad: `Código → PR-IMPL → DD-SYS-002 → PRD-REQ-028 → DTP`.

| ID Mapeo | PR-IMPL | Design Doc | FSD / PRD | Descripción de la Tarea |
| :--- | :--- | :--- | :--- | :--- |
| PM-001 | PR-IMPL-012 | DD-SYS-002 | PRD-REQ-028 | Asistente virtual SIGESA (MOD-ASSISTANT): backend proxy Open WebUI + frontend `/ayuda` + Docker Ollama |
| PM-002 | PR-IMPL-013 | DD-SYS-002 §11 | PRD-REQ-028 / FSD-UC-002 | Tool calling read-only: loop backend + tool `list_users` (solo JD) |
| PM-003 | PR-IMPL-011 | DD-UC-011 | FSD-UC-011 | Conexión completa a base de datos real en el Dashboard (UC-011), remoción total de stubs y mocks. |
| PM-004 | PR-IMPL-019 | DD-UC-019 | FSD-UC-019 | Consulta de procesos de acreditación (GET listado + detalle, RBAC, UI `/procesos`) |
| PM-005 | N/A (Hotfix) | DD-UC-011 | FSD-UC-011 | Corrección de decimales a exactamente 2 decimales en los KPIs del Dashboard |
| PM-003 | PR-IMPL-019 | DD-UC-019 | FSD-UC-019 | Consulta de procesos de acreditación (GET listado + detalle, RBAC, UI `/procesos`) |
| PM-004 | PR-IMPL-014 | DD-UC-020 | PRD-REQ-029 / FSD-UC-020 | Rol evaluador externo [EE]: revisión documental solo lectura por carrera asignada |
| PM-005 | PR-IMPL-015 | DD-UC-002 | PRD-REQ-001 / FSD-UC-002 | Gestión usuarios [JD]: listado nombre completo, modal alta, validaciones y credenciales al crear |
| PM-006 | PR-IMPL-021 | DD-UC-021 | FSD-UC-021 | CRUD plantillas normativas (API-TPL-01…08), Flyway V5, hook UC-003 PUBLISHED + clonación referenceUrl |
| PM-007 | PR-IMPL-021 | DD-UC-021 | FSD-UC-021 | Cierre documental: `@save-prompt-mapping` + `@dtp-sync` (autor Boris Anthony Angulo Urquieta) |
| PM-008 | PR-IMPL-022 | DD-UC-022 | FSD-UC-022 | Contrato implementación CRUD estructura proceso ACTIVE (API-PROC-05…08); cierre documental orchestrator |
| PM-009 | PR-IMPL-022 | DD-UC-022 | FSD-UC-022 | Full-Stack UC-022: backend ProcessStructure + frontend `/procesos/{id}/estructura` |
| PM-010 | PR-IMPL-023 | DD-UC-023 | FSD-UC-023 | Full-Stack UC-023: asignación responsable [CC] + UI detalle/listado |
| PM-011 | PR-IMPL-024 | DD-AGENT-001 | FSD-UC-022 / PRD-REQ-028 | Copiloto fases embebido (`agent=phases`): CC lectura, tools subfases, UI responsive, PR #28 |
| PM-012 | PR-IMPL-025 | DD-AGENT-002 | FSD-UC-002 / PRD-REQ-028 | Copiloto usuarios embebido (`agent=users`): tools alta/estado/asignación JD-only, UsersCopilotPanel |
| PM-013 | PR-IMPL-026 | DD-AGENT-003 | FSD-UC-024 / PRD-REQ-028 | Copiloto control documental (`agent=evidence`) + MCP sigesa-evidence |
| PM-014 | PR-IMPL-006 | DD-UC-004 | FSD-UC-004 | Selectores Indicador/Criterio + GET uploadable |
| PM-015 | PR-IMPL-026 | DD-AGENT-003 | FSD-UC-024 / FSD-UC-004 | Historial de acciones del agente de evidencias |
| PM-016 | PR-IMPL-006 | DD-UC-004 | FSD-UC-004 / FSD-UC-019 | Espacio de carga de evidencias por subfase en estructura |

| PM-016 | N/A (Hotfix) | DD-UC-007 | FSD-UC-007 | Refinamiento y Hotfixes de Búsqueda Inteligente (FSD-UC-007): corrección JPQL, robustez de roles bypass TD, carga inicial y paginación. |
| PM-017 | N/A (Refinement) | DD-UC-007 | FSD-UC-007 | Integración de búsqueda interactiva (slash command /buscar) y tarjetas de resultados con modal de detalles en el Asistente Virtual. |
| PM-018 | N/A (Refinement) | DD-UC-007 | FSD-UC-007 | Refinamiento de Consola de Depuración y Fallback Inteligente ante Fallas del LLM |
| PM-019 | PR-IMPL-007-MCP | DD-UC-007-MCP | FSD-UC-007 | Implementación de Búsqueda Inteligente Multi-Token y Contextualizada con servidor MCP embebido en Java. |
## PM-001

| Campo | Valor |
| --- | --- |
| **ID** | PM-001 |
| **Fecha** | 2026-07-27 |
| **Solicitante** | Boris Anthony Angulo Urquieta |
| **Agente/Entorno** | Cursor IDE — Agent |
| **Modelo** | Composer |
| **Tarea** | Implementación MOD-ASSISTANT — chatbot FAQ acreditación (MVP) |
| **Objetivo** | Integrar asistente virtual en SIGESA: UI `/ayuda`, API backend, proxy a Open WebUI/Ollama, stack Docker Compose |
| **Contexto** | PRD-REQ-028 / BRD-REQ-024. MVP sin RAG ni persistencia; alineado a arquitectura hexagonal y reglas frontend (Orval, tokens UMSS). Branch `feature/chatbot-boris`. |
| **PR-IMPL vinculado** | [PR-IMPL-012](../../prompts/impl/PR-IMPL-012.md) |
| **DD vinculado** | [DD-SYS-002](../../design/DD-SYS-002.md) |
| **PRD / FSD vinculado** | PRD-REQ-028 (Chatbot FAQ normativo) |
| **Estado** | completado |

### Prompt usado exacto

```text
Implementar asistente virtual SIGESA (MOD-ASSISTANT):
- Backend: AssistantController, SendChatMessageService, OpenWebUiChatAdapter,
  AssistantProperties, AssistantModuleConfig, manejo de errores.
- Frontend: feature assistant en /ayuda, useAssistantChat, AssistantChatUI, Sidebar.
- Docker: ollama + open-webui en docker-compose; SIGESA_ASSISTANT_* en backend.
- Proxy Open WebUI API OpenAI-compatible; modelo llama3.2:3b; system prompt SIGESA.
```

### Entradas auxiliares

```text
docs/product/03_prd/PRD.md (PRD-REQ-028)
AGENTS.md
.cursor/rules/frontend-design.mdc
docker-compose.yml
```

### Archivos generados o modificados

**Backend (nuevos)**

- `adapter/in/web/AssistantController.java`
- `adapter/in/web/advice/AssistantExceptionHandler.java`
- `adapter/in/web/dto/AssistantStatusResponse.java`
- `adapter/in/web/dto/SendChatMessageRequest.java`
- `adapter/in/web/dto/SendChatMessageResponse.java`
- `adapter/in/web/dto/ChatMessageDto.java`
- `adapter/out/assistant/OpenWebUiChatAdapter.java`
- `application/port/in/SendChatMessageUseCase.java`
- `application/port/out/ChatCompletionPort.java`
- `application/service/assistant/SendChatMessageService.java`
- `config/AssistantProperties.java`
- `config/AssistantModuleConfig.java`
- `domain/model/ChatMessage.java`, `ChatRole.java`
- `domain/exception/AssistantUnavailableException.java`, `AssistantCompletionException.java`

**Backend (modificados)**

- `src/main/resources/application.yaml` (bloque `sigesa.assistant`)

**Frontend (nuevos)**

- `features/assistant/AssistantPage.tsx`
- `features/assistant/components/AssistantChatUI.tsx`
- `features/assistant/hooks/useAssistantChat.ts`
- `features/assistant/hooks/mapAssistantError.ts`
- `api/endpoints/assistant-controller/assistant-controller.ts`
- `api/model/assistantTypes.ts`

**Frontend (modificados)**

- `App.tsx` (ruta `/ayuda`)
- `components/layout/Sidebar.tsx` (nav AYUDA)

**Infraestructura**

- `docker-compose.yml` (servicios `ollama`, `open-webui`; env assistant en `backend`)
- `.env` (raíz, gitignored — `SIGESA_ASSISTANT_API_KEY`)

**Documentación**

- `docs/design/DD-SYS-002.md`
- `docs/prompts/impl/PR-IMPL-012.md`
- `docs/sprints/sprint_02/PROMPT_MAPPING.md` (este archivo)
- `docs/product/DTP.md` (§MOD-ASSISTANT, changelog)

### Cambios realizados

1. **Backend hexagonal:** caso de uso `SendChatMessageService` antepone system prompt institucional; adaptador `OpenWebUiChatAdapter` consume API Open WebUI con HTTP/1.1 forzado (fix uvicorn 400).
2. **API REST:** `GET /api/v1/assistant/status`, `POST /api/v1/assistant/chat` bajo JWT; errores 503/502 con códigos `ASSISTANT_UNAVAILABLE` / `ASSISTANT_COMPLETION_FAILED`.
3. **Frontend:** chat en `/ayuda` con optimistic UI, historial en memoria, hooks React Query; UI con tokens institucionales UMSS.
4. **Docker:** Ollama + Open WebUI en red interna; backend espera healthcheck de Open WebUI antes de arrancar.
5. **Operación:** API key Open WebUI en `.env`; rotación documentada en DD-SYS-002 §6.3.

### Notas de cierre

- Primera inferencia puede tardar 5–10 s (carga del modelo en Ollama).
- No commitear `.env` con `SIGESA_ASSISTANT_API_KEY`.
- Evolución: streaming, RAG normativo, persistencia — ver DD-SYS-002 §8.

---

## PM-002

| Campo | Valor |
| --- | --- |
| **ID** | PM-002 |
| **Fecha** | 2026-07-31 |
| **Solicitante** | Boris Anthony Angulo Urquieta |
| **Agente/Entorno** | Cursor IDE — Agent |
| **Tarea** | Contrato implementación tool calling read-only (Fase 1.1 MOD-ASSISTANT) |
| **Objetivo** | Documentar e implementar loop de tool calling en backend con tool `list_users` exclusiva JD |
| **Contexto** | Extiende PR-IMPL-012 / DD-SYS-002 §11. Catálogo [`TOOL-CATALOG.md`](../../design/assistant/TOOL-CATALOG.md). API [`API-USER-03`](../../product/api/API-USER-03.md). |
| **PR-IMPL vinculado** | [PR-IMPL-013](../../prompts/impl/PR-IMPL-013.md) |
| **DD vinculado** | [DD-SYS-002 §11](../../design/DD-SYS-002.md#11-tool-calling-fase-11--read-only) |
| **PRD / FSD vinculado** | PRD-REQ-028 · FSD-UC-002 (listado usuarios vía `ListUsersUseCase`) |
| **Estado** | completado |

### Prompt usado exacto

```text
Crear PR-IMPL-013 con el loop de tool calling:
- SendChatMessageService multi-turno (max 3 iteraciones).
- AssistantToolRegistry + AssistantToolExecutor.
- Tool list_users (solo JD) → ListUsersUseCase.
- Extender ChatCompletionPort / OpenWebUiChatAdapter (tools + tool_calls).
- Sin cambios contrato frontend POST /assistant/chat.
```

### Entradas auxiliares

```text
docs/design/DD-SYS-002.md §11
docs/design/assistant/TOOL-CATALOG.md
docs/product/api/API-USER-03.md
docs/prompts/impl/PR-IMPL-012.md
AGENTS.md
```

### Entregables documentales (esta fase)

| Tipo | Ruta |
| --- | --- |
| Contrato prompt | `docs/prompts/impl/PR-IMPL-013.md` |
| Catálogo tools | `docs/design/assistant/TOOL-CATALOG.md` |
| Contrato API | `docs/product/api/API-USER-03.md` |
| DD §11 | `docs/design/DD-SYS-002.md` |

### Criterios de cierre implementación

1. Loop tool calling operativo con `list_users` para JD.
2. CC/TD no reciben tool en payload LLM.
3. Tests §10 PR-IMPL-013 verdes; JaCoCo ≥ 90% clases assistant tocadas.
4. `@dtp-sync` actualiza DTP §B.5.
5. Estado PM-002 → completado.

---

## PM-003

| Campo | Valor |
| --- | --- |
| **ID** | PM-003 |
| **Fecha** | 2026-08-03 |
| **Solicitante** | Tech Lead / User |
| **Agente/Entorno** | Google Deepmind Antigravity Agent |
| **Modelo** | Gemini |
| **Tarea** | Implementación Real Database Persistence para UC-011 Dashboard (Frontend & Backend) |
| **Objetivo** | Quitar fallbacks mock, conectar a Postgres/H2, poblar datos via ApplicationRunner y remover select de simulación en frontend. |
| **Contexto** | FSD-UC-011 (Dashboard) / DD-UC-011. Integración completa de persistencia JPA y APIs sin stubs. |
| **PR-IMPL vinculado** | [PR-IMPL-011](../../prompts/impl/PR-IMPL-011.md) |
| **DD vinculado** | [DD-UC-011](../../design/DD-UC-011.md) |
| **PRD / FSD vinculado** | FSD-UC-011 (Dashboard) |
| **Estado** | completado |

### Prompt usado exacto

```text
pls check the function specs and the development contract for the uc-011 (dashbord) some actions are pointing to an specific harcoded data instead of the real database (postgres) so you need to review this and fix the documentation + this function from backend and frontend to show the real data instead of mocked data
```

### Entradas auxiliares

```text
AGENTS.md
docs/product/DTP.md
docs/design/DD-UC-011.md
```

### Archivos generados o modificados

**Backend (nuevos)**

- `config/DashboardDataLoader.java` (Semillado de datos del dashboard para H2/dev)
- `config/JpaConfig.java` (Aislamiento de la configuración JPA para tests)
- `db/seed.sql` (Semilla SQL inicial para inicialización limpia en el contenedor de Postgres)

**Backend (modificados)**

- `SigesaApplication.java` (Desacoplamiento de JPA bootstrapping)
- `adapter/out/persistance/JpaDashboardQueryAdapter.java` (Cálculo dinámico e integración multitenant)
- `adapter/out/persistance/IndicatorJpaRepository.java` (Consultas JPQL para filtrado de indicadores por fase)
- `adapter/out/persistance/ObservationJpaRepository.java` (Conteos programáticos de observaciones por estado)
- `adapter/out/persistance/repository/SpringDataAccreditationProcessRepository.java` (Conteo de procesos activos por carrera)
- `config/EvidenceDataLoader.java` (Alineación de SEED_PROGRAM_ID y robustez de asignaciones)
- `config/AuthDataLoader.java` (Robustez de inicialización de asignación de usuarios)

**Frontend (modificados)**

- `features/dashboard/api/dashboardHooks.ts` (Remoción completa de interceptores mock)
- `features/dashboard/pages/DashboardPage.tsx` (Remoción del selector de Persona)

**Infraestructura (modificados)**

- `docker-compose.yml` (Montaje de `db/seed.sql` para el servicio `db` y refactorización de healthcheck/dependencias de `open-webui`)

### Cambios realizados

1. **Persistencia Dinámica del Dashboard:** Implementación de cálculo en tiempo real en `JpaDashboardQueryAdapter`. Si existe un proceso de acreditación activo para la carrera, los KPIs, avances globales y fases se calculan en caliente desde las tablas operativas de evidencias e indicadores. Si no existe, se muestra el estado semillado en `tb_program_dashboard_summary`.
2. **Alineación de Datos de Desarrollo:** Corrección de la desalineación de IDs en `EvidenceDataLoader` hacia `DevSeedData.PROGRAM_INF_SIS`.
3. **Frontend React:** Eliminación definitiva de todos los fallbacks mock y refresco automático al impactar datos reales.
4. **Isolación de Tests Slice:** Creación de `JpaConfig` para que `@WebMvcTest` no intente inicializar repositorios JPA.
5. **Semillado Postgres Seguro:** Creación de un archivo `seed.sql` e integración a través de `/docker-entrypoint-initdb.d/` en `docker-compose.yml` para garantizar que cuando se inicie la base de datos Postgres por primera vez, cuente con la estructura y semilla lista de manera nativa.
6. **Robustez de Carga en Inits:** Corrección de `EvidenceDataLoader` y `AuthDataLoader` para garantizar la inserción de las relaciones `user_program_assignment` en cualquier reinicio, incluso si el usuario existía previamente.

---

## PM-004

| Campo | Valor |
| --- | --- |
| **ID** | PM-004 |
| **Fecha** | 2026-08-03 |
| **Hora** | 19:26 |
| **Solicitante** | Boris Anthony Angulo Urquieta |
| **Tarea** | Consulta de procesos de acreditación |
| **Objetivo** | Exponer `GET /api/v1/processes` y `GET /api/v1/processes/{id}` con RBAC JD/TD/CC; UI listado + detalle |
| **PR-IMPL vinculado** | [PR-IMPL-019](../../prompts/impl/PR-IMPL-019.md) |
| **DD vinculado** | [DD-UC-019](../../design/DD-UC-019.md) |
| **FSD / PRD vinculado** | FSD-UC-019 |
| **Estado** | completado |

### Criterios de cierre

1. GET listado + detalle con RBAC y filtro [CC] por carrera.
2. UI `/procesos` con sidebar desplegable.
3. Tests ProcessAccessPolicy / ListProcesses / GetProcessDetail verdes.

---

## PM-004

| Campo | Valor |
| --- | --- |
| **ID** | PM-004 |
| **Fecha** | 2026-08-03 |
| **Solicitante** | Aylen Gonzáles |
| **Agente/Entorno** | Cursor IDE — Agent |
| **Tarea** | Habilitación rol evaluador externo [EE] (MOD-REVIEW) |
| **Objetivo** | Documentar e implementar acceso solo lectura de [EE] a documentación de carrera asignada |
| **Contexto** | Rol planificado en BRD/MRD/PRD; v1.1. Alcance: login JWT, alta por [JD], dashboard KPI reutilizado, sin mutaciones. |
| **PR-IMPL vinculado** | [PR-IMPL-014](../../prompts/impl/PR-IMPL-014.md) |
| **DD vinculado** | [DD-UC-020](../../design/DD-UC-020.md) |
| **PRD / FSD vinculado** | PRD-REQ-029 · PRD-US-026 · FSD-UC-020 |
| **Estado** | completado |

### Prompt usado exacto

```text
Implementar rol EE (evaluador externo):
- Documentación: FSD-UC-020, DD-UC-020, PR-IMPL-014, PRD-US-026, FSD-BR-19.
- Backend: Role.EE, RegisterUserService scope, dashboard aggregation, SecurityConfig export.
- Frontend: roleLabels, CcOnlyRoute, Sidebar acotado, dashboard read-only.
- Seed dev: ee@umss.edu.bo / EvalDemo2026!
- Registrar PM-004 en sprint_02/PROMPT_MAPPING.md
```

### Archivos generados o modificados

**Documentación**

- `docs/product/uc/FSD-UC-020.md`
- `docs/design/DD-UC-020.md`
- `docs/prompts/impl/PR-IMPL-014.md`
- `docs/product/glosario.md`, `FSD.md`, `PRD.md`, `reglas_negocio.md`, `FSD-UC-002.md`, `DTP.md`
- `docs/sprints/sprint_02/PROMPT_MAPPING.md` (PM-004)

**Backend**

- `domain/model/Role.java`
- `application/service/auth/RegisterUserService.java`
- `application/service/dashboard/DashboardSummaryAggregationService.java`
- `adapter/in/security/SecurityConfig.java`
- `config/AuthDataLoader.java`
- `application/service/assistant/AssistantToolRegistry.java`
- `RegisterUserServiceTest.java`

**Frontend**

- `lib/auth/roleLabels.ts`
- `components/auth/CcOnlyRoute.tsx`
- `App.tsx`, `Sidebar.tsx`
- `features/dashboard/pages/DashboardPage.tsx`
- `features/dashboard/components/CoordinatorDashboardSection.tsx`
- `README.md`

### Criterios de cierre

1. [JD] registra [EE] con carrera asignada.
2. [EE] inicia sesión y ve dashboard solo lectura de su carrera.
3. POST evidencias / export / admin → 403 para EE.
4. Documentación viva actualizada (PRD, FSD, reglas, DTP).

---

## PM-005

| Campo | Valor |
| --- | --- |
| **ID** | PM-005 |
| **Fecha** | 2026-08-03 |
| **Solicitante** | Aylen Gonzáles |
| **Agente/Entorno** | Cursor IDE — Agent |
| **Tarea** | Mejora UI gestión de usuarios [JD] (MOD-AUTH) |
| **Objetivo** | Listado con nombre completo, modal de alta según mockup, validaciones de campos y entrega de credenciales solo al crear usuario |
| **Contexto** | Extiende FSD-UC-002 / DD-UC-002. Perfil extendido en BD; contraseña definida por JD (no recuperable después del alta). Sin campo cédula. |
| **PR-IMPL vinculado** | [PR-IMPL-015](../../prompts/impl/PR-IMPL-015.md) |
| **DD vinculado** | [DD-UC-002](../../design/DD-UC-002.md) |
| **PRD / FSD vinculado** | PRD-REQ-001 · PRD-US-002 · FSD-UC-002 |
| **Estado** | completado |

### Prompt usado exacto

```text
En la pantalla de gestión de usuarios debe verse la lista de usuarios registrados con nombre completo.
Botón "Agregar Usuario" → pop up centralizado (mockup Datos personales) con:
  Nombre(s), Apellido(s), Correo, Celular, Rol, Contraseña, Repetir contraseña.
  Sin Cédula de Identidad. Botones Guardar y Cerrar.
  Diálogo de confirmación al guardar con credenciales para compartir (contraseña no recuperable después).
Validaciones con mensajes claros:
  - Nombre/Apellido: al menos una letra; no solo números ni símbolos.
  - Celular: numérico 8 dígitos, rango 60000000–79999999 (Bolivia).
  - Correo: formato email estándar @umss.edu.bo.
  - Contraseña: coincidir y regla mínima de seguridad del sistema.
Backend: persistir firstName, lastName, phoneNumber; password del JD en POST.
Registrar PM-005 en sprint_02/PROMPT_MAPPING.md.
```

### Entradas auxiliares

```text
docs/design/DD-UC-002.md
docs/product/uc/FSD-UC-002.md
AGENTS.md
.cursor/rules/frontend-design.mdc
Mockup UI "AÑADIR USUARIO" (Aylen)
```

### Archivos generados o modificados

**Documentación**

- [ ] Rebuild backend Docker + `pnpm run generate:api`
- [ ] Verificación E2E con `jd@umss.edu.bo` y usuario CC seed
- [ ] JaCoCo ≥ 90% en servicios `process/*`

---

## PM-005

| Campo | Valor |
| --- | --- |
| **ID** | PM-005 |
| **Fecha** | 2026-08-04 |
| **Solicitante** | Tech Lead / User |
| **Agente/Entorno** | Google Deepmind Antigravity Agent |
| **Modelo** | Gemini 3.5 Flash |
| **Tarea** | Redondeo de KPIs a exactamente 2 decimales (Dashboard) |
| **Objetivo** | Resolver errores visuales donde los porcentajes de avance global, avance de fases y promedio global presentaban más de dos decimales tanto en las consultas de backend como en el renderizado del frontend. |
| **Contexto** | FSD-UC-011 (Dashboard) / DD-UC-011. Hotfix directo sobre lógica de persistencia backend y formateo en componentes de UI. |
| **PR-IMPL vinculado** | N/A (Hotfix / Tarea de código) |
| **DD vinculado** | [DD-UC-011](../../design/DD-UC-011.md) |
| **PRD / FSD vinculado** | FSD-UC-011 (Dashboard) |
| **Estado** | completado |

### Prompt usado exacto

```text
hi pls review the UC-011 related to the dashbaord for the backend, pls fix that the kpis shoudl be rounded to 2 decimals
the frontend is still the same showing more thn 2 decimals, pls check as well the frontned and backend for this
```

### Entradas auxiliares

```text
AGENTS.md
docs/design/DD-UC-011.md
```

### Archivos generados o modificados

**Backend (modificados)**

- `adapter/out/persistance/JpaDashboardQueryAdapter.java` (Redondeo matemático con Math.round en cálculos de progreso general, de fases y promedio general)

**Frontend (modificados)**

- `features/dashboard/components/CoordinatorDashboardSection.tsx` (Formateo .toFixed(2) para avance global e individual de fases)
- `features/dashboard/components/ExecutiveDashboardSection.tsx` (Formateo .toFixed(2) para avance institucional)

### Cambios realizados

1. **Ajuste en Backend:** Se agregaron operaciones de redondeo matemático `Math.round(value * 100.0) / 100.0` a las variables `progress`, `phaseProgress` y `averageProgress` calculadas dinámicamente en `JpaDashboardQueryAdapter.java`.
2. **Ajuste en Frontend:** Se implementó formateo de presentación `.toFixed(2)` en los componentes de React (`CoordinatorDashboardSection.tsx` y `ExecutiveDashboardSection.tsx`) para asegurar que todos los valores porcentuales se muestren con exactamente 2 decimales sin importar la representación exacta del tipo `number`.
3. **Mantenimiento y Resolución de Conflictos:** Se resolvieron conflictos Git legados en `PROMPT_MAPPING.md`, ordenando los identificadores de trazabilidad cronológicos.

### Validación ejecutada

- [x] `./mvnw test` — **OK** (119 pruebas verdes, regresiones completas limpias en persistencia y controladores)
- [x] `npx oxlint` — **OK** (Cero warnings y errores en 135 archivos frontend analizados)
- [x] `npx tsc -b` — **OK** (Verificación estricta de compilador TypeScript sin errores de tipos)
- `docs/prompts/impl/PR-IMPL-015.md`
- `docs/sprints/sprint_02/PROMPT_MAPPING.md` (PM-005)

**Backend**

- `db/migration/V4__app_user_profile_fields.sql`
- `domain/model/UserProfile.java`, `AppUser.java`
- `domain/exception/InvalidUserProfileException.java`, `WeakPasswordException.java`
- `application/port/in/RegisterUserUseCase.java`, `ListUsersUseCase.java`
- `application/service/auth/RegisterUserService.java`, `ListUsersService.java`
- `adapter/in/web/UserAdminController.java`
- `adapter/in/web/dto/RegisterUserRequest.java`, `UserAdminSummaryResponse.java`
- `adapter/in/web/advice/AuthExceptionHandler.java`
- `adapter/out/persistance/entity/AppUserEntity.java`
- `config/AuthDataLoader.java`
- `RegisterUserServiceTest.java`, `UserAdminControllerTest.java`, `ModAuthServiceIntegrationTest.java`

**Frontend**

- `features/admin/users/pages/UsersAdminPage.tsx`
- `features/admin/users/components/UsersTableUI.tsx`
- `features/admin/users/components/AddUserModalUI.tsx`
- `features/admin/users/components/UserSaveSuccessDialog.tsx`
- `features/admin/users/lib/userFormValidation.ts`
- `features/admin/users/hooks/useRegisterUserForm.ts`, `useUsersList.ts`
- `components/ui/TextInput.tsx`, `Select.tsx` (prop `requiredMark`)
- `api/model/registerUserRequest.ts`, `userAdminSummaryResponse.ts`
- Eliminado: `RegisterUserFormUI.tsx`

### Criterios de cierre

1. Listado muestra nombre completo, correo, celular, rol y estado desde BD.
2. Modal de alta con validaciones y diseño institucional UMSS.
3. Diálogo post-alta con credenciales copiables (solo al crear).
4. Contraseñas existentes no visibles (hash Argon2).
5. PM-005 registrado en este archivo.

---

## PM-006

| Campo | Valor |
| --- | --- |
| **ID** | PM-006 |
| **Fecha** | 2026-08-07 |
| **Hora** | 15:52 |
| **Solicitante** | Boris Anthony Angulo Urquieta |
| **Agente/Entorno** | Cursor IDE — Agent |
| **Modelo** | Composer |
| **Tarea** | Implementación backend gestión CRUD plantillas normativas |
| **Objetivo** | Exponer API-TPL-01…08 bajo `/api/v1/templates` (solo [JD]); migración Flyway V5; puerto `TemplateManagementPort`; validar plantilla PUBLISHED al crear proceso y clonar `referenceUrl` |
| **Contexto** | FSD-UC-021 / DD-UC-021 / PR-IMPL-021. Arquitectura hexagonal estricta; CQRS read (`TemplatePort`) vs write (`TemplateManagementPort`). |
| **PR-IMPL vinculado** | [PR-IMPL-021](../../prompts/impl/PR-IMPL-021.md) |
| **DD-UC vinculado** | [DD-UC-021](../../design/DD-UC-021.md) |
| **FSD-UC vinculado** | [FSD-UC-021](../../product/uc/FSD-UC-021.md) |
| **Estado** | completado |

### Prompt usado exacto

```text
@PR-IMPL-021.md (1-218)
```

### Archivos generados o modificados

**Backend (nuevos)**

- `resources/db/migration/V5__template_management.sql`
- `domain/model/TemplateStatus.java`; excepciones `Template*Exception`
- `application/port/in/*TemplateUseCase.java`; `application/port/out/TemplateManagementPort.java`
- `application/service/template/*Service.java`, `TemplateStructureValidator.java`
- `adapter/out/persistance/TemplateManagementJpaAdapter.java`, `mapper/TemplatePersistenceMapper.java`
- `adapter/in/web/TemplateController.java`; DTOs `Template*`, `UpsertTemplateRequestDto`
- Tests: `TemplateStructureValidatorTest`, `*TemplateServiceTest`, `TemplateControllerWebMvcTest`

**Backend (modificados)**

- `CreateProcessUseCaseImpl.java` (rechazo DRAFT; clonación `referenceUrl`)
- `ProcessModuleConfig.java`, `TemplateSeedDataLoader.java`, entidades JPA template/phase/subphase
- `ProcessExceptionHandler.java`, `SecurityConfig.java` (PathPattern Spring 7)
- `AuthDataLoader.java` (fix seed idempotente)

**Docs**

- `docs/product/api_contracts.md`, `FSD.md`, `reglas_negocio.md`, `FSD-UC-003.md`, `DD-UC-003.md`

### Validación ejecutada

- [x] `./mvnw test` — **OK** (135 tests, 0 failures, 1 skipped)

### Resultado obtenido

Backend UC-021 listo para merge y generación Orval (`PR-IMPL-021-FE` pendiente). API-TPL-01…08 documentada en OpenAPI; [CC] recibe 403 en `/templates`.

### Próximos pasos

- [ ] `PR-IMPL-021-FE` — UI `/admin/plantillas`
- [ ] `PR-IMPL-022` — estructura en proceso ACTIVE
- [ ] `PR-IMPL-023` — asignación responsable [CC]

---

## PM-007

| Campo | Valor |
| --- | --- |
| **ID** | PM-007 |
| **Fecha** | 2026-08-07 |
| **Hora** | 16:23 |
| **Solicitante** | Boris Anthony Angulo Urquieta |
| **Agente/Entorno** | Cursor IDE — Agent |
| **Modelo** | Composer |
| **Tarea** | Cierre trazabilidad documental PR-IMPL-021 |
| **Objetivo** | Registrar `@save-prompt-mapping` + sincronizar capa viva (`DTP`, `FSD`, `FSD-UC-021`, `DD-UC-021`) tras implementación backend UC-021 |
| **Contexto** | PM-006 registró la implementación; esta entrada cierra auditoría AI-SDLC solicitada explícitamente por el Tech Lead. Incluye delta dev Docker (Flyway vs Hibernate). |
| **PR-IMPL vinculado** | [PR-IMPL-021](../../prompts/impl/PR-IMPL-021.md) |
| **DD-UC vinculado** | [DD-UC-021](../../design/DD-UC-021.md) |
| **FSD-UC vinculado** | [FSD-UC-021](../../product/uc/FSD-UC-021.md) |
| **Estado** | completado |

### Prompt usado exacto

```text
@.agents/skills/dtp-sync/SKILL.md @.agents/skills/save-prompt-mapping/SKILL.md  sprint=2 pr=PR-IMPL-021 autor: Boris Anthony Angulo Urquieta
```

### Entradas auxiliares

- `docs/prompts/impl/PR-IMPL-021.md`
- `docs/sprints/sprint_02/PROMPT_MAPPING.md` (PM-006 previo)
- `docs/product/DTP.md`

### Archivos generados o modificados (cierre documental)

| Acción | Ruta |
| --- | --- |
| modificado | `docs/product/DTP.md` (§A.1 autor, §A.2 delta #13, §B.7 MOD-TEMPLATE) |
| modificado | `docs/product/FSD.md` (estado UC-021, changelog) |
| modificado | `docs/product/uc/FSD-UC-021.md` (estado Hecho backend) |
| modificado | `docs/design/DD-UC-021.md` (status implementado backend) |
| modificado | `docs/sprints/sprint_02/PROMPT_MAPPING.md` (PM-007) |
| modificado | `backend/src/main/resources/application-dev.yaml` (hotfix dev: ddl-auto update, flyway off) |

### Cambios realizados

1. **@save-prompt-mapping:** PM-006 ya existía; se añade PM-007 como registro de cierre documental (append-only).
2. **@dtp-sync:** DTP §B.7 MOD-TEMPLATE; delta §A.2 #13 (dev Docker Hibernate vs Flyway prod); FSD-UC-021 → Hecho (backend); autor changelog → Boris Anthony Angulo Urquieta.
3. **Baseline:** `docs/baseline/` intacto (verificado).

### Validación ejecutada

- [x] `./mvnw test` — **OK** (135 tests, PM-006)
- [x] Backend Docker `:8080` — **OK** tras hotfix `application-dev.yaml`

### Resultado obtenido

Trazabilidad `FSD-UC-021 → DD-UC-021 → PR-IMPL-021 → PM-006/PM-007 → DTP` cerrada. Frontend UI pendiente contrato `PR-IMPL-021-FE`.

### Próximos pasos

- [ ] `PR-IMPL-021-FE` — UI `/admin/plantillas` + `@save-prompt-mapping` + `@dtp-sync` Full-Stack
- [ ] `PR-IMPL-022` / `PR-IMPL-023`

---

## PM-008

| Campo | Valor |
| --- | --- |
| **ID** | PM-008 |
| **Fecha** | 2026-08-07 |
| **Hora** | 16:35 |
| **Solicitante** | Boris Anthony Angulo Urquieta |
| **Agente/Entorno** | Cursor IDE — Agent (sigesa-orchestrator) |
| **Modelo** | Composer |
| **Tarea** | Cierre documental PR-IMPL-022 (contrato UC-022) |
| **Objetivo** | Registrar trazabilidad del contrato `PR-IMPL-022` y sincronizar capa viva (`DTP`, `FSD`, `DD-UC-022`) vía `@dtp-sync` + `@save-prompt-mapping` |
| **Contexto** | Pasos 0–2 del pipeline completos (FSD-UC-022, DD-UC-022, PR-IMPL-022 aprobado). **Paso 3a backend NO ejecutado** — sin `ProcessStructurePort`, `ProcessStructureController` ni tests en repo. Depende de PR-IMPL-021 (V5). |
| **PR-IMPL vinculado** | [PR-IMPL-022](../../prompts/impl/PR-IMPL-022.md) |
| **DD-UC vinculado** | [DD-UC-022](../../design/DD-UC-022.md) |
| **FSD-UC vinculado** | [FSD-UC-022](../../product/uc/FSD-UC-022.md) |
| **Estado** | en_progreso |

### Prompt usado exacto

```text
@.cursor/agents/sigesa-orchestrator.md sprint=2 pr=PR-IMPL-022 solicitante="Boris Anthony Angulo Urquieta" — cierra con @.agents/rules/dtp-sync.mdc y @.agents/rules/save-prompt-mapping.mdc
```

### Entradas auxiliares

- `docs/prompts/impl/PR-IMPL-022.md`
- `docs/design/DD-UC-022.md`
- `docs/product/uc/FSD-UC-022.md`
- `docs/product/api_contracts.md` (API-PROC-05…08)
- `.cursor/agents/sigesa-orchestrator.md`

### Archivos generados o modificados (alcance contrato / cierre)

| Acción | Ruta |
| --- | --- |
| generado | `docs/prompts/impl/PR-IMPL-022.md` |
| generado | `docs/design/DD-UC-022.md` |
| generado | `docs/product/uc/FSD-UC-022.md` |
| modificado | `docs/product/api_contracts.md` (API-PROC-05…08, extensión GET detalle subfases) |
| modificado | `docs/product/reglas_negocio.md` (BR-21, BR-22, BR-23 trazabilidad UC-022) |
| modificado | `docs/product/FSD.md` (changelog UC-022) |
| modificado | `docs/product/DTP.md` (§A.1, §A.3, §B.2.1 planificado) |
| modificado | `docs/design/DD-UC-022.md` (status contrato aprobado) |
| modificado | `docs/sprints/sprint_02/PROMPT_MAPPING.md` (PM-008) |

### Cambios realizados (@dtp-sync)

1. DTP §A.1: fila MOD-PROCESS estructura UC-022 (contrato, sin código).
2. DTP §A.3: `FSD-UC-022` → **contrato aprobado**; implementación pendiente.
3. DTP §B.2.1: contrato técnico planificado API-PROC-05…08, puertos y guardas.
4. `DD-UC-022`: status → contrato aprobado (implementación pendiente).
5. Baseline `docs/baseline/` intacto.

### Validación ejecutada

- [ ] `./mvnw test` — **N/A** (sin implementación backend UC-022)
- [ ] Paso 3c Docker smoke — **N/A** (sin endpoints PROC-05…08)
- [x] Verificación repo: **sin** `ProcessStructurePort` / `ProcessStructureController` en `backend/src`

### Resultado obtenido

Trazabilidad documental `FSD-UC-022 → DD-UC-022 → PR-IMPL-022 → PM-008 → DTP` registrada. **Gap:** código backend y frontend (`PR-IMPL-022-FE`) pendientes; PM-008 permanece `en_progreso` hasta Paso 3 completado.

### Próximos pasos

- [ ] Ejecutar Paso 3a: implementar `PR-IMPL-022` (backend) tras merge PR-IMPL-021
- [ ] `./mvnw test` + Paso 3c Docker
- [ ] `PR-IMPL-022-FE` — UI `/procesos/{id}/estructura`
- [ ] Nuevo PM o actualizar estado PM-008 → `completado` post-implementación

---

## PM-009

| Campo | Valor |
| --- | --- |
| **ID** | PM-009 |
| **Fecha** | 2026-08-07 |
| **Hora** | 17:10 |
| **Solicitante** | Boris Anthony Angulo Urquieta |
| **Agente/Entorno** | Cursor IDE — Agent (sigesa-orchestrator) |
| **Modelo** | Composer |
| **Tarea** | Implementación Full-Stack FSD-UC-022 |
| **Objetivo** | CRUD estructural de fases/subfases en proceso ACTIVE (API-PROC-05…08) + UI `/procesos/{processId}/estructura`; cierre con `@dtp-sync` + `@save-prompt-mapping` |
| **Contexto** | PM-008 registró contrato documental. Pipeline orchestrator Pasos 3a–3c, 4 y 5. Fix JPA: operaciones granulares en `ProcessStructureJpaAdapter` (orphanRemoval / UUID preasignado). |
| **PR-IMPL vinculado** | [PR-IMPL-022](../../prompts/impl/PR-IMPL-022.md) |
| **DD-UC vinculado** | [DD-UC-022](../../design/DD-UC-022.md) |
| **FSD-UC vinculado** | [FSD-UC-022](../../product/uc/FSD-UC-022.md) |
| **Estado** | completado |

### Prompt usado exacto

```text
@sigesa-orchestrator fsd=FSD-UC-022 sprint=2 solicitante="Boris Anthony Angulo Urquieta"

run all steps
```

### Entradas auxiliares

- `docs/prompts/impl/PR-IMPL-022.md`
- `docs/design/DD-UC-022.md`
- `docs/product/uc/FSD-UC-022.md`
- `.cursor/agents/sigesa-orchestrator.md`

### Archivos generados o modificados (UC-022)

| Acción | Ruta |
| --- | --- |
| generado | `backend/.../ProcessStructureController.java`, services/guard/ports/adapters UC-022 |
| generado | `backend/src/test/.../ProcessStructureControllerWebMvcTest.java`, `ProcessStructureGuardTest`, `*ServiceTest` |
| generado | `frontend/src/features/processes/{ProcessStructure*,useProcessStructureEditor}` |
| generado | `frontend/src/api/endpoints/estructura-de-proceso/` |
| modificado | `ProcessController.java`, `ProcessResponseDto.java`, `ProcessExceptionHandler.java`, `ProcessModuleConfig.java` |
| modificado | `frontend/src/App.tsx`, `ProcessDetailView.tsx`, `ProcessPhaseTree.tsx` |
| modificado | `docs/product/DTP.md`, `FSD.md`, `uc/FSD-UC-022.md`, `design/DD-UC-022.md` |

### Cambios realizados (@dtp-sync)

1. DTP §A.1: fila implementación Full-Stack UC-022 (PM-009).
2. DTP §A.3: `FSD-UC-022` → **hecho (Full-Stack)**; 148 tests.
3. DTP §B.2.1: contrato planificado → detalle vigente con adaptadores y anti-patrones JPA.
4. `FSD-UC-022`: estado → **Hecho**; `DD-UC-022`: status → implementado.
5. Baseline `docs/baseline/` intacto.

### Validación ejecutada

- [x] `./mvnw test` — **148 tests OK**
- [x] `pnpm lint` + `tsc -b` — OK
- [x] Docker stack up — frontend `:3000` HTTP 200
- [ ] Paso 3c curl JWT smoke manual — pendiente verificación interactiva (login JD en UI)

### Resultado obtenido

Backend hexagonal UC-022 operativo con guardas BR-21/22/23; frontend editor estructural en ruta dedicada; Orval regenerado. Trazabilidad `FSD-UC-022 → DD-UC-022 → PR-IMPL-022 → PM-009 → DTP` cerrada.

### Próximos pasos

- [ ] Smoke E2E manual: JD en `/procesos/{id}/estructura`; CC → 403 en mutaciones

---

## PM-010

| Campo | Valor |
| --- | --- |
| **ID** | PM-010 |
| **Fecha** | 2026-08-07 |
| **Hora** | 17:20 |
| **Solicitante** | Boris Anthony Angulo Urquieta |
| **Agente/Entorno** | Cursor IDE — Agent (sigesa-orchestrator) |
| **Modelo** | Composer |
| **Tarea** | Implementación Full-Stack FSD-UC-023 |
| **Objetivo** | Asignación de responsable [CC] a proceso ACTIVE (API-PROC-09…11) + UI sección/modal; extensión UC-019 con campo `responsible` |
| **Contexto** | Pipeline orchestrator Pasos 3a–5. Flyway `V7__process_responsible.sql` (V6 ocupado por rol EE). Seed CC: `cc@umss.edu.bo`, `cc2@umss.edu.bo`. |
| **PR-IMPL vinculado** | [PR-IMPL-023](../../prompts/impl/PR-IMPL-023.md) |
| **DD-UC vinculado** | [DD-UC-023](../../design/DD-UC-023.md) |
| **FSD-UC vinculado** | [FSD-UC-023](../../product/uc/FSD-UC-023.md) |
| **Estado** | completado |

### Prompt usado exacto

```text
@sigesa-orchestrator fsd=FSD-UC-023 sprint=2 solicitante="Boris Anthony Angulo Urquieta"
```

### Validación ejecutada

- [x] `./mvnw test` — **157 tests OK**
- [x] `pnpm lint` + `tsc -b` — OK
- [x] `docker compose up -d --build backend frontend`
- [x] `pnpm run generate:api` — Orval OK

### Resultado obtenido

UC-023 operativo: [JD] asigna [CC] único por proceso ACTIVE con validación carrera (BR-09) y unicidad global (BR-20). Trazabilidad `FSD-UC-023 → DD-UC-023 → PR-IMPL-023 → PM-010 → DTP` cerrada.

---

## PM-011

| Campo | Valor |
| --- | --- |
| **ID** | PM-011 |
| **Fecha** | 2026-08-11 |
| **Hora** | 16:05 |
| **Solicitante** | Boris Anthony Angulo Urquieta |
| **Agente/Entorno** | Cursor IDE — Agent |
| **Modelo** | Composer |
| **Tarea** | Copiloto de fases embebido (MOD-ASSISTANT `agent=phases`) |
| **Objetivo** | Agente especializado en detalle/estructura de proceso: CC solo lectura; JD/TD lectura+escritura vía chat; tools de subfases; UI responsive; sync Orval; diseño `DD-AGENT-001` |
| **Contexto** | Extensión de PR-IMPL-013 (tool calling) + FSD-UC-022 (estructura proceso). Iteración 2 incluyó hotfixes en sesión: UUID placeholder del LLM, orden subfase duplicado, preview legible de orden. PR #28 mergeado en `main`. |
| **PR-IMPL vinculado** | [PR-IMPL-024](../../prompts/impl/PR-IMPL-024.md) |
| **DD vinculado** | [DD-AGENT-001](../../design/assistant/DD-AGENT-001.md) |
| **FSD / PRD vinculado** | [FSD-UC-022](../../product/uc/FSD-UC-022.md), PRD-REQ-028 |
| **Estado** | completado |

### Prompt usado exacto

```text
Siguiente iteración (cuando quieras)
Copiloto solo lectura para CC en su carrera asignada
Tools de subfases en el agente
Sincronizar OpenAPI/Orval (pnpm run generate:api) cuando el backend esté levantado
¿Quieres que documente esto en un DD-AGENT-001 o que afinemos el layout mobile del panel?

Prosigamos con esto
```

```text
Agrega la subfase «Evidencia docente» en la Fase 1 con enlace https://umss.edu.bo/ejemplo
(confirmo → error: Orden de subfase duplicado en la misma fase)
```

```text
no seria muy moroso hacer eso?, que pasaria si existen 3 subfases, haria todo eso 3 veces,
no seria mejor implementar primero una respuesta que diga el ultimo orden que hay y luego
aplicar el siguiente disponible?
```

```text
ya esta funcional, agrega cosas para hacer en un futuro en DD-AGENT-001.md para ir mejorando
```

```text
realiza el commit y pr al main
```

```text
puedes guardar todo esto en el prompt mapping de sprint 2
```

### Entradas auxiliares

- [DD-AGENT-001](../../design/assistant/DD-AGENT-001.md)
- [TOOL-CATALOG.md](../../design/assistant/TOOL-CATALOG.md)
- [DD-SYS-002 §11](../../design/DD-SYS-002.md)
- [PR-IMPL-013](../../prompts/impl/PR-IMPL-013.md) (tool calling base)
- [PR-IMPL-022](../../prompts/impl/PR-IMPL-022.md) (estructura proceso)
- PR GitHub: https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss/pull/28

### Archivos generados o modificados

| Acción | Ruta |
| --- | --- |
| generado | `backend/.../AssistantChatContext.java`, `AssistantAgentProfile.java`, `AssistantChatContextFactory.java`, `AssistantStructureLookup.java` |
| generado | `backend/.../dto/AssistantChatContextDto.java` |
| generado | `backend/src/test/.../AssistantStructureLookupTest.java` |
| generado | `frontend/.../PhasesCopilotPanel.tsx`, `usePhasesCopilot.ts` |
| generado | `frontend/src/api/model/assistantChatContextDto.ts`, `assistantDemoScenarioResponse.ts`, `getStatus1Params.ts` |
| generado | `docs/design/assistant/DD-AGENT-001.md` |
| generado | `docs/prompts/impl/PR-IMPL-024.md` |
| modificado | `AssistantController.java`, `SendChatMessageService.java`, `AssistantToolRegistry.java`, `AssistantToolExecutor.java`, `AssistantKeywordRouter.java`, `AssistantResponseFormatter.java`, `AssistantProcessResolver.java`, `AssistantCapabilitiesCatalog.java`, `AssistantModuleConfig.java` |
| modificado | `ProcessDetailView.tsx`, `ProcessStructureView.tsx` |
| modificado | `frontend/src/api/endpoints/assistant-controller/assistant-controller.ts`, `assistantTypes.ts`, Orval models |
| modificado | `docs/design/assistant/TOOL-CATALOG.md` |
| modificado | Tests: `AssistantToolRegistryTest`, `SendChatMessageServiceToolLoopTest`, `AssistantToolExecutorTest`, `AssistantControllerToolCallingTest` |

### Cambios realizados

1. **Agente `phases`:** `context.processId` + catálogo de fases en prompt; subset de 4 tools filtradas por rol.
2. **RBAC CC:** solo `list_process_phases` y `list_process_structure` en copiloto; UI `readOnly`.
3. **Tools nuevas:** `list_process_structure`, `manage_process_subphase` (CREATE/UPDATE/DELETE con confirmación).
4. **`AssistantStructureLookup`:** resuelve `UUID_FASE_1`, `phaseOrder`, nombres naturales.
5. **`SubphaseOrderPlan`:** preview con último orden existente + siguiente disponible (sin reintentos en bucle).
6. **Frontend:** panel colapsable mobile + sidebar sticky `xl+`; samples filtrados para CC.
7. **`DD-AGENT-001`:** diseño + backlog P1–P3 (UX confirmación, refresh árbol, KEYWORD CREATE, etc.).

### Validación ejecutada

- [x] `AssistantStructureLookupTest`, `SendChatMessageServiceToolLoopTest`, `AssistantToolRegistryTest` — OK (Maven Docker)
- [x] `pnpm run lint` + `pnpm run build` — OK
- [x] `pnpm run generate:api` con backend `:8080` — OK
- [x] `docker compose build backend` + smoke manual copiloto (CREATE subfase con confirmación) — OK (reportado por solicitante)
- [x] PR #28 → merge `60cb091` en `main`

### Resultado obtenido

Copiloto de fases operativo en `/procesos/{id}` y `/procesos/{id}/estructura`. CC consulta estructura en su carrera; JD/TD crean subfases vía chat con preview de orden y confirmación. Trazabilidad `FSD-UC-022 + PR-IMPL-013 → DD-AGENT-001 → PR-IMPL-024 → PM-011 → PR #28`.

### Próximos pasos

- [ ] `@dtp-sync` — registrar agente `phases` en DTP §MOD-ASSISTANT
- [ ] AGENT-UX-01 (P1): botones Confirmar/Cancelar en panel
- [ ] AGENT-BE-01 (P1): catálogo subfases en prompt
- [ ] AGENT-QA-01 (P1): E2E CC lectura + JD CREATE subfase

---

## PM-012

| Campo | Valor |
|---|---|
| **ID** | PM-012 |
| **Fecha** | 2026-08-11 |
| **Hora** | 22:45 |
| **Solicitante** | Cursor Agent |
| **Agente/Entorno** | sigesa-orchestrator (Cursor) |
| **Modelo** | Cursor Grok 4.5 |
| **Tarea** | Copiloto de usuarios embebido (MOD-ASSISTANT `agent=users`) |
| **Objetivo** | Extender FSD-UC-002 con agente conversacional JD-only en `/admin/users`, análogo a phases |
| **Contexto** | CRUD UC-002 ya Hecho; patrón DD-AGENT-001 / PR-IMPL-024 |
| **PR-IMPL vinculado** | [PR-IMPL-025](../../prompts/impl/PR-IMPL-025.md) |
| **DD vinculado** | [DD-AGENT-002](../../design/assistant/DD-AGENT-002.md) (+ DD-UC-002 CRUD) |
| **FSD-UC vinculado** | [FSD-UC-002](../../product/uc/FSD-UC-002.md) |
| **Estado** | completado (código listo; tests/Docker smoke pendientes por entorno local) |

### Prompt usado exacto

```text
Implementa el agente embebido `users` para SIGESA siguiendo el pipeline AI-SDLC.

## Entrada funcional
**FSD-UC-002** — Gestión de usuarios [JD]
Trazabilidad resuelta esperada: FSD-UC-002 → DD-UC-002 → PR-IMPL-002

NOTA IMPORTANTE: FSD-UC-002 ya está marcado como "Hecho" (gestión de usuarios CRUD tradicional). Este trabajo es una **extensión**: agente embebido conversacional `users` análogo al agente `phases` (DD-AGENT-001). …
(objetivo completo: UsersCopilotPanel, RBAC JD 403, tools list/get/create/manage_status/manage_assignment, DD-AGENT-002, TOOL-CATALOG, Orval, Paso 3c, code review, @dtp-sync, @save-prompt-mapping)
```

### Archivos generados / modificados (git status)

| Tipo | Ruta |
|------|------|
| generado | `docs/design/assistant/DD-AGENT-002.md` |
| generado | `docs/prompts/impl/PR-IMPL-025.md` |
| generado | `frontend/.../UsersCopilotPanel.tsx`, `useUsersCopilot.ts` |
| generado | `ManageUserProgramAssignmentUseCase/Service` + test |
| generado | `AssistantUserActionPlan`, `AssistantAgentAccessDeniedException` |
| modificado | Assistant* (registry, executor, controller, keyword, formatter, context, capabilities) |
| modificado | `TOOL-CATALOG.md`, `DTP.md`, `assistantTypes` / `assistantChatContextDto` |
| modificado | `UsersAdminPage.tsx` |

### Criterios cubiertos

1. **UI:** panel colapsable mobile + sidebar sticky desktop; solo JD (JdOnlyRoute + `session.role === 'JD'`).
2. **RBAC:** 403 en chat/status `agent=users` si rol ≠ JD.
3. **Tools:** `list_users`, `get_user_detail`, `create_user`, `manage_user_status`, `manage_user_assignment` + `UserActionPlan`.
4. **Docs:** DD-AGENT-002 + TOOL-CATALOG §8 + PR-IMPL-025.
5. **DTP:** changelog A.1 + §B.5 agentes embebidos.

### Validación ejecutada

- [ ] `./mvnw test` — **bloqueado** (PKIX / parent POM Spring Boot 4.1.0 no resoluble en host)
- [ ] Docker smoke Paso 3c — **bloqueado** (Docker Desktop no disponible)
- [ ] `pnpm run generate:api` — pendiente backend `:8080`; DTO Orval actualizado a mano
- [x] Code review arquitectónico ligero (hexagonal, DTO, JD-only) — OK
- [x] `@dtp-sync` + este `@save-prompt-mapping` — OK

### Resultado obtenido

Copiloto `agent=users` implementado de punta a punta (docs + backend + frontend). Validación runtime pendiente de entorno Maven/Docker del solicitante.

### Próximos pasos

- [ ] Arrancar Docker Desktop + `docker compose up -d --build` y smoke JD en `/admin/users`
- [ ] `./mvnw test` en host con m2 sano
- [ ] `pnpm run generate:api` contra `:8080`
- [ ] USERS-UX-01: botones Confirmar/Cancelar

---

## PM-013

| Campo | Valor |
|---|---|
| **ID** | PM-013 |
| **Fecha** | 2026-08-12 |
| **Hora** | 23:45 |
| **Solicitante** | Usuario |
| **Agente/Entorno** | Cursor Agent |
| **Modelo** | Cursor Grok 4.5 |
| **Tarea** | Implementar agente + MCP de control documental FSD-UC-024 / DD-AGENT-003 |
| **Objetivo** | Perfil `agent=evidence`, tools de lectura, UI EvidenceCopilotPanel, servidor MCP espejo |
| **Contexto** | Plan fase 1 solo lectura; patrón DD-AGENT-001/002; UC-008/009 fuera de alcance |
| **PR-IMPL vinculado** | PR-IMPL-026 |
| **DD vinculado** | DD-AGENT-003 |
| **FSD / PRD vinculado** | FSD-UC-024 / PRD-REQ-028 |
| **Estado** | completado |

### Prompt usado exacto

```text
implementar el plan para crear agente +MCP de la documentacion FSD-UC-024 + DD-AGENT-003
```

### Entradas auxiliares

- `docs/product/uc/FSD-UC-024.md`
- `docs/design/assistant/DD-AGENT-003.md`
- `docs/prompts/impl/PR-IMPL-026.md`
- `docs/design/assistant/TOOL-CATALOG.md`

### Archivos generados o modificados

| Acción | Ruta |
|---|---|
| generado | `docs/product/uc/FSD-UC-024.md` |
| generado | `docs/design/assistant/DD-AGENT-003.md` |
| generado | `docs/prompts/impl/PR-IMPL-026.md` |
| generado | `mcp/sigesa-evidence/**` |
| generado | `frontend/src/features/evidence/components/EvidenceCopilotPanel.tsx` |
| generado | `frontend/src/features/evidence/hooks/useEvidenceCopilot.ts` |
| generado | backend evidence control query/use cases/adapter |
| modificado | Assistant profile/registry/executor/keyword/controller |
| modificado | `EvidenceUploadPage.tsx`, `assistantTypes.ts`, `TOOL-CATALOG.md`, `DTP.md`, `FSD.md` |

### Validación ejecutada

- [x] Backend compile + tests assistant
- [x] `pnpm install` en `mcp/sigesa-evidence`
- [ ] Smoke E2E chat `agent=evidence` — pendiente entorno local

### Resultado obtenido

Agente `evidence` operativo en backend/FE; MCP stdio en `mcp/sigesa-evidence/README.md`.


---

## PM-014

| Campo | Valor |
|---|---|
| **ID** | PM-014 |
| **Fecha** | 2026-08-13 |
| **Hora** | 12:26 |
| **Solicitante** | Usuario |
| **Agente/Entorno** | Cursor Agent |
| **Modelo** | Cursor Grok 4.5 |
| **Tarea** | Selectores Indicador / Criterio en Cargar Evidencia |
| **Objetivo** | Reemplazar inputs UUID por selects legibles; GET uploadable filtra PENDIENTE/OBSERVADO por carrera del [CC]; criterio 1:1 al elegir indicador |
| **Contexto** | Plan selectores_indicador_criterio; FSD-UC-004 / DD-UC-004 |
| **PR-IMPL vinculado** | PR-IMPL-006 |
| **DD vinculado** | DD-UC-004 |
| **FSD / PRD vinculado** | FSD-UC-004 / PRD-US-005 |
| **Estado** | completado |

### Prompt usado exacto

```text
Mejorar esta frontend, mostrando opciones al usuario de los datos que debe escribir en indicador y creterio
```

### Archivos generados o modificados

| Acción | Ruta |
|---|---|
| modificado | `backend/.../entity/IndicatorEntity.java` |
| modificado | `backend/.../config/EvidenceDataLoader.java` |
| modificado | `backend/.../EvidenceControlQueryPort.java` |
| modificado | `backend/.../EvidenceControlJpaAdapter.java` |
| modificado | `backend/.../EvidenceModuleConfig.java` |
| modificado | `backend/.../security/SecurityConfig.java` |
| generado | `UploadableIndicator.java`, `ListUploadableIndicatorsUseCase.java`, `ListUploadableIndicatorsService.java` |
| generado | `UploadableIndicatorController.java`, `UploadableIndicatorResponse.java` |
| generado | `ListUploadableIndicatorsServiceTest.java` |
| generado | `frontend/.../api/fetchUploadableIndicators.ts` |
| generado | `frontend/.../hooks/useUploadableIndicators.ts` |
| modificado | `frontend/.../hooks/useEvidenceUpload.ts` |
| modificado | `frontend/.../components/EvidenceUploadUI.tsx` |
| modificado | `frontend/.../EvidenceUploadPage.tsx` |
| modificado | `docs/product/uc/FSD-UC-004.md`, `docs/design/DD-UC-004.md` |

### Resultado obtenido

[CC] elige indicador por etiqueta (`code — title`); criterio se fija automáticamente; API `GET /api/v1/indicators/uploadable` + seeds IND-01…03 PENDIENTE.


<<<<<<< HEAD
=======
---

>>>>>>> origin/main
## PM-015

| Campo | Valor |
|---|---|
| **ID** | PM-015 |
<<<<<<< HEAD
| **Fecha** | 2026-08-08 |
| **Hora** | 15:40 |
| **Solicitante** | Tech Lead / User |
| **Agente/Entorno** | Google Deepmind Antigravity Agent |
| **Modelo** | Gemini 3.5 Flash |
| **Tarea** | Buscar Evidencia Inteligente |
| **Objetivo** | Implementar búsqueda inteligente de evidencias de punta a punta con enrutamiento híbrido de consultas e integración frontend. |
| **Contexto** | FSD-UC-007 / DD-UC-007. Aislamiento por carrera FSD-BR-09. |
| **PR-IMPL vinculado** | [PR-IMPL-007](../../prompts/impl/PR-IMPL-007.md) |
| **DD-UC vinculado** | [DD-UC-007](../../design/DD-UC-007.md) |
| **FSD-UC vinculado** | [FSD-UC-007.md](../../product/uc/FSD-UC-007.md) |
=======
| **Fecha** | 2026-08-18 |
| **Hora** | 11:36 |
| **Solicitante** | Usuario |
| **Agente/Entorno** | Cursor Agent |
| **Modelo** | Cursor Grok 4.5 |
| **Tarea** | Historial de acciones del agente en Cargar Evidencia (FSD-UC-004 / UC-024) |
| **Objetivo** | Cada respuesta del copiloto registra tool, camino, fuentes y resumen en un historial de sesión |
| **Contexto** | Superficie `/evidencias/cargar` + agent=evidence |
| **PR-IMPL vinculado** | PR-IMPL-026 |
| **DD vinculado** | DD-AGENT-003 |
| **FSD / PRD vinculado** | FSD-UC-024 / FSD-UC-004 / PRD-REQ-028 |
>>>>>>> origin/main
| **Estado** | completado |

### Prompt usado exacto

```text
<<<<<<< HEAD
Por favor implementa el caso de uso FSD-UC-007 de punta a punta siguiendo rigurosamente el documento de diseño docs/design/DD-UC-007.md.
Asegúrate de:
1. Crear el contrato de prompt en docs/prompts/impl/PR-IMPL-007.md.
2. Implementar el backend (puertos, adaptadores, servicios, y el controlador REST expuesto que reciba el header X-AI-Enabled).
3. Implementar el control de acceso acotado por carrera (FSD-BR-09).
4. Ejecutar la regeneración de clientes con Orval en el frontend.
5. Crear las vistas/componentes del frontend consumiendo los hooks autogenerados.
6. Correr el code review y registrar la trazabilidad con @dtp-sync y @save-prompt-mapping.
```

### Entradas auxiliares

- `docs/product/uc/FSD-UC-007.md`
- `docs/design/DD-UC-007.md`
- `docs/prompts/impl/PR-IMPL-007.md`

=======
Para FSD-UC-004 — Cargar Evidencia, el agente Cuando cargue sus respuestas debe generar un historial de todo lo que hace.
```

>>>>>>> origin/main
### Archivos generados o modificados

| Acción | Ruta |
|---|---|
<<<<<<< HEAD
| generado | `backend/src/main/java/com/umss/sigesa/adapter/in/web/dto/EvidenceSearchDetailDto.java` |
| generado | `backend/src/main/java/com/umss/sigesa/adapter/in/web/dto/SearchQueryResponseDto.java` |
| generado | `backend/src/main/java/com/umss/sigesa/application/port/in/SearchEvidenceUseCase.java` |
| generado | `backend/src/main/java/com/umss/sigesa/application/port/out/SearchEvidenceQueryPort.java` |
| generado | `backend/src/main/java/com/umss/sigesa/application/port/out/AssistantQueryPort.java` |
| generado | `backend/src/main/java/com/umss/sigesa/application/service/evidence/SearchEvidenceService.java` |
| generado | `backend/src/main/java/com/umss/sigesa/adapter/out/persistance/SearchEvidenceJpaAdapter.java` |
| generado | `backend/src/main/java/com/umss/sigesa/adapter/out/assistant/SearchAssistantAdapter.java` |
| generado | `backend/src/main/java/com/umss/sigesa/adapter/in/web/SearchEvidenceController.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/config/EvidenceModuleConfig.java` |
| generado | `backend/src/test/java/com/umss/sigesa/application/service/evidence/SearchEvidenceServiceTest.java` |
| generado | `frontend/src/features/evidence/EvidenceSearchPage.tsx` |
| modificado | `frontend/src/App.tsx` |
| modificado | `frontend/src/components/layout/Sidebar.tsx` |

### Cambios realizados

- **Backend hexagonal:** Implementación completa de la búsqueda de evidencias por título y descripción filtrada por carrera (CC) o acceso global (TD/JD).
- **Asistente inteligente:** Adaptador OpenWebUI integrado con la herramienta `buscar_evidencias_por_parametros` y system prompt de enrutamiento. Fallback elegante y seguro.
- **Frontend React:** Creación de la pantalla de búsqueda con soporte de toggle de IA (header `X-AI-Enabled`), visualización de metadatos de enrutamiento y tabla de resultados.

### Validación ejecutada

- [x] `./mvnw test` — resultado: BUILD SUCCESS (todas las pruebas pasan con éxito)
- [x] `oxlint` y `tsc` — resultado: OK (sin errores ni warnings)
=======
| modificado | `frontend/.../hooks/useEvidenceCopilot.ts` |
| modificado | `frontend/.../components/EvidenceCopilotPanel.tsx` |
| modificado | `backend/.../AssistantResponseFormatter.java` |
| modificado | `docs/product/uc/FSD-UC-024.md`, `docs/product/uc/FSD-UC-004.md` |

### Resultado obtenido

Panel «Historial de acciones» en el copiloto; respuestas de tools de evidencia formateadas (no solo «Consulta completada»).

>>>>>>> origin/main

---

## PM-016

| Campo | Valor |
<<<<<<< HEAD
| --- | --- |
| **ID** | PM-016 |
| **Fecha** | 2026-08-09 |
| **Solicitante** | Tech Lead / User |
| **Agente/Entorno** | Antigravity AI |
| **Modelo** | Gemini |
| **Tarea** | Refinamiento y Hotfixes de Búsqueda Inteligente (FSD-UC-007) |
| **Objetivo** | Corregir JPQL, mejorar contraste de consola debug, robustecer roles (bypass TD), habilitar carga inicial y paginación. |
| **PR-IMPL vinculado** | N/A (Hotfix) |
| **DD vinculado** | [DD-UC-007](../../design/DD-UC-007.md) |
| **PRD / FSD vinculado** | FSD-UC-007 |
=======
|---|---|
| **ID** | PM-016 |
| **Fecha** | 2026-08-18 |
| **Hora** | 11:48 |
| **Solicitante** | Usuario |
| **Agente/Entorno** | Cursor Agent |
| **Modelo** | Cursor Grok 4.5 |
| **Tarea** | Espacio de carga de evidencias por subfase en Estructura del proceso |
| **Objetivo** | En cada subfase permitir adjuntar PDF/Word/Excel/imagen vía UC-004 (indicador + archivo) |
| **Contexto** | UI detalle proceso; evidencia sigue ligada a indicador (sin FK subfase) |
| **PR-IMPL vinculado** | PR-IMPL-006 |
| **DD vinculado** | DD-UC-004 |
| **FSD / PRD vinculado** | FSD-UC-004 / FSD-UC-019 |
>>>>>>> origin/main
| **Estado** | completado |

### Prompt usado exacto

```text
<<<<<<< HEAD
the user "Tecnico DUEA" shoudl be able to see all evidences uploaded in the system, but not has the same filter as the CC use... same error, this coudl be due to the seed applied pls check it for some reason its not returing those records, in addition pls include a test case to validate this case as well
=======
en esta estructura del proceso, crear un espacio para que permita subir evidencias de diferentes tipos de archivos en cada subfase
>>>>>>> origin/main
```

### Archivos generados o modificados

| Acción | Ruta |
|---|---|
<<<<<<< HEAD
| modificado | `backend/src/main/java/com/umss/sigesa/adapter/out/persistance/SearchEvidenceJpaAdapter.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/application/service/evidence/SearchEvidenceService.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/adapter/in/web/SearchEvidenceController.java` |
| modificado | `backend/src/test/java/com/umss/sigesa/application/service/evidence/SearchEvidenceServiceTest.java` |
| modificado | `frontend/src/features/evidence/EvidenceSearchPage.tsx` |
| modificado | `db/seed_evidences.sql` |

### Cambios realizados

- **Corección de Base de Datos (seed_evidences.sql):** Se agregaron los programas de CEUB y ARCU-SUR al catálogo de programas de la base de datos de pruebas, permitiendo que la consulta JPQL con INNER JOIN no descarte los registros correspondientes.
- **Robustez de Extracción de Roles:** Se modificó la extracción del rol del usuario autenticado en el controlador de backend para manejar de manera tolerante tanto los roles con prefijo `ROLE_` como sin él, evitando fallbacks indeseados al rol restrictivo de Coordinador (CC) en el caso del Técnico DUEA (TD).
- **Carga inicial y Paginación:** Se habilitó el listado completo de evidencias en consultas vacías y se añadió paginación del lado del cliente (5 elementos por página) con controles de navegación.
- **Tests unitarios e Integración:** Se escribió un nuevo caso de prueba en `SearchEvidenceServiceTest.java` que valida explícitamente el aislamiento de carrera para CC y el bypass para TD.

### Validación ejecutada

- [x] `./mvnw test` — resultado: BUILD SUCCESS (todas las pruebas pasan con éxito)
- [x] `oxlint` y `tsc` — resultado: OK (sin errores ni warnings)

---

## PM-017

| Campo | Valor |
| --- | --- |
| **ID** | PM-017 |
| **Fecha** | 2026-08-11 |
| **Solicitante** | Tech Lead / Boris Anthony Angulo Urquieta |
| **Agente/Entorno** | Antigravity AI Coding Assistant |
| **Modelo** | Gemini 2.0 |
| **Tarea** | Refinamiento de Búsqueda de Evidencias en Chatbot |
| **Objetivo** | Integrar búsqueda interactiva de evidencias dentro del asistente de ayuda. |
| **Contexto** | FSD-UC-007 / DD-UC-007. Implementación de comando `/buscar <query>` y `/search <query>` para evitar enrutamientos LLM ambiguos. Mapeo de tarjetas de evidencias y modal interactivo de detalles. |
| **PR-IMPL vinculado** | N/A (Refinamiento y alineación interactiva) |
| **DD vinculado** | [DD-UC-007](../../design/DD-UC-007.md) |
| **PRD / FSD vinculado** | [FSD-UC-007](../../product/uc/FSD-UC-007.md) |
| **Estado** | completado |

### Archivos modificados/creados

- **Creado:** N/A
- **Modificado:**
  - `backend/src/main/java/com/umss/sigesa/application/service/assistant/AssistantToolRegistry.java`
  - `backend/src/main/java/com/umss/sigesa/application/service/assistant/AssistantToolExecutor.java`
  - `backend/src/main/java/com/umss/sigesa/application/service/assistant/AssistantKeywordRouter.java`
  - `backend/src/main/java/com/umss/sigesa/application/service/assistant/AssistantResponseFormatter.java`
  - `backend/src/main/java/com/umss/sigesa/config/AssistantModuleConfig.java`
  - `frontend/src/features/assistant/components/AssistantChatUI.tsx`
  - `backend/src/test/java/com/umss/sigesa/application/service/assistant/AssistantToolRegistryTest.java`
  - `backend/src/test/java/com/umss/sigesa/application/service/assistant/AssistantToolExecutorTest.java`
  - `backend/src/test/java/com/umss/sigesa/application/service/assistant/SendChatMessageServiceToolLoopTest.java`

### Cambios realizados

- **Backend Tool & Routing:** Se creó la tool `buscar_evidencias` que invoca dinámicamente al caso de uso de búsqueda híbrida. Se implementó enrutamiento directo por palabras clave cuando el usuario usa el comando `/buscar <query>` o `/search <query>`.
- **Formateador de Respuestas:** El backend serializa la lista de evidencias en formato JSON cuando la tool es ejecutada.
- **Frontend Interactivo:** Se actualizó `AssistantChatUI.tsx` para interceptar la tool `buscar_evidencias`, parsear el JSON de la respuesta y renderizar tarjetas de evidencias inline con botones para ver el detalle en un modal premium interactivo.
- **Calidad de Código (Oxlint):** Se solucionó la advertencia de catch param no utilizado para garantizar compatibilidad con oxlint.

### Validación ejecutada

- [x] `./mvnw test` — resultado: BUILD SUCCESS (todas las pruebas pasan con éxito)
- [x] `pnpm run build` — resultado: OK (cero warnings, cero errores de TypeScript o OxLint)

---

## PM-018

| Campo | Valor |
| --- | --- |
| **ID** | PM-018 |
| **Fecha** | 2026-08-12 |
| **Solicitante** | Tech Lead / Boris Anthony Angulo Urquieta |
| **Agente/Entorno** | Antigravity AI Coding Assistant |
| **Modelo** | Gemini 3.5 Flash |
| **Tarea** | Refinamiento de Consola de Depuración, Fallback Inteligente y Descarga de Evidencias en Chatbot |
| **Objetivo** | Implementar la entrega dinámica de la traza de LLM del backend al frontend, solucionar simulación estática, agregar comandos de prefijo al chatbot y habilitar la descarga segura/autenticada de evidencias en los popups. |
| **Contexto** | FSD-UC-007 / DD-UC-007. Reporte de fallas, descarga segura de archivos con aislamiento por carrera y comandos de prefijo chatbot. |
| **PR-IMPL vinculado** | N/A (Refinamiento e integración funcional) |
| **DD vinculado** | [DD-UC-007](../../design/DD-UC-007.md) |
| **PRD / FSD vinculado** | [FSD-UC-007](../../product/uc/FSD-UC-007.md) |
| **Estado** | completado |

### Archivos modificados/creados

- **Creado:**
  - `backend/src/main/java/com/umss/sigesa/application/port/in/DownloadEvidenceUseCase.java`
  - `backend/src/main/java/com/umss/sigesa/application/service/evidence/DownloadEvidenceService.java`
  - `backend/src/main/java/com/umss/sigesa/domain/exception/EvidenceNotFoundException.java`
- **Modificado:**
  - `backend/src/main/java/com/umss/sigesa/adapter/in/web/dto/SearchQueryResponseDto.java`
  - `backend/src/main/java/com/umss/sigesa/adapter/out/assistant/SearchAssistantAdapter.java`
  - `backend/src/main/java/com/umss/sigesa/application/service/evidence/SearchEvidenceService.java`
  - `backend/src/main/java/com/umss/sigesa/application/port/out/SearchEvidenceQueryPort.java`
  - `backend/src/main/java/com/umss/sigesa/application/port/out/EvidenceBlobStoragePort.java`
  - `backend/src/main/java/com/umss/sigesa/adapter/out/persistance/SearchEvidenceJpaAdapter.java`
  - `backend/src/main/java/com/umss/sigesa/adapter/out/evidence/LocalFileEvidenceBlobStorageAdapter.java`
  - `backend/src/main/java/com/umss/sigesa/config/EvidenceModuleConfig.java`
  - `backend/src/main/java/com/umss/sigesa/adapter/in/web/SearchEvidenceController.java`
  - `backend/src/main/java/com/umss/sigesa/adapter/in/web/advice/EvidenceExceptionHandler.java`
  - `backend/src/main/java/com/umss/sigesa/application/service/assistant/AssistantKeywordRouter.java`
  - `backend/src/main/java/com/umss/sigesa/adapter/out/persistance/entity/TemplateJpaEntity.java`
  - `backend/src/main/java/com/umss/sigesa/config/AssistantModuleConfig.java`
  - `frontend/src/api/model/searchQueryResponseDto.ts`
  - `frontend/src/features/evidence/EvidenceSearchPage.tsx`
  - `frontend/src/features/assistant/components/AssistantChatUI.tsx`
  - `frontend/src/features/assistant/hooks/useAssistantChat.ts`

### Cambios realizados

- **Backend (Trazabilidad y Fallback):** Campo `llmThought` añadido a `SearchQueryResponseDto` y rellenado dinámicamente con logs de error o razonamientos reales en el servicio y adaptador.
- **Frontend (Trazabilidad Real):** Corrección de falsas simulaciones en la interfaz de depuración al detectar el Escenario 4 (Fallback) ante errores de conexión a la IA.
- **Chatbot Prefijos:** Añadido soporte para los prefijos `/search-evidence`, `/search-evidences`, `/buscar-evidencia` y `/buscar-evidencias` en el router de comandos directos del chatbot (`AssistantKeywordRouter.java`).
- **Descarga Segura:** Definido el caso de uso `DownloadEvidenceUseCase` y su servicio de descarga que lee bytes de storage con `EvidenceBlobStoragePort` aplicando reglas de aislamiento por carrera mediante `SearchEvidenceQueryPort.findVersionById`.
- **Controlador REST:** Exposición del endpoint `GET /api/v1/evidences/{versionId}/download` con extracción automática de rol y scopes de carrera.
- **Frontend (Popup Descarga):** Agregada la función `handleDownloadEvidence` y el botón premium "Descargar Archivo" en el modal de detalles de la evidencia que se abre desde el chatbot de ayuda.
- **Corrección de Contenedor Dev (Error SQL):** Se añadieron los atributos `columnDefinition` en `@Column` para los nuevos campos de `TemplateJpaEntity.java` asignándoles valores por defecto (`DEFAULT 'PUBLISHED'` y `DEFAULT now()`). Esto soluciona la excepción `PSQLException` en desarrollo cuando Hibernate (`ddl-auto: update`) intenta alterar la tabla `templates` pre-existente violando la restricción de nulos.
- **Resaltado de Comandos en Chat (Badges):** Se añadió soporte interactivo de chips/badges en el input del chatbot. Si el usuario escribe un comando válido (como `/search-evidences `) al inicio del texto, este se aísla visualmente como un bloque bloqueado e inalterable. Para eliminarlo, el usuario puede presionar `Backspace` sobre la caja vacía o hacer clic en el botón 'X' del chip. Al enviar el mensaje, el backend recibe de forma transparente el comando concatenado al texto.
- **Corrección de Serialización (Jackson `JavaTimeModule`):** Se registraron instancias de `JavaTimeModule` en los `ObjectMapper` configurados en `AssistantModuleConfig.java`. Esto corrige el fallo `"No se pudo serializar el resultado de la tool."` que ocurría al procesar registros de tipo `LocalDateTime` devueltos por la herramienta `buscar_evidencias`.

### Validación ejecutada

- [x] `./mvnw test` — resultado: BUILD SUCCESS (181 pruebas pasadas exitosamente con 0 fallos)
- [x] `./mvnw compile` — resultado: BUILD SUCCESS (código compilado con éxito)
- [x] `npx tsc --noEmit` — resultado: OK (frontend compila con cero errores de tipos)
- [x] `docker compose logs backend` — resultado: Container sigesa-backend se levanta establemente escuchando en 8080 sin crasheos.
- [x] `docker compose build backend && docker compose up -d backend` — resultado: Contenedor backend reconstruido y reiniciado exitosamente aplicando el módulo de Jackson.

---

## PM-019

| Campo | Valor |
|---|---|
| **ID** | PM-019 |
| **Fecha** | 2026-08-13 |
| **Hora** | 17:58 |
| **Solicitante** | Tech Lead / Alex |
| **Agente/Entorno** | Google Deepmind Antigravity Agent |
| **Modelo** | Gemini 3.5 Pro |
| **Tarea** | Implementación Búsqueda Inteligente Multi-Token (MCP) |
| **Objetivo** | Implementar la descomposición de frases complejas de búsqueda (Multi-Token) mediante servidor MCP embebido en Java (Spring AI) y búsqueda por trigramas (`pg_trgm`) en Postgres con visualización agrupada por subsets en el frontend. |
| **Contexto** | FSD-UC-007 / DD-UC-007-MCP. Contrato de prompt `PR-IMPL-007-MCP`. Aislamiento por carrera FSD-BR-09. |
| **PR-IMPL vinculado** | [PR-IMPL-007-MCP](../../prompts/impl/PR-IMPL-007-MCP.md) |
| **DD-UC vinculado** | [DD-UC-007-MCP](../../design/DD-UC-007-MCP.md) |
| **FSD-UC vinculado** | [FSD-UC-007.md](../../product/uc/FSD-UC-007.md) |
| **Estado** | completado |

### Prompt usado exacto

```text
pls execute the propm contract PR-IMPL-007-MCP, let me know if you have any question
```

### Entradas auxiliares

- `docs/product/uc/FSD-UC-007.md`
- `docs/design/DD-UC-007-MCP.md`
- `docs/prompts/impl/PR-IMPL-007-MCP.md`

### Archivos generados o modificados

| Acción | Ruta |
|---|---|
| generado | `backend/src/main/resources/db/migration/V8__mcp_trgm_search.sql` |
| generado | `backend/src/main/java/com/umss/sigesa/adapter/out/assistant/mcp/AcademicContextMcpServer.java` |
| generado | `backend/src/main/java/com/umss/sigesa/adapter/out/assistant/mcp/dto/UserContextDto.java` |
| generado | `backend/src/main/java/com/umss/sigesa/adapter/in/web/dto/SearchSubsetDto.java` |
| generado | `frontend/src/api/model/searchSubsetDto.ts` |
| modificado | `db/seed.sql` |
| modificado | `backend/src/main/java/com/umss/sigesa/adapter/in/web/dto/SearchQueryResponseDto.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/application/service/assistant/AssistantToolExecutor.java` |
| modificado | `backend/src/main/java/com/umss/sigesa/application/service/evidence/SearchEvidenceService.java` |
| modificado | `backend/src/test/java/com/umss/sigesa/application/service/evidence/SearchEvidenceServiceTest.java` |
| modificado | `backend/src/test/java/com/umss/sigesa/application/service/assistant/SendChatMessageServiceToolLoopTest.java` |
| modificado | `frontend/src/features/evidence/EvidenceSearchPage.tsx` |
| modificado | `frontend/src/features/evidence/hooks/useEvidenceUpload.ts` |
| modificado | `frontend/src/features/assistant/hooks/useAssistantChat.ts` |
| modificado | `frontend/src/features/processes/hooks/usePhasesCopilot.ts` |
| modificado | `frontend/src/features/admin/users/hooks/useUsersCopilot.ts` |
| modificado | `frontend/src/features/admin/users/components/UsersCopilotPanel.tsx` |
| modificado | `frontend/src/features/accreditation-process/hooks/useProgramSearch.ts` |
| modificado | `frontend/src/features/admin/users/hooks/useRegisterUserForm.ts` |
| modificado | `frontend/src/features/processes/components/PhasesCopilotPanel.tsx` |
| eliminado | `frontend/src/api/endpoints/evidence-controller` |
| eliminado | `frontend/src/api/endpoints/assistant-controller` |
| eliminado | `frontend/src/api/endpoints/program-catalog-controller` |
| generado | `frontend/src/features/processes/components/SubphaseEvidenceUploadSlot.tsx` |
| generado | `frontend/src/features/evidence/api/uploadEvidence.ts` |

- **Persistencia (Flyway V8 & Postgres Init Seed):** Migración SQL V8 para activar `pg_trgm` y crear índices GIN. Adicionalmente, se actualizaron el script de base de datos de PostgreSQL `db/seed.sql` para habilitar la extensión `pg_trgm`, crear índices trigram y poblar las tablas de programas, dimensiones y evidencias automáticamente al arrancar el contenedor PostgreSQL.
- **Servidor MCP embebido:** Implementado `AcademicContextMcpServer` usando Spring AI para ofrecer herramientas al motor LLM, extrayendo dinámicamente y agrupando evidencias en subconjuntos (`subsets`).
- **Aislamiento y Seguridad:** Validación del rol y de la carrera autorizada (`programScope`) en el servidor MCP para rechazar consultas de otras carreras si es Coordinador.
- **Frontend React:** Adaptación de `EvidenceSearchPage` para iterar y mostrar resultados agrupados en bloques/pestañas de subconjuntos. Actualización de hooks Orval y saneamiento de importaciones obsoletas en copilotos del frontend para compilación libre de warnings.

### Validación ejecutada

- [x] `./mvnw test` — resultado: BUILD SUCCESS (184 pruebas exitosas en host local)
- [x] `pnpm tsc -b` — resultado: exit code 0 (cero errores en compilación frontend)
- [x] `pnpm run lint` — resultado: exit code 0 (cero warnings de oxlint introducidos)

### Resultado obtenido

Búsqueda inteligente multi-token mediante servidor MCP embebido y trigram search en Postgres integrada y validada con cero errores en backend y frontend.
Cada subfase muestra zona de carga; [CC] elige indicador + archivo; [JD/TD] ven el espacio con enlace a Cargar evidencia.

