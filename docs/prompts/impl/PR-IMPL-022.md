---
id: PR-IMPL-022
feature_asociado: DD-UC-022
fsd_uc:
  - "FSD-UC-022"
fecha: "2026-08-07"
version: "1.0"
estado: Aprobado
autor: "AI Prompt Architect (@sigesa-prompt-contract-architect)"
skill_origen: sigesa-prompt-contract-architect
alcance: backend-spring-boot
depende_de:
  - "PR-IMPL-021"
bloquea_a: []
---

# Prompt Contract — Implementación `PR-IMPL-022`

> **Generado vía** `@sigesa-prompt-contract-architect`.  
> **Design doc fuente:** [`DD-UC-022`](../../design/DD-UC-022.md) · **FSD:** [`FSD-UC-022`](../../product/uc/FSD-UC-022.md) · **Reglas:** `FSD-BR-07`, `FSD-BR-21`, `FSD-BR-22`, `FSD-BR-23`.

---

## 1. Propósito y Objetivo

Implementar **CRUD estructural** de fases/subfases en instancias de `AccreditationProcess` **ACTIVE** según [`DD-UC-022`](../../design/DD-UC-022.md):

- Endpoints **API-PROC-05…08** bajo `/api/v1/processes/{processId}/...`.
- Puertos **`ProcessStructurePort`** + **`SubphaseWorkflowPort`** (stub hasta MOD-EVIDENCE).
- Extender entidades/dominio/DTOs con `description` y `referenceUrl` (columnas V5).
- Extender **GET detalle UC-019** (`PhaseDto` / `SubphaseDto`) con campos nuevos.
- Solo **[JD]**; guardas `ProcessStructureGuard`.

**Dependencia:** `PR-IMPL-021` (migración V5 y clonación `referenceUrl` desde plantilla).

**Frontend:** contrato complementario **`PR-IMPL-022-FE`** post-Orval.

---

## 2. Rol y Persona

- **Identidad:** Desarrollador Backend Senior SIGESA (Hexagonal).
- **Expertise:** Java 21, Spring Boot 4.x, JPA aggregate persistence, JUnit 5, Mockito.

---

## 3. Límites de Alcance

### In-Scope

| Área | Entregables |
|---|---|
| **Dominio** | Extender `Phase`, `Subphase`; excepciones `ProcessNotEditableException`, `SubphaseHasEvidenceException`, `ProcessStructureOrderConflictException`, `SubphaseLinkRequiredException` |
| **Guard** | `ProcessStructureGuard` |
| **Puertos IN** | `AddProcessPhaseUseCase`, `UpdateProcessPhaseUseCase`, `DeleteProcessPhaseUseCase`, `AddProcessSubphaseUseCase`, `UpdateProcessSubphaseUseCase`, `DeleteProcessSubphaseUseCase`, `ReorderProcessStructureUseCase` |
| **Puertos OUT** | `ProcessStructurePort`, `SubphaseWorkflowPort` |
| **Adaptador OUT** | `ProcessStructureJpaAdapter`, `SubphaseWorkflowStubAdapter` (`return false` v1.0) |
| **Adaptador IN** | `ProcessStructureController` **o** métodos en `ProcessController` bajo rutas anidadas |
| **DTOs** | `CreatePhaseRequestDto`, `CreateSubphaseRequestDto`, `ReorderStructureRequestDto`; extender `PhaseDto`, `SubphaseDto` |
| **Advice** | Handlers 409/400 en `ProcessExceptionHandler` |
| **UC-019** | `GetProcessDetailService` / mapper devuelve `description`, `referenceUrl` |
| **Tests** | Ver §6.4 |
| **OpenAPI** | Documentar API-PROC-05…08 |

### Out-of-Scope

- UI `/procesos/{id}/estructura` (PR-IMPL-022-FE).
- CRUD indicadores; cierre de fase (UC-010).
- Implementación real de `hasBlockingEvidence` (stub hasta UC-004).
- Bitácora UC-017.
- Modificar `docs/baseline/`.

---

## 4. Restricciones y Reglas

| ID | Regla |
|---|---|
| R1 | Dominio puro; sin Spring/JPA en domain. |
| R2 | **`ProcessStructurePort` separado** de `ProcessQueryPort` y `AccreditationProcessPort`. |
| R3 | Solo **[JD]**: `@PreAuthorize("hasRole('JD')")`. |
| R4 | Mutaciones solo si `process.status == ACTIVE` → else `409 PROCESS_NOT_EDITABLE`. |
| R5 | Subfase requiere `referenceUrl` HTTPS → `400 SUBPHASE_LINK_REQUIRED`. |
| R6 | DELETE subfase si `SubphaseWorkflowPort.hasBlockingEvidence(id)` → `409 SUBPHASE_HAS_EVIDENCE` (FSD-BR-22). |
| R7 | DELETE fase solo si **todas** subfases pasan R6. |
| R8 | `order` único → `400 PROCESS_STRUCTURE_ORDER_CONFLICT`. |
| R9 | Operaciones **granulares** (no PUT replace árbol completo). |
| R10 | JaCoCo ≥ **90%** en `ProcessStructureGuard`, `DeleteSubphaseService`, `AddSubphaseService`, `ReorderProcessStructureService`. |
| R11 | Post-merge: `@save-prompt-mapping PR-IMPL-022` → `@dtp-sync`. |

---

## 5. Especificaciones de Entrada

### Documentos obligatorios

| Documento | Uso |
|---|---|
| [`DD-UC-022`](../../design/DD-UC-022.md) | Puertos, API, secuencia |
| [`FSD-UC-022`](../../product/uc/FSD-UC-022.md) | Gherkin TC-22 |
| [`DD-UC-019`](../../design/DD-UC-019.md) | Extensión DTOs lectura |
| `PhaseJpaEntity.java`, `SubphaseJpaEntity.java`, `ProcessQueryJpaAdapter.java` | Base JPA |

### Escenarios Gherkin → tests

| Escenario | Test |
|---|---|
| Agregar subfase con enlace | `AddSubphaseServiceTest` |
| Eliminar subfase con evidencia (mock true) | `DeleteSubphaseServiceTest` → 409 |
| Proceso COMPLETED no editable | `ProcessStructureGuardTest` |
| Reordenar fases | `ReorderProcessStructureServiceTest` |
| CC POST phase | `ProcessStructureControllerWebMvcTest` → 403 |

---

## 6. Especificaciones de Salida

### 6.1 Firma `ProcessStructurePort`

```java
AccreditationProcess loadActiveProcess(UUID processId);
Phase savePhase(UUID processId, Phase phase);
Subphase saveSubphase(UUID processId, UUID phaseId, Subphase subphase);
void deletePhase(UUID processId, UUID phaseId);
void deleteSubphase(UUID processId, UUID phaseId, UUID subphaseId);
void reorderPhases(UUID processId, List<UUID> phaseIdsInOrder);
void reorderSubphases(UUID processId, UUID phaseId, List<UUID> subphaseIdsInOrder);
```

### 6.2 Firma `SubphaseWorkflowPort`

```java
boolean hasBlockingEvidence(UUID subphaseId);
```

Implementación v1.0: **`SubphaseWorkflowStubAdapter`** retorna `false` siempre; documentar `@ConditionalOnMissingBean` para reemplazo en PR-IMPL-006+.

### 6.3 Endpoints REST

| Método | Path | Body | Respuesta |
|---|---|---|---|
| POST | `/processes/{processId}/phases` | `{ name, order, description? }` | 201 `PhaseDto` |
| PUT | `/processes/{processId}/phases/{phaseId}` | parcial | 200 |
| DELETE | `/processes/{processId}/phases/{phaseId}` | — | 204 / 409 |
| POST | `/processes/{processId}/phases/{phaseId}/subphases` | `{ name, order, referenceUrl, description? }` | 201 |
| PUT | `/processes/{processId}/phases/{phaseId}/subphases/{subphaseId}` | parcial | 200 |
| DELETE | `/processes/{processId}/phases/{phaseId}/subphases/{subphaseId}` | — | 204 / 409 |
| PUT | `/processes/{processId}/structure/reorder` | `{ "phases": [uuid], "subphasesByPhase": { "phaseId": [uuid] } }` | 200 árbol |

### 6.4 Definition of Done

- [ ] `./mvnw -q test` verde.
- [ ] JaCoCo ≥ 90% (R10).
- [ ] GET `/processes/{id}` incluye `referenceUrl` y `description` en subfases.
- [ ] Stub `SubphaseWorkflowPort` registrado y testeable con mock.
- [ ] PR: `FSD-UC-022` · `DD-UC-022` · `PR-IMPL-022`.

---

## 7. Anti-patrones

- ❌ Confundir con UC-010 (cierre workflow).
- ❌ DELETE físico ignorando guard BR-22 cuando port retorna true.
- ❌ Permitir [TD]/[CC] mutar estructura.
- ❌ `@EntityGraph` con `phases.subphases` en listado UC-019 (no tocar performance list).

---

## 8. Verificación manual

1. [JD] → POST subfase con URL en proceso ACTIVE → 201.
2. GET detalle → subfase visible con link.
3. PUT reorder → orders 1..N.
4. Marcar proceso COMPLETED (seed/SQL) → POST fase → 409.
5. [CC] → DELETE subfase → 403.

---

## 9. Trazabilidad

```text
PR-IMPL-021 (V5 + referenceUrl) → PR-IMPL-022 → PR-IMPL-022-FE
FSD-UC-022 → DD-UC-022 → PR-IMPL-022
```
