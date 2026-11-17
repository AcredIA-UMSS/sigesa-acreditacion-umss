---
id: FSD-UC-041
nombre: Dictamen estructurado del par evaluador [EE]
estado: Pendiente
release: v2.1
actor_principal: "[EE]"
actores_secundarios: "[JD], [TD]"
modulo: MOD-REVIEW
reglas: FSD-BR-04, FSD-BR-09, FSD-BR-19
design_doc: —
pr_impl: —
ultima_actualizacion: "2026-11-15"
relacionados: FSD-UC-020, FSD-UC-030, FSD-UC-039
---

# FSD-UC-041 — Dictamen estructurado del par evaluador [EE]

## Contexto

UC-020 da al [EE] **lectura** del expediente y checklist de Sala de Pares. Falta el **juicio formal por dimensión/criterio** (cumple / no cumple / con observaciones) que el informe de pares CEUB/ARCU-SUR exige. El dictamen institucional final sigue siendo humano (FSD-BR-04); este UC registra el dictamen del par, no lo sustituye por IA.

| Campo | Valor |
|-------|-------|
| **Pantalla (objetivo)** | `/procesos/{processId}/dictamen-pares` |
| **API (objetivo)** | `GET/PUT /api/v1/processes/{processId}/peer-judgment` |
| **No incluye** | Aprobar indicadores (UC-009). Freeze (UC-030). Matriz de cobertura (UC-039). |

## Flujo principal

1. [EE] con carrera asignada abre el dictamen del proceso congelado o en visita.
2. Por cada N1 (dimensión/área) elige `CUMPLE` \| `CUMPLE_CON_OBSERVACIONES` \| `NO_CUMPLE` y redacta fundamento (mín. 20 caracteres).
3. Puede guardar borrador (`DRAFT`) o enviar (`SUBMITTED`).
4. [JD]/[TD] leen el dictamen enviado; no lo editan.
5. Un dictamen `SUBMITTED` es inmutable salvo que [JD] lo devuelva a `DRAFT` con motivo.

## Excepciones

| Condición | Respuesta |
|-----------|-----------|
| [EE] de otra carrera | `404 PROCESS_NOT_FOUND` |
| [EE] intenta PUT en proceso de otra carrera | `404` |
| Enviar con N1 sin juicio | `422 JUDGMENT_INCOMPLETE` |
| Editar `SUBMITTED` sin devolución [JD] | `409 JUDGMENT_IMMUTABLE` |
| [CC] escribe dictamen | `403` |

## Criterios de aceptación

```gherkin
# language: es
@FSD-UC-041 @TC-JUDGMENT
Característica: Dictamen por dimensión del par evaluador

  Escenario: El par emite juicio por dimensión
    Dado un [EE] asignado a la carrera del proceso
    Cuando califica cada N1 y envía el dictamen
    Entonces el estado es SUBMITTED
    Y [JD] puede leer los juicios y fundamentos

  Escenario: No se envía incompleto
    Dado un [EE] con al menos un N1 sin calificación
    Cuando intenta enviar
    Entonces recibe 422 JUDGMENT_INCOMPLETE

  Escenario: El coordinador no dictamina
    Dado un [CC] autenticado
    Cuando intenta PUT del dictamen
    Entonces recibe 403
```
