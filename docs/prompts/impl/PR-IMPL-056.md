---
id: PR-IMPL-056
feature_asociado: DD-UC-039
fsd_uc:
  - FSD-UC-039
fecha: "2026-11-15"
version: "1.1"
estado: Implementado
---

# PR-IMPL-056 — Matriz de cobertura

Implementar FSD-UC-039 / DD-UC-039: JSON + PDF síncrono, alcance via `EvaluationAccessGuard`, UI `/procesos/{id}/cobertura`, Gherkin `@TC-COVERAGE`. No ZIP. No `docs/baseline/`.

## Cierre
- Endpoints `GET /coverage-matrix` y `GET /coverage-matrix/pdf`, servicio/puertos/adaptadores y renderer OpenPDF implementados.
- UI de matriz y acceso desde detalle de proceso.
- Cobertura de pruebas: ensamblado de dominio, servicio (incl. proceso vacío y alcance CC), renderer PDF, IT Gherkin `@TC-COVERAGE` y tests de vista frontend.
