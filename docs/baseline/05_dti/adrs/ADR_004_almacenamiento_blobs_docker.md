# ADR_004: Almacenamiento de blobs de Evidencia en volumen Docker

| Campo | Valor |
|-------|-------|
| **Canónico** | [ADR-0004](../../adr/ADR-0004-evidence-blob-storage-docker.md) |
| **Origen equipo** | `team/aylenGonzales/09_dti/adr/ADR-001.md` |
| **Relacionado** | [ADR_001](ADR_001_append_only_evidencia.md) (modelo de versiones) · [ADR-0013](../../adr/ADR-0013-s3-evidence-blob-storage.md) |

## Metadatos

| Campo | Valor |
|-------|-------|
| Número | `0001` |
| Título | Almacenamiento de archivos de evidencia en sistema de archivos local |
| Fecha | 14/05/2026 |
| Autor(es) | Equipo AcredIA |
| Estado | **Supersedida para cloud v1.0** |
| Alcance | MOD-02 — Repositorio de evidencias y versionado |
| Stakeholders consultados | Tech Lead AcredIA · Jefa DUEA · TI UMSS |

---

## 1. Contexto

AcredIA / SIGESA requiere almacenar blobs de Evidence (PDF, DOCX, XLSX; hasta 50 MB por Evidence) cargados por los Coordinadores de Carrera [CC] para los procesos de acreditación CEUB y ARCU-SUR. Este ADR conserva la decisión histórica de volumen Docker. Para la arquitectura cloud distribuida v1.0 vigente, la decisión aplicable es [ADR-0013](../../adr/ADR-0013-s3-evidence-blob-storage.md): S3 para blobs de Evidence.

**Restricciones relevantes:**
- Presupuesto de infraestructura sujeto a aprobación institucional UMSS; costo cero en servicios cloud de pago es prioritario en v1.0.
- El sistema se despliega en un servidor institucional o VPS con Docker disponible (SA-05 del FSD).
- Los documentos aprobados son inmutables y no pueden eliminarse (RB-04, RBN-02).
- La integridad de cada archivo debe ser verificable (hash SHA-256, PRD-REQ-004).
- Se requiere disponibilidad ≥ 99 % en horario hábil (NFR-005).

**Fuerzas en tensión:**
- **Costo vs. escalabilidad**: almacenamiento cloud (S3-compatible) es más escalable pero introduce costo OPEX y complejidad de configuración.
- **Simplicidad vs. resiliencia geográfica**: volumen local es simple y gratuito pero no tiene replicación automática fuera del servidor.
- **Time-to-market**: la v1.0 debe desplegarse en Q4 2026; integrar un proveedor cloud agrega semanas de configuración y negociación contractual con TI UMSS.

---

## 2. Alternativas consideradas

| Alternativa | Pros | Contras | Costo aproximado |
|-------------|------|---------|-----------------|
| **A. Volumen Docker local** (`/data/evidencias/`) | Costo $0; sin dependencias externas; despliegue inmediato; control total sobre la ruta y estructura de carpetas por `proceso_id/fase_id/indicador_id/` | Sin replicación geográfica automática; capacidad limitada al disco del servidor; migración a cloud requiere copiar archivos | $0 CAPEX/OPEX en v1.0 |
| **B. Almacenamiento cloud S3-compatible** (AWS S3, MinIO gestionado, Backblaze B2) | Replicación automática; escalabilidad ilimitada; URLs firmadas para descarga segura | Costo OPEX mensual; requiere credenciales cloud y política de IAM; latencia de red adicional en UMSS (red institucional variable); negociación contractual TI | USD 20–80/mes según volumen |
| **C. Base de datos PostgreSQL (BYTEA/Large Objects)** | Transacciones ACID garantizadas para archivos + metadatos; respaldo integrado con `pg_dump` | Infla enormemente la BD; degradación de rendimiento con archivos grandes; anti-patrón conocido para archivos binarios | $0 adicional pero degrada PostgreSQL 16 |

---

## 3. Decisión

> **Decisión histórica supersedida:** almacenamiento de blobs de Evidence en volumen Docker local `/data/evidencias/` con estructura jerárquica por proceso/fase/indicador/versión.

La alternativa A es la única que cumple simultáneamente el requisito de costo $0 en v1.0 (restricción institucional UMSS), el time-to-market Q4 2026 y la integridad verificable mediante hash SHA-256. La alternativa B es la correcta para v2.0 una vez que el piloto valide el volumen real de documentos y se cuente con presupuesto aprobado. La alternativa C queda descartada definitivamente por ser un anti-patrón documentado que degradaría el rendimiento de las consultas de gestión de fases e indicadores.

La ruta de almacenamiento sigue la convención: `/data/evidencias/{proceso_id}/{fase_id}/{indicador_id}/{version}_{nombre_original}`, lo que permite localizar cualquier versión de un archivo sin consultar la base de datos si fuera necesario.

---

## 4. Consecuencias

### 4.1 Positivas
- Costo de infraestructura $0 en v1.0; no requiere cuentas ni contratos con proveedores cloud.
- Despliegue inmediato con `docker-compose`; sin configuración adicional de IAM o políticas de bucket.
- La ruta `ruta_relativa` en la entidad histórica `EVIDENCIA` permitía migrar a S3-compatible reemplazando solo la capa de almacenamiento; esa migración queda formalizada por ADR-0013 para cloud v1.0.
- Control total sobre la estructura de carpetas, facilitando la auditoría manual si fuera requerida por TI UMSS.

### 4.2 Negativas / costos
- Sin replicación geográfica automática: una falla de disco del servidor pierde los archivos si el respaldo diario (MOD-12, RBN-14) no fue ejecutado correctamente.
- Capacidad limitada al disco del servidor; se requiere monitoreo activo (alerta al 70% de ocupación, RF-04).
- La migración a S3-compatible deja de ser v2.0: ADR-0013 la promueve a decisión cloud v1.0.

### 4.3 Neutras / observables
- El hash SHA-256 se calcula post-escritura en disco (invariant de PC-002), garantizando integridad independientemente del medio de almacenamiento.
- El volumen Docker debe declararse como `named volume` en `docker-compose.yml` para sobrevivir recreaciones del contenedor.
- La estimación de capacidad inicial es ≥ 500 GB (SA-03 del FSD), pendiente confirmación con TI UMSS.

---

## 5. Impacto en el sistema

- **Código**: `T-04` (FSD) — implementar endpoint `POST /evidencias/{indicador_id}` con escritura en volumen local. El campo `ruta_relativa` en `EVIDENCIA` abstrae el medio de almacenamiento.
- **Operaciones**: el `docker-compose.yml` debe declarar el named volume `evidencias_data` mapeado a `/data/evidencias/`. El script de respaldo diario (MOD-12) debe incluir `tar + rsync` del volumen además del `pg_dump`.
- **Seguridad**: el directorio `/data/evidencias/` no debe ser servido directamente por el servidor web; los archivos se descargan a través de la API con validación de JWT y permisos por `carrera_id`.
- **Equipo**: no requiere habilidades adicionales; el equipo conoce Docker Volumes.
- **Costo**: $0 adicional en v1.0. En v2.0, presupuestar USD 20–80/mes para almacenamiento cloud.

---

## 6. Plan de reversión

**Señales de que la decisión fue incorrecta:**
- El disco del servidor supera el 85% de ocupación antes de Q2 2027.
- Se produce una pérdida de archivos por falla de disco no cubierta por respaldo.
- TI UMSS exige replicación geográfica como condición de continuidad del piloto.

**Costo estimado de revertir:** medio. Requiere migrar archivos existentes al bucket S3-compatible, actualizar `ruta_relativa` en `EVIDENCIA` para todas las filas existentes, y desplegar la nueva capa de almacenamiento. Se estima 1–2 sprints de trabajo.

**Plan B:** Migrar a MinIO (S3-compatible self-hosted) en el mismo servidor como paso intermedio de costo $0, ganando la API S3 sin costo cloud, antes de migrar a AWS S3 o Backblaze B2.

---

## 7. Validación

- **Métrica**: 0 pérdidas documentales en el primer proceso de acreditación gestionado íntegramente con SIGESA (KPI-04).
- **Métrica**: El script de respaldo diario genera confirmación al [JD] cada día (RBN-14).
- **Métrica**: La alerta de ocupación de disco al 70% se dispara correctamente en prueba de integración (TC-004 adaptado).
- **Responsable**: @ArchAgent + Tech Lead AcredIA.
- **Plazo**: verificación en piloto cerrado Q3 2026 con 5 carreras piloto.

---

## 8. Referencias

- FSD v1.0 AcredIA/SIGESA — §2.3 Stack tecnológico, SA-03, T-04, PC-002, RF-04.
- PRD v1.0 — PRD-REQ-003, PRD-REQ-004, PRD-NFR-012.
- BRD v2.0 — BR-001, BR-002, RB-04, BR-012.
- Docker Volumes documentation: https://docs.docker.com/storage/volumes/
- ADR relacionado: ADR-0002 (Log de auditoría append-only en PostgreSQL).

---

## 9. Historial

| Versión | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| 1 | 14/05/2026 | Equipo AcredIA | Propuesta inicial derivada de FSD v1.0 §2.3 y §15 |
| 2 | 16/05/2026 | Equipo AcredIA | Aceptada tras revisión de restricciones de presupuesto UMSS y análisis de alternativas |