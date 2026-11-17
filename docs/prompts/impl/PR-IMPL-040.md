---
id: PR-IMPL-040
feature_asociado: DD-UC-011
fsd_uc:
  - FSD-UC-011
  - FSD-UC-014
fecha: "2026-10-17"
version: "1.0"
estado: Aprobado
autor: "Boris Anthony Angulo Urquieta"
github_issue: "https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss/issues/50"
release: "8.1.0"
---

# Prompt Contract — Implementación `PR-IMPL-040`

> **Issue:** [#50 FEAT-01](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss/issues/50) — Desacoplamiento de I/O en reportes y tuning de pool Hikari.  
> **Design docs:** [`DD-UC-011`](../../design/DD-UC-011.md) · [`DD-UC-014`](../../design/DD-UC-014.md)  
> **FSD:** [`FSD-UC-011`](../../product/uc/FSD-UC-011.md) · [`FSD-UC-014`](../../product/uc/FSD-UC-014.md)  
> **Auditoría:** CON-03 en `docs/auditoria/concurrencia-boris.md` (veredicto histórico `FALLA`; esta pieza es remediación 8.1.0).  
> **Rama:** `feat/50-hikari-report-io`

---

## 1. Propósito y Objetivo

Corregir retención de conexiones Hikari durante generación/escritura de reportes (PDF/XLSX/CSV) y declarar el pool de forma explícita.

- Extraer I/O de archivo de métodos `@Transactional` de larga duración.
- Configurar HikariCP en YAML (tamaño, timeout, leak detection) alineado a \(N=1\), pool 10, acquire 30 s.
- Verificar con tests que la persistencia del job ocurre en TX cortas y el archivo se genera fuera de TX write.

---

## 2. Rol y Persona

Desarrollador Backend Senior SIGESA — Java 21, Spring Boot 4.x, hexagonal, HikariCP.

---

## 3. Límites de Alcance

### In-Scope

- `ReportExportJobService.processJobAsync` / `getJobFileStream` (UC-011 export async).
- `ProcessReportJobService.process` (UC-014 PDF ejecutivo).
- Puerto `TransactionRunnerPort` + adaptador `SpringTransactionRunnerAdapter`.
- `spring.datasource.hikari` en `application.yaml`, `application-dev.yaml`, `application-prod.yaml`, `application-test.yaml`.
- Tests unitarios + `ReportExportAsyncE2EIT` sin TX de clase que enmascare el commit.
- Docs vivos: FSD/DD/DTP + este contrato + PM en sprint 03.

### Out-of-Scope

- MinIO/S3 y lock de evidencias (FEAT-02 / #51).
- Cliente LLM timeout/async (FEAT-03 / #52).
- Cambio de motor de reportes o plantillas CNACU.
- Mapeo HTTP 503 por pool exhausted (complemento CON-03, no criterio del issue #50).
- `docs/baseline/`.

---

## 4. Restricciones y Reglas

| ID | Regla |
|----|-------|
| R1 | Dominio sin Spring/JPA. La aplicación no usa `@Transactional` envolviendo I/O; usa `TransactionRunnerPort`. |
| R2 | `@Async` en export dashboard se mantiene (no cambia el contrato REST). |
| R3 | Pool explícito: `maximum-pool-size` 10, `connection-timeout` 30000 ms, `leak-detection-threshold` 20000 ms (sobrescribible por env `SIGESA_HIKARI_*`). |
| R4 | El stream JPA de observaciones se consume **dentro** de TX read-only; `generateReport` / `PdfRendererPort.render` / `store` **fuera** de TX write. |
| R5 | JaCoCo del servicio de export no debe caer por el refactor. |
| R6 | Cierre: `@save-prompt-mapping sprint=03 pr=PR-IMPL-040`. |

---

## 5. Criterio de aceptación (issue #50)

- Las conexiones a DB se liberan antes del procesamiento del archivo.
- Tests de integración verifican la persistencia sin retención de conexión durante I/O.

---

## 6. Trazabilidad

`FSD-UC-011` / `FSD-UC-014` → `DD-UC-011` / `DD-UC-014` → `PR-IMPL-040` → GitHub #50 → sprint 03 `PM-019`
