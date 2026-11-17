---
id: FSD-UC-028
nombre: Compuertas de avance (gate preview)
estado: Hecho (M10–M11 E5–E7)
release: v2.1
actor_principal: "[TD]"
trazabilidad_prd: PRD-US-025
modulo: MOD-WORKFLOW
reglas: FSD-BR-25.8, FSD-BR-28.1
ultima_actualizacion: "2026-10-13"
design_doc: DD-UC-031
pr_impl: PR-IMPL-M9-031
nota_implementacion: "M6 E1; M8 E2/E3; M9 DRAFTING; M10 E5/E6 (freeze+checklist); M11 E7 (justificación + IMPROVEMENT_PROGRESS_REPORT)."
---

# FSD-UC-028 — Compuertas de avance

Extiende el preview (`GET …/stages/{stageId}/gate`) y el bloqueo al aprobar etapa (`409 STAGE_GATE_BLOCKED`).

Contrato: [FSD-UC-025](FSD-UC-025.md) · [FSD-UC-029](FSD-UC-029.md) · [FSD-UC-031](FSD-UC-031.md) · [`DD-UC-031`](../../design/DD-UC-031.md).

## Reglas vigentes

| Transición | Condición |
|------------|-----------|
| Cerrar E1 (`PREPARATORY`) | Entregables E1 `APPROVED` |
| E2 → E3 (`COLLECTION`) | 100% indicadores con ≥1 evidencia; ≥1 `PrimarySurveyBatch`; entregables E2 `APPROVED` |
| Cerrar E3 (`SYSTEMATIZATION`) | 100% `IndicatorAnalysis` completos; entregables E3 `APPROVED` |
| E4 → E5 (`DRAFTING`) | Cada entregable E4 tiene `documentAssetId` y está `APPROVED` |
| E5 → E6 (`PRESENTATION`) | Entregables E5 `APPROVED` y freeze `AUDIT_READONLY` |
| E6 → E7 (`EXTERNAL_PREP`) | Freeze `AUDIT_READONLY`; E6 `APPROVED`; checklist Sala de Pares 100% |
| Corte E7 (`POST_ACCREDITATION`) | Ítems vencidos incompletos con `justification`; entregable E7 generado y `APPROVED` |
