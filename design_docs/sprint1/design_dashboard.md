# Diseño Técnico — Reporting / Dashboard (Backend) — Sprint 1

> **Copia sprint:** ver versión canónica en [`../design_dashboard.md`](../design_dashboard.md) (v1.2, 2026-06-23).

## Resumen Sprint 1

| Ítem | Estado |
|------|--------|
| Documento diseño v1.2 + trazabilidad FSD | ✓ |
| ADR-0015 sync/async | ✓ (`docs/adr/`) |
| PC-MOD-DASH-01 prompt contract | ✓ (`docs/06_prompt_contracts/`) |
| PM-056 prompt mapping | ✓ |
| `DashboardController` `/kpis`, `/data` | ✓ stub |
| Rutas canónicas API-DASH-01/02/03 | Pendiente |
| JaCoCo ≥ 90 % DashboardServiceImpl | Pendiente |
| ReportService async + Excel export | Pendiente |

## Trazabilidad demo

- **FSD-UC-011** Dashboard [CC] → TC-09a, TC-09c  
- **FSD-UC-012** Bandeja [TD] → TC-09b  
- **FSD-UC-013** Semáforo [JD] → TC-09 (v1.1)  
- **FSD-UC-014** Reporte PDF → TC-11  

## Referencias rápidas

- FSD: `sigesa-docs/docs/04_fsd/casos_uso.md`
- API: `sigesa-docs/docs/04_fsd/api_contracts.md` §7
- ADR: `sigesa-docs/docs/adr/ADR-0015-dashboard-sync-async-reporting.md`
- UI AcredIA DS: `sigesa-docs/figma/frames/prototipo/jd-admin-dashboard.md`
- Prompt: `app/sigesa-backend/.cursor/prompts/reporting_dashboard.prompt.md`
