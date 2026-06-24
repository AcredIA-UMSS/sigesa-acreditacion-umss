# ADR_005: Bitácora append-only en PostgreSQL

| Campo | Valor |
|-------|-------|
| **Canónico** | [ADR-0005](../../adr/ADR-0005-audit-log-append-only-postgresql.md) |
| **Origen equipo** | `team/aylenGonzales/09_dti/adr/ADR-002.md` |

## Metadatos

| Campo | Valor |
|-------|-------|
| Número | `0002` |
| Título | Log de auditoría como tabla append-only en PostgreSQL con REVOKE DELETE/UPDATE |
| Fecha | 14/05/2026 |
| Autor(es) | Equipo AcredIA |
| Estado | **Aceptada** |
| Alcance | MOD-09 — Log de auditoría; transversal a todos los módulos |
| Stakeholders consultados | Tech Lead AcredIA · @ArchAgent · Jefa DUEA |

---

## 1. Contexto

AcredIA / SIGESA requiere un registro inmutable de **todas las acciones** del sistema (carga de evidencias, aprobaciones, rechazos, avance de fase, generación de reportes, login/logout) para satisfacer los requisitos de trazabilidad de los organismos acreditadores CEUB y ARCU-SUR, que exigen evidencia auditable por pares evaluadores internacionales.

La inmutabilidad es una restricción de negocio dura (RBN-07, BR-009): ningún usuario —incluyendo la Jefatura DUEA— puede modificar o eliminar registros del log una vez escritos. Esta restricción es verificable técnicamente y debe ser demostrable ante auditores externos.

**Restricciones relevantes:**
- El stack elegido incluye PostgreSQL 16 como base de datos principal (ADR-0003 en proceso).
- El log debe registrar: `usuario_id`, `accion`, `entidad_tipo`, `entidad_id`, `detalle` (JSONB), `ip_origen`, `fecha_hora`.
- 100% de eventos críticos deben estar cubiertos (NFR-004, NFR-013).
- El rol de aplicación `sigesa_app` no puede tener permisos de DELETE ni UPDATE sobre la tabla de log (RBN-07).
- El volumen esperado es moderado en v1.0 (estimado < 1M filas/año en piloto UMSS).

**Fuerzas en tensión:**
- **Simplicidad vs. robustez**: una tabla PostgreSQL con REVOKE es simple y suficiente para el piloto, pero no es un sistema de auditoría enterprise.
- **Integración vs. externalización**: mantener el log en la misma BD reduce la complejidad operativa; externalizarlo (ELK, OpenTelemetry) añade infraestructura innecesaria en v1.0.
- **Rendimiento vs. trazabilidad**: un log exhaustivo en la misma BD puede generar contención de escritura bajo carga alta, aunque con el volumen esperado en UMSS esto no es un riesgo real en v1.0.

---

## 2. Alternativas consideradas

| Alternativa | Pros | Contras | Costo aproximado |
|-------------|------|---------|-----------------|
| **A. Tabla append-only en PostgreSQL con REVOKE DELETE/UPDATE** | Sin infraestructura adicional; inmutabilidad técnica demostrable con `REVOKE`; JSONB para detalles variables; índices GIN para búsqueda eficiente; respaldo integrado con `pg_dump` | No es un sistema de auditoría enterprise; si el rol `postgres` es comprometido, los registros son vulnerables | $0 adicional |
| **B. Sistema de logging externo (ELK Stack / OpenSearch)** | Logs distribuidos; dashboards Kibana nativos; escalabilidad horizontal | Agrega 3 servicios (Elasticsearch, Logstash, Kibana) al `docker-compose`; complejidad de mantenimiento desproporcional para el piloto; costo de infraestructura | USD 0 self-hosted pero ~40h de configuración |
| **C. Archivo de log en disco (texto plano / NDJSON)** | Máxima simplicidad; sin dependencias | Sin indexación; búsqueda O(n); sin garantía de inmutabilidad real; no consultable desde la API; imposible correlacionar con entidades de BD | $0 pero inutilizable para auditoría |
| **D. Servicio cloud de auditoría (AWS CloudTrail, Datadog)** | Inmutabilidad garantizada por el proveedor; dashboards listos; retención configurable | Costo OPEX; datos institucionales salen del servidor UMSS (posible restricción de política TI); dependencia de terceros | USD 50–200/mes |

---

## 3. Decisión

> **Elegimos la alternativa A: tabla `LOG_AUDITORIA` append-only en PostgreSQL 16, con inmutabilidad reforzada mediante `REVOKE DELETE, UPDATE ON LOG_AUDITORIA FROM sigesa_app`.**

PostgreSQL 16 ya es la base de datos principal del sistema (SA en FSD §2.3); agregar una tabla con restricciones de permisos no introduce ninguna dependencia nueva. La inmutabilidad técnica es demostrable ante auditores externos ejecutando `\z LOG_AUDITORIA` en psql. El campo `detalle JSONB` permite almacenar contexto variable por tipo de evento sin esquema rígido. Los índices `btree` sobre `(usuario_id)`, `(entidad_tipo, entidad_id)` y `(fecha_hora DESC)` garantizan consultas de auditoría eficientes. El volumen esperado (< 1M filas/año) es trivial para PostgreSQL 16.

Las alternativas B y D agregan complejidad y costo desproporcionados para un piloto universitario. La alternativa C no es auditable y queda descartada.

---

## 4. Consecuencias

### 4.1 Positivas
- Inmutabilidad técnica demostrable: cualquier auditor puede ejecutar `\z LOG_AUDITORIA` y verificar que `sigesa_app` no tiene DELETE ni UPDATE.
- Cero infraestructura adicional: el log vive en la misma BD respaldada diariamente (MOD-12).
- El campo `detalle JSONB` permite enriquecer los registros por tipo de evento sin migraciones de esquema.
- Consultas de auditoría filtradas por actor, entidad o rango de fechas con rendimiento adecuado para el volumen esperado.

### 4.2 Negativas / costos
- Si el superusuario `postgres` es comprometido, los registros pueden ser alterados. Mitigación: acceso SSH al servidor restringido a @ArchAgent y Tech Lead.
- Contención de escritura posible si el volumen de eventos supera 10K/min; para el piloto UMSS esto no es un riesgo real.
- En v2.0, si se requiere retención de logs por > 5 años, habrá que implementar particionamiento por rango de fechas o migrar a solución especializada.

### 4.3 Neutras / observables
- La tabla usa `BIGSERIAL` para el `id`, anticipando volúmenes altos a largo plazo.
- El `pg_dump` diario incluye `LOG_AUDITORIA` automáticamente.
- Los tests de mutación (TC-006) verifican que `UPDATE/DELETE` en `LOG_AUDITORIA` lanzan `ERROR: permission denied`.

---

## 5. Impacto en el sistema

- **Código**: `T-11` (FSD) — implementar `LOG_AUDITORIA` con DDL de REVOKE. Todo módulo que genere un evento crítico debe insertar en `LOG_AUDITORIA` dentro de la misma transacción de negocio o inmediatamente después con manejo de error.
- **Operaciones**: el script de respaldo diario (MOD-12) incluye `LOG_AUDITORIA` automáticamente via `pg_dump`. No requiere configuración adicional.
- **Seguridad**: el acceso SSH al servidor debe restringirse a roles autorizados (Tech Lead + @ArchAgent). El rol `sigesa_app` tiene solo INSERT sobre `LOG_AUDITORIA`.
- **Equipo**: no requiere habilidades adicionales; PostgreSQL RBAC es conocido por el equipo.
- **Costo**: $0 adicional.

---

## 6. Plan de reversión

**Señales de que la decisión fue incorrecta:**
- El volumen de logs supera los 5M de filas y las consultas de auditoría superan los 5 s de respuesta a pesar de los índices.
- TI UMSS o CEUB exigen retención de logs con garantía criptográfica (firma digital de cada fila).
- Se detecta una brecha de seguridad que compromete el superusuario `postgres`.

**Costo estimado de revertir:** bajo-medio. Los datos del log son exportables a NDJSON con `COPY TO` y pueden ingestarse en ELK u OpenSearch. Se estima 1 sprint de trabajo para migrar la infraestructura de logs sin afectar el resto del sistema.

**Plan B:** Agregar particionamiento por rango de fechas (`PARTITION BY RANGE (fecha_hora)`) para mantener el rendimiento si el volumen crece inesperadamente, antes de migrar a solución externa.

---

## 7. Validación

- **TC-006** (plan de pruebas FSD §12.2): verificar que `UPDATE/DELETE` sobre `LOG_AUDITORIA` con el rol `sigesa_app` lanza `ERROR: permission denied`.
- **NFR-004** (no repudio): ≥ 95% de eventos críticos en `LOG_AUDITORIA` con actor, timestamp y entidad — medición con test de integración específico.
- **NFR-012** (integridad): 100% de intentos de DELETE/UPDATE bloqueados — verificación con tests de mutación en BD.
- **Responsable**: @ArchAgent + @QaAgent.
- **Plazo**: sprint de QA previo al despliegue piloto Q3 2026.

---

## 8. Referencias

- FSD v1.0 AcredIA/SIGESA — §5 (RBN-07), §6.2 (DDL de `LOG_AUDITORIA`), §10 (NFR-004, NFR-012), §12.2 (TC-006), T-11.
- PRD v1.0 — PRD-REQ-011.
- BRD v2.0 — BR-009, RB-04.
- PostgreSQL 16 GRANT/REVOKE: https://www.postgresql.org/docs/16/sql-revoke.html
- ADR relacionado: ADR-0001 (almacenamiento de evidencias en volumen local).

---

## 9. Historial

| Versión | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| 1 | 14/05/2026 | Equipo AcredIA | Propuesta inicial derivada de FSD v1.0 §6.2 y §15 |
| 2 | 16/05/2026 | Equipo AcredIA | Aceptada tras validación de requisitos CEUB/ARCU-SUR de trazabilidad inmutable |