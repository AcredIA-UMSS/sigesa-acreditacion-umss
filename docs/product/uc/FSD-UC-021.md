---
id: FSD-UC-021
nombre: Gestión de plantillas normativas (jerarquía N1–N3 + Indicador)
estado: Hecho (Full-Stack v2)
release: v2.0
actor_principal: "[JD]"
trazabilidad_prd: PRD-US-023
modulo: MOD-PROCESS
reglas: FSD-BR-21, FSD-BR-23, FSD-BR-24, FSD-BR-25
ultima_actualizacion: "2026-09-08"
design_doc: DD-UC-021
pr_impl: PR-IMPL-021
nota_implementacion: "API-TPL-08 jerarquía v2 + UI `/admin/plantillas` (tabs v2/legacy); publicación exige indicadores v2 (BR-24); duplicar clona árbol v2"
---

# FSD-UC-021 — Gestión de plantillas normativas (jerarquía N1–N3 + Indicador)

## Contexto

| Campo | Valor |
|-------|-------|
| **Trazabilidad** | PRD-REQ-002, 004 · PRD-US-023 |
| **Design Doc** | [`DD-UC-021`](../../design/DD-UC-021.md) *(actualizar para v2.0)* |
| **Relación** | Complementa [FSD-UC-003](FSD-UC-003.md) (creación de proceso desde plantilla). Las plantillas son el **molde normativo** CEUB/ARCU-SUR antes de instanciar un proceso. |
| **Precondiciones** | [JD] autenticado; comité normativo valida taxonomía antes de marcar plantilla como **publicada** |
| **Pantallas** | `/admin/plantillas` (listado) · `/admin/plantillas/nueva` · `/admin/plantillas/{templateId}` (editor árbol multinivel) |

Permite a **[JD]** crear, consultar, modificar y desactivar **plantillas normativas** con la jerarquía v2.0:

**Modelo evaluador → Nivel 1 → Nivel 2 → Nivel 3 → Indicador**

> **Distinción clave:** esta UC opera sobre entidades de **plantilla** (`AccreditationTemplate`, `TemplateLevel1`, `TemplateLevel2`, `TemplateLevel3`, `TemplateIndicator`). No modifica instancias de un proceso en curso (ver [FSD-UC-022](FSD-UC-022.md)).

## Modelo funcional de plantilla

### Plantilla (raíz)

| Campo | Obl. | Descripción |
|-------|------|-------------|
| `name` | Sí | Nombre visible (ej. «CEUB 2026 — Ingenierías») |
| `description` | No | Resumen del propósito o convocatoria |
| `evaluatorModel` | Sí | `CEUB` \| `ARCU-SUR` |
| `status` | Sí | `DRAFT` \| `PUBLISHED` \| `ARCHIVED` |
| `level1Count` | Derivado | Cantidad de nodos Nivel 1 |
| `indicatorCount` | Derivado | Cantidad total de indicadores en el árbol |

### Nivel 1 (Dimensión / Área)

| Campo | Obl. | Descripción |
|-------|------|-------------|
| `name` | Sí | Nombre del Nivel 1 |
| `order` | Sí | Orden ascendente único dentro de la plantilla |
| `description` | No | Notas operativas |

### Nivel 2 (Componente / Variable)

| Campo | Obl. | Descripción |
|-------|------|-------------|
| `name` | Sí | Nombre del Nivel 2 |
| `order` | Sí | Orden ascendente único dentro del Nivel 1 padre |
| `description` | No | Texto auxiliar |

### Nivel 3 (Criterio / Sub-variable)

| Campo | Obl. | Descripción |
|-------|------|-------------|
| `name` | Sí | Nombre del Nivel 3 |
| `order` | Sí | Orden ascendente único dentro del Nivel 2 padre |
| `description` | No | Texto auxiliar |

### Indicador (hoja verificable)

| Campo | Obl. | Descripción |
|-------|------|-------------|
| `code` | Sí | Código normativo único dentro del Nivel 3 |
| `description` | Sí | Enunciado del indicador |
| `weight` | Sí | Ponderación numérica (≥ 0) |
| `order` | Sí | Orden ascendente único dentro del Nivel 3 |
| `referenceUrl` | Sí | Enlace HTTPS a guía, criterio o recurso normativo |

## Flujo principal — Crear plantilla

1. [JD] accede a `/admin/plantillas/nueva`.
2. Completa **nombre**, **descripción** y **modelo evaluador** (`CEUB` o `ARCU-SUR`).
3. Construye el **árbol normativo**: agrega Nivel 1 → Nivel 2 → Nivel 3 → Indicadores.
4. Por cada indicador, completa **código**, **descripción**, **ponderación** y **`referenceUrl`** obligatorio.
5. Guarda en estado `DRAFT`.
6. Tras validación interna, [JD] publica la plantilla (`status = PUBLISHED`).
7. La plantilla queda disponible en [FSD-UC-003](FSD-UC-003.md) al crear procesos.

## Flujos alternos — Edición y baja

| ID | Acción | Comportamiento |
|----|--------|----------------|
| E1 | Editar plantilla `DRAFT` | CRUD libre del árbol normativo |
| E2 | Editar plantilla `PUBLISHED` | Permitido; **no** altera procesos ya instanciados (FSD-BR-21) |
| E3 | Eliminar indicador en plantilla usada | Soft-delete o archivar versión; procesos existentes conservan snapshot clonado |
| E4 | Archivar plantilla | `ARCHIVED`; no aparece en selector de `/procesos/nuevo` |
| E5 | Duplicar plantilla | Crea copia `DRAFT` con misma estructura (acelerador operativo) |

## Excepciones

| ID | Condición | Respuesta |
|----|-----------|-----------|
| A1 | `referenceUrl` inválida o vacía en indicador | `400 TEMPLATE_INDICATOR_LINK_REQUIRED` |
| A2 | `order` duplicado en mismo contenedor | `400 TEMPLATE_ORDER_CONFLICT` |
| A3 | Plantilla sin al menos 1 indicador al publicar | `400 TEMPLATE_STRUCTURE_INCOMPLETE` |
| A4 | Indicador sin `code` o sin `weight` | `400 TEMPLATE_INDICATOR_INCOMPLETE` |
| A5 | Rol distinto de [JD] | `403 FORBIDDEN_ROLE` |
| A6 | Eliminar plantilla con procesos activos referenciándola | `409 TEMPLATE_IN_USE` — solo archivar |

## Postcondiciones

- Plantilla persistida con conteos `level1Count` / `indicatorCount` coherentes.
- Indicadores publicados tienen enlace de referencia accesible desde UI de detalle de proceso ([FSD-UC-019](FSD-UC-019.md)).
- Nuevos procesos pueden seleccionar plantillas `PUBLISHED`.

## Fuera de alcance (v2.0)

- Versionado automático con diff normativo (roadmap v2.1).
- Importación masiva desde Excel/CSV (UC-018).

## API propuesta (resumen v2.0)

Ver [`api_contracts.md`](../api_contracts.md) § API-TPL-01…08 *(extender para niveles e indicadores)*.

## Escenarios Gherkin

```gherkin
# language: es
@PRD-US-023 @FSD-UC-021 @TC-21
Característica: Gestión de plantillas normativas v2.0

  Escenario: Crear plantilla CEUB con árbol normativo e indicadores
    Dado un [JD] autenticado
    Cuando crea una plantilla "CEUB 2026 Piloto" modelo CEUB
      Y agrega el Nivel 1 "Área académica" con orden 1
      Y bajo Nivel 2 "Variable docente" y Nivel 3 "Sub-variable formación"
      Y agrega el indicador "IND-01" con ponderación 0.15 y enlace "https://duea.umss.edu.bo/guia/ind-01"
    Entonces la plantilla queda en estado DRAFT
    Y el resumen muestra 1 Nivel 1 y 1 indicador

  Escenario: Publicar plantilla completa
    Dado una plantilla DRAFT con al menos un indicador con enlace válido
    Cuando el [JD] la publica
    Entonces el estado pasa a PUBLISHED
    Y aparece en el selector de creación de proceso

  Escenario: Editar plantilla publicada no migra procesos en curso
    Dado una plantilla PUBLISHED usada por un proceso ACTIVE
    Cuando el [JD] agrega un nuevo indicador a la plantilla
    Entonces el proceso ACTIVE conserva la estructura original clonada al crearlo
    Y los nuevos procesos reciben la estructura actualizada

  Escenario: Rechazo por indicador sin enlace
    Dado un [JD] editando una plantilla
    Cuando intenta guardar un indicador sin referenceUrl
    Entonces el sistema responde 400 TEMPLATE_INDICATOR_LINK_REQUIRED
```

## Nota de migración v1.x

UI y API actuales (`TemplatePhase`, `TemplateSubphase`, fases/subfases) permanecen operativas hasta migración. Mapeo documental: **Fase → Nivel 1**, **Subfase → Indicador** (sin N2/N3 en datos legacy).
