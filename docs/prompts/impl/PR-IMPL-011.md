---
id: PR-IMPL-011
feature_asociado: DD-UC-011
fsd_uc:
  - "FSD-UC-011"
fecha: "2026-06-28"
version: "1.0"
estado: Aprobado
autor: "AI Prompt Architect (@sigesa-prompt-contract-architect)"
skill_origen: sigesa-prompt-contract-architect
---

# Prompt Contract — Implementación `PR-IMPL-011`

> **Generado vía** `@sigesa-prompt-contract-architect`.  
> **Design doc fuente:** [`DD-UC-011`](../../design/DD-UC-011.md) · **FSD:** [`FSD-UC-011`](../../product/uc/FSD-UC-011.md) · **Plan de Pruebas:** `implementation_plan.md`.

---

## 1. Propósito y Objetivo

Generar e implementar el código Java y las migraciones DDL para el **Dashboard Híbrido y Exportación Asíncrona de Reportes** (`MOD-DASH` / `MOD-REPORT`) respondiendo strictly a [`DD-UC-011`](../../design/DD-UC-011.md):

- **Modelo de Datos de Alto Rendimiento (+1M Registros):** Creación de la tabla de agregados pre-calculados (`tb_program_dashboard_summary`), particionamiento de tablas operacionales e índice parcial cubriente (`idx_obs_program_estado_deadline`) para responder consultas en `< 150ms`.
- **Exportación Asíncrona ("Promise of Return"):** Implementación de la arquitectura de trabajos en segundo plano (`tb_report_export_job`), endpoints de encolamiento (`POST /export-jobs`), sondeo de estado (`GET /export-jobs/{jobId}`) y descarga binaria en streaming (`SXSSFWorkbook` / `FastCSV`).
- **Suite Completa de Pruebas Backend:** Cobertura exhaustiva con pruebas Unitarias, de Regresión (PBAC), de Carga/Estres (Testcontainers con +1,000,000 registros) y E2E de integración.

---

## 2. Rol y Persona

- **Identidad:** Desarrollador Backend Senior experto en SIGESA.
- **Expertise:** Java 21 (Virtual Threads), Spring Boot 4.x, Arquitectura Hexagonal, Spring Security (PBAC/JWT), PostgreSQL 16+ (particionamiento e índices parciales), Apache POI SXSSF, Flyway, Testcontainers y JUnit 5.

---

## 3. Límites de Alcance

### In-Scope

- **Migraciones DDL (Flyway):** Creación de `tb_program_dashboard_summary`, `tb_program_phase_summary`, `tb_report_export_job` y el índice parcial `idx_obs_program_estado_deadline`.
- **Capa de Dominio:** Entidades de dominio y agregados `ReportExportJob`, `JobStatus`, `ReportFormat`, y excepciones del dominio (`JobNotFoundException`, `InvalidJobStateException`).
- **Puertos e Interfaces (Application Ports):**
  - Ports IN: `ExportReportJobUseCase`, `GetReportJobStatusUseCase`, `GetCompositeDashboardSummaryUseCase`.
  - Ports OUT: `ReportExportJobRepositoryPort`, `DashboardQueryPort`, `ReportGeneratorPort`.
- **Servicios de Aplicación:** `ReportExportJobService` (procesamiento `@Async` en segundo plano), `DashboardSummaryAggregationService` (actualización incremental de métricas).
- **Adaptadores de Entrada (Web REST):** `DashboardCompositeController` extendido con `/export-jobs`, `/export-jobs/{jobId}`, `/export-jobs/{jobId}/download`.
- **Adaptadores de Salida (Infraestructura):** `JpaReportExportJobAdapter`, `ApachePoiReportStreamingAdapter` (SXSSF streaming binario), `FastCsvReportStreamingAdapter`.
- **Suite de Pruebas Backend:**
  - Unit: `ReportExportJobServiceTest`, `DashboardSummaryAggregationServiceTest`, `ApachePoiReportStreamingAdapterTest`.
  - Regresión: `DashboardCompositeControllerTest` (validación PBAC / `FSD-BR-09`).
  - Rendimiento/Carga: `Dashboard1MPerformanceTest` usando Testcontainers PostgreSQL con 1,000,000+ filas simuladas.
  - E2E Integration: `ReportExportAsyncE2EIT`.

### Out-of-Scope

- Componentes visuales Frontend (pantallas React `/coordinator/dashboard`).
- Integración con brokers de mensajería externos distribuidos como RabbitMQ o Apache Kafka (v1.0 utiliza procesamiento asíncrono en memoria/Spring `@Async` con persistencia en DB).
- Push en tiempo real por WebSockets (v1.0 utiliza sondeo REST `GET /export-jobs/{jobId}`).
- Modificaciones a specs baseline en `docs/baseline/`.

---

## 4. Restricciones y Reglas

| ID | Regla |
|---|---|
| R1 | Las capas de Dominio y Casos de Uso **no deben contener** anotaciones ni dependencias de Spring, JPA o Hibernate. |
| R2 | Los controladores REST exponen exclusivamente DTOs de tipo `record`; **nunca** exponen entidades `@Entity`. |
| R3 | La generación de reportes masivos debe emplear **streaming estricto** (`SXSSFWorkbook` / `FastCSV`) sin cargar la totalidad de las 1M filas en memoria Heap (RAM < 512MB durante la exportación). |
| R4 | Aislamiento multitenant estricto por `program_id` en todas las consultas SQL (`FSD-BR-09`). Prohibida la fuga cross-carrera. |
| R5 | Cobertura de código JaCoCo ≥ 90% en `ReportExportJobService` y `DashboardSummaryAggregationService`. |
| R6 | Flujo post-implementación obligatorio: `@save-prompt-mapping PR-IMPL-011` → `@dtp-sync`. |

---

## 5. Especificaciones de Entrada

| Documento | Uso |
|---|---|
| `docs/design/DD-UC-011.md` | Contratos API, arquitectura híbrida PBAC, diseño técnico |
| `docs/product/uc/FSD-UC-011.md` | Gherkin, reglas de negocio FSD-BR-09 |
| `implementation_plan.md` | Plan detallado de pruebas Unit, Regression, Performance (+1M) y E2E |
| `docs/product/api_contracts.md` | Especificaciones OpenAPI/REST |

### DTOs y Contratos JSON

```json
// POST /api/v1/dashboards/coordinator/export-jobs
// Request Body
{
  "format": "xlsx",
  "faseId": 1
}

// Response (202 Accepted)
{
  "jobId": "e2a1b94c-8821-4b12-9c10-09281a8b191a",
  "status": "PROCESSING",
  "message": "La generación del reporte ha comenzado en segundo plano.",
  "statusUrl": "/api/v1/dashboards/export-jobs/e2a1b94c-8821-4b12-9c10-09281a8b191a"
}
```

---

## 6. Especificaciones de Salida

**Estructura de Paquetes (`com.umss.sigesa`):**

```
com.umss.sigesa.domain.model          → ReportExportJob, JobStatus, ReportFormat
com.umss.sigesa.domain.exception      → JobNotFoundException, InvalidJobStateException
com.umss.sigesa.application.port.in   → ExportReportJobUseCase, GetReportJobStatusUseCase
com.umss.sigesa.application.port.out  → ReportExportJobRepositoryPort, ReportGeneratorPort
com.umss.sigesa.application.service.report → ReportExportJobService
com.umss.sigesa.adapter.in.web        → DashboardCompositeController, ExportJobRequest, ExportJobResponse
com.umss.sigesa.adapter.out.persistance → JpaReportExportJobAdapter, ReportExportJobEntity
com.umss.sigesa.adapter.out.report    → ApachePoiReportStreamingAdapter, FastCsvReportStreamingAdapter
src/test/java/com/umss/sigesa/...   → Pruebas Unit, Regression, Performance y E2E
```

| Escenario HTTP | Código | Respuesta / Headers |
|---|---|---|
| Solicitud de Exportación Encolada | 202 Accepted | `{ jobId, status: "PROCESSING", statusUrl }` |
| Estado de Trabajo Completado | 200 OK | `{ jobId, status: "COMPLETED", progressPercentage: 100, downloadUrl }` |
| Descarga de Archivo Generado | 200 OK | `Content-Disposition: attachment; filename="..."` (Binary Stream) |
| Intento de Descarga en Proceso/Fallido | 409 Conflict | `{ "error": "JOB_NOT_READY", "message": "El reporte aún no se ha generado." }` |
| Intento de Acceso Cross-Carrera | 403 Forbidden | `{ "error": "ACCESS_DENIED", "message": "No tiene permisos sobre este programa." }` |

---

## 7. Anti-patrones y Violaciones

- ❌ Utilizar `@Service` o anotaciones de Spring/JPA dentro de las clases de Dominio o Casos de Uso.
- ❌ Ejecutar consultas síncronas pesadas en la ruta de petición HTTP sin utilizar la cola de trabajos en segundo plano (`tb_report_export_job`).
- ❌ Cargar colecciones masivas en memoria usando `findAll()` de Spring Data JPA.
- ❌ Generar archivos Excel con `XSSFWorkbook` estándar en lugar del adaptador en streaming `SXSSFWorkbook`.
- ❌ Omitir la validación de permisos PBAC y el filtrado por `program_id` en las descargas asíncronas.

---

## 8. Checklist de Validación del Contrato

- [x] Enlazado a [`DD-UC-011`](../../design/DD-UC-011.md) y [`FSD-UC-011`](../../product/uc/FSD-UC-011.md).
- [x] Alcance In/Out, reglas R1–R6 y arquitectura de 1M+ registros definidos.
- [x] Contrato registrado como **`PR-IMPL-011.md`** en `docs/prompts/impl/`.
