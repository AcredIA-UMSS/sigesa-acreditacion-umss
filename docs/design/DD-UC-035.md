---
id: DD-UC-035
titulo: Noticias y destacados
producto: SIGESA
fsd_uc:
  - "FSD-UC-035"
prompts:
  - "PR-IMPL-051"
release: "1.1.0"
status: implementado
fecha: "04/11/2026"
autores:
  - "Cursor Agent"
---

# Design Doc `DD-UC-035` — Noticias y destacados

## 1. Objetivo

[JD] gestiona noticias (`borrador` / `publicada`, a lo sumo una destacada). [P] lee solo `publicada` en home `#noticias`, `/public/news` y `/public/news/{slug}`. Mismo criterio que **FSD-BR-10**: borrador o slug desconocido → 404 genérico.

## 2. Contratos

Prefijo de plataforma `/api/v1/`. Lectura anónima `GET /api/v1/public/**` (ya `permitAll` por UC-016). Escritura ` /api/v1/admin/news/**` con `hasRole("JD")`.

| ID | HTTP | Notas |
|----|------|-------|
| API-NEWS-01 | `GET /api/v1/public/news` | Solo `publicada`. Pageable. `publishedAt` desc. |
| API-NEWS-02 | `GET /api/v1/public/news/featured` | Destacada o, si no hay, la más reciente. **204** si cero publicadas. Path estático antes de `{slug}`. |
| API-NEWS-03 | `GET /api/v1/public/news/{slug}` | Detalle si `publicada`. Si no: 404 genérico. |
| API-NEWS-04…11 | `/api/v1/admin/news` | CRUD + publish / unpublish / feature. |

404 público: `{ "error": "NOT_FOUND", "message": "El recurso solicitado no existe." }`.

Destacar borrador: **409** `{ "error": "NEWS_NOT_PUBLISHED", "message": "…" }`.
Eliminar publicada: **409** (despublicar antes).
Publicar sin `body`: **400** `VALIDATION_ERROR`.

## 3. Modelo

Tabla `news_article` (Flyway V28). Slug kebab único derivado del título. `featured` a lo sumo una `publicada`. Despublicar fuerza `featured=false`. Cuerpo: texto plano o Markdown sanitizado (sin HTML crudo).

UI: `/admin/noticias`, `/public/news`, `/public/news/{slug}`. Home `#noticias` consume featured + listado excluyendo ese slug. Orval only. Sin `react-markdown`.

## 4. Fuera de alcance

Imágenes, tags libres, programación de publicación, portal de carreras (UC-016), `docs/baseline/`.
