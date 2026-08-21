---
id: DD-UC-009
titulo: "Aprobar Indicador"
producto: "SIGESA / AcredIA"
grupo: "G2"
fsd_uc:
  - "FSD-UC-009"
prd_refs:
  - "PRD-REQ-009"
prompts:
  - "PR-IMPL-009"
release: "v1.0"
status: borrador
fecha: "19/08/2026"
autores:
  - "Tech Lead"
---

# Design Doc DD-UC-009 — Aprobar Indicador

## 1. Objetivo y contexto
- **Qué resuelve este feature**: Permite a un Director Técnico [TD] aprobar un indicador en estado `SUBIDO` o `SUBSANADO`.
- **Caso(s) de uso del FSD**: `FSD-UC-009` (Aprobar Indicador), [Ver detalle](docs/product/uc/FSD-UC-009.md).

## 2. Diseño
- **Endpoint**: `POST /api/v1/indicators/{indicatorId}/approve`
- **Unidad de Evaluación**: La aprobación se aplica al **Indicador completo** tras verificar que todo el conjunto de evidencias presentadas cumple con los criterios requeridos.
- **Enrutamiento Híbrido de Filtrado (`filter_indicators`)**:
  - Soporta la localización rápida de indicadores pendientes mediante 4 escenarios (SQL directo, AI Tool Calling con AI Toggle = ON, Out-of-Scope, e AI Toggle = OFF con fallback `null`).
- **Lógica**:
  1. Validar rol TD o JD.
  2. Verificar estado actual (SUBIDO, SUBSANADO o PENDIENTE).
  3. Resolver todas las observaciones previas del indicador (cambiar estado a `RESOLVED`).
  4. Insertar transición a `APROBADO` en `indicator_state_history`.
  5. Publicar evento `IndicatorApproved` / encolar notificación.

