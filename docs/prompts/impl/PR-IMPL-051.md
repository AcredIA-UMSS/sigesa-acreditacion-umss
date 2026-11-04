---
id: PR-IMPL-051
feature_asociado: DD-UC-035
fsd_uc:
  - FSD-UC-035
fecha: "2026-11-04"
version: "1.0"
estado: Implementado
autor: "Cursor Agent"
release: "1.1.0"
---

# Prompt Contract — Implementación `PR-IMPL-051`

> **Design:** [`DD-UC-035`](../../design/DD-UC-035.md)  
> **FSD:** [`FSD-UC-035`](../../product/uc/FSD-UC-035.md)

## 1. Propósito

Noticias y destacados FSD-UC-035: CRUD [JD], lectura anónima, home `#noticias` real, 404 genérico, una destacada.

## 2. In-Scope

- API-NEWS-01…11, tabla `news_article` V28, seed `!prod`, matcher JD en `/api/v1/admin/news/**`.
- Seis tests Gherkin `@PRD-US-027|028|029 @FSD-UC-035 @TC-NEWS`.
- Frontend Orval: `/admin/noticias`, `/public/news`, `/public/news/{slug}`, home.
- Docs vivos (no baseline).

## 3. Out-of-Scope

- Imágenes / tags / schedule, UC-016 / UC-015 / UC-002, `docs/baseline/`, `react-markdown`.

## 4. Cierre

`@save-prompt-mapping sprint=03 fsd=FSD-UC-035`
