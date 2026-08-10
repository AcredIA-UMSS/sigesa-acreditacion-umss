---
id: FSD-UC-022
nombre: Gestión de fases y subfases en proceso
estado: Hecho
release: v1.0
actor_principal: "[JD], [TD]"
trazabilidad_prd: PRD-US-023
modulo: MOD-PROCESS
reglas: FSD-BR-07, FSD-BR-21, FSD-BR-22, FSD-BR-23
ultima_actualizacion: "2026-08-09"
design_doc: DD-UC-022
pr_impl: PR-IMPL-022
---

# FSD-UC-022 — Gestión de fases y subfases en proceso

## Contexto

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-002, 004 · PRD-US-023 |
| **Design Doc** | [`DD-UC-022`](../../design/DD-UC-022.md) |
| **Relación** | Complementa [FSD-UC-019](FSD-UC-019.md) (consulta) y [FSD-UC-021](FSD-UC-021.md) (plantilla origen). Opera sobre instancias **`Phase` / `Subphase`** de un `AccreditationProcess`. |
| **Precondiciones** | Proceso en estado `ACTIVE`; [JD] o [TD] autenticado |
| **Pantalla** | `/procesos/{processId}/estructura` (modo edición desde detalle UC-019) |

Permite a **[JD]** y **[TD]** **crear, modificar y eliminar** fases y subfases **dentro de un proceso ya instanciado**, cuando la operación institucional lo requiera (ajuste puntual de cronograma o estructura no cubierta por la plantilla base). También disponible vía asistente virtual (`manage_process_phase`, `list_process_phases`).

> **No confundir con [FSD-UC-010](FSD-UC-010.md):** UC-010 es **cerrar/avanzar** fase por workflow de indicadores aprobados, no editar la taxonomía.

## Flujo principal — Agregar estructura

1. [JD] abre detalle del proceso (`/procesos/{processId}`) y entra a **Editar estructura**.
2. Puede **agregar fase** con nombre, `order` y descripción opcional.
3. Dentro de una fase, **agregar subfase** con nombre, `order`, **`referenceUrl`** y descripción opcional.
4. Puede **reordenar** fases/subfases (actualiza `order` sin colisiones).
5. Guarda cambios; el árbol actualizado es visible de inmediato en UC-019.

## Flujos alternos — Modificar y eliminar

| ID | Acción | Regla |
|----|--------|-------|
| M1 | Modificar nombre/descripción/enlace | Permitido en cualquier subfase sin evidencias bloqueadas |
| M2 | Eliminar subfase | Solo si **no** tiene indicadores con evidencia en estado distinto de `PENDIENTE` vacío (FSD-BR-22) |
| M3 | Eliminar fase | Solo si **todas** sus subfases cumplen regla M2 |
| M4 | Proceso `COMPLETED` o `CANCELLED` | Estructura **solo lectura** |

## Excepciones

| ID | Condición | Respuesta |
|----|-----------|-----------|
| A1 | Eliminar subfase con evidencia/workflow iniciado | `409 SUBPHASE_HAS_EVIDENCE` |
| A2 | `order` duplicado | `400 PROCESS_STRUCTURE_ORDER_CONFLICT` |
| A3 | Subfase sin `referenceUrl` | `400 SUBPHASE_LINK_REQUIRED` |
| A4 | Proceso no `ACTIVE` | `409 PROCESS_NOT_EDITABLE` |
| A5 | Rol distinto de [JD] o [TD] (p. ej. [CC], [EE]) | `403 FORBIDDEN_ROLE` |

## Postcondiciones

- Árbol Fase → Subfase del proceso actualizado y ordenado.
- Bitácora registra cambios estructurales (UC-017, release posterior).
- Cierre de fase (UC-010) recalcula conteos sobre la estructura vigente.

## Fuera de alcance (v1.0)

- Creación/eliminación de **indicadores** dentro de subfase.
- Migración masiva desde otra plantilla.
- Edición por [CC].

## API propuesta (resumen)

Ver [`api_contracts.md`](../api_contracts.md) § API-PROC-05…08.

## Escenarios Gherkin

```gherkin
# language: es
@PRD-US-023 @FSD-UC-022 @TC-22
Característica: Gestión estructural de fases y subfases en proceso

  Escenario: Agregar subfase con enlace a proceso activo
    Dado un proceso ACTIVE con al menos una fase
    Cuando el [JD] agrega la subfase "Informe parcial" con enlace "https://duea.umss.edu.bo/ref/informe"
    Entonces el detalle del proceso muestra la nueva subfase ordenada
    Y UC-019 refleja el conteo actualizado de subfases

  Escenario: Bloqueo al eliminar subfase con evidencia
    Dado una subfase con al menos una evidencia SUBIDA u OBSERVADA
    Cuando el [JD] intenta eliminarla
    Entonces el sistema responde 409 SUBPHASE_HAS_EVIDENCE

  Escenario: Proceso cerrado no editable
    Dado un proceso en estado COMPLETED
    Cuando el [JD] intenta agregar una fase
    Entonces el sistema responde 409 PROCESS_NOT_EDITABLE
```
