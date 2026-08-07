---
id: FSD-UC-023
nombre: Asignación de responsable [CC] a proceso
estado: Hecho
release: v1.0
actor_principal: "[JD]"
trazabilidad_prd: PRD-US-002, PRD-US-023
modulo: MOD-PROCESS
reglas: FSD-BR-09, FSD-BR-20, FSD-BR-23
ultima_actualizacion: "2026-08-07"
design_doc: DD-UC-023
pr_impl: PR-IMPL-023
---

# FSD-UC-023 — Asignación de responsable [CC] a proceso

## Contexto

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-001, 002 · PRD-US-002 · PRD-US-023 |
| **Design Doc** | [`DD-UC-023`](../../design/DD-UC-023.md) |
| **Relación** | Complementa [FSD-UC-002](FSD-UC-002.md) (alta de usuarios [CC]) y [FSD-UC-003](FSD-UC-003.md) / [FSD-UC-019](FSD-UC-019.md) (proceso). |
| **Precondiciones** | Proceso `ACTIVE`; usuario candidato con rol `[CC]`, cuenta `ACTIVE` y asignación de carrera coherente con `process.career_id` |
| **Pantalla** | `/procesos/{processId}` — sección «Responsable del proceso» |

Permite a **[JD]** designar un **Coordinador de Carrera [CC]** como **responsable único** del proceso de acreditación, garantizando que ese [CC] **no sea responsable de otro proceso activo** simultáneamente.

## Modelo funcional

| Campo | Descripción |
|-------|-------------|
| `processId` | Proceso de acreditación |
| `responsibleUserId` | Usuario [CC] designado |
| `assignedAt` | Timestamp de asignación |
| `assignedBy` | [JD] que ejecutó la acción |

**Reglas de unicidad (FSD-BR-20):**

- Un proceso `ACTIVE` tiene **como máximo un** responsable [CC] activo.
- Un [CC] solo puede ser responsable de **un** proceso `ACTIVE` a la vez.
- El [CC] debe tener `user_program_assignment` activo para la **misma carrera** del proceso (FSD-BR-09).

## Flujo principal

1. [JD] abre detalle del proceso.
2. Selecciona «Asignar responsable».
3. El sistema lista solo usuarios [CC] que cumplan:
   - Cuenta `ACTIVE`;
   - Asignados a la carrera del proceso;
   - **Sin** responsabilidad activa en otro proceso.
4. [JD] confirma la selección.
5. El sistema persiste la asignación y expone el nombre del responsable en listado/detalle (UC-019).

## Flujos alternos

| ID | Acción | Comportamiento |
|----|--------|----------------|
| R1 | Cambiar responsable | Revoca asignación anterior (soft) y asigna nuevo [CC] elegible |
| R2 | Quitar responsable | Deja proceso sin responsable; [CC] queda disponible para otro proceso |
| R3 | Proceso `COMPLETED` / `CANCELLED` | Asignación histórica solo lectura |

## Excepciones

| ID | Condición | Respuesta |
|----|-----------|-----------|
| A1 | [CC] ya responsable de otro proceso ACTIVE | `409 CC_ALREADY_ASSIGNED_TO_PROCESS` |
| A2 | Usuario no es rol [CC] o cuenta inactiva | `400 INVALID_RESPONSIBLE_USER` |
| A3 | [CC] no asignado a la carrera del proceso | `409 CAREER_SCOPE_MISMATCH` |
| A4 | Proceso no `ACTIVE` | `409 PROCESS_NOT_EDITABLE` |
| A5 | Rol distinto de [JD] | `403 FORBIDDEN_ROLE` |

## Postcondiciones

- Proceso muestra responsable [CC] en UI y API de detalle.
- [CC] designado mantiene acceso UC-019 filtrado por su carrera (sin cambio de RBAC base).
- Evento auditable para UC-017 (release posterior).

## Fuera de alcance (v1.0)

- Auto-asignación por [CC].
- Múltiples responsables por proceso.
- Notificación automática al [CC] (UC-015).

## API propuesta (resumen)

Ver [`api_contracts.md`](../api_contracts.md) § API-PROC-09…10.

## Escenarios Gherkin

```gherkin
# language: es
@PRD-US-002 @PRD-US-023 @FSD-UC-023 @TC-23
Característica: Asignación de responsable de proceso

  Escenario: Asignar CC disponible a proceso activo
    Dado un proceso ACTIVE de la carrera "Ingeniería de Sistemas"
    Y un usuario [CC] activo asignado a esa carrera sin otro proceso ACTIVE como responsable
    Cuando el [JD] lo designa responsable del proceso
    Entonces el detalle del proceso muestra su nombre como responsable
    Y el [CC] aparece como no disponible para otros procesos ACTIVE

  Escenario: Rechazo si CC ya responsable de otro proceso
    Dado un [CC] ya responsable de un proceso ACTIVE
    Cuando el [JD] intenta asignarlo a un segundo proceso ACTIVE
    Entonces el sistema responde 409 CC_ALREADY_ASSIGNED_TO_PROCESS

  Escenario: Rechazo por carrera incompatible
    Dado un [CC] asignado solo a la carrera "Medicina"
    Cuando el [JD] intenta asignarlo a un proceso de "Ingeniería de Sistemas"
    Entonces el sistema responde 409 CAREER_SCOPE_MISMATCH
```
