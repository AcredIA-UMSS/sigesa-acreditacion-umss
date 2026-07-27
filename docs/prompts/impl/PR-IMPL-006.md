---
id: PR-IMPL-006
feature_asociado: DD-UC-004
fsd_uc:
  - FSD-UC-004
fecha: "2026-06-26"
version: "1.0"
estado: Aprobado
autor: "AI Prompt Architect (@sigesa-prompt-contract-architect)"
---

# Prompt Contract — Implementación `PR-IMPL-006`

> **Design doc:** [`DD-UC-004`](../../design/DD-UC-004.md) · **FSD:** FSD-UC-004 · **Reglas:** FSD-BR-01, BR-03, BR-09, BR-18.

## 1. Objetivo

Implementar **MOD-EVIDENCE** — carga Evidencia v1 vía `POST /api/v1/indicators/{indicatorId}/evidences` (multipart), hexagonal estricta.

## 2. In-Scope

- Dominio, puertos, `UploadEvidenceService`
- JPA: `indicator`, `indicator_state_history`, `evidence`, `evidence_version`
- Storage local, SHA-256, upload lock, outbox stub
- `EvidenceController`, `EvidenceModuleConfig`, SecurityConfig CC
- Seed dev: CC + indicador PENDIENTE
- Tests unitarios + frontend upload con progress bar (>5MB UX)

## 3. Out-of-Scope

- UC-006 subsanación (v2+)
- UC-015 worker SMTP real
- S3 (v1.1)

## 4. Restricciones

- R1: Dominio sin Spring/JPA
- R2: DTOs record; nunca `@Entity` en controller
- R3: No `UPDATE` destructivo de estado Indicador
- R4: MIME whitelist; max 50MB
- R5: JaCoCo ≥90% en `UploadEvidenceService`
