---
id: FSD-UC-037
nombre: Fase del proceso de acreditación
estado: implementado
release: v2.1
actor_principal: "[TD]"
modulo: MOD-WORKFLOW
design_doc: —
ultima_actualizacion: "2026-11-13"
relacionados: FSD-UC-019, FSD-UC-025, FSD-UC-032
---

# FSD-UC-037 — Fase del proceso de acreditación

## Contexto

| Campo | Valor |
| --- | --- |
| **Actor** | [TD] consulta y da seguimiento. [CC] carga los documentos; no usa esta pantalla. |
| **Precondiciones** | [TD] autenticado. Los procesos que ve son los de su alcance (FSD-UC-019). Las etapas y entregables existen por FSD-UC-025. |
| **Pantalla** | Menú principal **Seguimiento de procesos** → `/reportes/fase` |
| **No incluye** | Aprobar la etapa, enviar la etapa a revisión ni reemplazar la línea de tiempo de `/procesos/{id}` (FSD-UC-025…028). |

[TD] elige una carrera y ve en qué fase está su proceso de acreditación, según la secuencia de autoevaluación ARCU-SUR. Desde ahí revisa los documentos de la fase y registra la retroalimentación.

## Flujo principal

1. [TD] abre **Seguimiento de procesos** en el menú principal, debajo de Gestión procesos.
2. El sistema lista las carreras que tienen un proceso en su alcance. Si hay varias, elige la de estado `ACTIVE`.
3. El sistema muestra la fase actual y la secuencia: inicio, siete etapas metodológicas, hito de preparación externa y cierre del alcance de la guía.
4. La fase actual se elige en este orden: `IN_PROGRESS`, `OBSERVED`, `SUBMITTED_FOR_REVIEW`, `PENDING` o, si ninguna aplica, la última etapa.
5. En **Inicio** el documento a registrar es la carta de solicitud de participación. [TD] puede marcarla completada en la visita, ver la evidencia que [CC] cargó en **Cargar evidencia** y registrar una observación sobre esa misma evidencia (FSD-UC-032). Al enviarla, [CC] la ve en Cargar evidencia, junto al archivo.
6. En cada etapa, [TD] ve los entregables con su estado. **Revisar** abre el archivo cuando el entregable tiene documento. **Retroalimentación** guarda el texto en el entregable y queda visible como seguimiento.

## Criterios de aceptación

```gherkin
# language: es
Característica: Fase del proceso para el técnico

  Escenario: El técnico entra desde el menú principal
    Dado un usuario con rol TD
    Cuando abre el panel
    Entonces ve «Seguimiento de procesos» en el menú principal
    Y al elegirlo llega a /reportes/fase

  Escenario: Otro rol no usa la pantalla
    Dado un usuario con rol JD, CC o EE
    Cuando abre /reportes/fase
    Entonces no permanece en esa pantalla

  Escenario: La fase actual corresponde al proceso de la carrera
    Dado un proceso de la carrera con una etapa en curso
    Cuando el técnico selecciona esa carrera
    Entonces la fase marcada es la etapa en curso
    Y ve las siete etapas de la secuencia ARCU-SUR

  Escenario: Inicio solo pide la carta de solicitud
    Dado el técnico en Seguimiento de procesos
    Cuando mira los documentos de Inicio
    Entonces solo aparece «Carta de solicitud de participación»

  Escenario: Revisar un entregable sin archivo
    Dado un entregable de la etapa sin documento cargado
    Cuando el técnico pulsa Revisar
    Entonces el sistema informa que el coordinador todavía no cargó ese documento

  Escenario: La observación del técnico queda sobre la evidencia del coordinador
    Dado una evidencia que el coordinador cargó en Cargar evidencia
    Cuando el técnico la revisa en Seguimiento de procesos y envía la observación
    Entonces la observación queda asociada a esa evidencia
    Y el coordinador la ve en Cargar evidencia, con el mismo archivo y folio

  Escenario: El técnico registra retroalimentación del entregable
    Dado un entregable de la etapa
    Cuando el técnico escribe una observación de al menos 10 caracteres y la registra
    Entonces el texto queda guardado en el entregable
    Y se muestra como seguimiento sin cambiar el estado de aprobación

  Escenario: Un rol distinto no registra retroalimentación
    Dado un usuario que no es TD
    Cuando intenta guardar la retroalimentación de un entregable
    Entonces el sistema rechaza la operación
```

## Excepciones

| Condición | Respuesta |
| --- | --- |
| Rol distinto de [TD] en `/reportes/fase` | Redirección a su pantalla de inicio |
| Rol distinto de [TD] en la retroalimentación | 403 |
| Carrera sin procesos en el alcance | Mensaje «No hay procesos de acreditación en su alcance.» |
| Etapa o entregable inexistente | 404 |
| Retroalimentación en blanco o menor a 10 caracteres | No se guarda |

## API

| Método | Ruta | Rol |
| --- | --- | --- |
| GET | `/api/v1/processes` | TD, según FSD-UC-019 |
| GET | `/api/v1/processes/{processId}/stages` | TD, según FSD-UC-025 |
| GET | `/api/v1/document-assets?processId=` | TD |
| PUT | `/api/v1/processes/{processId}/stages/{stageId}/deliverables/{deliverableId}/feedback` | TD |

El cuerpo de la retroalimentación es `{ "technicalObservations": "…" }`. La respuesta es el entregable. El estado de aprobación no cambia.
