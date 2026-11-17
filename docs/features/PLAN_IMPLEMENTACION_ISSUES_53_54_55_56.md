# Plan de Implementación Integral: Módulo 8 (Issues #53, #54, #55, #56)

> **Repositorio:** [AcredIA-UMSS/sigesa-acreditacion-umss](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss)  
> **Release Target:** Release 8.1.0 / v2.1 (Demo 1)  
> **Asignado / Responsable:** Alexander Alvarez (`Alex553-bot`)  
> **Metodología y Gobernanza:** AI-SDLC UMSS · Hexagonal Estricta (Java 21 / Spring Boot 4.x) + React 19 / TypeScript · JaCoCo > 90%  
> **Skills Aplicados:**
> - `sigesa-architectural-code-reviewer` (Guardián arquitectónico, modelo C4, desacoplamiento transaccional y Ley de Little).
> - `sigesa-prompt-contract-architect` (Contratos de prompts deterministas y sin alucinaciones).

---

## 1. Resumen Ejecutivo del Alcance

| Issue | Feature | Caso de Uso / Task | Módulo | Dimensión Auditoría | Complejidad |
|---|---|---|:---:|---|:---:|
| **[#53](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss/issues/53)** | **FEAT-04: Fail-fast de configuración y hardening de secretos por entorno** | `F12-01` / `F12-02` | `MOD-CONFIG` | 12-Factor (III) | Media |
| **[#54](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss/issues/54)** | **FEAT-05: Sistema Transaccional de Notificaciones y Alertas Outbox** | `FSD-UC-015` / `T-008` | `MOD-NOTIFY` | Concurrencia (`CON-01…04`), 12-Factor (`F12-03`) | Alta |
| **[#55](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss/issues/55)** | **FEAT-06: Bandeja Centralizada de Auditoría y Dictamen del Técnico DUEA** | `FSD-UC-012` / `T-007` | `MOD-DASH` / `MOD-EVIDENCE` | Concurrencia (`CON-01…04`), 12-Factor (`F12-03`) | Alta |
| **[#56](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss/issues/56)** | **FEAT-07: Bitácora Inmutable de Auditoría Append-Only** | `FSD-UC-017` / `T-010` | `MOD-AUDIT` | Concurrencia (`CON-01…03`), 12-Factor (`F12-02, 04`) | Alta |

---

## 2. Mapa de Arquitectura y Dependencias

```mermaid
graph TD
    subgraph Dominio & Aplicación
        A1[FSD-UC-008 / FSD-UC-009<br/>Dictamen Técnico] -->|Publica Evento Dominio| E1(IndicatorReviewedEvent)
        E1 -->|@TransactionalEventListener| MOD_NOTIFY[MOD-NOTIFY<br/>JpaNotificationOutboxAdapter]
        E1 -->|@TransactionalEventListener AFTER_COMMIT| MOD_AUDIT[MOD-AUDIT<br/>JpaAuditLogAdapter]
        A2[FSD-UC-012<br/>TechnicianInboxUseCase] -->|Lectura Paginada| MOD_DASH[MOD-DASH<br/>TechnicianInboxRepository]
    end

    subgraph Infraestructura & Persistencia
        MOD_NOTIFY -->|Atómico con Negocio| T_OUTBOX[(notification_outbox)]
        WORKER[Scheduled Outbox Worker<br/>SKIP LOCKED] -->|Pollea Lote| T_OUTBOX
        WORKER -->|Async con Jitter & Timeout| SMTP[Servidor SMTP / Mock]
        
        MOD_AUDIT -->|Append-Only INSERT| T_AUDIT[(audit_log)]
        MOD_DASH -->|Index Scan Wdb < 30ms| T_INDICATOR[(indicator / evidence)]
    end

    subgraph Frontend React 19
        UI_INBOX[Technician Inbox View<br/>AbortSignal.timeout 10s] -->|GET /dashboards/technician/inbox| MOD_DASH
        UI_AUDIT[Audit Log Viewer & CSV Stream<br/>Rol JD Exclusivo] -->|GET /audit/logs /export| MOD_AUDIT
    end
```

---

## 3. Desglose Detallado por Issue

### 3.1. Issue #53: FEAT-04 — Fail-fast de configuración y hardening de secretos (F12-01 / F12-02)

#### Diagnóstico Técnico:
- `backend/src/main/resources/application.yaml` posee fallback embebido para `${SIGESA_JWT_SECRET:...}`.
- `backend/src/main/resources/application-prod.yaml` mantiene fallback a `localhost:5432` en `DATABASE_URL` y password vacío.
- Falta validador de ciclo de vida (`ApplicationListener<ApplicationEnvironmentPreparedEvent>` o bean `@ConfigurationProperties` con `@Validated`) que aborte el arranque tempranamente si faltan variables críticas en entornos productivos.

#### Pasos de Implementación:
1. **Configuración YAML:**
   - Modificar `application-prod.yaml` eliminando fallbacks de `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD` y `SIGESA_JWT_SECRET`.
   - Limpiar `docker-compose.yml` de literales de producción hardcodeados.
2. **Componente de Fail-Fast:**
   - Crear `EnvironmentSecurityValidator` en `com.umss.sigesa.config`:
     - Al activarse el perfil `prod` (o Docker container), verificar presencia y robustez (mínimo 256 bits para `SIGESA_JWT_SECRET`).
     - Lanzar `IllegalStateException` descriptiva sin imprimir secretos en consola.
3. **Sanitización de Logs y Actuator:**
   - Configurar `SanitizingFunction` para enmascarar tokens, passwords y credenciales en salidas Actuator/SLF4J.
4. **Pruebas (Coverage > 90%):**
   - Test con `ApplicationContextRunner` simulando perfil `prod` sin variables (debe fallar arranque).
   - Test con perfil `prod` y variables válidas (debe iniciar limpiamente).

---

### 3.2. Issue #54: FEAT-05 — Sistema Transaccional de Notificaciones Outbox (FSD-UC-015)

#### Diagnóstico Técnico:
- Existe `NotificationOutboxPort.java` pero su implementación es `NoOpNotificationOutboxAdapter.java`.
- Las observaciones y aprobaciones no alertan a Coordinadores de Carrera (`[CC]`).

#### Pasos de Implementación:
1. **Migración Flyway (`V2_1_1__create_notification_outbox.sql`):**
   - Tabla `notification_outbox`:
     - `id` (UUID, PK), `recipient_email` (VARCHAR 255), `subject` (VARCHAR 255), `body` (TEXT), `deep_link` (VARCHAR 500), `status` (VARCHAR 30: `PENDING`, `PROCESSING`, `SENT`, `FAILED`), `retry_count` (INT DEFAULT 0), `max_retries` (INT DEFAULT 5), `next_retry_at` (TIMESTAMP), `created_at` (TIMESTAMP), `sent_at` (TIMESTAMP NULL).
   - Índice de sondeo optimizado: `CREATE INDEX idx_outbox_poll ON notification_outbox(status, next_retry_at);`.
2. **Adaptador JPA:**
   - Reemplazar `NoOpNotificationOutboxAdapter` por `JpaNotificationOutboxAdapter`:
     - Inserta en `notification_outbox` dentro de la transacción JPA del caso de uso de negocio ($W_{db} < 5\text{ ms}$).
3. **Outbox Worker Resiliente:**
   - Crear `NotificationOutboxWorker` con `@Scheduled`:
     - Consulta PostgreSQL: `SELECT o FROM NotificationOutboxJpaEntity o WHERE o.status = 'PENDING' AND (o.nextRetryAt IS NULL OR o.nextRetryAt <= :now) ORDER BY o.createdAt ASC` con lock hint `PESSIMISTIC_WRITE` y query nativa `FOR UPDATE SKIP LOCKED`.
     - Despacho SMTP asíncrono con `JavaMailSender` o cliente HTTP.
     - Timeout estricto de socket y conexión ($\le 10\text{ s}$).
     - Algoritmo de reintento: Exponential Backoff con Jitter:
       $$T_{\text{wait}} = 2^{\text{retryCount}} \times 5\text{ s} + \text{random}(0, 2000)\text{ ms}$$
     - Si `retryCount >= 5`, marcar como `FAILED`.
4. **Deep-linking:**
   - Construcción de enlace `/evaluation/program/{programId}/indicator/{indicatorId}?observationId={id}`.
5. **Pruebas:**
   - Unitarias de backoff y jitter.
   - Integración con Testcontainers / PostgreSQL verificando `SKIP LOCKED` y no retención de conexiones en Hikari.

---

### 3.3. Issue #55: FEAT-06 — Bandeja Centralizada de Auditoría DUEA (FSD-UC-012)

#### Diagnóstico Técnico:
- El Técnico DUEA (`[TD]`) debe navegar árbol por árbol para auditar evidencias en estado `SUBIDO` o `SUBSANADO`.
- Se requiere un endpoint y pantalla unificada que centralice la bandeja técnica sin degradar el pool Hikari.

#### Pasos de Implementación:
1. **Backend - Endpoint y Repository:**
   - `GET /api/v1/dashboards/technician/inbox`:
     - Filtros: `programId` (UUID), `level1Id` (UUID), `status` (Enum: `SUBIDO`, `SUBSANADO`, `OBSERVADO`), `page`, `size`.
     - Seguridad: `@PreAuthorize("hasAnyRole('TD', 'JD')")`.
   - Consulta JPA / DTO de proyección:
     - Retorna: `indicatorId`, `indicatorCode`, `indicatorName`, `pathHierarchy` (N1 > N2 > N3), `evidenceId`, `evidenceFilename`, `uploadedAt`, `activeObservationsCount`, `version`.
     - Transacción estrictamente `@Transactional(readOnly = true)` para liberar la conexión de Hikari en $W_{db} < 30\text{ ms}$.
   - Índices en base de datos: `CREATE INDEX idx_evidence_inbox ON evidence(program_id, status, uploaded_at DESC);`.
2. **Frontend - Pantalla `/technician/inbox`:**
   - Generación de cliente vía OpenAPI / Orval.
   - Vista React con barra de filtros responsiva.
   - Paginación estricta y control de orden.
   - Acciones en $\le 2$ clics:
     - **Ver Detalle / Evidencia:** visualizador de metadatos o previsualización.
     - **Aprobar (UC-009):** Modal de confirmación que invoca `POST /indicators/{id}/approve` con control optimista de versión (`version`).
     - **Observar (UC-008):** Modal con plazo y descripción que invoca `POST /indicators/{id}/reject`.
   - Resiliencia de red: Todas las llamadas usan `AbortSignal.timeout(10000)` para cancelar peticiones rezagadas.
3. **Pruebas:**
   - Pruebas unitarias de controladores y repositorio (Spring).
   - Pruebas unitarias de componentes con Vitest / Testing Library en frontend.

---

### 3.4. Issue #56: FEAT-07 — Bitácora Inmutable de Auditoría Append-Only (FSD-UC-017)

#### Diagnóstico Técnico:
- Actualmente `NoOpAuditLogAdapter.java` no persiste acciones sensibles (aprobaciones, rechazos, cambios FODA, cierres de fase).
- Se carece de validez legal para comités evaluadores (CEUB / ARCU-SUR).

#### Pasos de Implementación:
1. **Migración Flyway (`V2_1_2__create_audit_log.sql`):**
   - Tabla `audit_log`:
     - `id` (UUID, PK), `actor_email` (VARCHAR 255), `actor_role` (VARCHAR 50), `action` (VARCHAR 100), `entity_type` (VARCHAR 100), `entity_id` (VARCHAR 255), `payload_diff` (TEXT), `ip_address` (VARCHAR 50), `created_at` (TIMESTAMP NOT NULL).
   - Invariante a nivel BD: Tabla sin triggers de update; usuario de aplicación con privilegios solo de `INSERT` y `SELECT`.
2. **Adaptador JPA y Eventos de Dominio:**
   - Reemplazar `NoOpAuditLogAdapter` por `JpaAuditLogAdapter` implementando `AuditLogPort`.
   - Registrar eventos asíncronamente con `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` para no retener conexiones ni alargar la transacción de negocio principal.
   - Sanitización de datos sensibles antes de persistir (`F12-02`).
   - Log estructurado adicional a `stdout` (SLF4J) con hash de trazabilidad.
3. **Endpoints de Consulta y Exportación:**
   - `GET /api/v1/audit/logs`: Consulta paginada con filtros (`actor`, `entityType`, rango de fechas).
   - `GET /api/v1/audit/logs/export`: Streaming CSV (`StreamingResponseBody`) con lectura en chunks JPA para no saturar memoria heap.
   - Seguridad: Restringido estrictamente a `[JD]` (HTTP `403 Forbidden` para otros roles).
4. **Frontend - Vista de Bitácora:**
   - Panel exclusivo para Jefatura (`[JD]`).
   - Búsqueda con filtros, visualización de diferencias y botón de descarga con barra de progreso.
5. **Pruebas:**
   - Unitarias de sanitización e inserción append-only.
   - Verificación de RBAC (403 para `[CC]` y `[TD]`).
   - Prueba de carga concurrente en exportación CSV.

---

## 4. Matriz de Cumplimiento de Gobernanza y Skills

| Aspecto de Calidad / Gobernanza | Aplicación en el Plan | Skill Asociado |
|---|---|---|
| **Arquitectura Hexagonal Estricta** | El Dominio queda 100% puro; puertos `NotificationOutboxPort` y `AuditLogPort` implementados exclusivamente en adaptadores `adapter/out`. | `sigesa-architectural-code-reviewer` |
| **Ley de Little ($L = \lambda W$)** | Desacoplamiento de I/O externo (SMTP, streaming CSV); transacciones de lectura `< 30 ms` para proteger el pool de 10 conexiones de Hikari. | `sigesa-architectural-code-reviewer` |
| **Contratos de Prompts Ejecutables** | Prompts de implementación con formato determinista, inputs/outputs tipados y control de invariantes. | `sigesa-prompt-contract-architect` |
| **Gobernanza Monorepo & AI-SDLC** | Protección estricta de `docs/baseline/`; actualización en `docs/features/` y `docs/product/`; registro de prompts con `@save-prompt-mapping`. | Reglas de Monorepo SIGESA |
| **Calidad de Código y Cobertura** | Cobertura unitaria e integración > 90% (JaCoCo backend / OxLint y Vitest frontend). | Reglas de Proyecto |
