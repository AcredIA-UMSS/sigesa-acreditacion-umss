---
id: FSD-UC-032
nombre: Observaciones del técnico sobre evidencias del proceso
estado: implementado
release: 8.4.0
actor_principal: "[TD]"
actores_secundarios: "[CC]"
modulo: MOD-WORKFLOW
pr_impl: PR-IMPL-055
design_doc: DD-UC-032
ultima_actualizacion: "2026-11-15"
---

# FSD-UC-032 — Observaciones del técnico sobre evidencias

## Contexto

| Campo | Valor |
| --- | --- |
| **Actor** | [TD] registra. [CC] consulta, responde y marca el seguimiento. |
| **Precondiciones** | Proceso visible (FSD-UC-019). El indicador tiene **al menos una evidencia** cargada (documentación solicitada entregada). |
| **No incluye** | Cambiar el estado del indicador (FSD-UC-008). Reutilizar `tb_observation` del dashboard. |

## Flujo principal (indicador)

1. [TD] abre el indicador en el detalle del proceso.
2. Si hay evidencia, ve **Registrar observación**. Sin evidencia el botón no aparece.
3. El modal muestra el **historial** append-only de observaciones de ese indicador.
4. [TD] ingresa texto (≥10) y recomendación; envía.
5. Queda `ENVIADA`. El sistema notifica al [CC] (EMAIL + in-app, FSD-UC-015 / `TechnicalObservationIssued`).
6. [CC] ve el historial en el mismo indicador y puede responder en el panel de proceso.

Cada envío es una fila nueva: no se pisa el texto anterior.

## Estados

`BORRADOR` → `ENVIADA` → `EN_REVISION` → `RESPONDIDA` → `ATENDIDA` → `CERRADA`.

El modal del indicador crea borrador y lo envía en el mismo paso.

## Excepciones

| Condición | Respuesta |
| --- | --- |
| Indicador sin evidencia | El botón no se renderiza; API 422 si se intenta crear sin evidencia válida |
| [CC] de otra carrera | `404 PROCESS_NOT_FOUND` |
| Rol distinto de [TD] o [CC] | `403 FORBIDDEN_ROLE` |
| Descripción menor a 10 caracteres o sin recomendación al enviar | `422 OBSERVATION_INCOMPLETE` |

## Escenarios

```gherkin
# language: es
Característica: Observación de evidencia por indicador

  Escenario: El técnico observa solo si hay documentación
    Dado un indicador ACTIVE con evidencia cargada
    Cuando el [TD] abre el indicador
    Entonces ve el botón Registrar observación
    Y al enviarla el historial muestra la nueva entrada
    Y el [CC] recibe notificación in-app y correo

  Escenario: Sin evidencia no hay botón
    Dado un indicador sin evidencias
    Cuando el [TD] abre el indicador
    Entonces no ve Registrar observación
```
