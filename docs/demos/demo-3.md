# Demo 3 — AcredIA / SIGESA (miércoles 4 de noviembre)

**Release:** `8.3.0`  
**Rama:** `release/8.3.0`  
**Repositorio:** [https://github.com/BorisTB/sigesa-acreditacion-umss](https://github.com/BorisTB/sigesa-acreditacion-umss)

Entrega de aula: enlace al repositorio + nombre del release (`8.3.0`). El stack de la demo se presenta con `docker compose up -d --wait` desde un clon limpio (imagen multi-stage, healthchecks). Análisis k6 del grupo: `bench/e3/` (mediana de 3 corridas).

Registro de aportes: [`8.3.0-aportes.md`](../../8.3.0-aportes.md) (raíz). Cada commit con la identidad de su autor.

---

## Orden de presentación

Sorteo en clase (aleatorio). Completar tras el sorteo:

1. _por sorteo_
2. _por sorteo_
3. _por sorteo_
4. _por sorteo_

---

## Compromiso por integrante

Cada persona presenta **1 a 3** funcionalidades del release (1 grande, 2 medianas o 3 pequeñas), con **caso de uso FSD** y **criterios de aceptación**. Lo técnico de esta demo (imagen/compose y/o k6) cuenta como una funcionalidad de quien lo hace.

| Integrante | Funcionalidades (1–3) | FSD / contrato | Criterios de aceptación (resumen) | Issue / evidencia |
|------------|----------------------|----------------|-----------------------------------|-------------------|
| **Boris Terrazas** | 1. RAG ARCU-SUR 2023 en el asistente existente (sin agente nuevo) | [FSD-UC-033](../product/uc/FSD-UC-033.md) | Corpus MD indexado; `search_normative_docs` / path RAG; respuesta cita el manual 2023 (no placeholders); el cliente no transfiere el PDF | [#76](https://github.com/BorisTB/sigesa-acreditacion-umss/issues/76) |
| | 2. Contrato uniforme de errores REST | [FSD-UC-034](../product/uc/FSD-UC-034.md) | Envelope `{error, message, details?}`; 400/404/405/415/500; 500 genérico; `include-message/binding/stacktrace=never` | [#67](https://github.com/BorisTB/sigesa-acreditacion-umss/issues/67) |
| | 3. Imagen y stack de producción (ítem técnico Demo 3) | NFR imagen / compose | Dockerfile multi-stage (backend Temurin Alpine, frontend nginx); `docker compose up -d --wait` con healthchecks; hadolint/Trivy sin HIGH/CRITICAL injustificados | Lab sesión 8 + `backend/Dockerfile`, `frontend/Dockerfile`, `docker-compose.yml` |
| **Alexander Villca** | _por registrar_ | | | |
| **Aylen Laura** | _por registrar_ | | | |
| **Marlene Velasquez** | _por registrar_ | | | |

Análisis k6 (opción A del enunciado, lab E3 del grupo): script en `bench/e3/`, ritmo fijo, tabla p95 / % errores (mediana de 3) en [`bench/e3/INFORME.md`](../../bench/e3/INFORME.md). Lo presenta quien lo ejecutó; no se atribuye a Boris.

---

## Boris — detalle para la exposición

### 1. RAG ARCU-SUR (FSD-UC-033) — grande

- Pregunta de demo: *«¿Cómo se constituye el comité de pares ARCU-SUR?»*
- El chat sigue siendo `POST /api/v1/assistant/chat` (FAB / `/ayuda`).
- El PDF no viaja al navegador; el índice son 33 chunks Markdown en classpath + FTS PostgreSQL.

### 2. Errores REST (FSD-UC-034) — mediana

- Mostrar 400 (validación con `details`), 404, 405 y un 500 genérico (`INTERNAL_ERROR`) sin stack en JSON.
- Contraste: Spring Boot 4 por defecto ya no pone `message` en el body; el envelope del producto sí.

### 3. Stack Docker (ítem técnico) — mediana

Desde un clon limpio (con `.env` a partir de `.env.example`):

```bash
docker compose up -d --wait
```

`--wait` exige healthcheck en los servicios del path de producto (`db`, `minio`, `ollama`, `open-webui`, `backend`, `frontend`). El backend no arranca hasta que Postgres y MinIO estén healthy.

Hadolint / Trivy: ver evidencia del lab sesión 8 sobre `backend/Dockerfile` (usuario no-root, multi-stage, fat jar). Hallazgos altos/críticos: justificar en la exposición o no presentar imagen sucia.

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
