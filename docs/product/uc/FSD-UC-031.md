---
id: FSD-UC-031
nombre: Plan de Mejoras y documentos E4
estado: Hecho (M9+M11)
release: v2.1
actor_principal: "[CC], [TD]"
trazabilidad_prd: PRD-US-025
modulo: MOD-EVALUATION
reglas: FSD-BR-31.1, FSD-BR-31.2, FSD-BR-31.3
ultima_actualizacion: "2026-10-13"
design_doc: DD-UC-031
pr_impl: PR-IMPL-M9-031
nota_implementacion: "M9: plan + E4. M11: justification/avance en monitoreo + generate E7 + gate POST_ACCREDITATION (DD-UC-030)."
---

# FSD-UC-031 — Plan de Mejoras y documentos E4

## Contexto

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | [ADR-0005](../../adr/ADR-0005-workflow-metodologico-evaluacion-transversal.md) · [FSD §8–§10](../FSD.md) |
| **Design Doc** | [`DD-UC-031`](../../design/DD-UC-031.md) |
| **Relacionados** | FSD-UC-028 (compuerta E4→E5) · FSD-UC-029 (FODA origen) · FSD-UC-025 (timeline) |
| **Precondiciones** | Proceso `ACTIVE`; usuario autenticado CC o TD (lectura también JD) |
| **Pantalla** | `/procesos/{processId}` — sección Plan de Mejoras + timeline etapa `DRAFTING` |

M9 cubre la **elaboración de documentos clave (E4)** y el **Plan de Mejoras** alimentado desde debilidades FODA. No usa plantillas oficiales CNACU/CEUB: los PDF son **borradores institucionales SIGESA**.

## Flujo — Plan de Mejoras

1. [CC] abre el proceso y consulta el plan.
2. Puede **importar desde FODA**: cada `IndicatorAnalysis` con `fodaQuadrant = WEAKNESS` sin ítem previo genera una fila (`sourceIndicatorId` único).
3. Puede **crear** ítems manuales (`sourceIndicatorId` nulo) y **editar** objetivo, actividad, responsable, plazo y avance (0–100).
4. [TD]/[JD] consultan el mismo listado (solo lectura).

## Flujo — Generar entregable E4

1. [CC] en etapa `DRAFTING` pulsa generar en un entregable E4.
2. El sistema produce un PDF borrador, lo guarda como `DocumentAsset` y lo asocia a `StageDeliverable.documentAssetId`.
3. [TD] aprueba el entregable (API-WF-08) cuando el asset existe.
4. Cerrar E4 (pasar a E5) exige los cuatro entregables **generados y APPROVED**.

Catálogo E4: `SELF_ASSESSMENT_REPORT`, `OFFICIAL_DATA_FORM`, `IMPROVEMENT_PLAN`, `ACADEMIC_DEV_PLAN`.

## Excepciones

| ID | Condición | Respuesta |
|----|-----------|-----------|
| A1 | Proceso inexistente o fuera de alcance [CC] | `404 PROCESS_NOT_FOUND` |
| A2 | Ítem inexistente | `404 IMPROVEMENT_ITEM_NOT_FOUND` |
| A3 | Importación sin nuevas debilidades | `200` con `importedCount: 0` (idempotente) |
| A4 | PUT con `progress` fuera 0–100 u objetivo vacío | `400 VALIDATION_ERROR` |
| A5 | Generar E4 fuera de `DRAFTING` o código no-E4 | `409 INVALID_STAGE_STATE` |
| A6 | Regenerar entregable ya `APPROVED` | `409 DELIVERABLE_IMMUTABLE` |
| A7 | Aprobar E4 sin `documentAssetId` | `409 STAGE_GATE_BLOCKED` / entregable sin documento |
| A8 | Rol no autorizado a escribir | `403` |

## Postcondiciones

- `improvement_plan_items` persistidos; UNIQUE `(process_id, source_indicator_id)` cuando `source_indicator_id` no es nulo.
- Entregables E4 con asset y folio; compuerta DRAFTING evaluable.

## API (resumen)

| Método | Ruta | Rol |
|--------|------|-----|
| GET | `/api/v1/processes/{processId}/improvement-plan` | CC, TD, JD |
| POST | `/api/v1/processes/{processId}/improvement-plan` | CC |
| POST | `/api/v1/processes/{processId}/improvement-plan/import-from-foda` | CC |
| PUT | `/api/v1/processes/{processId}/improvement-plan/{itemId}` | CC |
| POST | `/api/v1/processes/{processId}/stages/{stageId}/deliverables/{deliverableId}/generate` | CC |

## Fuera de alcance (M9)

- Ciclo post-acreditación E7 / `IMPROVEMENT_MONITORING` (M11).
- Plantillas oficiales CNACU/CEUB.
- Congelamiento / Sala de Pares (M10).
- `CAE_MEMBER`.

## Escenarios Gherkin

```gherkin
# language: es
Escenario: Importar debilidades FODA
  Dado 2 análisis WEAKNESS sin ítem
  Cuando [CC] importa desde FODA
  Entonces importedCount es 2 y el listado muestra 2 filas

Escenario: Compuerta E4 bloqueada sin PDF
  Dado entregables E4 PENDING sin documentAssetId
  Cuando [TD] previsualiza la compuerta DRAFTING
  Entonces pass es false
```
