---
id: FSD-UC-035
nombre: Noticias y destacados
estado: Implementado
release: v1.1
actor_principal: "[JD] (escribe) · [P] (lee)"
trazabilidad_prd: PRD-US-027, PRD-US-028, PRD-US-029
modulo: MOD-PUBLIC
reglas: FSD-BR-10
design_doc: DD-UC-035
pr_impl: PR-IMPL-051
ultima_actualizacion: "2026-11-04"
---

# FSD-UC-035 — Noticias y destacados

## Contexto

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-030 · PRD-US-027, 028, 029 · FSD-BR-10 |
| **Pantallas [P]** | Home `#noticias` · `/public/news` · `/public/news/{slug}` |
| **Pantalla [JD]** | `/admin/noticias` |
| **API** | Prefijo de plataforma `/api/v1/`. Lectura anónima bajo `/api/v1/public/news`. Escritura solo [JD] bajo `/api/v1/admin/news`. |
| **Invariante** | Mismo criterio que **FSD-BR-10**: el visitante anónimo solo ve noticias `publicada`. Borrador o slug desconocido → **404 genérico** (no revela existencia). |
| **Hoy en código** | `HomeNewsSection` (`#noticias`) usa `HOME_NEWS` hardcodeado; el enlace «Ver mas Noticias» apunta a `/login` (TODO). Este UC sustituye ese stub. |

**No es** el portal de estado de carreras (`FSD-UC-016`). No comparte entidades ni endpoints de programas.

## Modelo

| Campo | Tipo | Notas |
|-------|------|-------|
| `title` | texto | Obligatorio |
| `slug` | texto único | Derivado del título (kebab minúsculas). Identificador público. |
| `category` | enum | Catálogo inicial: `EVENTO`, `CAPACITACION`, `LOGRO` (la home actual usa esas tres). |
| `summary` | texto | Obligatorio. Tarjeta de listado y home. |
| `body` | texto | Obligatorio para publicar. Texto plano o Markdown **sanitizado**; **sin HTML crudo**. Detalle `/public/news/{slug}`. |
| `status` | enum | `borrador` \| `publicada` |
| `publishedAt` | fecha/hora | Se setea al publicar; `null` en borrador. |
| `featured` | bool | A lo sumo **una** noticia `publicada` con `featured=true`. Al marcar otra, se desmarca la anterior. Si ninguna está destacada, la home usa la publicada más reciente (`publishedAt` desc). |

Sin columnas de observaciones internas. Sin adjuntos.

## Contratos API

404 genérico (público): `{ "error": "NOT_FOUND", "message": "El recurso solicitado no existe." }` — igual que UC-016.

### Lectura anónima `[P]`

| ID | HTTP | Contrato |
|----|------|----------|
| API-NEWS-01 | `GET /api/v1/public/news` | Solo `publicada`. Paginado (`page`, `size`). Orden: `publishedAt` desc. Campos de listado: `slug`, `title`, `category`, `summary`, `publishedAt`, `featured`. En `#noticias`, el recorte **excluye** el `slug` de la tarjeta principal (`API-NEWS-02`). |
| API-NEWS-02 | `GET /api/v1/public/news/featured` | Tarjeta de `#noticias`. Si hay `featured=true` publicada, esa. Si no, la publicada más reciente. **204** solo si no hay ninguna publicada. Path estático **antes** de `{slug}`. |
| API-NEWS-03 | `GET /api/v1/public/news/{slug}` | Detalle si `publicada`. Si no existe o es borrador: **404 genérico**. |

`permitAll` en `GET /api/v1/public/**` (ya abierto por UC-016).

### Administración `[JD]` (JWT, rol JD)

| ID | HTTP | Contrato |
|----|------|----------|
| API-NEWS-04 | `GET /api/v1/admin/news` | Listado de **todas** (borrador y publicada), paginado. |
| API-NEWS-05 | `GET /api/v1/admin/news/{id}` | Detalle admin por UUID. |
| API-NEWS-06 | `POST /api/v1/admin/news` | Crea en `borrador`. `featured` inicial `false`. |
| API-NEWS-07 | `PUT /api/v1/admin/news/{id}` | Edita título, categoría, resumen, cuerpo. No cambia `status` (usar publicar/despublicar). |
| API-NEWS-08 | `DELETE /api/v1/admin/news/{id}` | Elimina **solo** si está en `borrador`. Publicada → `409` (despublicar antes). |
| API-NEWS-09 | `POST /api/v1/admin/news/{id}/publish` | Pasa a `publicada` y setea `publishedAt`. |
| API-NEWS-10 | `POST /api/v1/admin/news/{id}/unpublish` | Vuelve a `borrador`. Si era destacada, **`featured=false`**. Desaparece de home y listado público. |
| API-NEWS-11 | `POST /api/v1/admin/news/{id}/feature` | Marca destacada. Exige `publicada`. **Desmarca** la destacada anterior. Borrador → **409** envelope UC-034 `{error,message}`. |

Sin rol [JD]: **403** `ACCESS_DENIED`. Sin token: **401**.

## Flujo principal

1. [JD] autenticado abre `/admin/noticias` y crea una noticia en `borrador` (`API-NEWS-06`).
2. Edita contenido (`API-NEWS-07`) y publica (`API-NEWS-09`).
3. Opcional: marca destacada (`API-NEWS-11`); la destacada previa deja de serlo.
4. [P] abre `/` → `#noticias` consume `API-NEWS-02` (destacada o, si no hay, la publicada más reciente) y un recorte de `API-NEWS-01` **sin** esa noticia (no se duplica en el listado lateral).
5. [P] entra a `/public/news` (listado paginado) y a `/public/news/{slug}` (cuerpo).

## Excepciones y flujos alternos

| Condición | Comportamiento |
|-----------|----------------|
| Slug inexistente o `borrador` (público) | `404` genérico |
| [CC]/[TD]/[EE]/anónimo muta admin | `401` o `403` |
| Destacar un borrador | **409** envelope UC-034 `{ "error": "NEWS_NOT_PUBLISHED", "message": "…" }` |
| Despublicar una destacada | `status=borrador` y **`featured=false`** |
| Destacar otra publicada | OK; la anterior queda `featured=false` (una sola destacada) |
| No hay destacada y sí hay publicadas | `GET .../featured` → 200 con la más reciente; la home la muestra como tarjeta principal |
| Eliminar publicada | `409`; debe despublicarse primero |
| Publicar sin `body` | `400` `VALIDATION_ERROR` |

## Postcondiciones

Home y listado público coherentes con lo autorizado por [JD]. A lo sumo una destacada; al marcar una nueva se desmarca la anterior. Sin destacada, la home muestra la publicada más reciente. Ningún borrador enumerable ni adivinable por [P].

## Fuera de alcance

- Imágenes / portada (la home actual usa fotos CSS decorativas; no hay upload).
- Etiquetas / tags libres (solo el enum `category`).
- Programación de publicación (`publishedAt` futuro, jobs).
- Portal de carreras y certificados (`FSD-UC-016`).
- `docs/baseline/`.

## Diagramas

- [Noticias y destacados](../diagramas/AYL-SEQ-012-noticias-destacados.mmd)

## Escenarios Gherkin

```gherkin
# language: es
@PRD-US-027 @FSD-UC-035 @TC-NEWS
Característica: Noticias y destacados

  Escenario: Borrador no visible para visitante anónimo
    Dado una noticia en estado borrador creada por [JD]
    Cuando un visitante anónimo consulta el listado público, la home o el detalle por slug
    Entonces no ve esa noticia
    Y el detalle responde 404 genérico
    Y el cuerpo no revela si el slug existe

@PRD-US-028 @FSD-UC-035 @TC-NEWS
  Escenario: [JD] publica una noticia y aparece en la home y en el listado
    Dado un [JD] autenticado y una noticia en borrador con título, categoría, resumen y cuerpo
    Cuando la publica
    Entonces un visitante anónimo la ve en GET /api/v1/public/news
    Y aparece en la sección #noticias o en /public/news

@PRD-US-028 @FSD-UC-035 @TC-NEWS
  Escenario: [JD] despublica y deja de aparecer
    Dado una noticia publicada visible en el listado público
    Cuando el [JD] la despublica
    Entonces deja de aparecer en la home y en /public/news
    Y GET /api/v1/public/news/{slug} responde 404 genérico
    Y si era destacada queda featured=false

@PRD-US-029 @FSD-UC-035 @TC-NEWS
  Escenario: Solo una destacada a la vez
    Dado una noticia publicada ya marcada como destacada
    Cuando el [JD] marca otra noticia publicada como destacada
    Entonces GET /api/v1/public/news/featured devuelve solo la nueva
    Y la anterior queda featured=false

@PRD-US-028 @FSD-UC-035 @TC-NEWS
  Escenario: Un usuario sin rol [JD] no puede crear ni editar
    Dado un usuario autenticado con rol distinto de [JD] o un anónimo
    Cuando intenta crear o editar una noticia en /api/v1/admin/news
    Entonces el sistema rechaza la operación (401 o 403)
    Y no se persiste ningún cambio

@PRD-US-027 @FSD-UC-035 @TC-NEWS
  Escenario: Sin destacada, la home muestra la más reciente
    Dado dos o más noticias publicadas y ninguna marcada como destacada
    Cuando un visitante anónimo abre la home
    Entonces GET /api/v1/public/news/featured devuelve la de publishedAt más reciente
    Y esa noticia es la tarjeta principal
    Y no se repite en el recorte lateral de API-NEWS-01
```
