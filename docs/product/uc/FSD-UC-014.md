---
id: FSD-UC-014
nombre: Reporte ejecutivo PDF
estado: Pendiente
release: v1.0
actor_principal: "[JD]"
trazabilidad_prd: PRD-US-021
modulo: MOD-REPORT
reglas: FSD-BR-14
ultima_actualizacion: "2026-06-15"
---

# FSD-UC-014 — Reporte ejecutivo PDF

## Contexto

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-014 · PRD-US-021 · NFR-003 |
| **API** | `POST /reports/executive/pdf` |

## Flujo principal

1. [JD] aplica filtros en panel ejecutivo.
2. Invoca generación PDF (≤ **2 clics** desde contexto de trabajo).
3. Sistema genera PDF con timestamp, filtros aplicados y marca institucional UMSS.
4. Entrega asíncrona; P95 generación ≤ **5 min**.

## Excepciones y flujos alternos

| Condición | Respuesta |
|-----------|-----------|
| Sin autorización [JD] | `403` |
| Timeout generación | Reintento o error documentado |

## Postcondiciones

PDF descargable con metadatos de contexto y fecha de generación.

## Diagramas

- [Reporte PDF asíncrono](../diagramas/MAR-SEQ-005-reporte-pdf-asincrono.mmd)
- [D-SEQ-004 reporte](../diagramas/D-SEQ-004-reporte-pdf.mmd)
- [AYL reporte async](../diagramas/AYL-SEQ-005-reporte-pdf-async.mmd)

## Escenarios Gherkin

```gherkin
# language: es
@PRD-US-021 @FSD-UC-014 @NFR-003 @TC-11
Característica: Reporte ejecutivo PDF

  Escenario: Generación de reporte ejecutivo
    Dado un [JD] en el panel con filtros aplicados
    Cuando selecciona "Generar reporte PDF" desde el contexto de trabajo
    Entonces el sistema produce un PDF con marca temporal y filtros
    Y el tiempo de generación P95 es como máximo 5 minutos
```
