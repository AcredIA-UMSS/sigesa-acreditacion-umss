---
id: PR-IMPL-005
feature_asociado: DD-UC-014
fsd_uc:
  - FSD-UC-014
fecha: "2026-06-26"
version: "1.0"
estado: Aprobado
autor: "AI Prompt Architect (@sigesa-prompt-contract-architect)"
skill_origen: sigesa-prompt-contract-architect
---

# Prompt Contract — Implementación `PR-IMPL-005`

> **Design doc fuente:** [`DD-UC-014`](../../design/DD-UC-014.md) · **FSD:** FSD-UC-014 · **Regla:** FSD-BR-14.

---

## 1. Propósito y Objetivo

Generar el código Java del módulo **MOD-REPORT** (reporte ejecutivo PDF asíncrono) según [`DD-UC-014`](../../design/DD-UC-014.md):

- **FSD-UC-014:** [JD] genera PDF con filtros; P95 ≤ 5 min; job asíncrono.
- **FSD-BR-14:** solo `[JD]` en `/reports/**`.

---

## 2. Rol y Persona

Desarrollador Backend Senior SIGESA — Java 21, Spring Boot 4.x, hexagonal, OpenPDF.

---

## 3. Límites de Alcance

### In-Scope

- Dominio: `ReportJob`, `ReportJobStatus`, `ExecutiveReportFilters`, `ExecutiveReportSnapshot`, excepciones.
- Casos de uso: `GenerateExecutiveReportUseCase`, `GetReportJobStatusUseCase`, `ProcessReportJobUseCase`, `DownloadReportArtifactUseCase`.
- Puertos out: `ReportJobRepositoryPort`, `ExecutiveDataPort`, `PdfRendererPort`, `ReportArtifactStoragePort`.
- Adaptadores: `ReportController`, JPA `report_job`, `OpenPdfRendererAdapter`, `LocalFileReportArtifactStorageAdapter`, `ReportJobAsyncDispatcher`.
- API: `POST/GET /api/v1/reports/executive/pdf`, `GET .../{jobId}/download`.
- Dependencia Maven: `com.github.librepdf:openpdf:2.0.3`.
- Tests unitarios + WebMvc mínimo.
- `ReportModuleConfig` (wiring `@Bean`, no `@Service` en dominio).

### Out-of-Scope

- Dashboard UC-013 (datos reales de semáforo — usar `ExecutiveDataStubAdapter` v1.0).
- SMTP al superar 5 min (UC-015).
- S3 / cola externa SQS.
- Frontend (skill `@generate-frontend-feature` separado).
- Modificar `docs/baseline/`.

---

## 4. Restricciones y Reglas

| ID | Regla |
|----|-------|
| R1 | Dominio/casos de uso sin Spring/JPA. |
| R2 | DTOs `record` en controladores. |
| R3 | `SecurityConfig`: `/api/v1/reports/**` → `hasRole("JD")`. |
| R4 | POST generación → **202** `{ jobId }`. |
| R5 | PDF con timestamp, filtros y marca «Universidad Mayor de San Simón — SIGESA». |
| R6 | JaCoCo ≥ 90% en `*Report*Service` de aplicación. |

---

## 5. Entrada

Documentos: `DD-UC-014`, `FSD-UC-014`, `docs/product/api_contracts.md` API-REP-01.

---

## 6. Salida

Paquetes bajo `com.umss.sigesa` según DD-UC-014 §2–§7. Compilación `mvn verify` exitosa.

---

## 7. Anti-patrones

- ❌ Exponer `@Entity` en controladores.
- ❌ Permitir `[CC]`/`[TD]` en reportes.
- ❌ Generación síncrona bloqueante en request thread.
- ❌ `fetch` manual en frontend.
