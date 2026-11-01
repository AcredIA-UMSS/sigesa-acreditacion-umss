---
id: PR-IMPL-M7-004
feature_asociado: DD-UC-004-M7
fsd_uc:
  - "FSD-UC-004"
  - "FSD-UC-005"
fecha: "2026-10-13"
version: "1.0"
estado: Listo
autor: "Cursor Agent (M7 DocumentAsset)"
skill_origen: sigesa-orchestrator
alcance: full-stack
depende_de:
  - "PR-IMPL-M6-025"
bloquea_a:
  - "M8 FODA"
---

# Prompt Contract — Implementación `PR-IMPL-M7-004`

> **Design doc:** [`DD-UC-004-M7`](../../design/DD-UC-004-M7.md)  
> **FSD:** [UC-004](../../product/uc/FSD-UC-004.md), [UC-005](../../product/uc/FSD-UC-005.md)  
> **ADR:** [ADR-0005](../../adr/ADR-0005-workflow-metodologico-evaluacion-transversal.md)  
> **Milestone:** M7 (release 2.1.0)

---

## 1. Propósito y Objetivo

Implementar **DocumentAsset** y redireccionamiento **M:N** indicador↔documento:

- Flyway **V19** (`document_assets`, `indicator_evidence_links`) + backfill desde evidencias normativas existentes.
- Deduplicación por `(process_id, content_hash)` en carga.
- API link + GET asset; extender 201 de upload con `documentAssetId`, `folioCode`, `reusedAsset`.
- UI: vincular documento existente + mostrar folio.
- Tests ≥90 % en servicios nuevos; no romper suite WebMvc/evidencia.

**No** usar versión Flyway V18 (ya existe `V18__normative_evidence_legacy_nullable.sql`).

## 2. Rol

Eres el implementador hexagonal SIGESA (Java 21 / Spring Boot 4 / React 19). Dominio puro; DTOs en web; Orval only en frontend; tokens Tailwind institucionales.

## 3. Límites de Alcance

### In-Scope

| Área | Entregables |
|------|-------------|
| Dominio | `DocumentAsset`, `IndicatorEvidenceLink`, excepciones 409/404 asset |
| Puertos | `DocumentAssetPort`; `LinkDocumentAssetToIndicatorUseCase`; `GetDocumentAssetUseCase`; resultado upload extendido |
| Servicios | Extender `UploadNormativeIndicatorEvidenceService`; servicios link/get |
| Persistencia | Entidades JPA, adapters, V19 + backfill |
| REST / OpenAPI | EVD-01 extendido, EVD-06 link, EVD-07 GET asset |
| Frontend | Modal carga + selector de assets del proceso; folio en lista |
| Docs | `api_contracts.md`, `DTP.md` changelog M7 |

### Out-of-Scope

- M8 FODA / CAE, M9 informes, M10 freeze/[EE], M11 post-acreditación.
- SMTP UC-015, bitácora real UC-017 (seguir NoOp).
- Múltiples documentos por un mismo indicador.
- MinIO nuevo: reutilizar `EvidenceBlobStoragePort`.

## 4. Restricciones

| ID | Regla |
|----|-------|
| R1 | Dominio sin Spring/JPA. |
| R2 | Un `Evidence` por indicador normativo (unique vigente). |
| R3 | Hash coincidente en el **mismo proceso** → no segundo blob. |
| R4 | Asset de otro proceso → `403 PROGRAM_SCOPE_DENIED`. |
| R5 | Indicador con evidencia → `409 INDICATOR_ALREADY_HAS_EVIDENCE`. |
| R6 | DELETE evidencia aprobada → `409 EVIDENCE_IMMUTABLE` (sin borrar asset compartido). |
| R7 | Frontend solo Orval; sin hex arbitrarios. |
| R8 | Tras código: `@dtp-sync` + `@save-prompt-mapping fsd=FSD-UC-004`. |

## 5. Contratos de salida (API)

**POST** `/api/v1/indicators/{indicatorId}/evidences` (existente, multipart) **201:**

```json
{
  "evidenceId": "uuid",
  "version": 1,
  "contentHash": "hex",
  "documentAssetId": "uuid",
  "folioCode": "SIGESA-20261013-abcd1234",
  "reusedAsset": false,
  "event": "EvidenceUploaded",
  "currentState": "SUBIDO"
}
```

**POST** `/api/v1/indicators/{indicatorId}/evidences/link` JSON `{ "documentAssetId": "uuid" }` → mismo shape, `reusedAsset: true`.

**GET** `/api/v1/document-assets/{documentAssetId}` **200:** `{ id, folioCode, title, contentHash, processId, linkedIndicatorIds[], createdAt }`.

## 6. Validación

- [ ] `cd backend && ./mvnw test` verde (incl. servicios DocumentAsset / WebMvc evidencia).
- [ ] Orval regenerado (`pnpm --dir frontend generate:api`).
- [ ] `pnpm --dir frontend test:run` verde.
- [ ] Docker: Flyway V19 en `sigesa-postgres` (Paso 3c si Compose está arriba).

## 7. Trazabilidad documental

| Artefacto | Acción |
|-----------|--------|
| `docs/product/FSD.md` | Ya actualizado (En Curso M7) |
| `docs/product/api_contracts.md` | API-EVD-01 ext + EVD-06/07 |
| `docs/product/DTP.md` | V19 + changelog M7 |
| `docs/sprints/sprint_03/PROMPT_MAPPING.md` | PM-015 vía `@save-prompt-mapping` |
