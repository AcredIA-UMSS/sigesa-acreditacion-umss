---
id: FSD-UC-039
nombre: Matriz de cobertura / informe de autoevaluación
estado: En Curso
release: v2.1
actor_principal: "[JD], [TD], [EE], [CC]"
modulo: MOD-EVAL
reglas: FSD-BR-09
design_doc: DD-UC-039
pr_impl: PR-IMPL-054
ultima_actualizacion: "2026-11-15"
---

# FSD-UC-039 — Matriz de cobertura / informe de autoevaluación

## Contexto

El par evaluador no inspecciona un ZIP: necesita **criterio → indicador → evidencia → estado** en una matriz imprimible. El dossier (ZIP) y el semáforo (UC-013) no sustituyen este informe.

| Campo | Valor |
|-------|-------|
| **Pantalla** | `/procesos/{processId}/cobertura` |
| **API** | `GET /api/v1/processes/{processId}/coverage-matrix` · `GET /api/v1/processes/{processId}/coverage-matrix/pdf` |
| **No incluye** | Generar el dossier ZIP (FEAT-11). Editar indicadores. Dictamen [EE] (FSD-UC-041). |

## Flujo principal

1. [JD], [TD], [EE] o [CC] (solo su carrera) abre el proceso y **Matriz de cobertura**.
2. El sistema arma el árbol N1→N2→N3→Indicador del proceso, con estado del indicador y cantidad de evidencias.
3. Muestra totales: indicadores, con evidencia, `APROBADO` / `OBSERVADO` / resto, y % de cobertura (`con evidencia / total`).
4. **Descargar PDF** entrega un informe A4 con la misma matriz (no un ZIP).

## Excepciones

| Condición | Respuesta |
|-----------|-----------|
| Proceso inexistente o fuera de alcance | `404 PROCESS_NOT_FOUND` |
| Anónimo | `401` |
| Proceso sin indicadores | `200` con totales en cero y lista vacía |

## Criterios de aceptación

```gherkin
# language: es
@FSD-UC-039 @TC-COVERAGE
Característica: Informe de autoevaluación en matriz

  Escenario: El par ve criterio, indicador, evidencias y estado
    Dado un proceso con árbol normativo e indicadores
    Y un [JD] autenticado
    Cuando consulta la matriz de cobertura
    Entonces ve cada indicador con código, estado y cantidad de evidencias
    Y ve el criterio (N3) y la dimensión (N1) que lo contienen

  Escenario: PDF imprimible distinto del dossier
    Dado un [TD] autenticado con alcance al proceso
    Cuando descarga el PDF de la matriz
    Entonces la respuesta es application/pdf
    Y el archivo no es un ZIP

  Escenario: Coordinador no ve otra carrera
    Dado un [CC] autenticado
    Cuando pide la matriz de un proceso de otra carrera
    Entonces recibe 404 PROCESS_NOT_FOUND
```
