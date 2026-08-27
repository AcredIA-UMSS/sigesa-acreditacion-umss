---
id: FSD-UC-003
nombre: Plantillas y Proceso CEUB/ARCU-SUR
estado: Implementado
release: v1.0
actor_principal: "[JD]"
trazabilidad_prd: PRD-US-023
modulo: MOD-PROCESS
reglas: FSD-BR-08, FSD-BR-17, FSD-BR-21
ultima_actualizacion: "2026-08-27"
---

# FSD-UC-003 — Plantillas y Proceso CEUB/ARCU-SUR

## Contexto

| Campo | Valor |
| ------- | ------- |
| **Trazabilidad** | PRD-REQ-002, 004, 016 · PRD-US-023 |
| **Relación** | **Creación de proceso** desde plantilla. La **gestión de plantillas** (CRUD nombre, descripción, fases/subfases con enlaces) está en [FSD-UC-021](FSD-UC-021.md). La **edición estructural del proceso instanciado** en [FSD-UC-022](FSD-UC-022.md). La **asignación de responsable [CC]** en [FSD-UC-023](FSD-UC-023.md). |
| **Precondiciones** | Plantilla `PUBLISHED` (CEUB o ARCU-SUR); carrera registrada en catálogo `programs` |
| **Nota implementación viva** | Catálogo de **carreras UMSS** persistido en BD (`programs`) con búsqueda `GET /programs?q=`. UI: autocomplete de carrera en `/procesos/nuevo`. Selector de plantillas alimentado por plantillas publicadas (UC-021). Creación de proceso: `POST /api/v1/processes` + clonación Fase→Subfase desde plantilla `PUBLISHED` (2026-08-27). |

Taxonomía: **Proceso → Fase → Subfase**.
*(Una plantilla/proceso puede tener múltiples fases y múltiples subfases; cada subfase incluye enlace de referencia normativo — ver UC-021).*

## Flujo principal

1. [JD] crea o selecciona una plantilla publicada vía [FSD-UC-021](FSD-UC-021.md) (nombre, descripción, fases/subfases con `referenceUrl`).
2. [JD] inicia un `AccreditationProcess` para una carrera, seleccionando plantilla CEUB o ARCU-SUR en `/procesos/nuevo`.
3. El sistema clona la taxonomía **Fase → Subfase** (incluyendo enlaces) al nuevo proceso.
4. El sistema valida **un solo proceso activo** por carrera **y tipo de plantilla** (CEUB / ARCU-SUR) — FSD-BR-08.
5. Opcionalmente [JD] asigna responsable [CC] ([FSD-UC-023](FSD-UC-023.md)) o ajusta estructura ([FSD-UC-022](FSD-UC-022.md)).

## Excepciones y flujos alternos

| ID | Condición | Respuesta |
|----|-----------|-----------|
| A1 | Proceso activo existente (misma carrera + mismo tipo plantilla) | `409 PROCESS_ALREADY_ACTIVE` |
| A2 | Actualización de plantilla base | Procesos en curso **conservan** la estructura clonada al crear; no migran retroactivamente (FSD-BR-21) |
| A3 | Plantilla no publicada | `400 TEMPLATE_NOT_PUBLISHED` |

## Postcondiciones

Proceso activo para la carrera con instancias de Fase y Subfase (y enlaces clonados desde plantilla).

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

  Escenario: Inicio de proceso con plantilla de fases y subfases
    Dado que existen plantillas PUBLISHED ARCU-SUR y CEUB con fases, subfases y enlaces
    Cuando un [JD] inicia un Proceso de acreditación para una carrera utilizando una plantilla elegida
    Entonces el nuevo Proceso adopta la estructura de Fases y Subfases de esa plantilla
    Y cada subfase clonada conserva su referenceUrl

  Escenario: Intento de iniciar un proceso cuando ya existe uno activo del mismo tipo
    Dado que una carrera tiene un Proceso CEUB actualmente ACTIVE
    Cuando un [JD] intenta iniciar otro Proceso CEUB para la misma carrera
    Entonces el sistema rechaza la operación con el error PROCESS_ALREADY_ACTIVE

  Escenario: Dos procesos activos de distinto tipo en la misma carrera
    Dado que una carrera tiene un Proceso CEUB ACTIVE
    Cuando un [JD] inicia un Proceso ARCU-SUR para la misma carrera
    Entonces el sistema crea el segundo proceso exitosamente
```
