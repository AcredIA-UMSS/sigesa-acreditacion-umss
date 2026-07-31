---
id: FSD-UC-019
nombre: Diagnóstico Semántico de Fases con IA
estado: Pendiente
release: v1.0
actor_principal: "[CC], [JD]"
trazabilidad_prd: PRD-US-019
modulo: MOD-AI
reglas: FSD-BR-09
ultima_actualizacion: "2026-07-30"
---

# FSD-UC-019 — Diagnóstico Semántico de Fases con IA (RAG)

## Contexto

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-019 · PRD-US-019 |
| **Pantalla** | `/coordinator/dashboard` (CC) y `/executive/process/{processId}/phase/{phaseId}` (JD) |
| **API** | `POST /api/v1/phases/{phaseId}/diagnose` |

## Flujo principal

1. El usuario ([CC] o [JD]) accede a la vista de la fase (fase actual de su carrera para [CC] o fase seleccionada de cualquier carrera para [JD]).
2. El sistema muestra la tarjeta de diagnóstico con el reporte más reciente si este fue actualizado hace menos de 15 minutos.
3. Si el diagnóstico es antiguo o no existe, el botón "Diagnosticar Fase" se habilita.
4. El usuario hace clic en el botón.
5. El sistema extrae de forma síncrona el contexto relacional de la fase en la BD (indicadores, estados, tiempos y observaciones textuales) y busca la correspondencia de la norma CEUB/ARCUSUR en la base documental (RAG).
6. El sistema envía este contexto compilado al LLM a través del puerto de diagnóstico de IA.
7. El sistema guarda la respuesta generada en la tabla `phase_diagnostic` y la retorna.
8. La pantalla actualiza la vista mostrando el reporte semántico (Resumen de bloqueo, causa raíz, impacto de norma y plan de acción).

## Excepciones y flujos alternos

| Condición | Comportamiento |
|-----------|----------------|
| [CC] intenta diagnosticar fase de otra carrera | Error `403 Forbidden` por restricción de aislamiento (FSD-BR-09). |
| Múltiples clics simultáneos en la misma fase | El backend detecta la ejecución activa, bloquea las llamadas concurrentes duplicadas (Single-flight) y comparte el resultado. |
| Re-evaluación en cooldown (< 15 minutos) | Botón de UI deshabilitado. Petición directa API retorna la caché almacenada sin invocar al LLM. |

## Postcondiciones

* Diagnóstico semántico generado y cacheado para la toma de decisiones.
* Trazabilidad completa de las causas de retraso según la reglamentación.

## Escenarios Gherkin

```gherkin
# language: es
@PRD-US-019 @FSD-UC-019 @TC-19a
Característica: Diagnóstico Semántico de Fase con IA RAG

  Escenario: Coordinador genera diagnóstico de su fase activa
    Dado un [CC] autenticado de la carrera "Ingeniería de Sistemas"
    Cuando abre su dashboard de la fase activa actual
    Y hace clic en "Diagnosticar Fase"
    Entonces el sistema retorna un análisis de bloqueos y recomendaciones
    Y guarda el reporte en caché

  Escenario: Bloqueo de acceso cross-carrera
    Dado un [CC] autenticado de la carrera "Medicina"
    Cuando intenta llamar a POST /api/v1/phases/uuid-sistemas/diagnose
    Entonces el sistema responde con un error 403 Forbidden

  Escenario: Prevención de llamadas duplicadas (Single-flight)
    Dado un diagnóstico en curso iniciado por el usuario A para la fase X
    Cuando el usuario B hace clic en "Diagnosticar Fase" para la misma fase X
    Entonces el sistema no realiza una segunda llamada al LLM
    Y el usuario B recibe el mismo resultado generado por la llamada del usuario A
```
