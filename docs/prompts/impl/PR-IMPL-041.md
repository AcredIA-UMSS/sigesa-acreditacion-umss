---
id: PR-IMPL-041
feature_asociado: DD-UC-004-M7
fsd_uc:
  - FSD-UC-004
  - FSD-UC-005
fecha: "2026-10-17"
version: "1.0"
estado: Aprobado
autor: "Boris Anthony Angulo Urquieta"
github_issue: "https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss/issues/51"
release: "8.1.0"
---

# Prompt Contract — Implementación `PR-IMPL-041`

> **Issue:** [#51 FEAT-02](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss/issues/51) — Object storage S3/MinIO + lock de upload en BD.  
> **Design doc:** [`DD-UC-004-M7`](../../design/DD-UC-004-M7.md)  
> **FSD:** [`FSD-UC-004`](../../product/uc/FSD-UC-004.md) · [`FSD-UC-005`](../../product/uc/FSD-UC-005.md)  
> **Auditoría:** F12-03 en `docs/auditoria/12factor-boris.md` (veredicto histórico `FALLA`; remediación 8.1.0).  
> **Rama:** `feat/51-s3-evidence-lock`

---

## 1. Propósito

- Sustituir el disco del contenedor (`LocalFileEvidenceBlobStorageAdapter` en Docker) por un adaptador S3-compatible (MinIO local; endpoint remoto vía env).
- Sustituir `ConcurrentHashMap` por `INSERT` atómico en `evidence_upload_lock` (PK `indicator_id`).

---

## 2. In-Scope

- `S3EvidenceBlobStorageAdapter` + `S3EvidenceStorageConfig` (`sigesa.evidence.storage=s3`).
- Object storage en `docker-compose.yml` (servicio `minio`, imagen Silo, volumen `minio_data`).
- `JdbcEvidenceUploadLockAdapter` + Flyway `V23` + entity Hibernate (dev `ddl-auto`).
- Keys opacas `evidences/{id}/v{n}/{file}` (nunca path absoluto).
- Local filesystem **solo** si `SIGESA_EVIDENCE_STORAGE=local` (H2/tests).
- `S3ReportArtifactStorageAdapter` (PDF ejecutivo) y persistencia de export dashboard vía `EvidenceBlobStoragePort` (no path absoluto).
- `GET /api/v1/document-assets/{id}/content` (API-EVD-07b).
- Imagen Compose `docker.io/pgsty/silo:RELEASE.2026-10-15T00-00-00Z` (API S3/MinIO; Hub y Quay de MinIO ya no son públicos).

## 3. Out-of-Scope

- FEAT-01 (#50) TX de reportes.
- FEAT-03 (#52) LLM.
- `docs/baseline/`.

## 4. Config (cambio futuro a S3 remoto)

| Variable | Demo Compose | Remoto futuro |
|----------|--------------|---------------|
| `SIGESA_EVIDENCE_STORAGE` | `s3` | `s3` |
| `SIGESA_S3_ENDPOINT` | `http://minio:9000` | `https://s3.ejemplo.edu` |
| `SIGESA_S3_BUCKET` | `sigesa-evidences` | mismo o prod |
| `SIGESA_S3_ACCESS_KEY` / `SECRET_KEY` | demo | secretos de plataforma |
| `SIGESA_S3_PATH_STYLE` | `true` (MinIO) | `false` en AWS puro si aplica |

## 5. Criterio de aceptación

- Recrear el contenedor `backend` no pierde blobs; `GET .../content` los sirve desde Silo/S3.
- PDF ejecutivo y export dashboard usan object storage (keys `reports/` y `evidences/`), no disco del contenedor.
- Uploads concurrentes al mismo indicador: un `tryAcquire` gana; el otro 409 `UploadInProgress` — sin `ConcurrentHashMap`.
- \(N>1\) réplicas API: mismo bucket + misma tabla de lock.

## 6. Trazabilidad

`FSD-UC-004` → `DD-UC-004-M7` → `PR-IMPL-041` → GitHub #51 → sprint 03 `PM-020`
