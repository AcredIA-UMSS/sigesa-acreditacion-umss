# Demo 3 — AcredIA / SIGESA (miércoles 4 de noviembre)

**Release:** `8.3.0`  
**Rama:** `release/8.3.0`  
**Repositorio:** [https://github.com/BorisTB/sigesa-acreditacion-umss](https://github.com/BorisTB/sigesa-acreditacion-umss)

Entrega de aula: enlace al repositorio + nombre del release (`8.3.0`). 

Registro de aportes: [`8.3.0-aportes.md`](../../8.3.0-aportes.md) (raíz). Cada commit con la identidad de su autor.

---

## Compromiso por integrante

Cada persona presenta **1 a 3** funcionalidades del release (1 grande, 2 medianas o 3 pequeñas), con **caso de uso FSD** y **criterios de aceptación**. Lo técnico de esta demo (imagen/compose y/o k6) cuenta como una funcionalidad de quien lo hace.

| Integrante | Funcionalidades (1–3) | FSD / contrato | Criterios de aceptación (resumen) | Issue / evidencia |
|------------|----------------------|----------------|-----------------------------------|-------------------|
| **Boris Anthony Angulo Urquieta** | 1. RAG ARCU-SUR 2023 en el asistente existente (sin agente nuevo) | [FSD-UC-033](../product/uc/FSD-UC-033.md) | Corpus MD indexado; `search_normative_docs` / path RAG; respuesta cita el manual 2023 (no placeholders); el cliente no transfiere el PDF | [#76](https://github.com/BorisTB/sigesa-acreditacion-umss/issues/76) |
| | 2. Contrato uniforme de errores REST | [FSD-UC-034](../product/uc/FSD-UC-034.md) | Envelope `{error, message, details?}`; 400/404/405/415/500; 500 genérico; `include-message/binding/stacktrace=never` | [#67](https://github.com/BorisTB/sigesa-acreditacion-umss/issues/67) |
| **Alexander Villca** | _por registrar_ | | | |
| **Aylen Laura** | 1. Portal público de acreditación (listado, estado y certificado PDF) | [FSD-UC-016](../product/uc/FSD-UC-016.md) | Anónimo ve solo carreras `published`; busca por nombre; 404 genérico si no publicado; PDF solo si certificado publicado | `b68fd5d1` · PM-039 · PR-IMPL-050 |
| | 2. Noticias y destacados (home + listado + admin [JD]) | [FSD-UC-035](../product/uc/FSD-UC-035.md) | [P] lee solo `publicada`; una destacada o la más reciente; [JD] CRUD/publicar/destacar; 404 genérico en borrador | `df465c15` `07c0ae8c` `0da6bdff` `fd5c5810` · PM-040 · PR-IMPL-051 |
| **Marlene Velasquez** | _por registrar_ | | | |

Análisis k6 (opción A del enunciado, lab E3 del grupo): script en `bench/e3/`, ritmo fijo, tabla p95 / % errores (mediana de 3) en [`bench/e3/INFORME.md`](../../bench/e3/INFORME.md). Lo presenta quien lo ejecutó.

---

## Boris Anthony Angulo Urquieta — detalle para la exposición

### 1. RAG ARCU-SUR (FSD-UC-033) — grande

- Pregunta de demo: *«¿Cómo se constituye el comité de pares ARCU-SUR?»*
- El chat sigue siendo `POST /api/v1/assistant/chat` (FAB / `/ayuda`).
- El PDF no viaja al navegador; el índice son 33 chunks Markdown en classpath + FTS PostgreSQL.

### 2. Errores REST (FSD-UC-034) — mediana

- Mostrar 400 (validación con `details`), 404, 405 y un 500 genérico (`INTERNAL_ERROR`) sin stack en JSON.
- Contraste: Spring Boot 4 por defecto ya no pone `message` en el body; el envelope del producto sí.

---

## Aylen Laura — detalle para la exposición

Identidad Git de los commits: **Aylen Gonzáles** `<82118099+Aylen-omg@users.noreply.github.com>`.

### 1. Portal público (FSD-UC-016) — grande

- Ruta UI: `/public/status` (banner «Estado de Acreditación» en la home).
- Anónimo: `GET /api/v1/public/programs` (nombre + slug), `GET /api/v1/public/programs/{slug}` (estado vigente), `GET /api/v1/public/programs/{slug}/certificate` (PDF en memoria).
- FSD-BR-10: borrador u observaciones internas → **404 genérico** (`NOT_FOUND` / «El recurso solicitado no existe.»). No revela si la carrera existe.
- Slug = `programs.code` en minúsculas (`INF-SIS` → `inf-sis`). Filtro por nombre en cliente.
- Gherkin: `@PRD-US-016 @FSD-UC-016 @TC-PUB` y `@PRD-US-020 @TC-PUB2`.

### 2. Noticias y destacados (FSD-UC-035) — grande

- [P]: home `#noticias`, `/public/news`, `/public/news/{slug}`. El botón **Noticias** del header abre el listado y conserva header/footer.
- [JD]: `/admin/noticias` — crear borrador, editar, publicar/despublicar, destacar. Seed: `jd@umss.edu.bo` / `JefeDemo2026!`.
- `GET /api/v1/public/news`, `/featured` (destacada o más reciente; 204 si no hay), `/{slug}`. Admin `/api/v1/admin/news` (JWT, rol JD).
- Una sola destacada; despublicar fuerza `featured=false`; destacar borrador → 409 `NEWS_NOT_PUBLISHED`.
- Home institucional: logos DUEA/UMSS y fotos de campus en hero y noticias.
- Gherkin: `@PRD-US-027|028|029 @FSD-UC-035 @TC-NEWS` (6 escenarios).

Commits y criterios escritos: [`8.3.0-aportes.md`](../../8.3.0-aportes.md).

---

## Cómo levantar la demo

```bash
git clone https://github.com/BorisTB/sigesa-acreditacion-umss.git
cd sigesa-acreditacion-umss
git checkout release/8.3.0   # o git fetch --tags && git checkout 8.3.0
cp .env.example .env         # JWT, Postgres, MinIO, API keys
docker compose up -d --wait
```

- Frontend: http://localhost:3000  
- Backend: http://localhost:8080 (`/api/health`, `/v3/api-docs`)  
- MinIO consola: http://localhost:9001  

Usuarios seed: `admin` / `admin123`, `ceub` / `ceub123`, `docente` / `docente123`.

---

## Fuera de esta demo (ya en 8.2.0)

Contrato OpenAPI + CI (`openapi.yaml`, Spectral, Dredd, Orval) — Demo 2. No recontar como aporte nuevo salvo delta posterior.
