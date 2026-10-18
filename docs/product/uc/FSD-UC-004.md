---
id: FSD-UC-004
nombre: Cargar Evidencia en Indicador
estado: Hecho (M7 DocumentAsset)
release: v2.1
actor_principal: "[CC]"
trazabilidad_prd: PRD-US-005, PRD-US-025
modulo: MOD-EVIDENCE
reglas: FSD-BR-01, FSD-BR-03, FSD-BR-18
design_doc: DD-UC-004-M7
pr_impl: PR-IMPL-M7-004
ultima_actualizacion: "2026-10-17"
nota_implementacion: "M7: blob físico en DocumentAsset; Evidence 1:1 por indicador. FEAT-02 (#51): blobs S3-compatible (Compose: Silo `pgsty/silo`, API MinIO) o local (H2); lock evidence_upload_lock."
---

# FSD-UC-004 — Cargar Evidencia en Indicador

## Contexto

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-005, 022 · PRD-US-005, 025 |
| **Design Doc** | [`DD-UC-004-M7`](../../design/DD-UC-004-M7.md) |
| **Prompt impl** | [`PR-IMPL-M7-004`](../../prompts/impl/PR-IMPL-M7-004.md) · [`PR-IMPL-041`](../../prompts/impl/PR-IMPL-041.md) (#51) |
| **ADR** | [ADR-0005](../../adr/ADR-0005-workflow-metodologico-evaluacion-transversal.md) § entidades DocumentAsset |
| **Precondiciones** | Indicador en proceso ACTIVE; [CC] con alcance sobre la carrera; sin observación OPEN; el indicador **no** tiene evidencia vigente |

## Flujo principal (carga)

1. [CC] abre detalle del proceso y **Subir evidencia** en el indicador.
2. Adjunta archivo y/o enlace externo y descripción; `POST /api/v1/indicators/{indicatorId}/evidences`.
3. Sistema valida tipo/tamaño; calcula SHA-256 (archivos).
4. Si el hash ya existe en `DocumentAsset`, **reutiliza** el blob (no duplica almacenamiento) y crea `Evidence` + `IndicatorEvidenceLink` para este indicador.
5. Si el hash es nuevo, persiste `DocumentAsset` (`folioCode`, `storageKey`, `contentHash`) y luego `Evidence` v1 + link primario.
6. **Indicador** transiciona a `SUBIDO`.
7. Notification Service notifica al [TD] (UC-015 — stub vigente).

## Flujo alterno — redireccionamiento (M:N)

1. [CC] elige **Vincular documento existente** (mismo proceso, documento ya cargado en otro indicador).
2. `POST /api/v1/indicators/{indicatorId}/evidences/link` con `documentAssetId`.
3. Sistema crea `Evidence` + `IndicatorEvidenceLink` **sin** nuevo blob.
4. Indicador destino → `SUBIDO` si estaba `PENDIENTE`.

## Excepciones y flujos alternos

| Condición | Respuesta |
|-----------|-----------|
| Sin `indicatorId` o metadatos | `400 EVIDENCE_UNCLASSIFIED` |
| Observación OPEN pendiente | `409 SUBSANATION_NOT_ALLOWED` |
| Formato inválido | `422 INVALID_EVIDENCE_FORMAT` |
| Indicador inexistente o fuera de alcance [CC] | `404 INDICATOR_NOT_FOUND` |
| Indicador ya tiene evidencia | `409 INDICATOR_ALREADY_HAS_EVIDENCE` |
| `documentAssetId` de otro proceso | `403 PROGRAM_SCOPE_DENIED` |

## Postcondiciones

`evidenceId`, `version=1`, `documentAssetId`, `folioCode`, `contentHash`, `reusedAsset`, evento `EvidenceUploaded`; indicador en `SUBIDO`.

## Datos

| Entrada | Salida |
|---------|--------|
| `indicatorId`, `file` (opc.), `externalUrl` (opc.), `description` | `evidenceId`, `version`, `documentAssetId`, `folioCode`, `contentHash`, `reusedAsset` |
| Link: `indicatorId`, `documentAssetId` | igual, `reusedAsset: true` |

**API v2.1 (M7):** `POST /indicators/{id}/evidences` (multipart, reutiliza por hash) · `POST /indicators/{id}/evidences/link` · `GET /document-assets/{id}` · `GET /document-assets/{id}/content` (API-EVD-07b, blob MinIO/S3).

**API v2.0 (sigue válido):** `POST/GET /indicators/{indicatorId}/evidences`.

## Escenarios Gherkin

```gherkin
# language: es
@PRD-US-005 @FSD-UC-004 @FSD-BR-01 @TC-04
Característica: Carga de Evidencia en indicador

  Escenario: Carga exitosa crea DocumentAsset y folio
    Dado un [CC] autenticado y un indicador válido en su carrera
    Cuando carga una Evidencia con descripción y archivo
    Entonces el sistema crea DocumentAsset con folioCode
    Y Evidence versión 1 vinculada al indicador
    Y el indicador pasa a SUBIDO

  Escenario: Mismo hash no duplica blob
    Dado un DocumentAsset existente con contentHash H
    Cuando [CC] carga el mismo archivo en otro indicador del mismo proceso
    Entonces reusedAsset es verdadero
    Y no se crea un segundo objeto en almacenamiento
    Y ambos indicadores quedan SUBIDO

  Escenario: Vincular documento existente
    Dado un DocumentAsset del proceso P
    Cuando [CC] vincula ese asset a un indicador sin evidencia
    Entonces se crea IndicatorEvidenceLink
    Y no se almacena un blob nuevo
```
