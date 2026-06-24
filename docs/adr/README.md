# Registro de decisiones arquitectónicas (ADR) — SIGESA Backend

| Campo | Valor |
|-------|-------|
| **Ubicación** | `docs/adr/` (submódulo `sigesa-backend`) |
| **Complementa** | `docs/baseline/05_dti/adrs/` (DTI congelado M4) |
| **Audiencia** | Design docs, DTP, implementación |

> **Regla:** decisiones tomadas durante la fase de implementación viven aquí. El baseline (`docs/baseline/`) **no se modifica**.

## Índice

| ADR | Título | Estado | Relacionado |
|-----|--------|--------|-------------|
| [ADR-0015](ADR-0015-dashboard-sync-async-reporting.md) | Dashboards síncronos y exportación asíncrona (MOD-DASH/MOD-REPORT) | Aceptado | DD-UC-004 · FSD-UC-011–014 |

## Trazabilidad

| Documento | Uso |
|-----------|-----|
| [`docs/design/DD-UC-004.md`](../design/DD-UC-004.md) | Diseño MOD-DASH/MOD-REPORT |
| [`docs/product/DTP.md`](../product/DTP.md) | Contrato técnico vivo §B.2 |
| [`docs/PROMPT_MAPPING.md`](../PROMPT_MAPPING.md) | Ejecuciones PM-NNN |

## Registro de cambios

| Fecha | Cambio |
|-------|--------|
| 2026-06-23 | ADR-0015 promovido desde `sigesa-docs/docs/adr/`; índice inicial |
