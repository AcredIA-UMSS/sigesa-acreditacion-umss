---
id: PR-IMPL-023
feature_asociado: DD-UC-023
fsd_uc:
  - "FSD-UC-023"
fecha: "2026-08-07"
version: "1.0"
estado: Aprobado
autor: "AI Prompt Architect (@sigesa-prompt-contract-architect)"
skill_origen: sigesa-prompt-contract-architect
alcance: backend-spring-boot
depende_de: []
bloquea_a: []
---

# Prompt Contract — Implementación `PR-IMPL-023`

> **Generado vía** `@sigesa-prompt-contract-architect`.  
> **Design doc fuente:** [`DD-UC-023`](../../design/DD-UC-023.md) · **FSD:** [`FSD-UC-023`](../../product/uc/FSD-UC-023.md) · **Reglas:** `FSD-BR-09`, `FSD-BR-20`, `FSD-BR-23`.

---

## 1. Propósito y Objetivo

Implementar **asignación de responsable [CC]** a proceso de acreditación según [`DD-UC-023`](../../design/DD-UC-023.md):

- Tabla **`process_responsible_assignment`** + Flyway **`V6__process_responsible.sql`**.
- Endpoints **API-PROC-09…11** (asignar, quitar, listar candidatos).
- Puerto **`ProcessResponsiblePort`** + use cases de asignación.
- Extender **UC-019** (`ProcessSummaryResponseDto`, `ProcessResponseDto`) con `ProcessResponsibleDto` opcional.
- Un [CC] solo responsable de **un** proceso con asignación activa (FSD-BR-20).

**Frontend:** contrato complementario **`PR-IMPL-023-FE`** post-Orval.

---

## 2. Rol y Persona

- **Identidad:** Desarrollador Backend Senior SIGESA (Hexagonal).
- **Expertise:** Java 21, Spring Boot 4.x, JPA, índices únicos parciales PostgreSQL, Spring Security.

---

## 3. Límites de Alcance

### In-Scope

| Área | Entregables |
|---|---|
| **Flyway** | `V6__process_responsible.sql` (ver DD-UC-023 §2.2) |
| **Dominio** | `ProcessResponsibleAssignment`; excepciones `CcAlreadyAssignedToProcessException`, `InvalidResponsibleUserException`, `CareerScopeMismatchException` |
| **Modelo app** | `ProcessResponsibleInfo` record |
| **Puertos IN** | `AssignProcessResponsibleUseCase`, `RemoveProcessResponsibleUseCase`, `ListEligibleResponsiblesUseCase` |
| **Puertos OUT** | **`ProcessResponsiblePort`** |
| **Reutiliza** | `ProcessQueryPort` o `AccreditationProcessPort` (load process + status), `UserProgramAssignmentRepositoryPort`, repositorio usuarios (rol CC) |
| **JPA** | `ProcessResponsibleAssignmentJpaEntity`, `SpringDataProcessResponsibleRepository` |
| **Adaptador OUT** | `ProcessResponsibleJpaAdapter` |
| **Adaptador IN** | Endpoints en `ProcessController` o `ProcessResponsibleController` |
| **DTOs** | `AssignResponsibleRequestDto`, `ProcessResponsibleDto`, `EligibleResponsibleDto` |
| **UC-019** | Enriquecer list/detail con responsable (join por processId) |
| **Advice** | Handlers 409/400 |
| **Tests** | Ver §6.4 |
| **OpenAPI** | API-PROC-09…11 |

### Out-of-Scope

- UI modal asignación (PR-IMPL-023-FE).
- Notificaciones al [CC] (UC-015).
- Múltiples responsables por proceso.
- Auto-revoke al COMPLETED proceso (hook futuro; documentar TODO en comentario de servicio **prohibido** en código — usar nota en DD solamente).
- Modificar `docs/baseline/`.

---

## 4. Restricciones y Reglas

| ID | Regla |
|---|---|
| R1 | Dominio puro; entidad JPA solo en adapter OUT. |
| R2 | Solo **[JD]** muta: PUT/DELETE responsible; GET candidates. |
| R3 | Proceso debe estar **`ACTIVE`** para asignar/quitar → `409 PROCESS_NOT_EDITABLE`. |
| R4 | Usuario candidato: rol **CC**, cuenta **ACTIVE** → else `400 INVALID_RESPONSIBLE_USER`. |
| R5 | `user_program_assignment` activo con `program_id == process.career_id` → else `409 CAREER_SCOPE_MISMATCH` (FSD-BR-09). |
| R6 | Un [CC] con asignación activa en otro proceso → `409 CC_ALREADY_ASSIGNED_TO_PROCESS` (FSD-BR-20). |
| R7 | Cambiar responsable: **revoke** soft fila anterior (`revoked_at`) antes de insertar nueva. |
| R8 | Índices únicos parciales en BD refuerzan R6 (uk_pra_active_process, uk_pra_active_user). |
| R9 | DTO responsable: `userId`, `fullName`, `email`, `assignedAt` — **sin** password/hash. |
| R10 | JaCoCo ≥ **90%** en `AssignProcessResponsibleService`, `ListEligibleResponsiblesService`, `RemoveProcessResponsibleService`. |
| R11 | Post-merge: `@save-prompt-mapping PR-IMPL-023` → `@dtp-sync`. |

---

## 5. Especificaciones de Entrada

### Documentos obligatorios

| Documento | Uso |
|---|---|
| [`DD-UC-023`](../../design/DD-UC-023.md) | ER, flujo, API |
| [`FSD-UC-023`](../../product/uc/FSD-UC-023.md) | Gherkin TC-23 |
| [`DD-UC-019`](../../design/DD-UC-019.md) | Extensión DTOs |
| [`DD-UC-002`](../../design/DD-UC-002.md) | Usuarios CC |
| `UserProgramAssignmentRepositoryPort.java`, `ListUsersService.java` | Patrones existentes |

### Escenarios Gherkin → tests

| Escenario | Test |
|---|---|
| Asignar CC disponible | `AssignProcessResponsibleServiceTest` |
| CC ya responsable otro proceso | → 409 |
| Carrera incompatible | → 409 CAREER_SCOPE_MISMATCH |
| Quitar responsable | `RemoveProcessResponsibleServiceTest` |
| Candidatos excluye CC ocupados | `ListEligibleResponsiblesServiceTest` |
| Detalle UC-019 muestra responsable | `GetProcessDetailServiceTest` (integración mock) |
| CC PUT responsible | WebMvc → 403 |

---

## 6. Especificaciones de Salida

### 6.1 Firma `ProcessResponsiblePort`

```java
ProcessResponsibleAssignment save(ProcessResponsibleAssignment assignment);
Optional<ProcessResponsibleAssignment> findActiveByProcessId(UUID processId);
Optional<ProcessResponsibleAssignment> findActiveByUserId(UUID userId);
void revokeActiveByProcessId(UUID processId);
Set<UUID> findUserIdsWithActiveAssignment(); // candidatos a excluir
```

### 6.2 Endpoints REST

#### API-PROC-09 — PUT `/processes/{processId}/responsible`

**Request:**
```json
{ "userId": "660e8400-e29b-41d4-a716-446655440099" }
```

**Response 200:**
```json
{
  "userId": "660e8400-e29b-41d4-a716-446655440099",
  "fullName": "María Coordinadora",
  "email": "maria.coord@umss.edu.bo",
  "assignedAt": "2026-08-07T15:00:00"
}
```

#### API-PROC-10 — DELETE `/processes/{processId}/responsible` → 204

#### API-PROC-11 — GET `/processes/{processId}/responsible/candidates` → 200

```json
[
  {
    "userId": "660e8400-e29b-41d4-a716-446655440099",
    "fullName": "María Coordinadora",
    "email": "maria.coord@umss.edu.bo"
  }
]
```

Filtros candidatos:
- Rol CC, ACTIVE.
- `user_program_assignment` activo para `process.career_id`.
- Excluir `userId ∈ findUserIdsWithActiveAssignment()` salvo el responsable actual del proceso (permite re-selección al cambiar).

### 6.3 Extensión UC-019 DTOs

```java
@Builder
public class ProcessResponsibleDto {
    UUID userId;
    String fullName;
    String email;
    LocalDateTime assignedAt;
}
// ProcessSummaryResponseDto + ProcessResponseDto: ProcessResponsibleDto responsible; // nullable
```

`ListProcessesService` / `GetProcessDetailService`: cargar responsable vía port; no N+1 — batch opcional v1.0 (procesos < 100).

### 6.4 Definition of Done

- [ ] `./mvnw -q test` verde.
- [ ] JaCoCo ≥ 90% (R10).
- [ ] Migración V6 aplicada; índices únicos verificados en `@DataJpaTest`.
- [ ] GET list/detail proceso incluye `responsible` cuando existe.
- [ ] PR: `FSD-UC-023` · `DD-UC-023` · `PR-IMPL-023`.

---

## 7. Anti-patrones

- ❌ Columna `responsible_user_id` directa en `accreditation_processes` (sin historial).
- ❌ Permitir dos asignaciones activas mismo CC (viola BR-20).
- ❌ Asignar [TD] o [JD] como responsable.
- ❌ Exponer hash de contraseña en DTO.

---

## 8. Verificación manual

1. [JD] → GET candidates para proceso ACTIVE → lista CC de carrera libres.
2. PUT responsible → 200.
3. GET `/processes/{id}` → campo `responsible` poblado.
4. Intentar mismo CC en segundo proceso ACTIVE → 409.
5. DELETE responsible → 204; CC reaparece en candidates.
6. [CC] → PUT responsible → 403.

---

## 9. Trazabilidad

```text
FSD-UC-023 → DD-UC-023 → PR-IMPL-023 (backend) → PR-IMPL-023-FE (frontend)
Extiende: DD-UC-019 (lectura responsable)
```

**Paralelismo:** Implementable en paralelo con PR-IMPL-022 (migración V6 independiente de V5).
