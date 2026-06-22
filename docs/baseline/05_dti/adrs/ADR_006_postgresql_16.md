# ADR_006: PostgreSQL 16 como base de datos principal

| Campo | Valor |
|-------|-------|
| **Canónico** | [ADR-0006](../../adr/ADR-0006-postgresql-16-primary-database.md) |
| **Origen equipo** | `team/aylenGonzales/09_dti/adr/ADR-003.md` |

## Metadatos

| Campo | Valor |
|-------|-------|
| Número | `0003` |
| Título | PostgreSQL 16 como base de datos relacional principal de SIGESA |
| Fecha | 16/05/2026 |
| Autor(es) | Equipo AcredIA |
| Estado | **Aceptada** |
| Alcance | Todo el sistema — capa de persistencia |
| Stakeholders consultados | Tech Lead AcredIA · @ArchAgent · @DevAgent |

---

## 1. Contexto

AcredIA / SIGESA requiere una base de datos que soporte el modelo de datos de 9 entidades relacionadas (USUARIO, CARRERA, FACULTAD, PROCESO_ACREDITACION, FASE, INDICADOR, EVIDENCIA, LOG_AUDITORIA, NOTIFICACION) con las siguientes características críticas:

- **Integridad referencial fuerte**: las relaciones entre proceso → fase → indicador → evidencia son jerárquicas y normativas; un error de integridad puede invalidar una acreditación.
- **Búsqueda full-text**: el buscador de documentos (FSD-UC-007) requiere búsqueda por título, carrera, facultad, modalidad y gestión con respuesta ≤ 3 s en p95 (NFR-001).
- **JSONB para contexto variable**: el campo `detalle` de `LOG_AUDITORIA` almacena contexto heterogéneo por tipo de evento.
- **Transacciones ACID**: la carga de una evidencia (escritura en disco + registro en BD + cambio de estado del indicador + encolado de notificación) debe ser atómica o reversible (PC-002).
- **Inmutabilidad mediante RBAC**: el rol `sigesa_app` requiere REVOKE DELETE/UPDATE sobre `LOG_AUDITORIA` (ADR-0002).
- **Costo $0 en v1.0**: restricción institucional UMSS.

El stack de despliegue es Docker + Docker Compose (SA-05 del FSD); la BD debe ser containerizable sin licencias.

---

## 2. Alternativas consideradas

| Alternativa | Pros | Contras | Costo aproximado |
|-------------|------|---------|-----------------|
| **A. PostgreSQL 16** | Open source, $0; ACID; full-text nativo con GIN e índices `tsvector`; JSONB nativo; RBAC granular (REVOKE); particionamiento nativo; imagen Docker oficial; amplio conocimiento del equipo | Requiere gestión de conexiones (pg_pool o connection pooling en la app) bajo carga | $0 |
| **B. MySQL 8 / MariaDB** | Popular, $0; rendimiento adecuado para CRUD básico | Full-text menos maduro que PostgreSQL GIN; JSONB no nativo (JSON con limitaciones); RBAC menos granular; no soporta REVOKE por columna/operación de forma equivalente | $0 |
| **C. SQLite** | Cero configuración; ideal para prototipo | Sin servidor; sin concurrencia real de escrituras (WAL tiene limitaciones con múltiples writers); no adecuado para sistema multi-usuario concurrente en producción | $0 |
| **D. MongoDB** | Schema flexible; BSON nativo | No relacional: la integridad jerárquica proceso→fase→indicador→evidencia requeriría validación en aplicación; sin RBAC granular equivalente; menor conocimiento del equipo | $0 community |

---

## 3. Decisión

> **Elegimos la alternativa A: PostgreSQL 16 como único motor de base de datos relacional de SIGESA.**

PostgreSQL 16 es el único motor que satisface simultáneamente los cuatro requisitos críticos: ACID para transacciones de carga de evidencias, full-text nativo con índices GIN para el buscador (NFR-001 ≤ 3 s), JSONB para el campo `detalle` de LOG_AUDITORIA, y RBAC granular para la inmutabilidad del log (ADR-0002). La elección elimina la necesidad de componentes adicionales (Elasticsearch para búsqueda, Redis para caché de sesiones en v1.0) manteniendo el stack mínimo compatible con el time-to-market Q4 2026.

El equipo tiene experiencia previa con PostgreSQL, lo que reduce el riesgo de implementación. La imagen Docker oficial `postgres:16` está disponible sin costo y es compatible con el entorno de despliegue de la UMSS.

---

## 4. Consecuencias

### 4.1 Positivas
- Un solo motor para todos los requisitos: relacional, full-text, JSONB y RBAC granular.
- Los índices GIN sobre `tsvector` permiten búsquedas full-text en ≤ 3 s p95 con 1.000 documentos indexados (TC-010).
- `pg_dump` cubre toda la persistencia del sistema en un único respaldo diario (MOD-12).
- Migraciones gestionadas con Flyway (Node) o Alembic (Python) según resultado del spike de backend (T-03).
- RBAC de PostgreSQL soporta nativamente `REVOKE DELETE, UPDATE ON LOG_AUDITORIA FROM sigesa_app` (ADR-0002).

### 4.2 Negativas / costos
- Requiere configuración de connection pooling (PgBouncer o pooling en la app) si los usuarios concurrentes superan las 100 conexiones simultáneas — no es un riesgo en el piloto UMSS pero debe contemplarse para v1.1.
- El equipo debe gestionar índices y vacuum periódico; no es un managed service con mantenimiento automático.
- Si en v2.0 se requiere búsqueda semántica para el módulo de IA asistencial (RBN-15), habrá que evaluar la extensión `pgvector` o migrar la capa de búsqueda semántica a un servicio externo.

### 4.3 Neutras / observables
- La imagen `postgres:16` se declara en `docker-compose.yml` con volume persistente para `/var/lib/postgresql/data`.
- La variable de entorno `POSTGRES_PASSWORD` debe gestionarse con Docker Secrets o archivo `.env` no versionado.
- El particionamiento de `LOG_AUDITORIA` por rango de fechas puede activarse en v2.0 sin cambiar la interfaz de la tabla (plan de reversión de ADR-0002).

---

## 5. Impacto en el sistema

- **Código**: `T-03` (FSD) — esquema completo con Flyway/Alembic; `T-10` (FSD) — índices full-text GIN para el buscador. Todos los módulos (MOD-01 a MOD-12) usan PostgreSQL como única fuente de verdad persistente.
- **Operaciones**: `docker-compose.yml` declara el servicio `db` con imagen `postgres:16` y volume `pg_data`. El script de respaldo diario ejecuta `pg_dump` y envía confirmación al [JD] (RBN-14).
- **Seguridad**: `sigesa_app` tiene permisos mínimos (SELECT, INSERT, UPDATE sobre tablas de negocio; solo INSERT sobre `LOG_AUDITORIA`). El superusuario `postgres` está protegido con contraseña gestionada via Docker Secrets.
- **Equipo**: habilidades existentes en el equipo. No requiere capacitación adicional.
- **Costo**: $0 en v1.0. Imagen oficial Docker, sin licencias.

---

## 6. Plan de reversión

**Señales de que la decisión fue incorrecta:**
- Las consultas del buscador superan los 3 s p95 con volúmenes reales de producción a pesar de los índices GIN.
- El número de conexiones concurrentes supera la capacidad de PostgreSQL sin PgBouncer.
- En v2.0, los requisitos de búsqueda semántica de IA hacen insostenible mantener todo en PostgreSQL.

**Costo estimado de revertir:** alto. Migrar el modelo de datos relacional a otro motor requeriría reescribir las migraciones y validar la integridad referencial. Se estima 2–3 sprints.

**Plan B:** Antes de migrar el motor, explorar extensiones PostgreSQL: `pg_partman` para particionamiento automático, `pgvector` para búsqueda semántica en v2.0, PgBouncer para connection pooling.

---

## 7. Validación

- **TC-007** (plan de pruebas FSD §12.2): dashboard con 50 procesos activos responde en ≤ 3 s p95 con 50 VUs — medición con k6.
- **TC-010**: buscador retorna resultados en ≤ 3 s con 1.000 documentos indexados — medición con k6.
- **NFR-001**: latencia p95 ≤ 3.000 ms en dashboard y buscador bajo 50 VUs concurrentes — herramienta k6.
- **Responsable**: @DevAgent + @QaAgent.
- **Plazo**: sprint de carga Q3 2026 previo al piloto.

---

## 8. Referencias

- FSD v1.0 AcredIA/SIGESA — §2.3 Stack tecnológico, §6 Modelo de datos, T-03, T-10, T-11.
- PostgreSQL 16 Full Text Search: https://www.postgresql.org/docs/16/textsearch.html
- PostgreSQL 16 JSONB: https://www.postgresql.org/docs/16/datatype-json.html
- ADR relacionado: ADR-0002 (Log de auditoría append-only en PostgreSQL).
- ADR relacionado: ADR-0001 (metadatos append-only de Evidence) y ADR-0013 (S3 para blobs de Evidence).

---

## 9. Historial

| Versión | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| 1 | 16/05/2026 | Equipo AcredIA | Propuesta inicial |
| 2 | 16/05/2026 | Equipo AcredIA | Aceptada — confirma la elección implícita del FSD v1.0 §2.3 con justificación formal |