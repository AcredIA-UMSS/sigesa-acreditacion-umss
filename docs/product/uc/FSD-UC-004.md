---
id: FSD-UC-004
nombre: Cargar Evidencia
estado: Pendiente
release: v1.0
actor_principal: "[CC]"
trazabilidad_prd: PRD-US-005, PRD-US-025
modulo: MOD-EVIDENCE
reglas: FSD-BR-01, FSD-BR-03, FSD-BR-18
ultima_actualizacion: "2026-06-15"
---

# FSD-UC-004 — Cargar Evidencia

## Contexto

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-005, 022 · PRD-US-005, 025 |
| **Precondiciones** | Indicador en `PENDIENTE` u `OBSERVADO`; permiso sobre carrera del [CC] |

## Flujo principal

1. [CC] navega Proceso → Fase → Indicador.
2. Adjunta Evidence y metadatos obligatorios (`indicatorId`, `criterionId`, `description`).
3. Sistema valida tipo/tamaño; calcula SHA-256.
4. Evidence Service persiste `Evidence` v1 y publica `EvidenceUploaded`.
5. Audit Service inserta transición `PENDIENTE → SUBIDO` en `indicator_state_history`.
6. Notification Service notifica al [TD] (UC-015).
7. Si Evidence > 5 MB: barra de progreso y carga asíncrona (US-025).

## Excepciones y flujos alternos

| Condición | Respuesta |
|-----------|-----------|
| Sin Indicador asociado | `400` |
| Formato inválido | `422` |

## Postcondiciones

`evidenceId`, `version=1`, `contentHash`, evento `EvidenceUploaded`; Indicador en `SUBIDO`.

## Datos

| Entrada | Salida |
|---------|--------|
| `indicatorId`, `evidenceBlob`, `description`, `criterionId` | `evidenceId`, `version`, `contentHash`, `currentState` |

## Diagramas

- [Carga evidencia versionada](../diagramas/MAR-SEQ-002-carga-evidencia-versionada.mmd)
- [D-SEQ-002 carga](../diagramas/D-SEQ-002-carga-evidencia.mmd)
- [diag-02 evidencias](../diagramas/diag-02-seq-evidencias.mmd)

## Escenarios Gherkin

```gherkin
# language: es
@PRD-US-005 @FSD-UC-004 @FSD-BR-01 @TC-04
Característica: Carga de Evidencia

  Escenario: Carga exitosa con metadatos obligatorios
    Dado un [CC] autenticado y un Indicador válido en su carrera
    Cuando carga una Evidence y completa metadatos obligatorios
    Entonces el sistema crea la Evidencia versión 1 vinculada al Indicador
    Y notifica al [TD] asignado que hay revisión pendiente

  Escenario: Carga sin clasificación rechazada
    Dado un [CC] en el formulario de carga
    Cuando intenta guardar sin Indicador/Criterio asociado
    Entonces el sistema rechaza la operación
    Y indica qué campo falta completar

@PRD-US-025 @FSD-UC-004 @NFR-011 @TC-04b
  Escenario: Progreso en carga de Evidence grande
    Dado un [CC] cargando una Evidence mayor al umbral configurado de 5 MB
    Cuando la carga está en curso
    Entonces el sistema muestra barra de progreso determinada
    Y evita permitir un segundo envío duplicado hasta completar
```
