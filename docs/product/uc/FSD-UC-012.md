---
id: FSD-UC-012
nombre: Bandeja auditoría [TD]
estado: En Curso
release: v1.0
actor_principal: "[TD]"
trazabilidad_prd: PRD-US-014
modulo: MOD-DASH
reglas: —
ultima_actualizacion: "2026-08-27"
---

# FSD-UC-012 — Bandeja auditoría [TD]

## Contexto

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-012 · PRD-US-014 |
| **Pantalla** | `/technician/inbox` |
| **API** | `GET /api/v1/dashboards/me/summary` (sección `technicianSection`) |
| **Nota implementación viva (2026-08-27)** | Sección TD en `/dashboard` con KPIs y últimas evaluaciones. **Pendiente:** ruta `/technician/inbox`, filtros avanzados y acciones UC-008/UC-009. |

## Flujo principal

1. [TD] abre `/technician/inbox`.
2. Filtra por carrera, Fase, estado (`SUBIDO`, `SUBSANADO`, etc.).
3. Accede a revisión, rechazo (UC-008) o aprobación (UC-009) de la **Subfase**.

## Excepciones y flujos alternos

| Condición | Comportamiento |
|-----------|----------------|
| Filtro sin resultados | Mensaje vacío con sugerencia de ampliar criterios |

## Postcondiciones

Bandeja filtrada lista para acción de workflow.

## Criterio de éxito

Filtro representativo en ≤ **2 min** (BRD-REQ-026).

## Diagramas

- [Dashboard drilldown](../diagramas/MAR-SEQ-004-dashboard-drilldown.mmd)
- [Aprobación/rechazo](../diagramas/MAR-SEQ-003-aprobacion-rechazo-subfase.mmd)

## Escenarios Gherkin

```gherkin
# language: es
@PRD-US-014 @FSD-UC-012 @TC-09b
Característica: Bandeja de auditoría [TD]

  Escenario: Filtro por carrera y estado
    Dado un [TD] en la bandeja de revisión
    Cuando filtra por carrera "Ingeniería" y estado Pendiente
    Entonces solo ve Subfases que cumplen ambos filtros
```
