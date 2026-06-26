---
name: save-prompt-mapping
description: Registra una nueva entrada detallada PM-NNN en el PROMPT_MAPPING.md del Sprint actual cada vez que se ejecute un prompt de implementación (PR-IMPL-NNN), capturando el prompt exacto, los archivos realmente modificados y el resultado de la ejecución. Úsalo al finalizar la implementación asistida por IA para cerrar la trazabilidad de auditoría.
disable-model-invocation: true
---

# Save Prompt Mapping (Por Sprints)

Registra en **`docs/sprints/sprint-<numero>/PROMPT_MAPPING.md`** una entrada **`PM-NNN`** detallada por cada ejecución de un prompt de implementación (`PR-IMPL-NNN`). Es el registro append-only de auditoría profunda: *qué se pidió, qué se tocó y qué salió*.

## Invocación

```text
@save-prompt-mapping sprint=<numero> pr=<PR-IMPL-NNN> [estado=<borrador|en_progreso|completado|bloqueado|fallido>] [solicitante="<nombre>"]
```

- `sprint`: Número del sprint actual (ej. `1`, `02`). **Obligatorio**.
- `pr`: ID del prompt de implementación ejecutado (ej. `PR-IMPL-005`). **Obligatorio**.
- `estado`: estado final de la ejecución. Por defecto `completado` si no se indica.
- `solicitante`: persona o grupo que disparó la tarea. Si se omite, inferir del contexto o marcar `desconocido`.

## Archivos de referencia

- Registro vivo (destino): `docs/sprints/sprint-<numero>/PROMPT_MAPPING.md` (crear ruta si no existe).
- Prompt de implementación: `docs/prompts/impl/PR-IMPL-NNN.md`
- Design doc asociado: `docs/design/DD-UC-NNN.md`
- Caso de uso: `docs/product/uc/FSD-UC-NNN.md`
- DTP (changelog complementario): `docs/product/DTP.md`

## Principios

- **Organización por Sprint:** Toda la auditoría se guarda en la carpeta del sprint indicado. Si la carpeta o el archivo no existen, el agente DEBE crearlos con una tabla resumen básica al inicio.
- **Append-only:** solo **añadir** entradas al final del archivo del sprint. **Nunca** editar, reordenar ni borrar entradas `PM-*` previas.
- **ID secuencial por archivo:** `PM-001`, `PM-002`, … El siguiente ID es `max(PM existentes en este sprint) + 1`. Si el archivo es nuevo, empezar en `PM-001`.
- **Prompt exacto, sin paráfrasis:** el campo *Prompt usado exacto* debe ser copia literal del texto enviado al agente. No resumir ni reescribir.
- **Archivos verificados en repo:** listar solo archivos **realmente** creados o modificados confirmados vía `git status`.

## Flujo

- [ ] Paso 1: Resolver PR-IMPL-NNN y su cadena de trazabilidad.
- [ ] Paso 2: Ubicar/Crear `docs/sprints/sprint-<numero>/PROMPT_MAPPING.md`.
- [ ] Paso 3: Asignar el ID PM-NNN (siguiente correlativo del sprint).
- [ ] Paso 4: Recolectar metadatos de la ejecución y verificar git.
- [ ] Paso 5: Redactar la entrada con la plantilla PM-NNN detallada.
- [ ] Paso 6: Append en el archivo del sprint y validar.

### Paso 1 — Resolver PR-IMPL-NNN

- Localizar `docs/prompts/impl/PR-IMPL-NNN.md`.
- Extraer `feature_asociado` (DD-UC), objetivo, contexto y límites.
- Enlazar hacia atrás: `PR-IMPL-NNN → DD-UC-NNN → FSD-UC-NNN`.

### Paso 2 y 3 — Ubicación e ID correlativo PM-NNN

- Navegar a `docs/sprints/sprint-<numero>/PROMPT_MAPPING.md`.
- Si el archivo NO existe, inicializarlo con este encabezado:

```markdown
# Mapeo de Prompts - Sprint <numero>

| ID Mapeo | PR-IMPL | Design Doc | FSD Asociado | Descripción de la Tarea |
| :--- | :--- | :--- | :--- | :--- |
```

- Buscar todas las entradas previas `PM-\d{3}` en ESE archivo.
- Asignar el **siguiente número libre** (ej. si la última es `PM-004`, la nueva es `PM-005`).

### Paso 4 — Metadatos y Verificación de Repo

Recolectar (Fecha, Hora, Solicitante, Modelo, Tarea, Objetivo, etc.) copiando exactamente el cuerpo útil de `PR-IMPL-NNN.md`.

Ejecutar:

```bash
git status --short
```

para listar solo rutas que aparezcan en la salida de git (separando generados `A/??` vs modificados `M`).

### Paso 5 — Redactar entrada PM-NNN

Usar **exactamente** esta plantilla al final del archivo del sprint:

````markdown
---

## PM-NNN

| Campo | Valor |
|---|---|
| **ID** | PM-NNN |
| **Fecha** | YYYY-MM-DD |
| **Hora** | HH:MM |
| **Solicitante** | … |
| **Agente/Entorno** | … |
| **Modelo** | … |
| **Tarea** | … |
| **Objetivo** | … |
| **Contexto** | … |
| **PR-IMPL vinculado** | PR-IMPL-NNN |
| **DD-UC vinculado** | DD-UC-NNN |
| **FSD-UC vinculado** | FSD-UC-NNN |
| **Estado** | completado \| en_progreso \| … |

### Prompt usado exacto

```text
<texto literal del prompt — sin paráfrasis>
```

### Entradas auxiliares

- `docs/design/DD-UC-NNN.md`
- ...

### Archivos generados o modificados

| Acción | Ruta |
|---|---|
| generado | `src/...` |
| modificado | `docs/...` |

### Cambios realizados

- ...

### Validación ejecutada

- [ ] `mvn test` — resultado: ...
- [ ] `pnpm run lint` — resultado: ...

### Resultado obtenido

...

### Próximos pasos

- [ ] ...

### Paso 6 — Append y validar

- Añadir la nueva sección `## PM-NNN` al final del archivo del sprint correspondiente.
- Añadir una fila nueva al final de la tabla resumen superior del archivo:

```markdown
| PM-NNN | PR-IMPL-NNN | DD-UC-NNN | FSD-UC-NNN | <título corto> |
```

- Sugerir `@dtp-sync` en el chat si hubo cambios estructurales.

## Salida (reporte en el chat)

- ID asignado: `PM-NNN` y ruta del Sprint afectada.
- PR-IMPL, DD-UC y FSD-UC enlazados.
- Cantidad de archivos verificados (generados / modificados).