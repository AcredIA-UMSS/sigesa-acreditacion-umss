# ADR_012: Modelo DDL para historial de estados de Indicator (Append-Only)

| Campo | Valor |
|-------|-------|
| **Canonico** | [ADR-0012](../../adr/ADR-0012-indicator-state-history-append-only.md) |
| **Estado** | **Aceptada** |
| **Fecha** | 2026-05-25 |
| **Alcance** | Tablas `evidence`, `evidence_version`, `observation`, `indicator_state_history` |
| **Trazabilidad** | ADR_001 Append-Only Evidence · `04_state_machine.md` §2 · PC-SIG-14 REGLA 1 · `hybrid_architecture.md` §3.1 ER |
| **Supersede** | Extiende (no reemplaza) el DDL existente en `docs/05_dti/ddl_sigesa_append_only.sql` |

## Contexto

El DDL base en `docs/05_dti/ddl_sigesa_append_only.sql` estableció el primer esquema del monolito modular (ADR_002): tablas de Evidence con versionado `supersedes_id`, bitacora de acciones en `audit_log` (ADR_005) y la taxonomia normativa CEUB/ARCU-SUR (ADR_008). La arquitectura cloud v1.0 documentada en `hybrid_architecture.md` eleva un requisito a regla obligatoria: el historial de transiciones de estado del Indicator debe ser una tabla de primera clase, no un campo `status` actualizable en la tabla `indicator`.

El modelo anterior del monolito almacenaba `indicator.status` como una columna actualizable. Ese diseño queda clasificado como antecedente legacy y **no es apto** para la arquitectura v1.0 cloud. En adelante, Audit Service necesita:

1. Leer el estado actual del Indicator sin depender de una transaccion compartida con Evidence Service.
2. Escribir una nueva transicion sin ejecutar `UPDATE`, para garantizar el patron Append-Only a nivel de persistencia (no solo a nivel de aplicacion).
3. Reconstruir toda la secuencia de estados para un Indicator dado, incluyendo actor responsable y timestamp preciso, sin recurrir a `audit_log`.

Este ADR define el esquema DDL de la tabla `indicator_state_history` y la vista `indicator_current_view`, que reemplaza la columna `status` en la lectura del estado actual.

## Alternativas consideradas

| Alternativa | Pros | Contras | Veredicto |
|-------------|------|---------|-----------|
| **A. Tabla `indicator_state_history` + vista `indicator_current_view`** | Append-Only estricto; auditoria completa; sin UPDATE en tabla de estado | Consultas de estado actual requieren vista o subquery; mayor uso de almacenamiento | **Elegida** |
| **B. Columna `status` actualizable en `indicator` con trigger de auditoria** | Simple para consultas de estado actual; compatible con ORMs convencionales | El UPDATE destruye el estado anterior a nivel de fila; el trigger es una solucion proximal, no estructural | Rechazada |
| **C. Event Sourcing completo: reconstruir estado a partir de eventos** | Historial perfecto; sin tabla de estado | Complejidad de rehydration; rendimiento de lectura dependiente del numero de eventos por Indicator | Rechazada para v1; considerar en v3+ |

## Decision

Se adopta el esquema **tabla de historial + vista de estado actual**, con las siguientes especificaciones de DDL:

### Esquema conceptual de tablas criticas

**Tabla `indicator_state_history` (transiciones de estado — INSERT only):**

```
indicator_state_history
  id              UUID PRIMARY KEY
  indicator_id    UUID NOT NULL REFERENCES indicator(id)
  previous_state  TEXT NOT NULL  -- estado de origen
  new_state       TEXT NOT NULL  -- estado de destino (PENDIENTE|SUBIDO|OBSERVADO|SUBSANADO|APROBADO)
  created_by_role TEXT NOT NULL  -- ProgramCoordinator | DueaTechnician | System
  created_by_id   UUID NOT NULL  -- FK a users(id)
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
  correlation_id  UUID NOT NULL  -- trazabilidad con evento de dominio que origino la transicion

CONSTRAINT valid_states CHECK (new_state IN ('PENDIENTE','SUBIDO','OBSERVADO','SUBSANADO','APROBADO'))
INDEX idx_ish_indicator_at ON indicator_state_history (indicator_id, created_at DESC)
```

**Vista `indicator_current_view` (estado actual derivado — sin UPDATE):**

```sql
CREATE VIEW indicator_current_view AS
  SELECT DISTINCT ON (indicator_id)
    indicator_id,
    new_state AS current_state,
    created_by_role AS last_changed_by_role,
    created_at AS last_changed_at
  FROM indicator_state_history
  ORDER BY indicator_id, created_at DESC;
```

**Tabla `evidence` — columnas de auditoria obligatorias (Append-Only, extiende ADR_001):**

```
evidence
  id              UUID PRIMARY KEY
  indicator_id    UUID NOT NULL REFERENCES indicator(id)
  version         INT NOT NULL DEFAULT 1
  supersedes_id   UUID NULL REFERENCES evidence(id)
  observation_id  UUID NULL REFERENCES observation(id)
  s3_key          TEXT NOT NULL
  content_sha256  TEXT NOT NULL  -- integridad del blob
  created_by_role TEXT NOT NULL
  created_by_id   UUID NOT NULL
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now()

UNIQUE (indicator_id, version)
```

**Tabla `observation` — registro de rechazos y aprobaciones (Append-Only):**

```
observation
  id                    UUID PRIMARY KEY
  indicator_id          UUID NOT NULL REFERENCES indicator(id)
  evidence_id           UUID NOT NULL REFERENCES evidence(id)
  linked_observation_id UUID NULL REFERENCES observation(id)  -- cadena de respuestas
  reason                TEXT NOT NULL
  created_by_role       TEXT NOT NULL  -- siempre DueaTechnician para observaciones
  created_by_id         UUID NOT NULL
  created_at            TIMESTAMPTZ NOT NULL DEFAULT now()
```

**Tabla `processed_events` — idempotencia de handlers (Append-Only):**

```
processed_events
  correlation_id  UUID PRIMARY KEY
  event_type      TEXT NOT NULL
  processed_by    TEXT NOT NULL  -- nombre del servicio que proceso el evento
  processed_at    TIMESTAMPTZ NOT NULL DEFAULT now()
```

### Politica de permisos de BD

El rol de aplicacion `sigesa_app` recibe los siguientes permisos sobre las tablas criticas:

```
GRANT SELECT, INSERT ON indicator_state_history TO sigesa_app;
REVOKE UPDATE, DELETE ON indicator_state_history FROM sigesa_app;

GRANT SELECT, INSERT ON evidence TO sigesa_app;
REVOKE UPDATE, DELETE ON evidence FROM sigesa_app;

GRANT SELECT, INSERT ON observation TO sigesa_app;
REVOKE UPDATE, DELETE ON observation FROM sigesa_app;

GRANT SELECT, INSERT ON processed_events TO sigesa_app;
REVOKE UPDATE, DELETE ON processed_events FROM sigesa_app;
```

Este `REVOKE` a nivel de BD es la segunda capa de defensa tras la prohibicion a nivel de aplicacion (ADR_001 §Decision regla 3). Un bug en el codigo que intente `UPDATE indicator_state_history` recibira `permission denied` de PostgreSQL antes de llegar a los datos.

### Consulta de referencia — Hard Constraint de cierre de Phase

La query que Orchestration Service ejecuta para verificar la Hard Constraint de `04_state_machine.md` §3, dentro de una transaccion `REPEATABLE READ`:

```sql
SELECT
  COUNT(*) FILTER (WHERE v.current_state = 'APROBADO') AS aprobados,
  COUNT(*) AS total
FROM indicator i
JOIN indicator_current_view v ON v.indicator_id = i.id
WHERE i.phase_id = $1;
-- Condicion de cierre: aprobados == total
```

## Consecuencias

### Positivas

- El historial completo de transiciones de un Indicator es auditoriable directamente en BD sin recurrir a `audit_log` general (complementa ADR_005, no lo duplica).
- El `REVOKE DELETE` a nivel de BD garantiza Append-Only incluso ante bugs de aplicacion o migraciones mal ejecutadas.
- La vista `indicator_current_view` estandariza la consulta de estado actual: todos los servicios leen el mismo dato sin discrepancias de logica de filtrado.

### Negativas

- La tabla `indicator_state_history` crece de forma monotonica: cada ciclo de acreditacion de una carrera puede generar entre 50 y 200 transiciones. Para 50 carreras en 10 ciclos, el orden de magnitud es 100.000 filas, manejable con el indice `idx_ish_indicator_at`.
- Los ORM convencionales (TypeORM, Prisma) requieren configuracion especifica para trabajar con vistas como `indicator_current_view`; el equipo debe definir el patron de acceso (raw query o view entity).

### Impacto en actores

| Actor | Efecto |
|-------|--------|
| **[CC]** | Sin impacto visible; el estado del Indicator se muestra igual que antes |
| **[TD]** | Puede consultar el historial completo de transiciones de cualquier Indicator desde el dashboard |
| **[JD]** | Reportes de auditoria pueden mostrar linea temporal de estados sin depender de backups de BD |

## Validacion

- Test de permiso: ejecutar `UPDATE indicator_state_history SET new_state = 'APROBADO' WHERE id = X` con rol `sigesa_app` → debe retornar `ERROR: permission denied for table indicator_state_history`.
- Test de vista: insertar 3 transiciones para el mismo Indicator; verificar que `indicator_current_view` retorna exactamente 1 fila con el `new_state` del INSERT mas reciente.
- Test de Hard Constraint: crear Phase con 3 Indicators; aprobar 2; ejecutar query de cierre; verificar `aprobados=2, total=3`; aprobar el tercero; ejecutar de nuevo; verificar `aprobados=3, total=3`.

## Referencias

- [`docs/05_dti/hybrid_architecture.md`](../hybrid_architecture.md) §3.1 Diagrama ER
- [`docs/05_dti/adrs/ADR_001_append_only_evidencia.md`](ADR_001_append_only_evidencia.md)
- [`docs/05_dti/adrs/ADR_005_audit_log_postgresql.md`](ADR_005_audit_log_postgresql.md)
- [`docs/05_dti/adrs/ADR_011_sqs_fifo_phase_closure.md`](ADR_011_sqs_fifo_phase_closure.md)
- [`team/alexAlvarez/docs/context/04_state_machine.md`](../../../team/alexAlvarez/docs/context/04_state_machine.md) §3
