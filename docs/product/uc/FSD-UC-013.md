---
id: FSD-UC-013
nombre: Panel semáforo [JD]
estado: Pendiente
release: v1.0
actor_principal: "[JD]"
trazabilidad_prd: PRD-US-013
modulo: MOD-DASH
reglas: —
ultima_actualizacion: "2026-06-15"
---

# FSD-UC-013 — Panel semáforo [JD]

## Contexto

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-011 · PRD-US-013 |
| **Pantalla** | `/executive/semaphore` |
| **API** | `GET /dashboard/executive` |

## Flujo principal

1. [JD] abre `/executive/semaphore`.
2. Sistema calcula semáforo por carrera/facultad (**Rojo / Amarillo / Verde**) según reglas de completitud y vencimientos.
3. Vista consolidada disponible en ≤ **2 min** sin soporte ad-hoc.

## Excepciones y flujos alternos

| Condición | Comportamiento |
|-----------|----------------|
| Carrera sin datos | Celda gris / sin dato con leyenda |

## Postcondiciones

Panel ejecutivo actualizado para toma de decisiones institucional.

## Diagramas

- [Dashboard semáforos](../diagramas/AYL-SEQ-004-dashboard-semaforos.mmd)
- [Cobertura NFR](../diagramas/diag-10-pie-cobertura-nfr-iso25010.mmd)

## Escenarios Gherkin

```gherkin
# language: es
@PRD-US-013 @FSD-UC-013 @TC-09
Característica: Panel semáforo ejecutivo

  Escenario: Vista consolidada en menos de 2 minutos
    Dado un [JD] autenticado
    Cuando abre el panel ejecutivo global
    Entonces ve semáforos por carrera y facultad
    Y obtiene la vista sin asistencia técnica ad-hoc en menos de 2 minutos

  Escenario: Coherencia con reglas de completitud
    Dado reglas de completitud configuradas para el piloto
    Cuando una carrera tiene indicadores críticos vencidos
    Entonces el semáforo de esa carrera es Rojo
```
