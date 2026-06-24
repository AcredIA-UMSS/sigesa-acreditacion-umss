---
name: save-prompt-mapping
description: Registra una nueva entrada PM-NNN en docs/PROMPT_MAPPING.md cada vez que se ejecute un prompt de implementación (PR-IMPL-NNN), capturando el prompt exacto, los archivos realmente modificados y el resultado de la ejecución. Úsalo al finalizar la implementación asistida por IA de un feature, tras correr @feature-design-doc o antes de @dtp-sync, o cuando el docente/grupo pida auditar o cerrar la trazabilidad de un PR-IMPL.
disable-model-invocation: true
---

# Save Prompt Mapping

Registra en **`docs/PROMPT_MAPPING.md`** una entrada **`PM-NNN`** por cada ejecución de un prompt de implementación (`PR-IMPL-NNN`). Es el registro append-only de *qué se pidió, qué se tocó y qué salió*.

## Invocación

```
@save-prompt-mapping <PR-IMPL-NNN> [estado=<borrador|en_progreso|completado|bloqueado|fallido>] [solicitante="<nombre>"]
```

- `<PR-IMPL-NNN>`: prompt de implementación ejecutado. **Obligatorio** (sin PR-IMPL no hay entrada PM válida).
- `estado`: estado final de la ejecución. Por defecto `completado` si no se indica.
- `solicitante`: persona o grupo que disparó la tarea (docente, integrante, equipo). Si se omite, inferir del contexto o marcar `desconocido`.

## Archivos de referencia

- Registro vivo (destino): [`docs/PROMPT_MAPPING.md`](../../../docs/PROMPT_MAPPING.md)
- Prompt de implementación: `docs/prompts/impl/PR-IMPL-NNN.md`
- Design doc asociado: `docs/design/DD-UC-NNN.md`
- Caso de uso: `docs/product/uc/FSD-UC-NNN.md`
- Modelo documental: [`docs/MODELO_DOCUMENTAL_IMPLEMENTACION.md`](../../../docs/MODELO_DOCUMENTAL_IMPLEMENTACION.md)
- DTP (changelog complementario): [`docs/product/DTP.md`](../../../docs/product/DTP.md)

## Principios

- **Append-only:** solo **añadir** entradas al final de `docs/PROMPT_MAPPING.md`. **Nunca** editar, reordenar ni borrar entradas `PM-*` previas.
- **ID secuencial sin saltos:** `PM-001`, `PM-002`, … El siguiente ID es `max(PM existentes) + 1`. Si no hay entradas, empezar en `PM-001`.
- **Prompt exacto, sin paráfrasis:** el campo *Prompt usado exacto* debe ser copia literal del texto enviado al agente (desde `PR-IMPL-NNN.md` y/o el mensaje de chat que lo disparó). No resumir ni reescribir.
- **Archivos verificados en repo:** listar solo archivos **realmente** creados o modificados. Confirmar con `git status` y/o `git diff --name-status` antes de registrar.
- **No inventar:** si falta validación, resultado o contexto, marcarlo explícitamente (`pendiente`, `no ejecutado`, `N/A`) en lugar de suponer.
- **No tocar el baseline:** `docs/baseline/` permanece intocable.

## Flujo

```
- [ ] Paso 1: Resolver PR-IMPL-NNN y su cadena de trazabilidad
- [ ] Paso 2: Asignar el ID PM-NNN (siguiente correlativo)
- [ ] Paso 3: Recolectar metadatos de la ejecución
- [ ] Paso 4: Verificar archivos modificados en el repositorio
- [ ] Paso 5: Redactar la entrada con la plantilla PM-NNN
- [ ] Paso 6: Append en PROMPT_MAPPING.md y validar
```

### Paso 1 — Resolver PR-IMPL-NNN

- Localizar `docs/prompts/impl/PR-IMPL-NNN.md`. Si no existe, **detener** y pedir crearlo primero (p. ej. con `@feature-design-doc`).
- Extraer del frontmatter y del cuerpo: `feature_asociado` (DD-UC), objetivo, contexto y límites.
- Enlazar hacia atrás: `PR-IMPL-NNN → DD-UC-NNN → FSD-UC-NNN`. Reportar gaps si falta algún eslabón.

### Paso 2 — ID correlativo PM-NNN

- Leer `docs/PROMPT_MAPPING.md` completo.
- Buscar todas las entradas con patrón `PM-\d{3}` (o `PM-NNN` en encabezados).
- Asignar el **siguiente número libre** sin saltos. Ejemplo: si la última es `PM-004`, la nueva es `PM-005`.
- **Prohibido** reutilizar un ID ya usado.

### Paso 3 — Recolectar metadatos

Completar estos campos antes de escribir la entrada:

| Campo | Fuente |
|---|---|
| **Fecha** | Fecha local de cierre de la ejecución (`YYYY-MM-DD`) |
| **Hora** | Hora local de cierre (`HH:MM`, timezone si se conoce) |
| **Solicitante** | Parámetro `solicitante` o contexto del chat |
| **Agente/Entorno** | Cursor / IDE / CI (lo que aplique) |
| **Modelo** | Modelo del agente que ejecutó el prompt (si se conoce) |
| **Tarea** | Título corto derivado de `PR-IMPL-NNN` |
| **Objetivo** | §1 Propósito y Objetivo del PR-IMPL |
| **Contexto** | Design doc, FSD-UC, ADRs, restricciones relevantes |
| **Prompt usado exacto** | Texto literal del prompt (ver regla abajo) |
| **Entradas auxiliares** | Archivos leídos como contexto (DD, FSD, ADR, skills) |
| **Validación ejecutada** | Comandos/tests corridos (`mvn test`, linter, etc.) |
| **Resultado obtenido** | Qué se logró concretamente |
| **Estado** | `borrador` \| `en_progreso` \| `completado` \| `bloqueado` \| `fallido` |
| **Riesgos/observaciones** | Deuda, gaps, bloqueos, deltas vs diseño |
| **Lecciones/reuso** | Patrones reutilizables o anti-patrones detectados |
| **Próximos pasos** | Qué falta (tests, DTP sync, ADR, PR, etc.) |

**Regla — Prompt usado exacto:**

1. Si el prompt vive en `docs/prompts/impl/PR-IMPL-NNN.md`, copiar **todo el contenido útil** (frontmatter + secciones 1–5) sin reformular.
2. Si hubo instrucciones adicionales en el chat que modificaron el alcance, **concatenar** esas instrucciones literales después del bloque del archivo, separadas por `---`.
3. No sustituir citas por descripciones del tipo «se pidió implementar X».

### Paso 4 — Verificar archivos en el repo

Ejecutar **antes** de registrar *Archivos generados o modificados*:

```bash
git status --short
git diff --name-status
git diff --cached --name-status   # si hay staging
```

Reglas:

- Incluir **solo** rutas que aparezcan en la salida de git (o archivos nuevos no trackeados confirmados).
- Separar en **generados** vs **modificados** cuando sea posible (`A`/`??` vs `M`).
- **No listar** archivos «planificados» o «esperados» que no existan en el working tree.
- Excluir artefactos de build (`target/`, `build/`, `.class`) salvo que el prompt lo exija explícitamente.

Documentar también **Cambios realizados**: resumen breve por archivo o por capa (dominio, aplicación, adaptadores, tests, docs).

### Paso 5 — Redactar entrada PM-NNN

Usar **exactamente** esta plantilla (la definida en [`docs/PROMPT_MAPPING.md`](../../../docs/PROMPT_MAPPING.md)). Copiarla al final del archivo sin alterar entradas anteriores:

```markdown
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

```
<texto literal del prompt — sin paráfrasis>
```

### Entradas auxiliares

- `docs/design/DD-UC-NNN.md`
- …

### Archivos generados o modificados

| Acción | Ruta |
|---|---|
| generado | `src/...` |
| modificado | `docs/...` |

### Cambios realizados

- …

### Validación ejecutada

- [ ] `mvn test` — resultado: …
- [ ] …

### Resultado obtenido

…

### Riesgos / observaciones

…

### Lecciones / reuso

…

### Próximos pasos

- [ ] …
```

Sustituir `NNN` por el correlativo asignado en el Paso 2.

### Paso 6 — Append y validar

- Abrir `docs/PROMPT_MAPPING.md` y **añadir** la nueva sección `## PM-NNN` **al final**.
- **No modificar** filas, tablas ni secciones de entradas previas.
- Validar la cadena: `FSD-UC → DD-UC → PR-IMPL → PM-NNN → (archivos en repo)`.
- Si la tabla resumen del inicio del archivo existe, **opcionalmente** añadir **una fila nueva** al final de esa tabla (append-only); no reescribir filas existentes:

  `| PR-IMPL-NNN | DD-UC-NNN | FSD-UC-NNN | <título corto> | PM-NNN |`

- Sugerir `@dtp-sync` si hubo cambios de código no reflejados aún en `docs/product/DTP.md`.

## Salida (reporte en el chat)

- ID asignado: `PM-NNN`.
- PR-IMPL, DD-UC y FSD-UC enlazados.
- Cantidad de archivos verificados (generados / modificados).
- Estado final y validaciones ejecutadas (o pendientes).
- Gaps de trazabilidad detectados.
- Recordatorio: entrada append-only; entradas previas intactas.
