---
id: PR-IMPL-019
feature_asociado: DD-UC-019
fsd_uc:
  - "FSD-UC-019"
fecha: "2026-08-03"
version: "1.1"
estado: Aprobado
autor: "AI Prompt Architect (@sigesa-prompt-contract-architect)"
skill_origen: sigesa-prompt-contract-architect
alcance: backend-spring-boot
---

# Prompt Contract — Implementación Backend `PR-IMPL-019`

> **Generado vía** `@sigesa-prompt-contract-architect`.  
> **Design doc fuente:** [`DD-UC-019`](../../design/DD-UC-019.md) · **FSD:** [`FSD-UC-019`](../../product/uc/FSD-UC-019.md) · **Reglas:** `FSD-BR-09`, `FSD-BR-17`.

---

## 1. Propósito y Objetivo

Generar e implementar el **código Java/Spring Boot** para la consulta de procesos de acreditación (`MOD-PROCESS`), strictly según [`DD-UC-019`](../../design/DD-UC-019.md):

- Exponer **`GET /api/v1/processes`** (listado resumido) y **`GET /api/v1/processes/{processId}`** (detalle con árbol Fase → Subfase).
- Aplicar **RBAC** para roles **[JD]**, **[TD]** y **[CC]** con aislamiento por `programScope` (`FSD-BR-09`).
- Reutilizar persistencia existente (`accreditation_processes`, `phases`, `subphases`) sin migraciones Flyway nuevas.
- Entregar suite de pruebas unitarias y WebMvc con cobertura JaCoCo ≥ 90% en servicios de aplicación nuevos.

**Alcance explícito de este contrato:** solo backend Spring Boot. Frontend React/Orval queda **fuera de scope** (contrato separado futuro).

---

## 2. Rol y Persona

- **Identidad:** Desarrollador Backend Senior especializado en SIGESA (Arquitectura Hexagonal).
- **Tono:** Técnico, preciso, sin atajos ni placeholders.
- **Expertise requerida:**
  - Java 21, Spring Boot 4.x, Spring Data JPA, Spring Security JWT.
  - Patrón Puertos y Adaptadores (dominio puro).
  - JUnit 5, Mockito, `@WebMvcTest`.
  - OpenAPI/Swagger annotations para contrato REST.

---

## 3. Límites de Alcance

### In-Scope

| Área | Entregables |
|---|---|
| **Puertos OUT** | `ProcessQueryPort` + `ProcessQueryJpaAdapter` |
| **Puertos IN** | `ListProcessesUseCase`, `GetProcessDetailUseCase` |
| **Servicios aplicación** | `ListProcessesService`, `GetProcessDetailService`, `ProcessAccessPolicy` |
| **Dominio/excepciones** | `ProcessNotFoundException` en `domain.exception` |
| **Modelos aplicación** | `ProcessQueryContext`, `ProcessSummary` |
| **Persistencia** | Extender `SpringDataAccreditationProcessRepository` (`findAllByOrderByStartDateDesc`, `findByCareerIdInOrderByStartDateDesc`, `findWithPhasesById` con `@EntityGraph`) |
| **Mapper** | Extender `ProcessPersistenceMapper` o crear `ProcessQueryMapper` con ordenamiento por `order` |
| **REST** | Extender `ProcessController` con 2 endpoints GET |
| **DTOs** | `ProcessSummaryResponseDto`; extender `ProcessResponseDto` con campos de enriquecimiento |
| **Excepciones HTTP** | Handler `ProcessNotFoundException` → 404 en `ProcessExceptionHandler` |
| **Config** | Beans en `ProcessModuleConfig` |
| **OpenAPI** | Annotations en endpoints GET (para consumo posterior de Orval) |
| **Tests** | `ProcessAccessPolicyTest`, `ListProcessesServiceTest`, `GetProcessDetailServiceTest`, `ProcessControllerQueryTest` |
| **Docs post-código** | Actualizar `docs/product/api_contracts.md` (API-PROC-03, API-PROC-04) |

### Out-of-Scope

- Pantallas React, hooks Orval, rutas frontend, Sidebar.
- Paginación, filtros por estado/plantilla, búsqueda full-text.
- Edición de fases/subfases, evidencias, indicadores, dashboard KPIs.
- Migraciones Flyway / cambios DDL.
- Modificar `docs/baseline/`.
- Refactorizar `POST /api/v1/processes` (CreateProcess) salvo ajustes mínimos de DTO compartido.

---

## 4. Restricciones y Reglas

### Restricciones duras

| ID | Regla |
|---|---|
| R1 | Dominio y casos de uso **sin** anotaciones Spring/JPA/Hibernate. |
| R2 | Controladores exponen **DTOs Lombok `@Builder`** existentes; **nunca** entidades `@Entity`. |
| R3 | Puerto de lectura **`ProcessQueryPort` separado** de `AccreditationProcessPort` (no mezclar write/read). |
| R4 | [CC] consulta proceso fuera de `programScope` → lanzar `ProcessNotFoundException` → HTTP **404** con `{ "error": "PROCESS_NOT_FOUND", ... }`. **Prohibido 403** en este caso. |
| R5 | [JD] y [TD] ven **todos** los procesos sin filtro por carrera. |
| R6 | Listado **no** debe cargar subfases en memoria (consulta sin `@EntityGraph` de phases/subphases). |
| R7 | Detalle **debe** cargar fases + subfases y ordenarlas por campo `order` ascendente antes de mapear a DTO. |
| R8 | Enriquecimiento vía **`ProgramCatalogPort.findById`** y **`TemplatePort.findById`** — no JOIN manual cross-module en SQL. |
| R9 | Extracción de `programScope` en controlador: patrón **`UserProgramAssignmentRepositoryPort.findActiveByUserId`** (referencia: `DashboardCompositeController.extractProgramScopes`). |
| R10 | Roles Spring Security: `@PreAuthorize("hasAnyRole('JD','TD','CC')")` en ambos GET. |
| R11 | JaCoCo ≥ **90%** en `ListProcessesService`, `GetProcessDetailService`, `ProcessAccessPolicy`. |
| R12 | Post-implementación: `@save-prompt-mapping PR-IMPL-019` → `@dtp-sync`. |

### Límites funcionales

- Sin paginación: devolver lista completa en memoria (v1.0, < 100 procesos esperados).
- Sin caché distribuido.
- Sin endpoints de mutación (solo lectura).

### Palabras/patrones prohibidos en código generado

- `// TODO`, `// FIXME`, métodos vacíos, `throw new UnsupportedOperationException`.
- Roles genéricos (`Admin`, `SuperUser`) — usar **[JD]**, **[TD]**, **[CC]** en comentarios/tests.
- `@Autowired` en field injection — usar constructor injection (Lombok `@RequiredArgsConstructor` o manual como en proyecto).

---

## 5. Especificaciones de Entrada

### Documentos obligatorios (leer antes de codificar)

| Documento / archivo | Uso |
|---|---|
| [`DD-UC-019`](../../design/DD-UC-019.md) | Diseño hexagonal, contratos API, RBAC |
| [`FSD-UC-019`](../../product/uc/FSD-UC-019.md) | Gherkin TC-19, matriz autorización |
| [`docs/product/reglas_negocio.md`](../../product/reglas_negocio.md) | FSD-BR-09 (aislamiento [CC]) |
| `ProcessController.java` | Extender; no romper POST existente |
| `ProcessResponseDto.java` | Extender campos de lectura |
| `AccreditationProcessJpaEntity.java` | Modelo JPA existente |
| `ProcessPersistenceMapper.java` | Reutilizar `toDomain` |
| `ProcessExceptionHandler.java` | Añadir handler 404 |
| `ProgramCatalogPort.java` / `TemplatePort.java` | Enriquecimiento |
| `DashboardCompositeController.java` | Patrón `extractProgramScopes` |

### Contexto de autenticación (entrada runtime)

El controlador construye `ProcessQueryContext` desde el JWT autenticado:

```java
record ProcessQueryContext(String role, List<UUID> programScope) {}
```

**Derivación de `role`** desde `Authentication.getAuthorities()`:

| Authority detectada | `role` |
|---|---|
| `ROLE_JD` o `JD` | `"JD"` |
| `ROLE_TD` o `TD` | `"TD"` |
| `ROLE_CC` o `CC` | `"CC"` |

Prioridad si múltiples: JD > TD > CC (caso edge; en producción un usuario tiene un rol principal).

**Derivación de `programScope`:** IDs de carrera activos vía `UserProgramAssignmentRepositoryPort.findActiveByUserId(userId)`.

### Escenarios Gherkin → casos de prueba obligatorios

| Escenario FSD | Test mínimo |
|---|---|
| [JD] ve todos | `ListProcessesServiceTest` — N procesos, sin filtro |
| [TD] ve todos | `ListProcessesServiceTest` — mismo comportamiento que JD |
| [CC] solo su carrera | `ListProcessesServiceTest` — filtro `careerId ∈ programScope` |
| [CC] no abre ajeno por ID | `GetProcessDetailServiceTest` — `ProcessNotFoundException` |
| Detalle ordenado | `GetProcessDetailServiceTest` — phases/subphases por `order` |
| [CC] sin procesos | `ListProcessesServiceTest` — lista vacía `[]` |

---

## 6. Especificaciones de Salida

### 6.1 Estructura de paquetes

```
com.umss.sigesa.application.port.in
  → ListProcessesUseCase.java
  → GetProcessDetailUseCase.java

com.umss.sigesa.application.port.out
  → ProcessQueryPort.java

com.umss.sigesa.application.model.process
  → ProcessQueryContext.java
  → ProcessSummary.java

com.umss.sigesa.application.service.process
  → ProcessAccessPolicy.java
  → ListProcessesService.java
  → GetProcessDetailService.java

com.umss.sigesa.domain.exception
  → ProcessNotFoundException.java

com.umss.sigesa.adapter.out.persistance
  → ProcessQueryJpaAdapter.java

com.umss.sigesa.adapter.out.persistance.repository
  → SpringDataAccreditationProcessRepository.java  (extend)

com.umss.sigesa.adapter.in.web
  → ProcessController.java  (extend)

com.umss.sigesa.adapter.in.web.dto
  → ProcessSummaryResponseDto.java
  → ProcessResponseDto.java  (extend)

com.umss.sigesa.adapter.in.web.advice
  → ProcessExceptionHandler.java  (extend)

com.umss.sigesa.config
  → ProcessModuleConfig.java  (extend)

src/test/java/com/umss/sigesa/...
  → ProcessAccessPolicyTest.java
  → ListProcessesServiceTest.java
  → GetProcessDetailServiceTest.java
  → ProcessControllerQueryTest.java
```

### 6.2 Contratos REST

#### API-PROC-03 — `GET /api/v1/processes`

**Request:** Sin body. Header `Authorization: Bearer <JWT>`.

**Response 200:**

```json
[
  {
    "id": "950e8400-e29b-41d4-a716-446655440020",
    "careerId": "550e8400-e29b-41d4-a716-446655440000",
    "careerCode": "INF-SIS",
    "careerName": "Ingeniería de Sistemas",
    "templateId": "850e8400-e29b-41d4-a716-446655440010",
    "templateName": "CEUB 2026",
    "templateType": "CEUB",
    "status": "ACTIVE",
    "startDate": "2026-08-03T10:00:00",
    "phaseCount": 2,
    "subphaseCount": 5
  }
]
```

**Response 200 vacío (CC sin procesos en alcance):** `[]`

#### API-PROC-04 — `GET /api/v1/processes/{processId}`

**Response 200:**

```json
{
  "id": "950e8400-e29b-41d4-a716-446655440020",
  "careerId": "550e8400-e29b-41d4-a716-446655440000",
  "careerCode": "INF-SIS",
  "careerName": "Ingeniería de Sistemas",
  "templateId": "850e8400-e29b-41d4-a716-446655440010",
  "templateName": "CEUB 2026",
  "templateType": "CEUB",
  "status": "ACTIVE",
  "startDate": "2026-08-03T10:00:00",
  "phases": [
    {
      "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "name": "Autoevaluación",
      "order": 1,
      "subphases": [
        {
          "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
          "name": "Diagnóstico institucional",
          "order": 1
        }
      ]
    }
  ]
}
```

#### Tabla de respuestas HTTP

| Escenario | Código | Body |
|---|---|---|
| Listado exitoso | 200 | `ProcessSummaryResponseDto[]` |
| Detalle exitoso | 200 | `ProcessResponseDto` enriquecido |
| Sin JWT / token inválido | 401 | (Spring Security default) |
| `processId` inexistente | 404 | `{ "error": "PROCESS_NOT_FOUND", "message": "..." }` |
| [CC] accede a proceso ajeno | 404 | `{ "error": "PROCESS_NOT_FOUND", "message": "..." }` |
| Rol no autorizado | 403 | Spring Security `@PreAuthorize` |

### 6.3 Interfaces de puertos (firma exacta)

```java
// ProcessQueryPort
List<AccreditationProcess> findAllSummaries();
List<AccreditationProcess> findSummariesByCareerIds(List<UUID> careerIds);
Optional<AccreditationProcess> findDetailById(UUID id);

// ListProcessesUseCase
List<ProcessSummary> list(ProcessQueryContext ctx);

// GetProcessDetailUseCase
AccreditationProcess getDetail(UUID processId, ProcessQueryContext ctx);

// ProcessAccessPolicy
boolean canAccess(String role, UUID careerId, List<UUID> programScope);
void assertCanAccess(String role, UUID careerId, List<UUID> programScope); // throws ProcessNotFoundException
```

### 6.4 Criterio de aceptación técnica (Definition of Done)

- [ ] `./mvnw -q test` pasa sin fallos en tests nuevos.
- [ ] JaCoCo ≥ 90% en servicios de aplicación listados en R11.
- [ ] OpenAPI documenta GET endpoints en Swagger UI.
- [ ] `POST /api/v1/processes` sigue funcionando (regresión manual o test existente).
- [ ] PR declara: `FSD-UC-019` · `DD-UC-019` · `PR-IMPL-019`.

---

## 7. Anti-patrones y Violaciones

- ❌ Mezclar métodos de lectura en `AccreditationProcessPort` (viola separación CQRS del DD).
- ❌ Devolver **403** a [CC] en proceso ajeno (filtra existencia cross-carrera; viola FSD-UC-019 A2).
- ❌ Exponer entidades JPA en el controlador.
- ❌ Cargar `@EntityGraph` de subfases en el listado (impacto performance).
- ❌ `@Service` o `@Transactional` en clases de dominio.
- ❌ Consultas SQL con JOIN a `programs`/`templates` desde adapter JPA (usar puertos de catálogo).
- ❌ Omitir ordenamiento por `order` en detalle.
- ❌ Usar roles genéricos en tests (`ROLE_ADMIN`).
- ❌ Modificar `docs/baseline/`.

---

## 8. Checklist de Validación del Contrato

- [x] **Propósito** definido sin ambigüedad (backend Spring Boot only).
- [x] **Rol y Persona** específicos (Backend Senior SIGESA).
- [x] **Scope In/Out** explícito y exhaustivo.
- [x] **Restricciones** R1–R12 con reglas duras.
- [x] **Entrada** con documentos, contexto JWT y escenarios Gherkin mapeados.
- [x] **Salida** con paquetes, JSON de ejemplo y tabla HTTP.
- [x] **Ninguna suposición hallucinada** — basado en código existente verificado.
- [x] **Invariantes SIGESA** respetados:
  - [x] FSD-BR-09: [CC] aislado por carrera.
  - [x] Roles [JD], [TD], [CC] usados correctamente.
  - [x] Arquitectura hexagonal (dominio puro).
  - [x] Baseline no modificado.

---

## 9. Verificación manual post-implementación

1. `./mvnw spring-boot:run` (perfil dev) con PostgreSQL/H2 seed.
2. Login JWT [JD] (`jd@umss.edu.bo`) → `GET /api/v1/processes` → lista completa.
3. Login [CC] seed → `GET /api/v1/processes` → solo procesos de carrera asignada.
4. [CC] → `GET /api/v1/processes/{id-ajeno}` → 404 `PROCESS_NOT_FOUND`.
5. [JD] → `GET /api/v1/processes/{id}` → árbol fases/subfases ordenado.

---

## 10. Trazabilidad

```text
FSD-UC-019 → DD-UC-019 → PR-IMPL-019 (backend) → código Java
```

Frontend (Orval/React) **no cubierto** por este contrato; usar prompt separado cuando corresponda.
