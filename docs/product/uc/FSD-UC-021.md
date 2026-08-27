---
id: FSD-UC-021
nombre: Gestión de plantillas normativas (fases y subfases)
estado: Hecho (Full-Stack)
release: v1.0
actor_principal: "[JD]"
trazabilidad_prd: PRD-US-023
modulo: MOD-PROCESS
reglas: FSD-BR-21, FSD-BR-23
ultima_actualizacion: "2026-08-27"
design_doc: DD-UC-021
pr_impl: PR-IMPL-021
---

# FSD-UC-021 — Gestión de plantillas normativas (fases y subfases)

## Contexto

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-002, 004 · PRD-US-023 |
| **Design Doc** | [`DD-UC-021`](../../design/DD-UC-021.md) |
| **Relación** | Complementa [FSD-UC-003](FSD-UC-003.md) (creación de proceso desde plantilla). Las plantillas son el **molde normativo** CEUB/ARCU-SUR antes de instanciar un proceso. |
| **Precondiciones** | [JD] autenticado; comité normativo valida taxonomía antes de marcar plantilla como **publicada** |
| **Pantallas** | `/admin/plantillas` (listado) · `/admin/plantillas/nueva` · `/admin/plantillas/{templateId}` (editor) |

Permite a **[JD]** crear, consultar, modificar y desactivar **plantillas normativas** con taxonomía **Fase → Subfase**. Cada plantilla incluye metadatos descriptivos y, por subfase, un **enlace de referencia** (URL institucional o documento normativo).

> **Distinción clave:** esta UC opera sobre **`Template` / `TemplatePhase` / `TemplateSubphase`**. No modifica instancias de un proceso en curso (ver [FSD-UC-022](FSD-UC-022.md)).

## Modelo funcional de plantilla

| Campo (plantilla) | Obligatorio | Descripción |
|-------------------|-------------|-------------|
| `name` | Sí | Nombre visible (ej. «CEUB 2026 — Ingenierías») |
| `description` | No | Resumen del propósito o convocatoria |
| `type` | Sí | `CEUB` \| `ARCU-SUR` |
| `status` | Sí | `DRAFT` \| `PUBLISHED` \| `ARCHIVED` |
| `phaseCount` | Derivado | Cantidad de fases (calculado) |
| `subphaseCount` | Derivado | Cantidad total de subfases (calculado) |

| Campo (fase) | Obligatorio | Descripción |
|--------------|-------------|-------------|
| `name` | Sí | Nombre de la fase |
| `order` | Sí | Orden ascendente único dentro de la plantilla |
| `description` | No | Notas operativas |

| Campo (subfase) | Obligatorio | Descripción |
|-----------------|-------------|-------------|
| `name` | Sí | Nombre de la subfase |
| `order` | Sí | Orden ascendente único dentro de la fase |
| `referenceUrl` | Sí | Enlace HTTPS a guía, criterio o recurso normativo |
| `description` | No | Texto auxiliar para coordinadores |

## Flujo principal — Crear plantilla

1. [JD] accede a `/admin/plantillas/nueva`.
2. Completa **nombre**, **descripción** y **tipo** (`CEUB` o `ARCU-SUR`).
3. Agrega una o más **fases** con `order` y nombre.
4. Por cada fase, agrega **subfases** con nombre, `order` y **`referenceUrl`** obligatorio.
5. Guarda en estado `DRAFT`.
6. Tras validación interna, [JD] publica la plantilla (`status = PUBLISHED`).
7. La plantilla queda disponible en [FSD-UC-003](FSD-UC-003.md) al crear procesos.

## Flujos alternos — Edición y baja

| ID | Acción | Comportamiento |
|----|--------|----------------|
| E1 | Editar plantilla `DRAFT` | CRUD libre de fases/subfases |
| E2 | Editar plantilla `PUBLISHED` | Permitido; **no** altera procesos ya instanciados (FSD-BR-21) |
| E3 | Eliminar subfase en plantilla usada | Soft-delete o archivar versión; procesos existentes conservan snapshot clonado |
| E4 | Archivar plantilla | `ARCHIVED`; no aparece en selector de `/procesos/nuevo` |
| E5 | Duplicar plantilla | Crea copia `DRAFT` con misma estructura (acelerador operativo) |

## Excepciones

| ID | Condición | Respuesta |
|----|-----------|-----------|
| A1 | `referenceUrl` inválida o vacía | `400 TEMPLATE_SUBPHASE_LINK_REQUIRED` |
| A2 | `order` duplicado en misma fase/plantilla | `400 TEMPLATE_ORDER_CONFLICT` |
| A3 | Plantilla sin al menos 1 fase y 1 subfase al publicar | `400 TEMPLATE_STRUCTURE_INCOMPLETE` |
| A4 | Rol distinto de [JD] | `403 FORBIDDEN_ROLE` |
| A5 | Eliminar plantilla con procesos activos referenciándola | `409 TEMPLATE_IN_USE` — solo archivar |

## Postcondiciones

- Plantilla persistida con conteos `phaseCount` / `subphaseCount` coherentes.
- Subfases publicadas tienen enlace de referencia accesible desde UI de detalle de proceso ([FSD-UC-019](FSD-UC-019.md)).
- Nuevos procesos pueden seleccionar plantillas `PUBLISHED`.

## Fuera de alcance (v1.0)

- Indicadores/criterios/dimensiones (retirados del alcance v1.1; ver glosario v1.1).
- Versionado automático con diff normativo (v1.1 — alineado a plantillas versionadas en ROADMAP).
- Importación masiva desde Excel/CSV.

## API propuesta (resumen)

Ver [`api_contracts.md`](../api_contracts.md) § API-TPL-01…08.

## Escenarios Gherkin

```gherkin
# language: es
@PRD-US-023 @FSD-UC-021 @TC-21
Característica: Gestión de plantillas normativas

  Escenario: Crear plantilla CEUB con fases, subfases y enlaces
    Dado un [JD] autenticado
    Cuando crea una plantilla "CEUB 2026 Piloto" tipo CEUB
      Y agrega la fase "Autoevaluación" con orden 1
      Y agrega la subfase "Diagnóstico institucional" con enlace "https://duea.umss.edu.bo/guia/diagnostico"
    Entonces la plantilla queda en estado DRAFT
    Y el resumen muestra 1 fase y 1 subfase

  Escenario: Publicar plantilla completa
    Dado una plantilla DRAFT con al menos una fase y una subfase con enlace válido
    Cuando el [JD] la publica
    Entonces el estado pasa a PUBLISHED
    Y aparece en el selector de creación de proceso

  Escenario: Editar plantilla publicada no migra procesos en curso
    Dado una plantilla PUBLISHED usada por un proceso ACTIVE
    Cuando el [JD] agrega una nueva subfase a la plantilla
    Entonces el proceso ACTIVE conserva la estructura original clonada al crearlo
    Y los nuevos procesos reciben la estructura actualizada

  Escenario: Rechazo por subfase sin enlace
    Dado un [JD] editando una plantilla
    Cuando intenta guardar una subfase sin referenceUrl
    Entonces el sistema responde 400 TEMPLATE_SUBPHASE_LINK_REQUIRED
```
