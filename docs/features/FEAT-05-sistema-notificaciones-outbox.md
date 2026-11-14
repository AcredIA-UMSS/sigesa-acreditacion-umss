# FEAT-05: Sistema Transaccional de Notificaciones y Alertas Outbox

- **ID Issue:** [#54](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss/issues/54)
- **Trazabilidad Funcional:** `FSD-UC-015` · Task Spec Kit `T-008` · `PRD-US-017`, `PRD-US-018`, `PRD-US-019`
- **Módulo:** `MOD-NOTIFY`
- **Release:** 8.1.0 / v2.1
- **Responsable / Asignado:** Alexander Alvarez (`Alex553-bot`)

---

## 1. Contexto y Problema de Negocio (Pain Point)

En los procesos de autoevaluación y acreditación institucional (CEUB y ARCU-SUR), el cumplimiento de plazos y la coordinación entre actores es crítica. Actualmente, el sistema cuenta únicamente con una implementación stub vacía ([`NoOpNotificationOutboxAdapter.java`](../../backend/src/main/java/com/umss/sigesa/adapter/out/evidence/NoOpNotificationOutboxAdapter.java)).

### Dolores de los Actores:
- **Coordinador de Carrera (`[CC]`):** Experimenta incertidumbre continua. Cuando un Técnico DUEA observa o rechaza una evidencia, el `[CC]` no recibe aviso por ningún canal, enterándose días después al borde del vencimiento del cronograma.
- **Técnico DUEA (`[TD]`):** No tiene garantía de confirmación de recepción ni forma de saber si el `[CC]` ha sido alertado de subsanaciones pendientes.

---

## 2. Requisitos de Auditoría Técnica (12-Factor & Concurrencia)

| Dimensión | Factor / Regla | Impacto y Solución Arquitectónica |
|---|---|---|
| **Concurrencia** | `CON-02` / `CON-03` | **Desacoplamiento I/O y Ley de Little:** Prohibido despachar llamadas SMTP dentro de la transacción JPA principal de negocio. La inserción del evento en la tabla `notification_outbox` es atómica con la mutación del indicador/evidencia ($W_{db} < 5\text{ ms}$). El envío físico de correos corre en un worker asíncrono desacoplado, sin retener conexiones Hikari. |
| **Concurrencia** | `CON-04` | **Timeouts y Retries con Jitter:** Cliente de correo con timeout estricto de conexión y socket ($\le 10\text{ s}$). Los reintentos por caída temporal del servidor SMTP deben aplicar retroceso exponencial (*exponential backoff*) con *jitter* para evitar la estampida (*thundering herd*). |
| **12-Factor** | `F12-03` / `CON-01` | **Procesos sin Estado (Stateless) y Multi-instancia:** Sin cron jobs que asuman instancia única ni colas en memoria. El worker de base de datos debe utilizar `SELECT ... FOR UPDATE SKIP LOCKED` en PostgreSQL para permitir $N \ge 1$ instancias activas simultáneamente sin duplicidad de envíos. |
| **12-Factor** | `F12-01` / `F12-05` | **Configuración y Servicios Intercambiables:** Host, puerto, credenciales TLS y remitente SMTP leídos estrictamente de variables de entorno (`SPRING_MAIL_HOST`, `SPRING_MAIL_PORT`, etc.) validadas al inicio. |
| **12-Factor** | `F12-04` | **Logs a stdout:** Registro de fallas de envío y reintentos a la salida estándar sin volcar contraseñas ni tokens sensibles. |

---

## 3. Alcance de Implementación

### Backend (Spring Boot 4.x / Java 21)
1. **Migración Flyway:** Creación de tabla `notification_outbox`:
   - `id` (UUID / BIGSERIAL), `recipient_email` (VARCHAR), `subject` (VARCHAR), `body` (TEXT), `deep_link` (VARCHAR), `status` (`PENDING`, `PROCESSING`, `SENT`, `FAILED`), `retry_count` (INT), `next_retry_at` (TIMESTAMP), `created_at` (TIMESTAMP), `sent_at` (TIMESTAMP).
   - Índice compuesto para el worker: `CREATE INDEX idx_outbox_poll ON notification_outbox(status, next_retry_at);`.
2. **Adaptador de Salida:**
   - Crear `JpaNotificationOutboxAdapter` que implemente el puerto `NotificationOutboxPort`.
3. **Worker Asíncrono:**
   - Componente `@Component` / `@Scheduled` con hilos virtuales o pool de tareas asíncrono.
   - Consulta transaccional mediante `SKIP LOCKED` para reclamar lotes pequeños de notificaciones.
4. **Deep-linking:**
   - Generación de URLs que dirijan al Coordinador directamente al indicador y observación abierta.

### Fuera de Alcance
- Notificaciones push en navegador o mensajería WhatsApp/Telegram.
- Modificación del esquema de plantillas de correos CNACU.

---

## 4. Criterios de Aceptación

- [ ] Toda observación (`UC-008`), aprobación (`UC-009`) o alerta de plazo (`UC-015`) encola un registro en `notification_outbox` en la misma transacción de base de datos.
- [ ] El envío SMTP se ejecuta fuera de la transacción principal; la conexión Hikari se libera de inmediato tras el `COMMIT`.
- [ ] El worker procesa mensajes concurrentes de forma segura en $N \ge 1$ instancias de backend.
- [ ] Si el servidor SMTP falla, el mensaje reintenta con backoff + jitter hasta 5 intentos antes de marcarse como `FAILED`.
- [ ] Cobertura de pruebas unitarias y de integración > 90% (JaCoCo).
