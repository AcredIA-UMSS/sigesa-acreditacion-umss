---
id: FSD-UC-040
nombre: Agenda de visita de pares
estado: Implementado
release: v2.1
actor_principal: "[TD], [JD]"
actores_secundarios: "[EE], [CC]"
modulo: MOD-WORKFLOW
reglas: FSD-BR-09
design_doc: DD-UC-040
pr_impl: PR-IMPL-057
ultima_actualizacion: "2026-11-16"
relacionados: FSD-UC-030
---

# FSD-UC-040 — Agenda de visita de pares

## Contexto

UC-030 congela el expediente y materializa el checklist de Sala de Pares. **No** cubre la semana de visita: fechas, sala, entrevistas ni documentos de la visita (agenda diaria, lista de pares, informe de autoevaluación). ARCU-SUR/CEUB gira en torno a esa semana.

| Campo | Valor |
|-------|-------|
| **Pantalla** | `/procesos/{processId}/visita-pares` |
| **API** | `GET/PUT /api/v1/processes/{processId}/peer-visit` |
| **No incluye** | Freeze (UC-030). Dictamen por dimensión (UC-041). Subir blobs MinIO de la visita. |

## Flujo principal

1. [TD] o [JD] abre **Agenda de visita** del proceso.
2. Si no hay visita, el sistema responde `visit: null`.
3. [TD]/[JD] registra ventana (`startsOn`, `endsOn`), sede, sala, estado y ítems: `INTERVIEW`, `FACILITY`, `DOCUMENT`, `SESSION`.
4. [EE] y [CC] de la carrera **solo leen**.
5. Una visita `COMPLETED` no se edita (`409 VISIT_IMMUTABLE`).

## Excepciones

| Condición | Respuesta |
|-----------|-----------|
| `endsOn` anterior a `startsOn` | `422 INVALID_DATE_RANGE` |
| Proceso fuera de alcance | `404 PROCESS_NOT_FOUND` |
| Editar visita `COMPLETED` | `409 VISIT_IMMUTABLE` |
| Actor [CC]/[EE] en PUT | `403` |

## Criterios de aceptación

```gherkin
# language: es
@FSD-UC-040 @TC-VISIT
Característica: Agenda de la semana de pares

  Escenario: Técnico programa la visita
    Dado un [TD] autenticado y un proceso visible
    Cuando registra sede, sala y fechas de visita con un ítem DOCUMENT
    Entonces la consulta posterior devuelve esos datos
    Y un [EE] de la misma carrera puede leerlos

  Escenario: Rango inválido
    Dado un [JD] autenticado
    Cuando la fecha de fin es anterior al inicio
    Entonces recibe 422 INVALID_DATE_RANGE

  Escenario: Visita cerrada inmutable
    Dado una visita en estado COMPLETED
    Cuando [TD] intenta modificarla
    Entonces recibe 409 VISIT_IMMUTABLE
```
