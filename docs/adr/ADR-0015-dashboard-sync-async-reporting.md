# ADR-0015: Dashboards síncronos y exportación asíncrona (MOD-DASH / MOD-REPORT)

| Campo | Valor |
|-------|-------|
| Estado | **Aceptado** |
| Fecha | 2026-06-23 |
| Autor | alexAlvarez |
| Alcance | MOD-DASH · MOD-REPORT · `com.umss.sigesa.reports` |
| Design doc | [DD-UC-004](../design/DD-UC-004.md) |
| Relacionado | Baseline ADR_007 (JWT/RBAC) · ADR_013 (S3 blobs) · FSD-UC-011–014 |

## Contexto

SIGESA expone paneles operativos para [CC], [TD] y [JD] (FSD-UC-011–013) y reportes ejecutivos (FSD-UC-014). Los dashboards requieren baja latencia (NFR-001: p95 < 3 s); las exportaciones Excel/PDF pueden ser pesadas y no deben bloquear el hilo HTTP.

Restricciones: FSD-BR-09 (aislamiento carrera), FSD-BR-14 (reporte solo [JD]), Evidence append-only (baseline ADR_001).

## Decisión

Arquitectura **híbrida sync/async** en `com.umss.sigesa.reports`:

1. **Sync (dashboard):** lecturas HTTP sin crear `ReportRun`; cache Caffeine TTL ≤ 5 min; `SecurityInjector` muta `FilterPayload` según JWT (MOD-AUTH).
2. **Async (reports):** `POST /reports/{id}/export` → `ReportRun` PROCESSING → worker Virtual Thread → Excel streaming → S3/MinIO pre-signed URL.
3. **Persistencia:** `report_definition` (catálogo versionado) + `report_run` (historial append-only de ejecuciones).
4. **Rutas canónicas:** `/dashboard/coordinator`, `/technician`, `/executive`; rutas provisionales `/kpis`, `/data` hasta alineación front.

## Consecuencias

**Positivas:** UI responsiva; auditoría por `report_run`; reutiliza almacenamiento blob; JaCoCo ≥ 90 % en servicios.

**Negativas:** dos superficies API; cache KPIs hasta 5 min desactualizado; drift temporal de rutas MVP.

## Validación

- [CC] no ve carrera ajena (403 o filtro).
- p95 dashboard < 3 s (NFR-001).
- Export 202 → poll COMPLETED con URL válida.
- Sin columnas técnicas en Excel.

## Referencias

- [`docs/product/uc/FSD-UC-011.md`](../product/uc/FSD-UC-011.md) … [`FSD-UC-014.md`](../product/uc/FSD-UC-014.md)
- [`docs/product/api_contracts.md`](../product/api_contracts.md) §MOD-DASH
- [`docs/design/DD-UC-004.md`](../design/DD-UC-004.md)
- [`docs/prompts/impl/PR-IMPL-005.md`](../prompts/impl/PR-IMPL-005.md)
