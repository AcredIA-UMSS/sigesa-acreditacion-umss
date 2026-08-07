---
id: PR-IMPL-021
feature_asociado: DD-UC-021
fsd_uc:
  - "FSD-UC-021"
fecha: "2026-08-07"
version: "1.0"
estado: Aprobado
autor: "AI Prompt Architect (@sigesa-prompt-contract-architect)"
skill_origen: sigesa-prompt-contract-architect
alcance: backend-spring-boot
depende_de: []
bloquea_a:
  - "PR-IMPL-022"
---

# Prompt Contract — Implementación `PR-IMPL-021`

> **Generado vía** `@sigesa-prompt-contract-architect`.  
> **Design doc fuente:** [`DD-UC-021`](../../design/DD-UC-021.md) · **FSD:** [`FSD-UC-021`](../../product/uc/FSD-UC-021.md) · **Reglas:** `FSD-BR-21`, `FSD-BR-23`.

---

## 1. Propósito y Objetivo

Implementar el **backend Spring Boot** para gestión CRUD de **plantillas normativas** (`Template` → `TemplatePhase` → `TemplateSubphase`) según [`DD-UC-021`](../../design/DD-UC-021.md):

- Exponer **API-TPL-01…08** bajo `/api/v1/templates` (solo **[JD]**).
- Migración Flyway **`V5__template_management.sql`** (columnas `description`, `status`, `reference_url`, timestamps).
- Puerto de escritura **`TemplateManagementPort`** separado de `TemplatePort` (lectura).
- Ajuste mínimo en **`CreateProcessUseCaseImpl`**: rechazar plantillas no `PUBLISHED`; clonar `referenceUrl`/`description` a subfases de proceso.
- Tests unitarios + WebMvc; JaCoCo ≥ 90% en validator y servicios de publicación/gestión.

**Frontend React/Orval:** contrato separado **`PR-IMPL-021-FE`** (generar tras merge backend + `pnpm run generate:api`).

---

## 2. Rol y Persona

- **Identidad:** Desarrollador Backend Senior SIGESA (Arquitectura Hexagonal estricta).
- **Expertise:** Java 21, Spring Boot 4.x, JPA, Flyway, Lombok, JUnit 5, Mockito, OpenAPI.

---

## 3. Límites de Alcance

### In-Scope

| Área | Entregables |
|---|---|
| **Flyway** | `V5__template_management.sql` (ver DD-UC-021 §2.2) |
| **Dominio** | Extender `Template`, `TemplatePhase`, `TemplateSubphase`; enum `TemplateStatus`; excepciones de dominio listadas en DD |
| **Validator** | `TemplateStructureValidator` (puro Java) |
| **Puertos IN** | `CreateTemplateUseCase`, `UpdateTemplateUseCase`, `GetTemplateUseCase`, `ListTemplatesUseCase`, `PublishTemplateUseCase`, `ArchiveTemplateUseCase`, `DuplicateTemplateUseCase`, `DeleteTemplateUseCase` |
| **Puertos OUT** | **`TemplateManagementPort`** + extender consultas en `SpringDataTemplateRepository` |
| **Servicios** | Implementaciones `@Service` transaccionales por use case |
| **Adaptador IN** | `TemplateController`; DTOs request/response |
| **Adaptador OUT** | `TemplateManagementJpaAdapter`; extender `ProcessPersistenceMapper` / template mapper |
| **Advice** | `TemplateExceptionHandler` o extender handler existente |
| **Config** | Beans en `ProcessModuleConfig` |
| **Seed** | Actualizar `TemplateSeedDataLoader` con `status=PUBLISHED`, `referenceUrl` por subfase |
| **UC-003 hook** | `CreateProcessUseCaseImpl`: validar `PUBLISHED`; clonar URL al crear proceso |
| **OpenAPI** | Annotations en todos los endpoints TPL |
| **Tests** | Ver §6.4 |
| **Docs** | Confirmar `api_contracts.md` API-TPL-01…08 |

### Out-of-Scope

- UI `/admin/plantillas/**` (PR-IMPL-021-FE).
- Indicadores/criterios en subfase.
- Versionado diff de plantillas (v1.1).
- Import CSV/Excel.
- Modificar `docs/baseline/`.
- Refactor masivo de `ProcessController` salvo hook UC-003.

---

## 4. Restricciones y Reglas

| ID | Regla |
|---|---|
| R1 | Dominio **sin** anotaciones Spring/JPA. |
| R2 | Controladores exponen **DTOs**; nunca `@Entity`. |
| R3 | **`TemplateManagementPort` separado** de `TemplatePort` (CQRS). |
| R4 | Solo **[JD]**: `@PreAuthorize("hasRole('JD')")` en `TemplateController`. |
| R5 | `referenceUrl` obligatorio en subfase: patrón `^https://.+`, no blank → `400 TEMPLATE_SUBPHASE_LINK_REQUIRED`. |
| R6 | Publicar exige ≥1 fase, ≥1 subfase total, todas con URL válida → `400 TEMPLATE_STRUCTURE_INCOMPLETE`. |
| R7 | `order` único por nivel → `400 TEMPLATE_ORDER_CONFLICT`. |
| R8 | `type` solo `CEUB` \| `ARCU-SUR`. |
| R9 | DELETE solo plantilla `DRAFT` sin procesos referenciados; si hay procesos → `409 TEMPLATE_IN_USE`. |
| R10 | Editar plantilla **no** altera procesos ACTIVE existentes (FSD-BR-21 — diseño snapshot al clonar). |
| R11 | JaCoCo ≥ **90%** en `TemplateStructureValidator`, `PublishTemplateService`, `DeleteTemplateService`, `DuplicateTemplateService`. |
| R12 | Post-merge: `@save-prompt-mapping PR-IMPL-021` → `@dtp-sync`. |

### Prohibido

- `// TODO`, `UnsupportedOperationException`, field `@Autowired`.
- Roles genéricos (`Admin`) — usar **[JD]** en tests/comentarios.
- Mezclar endpoints TPL dentro de `ProcessController`.

---

## 5. Especificaciones de Entrada

### Documentos obligatorios

| Documento | Uso |
|---|---|
| [`DD-UC-021`](../../design/DD-UC-021.md) | Modelo, API, diagrama |
| [`FSD-UC-021`](../../product/uc/FSD-UC-021.md) | Gherkin TC-21 |
| [`DD-UC-003`](../../design/DD-UC-003.md) | Hook clonación proceso |
| `TemplateJpaEntity.java`, `TemplatePort.java`, `CreateProcessUseCaseImpl.java` | Código base |
| [`reglas_negocio.md`](../../product/reglas_negocio.md) | BR-21, BR-23 |

### Escenarios Gherkin → tests obligatorios

| Escenario | Test |
|---|---|
| Crear plantilla con fases/subfases/enlaces | `CreateTemplateServiceTest` |
| Publicar plantilla completa | `PublishTemplateServiceTest` |
| Publicada no migra proceso ACTIVE | `CreateProcessUseCaseImplTest` + integración |
| Subfase sin enlace | `TemplateStructureValidatorTest` |
| DELETE con proceso referenciado | `DeleteTemplateServiceTest` |
| CC accede API | `TemplateControllerWebMvcTest` → 403 |

---

## 6. Especificaciones de Salida

### 6.1 Paquetes (nuevos / extendidos)

```
domain/model/TemplateStatus.java
domain/model/Template.java (+ description, status, timestamps)
domain/model/TemplatePhase.java (+ description)
domain/model/TemplateSubphase.java (+ referenceUrl, description)
domain/exception/Template*.java

application/port/in/CreateTemplateUseCase.java … DeleteTemplateUseCase.java
application/port/out/TemplateManagementPort.java
application/service/template/TemplateStructureValidator.java
application/service/template/*Service.java

adapter/out/persistance/TemplateManagementJpaAdapter.java
adapter/out/persistance/repository/SpringDataTemplateRepository.java (extend)
adapter/in/web/TemplateController.java
adapter/in/web/dto/Template*.java
adapter/in/web/advice/TemplateExceptionHandler.java

resources/db/migration/V5__template_management.sql
config/ProcessModuleConfig.java (extend)
config/TemplateSeedDataLoader.java (extend)

test/.../TemplateStructureValidatorTest.java
test/.../PublishTemplateServiceTest.java
test/.../TemplateControllerWebMvcTest.java
```

### 6.2 Firma `TemplateManagementPort`

```java
Template save(Template template);
Optional<Template> findByIdForEdit(UUID id);
List<Template> findByStatusAndType(Optional<TemplateStatus> status, Optional<String> type);
boolean existsActiveProcessByTemplateId(UUID templateId);
void delete(UUID templateId);
```

### 6.3 Contratos REST (resumen)

| Endpoint | Éxito | Error clave |
|---|---|---|
| GET `/templates` | 200 `[TemplateSummaryResponseDto]` | 403 |
| POST `/templates` | 201 DRAFT | 400 link/structure |
| GET `/templates/{id}` | 200 detalle | 404 |
| PUT `/templates/{id}` | 200 | 400 |
| DELETE `/templates/{id}` | 204 | 409 TEMPLATE_IN_USE |
| POST `/templates/{id}/publish` | 200 PUBLISHED | 400 |
| POST `/templates/{id}/duplicate` | 201 DRAFT copia | 404 |
| POST `/templates/{id}/archive` | 200 ARCHIVED | 404 |

### 6.4 Definition of Done

- [ ] `./mvnw -q test` verde.
- [ ] JaCoCo ≥ 90% (R11).
- [ ] Swagger documenta API-TPL-01…08.
- [ ] `POST /processes` rechaza plantilla DRAFT (`TEMPLATE_NOT_PUBLISHED`).
- [ ] Proceso nuevo clona `referenceUrl` en subfases.
- [ ] PR: `FSD-UC-021` · `DD-UC-021` · `PR-IMPL-021`.

---

## 7. Anti-patrones

- ❌ Write methods en `TemplatePort` read-only.
- ❌ CASCADE delete plantilla usada por procesos.
- ❌ HTTP permitido en `referenceUrl` (solo HTTPS v1.0).
- ❌ Modificar árbol de procesos ACTIVE al editar plantilla.

---

## 8. Verificación manual

1. `./mvnw spring-boot:run` (dev).
2. Login [JD] → POST plantilla DRAFT con 1 fase / 1 subfase + URL.
3. POST publish → status PUBLISHED.
4. POST `/processes` con esa plantilla → 201 con subfase.referenceUrl clonada.
5. PUT plantilla (agregar subfase) → proceso ACTIVE previo **sin** nueva subfase.
6. Login [CC] → GET `/templates` → 403.

---

## 9. Trazabilidad

```text
FSD-UC-021 → DD-UC-021 → PR-IMPL-021 (backend) → PR-IMPL-021-FE (frontend, posterior)
```
