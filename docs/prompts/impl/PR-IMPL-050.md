---
id: PR-IMPL-050
feature_asociado: DD-UC-016
fsd_uc:
  - FSD-UC-016
fecha: "2026-11-04"
version: "1.0"
estado: Implementado
autor: "Cursor Agent"
release: "1.1.0"
---

# Prompt Contract — Implementación `PR-IMPL-050`

> **Design:** [`DD-UC-016`](../../design/DD-UC-016.md)  
> **FSD:** [`FSD-UC-016`](../../product/uc/FSD-UC-016.md)

## 1. Propósito

Portal público FSD-UC-016: listado/estado/certificado bajo `/api/v1/public`, 404 genérico, PDF sin URL de archivo, UI `/public/status`.

## 2. In-Scope

- API-PUB-01/02/03, snapshot JPA, seed, `GET /api/v1/public/**` `permitAll`.
- Tres tests Gherkin `@PRD-US-016 @FSD-UC-016 @TC-PUB` y `@PRD-US-020 @TC-PUB2`.
- Frontend Orval `/public/status`.
- Docs vivos (no baseline).

## 3. Out-of-Scope

- Noticias / UC-035, UC-015, UC-002, search server-side, `docs/baseline/`.

## 4. Cierre

`@save-prompt-mapping sprint=03 fsd=FSD-UC-016`
