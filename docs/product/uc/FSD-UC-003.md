---
id: FSD-UC-003
nombre: Plantillas y Proceso CEUB/ARCU-SUR
estado: Implementado v2
release: v2.0
actor_principal: "[JD]"
trazabilidad_prd: PRD-US-023
modulo: MOD-PROCESS
reglas: FSD-BR-08, FSD-BR-17, FSD-BR-21
ultima_actualizacion: "2026-09-08"
nota_implementacion: "POST /processes clona árbol v2 N1→N2→N3→Indicador desde plantilla PUBLISHED; coexistencia legacy Fase/Subfase (M5)"
---

# FSD-UC-003 — Plantillas y Proceso CEUB/ARCU-SUR

## Contexto

| Campo | Valor |
| ------- | ------- |
| **Trazabilidad** | PRD-REQ-002, 004, 016 · PRD-US-023 |
| **Relación** | **Creación de proceso** desde plantilla. La **gestión de plantillas** (CRUD jerarquía normativa N1→N2→N3→Indicador) está en [FSD-UC-021](FSD-UC-021.md). La **edición estructural del proceso instanciado** en [FSD-UC-022](FSD-UC-022.md). La **asignación de responsable [CC]** en [FSD-UC-023](FSD-UC-023.md). |
| **Precondiciones** | Plantilla `PUBLISHED` (CEUB o ARCU-SUR); carrera registrada en catálogo `programs` |
| **Nota implementación viva** | Catálogo de **carreras UMSS** persistido en BD (`programs`) con búsqueda `GET /programs?q=`. UI: autocomplete de carrera en `/procesos/nuevo`. Selector de plantillas alimentado por plantillas publicadas (UC-021). **`POST /api/v1/processes`** clona **Nivel 1 → Nivel 2 → Nivel 3 → Indicador** desde plantilla `PUBLISHED` (`ProcessNormativeTreeCloner`); validación A4 si plantilla sin árbol v2 ni fases legacy. |

## Jerarquía normativa (v2.0)

```
Modelo evaluador (CEUB | ARCU-SUR)
  └── Nivel 1 (Área | Dimensión)
        └── Nivel 2 (Variable | Componente)
              └── Nivel 3 (Sub-variable | Criterio)
                    └── Indicador (código, descripción, ponderación)
                          └── Evidencias (UC-004 en adelante)
```

Un **Proceso** (`AccreditationProcess`) es la instancia operativa de ese árbol para una carrera y gestión.

## Flujo principal

1. [JD] crea o selecciona una plantilla publicada vía [FSD-UC-021](FSD-UC-021.md) (modelo CEUB o ARCU-SUR con árbol normativo completo).
2. [JD] inicia un `AccreditationProcess` para una carrera, seleccionando plantilla CEUB o ARCU-SUR en `/procesos/nuevo`.
3. El sistema clona la taxonomía **Nivel 1 → Nivel 2 → Nivel 3 → Indicador** (incluyendo `code`, `description`, `weight`, `referenceUrl`) al nuevo proceso.
4. El sistema valida **un solo proceso activo** por carrera **y tipo de plantilla** (CEUB / ARCU-SUR) — FSD-BR-08.
5. Opcionalmente [JD] asigna responsable [CC] ([FSD-UC-023](FSD-UC-023.md)) o ajusta estructura ([FSD-UC-022](FSD-UC-022.md)).

## Excepciones y flujos alternos

| ID | Condición | Respuesta |
|----|-----------|-----------|
| A1 | Proceso activo existente (misma carrera + mismo tipo plantilla) | `409 PROCESS_ALREADY_ACTIVE` |
| A2 | Actualización de plantilla base | Procesos en curso **conservan** la estructura clonada al crear; no migran retroactivamente (FSD-BR-21) |
| A3 | Plantilla no publicada | `400 TEMPLATE_NOT_PUBLISHED` |
| A4 | Plantilla sin al menos un Indicador en el árbol | `400 TEMPLATE_STRUCTURE_INCOMPLETE` |

## Postcondiciones

Proceso activo para la carrera con instancias de Nivel 1, Nivel 2, Nivel 3 e Indicadores clonados desde plantilla.

## Diagramas

- [Proceso y cierre Nivel 1](../diagramas/FSD-UC-003_010_proceso_y_cierre_nivel1_secuencia.mmd) *(renombrar desde `cierre_fase`)*
- [Secuencia UC03](../diagramas/UC03_secuencia.mmd)
- [Estados UC03](../diagramas/UC03_estado.mmd)
- [Ciclo proceso acreditación](../diagramas/MAR-STA-002-ciclo-proceso-acreditacion.mmd)

## Escenarios Gherkin

```gherkin
# language: es
@PRD-US-023 @FSD-UC-003 @TC-03
Característica: Plantillas normativas CEUB/ARCU-SUR

  Escenario: Inicio de proceso con plantilla normativa multinivel
    Dado que existen plantillas PUBLISHED ARCU-SUR y CEUB con Nivel 1, Nivel 2, Nivel 3 e Indicadores
    Cuando un [JD] inicia un Proceso de acreditación para una carrera utilizando una plantilla elegida
    Entonces el nuevo Proceso adopta la estructura normativa completa de esa plantilla
    Y cada indicador clonado conserva código, descripción, ponderación y referenceUrl

  Escenario: Intento de iniciar un proceso cuando ya existe uno activo del mismo tipo
    Dado que una carrera tiene un Proceso CEUB actualmente ACTIVE
    Cuando un [JD] intenta iniciar otro Proceso CEUB para la misma carrera
    Entonces el sistema rechaza la operación con el error PROCESS_ALREADY_ACTIVE

  Escenario: Dos procesos activos de distinto modelo evaluador en la misma carrera
    Dado que una carrera tiene un Proceso CEUB ACTIVE
    Cuando un [JD] inicia un Proceso ARCU-SUR para la misma carrera
    Entonces el sistema crea el segundo proceso exitosamente
```

## Nota de migración v1.x

La implementación actual clona **Fase → Subfase**. En v2.0 cada **Subfase legacy** se mapea conceptualmente a un **Indicador**; las **Fases** a **Nivel 1**. La expansión a N2/N3 requiere migración de datos y UI (ver FSD.md §7).
