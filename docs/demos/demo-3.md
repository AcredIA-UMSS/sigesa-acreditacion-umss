# Demo 3 — AcredIA / SIGESA (`release/8.3.0`)

**Fecha de demo:** Miércoles 4 de noviembre de 2026  
**Release:** `8.3.0`  
**Rama:** `release/8.3.0`  
**Tag previsto:** `8.3.0` / `v8.3.0`  
**Repositorio:** [AcredIA-UMSS/sigesa-acreditacion-umss](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss)  

Entrega de aula: enlace al repositorio + nombre del release (`8.3.0`).  
Registro de aportes: [`8.3.0-aportes.md`](../../8.3.0-aportes.md) (raíz). Cada commit con la identidad individual de su autor.

---

## 1. Compromiso por Integrante

Cada integrante presenta **1 a 3** funcionalidades del release (1 grande, 2 medianas o 3 pequeñas), con **caso de uso FSD** y **criterios de aceptación**. Lo técnico de esta demo (imagen/compose y/o análisis E3) cuenta como una funcionalidad de quien lo hace.

| Integrante | Funcionalidades Presentadas | FSD / Contrato | Criterios de Aceptación (Resumen) | Issue / Evidencia |
|---|---|---|---|---|
| **Alexander Alvarez** | 1. Exportación Asíncrona del Dossier Digital en ZIP Jerárquico (Grande)<br>2. Análisis Técnico 2️⃣🅱️ Extracción Microservicio E3 (Técnico) | [`FSD-UC-032`](../product/uc/FSD-UC-032.md)<br>[`ADR-001`](../adr/ADR-001-extraer-modulo.md) | Streaming directo a S3/MinIO sin archivos en disco local ni buffers en heap; semáforo global concurrencia (429 Retry-After) y 1 por usuario (409 Conflict); lectura paralela acotada a 8 hilos; purga automática TTL 24–48h; RBAC carrera CC; Presigned URL S3.<br>Análisis k6 E3: p95 +17.34 ms (> 15 ms) y health 1.322% errores; veredicto ADR-001 de mantener en monolito. | [#73](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss/issues/73)<br>[`bench/e3/INFORME.md`](../../bench/e3/INFORME.md) |
| **Boris Anthony Angulo Urquieta** | 1. RAG ARCU-SUR 2023 en el asistente existente (Grande)<br>2. Contrato uniforme de errores REST (Mediana)<br>3. Imagen y stack de producción endurecido (Técnico 1️⃣) | [`FSD-UC-033`](../product/uc/FSD-UC-033.md)<br>[`FSD-UC-034`](../product/uc/FSD-UC-034.md)<br>Ops/NFR | Corpus MD indexado en classpath + FTS; cita manual 2023; el cliente no transfiere PDF.<br>Envelope `{error, message, details?}`; 400/404/405/415/500; 500 genérico sin stacktrace.<br>Multi-stage Dockerfile JRE 21 Alpine, usuario `sigesa` (UID 100), Trivy 0 CVEs OS (High/Critical). | [#76](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss/issues/76)<br>[#67](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss/issues/67)<br>[`imagen-o-podman.md`](../laboratorios/imagen-o-podman.md) |
| **Aylen Mariangel Gonzáles Alvino** | 1. Portal público de acreditación (listado, estado y certificado PDF)<br>2. Noticias y destacados (home + listado + admin [JD]) | [`FSD-UC-016`](../product/uc/FSD-UC-016.md)<br>[`FSD-UC-035`](../product/uc/FSD-UC-035.md) | Anónimo ve solo carreras `published`; busca por nombre; 404 genérico si no publicado; PDF solo si certificado publicado.<br>[P] lee solo `publicada`; una destacada o la más reciente; [JD] CRUD/publicar/destacar; 404 genérico en borrador. | `b68fd5d1`<br>`df465c15`<br>`07c0ae8c`<br>`0da6bdff`<br>`fd5c5810`<br>PM-039 / PM-040 |
| **Marlene Velasquez** | 1. Validación de registro de usuarios<br>2. Login institucional y Liveness (`/api/health`) | [`FSD-UC-002`](../product/uc/FSD-UC-002.md)<br>[`FSD-UC-001`](../product/uc/FSD-UC-001.md) | Validación estricta cliente/servidor (email institucional `@umss.edu.bo`, celular 8 dígitos, contraseña fuerte); login JWT institucional; endpoint `/api/health` respondiendo `{"status":"UP"}`. | Rama release/8.2.0 + 8.3.0 |

---

## 2. Lo Técnico de Esta Demo

### 1️⃣ Imagen y Stack de Producción
- **Dockerfile multi-stage:** Construcción en dos etapas con `eclipse-temurin:21-jdk-alpine` para build y `eclipse-temurin:21-jre-alpine` para ejecución.
- **Usuario no-root:** Ejecución bajo usuario y grupo `sigesa` (`uid=100 gid=101`).
- **Seguridad y Auditoría:** 
  - **Trivy:** 0 vulnerabilidades High o Critical en el OS.
  - **hadolint:** Sin hallazgos de severidad Error ni Warning.
- **Compose reproducible:** Archivos [`compose.yaml`](../../compose.yaml) y [`docker-compose.yml`](../../docker-compose.yml) configurados con healthchecks activos para levantar todo el stack con:
  ```bash
  docker compose up -d --wait
  ```

### 2️⃣ Análisis Técnico: 🅱️ Extracción a Microservicio E3 (Alexander Alvarez)
- **Módulo evaluado:** Módulo de Notificaciones asíncronas (`notification_outbox` / `NotificationModulePort`).
- **Documento de Decisión:** [`ADR-001: Mantener el módulo de notificaciones en el monolito`](../adr/ADR-001-extraer-modulo.md).
- **Informe de Benchmark:** [`bench/e3/INFORME.md`](../../bench/e3/INFORME.md).
- **Resultados empíricos con 150k filas en PostgreSQL:**
  - $\Delta p95 = +17.34\text{ ms} > 15\text{ ms}$ (H1 falsificada por overhead HTTP y serialización).
  - En corte de red con Toxiproxy, el colapso del pool de conexiones del cliente HTTP degradó `GET /api/health` a 1.322% de errores (H2 falsificada al superar el 1% de SLA).
  - Veredicto: **Rechazar microservicio y mantener el módulo en el monolito modular.**

---

## 3. Detalle para Exposición de Integrantes

### Aylen Laura / Aylen Gonzáles
Identidad Git de los commits: **Aylen Gonzáles** `<82118099+Aylen-omg@users.noreply.github.com>`.

#### 1. Portal público (FSD-UC-016) — grande
- Ruta UI: `/public/status` (banner «Estado de Acreditación» en la home).
- Anónimo: `GET /api/v1/public/programs` (nombre + slug), `GET /api/v1/public/programs/{slug}` (estado vigente), `GET /api/v1/public/programs/{slug}/certificate` (PDF en memoria).
- FSD-BR-10: borrador u observaciones internas → **404 genérico** (`NOT_FOUND` / «El recurso solicitado no existe.»). No revela si la carrera existe.
- Slug = `programs.code` en minúsculas (`INF-SIS` → `inf-sis`). Filtro por nombre en cliente.
- Gherkin: `@PRD-US-016 @FSD-UC-016 @TC-PUB` y `@PRD-US-020 @TC-PUB2`.

#### 2. Noticias y destacados (FSD-UC-035) — grande
- [P]: home `#noticias`, `/public/news`, `/public/news/{slug}`. El botón **Noticias** del header abre el listado y conserva header/footer.
- [JD]: `/admin/noticias` — crear borrador, editar, publicar/despublicar, destacar. Seed: `jd@umss.edu.bo` / `JefeDemo2026!`.
- `GET /api/v1/public/news`, `/featured` (destacada o más reciente; 204 si no hay), `/{slug}`. Admin `/api/v1/admin/news` (JWT, rol JD).
- Una sola destacada; despublicar fuerza `featured=false`; destacar borrador → 409 `NEWS_NOT_PUBLISHED`.
- Home institucional: logos DUEA/UMSS y fotos de campus en hero y noticias.
- Gherkin: `@PRD-US-027|028|029 @FSD-UC-035 @TC-NEWS` (6 escenarios).

Commits y criterios escritos: [`8.3.0-aportes.md`](../../8.3.0-aportes.md).

---

## 4. Guía de Ejecución y Despliegue Limpio

```bash
git clone https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss.git
cd sigesa-acreditacion-umss
git checkout release/8.3.0
cp .env.example .env
docker compose up -d --wait
```

- **Frontend:** http://localhost:3000  
- **Backend:** http://localhost:8080 (`/api/health`, `/v3/api-docs`)  
- **MinIO Consola:** http://localhost:9001  
- **Usuarios de prueba:** `admin` / `admin123`, `ceub` / `ceub123`, `docente` / `docente123`.
