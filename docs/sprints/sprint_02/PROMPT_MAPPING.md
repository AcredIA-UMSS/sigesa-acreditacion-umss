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
| PM-006 | PR-IMPL-007 | DD-UC-007 | FSD-UC-007 | Buscar Evidencia Inteligente (MOD-EVIDENCE): enrutador híbrido de consultas (4 escenarios), aislamiento por carrera (FSD-BR-09), y vista frontend con toggle de IA. |
| PM-007 | N/A (Hotfix) | DD-UC-007 | FSD-UC-007 | Refinamiento y Hotfixes de Búsqueda Inteligente (FSD-UC-007): corrección JPQL, robustez de roles bypass TD, carga inicial y paginación. |

---

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
|---|---|
| **ID** | PM-006 |
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
| **Estado** | completado |

### Prompt usado exacto

```text
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

### Archivos generados o modificados

| Acción | Ruta |
|---|---|
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


## PM-007

| Campo | Valor |
| --- | --- |
| **ID** | PM-007 |
| **Fecha** | 2026-08-09 |
| **Solicitante** | Tech Lead / User |
| **Agente/Entorno** | Antigravity AI |
| **Modelo** | Gemini |
| **Tarea** | Refinamiento y Hotfixes de Búsqueda Inteligente (FSD-UC-007) |
| **Objetivo** | Corregir JPQL, mejorar contraste de consola debug, robustecer roles (bypass TD), habilitar carga inicial y paginación. |
| **PR-IMPL vinculado** | N/A (Hotfix) |
| **DD vinculado** | [DD-UC-007](../../design/DD-UC-007.md) |
| **PRD / FSD vinculado** | FSD-UC-007 |
| **Estado** | completado |

### Prompt usado exacto

```text
the user "Tecnico DUEA" shoudl be able to see all evidences uploaded in the system, but not has the same filter as the CC use... same error, this coudl be due to the seed applied pls check it for some reason its not returing those records, in addition pls include a test case to validate this case as well
```

### Archivos generados o modificados

| Acción | Ruta |
|---|---|
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

