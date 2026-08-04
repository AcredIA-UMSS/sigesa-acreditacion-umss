---
id: DD-UC-003
title: Plantillas y Proceso CEUB/ARCU-SUR
fsd_uc: FSD-UC-003
prd_refs: PRD-US-023
adrs: []
prompts: [PR-IMPL-003]
release: v2.1
status: Implementado
ultima_actualizacion: "2026-08-03"
---

# DD-UC-003: Plantillas y Proceso CEUB/ARCU-SUR

## 1. Contexto y Alcance

Este feature implementa la capacidad de iniciar un Proceso de Acreditación (`AccreditationProcess`) para una carrera específica a partir de una Plantilla (CEUB o ARCU-SUR). Las plantillas actúan como un molde que contiene un conjunto predefinido de Fases y Subfases.

**Fuera de alcance:** La interfaz de usuario (UI) para la creación/edición dinámica de plantillas por parte de los usuarios finales (las plantillas se inicializan por seeders o endpoints administrativos en esta fase).

## 2. Modelo de Datos y Arquitectura

Se requiere crear/actualizar las siguientes entidades y sus relaciones:

- **`Program` (carrera UMSS)**: Catálogo persistido para seleccionar la carrera al crear un proceso.
  - Campos: `id`, `code`, `name`, `faculty`, `active`.
  - Seed dev: `ProgramSeedDataLoader` (25 carreras).
  - API: `GET /api/v1/programs?q=` para autocomplete.
- **`Template`**: Representa la plantilla base (**solo CEUB o ARCU-SUR** en operación).
- **`TemplatePhase` / `TemplateSubphase`**: Estructura jerárquica de la plantilla.
- **`AccreditationProcess`**: Instancia de un proceso en curso para una carrera.
  - Campos clave: `id`, `career_id`, `template_id`, `status` (ACTIVE, COMPLETED, CANCELLED), `start_date`.
- **`Phase` / `Subphase`**: Instancias clonadas a partir de la plantilla al momento de crear el proceso, vinculadas al `AccreditationProcess`.

**Regla de Negocio Crítica:**
Para garantizar un único proceso activo por carrera, se debe implementar una restricción a nivel de base de datos (Ej: un índice único parcial en PostgreSQL) o una validación robusta en el servicio:
`CREATE UNIQUE INDEX idx_unique_active_process ON accreditation_processes (career_id) WHERE status = 'ACTIVE';`

## 3. Interfaces / API

### `POST /api/v1/processes`

Inicia un nuevo proceso de acreditación basándose en una plantilla.

**Request Payload:**

```json
{
  "career_id": "uuid-carrera",
  "template_id": "uuid-plantilla"
}
```

Respuestas:

201 Created: Proceso creado exitosamente (devuelve el árbol de Fases/Subfases clonadas).

409 Conflict: Si se viola la regla PROCESS_ALREADY_ACTIVE.

404 Not Found: `PROGRAM_NOT_FOUND` si `career_id` no existe en catálogo; `TEMPLATE_NOT_FOUND` si plantilla inexistente o tipo distinto de CEUB/ARCU-SUR.

### `GET /api/v1/programs`

Lista carreras activas. Query opcional `q` filtra por nombre o código (autocomplete UI).

**Response 200:**

```json
[
  { "id": "550e8400-e29b-41d4-a716-446655440000", "code": "INF-SIS", "name": "Ingeniería de Sistemas" }
]
```

## 3.1 Frontend (FSD-UC-003)

- Ruta `/procesos/nuevo` ([JD]).
- Componente `CareerAutocomplete`: búsqueda con debounce contra `GET /programs?q=`.
- Select de plantilla: únicamente **CEUB 2026** y **ARCU-SUR 2026** (IDs seed alineados a `TemplateSeedDataLoader`).

## 4. Impacto en Specs Vivas

FSD.md: Se actualizó el flujo en FSD-UC-003 para reflejar la taxonomía estricta Fase → Subfase y la unicidad del proceso activo.

DTP.md: Actualizado §B.2 con tabla `programs`, API búsqueda y UI autocomplete.

## 5. Seguridad y Permisos

Actor: [JD] (Jefe de Departamento / Administrador).

Autorización: El endpoint POST /api/v1/processes debe requerir un token JWT válido y el rol adecuado (ROLE_JD o equivalente) definido en el módulo de IAM.

## 6. Estrategia de Pruebas

Unitarias:

Validar que el servicio clona correctamente todas las fases y subfases de la plantilla al nuevo proceso.

Validar que el servicio arroja una excepción ProcessAlreadyActiveException si la carrera ya tiene un proceso en estado ACTIVE.

Integración:

Testear el endpoint POST /api/v1/processes simulando una carrera con y sin proceso activo. Verificación del índice único en BD.

## 7. Despliegue y Migraciones

Se requiere un script de migración (Flyway) que:

Cree la tabla `programs` (`V3__programs_catalog.sql`).

Cree las tablas templates, template_phases, template_subphases.

Cree la tabla accreditation_processes, phases, subphases.

Cree el índice único parcial idx_unique_active_process.

Inserte seed de carreras UMSS (`ProgramSeedDataLoader`) y plantillas CEUB/ARCU-SUR (`TemplateSeedDataLoader`).
