# Diseño Técnico — Reporting / Dashboard (Backend) — v1.2

> **Proyecto:** SIGESA — Sistema de Gestión de Acreditación Universitaria  
> **Feature:** Reporting / Dashboard (backend) — MOD-DASH · MOD-REPORT  
> **Versión del documento:** 1.2  
> **Autor:** alexAlvarez  
> **Fecha:** 2026-06-23  
> **Stack:** Java 21 · Spring Boot · Spring Data JPA · Hibernate Types (jsonb) · H2 (dev) / PostgreSQL (prod)

---

## 0. Trazabilidad y referencias

### 0.1 Cadena de especificación

| Capa | ID | Descripción |
|------|-----|-------------|
| PRD | PRD-REQ-011 | Panel semáforo [JD] |
| PRD | PRD-REQ-012 | Dashboard [CC] / bandeja [TD] |
| PRD | PRD-REQ-014 | Reporte ejecutivo PDF |
| PRD-US | US-012, US-015 | Dashboard [CC] y observaciones abiertas |
| PRD-US | US-013 | Panel semáforo [JD] |
| PRD-US | US-014 | Bandeja auditoría [TD] |
| PRD-US | US-021 | Reporte PDF ≤ 2 clics |
| FSD | FSD-UC-011 | Dashboard [CC] — `/coordinator/dashboard` |
| FSD | FSD-UC-012 | Bandeja [TD] — `/technician/inbox` |
| FSD | FSD-UC-013 | Semáforo [JD] — `/executive/semaphore` |
| FSD | FSD-UC-014 | Reporte PDF — `POST /reports/executive/pdf` |
| ADR | [ADR-0015](../../../docs/adr/ADR-0015-dashboard-sync-async-reporting.md) | Sync dashboard + async export |
| ADR | ADR-0007 | JWT/RBAC |
| ADR | ADR-0013 | S3/MinIO blobs |
| PC | [PC-MOD-DASH-01](../../../docs/06_prompt_contracts/contract_fsd_008_mod_dash_backend.md) | Prompt contract backend |
| PM | PM-056 | Registro prompt mapping (2026-06-23) |

Documentos canónicos (repo padre `sigesa-docs`, no duplicar en submodule):

- FSD casos de uso: `docs/04_fsd/casos_uso.md`
- Contratos API: `docs/04_fsd/api_contracts.md` §7–8 (MOD-DASH, MOD-REPORT)
- MVP runtime: `docs/05_dti/api_contracts_mvp_runtime.md` §4
- Matriz trazabilidad: `docs/09_trazabilidad/matriz_trazabilidad.md`
- Gherkin: `docs/04_fsd/gherkin.md` (TC-09a/b/c, TC-11)

### 0.2 Mapeo sección → FSD-UC

| Sección diseño | FSD-UC | Actor | Test case |
|----------------|--------|-------|-----------|
| §7 DashboardService sync | UC-011, UC-012, UC-013 | [CC], [TD], [JD] | TC-09a, TC-09b, TC-09 |
| §7 ReportService async | UC-014 | [JD] | TC-11 |
| §7 SecurityInjector | UC-011, UC-012 | [CC], [TD] | TC-09c, NFR-009 |
| §8 Export Engine | UC-014 | [JD] | TC-11 |

### 0.3 Alineación API (canónico vs implementación)

| ID | Contrato FSD | MVP runtime | Implementación Java actual | Estado |
|----|--------------|-------------|----------------------------|--------|
| API-DASH-01 | `GET /dashboard/coordinator` | ✓ MVP | Pendiente (usa `/kpis` genérico) | Drift |
| API-DASH-02 | `GET /dashboard/technician` | ✓ MVP | Pendiente (usa `/data` genérico) | Drift |
| API-DASH-03 | `GET /dashboard/executive` | Excluido MVP | No implementado | v1.1 |
| — | `GET /dashboard/kpis` | — | ✓ `DashboardController` | Provisional |
| — | `GET /dashboard/data` | — | ✓ `DashboardController` | Provisional |
| API-REP-01 | `POST /reports/executive/pdf` | Documentado | Pendiente | Próximo paso |

**Plan de alineación:** exponer rutas por rol que deleguen a `DashboardService` con `SecurityInjector`; deprecar `/kpis` y `/data` tras migración front.

### 0.4 UI — AcredIA Design System

El frontend consume estos endpoints según frames Figma:

| Rol | Frame Figma | Componentes DS |
|-----|-------------|----------------|
| [JD] | `figma/frames/prototipo/jd-admin-dashboard.md` | Sidebar rol, alertas auditoría, semáforos |
| [CC] | Coordinator dashboard (MVP front) | KPIs fase, observaciones abiertas |
| [TD] | Technician inbox | Bandeja filtrable por carrera/fase/estado |

Tokens y layout: `figma/tokens/css-variables.css`, `figma/layouts/layout-system.md`.

Diagramas de secuencia: `docs/07_diagramas/seq-004-004-dashboard-semaforos.mmd`, `seq-004-004-dashboard-drilldown.mmd`.

---

## 1. Objetivo
Proveer un diseño técnico backend claro, accionable y alineado con el estilo de `base_design_system.md` para implementar un módulo de reportes y dashboards que: (a) atienda las necesidades interactivas de la UI (KPIs y tablas paginadas) con baja latencia; (b) soporte exportaciones pesadas en formato Excel (.xlsx) por procesamiento asíncrono; (c) garantice seguridad de datos mediante inyección obligatoria de límites de alcance (RBAC) y limpieza estricta de datos exportados.

Este documento explica el porqué y el cómo de cada decisión, proporcionando DDL, snippets Java (Lombok + hibernate-types), DTOs, contratos de API y un plan de pruebas.

---

## 2. Alcance (ampliado y justificado)
Tabla y explicaciones: Prioriza el *por qué*.

| Incluido | Por qué | Excluido | Por qué no en v1.1 |
|---|---|---|---|
| Endpoints Dashboard (Sync): GET /api/v1/dashboard/... | UI necesita respuestas rápidas; evitar overhead de creación de Run | Frontend/UI rendering | Responsabilidad de frontend (layouts, charts) |
| Endpoints Reports (Async) para exportación .xlsx | Exportaciones pueden ser pesadas; procesarlas async protege la UI | CSV simple | .xlsx permite multi-hoja y mejor formateo para usuarios de coordinación |
| Pipeline unificado de filtros con inyección RBAC | Evita fugas de datos y asegura coherencia entre KPIs y filas | Confiar en filtros cliente | Inseguro; no garantiza alcance del usuario |
| Mapping JSONB usando hibernate-types | Mantiene tipos Java robustos para params y filtros | Almacenamiento de JSON como String | Pierde seguridad de tipos y búsqueda indexada |
| Caching de consultas (Caffeine) para KPIs | Reduce latencia en dashboard | Cache para export heavy runs | Exports must be fresh/snapshot-based |
| Export streaming a S3/MinIO y pre-signed URLs | Evita OOM y permite descarga segura | Almacenamiento en DB blob | Ineficiente para grandes archivos |

---

## 3. Modelo de Dominio (entidades y rationale)
Objetivo: mantener modelos minimalistas, auditables y versionados.

Entities (detallado con propósito):

- ReportDefinition
  - Propósito: describe un reporte reutilizable (metadata). Incluye `filters_allowed` y `metrics` en JSONB para flexibilidad. Versionado obligatorio (`version`) para trazabilidad.
  - Campos clave: `codigo` (único, legible), `audiences` (who may see/execute), `filtersAllowed` (restricción de parámetros permitidos).

- ReportRun
  - Propósito: historial inmutable de exportaciones (async) y su meta (KPIs snapshot + download_url).
  - Campos clave: `status` (ENUM, control estricto), `params` (JSONB), `result_metadata` (JSONB con KPIs), `download_url` (pre-signed), `created_by`.
  - Rationale: separar sync reads de runs evita contención y facilita auditoría.

- FilterPayload (value object)
  - Propósito: representación tipada de filtros aplicables (gestion, careerIds, facultyIds, processType, dateFrom/dateTo, status). Es la única fuente de truth que toca la BD.
  - Uso: mutado por el SecurityInjector antes de consultas.

- ReportMetric (POJO en metrics JSON)
  - Propósito: define nombre, expression (SQL/DSL), tipo de agregación y time_grain.

---

## 4. Base de Datos (DDL y explicaciones)
Se prioriza Postgres en prod; se incluye `report_run_status` ENUM y JSONB mapping.

```sql
-- Enum para status de ejecución: asegura valores válidos y permite consultas indexadas
CREATE TYPE IF NOT EXISTS report_run_status AS ENUM ('PENDING','PROCESSING','COMPLETED','FAILED');

CREATE TABLE report_definition (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  codigo VARCHAR(100) NOT NULL UNIQUE,
  nombre VARCHAR(255) NOT NULL,
  descripcion TEXT,
  owner_role VARCHAR(50), -- rol responsable (ej. CC, TD)
  audiences JSONB, -- lista de roles o grupos que pueden ver/ejecutar
  filters_allowed JSONB, -- schema de filtros permitidos (ej. {"careerId": "LONG", ...})
  metrics JSONB, -- definición de métricas y expresiones
  version INT DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted_at TIMESTAMP
);

-- Tabla de ejecuciones (report_run) para exportaciones async
CREATE TABLE report_run (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  report_definition_id BIGINT NOT NULL REFERENCES report_definition(id),
  params JSONB NOT NULL, -- FilterPayload serializado
  status report_run_status NOT NULL DEFAULT 'PENDING',
  started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  finished_at TIMESTAMP,
  result_json JSONB, -- optional raw result snapshot
  result_metadata JSONB, -- KPIs snapshot (structured) para quick view
  download_url VARCHAR(1000), -- pre-signed URL to S3/MinIO
  created_by VARCHAR(100)
);

-- Indexes para aceleración de consultas frecuentes y filtros compuestos
CREATE INDEX idx_report_def_codigo ON report_definition(codigo);
CREATE INDEX idx_report_run_started_at ON report_run(started_at);
CREATE INDEX idx_report_run_status ON report_run(status);
CREATE INDEX idx_report_run_def ON report_run(report_definition_id);
-- Evidence indexes (assumes evidencia table exists)
CREATE INDEX IF NOT EXISTS idx_evidence_career ON evidencia (career_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_evidence_faculty ON evidencia (faculty_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_evidence_process_type ON evidencia (process_type) WHERE deleted_at IS NULL;
```

Notas de diseño:
- JSONB permite consultas y indexado (GIN) si es necesario para búsquedas por claves de params.
- H2 en dev puede almacenar JSON en TEXT; tests deben mockear comportamiento JSONB o usar testcontainers Postgres para integración real.

---

## 5. Capa de Persistencia (JPA + hibernate-types)
Recomendación: incluir `com.vladmihalcea:hibernate-types-52` para mapear JSONB a Map/POJO.

Ejemplo entity con mapeo JSONB:

```java
@Entity
@Table(name = "report_definition")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReportDefinition {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 100)
  private String codigo;

  @Column(nullable = false, length = 255)
  private String nombre;

  @Column(columnDefinition = "TEXT")
  private String descripcion;

  @Type(com.vladmihalcea.hibernate.type.json.JsonType.class)
  @Column(columnDefinition = "jsonb")
  private Map<String, Object> filtersAllowed; // typed access to filter schema

  @Type(com.vladmihalcea.hibernate.type.json.JsonType.class)
  @Column(columnDefinition = "jsonb")
  private Map<String, Object> metrics; // metric definitions

  private Integer version;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime deletedAt;
}
```

Repository signatures:

```java
public interface ReportDefinitionRepository extends JpaRepository<ReportDefinition, Long>, JpaSpecificationExecutor<ReportDefinition> {
  Optional<ReportDefinition> findByCodigoAndDeletedAtIsNull(String codigo);
}

public interface ReportRunRepository extends JpaRepository<ReportRun, Long> {
  Page<ReportRun> findByCreatedBy(String createdBy, Pageable pageable);
}
```

---

## 6. DTOs y FilterPayload (tipado y propósito)
Se debe evitar exponer entidades. DTOs definen columnas exportables y API contract.

FilterPayload (POJO usado a lo largo del pipeline):
```java
public class FilterPayload {
  private Integer gestion; // por qué: año o gestión académica
  private List<Long> careerIds; // por qué: permite filtrado por carreras específicas
  private List<Long> facultyIds; // por qué: ámbito facultad
  private String processType; // NATIONAL | REGIONAL — por qué: cambia reglas de negocio
  private LocalDate dateFrom;
  private LocalDate dateTo;
  private List<String> statuses; // APROBADO, EN_PROCESO, etc.
}
```

Export DTOs (ejemplo para hoja Detallada):
```java
public record ReportDetailRowDTO(
  String careerName,
  String facultyName,
  String indicatorName,
  String evidenceDescription,
  String status,
  LocalDate submissionDate,
  Long evidenceCount
) {}
```

Export DTOs (hoja Resumen / KPIs):
```java
public record ReportKpiDTO(String key, String description, BigDecimal value) {}
```

Rationale: los DTOs contienen etiquetas legibles (nombres y descripciones) en vez de códigos crípticos; son la única fuente usada para escribir celdas en Excel.

---

## 7. Capa de Servicio — contratos y separación Sync vs Async
Interface pública (resumen):

```java
public interface DashboardService {
  DashboardKpiResponse getKpis(FilterPayload filter, Pageable pageable, String actor);
  Page<ReportDetailRowDTO> getPaginatedData(FilterPayload filter, Pageable pageable, String actor);
}

public interface ReportService {
  ReportDefinitionResponse createDefinition(ReportDefinitionCreateRequest req, String actor);
  ReportRunResponse submitExport(Long definitionId, FilterPayload filter, String actor); // creates ReportRun and returns runId
  ReportRunResponse getRun(Long runId, String actor);
}
```

Arquitectura de ejecución:
- DashboardService methods: Synchronous read-only; validate and mutate FilterPayload via SecurityInjector; hit DB or Caffeine cache; return DTOs.
- ReportService.submitExport: create ReportRun (status=PROCESSING), return 202 + runId; schedule async worker (Virtual Threads) to compute KPIs, stream rows to Excel using DTOs, upload to storage and update ReportRun with download_url and status.

SecurityInjector (cross-cutting):
- Implementar como Aspect (`@Around`) o como primer paso en controller that calls `FilterSanitizer.applySecurityContext(filter, SecurityContextHolder.getContext())`.
- Must enforce scope: if actor has role CC tied to career X, enforce filter.careerIds = [X]; if actor broader (JD), allow broader or all.

---

## 8. Export Engine (.xlsx) — rules and implementation notes
Requirements:
- Use streaming API (Alibaba EasyExcel recommended for simplicity or Apache POI SXSSFWorkbook for standard libs).
- Two sheets: "Resumen y Métricas" and "Datos Detallados".
- Always serialize DTOs -> Excel columns; NEVER dump entities.
- Cell content: prefer human-readable descriptions; e.g., `careerName` instead of `careerId`.
- Prevent inclusion of technical columns: exclude fields like `gtin`, `internal_id`, `Unnamed: 0`, surrogate keys.
- Memory: stream rows directly to output stream; flush periodically.

Pseudo-flow of async worker:
1. Load ReportDefinition and validated FilterPayload.
2. Compute KPIs (using same sanitized FilterPayload) and persist `result_metadata` in ReportRun.
3. Stream detailed rows as ReportDetailRowDTO via a JDBC cursor or optimized query; write to sheet 2.
4. Write sheet 1 with filters and KPI rows.
5. Upload file to S3/MinIO and obtain pre-signed URL.
6. Update ReportRun: status=COMPLETED, download_url, finished_at.
7. On failure set status=FAILED with error metadata.

Security: pre-signed URLs expire; set short TTL and require auth when appropriate.

---

## 9. API REST — endpoints, semantics y por qué

### 9.1 Endpoints canónicos (FSD / MOD-DASH)

| Método | Ruta | ID | UC | Roles | Descripción |
|--------|------|-----|-----|-------|-------------|
| GET | `/api/v1/dashboard/coordinator` | API-DASH-01 | FSD-UC-011 | [CC] | Avance por fase + observaciones abiertas de su carrera |
| GET | `/api/v1/dashboard/technician` | API-DASH-02 | FSD-UC-012 | [TD] | Bandeja Indicadores filtrable (`programId`, `phaseId`, `status`) |
| GET | `/api/v1/dashboard/executive` | API-DASH-03 | FSD-UC-013 | [JD] | Semáforo por carrera/facultad (fuera MVP front) |
| POST | `/api/v1/reports/executive/pdf` | API-REP-01 | FSD-UC-014 | [JD] | Job async PDF ejecutivo (202 + jobId) |

### 9.2 Endpoints de agregación (implementación provisional)

| Método | Ruta | Descripción | Notas |
|--------|------|-------------|-------|
| GET | `/api/v1/dashboard/kpis` | KPIs sync vía `FilterPayload` | Reemplazar por API-DASH-01/03 |
| GET | `/api/v1/dashboard/data` | Filas paginadas sync | Reemplazar por API-DASH-02 |
| POST | `/api/v1/reports` | Crear `ReportDefinition` | [CC], [TD] |
| PUT | `/api/v1/reports/{id}` | Actualizar definition | Invalida cache |
| POST | `/api/v1/reports/{id}/export` | Export async Excel | 202 + runId |
| GET | `/api/v1/reports/runs/{runId}` | Estado export + `download_url` | Poll hasta COMPLETED |

### 9.3 Ejemplo — Dashboard coordinador (API-DASH-01)

```bash
curl -s -H "Authorization: Bearer $JWT_CC" \
  "http://localhost:8080/api/v1/dashboard/coordinator"
```

```json
{
  "programId": 42,
  "phases": [
    { "phaseId": 1, "name": "Autoevaluación", "progressPct": 75, "status": "EN_PROCESO" }
  ],
  "openObservations": [
    { "observationId": 101, "indicatorCode": "IND-3.2", "dueDate": "2026-07-01", "daysRemaining": 8 }
  ]
}
```

### 9.4 Semántica request/response
- POST export: body = FilterPayload (client filters). Server will mutate via SecurityInjector and persist mutated `params` in ReportRun for audit.
- Responses include clear guidance on next steps (e.g., poll `/reports/runs/{runId}`).

Errors:
- 422 if filters invalid (e.g., careerId not in catalog)
- 403 if actor unauthorized for requested scope
- 202 accepted for accepted exports

---

## 10. Manejo de Errores, Observabilidad y Retries
- Retries: export worker retries transient DB/storage errors with exponential backoff (max 3 attempts).
- Observability: emit events/metrics for queue length, export durations, failure rates.
- Failure handling: persist `error` field in ReportRun.result_metadata; return 500 on unexpected errors for sync endpoints.

---

## 11. Estructura de Paquetes (explicación)
```
com.umss.sigesa.reports
├── domain         -- JPA entities (ReportDefinition, ReportRun)
├── repository     -- Spring Data repositories
├── service        -- DashboardService (sync) and ReportService (async)
├── service.impl   -- Implementations and async worker
├── web
│   ├── controller -- DashboardController (sync) and ReportController (async)
│   └── dto        -- DTOs and FilterPayload
└── config         -- SecurityInjector, Async config (VirtualThreads executor)
```
Rationale: separación clara entre sync and async simplifies testing and prevents accidental creation of runs by UI reads.

---

## 12. Pruebas

### 12.1 Cobertura por capa

| Capa | Enfoque | Cobertura objetivo | Artefacto test |
|------|---------|-------------------|----------------|
| `DashboardServiceImpl` | Unit tests Mockito | ≥ 90 % (JaCoCo) | `DashboardServiceImplTest` |
| `ReportServiceImpl` | Unit + worker mock | ≥ 90 % (JaCoCo) | `ReportServiceImplTest` (pendiente) |
| `DashboardController` | `@WebMvcTest` | Endpoints + RBAC | `DashboardControllerTest` |
| `ReportRunRepository` | `@DataJpaTest` | JSONB + status enum | `ReportRunRepositoryTest` (pendiente) |
| `SecurityInjector` | Unit | 100 % ramas RBAC | `SecurityInjectorTest` (pendiente) |

Regla de proyecto: `agents.md` y `AGENTS.md` exigen ≥ 90 % en capa servicio. Configurar JaCoCo en `pom.xml` para `DashboardServiceImpl` (análogo a `FaseServiceImpl`).

### 12.2 Casos críticos y trazabilidad Gherkin

| ID | Escenario | Capa | FSD-UC | TC |
|----|-----------|------|--------|-----|
| T-DASH-01 | [CC] obtiene KPIs solo de su carrera | Service + SecurityInjector | UC-011 | TC-09a |
| T-DASH-02 | [CC] intenta filtrar carrera ajena → 403 | Controller | UC-011 | TC-09a |
| T-DASH-03 | [TD] bandeja filtrada por fase y estado | Service | UC-012 | TC-09b |
| T-DASH-04 | Observaciones abiertas ordenadas por plazo | Service | UC-011 | TC-09c |
| T-DASH-05 | [JD] semáforo Rojo/Amarillo/Verde correcto | Service | UC-013 | TC-09 |
| T-DASH-06 | KPIs cacheados respetan TTL Caffeine | Service | UC-011 | NFR-001 |
| T-DASH-07 | Paginación `/data` respeta page/size max | Controller | UC-012 | — |
| T-DASH-08 | Filtro inválido (careerId inexistente) → 422 | Controller | UC-011 | Sad Path |
| T-RPT-01 | Export crea ReportRun PROCESSING → COMPLETED | Service + worker | UC-014 | TC-11 |
| T-RPT-02 | Export no incluye columnas técnicas | Worker | UC-014 | TC-11 |
| T-RPT-03 | Worker falla 3 veces → status FAILED | Service | UC-014 | Sad Path |
| T-RPT-04 | Solo [JD] puede POST executive/pdf → 403 otros roles | Controller | UC-014 | FSD-BR-14 |

### 12.3 Sad Paths obligatorios

- SecurityInjector: actor [CC] con `careerIds` manipulados en query → filtros sobrescritos o 403.
- Export worker: fallo S3 transitorio → reintento exponencial (max 3).
- Dashboard timeout: consulta > 3 s → métrica + posible 504 (NFR-001).

---

## 13. Supuestos y limitaciones
- JWT contains role and optionally bound career/faculty claims for CC users.
- Evidence versioning exists and is referenced; reports reference evidence_version_id when needed.
- H2 in dev stores JSON as TEXT; integration tests should use Postgres (testcontainers) to validate JSONB mappings.

---

## 14. Próximos pasos (implementación inmediata)

1. Promover ADR-0015 y PC-MOD-DASH-01 en catálogo `docs/06_prompt_contracts/`.
2. Alinear `DashboardController` a rutas API-DASH-01/02/03 (delegar a servicio existente).
3. Add hibernate-types dependency and configure `@Type` mapping.
4. Implement FilterPayload POJO and SecurityInjector Aspect (tests T-DASH-01/02).
5. Implement DashboardService (sync) first; add Caffeine cache for KPIs.
6. Implement ReportService.submitExport + Virtual Thread worker; S3 upload (ADR-0013).
7. Write unit + integration tests; JaCoCo ≥ 90 % en `DashboardServiceImpl`.
8. Actualizar submodule pointer en repo padre tras demo integrante.

---

## Quick Start (developer steps)
1. Add dependency: `com.vladmihalcea:hibernate-types-52` and configure JsonType.
2. Implement entities ReportDefinition and ReportRun with JSONB mapping.
3. Implement SecurityInjector to mutate FilterPayload from JWT claims.
4. Create DashboardController endpoints (GET /dashboard/kpis, /dashboard/data).
5. Create ReportController POST /reports/{id}/export and async worker using Virtual Threads.

---

*Este documento respeta la plantilla de `base_design_system.md`, traza FSD-UC-011–014 y documenta ADR-0015 (sync/async). Versión 1.2 — 2026-06-23.*

