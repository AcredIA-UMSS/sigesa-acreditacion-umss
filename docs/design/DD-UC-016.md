---
id: DD-UC-016
titulo: Portal público de transparencia
producto: SIGESA
fsd_uc:
  - "FSD-UC-016"
prompts:
  - "PR-IMPL-050"
release: "1.1.0"
status: implementado
fecha: "04/11/2026"
autores:
  - "Cursor Agent"
---

# Design Doc `DD-UC-016` — Portal público

## 1. Objetivo

Exponer a `[P]` (anónimo) el estado de acreditación **solo** si `[JD]` lo publicó (`FSD-BR-10`). Sin borradores, sin observaciones internas, sin URLs de archivo.

## 2. Contratos

Prefijo vivo **`/api/v1/public`**. El FSD original decía `GET /public/programs/{slug}` (sin `/api/v1`).

| ID | HTTP | Notas |
|----|------|-------|
| API-PUB-02 | `GET /api/v1/public/programs` | Adición 2026-11-04: `{slug,name}` de publicadas. Filtro por nombre en cliente. |
| API-PUB-01 | `GET /api/v1/public/programs/{slug}` | 200 si `published=true`; si no, 404 genérico |
| API-PUB-03 | `GET /api/v1/public/programs/{slug}/certificate` | PDF en memoria si `certificatePublished=true`; 404 genérico |

404 único: `{ "error": "NOT_FOUND", "message": "El recurso solicitado no existe." }`.

Slug = `programs.code` en minúsculas (`INF-SIS` → `inf-sis`).

## 3. Modelo

Tabla `publication_snapshot` (Flyway V27 + **V29**): snapshot denormalizado, **sin** columnas de observaciones. Flags `published` y `certificate_published`. `agency` y `resolution_number` nullable (certificado). Sin `issued_at`. PDF OpenPDF generado al vuelo; no se persiste blob ni se expone `storageKey`. QR a `{sigesa.public.base-url}/public/status?slug=`.

UI: `/public/status` (lee `?slug=`). Orval only.

## 4. Fuera de alcance

Noticias (propuesto UC-035), UI de publicación [JD], rate-limit de diagramas, `docs/baseline/`.
