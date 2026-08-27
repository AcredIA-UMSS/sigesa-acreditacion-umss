---
id: FSD-UC-010
nombre: Avanzar/cerrar Fase
estado: Implementado
release: v1.0
actor_principal: "[TD]"
trazabilidad_prd: PRD-US-011
modulo: MOD-WORKFLOW
reglas: FSD-BR-07
design_doc: DD-UC-010
pr_impl: PR-IMPL-039
ultima_actualizacion: "2026-08-27"
---

# FSD-UC-010 — Avanzar/cerrar Fase

## Contexto

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-010, 017 · PRD-US-011 |
| **Precondiciones** | Todas las **subfases** de la Fase en `APROBADO` |
| **Hard constraint** | Ver LFSD §3 regla 2 |

## Flujo principal

1. [TD] solicita cierre de fase.
2. Sistema verifica: `COUNT(subfases) = COUNT(subfases WHERE estado = APROBADO)`.
3. Si corresponde, registra fase como `COMPLETADA`.
4. Publica evento `PhaseCompleted`.

## Excepciones y flujos alternos

| Condición | Respuesta |
|-----------|-----------|
| Subfases pendientes | `409 FASE_CIERRE_BLOQUEADO` + lista de subfases |
| [CC] intenta forzar cierre | `403 FORBIDDEN_ROLE` |

## Postcondiciones

Fase en estado `COMPLETADA`; evento `PhaseCompleted` publicado.

## Diagramas

- [Estados cierre fase](../diagramas/FSD-UC-010_cierre_fase_estados.mmd)
- [Estados subfase](../diagramas/FSD-UC-006_008_009_estados_subfase.mmd)

## Escenarios Gherkin

```gherkin
# language: es
@PRD-US-011 @FSD-UC-010 @TC-SAD-002
Característica: Avance y cierre de Fase

  Escenario: Avance de Fase bloqueado con subfases pendientes
    Dado una Fase con al menos una Subfase no Aprobada
    Cuando el [TD] intenta cerrar la Fase
    Entonces el sistema rechaza la transición
    Y lista las Subfases pendientes
```
