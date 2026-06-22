---
id: FSD-UC-018
nombre: Importación masiva
estado: Pendiente
release: v1.1
actor_principal: "[CC]"
trazabilidad_prd: PRD-US-024
modulo: MOD-EVIDENCE
reglas: —
ultima_actualizacion: "2026-06-15"
---

# FSD-UC-018 — Importación masiva

## Contexto

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-020 · PRD-US-024 · MRD-N-14 |
| **API** | `POST /imports/evidences` |

## Flujo principal

1. [CC] descarga plantilla CSV institucional.
2. Completa filas vinculadas a Indicadores.
3. `POST /imports/evidences` con archivo validado.
4. Sistema crea borradores fila a fila.
5. Genera reporte de filas rechazadas con causa.

## Excepciones y flujos alternos

| Condición | Respuesta |
|-----------|-----------|
| Columnas obligatorias faltantes | Rechazo **total** sin registros parciales |
| Fila con Indicador inválido | Fila rechazada; resto continúa según política |

## Postcondiciones

Borradores creados + reporte de importación descargable.

## Diagramas

_Sin diagrama dedicado en baseline; ver [`../diagramas/README.md`](../diagramas/README.md) si se añade en diseño._

## Escenarios Gherkin

```gherkin
# language: es
@PRD-US-024 @FSD-UC-018 @TC-15
Característica: Importación masiva desde planilla

  Escenario: Importación válida desde planilla
    Dado un [CC] con plantilla de importación descargada
    Cuando sube la planilla con filas válidas
    Entonces el sistema crea actividades/evidencias en borrador vinculadas a Indicadores
    Y reporta filas rechazadas con causa por fila

  Escenario: Planilla con errores de formato
    Dado una planilla de importación con columnas obligatorias faltantes
    Cuando intenta importar
    Entonces el sistema rechaza la planilla completa
    Y no crea registros parciales inconsistentes
```
