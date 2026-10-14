---
id: FSD-UC-028
nombre: Compuertas de avance (gate preview)
estado: Hecho (M8)
release: v2.1
actor_principal: "[TD]"
trazabilidad_prd: PRD-US-025
modulo: MOD-WORKFLOW
reglas: FSD-BR-25.8, FSD-BR-28.1
ultima_actualizacion: "2026-10-13"
design_doc: DD-UC-029
pr_impl: PR-IMPL-M8-029
nota_implementacion: "M6: preview E1/E2. M8: E2 operativa (encuestas API) + evaluación SYSTEMATIZATION (FODA 100% + entregables E3)."
---

# FSD-UC-028 — Compuertas de avance (M8)

Extiende el preview M6 (`GET …/stages/{stageId}/gate`) y el bloqueo al aprobar etapa (`409 STAGE_GATE_BLOCKED`).

Contrato compartido: [FSD-UC-025](FSD-UC-025.md) · [FSD-UC-029](FSD-UC-029.md) · [`DD-UC-029`](../../design/DD-UC-029.md).

## Reglas M8

| Transición | Condición |
|------------|-----------|
| E2 → E3 (`COLLECTION`) | 100% indicadores con ≥1 evidencia; ≥1 `PrimarySurveyBatch`; entregables E2 `APPROVED` |
| Cerrar E3 (`SYSTEMATIZATION`) | 100% `IndicatorAnalysis` completos; entregables E3 (`FODA_FORMS_COMPLETE`, `SYNTHESIS_MATRIX`) `APPROVED` |

E1 permanece como en M6. Etapas E4–E7 siguen “compuerta no configurada” (PASS informativo) hasta M9–M11.
