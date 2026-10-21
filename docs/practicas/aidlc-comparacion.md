# Práctica: AI-DLC de AWS con y sin herramienta

> Copien este archivo en su repositorio como `docs/practicas/aidlc-comparacion.md` y complétenlo. Entreguen por Classroom el enlace al repositorio (y el del video, si eligieron esa opción).

## 1. Datos

- **Modalidad:** individual
- **Repositorio:** [https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss)
- **Rama sin herramienta (A):** `alex/feature/notifications/adhoc_aidlc` (alias local: `feature-sin-aidlc`)
- **Rama con herramienta (B):** `alex/feature/notifications/aidlc` (alias local: `feature-con-aidlc`)
- **Video (opcional):** N/A (entrega por enlace a repositorio y reporte de comparación)

**Responsables** (obligatorio si es en grupo; cada aporte debe verse en los commits con la identidad de su autor):

| Integrante | Qué hizo | Commits o PR |
|---|---|---|
| Alexander James Alvarez Rojas (`Alex553-bot` / `alexalvarez`) | **Versión A (Ad-hoc):** Implementación del backend del sistema transaccional de notificaciones outbox, worker asíncrono con `SKIP LOCKED`, tabla Flyway `V24`, puertos y adaptadores JPA, y suite de pruebas unitarias.<br>**Versión B (AI-DLC Express):** Conducción del ciclo AI-DLC Express, revisión de compuertas (Reverse Engineering, Requirements, Code Generation, Build/Test), generación de contratos REST (`/me`, `/unread-count`, `/read`), integración de Flyway `V26`, y artefactos de auditoría de ciclo de vida. | Versión A: Commit [`6ef989d`](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss/commit/6ef989d9c30c7f823631a96d590478f496e6551c)<br>Versión B: `PR-IMPL-045`, `DD-UC-015`, artefactos `aidlc/` y commits en rama `alex/feature/notifications/aidlc` |

---

## 2. El feature

- **Nombre del Feature:** Sistema Transaccional de Notificaciones y Alertas Outbox (`MOD-NOTIFY` / `FSD-UC-015` / `FEAT-05`).
- **Enlace de especificación:** [`docs/features/FEAT-05-sistema-notificaciones-outbox.md`](../features/FEAT-05-sistema-notificaciones-outbox.md) y [`docs/design/DD-UC-015.md`](../design/DD-UC-015.md).
- **Descripción:** Implementa el mecanismo transaccional de notificaciones del sistema de acreditación universitaria. Ante observaciones de evidencias (`UC-008`), aprobaciones (`UC-009`) o proximidad de vencimiento de plazos (`UC-015`), los eventos se registran de forma atómica en la tabla `notification_outbox` en la misma transacción de base de datos ($W_{db} < 5\text{ ms}$). Un worker asíncrono desacoplado despacha los avisos sin retener conexiones Hikari ni bloquear el hilo web, empleando `SELECT ... FOR UPDATE SKIP LOCKED` para permitir ejecución multi-instancia concurrente. Se exponen endpoints REST para el Coordinador de Carrera (`[CC]`): consulta de notificaciones paginadas (`GET /api/v1/notifications/me`), conteo de no leídas para badges de UI (`GET /api/v1/notifications/unread-count`), y marcado individual o masivo de lectura (`PATCH /{id}/read`, `POST /mark-all-read`).
- **Entradas y Salidas:**
  - `GET /api/v1/notifications/me?unreadOnly=boolean&page=int&size=int`: Recibe autenticación JWT y filtros de paginación/estado; retorna `Page<NotificationResponseDto>` con lista paginada de alertas y deep links directos.
  - `GET /api/v1/notifications/unread-count`: Retorna `{ "unreadCount": int }`.
  - `PATCH /api/v1/notifications/{id}/read`: Recibe UUID; actualiza `read_at = NOW()` y retorna HTTP `204 No Content`.
  - `POST /api/v1/notifications/mark-all-read`: Actualiza en lote las notificaciones del usuario autenticado; retorna HTTP `204 No Content`.
- **Errores esperados:** HTTP `401 Unauthorized` si no existe token JWT válido; HTTP `403 Forbidden` si el rol no tiene acceso al scope del programa; HTTP `404 Not Found` si el identificador de notificación no existe.
- **Criterio de aceptación:**
  1. La inserción en el outbox ocurre en la misma transacción JPA del cambio de estado del indicador.
  2. El envío y procesamiento I/O se ejecutan de manera asíncrona fuera de la transacción principal.
  3. Soporte multi-instancia seguro contra condiciones de carrera mediante `SKIP LOCKED`.
  4. Cobertura de pruebas unitarias y de integración $> 90\%$ (JaCoCo / Vitest).

---

## 3. Versión A: sin la herramienta

- **Cómo trabajaron (asistente de IA directo o flujo del curso):**
  Se trabajó utilizando el asistente de IA integrado en el IDE (Cursor Agent / Composer en modo chat libre) mediante prompts ad-hoc iterativos. El desarrollador debió especificar manualmente cada componente técnico en sucesivas iteraciones: solicitar la migración Flyway `V24__create_notification_outbox.sql`, guiar al asistente para no colocar llamadas SMTP dentro de `@Transactional`, forzar la creación de la entidad JPA y el repositorio con la consulta nativa `SKIP LOCKED`, y finalmente pedir la creación de tests unitarios para simular caídas de concurrencia.
  
  **Fricciones encontradas:**
  - El asistente proponía por defecto soluciones sincrónicas acopladas (ej. enviar el email directamente en el servicio de aplicación).
  - No hubo una etapa estructurada de análisis previo ni validación de requisitos no funcionales (NFRs) antes de escribir código.
  - La documentación y trazabilidad tuvieron que redactarse retrospectivamente en archivos sueltos (`FEAT-05-sistema-notificaciones-outbox.md`), sin validación automatizada de completitud de requisitos.
- **Resultado (enlace a la rama o al PR):**
  - Rama: [`alex/feature/notifications/adhoc_aidlc`](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss/tree/alex/feature/notifications/adhoc_aidlc) (commit [`6ef989d`](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss/commit/6ef989d9c30c7f823631a96d590478f496e6551c)).

---

## 4. Versión B: con AI-DLC de AWS 2.11.0 (perfil Express)

- **Comando usado:**
  ```bash
  /aidlc express "Implementar sistema transaccional de notificaciones backend y outbox pattern para alertas al Coordinador de Carrera FSD-UC-015"
  ```
- **Salida de `aidlc version`:**
  ```text
  2.11.0
  ```
- **Puertas que revisaron y qué corrigieron con Request Changes:**
  - **Reverse Engineering Gate:**
    - *Borrador del agente:* Quería crear una tabla desacoplada sin relaciones ni coherencia con las convenciones de esquemas Flyway existentes ni el modelo de usuarios del monorepo (`users`, `user_program_assignments`).
    - *Corrección (Request Changes):* Se solicitó reutilizar las convenciones existentes del proyecto (Flyway V24/V26, UUIDs nativos de PostgreSQL y esquemas de roles `CC` y `TD`).
  - **Requirements Analysis Gate (`requirements.md`):**
    - *Borrador del agente:* Se enfocó exclusivamente en el endpoint de consulta básico, olvidando los requisitos no funcionales de auditoría técnica del curso (12-Factor `CON-02`/`CON-03` para desacoplamiento I/O de HikariCP y reintentos con backoff exponencial con jitter).
    - *Corrección (Request Changes):* Se obligó a incluir explícitamente en `requirements.md` los NFRs de concurrencia, el endpoint de conteo para badges (`/unread-count`), y el criterio de latencia $W_{db} < 5\text{ ms}$.
  - **Code Generation Gate (`code-generation-plan.md`):**
    - *Borrador del agente:* El plan planteaba implementar la lógica de lectura directamente en un único adaptador monolítico de base de datos sin respetar la arquitectura hexagonal estricta del proyecto.
    - *Corrección (Request Changes):* Se ordenó segregar los contratos de puertos en entrada (`GetNotificationsUseCase`) y salida (`NotificationQueryPort` y `NotificationSenderPort`), asegurando que las entidades JPA no salgan de la capa de infraestructura.
  - **Build and Test Gate (`test-results.md`):**
    - *Borrador del agente:* Se ejecutaron únicamente pruebas unitarias con stubs simples, sin verificar el comportamiento de locking (`SKIP LOCKED`) ni el paso de parámetros de paginación en el controlador web.
    - *Corrección (Request Changes):* Se instruyó agregar pruebas específicas con MockMvc y pruebas unitarias de aislamiento para el worker y el repositorio.
- **Artefactos versionados:**
  - `aidlc/requirements.md` y `aidlc/spaces/default/intents/261020-notifications-backend/requirements.md`
  - `aidlc/code-generation-plan.md` y `aidlc/spaces/default/intents/261020-notifications-backend/code-generation-plan.md`
  - `aidlc/test-results.md` y `aidlc/spaces/default/intents/261020-notifications-backend/test-results.md`
  - `aidlc/spaces/default/intents/261020-notifications-backend/aidlc-state.md`
  - `aidlc/spaces/default/intents/261020-notifications-backend/audit/audit.jsonl`
  - `.aidlc-version` (versión `2.11.0`)

---

## 5. Comparación

| Aspecto | A · sin herramienta (Ad-hoc) | B · con AI-DLC de AWS 2.11.0 (Express) |
|---|---|---|
| **Tiempo total** | ~45 minutos (incluyendo depuración manual de dependencias y refactorización de código acoplado) | ~25 minutos (flujo guiado paso a paso con compuertas claras) |
| **Veces que tuvieron que intervenir o corregir** | 5 intervenciones reactivas (corregir acoplamiento transaccional SMTP, corregir entidad JPA expuesta en controlador, agregar índices olvidados en Flyway, ajustar mocks en tests) | 3 intervenciones preventivas en las puertas de decisión (Rechazo en Requirements por NFRs de concurrencia, corrección en plan de código para arquitectura hexagonal, y ampliación de tests en Build/Test) |
| **Artefactos que quedaron en el repositorio** | Código fuente Java, script Flyway V24, archivo markdown manual `FEAT-05-sistema-notificaciones-outbox.md` (sin log formal de auditoría de decisiones) | Código fuente Java/TS, scripts Flyway V24/V26, `aidlc/requirements.md`, `aidlc/code-generation-plan.md`, `aidlc/test-results.md`, log estructurado `aidlc-state.md` y auditoría transaccional JSONL en `aidlc/spaces/.../audit/audit.jsonl` |
| **¿Las pruebas pasan? (ejecutadas por ustedes)** | Sí: Pruebas unitarias de worker y outbox (`JpaNotificationOutboxAdapterTest`, `NotificationOutboxWorkerTest`) pasando al 100%. | Sí: Pruebas unitarias y de integración de frontend (62 tests en 6 suites de notificaciones) y backend pasando al 100%. |
| **Qué salió mejor** | Rapidez para generar un primer borrador funcional de código sin pasar por validaciones formales de ciclo de vida. | Rigor arquitectónico: el agente se vio forzado a respetar la arquitectura hexagonal, los NFRs de concurrencia (12-Factor) y a dejar evidencia auditable de cada paso. |
| **Qué salió peor** | Riesgo alto de deuda técnica silenciosa: el agente omitió inicialmente los NFRs críticos de concurrencia y expuso detalles de infraestructura. | Sobrecarga de proceso para cambios triviales: el ciclo de compuertas requiere aprobaciones explícitas que ralentizan si se busca una edición inmediata de 2 líneas. |

---

## 6. Conclusiones

### 1. ¿Qué decisiones tomó el agente sin que ustedes las vieran en cada versión?
- **En la Versión A (sin herramienta):** El agente tomó decisiones implícitas sobre el manejo de transacciones, decidiendo inicialmente agrupar la llamada de red dentro del `@Transactional` de Spring, lo cual hubiera causado agotamiento del pool HikariCP en producción bajo carga concurrente. Además, utilizó tipos primitivos y nombres de columnas arbitrarios en SQL sin verificar las restricciones existentes del esquema PostgreSQL.
- **En la Versión B (con AI-DLC):** El flujo Express acotó la libertad del agente obligándolo a manifestar sus intenciones en `requirements.md` y `code-generation-plan.md` antes de escribir una sola línea de Java. Sin embargo, el agente aún intentó omitir el perfil de seguridad del token JWT en el controlador web hasta que se revisó la compuerta de generación de código.

### 2. ¿Cuántas veces aprobaron una puerta por inercia, sin revisarla de verdad?
- En el primer intento de la compuerta de *Reverse Engineering*, hubo una tendencia natural a pulsar "Aprobar" rápidamente por tratarse de un análisis contextual. Sin embargo, al notar que no había detectado el patrón de usuarios ni el scope de programas (`user_program_assignments`), se detuvo la inercia y se ejecutó un *Request Changes*. En *Requirements Analysis* y *Code Generation*, las compuertas se inspeccionaron minuciosamente, encontrando en ambas omisiones que justificaron correcciones antes de autorizar el avance.

### 3. ¿Qué evidencia permite a otra persona entender por qué se hizo así: la auditoría de AI-DLC o el historial del PR?
- La **auditoría de AI-DLC (`aidlc-state.md` y `audit/audit.jsonl` junto a `requirements.md`)** proporciona una trazabilidad inmensamente superior. Mientras que un historial de PR frecuentemente muestra solo el resultado final ("qué se cambió") o mensajes de commit genéricos (`feat: outbox notifications`), la auditoría de AI-DLC documenta:
  1. Qué requisitos de negocio y NFRs motivaron la solución.
  2. Qué alternativas fueron descartadas o corregidas en las compuertas de revisión.
  3. Quién aprobó cada etapa y bajo qué criterios de aceptación se consideró finalizada la construcción.

### 4. ¿Para qué tamaño de cambio vale la pena AI-DLC de AWS y para cuál no?
- **Vale la pena:** Para cambios medianos, grandes o arquitectónicamente sensibles (nuevos módulos, patrones transaccionales concurrentes, integraciones con APIs externas, cambios en perfiles de seguridad o flujos con NFRs estrictos). En estos casos, el costo del control de compuertas se amortiza inmediatamente evitando deuda técnica y bugs en producción.
- **No vale la pena:** Para correcciones puntuales (*one-liners*), hotfixes de tipografía, actualización de un texto estático de UI, o ajustes cosméticos de estilos CSS, donde el overhead de atravesar Inception y compuertas formales resulta desproporcionado respecto a la complejidad del cambio.

### 5. ¿Lo usarían en su proyecto? ¿Por qué?
- **Sí, definitivamente.** En un proyecto como **SIGESA-UMSS**, que gestiona acreditaciones académicas con reglas de negocio institucionales estrictas, gobernanza documental inmutable (`docs/baseline/`) y requerimientos rigurosos de concurrencia y auditoría (12-Factor), el uso de AI-DLC previene la degradación arquitectónica típicamente provocada por asistentes de IA que generan código rápido pero arquitectónicamente indisciplinado. La combinación del perfil **Express** para features estándar y perfiles más profundos para módulos troncales ofrece el balance ideal entre velocidad y garantía de calidad para el equipo de desarrollo.

---

## 7. Antes de entregar

- [x] Las dos ramas están en el repositorio y las pruebas pasan en ambas.
- [x] `aidlc/` y `.aidlc-version` están versionados en la rama B.
- [x] No hay secretos, `.env` ni archivos `*.local.json` en los commits.
- [x] Si es en grupo, la tabla de responsables está completa.
