---
id: FSD-UC-030
nombre: Congelamiento y Sala de Pares
estado: Hecho
release: v2.1
actor_principal: "[TD], [EE]"
trazabilidad_prd: PRD-US-026, PRD-US-025
modulo: MOD-WORKFLOW
reglas: FSD-BR-19, FSD-BR-30.1, FSD-BR-30.2
ultima_actualizacion: "2026-10-13"
design_doc: DD-UC-030
pr_impl: PR-IMPL-M10-030
nota_implementacion: "M10 Hecho: ProcessFreeze AUDIT_READONLY, checklist Sala de Pares, EE lectura, E5–E6, gates PRESENTATION/EXTERNAL_PREP. PM-018."
---

# FSD-UC-030 — Congelamiento y Sala de Pares

## Contexto

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | [ADR-0005](../../adr/ADR-0005-workflow-metodologico-evaluacion-transversal.md) |
| **Design Doc** | [`DD-UC-030`](../../design/DD-UC-030.md) |
| **Relacionados** | [FSD-UC-020](FSD-UC-020.md) (EE) · [FSD-UC-028](FSD-UC-028.md) · [FSD-UC-031](FSD-UC-031.md) (M11) |
| **Precondiciones** | Proceso existente; [TD] autenticado para freeze; [EE] con carrera asignada para checklist/lectura |
| **Pantalla** | `/procesos/{processId}` — barra de modo operativo, checklist Sala de Pares |

## Flujo — Congelar

1. [TD] ejecuta freeze `AUDIT_READONLY`.
2. `AccreditationProcess.operationalMode = AUDIT_READONLY`.
3. Escrituras de evaluación (evidencia, FODA, encuesta, plan, generate E4) responden `409 PROCESS_FROZEN`.
4. [EE] y [TD] consultan árbol y evidencias; [EE] no muta evaluación.

## Flujo — Sala de Pares

1. Al freeze (o ensure E6) el sistema materializa ítems de checklist por proceso.
2. [EE] o [TD] marcan ítems.
3. Cerrar E6 exige freeze activo + checklist 100% + entregables E6 `APPROVED`.

## Flujo — Levantar freeze / monitoreo

1. [TD] levanta freeze hacia `ACTIVE` o `IMPROVEMENT_MONITORING` (M11).
2. `IMPROVEMENT_PARTIAL` en `ProcessFreeze.mode` al pasar a monitoreo.

## Excepciones

| ID | Condición | Respuesta |
|----|-----------|-----------|
| A1 | Freeze duplicado ya `AUDIT_READONLY` | `409 ALREADY_FROZEN` |
| A2 | Escritura CC/CAE en `AUDIT_READONLY` | `409 PROCESS_FROZEN` |
| A3 | [EE] sin scope de carrera | `404 PROCESS_NOT_FOUND` |
| A4 | [EE] intenta mutar evidencia/FODA/plan | `403` |
| A5 | Aprobar E5 sin freeze | `409 STAGE_GATE_BLOCKED` |
| A6 | Aprobar E6 con checklist incompleto | `409 STAGE_GATE_BLOCKED` |

## API (resumen)

| Método | Ruta | Rol |
|--------|------|-----|
| POST | `/api/v1/processes/{id}/freeze` | TD |
| POST | `/api/v1/processes/{id}/freeze/lift` | TD |
| GET | `/api/v1/processes/{id}/freeze` | CC, TD, JD, EE, CAE |
| GET/PUT | `/api/v1/processes/{id}/peer-room-checklist` | GET: CC,TD,JD,EE · PUT: TD, EE |

## Fuera de alcance de este átomo (van en el mismo PR-IMPL)

- Ciclo E7 / justificación de ítems vencidos → [FSD-UC-031](FSD-UC-031.md) M11.
- Alta `CAE_MEMBER` y delegaciones → extensión UC-002/004/029 en DD-UC-030.
