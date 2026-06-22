---
id: FSD-UC-011
nombre: Dashboard [CC] y observaciones
estado: Pendiente
release: v1.0
actor_principal: "[CC]"
trazabilidad_prd: PRD-US-012, PRD-US-015
modulo: MOD-DASH
reglas: FSD-BR-09
ultima_actualizacion: "2026-06-15"
---

# FSD-UC-011 — Dashboard [CC] y observaciones

## Contexto

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-012 · PRD-US-012, 015 |
| **Pantalla** | `/coordinator/dashboard` |
| **API** | `GET /dashboard/coordinator` |

## Flujo principal

1. [CC] abre `/coordinator/dashboard`.
2. Sistema muestra avance por Fase **solo de su carrera**.
3. Lista observaciones abiertas ordenadas por plazo ascendente.
4. Acceso en ≤ **3 clics** a formulario de subsanación (BRD-REQ-026).

## Excepciones y flujos alternos

| Condición | Comportamiento |
|-----------|----------------|
| Intento ver otra carrera | Datos filtrados; sin fuga cross-carrera (FSD-BR-09) |

## Postcondiciones

Vista actualizada del estado de acreditación de la carrera del [CC].

## Diagramas

- [Dashboard drilldown](../diagramas/MAR-SEQ-004-dashboard-drilldown.mmd)
- [Dashboard semáforos](../diagramas/AYL-SEQ-004-dashboard-semaforos.mmd)
- [Journey CC subsanación estados](../diagramas/PRD_journey_CC_subsanacion_estados.mmd)

## Escenarios Gherkin

```gherkin
# language: es
@PRD-US-012 @FSD-UC-011 @TC-09a
Característica: Dashboard del Coordinador de Carrera

  Escenario: Vista de carrera propia
    Dado un [CC] autenticado de la carrera X
    Cuando abre su dashboard
    Entonces ve el avance por Fase de la carrera X
    Y no ve datos de otras carreras

  Escenario: Acceso rápido a observación
    Dado una observación abierta en el dashboard
    Cuando selecciona la observación
    Entonces navega al Indicador y formulario de subsanación

@PRD-US-015 @FSD-UC-011 @TC-09c
  Escenario: Orden por fecha límite
    Dado tres observaciones abiertas con plazos distintos
    Cuando el [CC] abre su lista
    Entonces la observación con plazo más próximo aparece primero
```
