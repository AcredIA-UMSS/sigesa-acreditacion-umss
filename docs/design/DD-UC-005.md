---
id: DD-UC-005
fsd_ref: FSD-UC-005
titulo: "Diseño: Versionado y bloqueo de borrado (MOD-EVIDENCE)"
modulo: MOD-EVIDENCE
arquitectura: Hexagonal
tecnologia: Java 21, Spring Boot 4.x
estado: Aprobado
autor: AI Architect (@sigesa-orchestrator)
fecha: "2026-08-27"
fsd_uc:
  - FSD-UC-005
prd_refs:
  - PRD-US-007
  - PRD-US-008
prompts:
  - PR-IMPL-035
release: v1.0
---

# DD-UC-005: Versionado y bloqueo de borrado

## 1. Propósito

Consultar el **historial append-only** de versiones de una evidencia y **rechazar** cualquier intento de `DELETE` físico, registrando `AUDIT_DELETE_DENIED` (hook UC-017).

## 2. API

| Método | Ruta | Roles | Respuesta |
|--------|------|-------|-----------|
| GET | `/api/v1/evidences/{evidenceId}/versions` | CC, TD, JD | **200** lista ordenada DESC |
| DELETE | `/api/v1/evidences/{evidenceId}` | cualquier autenticado | **409** `EVIDENCE_IMMUTABLE` |

**200 item:** `{ versionId, version, supersedesVersion?, observationId?, description, contentHash, createdAt, createdBy, current }`

## 3. Reglas

| ID | Regla |
|----|-------|
| FSD-BR-02 | Sin DELETE físico; historial intacto |
| FSD-BR-15 | Versión vigente = `latestVersionId` en cabecera `evidence` |
| FSD-BR-09 | [CC] solo evidencias de su carrera (vía indicador o subfase) |

## 4. Capas

- **IN:** `ListEvidenceVersionsUseCase`, `AttemptDeleteEvidenceUseCase`
- **OUT:** `EvidenceLifecycleQueryPort` (contexto + versiones)
- **Adapter IN:** `EvidenceLifecycleController`
- **Adapter OUT:** `EvidenceLifecycleJpaAdapter`

## 5. Excepciones

| Excepción | HTTP | Código |
|-----------|------|--------|
| `EvidenceNotFoundException` | 404 | `EVIDENCE_NOT_FOUND` |
| `EvidenceImmutableException` | 409 | `EVIDENCE_IMMUTABLE` |
| `ProgramScopeDeniedException` | 403 | `PROGRAM_SCOPE_DENIED` |
