---
id: FSD-UC-010
nombre: Cerrar Nivel 1 (Dimensión/Área)
estado: Reespecificado
release: v2.0
actor_principal: "[TD]"
trazabilidad_prd: PRD-US-011
modulo: MOD-WORKFLOW
reglas: FSD-BR-07
design_doc: DD-UC-010
pr_impl: PR-IMPL-039
ultima_actualizacion: "2026-09-08"
nota_implementacion: "Código v1.x cierra Phase; migración v2.0 pendiente"
---

# FSD-UC-010 — Cerrar Nivel 1 (Dimensión/Área)

## Contexto

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-010, 017 · PRD-US-011 |
| **Precondiciones** | Todos los **indicadores** del subárbol del Nivel 1 en `APROBADO` |
| **Hard constraint** | Ver LFSD §3 regla 2 |
| **Nomenclatura UI** | ARCU-SUR: «Cerrar Dimensión» · CEUB: «Cerrar Área» |

## Flujo principal

1. [TD] solicita cierre del **Nivel 1** seleccionado.
2. Sistema verifica: `COUNT(indicadores_subárbol) = COUNT(indicadores WHERE estado = APROBADO)`.
3. Si corresponde, registra Nivel 1 como `COMPLETADO`.
4. Publica evento `Level1Completed`.

## Excepciones y flujos alternos

| Condición | Respuesta |
|-----------|-----------|
| Indicadores pendientes | `409 NIVEL1_CIERRE_BLOQUEADO` + lista de indicadores |
| [CC] intenta forzar cierre | `403 FORBIDDEN_ROLE` |

## Postcondiciones

Nivel 1 en estado `COMPLETADO`; evento `Level1Completed` publicado.

## Diagramas

- [Estados cierre Nivel 1](../diagramas/FSD-UC-010_cierre_nivel1_estados.mmd) *(renombrar desde `cierre_fase`)*
- [Estados indicador](../diagramas/FSD-UC-006_008_009_estados_indicador.mmd)

## Escenarios Gherkin

```gherkin
# language: es
@PRD-US-011 @FSD-UC-010 @TC-SAD-002
Característica: Cierre de Nivel 1 (Dimensión/Área)

  Escenario: Cierre bloqueado con indicadores pendientes
    Dado un Nivel 1 con al menos un Indicador no Aprobado
    Cuando el [TD] intenta cerrar el Nivel 1
    Entonces el sistema rechaza la transición
    Y lista los Indicadores pendientes
```

## Nota de migración v1.x

La implementación actual cierra **Fase** con la misma regla agregada sobre **Subfases**. En v2.0 el agregado es el subárbol completo N2→N3→Indicadores bajo el Nivel 1.
