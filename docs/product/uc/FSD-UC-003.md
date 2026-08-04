---
id: FSD-UC-003
nombre: Plantillas y Proceso CEUB/ARCU-SUR
estado: En Curso
release: v1.0
actor_principal: "[JD]"
trazabilidad_prd: PRD-US-023
modulo: MOD-PROCESS
reglas: FSD-BR-08, FSD-BR-17
ultima_actualizacion: "2026-08-03"
---

# FSD-UC-003 — Plantillas y Proceso CEUB/ARCU-SUR

## Contexto

| Campo | Valor |
| ------- | ------- |
| **Trazabilidad** | PRD-REQ-002, 004, 016 · PRD-US-023 |
| **Precondiciones** | Plantilla CEUB o ARCU-SUR validada por comité normativo; carrera registrada en catálogo `programs` |
| **Nota implementación viva** | Catálogo de **carreras UMSS** persistido en BD (`programs`) con búsqueda `GET /programs?q=`. Las **plantillas** de proceso son exclusivamente CEUB o ARCU-SUR (taxonomía Fase → Subfase predefinida). UI: autocomplete de carrera en `/procesos/nuevo`. |

Taxonomía: **Proceso → Fase → Subfase**.
*(Una plantilla/proceso puede tener múltiples fases y múltiples subfases).*

## Flujo principal

1. [JD] gestiona las plantillas (ARCU-SUR o CEUB), conformadas por un conjunto predefinido de Fases y Subfases.
2. [JD] o proceso automático crea e inicia un `AccreditationProcess` para una carrera específica, seleccionando una de las plantillas disponibles.
3. El sistema fija la taxonomía **Fase → Subfase** para el nuevo Proceso clonando la estructura de la plantilla elegida.
4. El sistema valida que exista **un solo Proceso activo** por carrera a la vez.

## Excepciones y flujos alternos

| ID | Condición | Respuesta |
|----|-----------|-----------|
| A1 | Proceso activo existente | Si la carrera ya tiene un proceso activo, se rechaza la creación con `PROCESS_ALREADY_ACTIVE` |
| A2 | Actualización de plantilla base | Si una plantilla es modificada, los Procesos que ya están en curso conservan las fases/subfases con las que iniciaron; no migran retroactivamente |

## Postcondiciones

Proceso activo para la carrera con sus respectivas instancias de Fase y Subfase.

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
    Dado que existen múltiples plantillas ARCU-SUR y CEUB configuradas con fases y subfases
    Cuando un [JD] inicia un Proceso de acreditación para una carrera utilizando una plantilla elegida
    Entonces el nuevo Proceso adopta la estructura de Fases y Subfases de esa plantilla
    Y el sistema garantiza que la carrera tenga un único Proceso activo a la vez

  Escenario: Intento de iniciar un proceso cuando ya existe uno activo
    Dado que una carrera tiene un Proceso de acreditación actualmente en curso
    Cuando un [JD] intenta iniciar un nuevo Proceso para la misma carrera
    Entonces el sistema rechaza la operación con el error PROCESS_ALREADY_ACTIVE
