---
producto: "SIGESA"
grupo: "ACREDIA"
documento: DTP                 
version: v1.1                  
fecha: "2026-07-26"
status: vivo                   
audiencia: dual               
baseline_ref:                 
  dti: "docs/baseline/DTI_vFinal.md"
  tag: "release/2.0.0"
  commit: "HEAD"
release: "release/3.0.0"      
stack:
  - "Java 21"
  - "Spring Boot 4.x"
  - "Hibernate / Spring Data JPA"
  - "PostgreSQL (Principal) / H2 (Pruebas)"
  - "React 19"
  - "AWS"
repo: "ruta/a/tu/repo/sigesa"
agents_md: "/AGENTS.md"
artefactos_vivos:
  prd: "docs/product/03_prd/PRD.md"          
  fsd: "docs/product/FSD.md"          
  prompt_mapping: "docs/sprints/sprint_1/PROMPT_MAPPING.md"
  design_docs_dir: "docs/design/"     
  adr_dir: "docs/adr/"
---

# Documento Técnico del Producto (DTP) – SIGESA

> **Qué es**: El contrato técnico vigente de SIGESA durante la fase de implementación.
> **Regla de oro**: Cero divergencia silenciosa. El baseline de la Fase de Diseño (`release/2.0.0`) permanece intacto en `docs/baseline/`.

---

## A. Control de cambios (Núcleo del DTP)

### A.1 Changelog de implementación

*(Este cuadro se llenará a medida que se ejecuten los prompts de implementación y se envíen los PRs)*

| Fecha | Cambio | Disparador (FSD-UC / DD) | ADR | PR / commit | Autor |
| ------- | -------- | -------------------------- | ----- | ------------- | ------- |
| 26/07/2026 | **PostgreSQL:** Configuración del motor transaccional, Flyway y propiedades de persistencia. | FSD-SYS-001 / DD-SYS-001 | ADR-0002 | Pendiente | AI Agent |
| 23/07/2026 | **Full-Stack MOD-PROCESS:** Arquitectura Hexagonal estricta Backend y UI Frontend (React/Orval) para clonación de Plantillas (`PROCESS_ALREADY_ACTIVE`). | FSD-UC-003 / DD-UC-003 | N/A | PM-001 | AI Agent |
| 23/06/2026 | Trazabilidad 1:1 MOD-AUTH: split `DD-UC-001`/`DD-UC-002`; prompts `PR-IMPL-001`/`PR-IMPL-002`; `PR-IMPL-004` → `archive/`. | FSD-UC-001 / DD-UC-001 · FSD-UC-002 / DD-UC-002 | ADR-0003 | docs sync | Cursor Agent |
| 23/06/2026 | Sync inconsistencias MOD-AUTH: diagramas, modelo_datos, api_contracts, ADR-0003 vivo, FSD-BR-12. | FSD-UC-001, FSD-UC-002 / DD-UC-001, DD-UC-002 | ADR-0003 | docs sync | Cursor Agent |
| 22/06/2026 | `@dtp-sync` DD-UC-001: consolidación MOD-AUTH en DTP, FSD, api_contracts, modelo_datos. | FSD-UC-001, FSD-UC-002 / DD-UC-001 | ADR-0003 | `f38976b` / PM-007 | Cursor Agent |
| 22/06/2026 | Implementación MOD-AUTH (JWT, login, admin users, user_program_assignment, hardening code-review). | FSD-UC-001, FSD-UC-002 / DD-UC-001 | ADR-0003 | `5cd14df`…`f38976b` | Cursor Agent |
| 22/06/2026 | Inicialización de la arquitectura base Spring Boot y DTP vivo. | N/A | N/A | `init` | Boris Angulo |

## [2026-06-29] - Actualización de Arquitectura Frontend (FSD-UC-003)

### Dependencias Añadidas

- `lucide-react`: Adoptado como estándar para la iconografía de la interfaz.
- `react-router-dom`: Configurado para la gestión de rutas del lado del cliente.
- `orval`: Utilizado para la autogeneración de clientes y hooks de React Query a partir de OpenAPI (Swagger).

### Decisiones Técnicas y Refactorización

- **Migración a Tailwind CSS v4:** Se eliminaron los archivos de configuración legados (`tailwind.config.ts`, `postcss.config.js`). El sistema de diseño institucional y los tokens de color ahora se gestionan nativamente mediante la directiva `@theme` en `src/index.css`.
- **Tipografía:** Se estableció `Inter` como la tipografía principal sin serifa (`sans`), manteniendo `IBM Plex Mono` para contextos específicos.
- **Enrutamiento y Vistas:** Se registró la ruta `/procesos/nuevo` conectada al contenedor lógico `CreateProcessView`. Se implementaron mocks temporales a la espera de los endpoints `GET /careers` y `GET /templates`.

### A.2 Deltas respecto al DTI vFinal

> Diferencias **deliberadas** entre lo diseñado y lo construido.

| # | Sección del DTI afectada | Qué decía el DTI vFinal | Qué dice ahora el DTP | Motivo | ADR |
| --- | -------------------------- | ------------------------- | ----------------------- | -------- | ----- |
| 1 | Perímetro API | Endpoints legacy sin auth explícita en DTI piloto | Todo `/api/v1/**` excepto `POST /auth/login` exige JWT Bearer | MOD-AUTH v1.0 unifica seguridad antes de MOD-EVIDENCE | N/A (DD-UC-001) |
| 2 | Entrega password temporal | No especificado en API baseline | Alta genera password en servidor; entrega **offline** v1.0 (no en JSON response) | Evitar exposición en tránsito; capacitación [JD] | N/A |
| 3 | Migración DDL MOD-AUTH | Índice parcial en DTI | Flyway perfil `prod` + script `V1__mod_auth_uk_upa_active.sql`; H2 dev: `AuthSchemaInitializer` | Hibernate no genera índices parciales | N/A |
| 4 | Dashboard Architecture | `GET /dashboard/coordinator` aislado por rol | Arquitectura Híbrida Compuesta PBAC (`GET /api/v1/dashboards/me/summary`) + endpoints modulares (`/details`, `/export`) | Optimizar peticiones HTTP para usuarios multi-rol y renderizado dinámico en UI | N/A (DD-UC-011) |
| 5 | Stack Persistencia | H2 (Memoria/Archivo) para todo | **PostgreSQL** (Principal) + H2 (Test) | Las reglas de negocio (índices únicos activos) requieren motor transaccional robusto | ADR-0002 |
| 6 | Acreditación (MOD-PROCESS) | Arquitectura por capas implícita | **Arquitectura Hexagonal Estricta** (Puertos In/Out, Dominio Puro, Adaptadores) | Escalabilidad del modelo normativo y desacoplamiento de frameworks (JPA) | N/A (DD-UC-003) |

### A.3 Estado de implementación por FSD-UC

| FSD-UC | Design Doc | Estado | Release | Tests/Evals | PR-IMPL | Notas |
| -------- | ------------ | -------- | --------- | ------------- | --------- | ------- |
| `FSD-UC-001` | `DD-UC-001` | hecho | `release/3.0.0` | Suite §6 DD-UC-001; JaCoCo pendiente `mvn verify` | `PR-IMPL-001` | JWT + LocalAuthAdapter; A1 estricto → 401 |
| `FSD-UC-002` | `DD-UC-002` | hecho | `release/3.0.0` | Suite §6 DD-UC-002; JaCoCo pendiente `mvn verify` | `PR-IMPL-002` | Alta INACTIVE; revoke soft; 409 email dup |
| `FSD-UC-003` | `DD-UC-003` | **hecho (Full-Stack)** | `release/3.0.0` | Suite unitaria (Mockito); React Hooks | `PR-IMPL-003V3` | Arquitectura Hexagonal (Backend) + UI React c/ Orval |
| `FSD-UC-011` | `DD-UC-011` | en diseño | `release/3.0.0` | Suite §6 DD-UC-011 (Gherkin TC-09a/c) | `PR-IMPL-011` | Suite Híbrida Compuesta PBAC (`/me/summary`, `/details`, `/export`) + streaming binario |
| `FSD-SYS-001` | `DD-SYS-001` | **hecho** | `release/3.0.0` | Tests de conexión locales (Flyway) | `PR-IMPL-004` | Integración con PostgreSQL (Driver, HikariCP, YML) |

### A.4 Trazabilidad código ↔ DTP

`BRD/MRD (baseline)` → `PRD/FSD vivo (FSD-UC-NNN)` → `Design Doc (DD-UC-NNN)` → `Prompt (PR-IMPL-NNN)` → `PR/commit` → `Tests/Evals` → `ADR (si aplica)` → **DTP**.

---

## B. Contenido técnico vigente

> SIGESA utiliza **Arquitectura Hexagonal (Puertos y Adaptadores)** de manera estricta para los nuevos módulos core (ej. MOD-PROCESS). Cualquier desviación de los principios de Clean Architecture o del uso estricto de DTOs en adaptadores web será documentada aquí.

| Sección (espejo del DTI) | ¿Cambió vs DTI vFinal? | Dónde está la versión vigente |
| -------------------------- | ------------------------ | ------------------------------- |
| §1 Visión del producto | no | DTI vFinal §1 |
| §2 Contexto del sistema (C4 N1) | no | DTI vFinal §2 |
| §3 Arquitectura de alto nivel (C4 N2/N3) | **sí** | PostgreSQL reemplaza H2 en la persistencia principal (C4 N2 Container Diagram) |
| §4 Modelo de dominio | no | DTI vFinal §4 |
| §5 Arquitectura hexagonal del core | **sí** | Confirmada su exigencia estricta en FSD-UC-003. Dominio encapsulado. |
| **MOD-AUTH (identidad)** | **sí** | Ver §B.1 abajo; design docs `DD-UC-001`, `DD-UC-002` |
| **MOD-PROCESS (acreditación)** | **sí** | Nuevo en el DTP vivo (Ver §B.2). Ref: `DD-UC-003`. |
| §8 Despliegue cloud (AWS) | no | DTI vFinal §8 |
| §10 Prompt mapping | **sí (crece)** | `docs/sprints/sprint_1/PROMPT_MAPPING.md` (Entradas vigentes PM-001+) |
| §21 ADRs | **sí (crece)** | [`docs/adr/`](../adr/) (ADR-0003 MOD-AUTH, **ADR-0002 PostgreSQL**; baseline en `docs/baseline/05_dti/adrs/`) |

### B.1 MOD-AUTH — contrato técnico vigente (`DD-UC-001` + `DD-UC-002`)

*(Sin cambios respecto a la actualización del 22/06/2026)*

| Área | Detalle vigente |
| --- | --- |
| **Endpoints** | `POST /api/v1/auth/login` (público); `POST /api/v1/admin/users` ([JD]); `PATCH /api/v1/admin/users/{id}/deactivate` ([JD]) |
| **Perímetro JWT** | Todo `/api/v1/**` excepto login exige `Authorization: Bearer` (delta §A.2 #1) |
| **Tablas JPA** | `app_user`, `user_program_assignment` |
| **Índice parcial** | `uk_upa_active` — Flyway (`application-prod.yaml`) |
| **Password hashing** | Argon2id (`Argon2PasswordEncoder`) |
| **JWT** | HS256; claims `sub`, `email`, `role`, `programScope[]`; secret `SIGESA_JWT_SECRET` |

### B.2 MOD-PROCESS — contrato técnico vigente (`DD-UC-003`)

**Implementación:** PM-001 · **Prompts vigentes:** `PR-IMPL-003V3`

| Área | Detalle vigente |
| --- | --- |
| **Endpoints (Web Adapter)** | `POST /api/v1/processes` ([JD]) |
| **DTOs (Web Adapter)** | `CreateProcessRequestDto`, `ProcessResponseDto` |
| **Lógica de Negocio (Use Case)** | Clonación profunda de estructura (Plantilla → Proceso Vivo). Aislamiento ACID (`@Transactional`). |
| **Tablas JPA (Persistencia)** | `templates`, `template_phases`, `template_subphases`, `accreditation_processes`, `phases`, `subphases` |
| **Regla Unicidad (BD + App)** | Error HTTP 409 `PROCESS_ALREADY_ACTIVE`. Flyway script con `CREATE UNIQUE INDEX idx_unique_active_process ON accreditation_processes (career_id) WHERE status = 'ACTIVE'` |
| **Modelos de Dominio** | Puros (sin `@Entity`, sin anotaciones Spring). Interfaz con JPA a través de `ProcessPersistenceMapper`. |

---

## C. Integraciones

### C.1 React + Orval (Frontend)

El consumo de la API REST se realiza **exclusivamente** mediante hooks de React Query autogenerados por Orval (ubicados en `frontend/src/api/`). Queda prohibida la escritura de clientes HTTP (fetch/axios) manuales en la capa de UI.

- **Ciclo de vida:** Cualquier cambio en los contratos de la API (DTOs del backend) requiere ejecutar el comando `pnpm run generate:api` en el entorno frontend antes de ser consumido por las vistas (ej. `CreateProcessView`).
