# FEAT-07: Bitácora Inmutable de Auditoría Append-Only

- **ID Issue:** [#56](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss/issues/56)
- **Trazabilidad Funcional:** `FSD-UC-017` · Task Spec Kit `T-010` · `PRD-US-022`, `PRD-US-025`
- **Módulo:** `MOD-AUDIT`
- **Release:** 8.1.0 / v2.1
- **Responsable / Asignado:** Alexander Alvarez (`Alex553-bot`)

---

## 1. Contexto y Problema de Negocio (Pain Point)

El proceso de acreditación ante comités evaluadores nacionales (CEUB) e internacionales (ARCU-SUR) exige certeza jurídica absoluta sobre las decisiones tomadas durante las etapas de autoevaluación, cierre de compuertas y Sala de Pares. Actualmente, el sistema utiliza [`NoOpAuditLogAdapter.java`](../../backend/src/main/java/com/umss/sigesa/adapter/out/auth/NoOpAuditLogAdapter.java), por lo que las operaciones sensibles no se registran en una bitácora inmutable.

### Dolores de los Actores:
- **Jefatura DUEA (`[JD]`):** No tiene evidencia auditable ante auditorías externas para demostrar quién autorizó el cierre de una etapa o modificó un análisis FODA.
- **Evaluador Externo / Par Académico (`[EE]`):** No puede verificar la cadena de custodia y fechado inmutable de las observaciones resueltas en la Sala de Pares.

---

## 2. Requisitos de Auditoría Técnica (12-Factor & Concurrencia)

| Dimensión | Factor / Regla | Impacto y Solución Arquitectónica |
|---|---|---|
| **12-Factor** | `F12-03` / `CON-01` | **Invariante Append-Only y Concurrencia Lineal:** La tabla `audit_log` es estrictamente append-only: solo admite operaciones `INSERT`. Al no realizarse `UPDATE` ni `DELETE`, no existen bloqueos de fila competitivos, garantizando alta escalabilidad ante concurrencia con $N \ge 1$ instancias de backend. |
| **Concurrencia** | `CON-02` / `CON-03` | **No Retención de Pool HikariCP:** El registro de auditoría debe ser ultraligero ($W_{db} < 5\text{ ms}$) o despachado vía `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` de modo que no alargue la transacción de negocio principal ni retenga conexiones JDBC. |
| **Concurrencia** | `CON-02` | **Exportación Asíncrona / Streaming:** El endpoint de exportación CSV `GET /api/v1/audit/logs/export` no debe cargar cientos de miles de registros en la memoria heap de Tomcat de golpe. Debe implementar streaming de respuesta (`StreamingResponseBody` o chunking JPA) para no estrangular hilos de atención ni provocar caídas por OutOfMemory. |
| **12-Factor** | `F12-04` | **Logs a la Salida Estándar:** Además de la persistencia en base de datos para la consulta de la Jefatura, se emitirá una línea estructurada a `stdout` (vía SLF4J) con el hash y el identificador de la acción auditada para los recolectores de la plataforma. |
| **12-Factor** | `F12-02` | **Sanitización Estricta de Secretos:** Prohibido registrar contraseñas, tokens JWT o credenciales en los campos de auditoría ni en la salida estándar. |

---

## 3. Alcance de Implementación

### Backend (Spring Boot 4.x / Java 21)
1. **Migración Flyway:** Tabla `audit_log`:
   - `id` (UUID), `actor_email` (VARCHAR), `action` (VARCHAR, e.g. `INDICATOR_APPROVED`, `FODA_UPDATED`, `STAGE_CLOSED`, `EVIDENCE_UPLOADED`), `entity_type` (VARCHAR), `entity_id` (VARCHAR), `payload_summary` (TEXT), `ip_address` (VARCHAR), `created_at` (TIMESTAMP).
   - Invariante a nivel BD: Sin permisos de `UPDATE` ni `DELETE` para el usuario de la aplicación.
2. **Adaptador de Salida:**
   - Crear `JpaAuditLogAdapter` implementando el puerto `AuditLogPort`.
3. **Endpoints de Auditoría:**
   - `GET /api/v1/audit/logs` con paginación (`Pageable`) y filtros por rango de fechas, actor y tipo de entidad. Protegido exclusivamente para rol `[JD]`.
   - `GET /api/v1/audit/logs/export` retornando archivo CSV descargable con codificación UTF-8 y streaming.

### Frontend (React 19 / TypeScript / Vite)
1. **Sección de Bitácora Institucional (Panel Jefatura):**
   - Tabla paginada de registros con filtros por fecha y actor.
   - Botón de descarga de informe CSV para la Sala de Pares.

### Fuera de Alcance
- Integración con cadenas de bloques (Blockchain) o firma criptográfica HSM.

---

## 4. Criterios de Aceptación

- [ ] Toda acción crítica del sistema (aprobación/rechazo de indicador, asignación de usuarios, cambios FODA, compuertas de etapa) genera un registro inmutable en `audit_log`.
- [ ] Intentos de acceso por roles distintos de `[JD]` son denegados con HTTP `403 Forbidden`.
- [ ] La descarga del reporte CSV de auditoría utiliza streaming y no degrada la latencia de otros endpoints operativos en ejecución concurrente.
- [ ] Cero secretos o contraseñas expuestos en los registros de auditoría.
- [ ] Cobertura de pruebas unitarias y de integración > 90% (JaCoCo).
