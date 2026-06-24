# ADR_009: Backend REST con Node.js 20 y Express 4

| Campo | Valor |
|-------|-------|
| **Canónico** | [ADR-0009](../../adr/ADR-0009-backend-nodejs-express.md) |
| **Origen equipo** | `team/aylenGonzales/09_dti/adr/ADR-006.md` |

## Metadatos

| Campo | Valor |
|-------|-------|
| Número | `0006` |
| Título | Backend REST con Node.js 20 + Express 4 (descartando FastAPI en v1.0) |
| Fecha | 16/05/2026 |
| Autor(es) | Equipo AcredIA |
| Estado | **Aceptada** |
| Alcance | Capa de aplicación — API REST, integraciones SMTP, generación PDF server-side |
| Stakeholders consultados | Tech Lead AcredIA · @ArchAgent · @DevAgent |

---

## 1. Contexto

El FSD v2.0 declara en §2.3 un **spike de 2 días** para elegir entre **Node.js/Express** y **FastAPI (Python)** como runtime del backend. La decisión **bloquea T-01** (Docker Compose monorepo) y **T-02** (autenticación JWT + RBAC), y condiciona el motor de reportes PDF (PDFKit vs. ReportLab), las notificaciones (Nodemailer vs. smtplib) y la estrategia de pruebas unitarias (Jest vs. Pytest) declaradas en el mismo FSD.

**Restricciones relevantes:**
- Arquitectura web pura: SPA React + API REST stateless (§2.1, ADR-0004).
- PostgreSQL 16 como única BD (ADR-0003); el ORM/driver debe ser maduro en el runtime elegido.
- Presupuesto $0 en v1.0 (SA-05); sin servicios PaaS de ejecución gestionados.
- Equipo de ~4 desarrolladores con plazo al piloto Q3–Q4 2026; la curva de aprendizaje no puede consumir más de un sprint de infraestructura.
- NFR-009 exige cobertura unitaria backend ≥ 80 % con herramienta ya prevista en FSD (Jest para Node).
- ADR-0004 referencia `jsonwebtoken` (Node) o `python-jose` (Python) según resultado de este spike.

**Fuerzas en tensión:**
- **Cohesión de stack vs. tipado fuerte**: FastAPI ofrece tipado nativo y OpenAPI automático; Express ofrece alineación con el ecosistema npm del frontend y menor fricción para el equipo.
- **Rendimiento async vs. time-to-market**: FastAPI/asyncio escala bien en I/O; Express con `async/await` nativo en Node 20 cubre el volumen del piloto (~150 usuarios concurrentes máx. según ADR-0004).
- **Herramientas transversales**: un solo ecosistema (JavaScript/TypeScript) simplifica monorepo, scripts CI y contratos compartidos con el frontend.

**Resultado del spike (2 días, criterios FSD §2.3):**
- Bootstrap Docker Compose + endpoint health + conexión PostgreSQL: **Express** completado en menor tiempo que el equivalente FastAPI para el equipo.
- POC JWT + middleware RBAC (T-02): implementación de referencia con `jsonwebtoken` + `express` validada contra escenarios Gherkin FSD-UC-001.
- POC carga multipart + hash SHA-256 (alineado ADR-0001): `multer` + `crypto` sin bloqueos.
- Alineación con PDFKit y Nodemailer ya listados en FSD §2.3 para la rama Node.

---

## 2. Alternativas consideradas

| Alternativa | Pros | Contras | Costo aproximado |
|-------------|------|---------|-----------------|
| **A. Node.js 20 LTS + Express 4** | Mismo ecosistema que React (npm); Jest/PDFKit/Nodemailer sin cambio de lenguaje; `jsonwebtoken` alineado con ADR-0004; amplia documentación; despliegue en imagen Docker `node:20-alpine` liviana | Tipado opcional (TypeScript recomendado pero no obligatorio en v1.0); OpenAPI requiere `swagger-jsdoc` o similar | $0 |
| **B. Python 3.12 + FastAPI** | Tipado con Pydantic; OpenAPI automático; async nativo; ReportLab maduro para PDF | Segundo lenguaje en el monorepo; Pytest en lugar de Jest unifica menos con frontend; Alembic vs. herramientas Node para migraciones; curva adicional para equipo con mayor experiencia en JavaScript | $0 |
| **C. Node.js + NestJS** | Estructura modular tipo Spring; DI integrada; buen soporte TypeScript | Más ceremonia y archivos que Express para el tamaño del piloto; tiempo de onboarding > spike de 2 días | $0 |
| **D. Serverless (AWS Lambda / similar)** | Escala automática | Costo OPEX; incompatible con restricción UMSS de servidor Docker institucional (SA-05) | Descartado |

---

## 3. Decisión

> **Elegimos la alternativa A: Node.js 20 LTS con Express 4 como framework HTTP del backend REST de SIGESA v1.0.**

Express satisface el resultado del spike: menor tiempo de bootstrap de T-01/T-02, coherencia con JWT (ADR-0004), almacenamiento de evidencias (ADR-0001), log en PostgreSQL (ADR-0002 y ADR-0003), y las herramientas auxiliares ya declaradas en FSD §2.3 (PDFKit, Nodemailer, Jest). FastAPI queda **descartada para v1.0** no por deficiencia técnica, sino por **costo de contexto** (segundo runtime, migraciones y pruebas en Python) sin beneficio proporcional para el volumen y plazo del piloto UMSS.

La API se organiza en capas (rutas → controladores → servicios → repositorios PostgreSQL) sin imponer NestJS en v1.0. TypeScript es **recomendado** para módulos críticos (auth, documentos) pero no bloquea el sprint inicial.

Migraciones de esquema: **node-pg-migrate** o **Knex** (equivalente operativo a Flyway/Alembic citado en T-03 del FSD para la rama Node).

---

## 4. Consecuencias

### 4.1 Positivas
- Monorepo con scripts npm unificados (`frontend/`, `backend/`).
- T-02 implementa `jsonwebtoken` según ADR-0004 sin bifurcación Python.
- NFR-009 verificable con Jest + Supertest en la misma toolchain.
- Imagen Docker backend compacta (`node:20-alpine`); compatible con Docker Compose de T-01.
- Comunidad y ejemplos abundantes para Express + PostgreSQL (`pg`).

### 4.2 Negativas / costos
- Tipado menos estricto que FastAPI si no se adopta TypeScript de forma consistente.
- OpenAPI no se genera automáticamente; documentar contratos REST con OpenAPI 3.0 manual o `swagger-jsdoc` (esfuerzo ~0,5 sprint).
- Carga CPU intensiva de PDF grandes puede bloquear el event loop; mitigación: worker threads o cola en proceso para T-08 (reportes).

### 4.3 Neutras / observables
- Versión objetivo: **Node 20 LTS**, **Express 4.x**.
- Driver BD: `pg` (node-postgres) con pool de conexiones.
- FastAPI permanece como **Plan B** documentado en §6 si el equipo incorpora especialistas Python o requisitos de ML on-prem en v2.0.

---

## 5. Impacto en el sistema

- **Código**: T-01 (Docker Compose con servicio `backend` Node); T-02 (MOD-01 auth); T-03 (migraciones Knex/node-pg-migrate); T-04 a T-12 (módulos REST). Estructura sugerida: `backend/src/{routes,controllers,services,repositories,middleware}/`.
- **Operaciones**: contenedor `sigesa-api` en `docker-compose.yml`; variable `NODE_ENV`; healthcheck `GET /health`.
- **Seguridad**: middleware `helmet`, `cors` restringido al origen de la SPA, validación de entrada con `zod` o `joi`; sin cambio respecto a TLS en reverse proxy (NFR-003).
- **Equipo**: refuerzo opcional de TypeScript en sprint 2; sin contratación de perfil Python exclusivo en v1.0.
- **Costo**: $0 licencias.

---

## 6. Plan de reversión

**Señales de que la decisión fue incorrecta:**
- Requisito institucional de bibliotecas científicas / ML solo disponibles en Python para módulos de analítica en v2.0.
- Deuda de tipado provoca regresiones que Supertest no detecta y el Tech Lead exige contratos Pydantic-equivalentes.
- Cuellos de botella demostrados en generación PDF/notificaciones que no se resuelven con workers Node.

**Costo estimado de revertir:** alto (2–3 sprints). Reescritura de API, migración de tests a Pytest, nuevo Dockerfile Python, adaptación de ADR-0004 a `python-jose`, revalidación de TC-001 a TC-010.

**Plan B:** FastAPI 3.12 + Uvicorn en contenedor paralelo; estrangulamiento por módulo empezando por reportes (T-08) si solo ese dominio requiere Python.

---

## 7. Validación

| Criterio | Métrica | Plazo | Responsable |
|----------|---------|-------|-------------|
| Spike cerrado | ADR-0006 aceptada; FSD §2.3 sin “pendiente spike” | Sprint 0 | @ArchAgent |
| T-01 | `docker compose up` levanta frontend + backend Node + PostgreSQL 16 + volumen evidencias | Sprint 1 | @DevAgent |
| T-02 | TC-001 y TC-002 pasan con backend Express | Sprint 1 | @DevAgent |
| NFR-009 | Cobertura Jest ≥ 80 % en `backend/src` | Sprint 2 | @DevAgent |
| Integración ADR | Sin referencias a `python-jose` en código productivo v1.0 | Sprint 1 | @ArchAgent |

---

## 8. Referencias

- FSD v2.0 AcredIA/SIGESA — §2.1, §2.3 Stack tecnológico, §2.4 T-01, T-02, T-03, T-08, NFR-001, NFR-009, FSD-UC-001, PC-001.
- PRD v1.0 — PRD-REQ-001 a PRD-REQ-013 (API REST transversal).
- BRD v2.0 — BR-008 (disponibilidad), RB-06 (dominio @umss.edu.bo).
- ADR-0001 (almacenamiento evidencias), ADR-0002 (log auditoría), ADR-0003 (PostgreSQL 16), ADR-0004 (JWT + RBAC).
- Documentación: [Express](https://expressjs.com/), [Node.js 20 LTS](https://nodejs.org/), [pg](https://node-postgres.com/).

---

## 9. Historial

| Versión | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| 1 | 16/05/2026 | Equipo AcredIA | Propuesta inicial post-spike |
| 2 | 16/05/2026 | Equipo AcredIA | Aceptada — cierra pendiente FSD §2.3; habilita T-01 y T-02 |
