---
id: FSD-UC-022
nombre: Gestión de estructura normativa en proceso
estado: Reespecificado
release: v2.0
actor_principal: "[JD], [TD]"
trazabilidad_prd: PRD-US-023
modulo: MOD-PROCESS
reglas: FSD-BR-07, FSD-BR-21, FSD-BR-22, FSD-BR-23
ultima_actualizacion: "2026-09-08"
design_doc: DD-UC-022
pr_impl: PR-IMPL-022
nota_implementacion: "Código v1.x opera Phase/Subphase; migración v2.0 pendiente"
---

# FSD-UC-022 — Gestión de estructura normativa en proceso

## Contexto

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-002, 004 · PRD-US-023 |
| **Design Doc** | [`DD-UC-022`](../../design/DD-UC-022.md) *(actualizar para v2.0)* |
| **Relación** | Complementa [FSD-UC-019](FSD-UC-019.md) (consulta) y [FSD-UC-021](FSD-UC-021.md) (plantilla origen). Opera sobre instancias **Nivel 1 / Nivel 2 / Nivel 3 / Indicador** de un `AccreditationProcess`. |
| **Precondiciones** | Proceso en estado `ACTIVE`; [JD] o [TD] autenticado |
| **Pantalla** | `/procesos/{processId}/estructura` (editor árbol normativo desde detalle UC-019) |

Permite a **[JD]** y **[TD]** **crear, modificar y eliminar** nodos del árbol normativo **dentro de un proceso ya instanciado**, cuando la operación institucional lo requiera (ajuste puntual no cubierto por la plantilla base). También disponible vía asistente virtual (`manage_process_level1`, `list_process_indicators` — contratos v2.0).

> **No confundir con [FSD-UC-010](FSD-UC-010.md):** UC-010 es **cerrar Nivel 1** cuando todos los indicadores de su subárbol están aprobados, no editar la estructura.

## Flujo principal — Agregar estructura

1. [JD]/[TD] abre detalle del proceso (`/procesos/{processId}`) y entra a **Editar estructura normativa**.
2. Puede **agregar Nivel 1** con nombre, `order` y descripción opcional.
3. Dentro de cada nivel, **agregar hijos** (N2 bajo N1, N3 bajo N2, Indicador bajo N3) con campos obligatorios según [FSD-UC-021](FSD-UC-021.md).
4. Al agregar **Indicador**: `code`, `description`, `weight`, `referenceUrl`, `order`.
5. Puede **reordenar** nodos en cada contenedor (actualiza `order` sin colisiones).
6. En detalle del proceso (UC-019), cada indicador muestra metadatos, evidencias cargadas (0..N) y observaciones registradas por [TD]/[JD].
7. Guarda cambios; el árbol actualizado es visible de inmediato en UC-019.

## Flujos alternos — Modificar y eliminar

| ID | Acción | Regla |
|----|--------|-------|
| M1 | Modificar nombre/descripción/enlace de nodo | Permitido en nodos sin workflow bloqueado |
| M2 | Eliminar **Indicador** | Solo si **no** tiene evidencias en workflow iniciado (estado = `PENDIENTE` vacío) (FSD-BR-22) |
| M3 | Eliminar Nivel 3 / 2 / 1 | Solo si **todos** los descendientes cumplen regla M2 |
| M4 | Proceso `COMPLETED` o `CANCELLED` | Estructura **solo lectura** |

## Excepciones

| ID | Condición | Respuesta |
|----|-----------|-----------|
| A1 | Eliminar indicador con evidencia/workflow iniciado | `409 INDICATOR_HAS_EVIDENCE` |
| A2 | `order` duplicado | `400 PROCESS_STRUCTURE_ORDER_CONFLICT` |
| A3 | Indicador sin `referenceUrl`, `code` o `weight` | `400 INDICATOR_INCOMPLETE` |
| A4 | Proceso no `ACTIVE` | `409 PROCESS_NOT_EDITABLE` |
| A5 | Rol distinto de [JD] o [TD] (p. ej. [CC], [EE]) | `403 FORBIDDEN_ROLE` |

## Postcondiciones

- Árbol normativo del proceso actualizado y ordenado.
- Bitácora registra cambios estructurales (UC-017, release posterior).
- Cierre de Nivel 1 (UC-010) recalcula conteos sobre la estructura vigente.

## Fuera de alcance (v2.0)

- Migración masiva desde otra plantilla.
- Edición por [CC].
- Recálculo automático de ponderaciones globales del proceso (manual en v2.0).

## API propuesta (resumen v2.0)

Ver [`api_contracts.md`](../api_contracts.md) § API-PROC-05…08 *(extender para niveles e indicadores)*.

## Escenarios Gherkin

```gherkin
# language: es
@PRD-US-023 @FSD-UC-022 @TC-22
Característica: Gestión estructural normativa en proceso v2.0

  Escenario: Agregar indicador con enlace a proceso activo
    Dado un proceso ACTIVE con al menos un Nivel 3
    Cuando el [JD] agrega el indicador "IND-PARC" con ponderación 0.1 y enlace "https://duea.umss.edu.bo/ref/informe"
    Entonces el detalle del proceso muestra el nuevo indicador ordenado
    Y UC-019 refleja el conteo actualizado de indicadores

  Escenario: Bloqueo al eliminar indicador con evidencia
    Dado un indicador con al menos una evidencia SUBIDA u OBSERVADA
    Cuando el [JD] intenta eliminarlo
    Entonces el sistema responde 409 INDICATOR_HAS_EVIDENCE

  Escenario: Proceso cerrado no editable
    Dado un proceso en estado COMPLETED
    Cuando el [JD] intenta agregar un Nivel 1
    Entonces el sistema responde 409 PROCESS_NOT_EDITABLE
```

## Nota de migración v1.x

La implementación actual (`Phase`/`Subphase`, API-PROC-05…08) mapéa **Fase → Nivel 1** e **Subfase → Indicador**. La UI `/procesos/{id}/estructura` debe evolucionar a editor multinivel en v2.0.
