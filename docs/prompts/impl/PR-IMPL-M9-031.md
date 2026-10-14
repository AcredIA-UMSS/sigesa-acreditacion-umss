---
id: PR-IMPL-M9-031
feature_asociado: DD-UC-031
fsd_uc:
  - "FSD-UC-031"
  - "FSD-UC-028"
fecha: "2026-10-13"
version: "1.0"
estado: Listo
autor: "Cursor Agent (M9 Plan Mejoras / E4)"
skill_origen: sigesa-orchestrator
alcance: full-stack
depende_de:
  - "PR-IMPL-M8-029"
bloquea_a:
  - "M11 post-acreditación"
---

# Prompt Contract — Implementación `PR-IMPL-M9-031`

> **Design doc:** [`DD-UC-031`](../../design/DD-UC-031.md)  
> **FSD:** [UC-031](../../product/uc/FSD-UC-031.md), [UC-028](../../product/uc/FSD-UC-028.md)  
> **ADR:** [ADR-0005](../../adr/ADR-0005-workflow-metodologico-evaluacion-transversal.md)  
> **Milestone:** M9 (release 2.1.0)

---

## 1. Propósito y Objetivo

Implementar **Plan de Mejoras** e **informes E4** (borrador SIGESA, no plantilla CNACU):

- Flyway **V21** `improvement_plan_items` + backfill entregables E4.
- CRUD + import FODA `WEAKNESS`.
- Generate PDF → `DocumentAsset` ligado a `StageDeliverable`.
- `StageGateEvaluator.evaluateDrafting`.
- UI plan + generar en timeline.
- Tests ≥90 % servicios nuevos.

**No** usar Flyway ≤ V20. Listar `V*.sql` antes de escribir.

## 2. Rol

Implementador hexagonal SIGESA (Java 21 / Spring Boot 4 / React 19). Dominio puro; DTOs web; Orval only; tokens Tailwind institucionales.

## 3. Límites de Alcance

### In-Scope

Dominio `ImprovementPlanItem`; puertos/servicios list/create/update/import/generate; OpenPDF vía puerto nuevo (no reusar snapshot ejecutivo); mapper `documentAssetId`; bootstrap E4; gate DRAFTING; REST API-WF-13…17; FE; `api_contracts.md` + DTP.

### Out-of-Scope

M10 freeze/[EE]; M11 ciclo E7; CAE; plantillas CNACU; SMTP/bitácora.

## 4. Restricciones

| ID | Regla |
|----|-------|
| R1 | Dominio sin Spring/JPA. |
| R2 | UNIQUE `(process_id, source_indicator_id)` parcial. |
| R3 | Import solo `WEAKNESS`; idempotente. |
| R4 | `progress` 0–100; `objective` no vacío. |
| R5 | Generate solo etapa `DRAFTING` y código E4; 409 si APPROVED. |
| R6 | Gate E4: 4 códigos con asset + APPROVED. |
| R7 | Aprobar E4 sin asset → 409 `DELIVERABLE_WITHOUT_DOCUMENT`. |
| R8 | PDF con leyenda de borrador institucional. |
| R9 | Frontend solo Orval. |
| R10 | `@dtp-sync` + `@save-prompt-mapping fsd=FSD-UC-031` → **PM-017**. |

## 5. Contratos de salida

Ver DD-UC-031 § API.

## 6. Criterios de aceptación

- [ ] V21; procesos existentes reciben E4 via ensure.
- [ ] Import/CRUD plan.
- [ ] Generate 201 + folio; GET stages muestra `documentAssetId`.
- [ ] Gate DRAFTING BLOCK/PASS según reglas.
- [ ] UI CC: plan + generar; TD ve listado.
- [ ] `./mvnw test` y Vitest verdes.
- [ ] Paso 3c smoke + Paso 5 PM-017.

## 7. No tocar

`docs/baseline/**`. M6–M8 solo como dependencia (FODA, DocumentAsset, bootstrap ensure).
