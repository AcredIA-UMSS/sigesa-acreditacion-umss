---
id: FSD-UC-003
nombre: Plantillas y Proceso CEUB/ARCU-SUR
estado: En Curso
release: v1.0
actor_principal: "[JD]"
trazabilidad_prd: PRD-US-023
modulo: MOD-PROCESS
reglas: FSD-BR-08, FSD-BR-17
ultima_actualizacion: "2026-06-15"
---

# FSD-UC-003 — Plantillas y Proceso CEUB/ARCU-SUR

## Contexto

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-002, 004, 016 · PRD-US-023 |
| **Precondiciones** | Plantilla CEUB o ARCU-SUR validada por comité normativo |
| **Nota implementación viva** | Submódulo **Gestión de Fases** (API `/api/v1/fases`) en curso — ver [`DTP.md`](../DTP.md) |

Taxonomía: **Proceso → Fase → Dimensión → Criterio → Indicador**.

## Flujo principal

1. [JD] activa plantilla para periodo vigente (`POST /templates/{id}/activate`).
2. Sistema fija taxonomía Fase → Dimensión → Criterio → Indicador para nuevos Procesos.
3. [JD] o proceso automático crea `AccreditationProcess` para carrera.
4. Sistema valida **un solo Proceso activo** por tipo/carrera/periodo.

## Excepciones y flujos alternos

| ID | Condición | Respuesta |
|----|-----------|-----------|
| A1 | Proceso duplicado | Rechaza con `PROCESS_ALREADY_ACTIVE` |
| A2 | Proceso en curso | Conserva plantilla con la que inició; no migra retroactivamente |

## Postcondiciones

Proceso activo con instancias de Fase e Indicador.

## Diagramas

- [Proceso y cierre de fase](../diagramas/FSD-UC-003_010_proceso_y_cierre_fase_secuencia.mmd)
- [Secuencia UC03](../diagramas/UC03_secuencia.mmd)
- [Estados UC03](../diagramas/UC03_estado.mmd)
- [Ciclo proceso acreditación](../diagramas/MAR-STA-002-ciclo-proceso-acreditacion.mmd)

## Escenarios Gherkin

```gherkin
# language: es
@PRD-US-023 @FSD-UC-003 @TC-03
Característica: Plantillas normativas CEUB/ARCU-SUR

  Escenario: Activación de plantilla CEUB
    Dado un [JD] con plantilla CEUB validada
    Cuando activa la plantilla para el periodo vigente
    Entonces los nuevos Procesos usan Fases e Indicadores de esa plantilla
    Y los Procesos en curso conservan la plantilla con la que iniciaron
```
