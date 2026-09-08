---
id: FSD-UC-019
nombre: Consulta de procesos de acreditación
estado: Reespecificado
release: v2.0
actor_principal: "[JD], [TD], [CC]"
trazabilidad_prd: PRD-US-023, PRD-US-012
modulo: MOD-PROCESS
reglas: FSD-BR-09, FSD-BR-17
ultima_actualizacion: "2026-09-08"
nota_implementacion: "Código v1.x devuelve phases/subphases; migración v2.0 pendiente"
---

# FSD-UC-019 — Consulta de procesos de acreditación

## Contexto

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-016 · PRD-US-023 (dominio proceso) · PRD-US-012 (alcance [CC]) |
| **Relación** | Complementa [FSD-UC-003](FSD-UC-003.md) (creación de proceso). No reemplaza [FSD-UC-011](FSD-UC-011.md) (dashboard KPIs/observaciones). |
| **Precondiciones** | Usuario autenticado con JWT válido; existe al menos un `AccreditationProcess` en el sistema |
| **Pantallas** | `/procesos` (listado) · `/procesos/{processId}` (detalle) |
| **API propuesta** | `GET /api/v1/processes` · `GET /api/v1/processes/{processId}` |

Permite a **JD**, **TD** y **CC** consultar procesos de acreditación institucional (CEUB / ARCU-SUR) con visibilidad acotada por rol. En el **detalle**, el usuario ve el árbol normativo completo **Nivel 1 → Nivel 2 → Nivel 3 → Indicador** clonado al crear el proceso (taxonomía de FSD-UC-003). La UI debe mostrar la **nomenclatura del modelo evaluador** (Dimensión/Área, etc.) según [`glosario.md`](../glosario.md) §2.2.

## Matriz de autorización (RBAC)

| Rol | Listado (`GET /processes`) | Detalle (`GET /processes/{id}`) | Criterio de filtro |
|-----|----------------------------|----------------------------------|--------------------|
| **[JD]** | Todos los procesos | Cualquier proceso | Sin filtro por carrera |
| **[TD]** | Todos los procesos | Cualquier proceso | Sin filtro por carrera |
| **[CC]** | Solo procesos de su carrera asignada | Solo si `process.career_id ∈ JWT.programScope` | `user_program_assignment` activo (FSD-BR-09) |

> **Regla:** [CC] **nunca** recibe en listado ni puede abrir por ID un proceso de otra carrera. Intento directo → `403 FORBIDDEN_SCOPE` o `404 PROCESS_NOT_FOUND` (preferible **404** para no filtrar existencia cross-carrera).

## Flujo principal — Listado

1. Usuario autenticado abre `/procesos`.
2. Frontend invoca `GET /api/v1/processes`.
3. Backend aplica filtro según rol:
   - **JD / TD:** devuelve todos los procesos (paginación opcional v1.1).
   - **CC:** devuelve solo procesos cuya `career_id` esté en `programScope` del JWT.
4. UI muestra tabla/tarjetas con: carrera (nombre/código), plantilla (CEUB/ARCU-SUR), estado (`ACTIVE`, `COMPLETED`, `CANCELLED`), fecha inicio.
5. Usuario selecciona un proceso → navega a `/procesos/{processId}`.

## Flujo principal — Detalle

1. Usuario abre `/procesos/{processId}`.
2. Frontend invoca `GET /api/v1/processes/{processId}`.
3. Backend valida permisos (rol + alcance carrera para [CC]).
4. Respuesta incluye metadatos del proceso y árbol normativo ordenado:
   - **Nivel 1** (`order` ascendente)
   - **Nivel 2** por Nivel 1
   - **Nivel 3** por Nivel 2
   - **Indicadores** por Nivel 3 (`code`, `description`, `weight`, `status`, `order`)
5. UI renderiza árbol jerárquico (acordeón o árbol expandible); en cada **indicador**, acción **Subir evidencia** (UC-004) y badge de estado de workflow.

## Modelo de respuesta (referencia)

Alineado al DTO existente `ProcessResponseDto` (POST create) extendido con datos de carrera/plantilla para lectura:

```json
{
  "id": "950e8400-e29b-41d4-a716-446655440020",
  "careerId": "550e8400-e29b-41d4-a716-446655440000",
  "careerCode": "INF-SIS",
  "careerName": "Ingeniería de Sistemas",
  "templateId": "850e8400-e29b-41d4-a716-446655440010",
  "templateName": "CEUB 2026",
  "evaluatorModel": "CEUB",
  "status": "ACTIVE",
  "startDate": "2026-08-03T10:00:00",
  "level1Nodes": [
    {
      "id": "...",
      "name": "Área académica",
      "label": "Área",
      "order": 1,
      "level2Nodes": [
        {
          "id": "...",
          "name": "Variable docente",
          "label": "Variable",
          "order": 1,
          "level3Nodes": [
            {
              "id": "...",
              "name": "Sub-variable formación",
              "label": "Sub-variable",
              "order": 1,
              "indicators": [
                {
                  "id": "...",
                  "code": "IND-01",
                  "description": "Plan de formación docente",
                  "weight": 0.15,
                  "order": 1,
                  "status": "PENDIENTE"
                }
              ]
            }
          ]
        }
      ]
    }
  ]
}
```

**Listado (resumen):** array de objetos sin árbol completo (conteos opcionales: `level1Count`, `indicatorCount`) para performance.

## Excepciones y flujos alternos

| ID | Condición | Respuesta |
|----|-----------|-----------|
| A1 | Sin JWT / token inválido | `401 UNAUTHORIZED` |
| A2 | [CC] consulta proceso de otra carrera | `404 PROCESS_NOT_FOUND` (o `403 FORBIDDEN_SCOPE`) |
| A3 | `processId` inexistente | `404 PROCESS_NOT_FOUND` |
| A4 | [CC] sin asignación de carrera activa | `200` con lista vacía / mensaje en UI |
| A5 | Sin procesos en el sistema | `200` con `[]` |

## Postcondiciones

- Usuario visualiza solo los procesos permitidos por su rol.
- En detalle, visualiza la estructura normativa completa del proceso (solo lectura; edición en UC-022).

## Fuera de alcance (v2.0)

- Edición del árbol normativo → [FSD-UC-022](FSD-UC-022.md).
- Gestión de plantillas normativas → [FSD-UC-021](FSD-UC-021.md).
- Asignación de responsable [CC] → [FSD-UC-023](FSD-UC-023.md).
- Cierre de Nivel 1 por workflow → [FSD-UC-010](FSD-UC-010.md).
- Evidencias por indicador (UC-004 en adelante).
- Paginación/filtros avanzados en listado (v1.1).

## Diagramas

- [Ciclo proceso acreditación](../diagramas/MAR-STA-002-ciclo-proceso-acreditacion.mmd)
- [Proceso y cierre Nivel 1](../diagramas/FSD-UC-003_010_proceso_y_cierre_nivel1_secuencia.mmd)

## Escenarios Gherkin

```gherkin
# language: es
@PRD-US-023 @PRD-US-012 @FSD-UC-019 @TC-19
Característica: Consulta de procesos de acreditación

  Escenario: [JD] ve todos los procesos
    Dado un [JD] autenticado
    Y existen procesos activos para las carreras "Ingeniería de Sistemas" e "Ingeniería Civil"
    Cuando solicita GET /api/v1/processes
    Entonces recibe ambos procesos en el listado
    Y puede abrir el detalle de cualquiera de ellos

  Escenario: [TD] ve todos los procesos
    Dado un [TD] autenticado
    Y existen al menos dos procesos en el sistema
    Cuando solicita GET /api/v1/processes
    Entonces recibe todos los procesos registrados

  Escenario: [CC] ve solo el proceso de su carrera
    Dado un [CC] autenticado asignado a la carrera "Ingeniería de Sistemas"
    Y existe un proceso ACTIVE para "Ingeniería de Sistemas"
    Y existe un proceso ACTIVE para "Ingeniería Civil"
    Cuando solicita GET /api/v1/processes
    Entonces recibe únicamente el proceso de "Ingeniería de Sistemas"
    Y no ve el proceso de "Ingeniería Civil"

  Escenario: [CC] no puede abrir proceso ajeno por ID
    Dado un [CC] autenticado asignado a la carrera X
    Y existe un proceso P perteneciente a la carrera Y
    Cuando solicita GET /api/v1/processes/{id de P}
    Entonces el sistema responde con error PROCESS_NOT_FOUND o FORBIDDEN_SCOPE

  Escenario: Detalle muestra árbol normativo e indicadores ordenados
    Dado un [JD] autenticado
    Y un proceso CEUB con 1 Nivel 1, 1 Nivel 2, 1 Nivel 3 y 3 indicadores
    Cuando solicita GET /api/v1/processes/{processId}
    Entonces la respuesta incluye el árbol N1→N2→N3 ordenado por "order"
    Y cada Nivel 3 incluye sus indicadores con code, weight y status

  Escenario: Listado vacío para [CC] sin proceso en su carrera
    Dado un [CC] autenticado asignado a una carrera sin proceso activo
    Cuando solicita GET /api/v1/processes
    Entonces recibe una lista vacía
```

## Trazabilidad técnica

| Artefacto | Estado | Enlace |
|-----------|--------|--------|
| `DD-UC-019` | Borrador | [`docs/design/DD-UC-019.md`](../../design/DD-UC-019.md) |
| `PR-IMPL-019` | Aprobado (backend Spring Boot) | [`docs/prompts/impl/PR-IMPL-019.md`](../../prompts/impl/PR-IMPL-019.md) |
| `api_contracts.md` | Pendiente (post-impl) | API-PROC-03, API-PROC-04 |
| Frontend | Legacy v1.x | Feature `processes/` — migrar a árbol normativo v2.0 |
| Tests | Pendiente (post-impl) | Aislamiento [CC] carrera A vs B; JD/TD ven todos |
