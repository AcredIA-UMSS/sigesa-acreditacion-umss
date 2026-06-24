# ADR_001: Versionado append-only de Evidencia normativa

| Campo | Valor |
|-------|-------|
| **Canónico** | [ADR-0001](../../adr/ADR-0001-append-only-evidence-storage.md) |
| **Estado** | **Aceptada** |
| **Fecha** | 2026-05-17 |
| **Alcance** | MOD-EVIDENCE · `evidence_version` · API de carga/descarga |
| **Trazabilidad** | BRD-CST-01 · PRD-REQ-007 · FSD-UC-005 · FSD-BR-02 · NFR-017 |

## Contexto

SIGESA automatiza el ciclo de acreditación CEUB/ARCU-SUR en la UMSS. La **Evidencia** es la prueba documental que un **Indicador** exige en una **Fase** del **Proceso**. Los organismos acreditadores y la DUEA exigen demostrar qué archivo existía en cada momento del dictamen, quién lo cargó y qué versión fue aceptada o rechazada por el **[TD]**. Un error de carga del **[CC]** no puede borrar la huella del intento anterior: la subsanación debe quedar registrada como nueva versión, no como ausencia del registro previo.

El BRD y el FSD establecen explícitamente que no existe eliminación física de Evidencias aprobadas (BRD-CST-01, FSD-BR-02). Patrones habituales en CRUD web — `DELETE` REST, columnas `is_deleted` o `deleted_at`, sobrescritura in-place del blob en almacenamiento — destruyen la trazabilidad que el piloto debe demostrar ante auditores. La skill `sigesa-db-architect-append-only` y el DDL [`ddl_sigesa_append_only.sql`](../ddl_sigesa_append_only.sql) materializan esta restricción a nivel de esquema: `REVOKE DELETE` sobre tablas normativas y FK `ON DELETE RESTRICT` hacia `evidence_version`.

Este ADR define el **modelo de versiones y políticas de API**. La ubicación física del blob de Evidence para cloud v1.0 se decide en [ADR-0013](../../adr/ADR-0013-s3-evidence-blob-storage.md). La **bitácora transversal** de acciones en [ADR_005](ADR_005_audit_log_postgresql.md).

## Alternativas consideradas

| Alternativa | Pros | Contras | Veredicto |
|-------------|------|---------|-----------|
| **A. Tabla `evidence_version` solo INSERT + `supersedes_id`** | Trazabilidad forense; alineado a CEUB; reversible en transacción con metadatos | Mayor uso de disco; consultas deben filtrar versión vigente | **Elegida** |
| **B. Soft-delete (`is_deleted`)** | Familiar para ORMs | Oculta filas a reportes; viola espíritu de auditoría universitaria | Rechazada |
| **C. Sobrescribir blob y fila única** | Menor almacenamiento | Imposible demostrar versión rechazada o hash previo | Rechazada |
| **D. Archivar en cold storage y borrar fila activa** | Ahorro de costo | Rompe integridad referencial del proceso en curso | Rechazada |

## Decisión

Se adopta el modelo **append-only** con las siguientes reglas obligatorias:

1. **Entidad lógica** `evidence` (cabecera por Indicador) y **versiones** en `evidence_version` (una fila por carga o subsanación).
2. Cada nueva carga incrementa `version` y opcionalmente enlaza `supersedes_id` a la versión anterior.
3. El rol de aplicación `sigesa_app` tiene **solo INSERT** (y SELECT/UPDATE acotado a metadatos no destructivos) en `evidence_version`; **REVOKE DELETE**.
4. Tras aprobación normativa (`estado` = `APROBADO` en la versión o en el Indicador según FSD), la API responde `409 EVIDENCE_IMMUTABLE` ante cualquier intento de borrado o reemplazo in-place.
5. Integridad de contenido: hash **SHA-256** persistido por versión (`content_sha256`); cálculo post-escritura del blob (ver ADR_004).
6. Subsanación motivada por **Observación** del [TD]: la versión n+1 referencia `observation_id` cuando aplica (ver [`modelo_datos.md`](../modelo_datos.md) §6).

## Consecuencias

### Positivas

- Cumplimiento demostrable de BRD-CST-01 y NFR-017 ante pares evaluadores.
- El [TD] puede reconstruir la línea temporal de un Indicador sin exportar backups manuales.
- La migración de medio de almacenamiento no exige cambiar el modelo de versiones, solo `storage_key` y el adaptador `BlobStoragePort`.

### Negativas

- Crecimiento monotónico de filas y blobs; requiere política de retención institucional acordada con TI UMSS (no implica DELETE de aprobados).
- Las consultas de “versión actual” deben usar vista o criterio `MAX(version)` / flag derivado; riesgo de bug si el equipo omite el filtro en listados.

### Impacto en actores

| Actor | Efecto |
|-------|--------|
| **[CC]** | Puede cargar nueva versión tras observación; no ve botón “eliminar” en Evidencia aprobada |
| **[TD]** | Dictamen siempre sobre historial completo de versiones |
| **[JD]** | Reportes y auditoría consumen historial sin huecos |

## Validación

- Tests de mutación: `DELETE` en `evidence_version` con rol `sigesa_app` → `permission denied`.
- API: `DELETE /api/v1/evidences/{id}` → `409` si versión aprobada (ver [`api_contracts.md`](../../04_fsd/api_contracts.md)).
- Casos Gherkin PRD-US vinculados a FSD-UC-005 en `docs/04_fsd/gherkin.md`.

## Referencias

- [`docs/05_dti/modelo_datos.md`](../modelo_datos.md) · [`docs/05_dti/ddl_sigesa_append_only.sql`](../ddl_sigesa_append_only.sql)
- [`docs/04_fsd/reglas_negocio.md`](../../04_fsd/reglas_negocio.md) — FSD-BR-02
- Equipo: decisiones de almacenamiento en `team/aylenGonzales/09_dti/adr/ADR-001.md` (mapea a ADR-0004, no a este ADR)
