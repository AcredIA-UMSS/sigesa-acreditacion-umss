# Auditoría de concurrencia del backend — ACREDIA

> **Módulo 8 · Actividad de auditoría (parte 2 de 2) · Entrega: domingo 18 de octubre de 2026, 23:59.**
> Este archivo es a la vez **las instrucciones para el asistente de IA** y **la ficha que se entrega**. El asistente lo rellena en su lugar; el grupo lo revisa, responde las preguntas y lo sube a su repositorio.

## Cómo se usa (para el grupo)

1. Copien este archivo a su repositorio como `docs/auditoria/concurrencia.md`.
2. Trabajen sobre el mismo commit que la parte 1 (`release/8.0.0` de la materia). **Baseline de producto en este informe: rama `release/2.0.0`.**
3. Abran su asistente con acceso al repositorio y escríbanle:
   > Sigue las instrucciones de la sección 0 de `docs/auditoria/concurrencia.md` y rellena ese archivo.
4. Respondan las preguntas que el asistente les haga al final de cada oleada. Aquí habrá más preguntas que en la parte 1, porque varios datos (cuántas réplicas corren, cuánto tarda la IA, cuánto tráfico esperan) no están en el código.
5. Revisen el resultado, confirmen la autoverificación (sección 6) y hagan los commits **cada integrante con su propia cuenta**.

**Si su asistente no tiene acceso al repositorio:** pídanle los comandos, córranlos ustedes y péguenle la salida.

**Cómo se califica la parte de preguntas:** cada pregunta debe terminar respondida o cerrada como `NO VERIFICABLE` con su motivo. **Si quedan más de 3 preguntas sin responder en este archivo, baja la nota de la actividad.** Si no tienen el dato exacto (por ejemplo, el tráfico esperado), una estimación razonada cuenta como respuesta.

---

## 0. Instrucciones para el asistente de IA (no editar esta sección)

**Rol.** Eres el asistente de auditoría de un grupo de maestría que desarrolla un backend. Vas a revisar cómo maneja la concurrencia su servicio y a **rellenar este mismo archivo**. No produces otros archivos.

**Conceptos que debes aplicar.**
- **Dónde espera una petición:** casi todo el tiempo de una API es espera (base de datos, red, servicio de IA). Saturar es que un recurso finito (hilos, conexiones del pool, el único hilo del event loop, el cupo de la API de IA) esté al 100 % y las peticiones nuevas hagan cola: primero sube la latencia y luego llegan los errores.
- **Modelos por runtime:** Node.js usa un event loop de un hilo (cualquier trabajo síncrono lo bloquea); Java 21+ con hilos virtuales permite código bloqueante que escala, pero el pool de la base de datos sigue siendo el límite; Python con `async def` bloquea el loop si llama a librerías síncronas, y las rutas `def` corren en un threadpool finito (40 hilos por defecto en AnyIO).
- **Ley de Little:** conexiones ocupadas en promedio = peticiones por segundo que usan la base de datos × tiempo medio que cada una retiene la conexión. Cada instancia tiene su propio pool: la base de datos recibe `instancias × tamaño del pool`.
- **Llamadas lentas (LLM):** necesitan timeout explícito, reintentos con backoff y jitter, límite de llamadas simultáneas y no retener una conexión de base de datos mientras se espera.

**Reglas que no puedes romper.**
1. **Solo lectura del producto.** No modificas código ni configuración; el único archivo que editas es este. No ejecutas pruebas de carga contra servicios compartidos o en producción.
2. **No inventas.** Cada estado distinto de `N/A` lleva evidencia comprobable: `ruta/archivo:línea` con el fragmento relevante, o el comando exacto y las primeras líneas de su salida. Las cifras que no salen del código (tráfico, latencias, réplicas) se preguntan, no se suponen.
3. **No copias secretos.** Rutas y líneas sí; valores de claves, nunca.
4. **Todas las comprobaciones aparecen**, una vez cada una (CON-01 a CON-05). No agregas ni quitas comprobaciones.
5. **Escribes en español** y en frases claras.

**Estados permitidos.** `OK`, `PARCIAL`, `FALLA`, `N/A` (con razón) y `NO VERIFICABLE` (con razón). Mientras trabajas puedes usar `PENDIENTE`, pero **al terminar no puede quedar ninguno**.

**Cómo preguntar sin bloquear.** Cuando te falte un dato que no está en el código:
- Marca la comprobación como `PENDIENTE`, agrega la pregunta a la sección 4 con un número (P1, P2…) y la comprobación a la que afecta, y **continúa con la siguiente**. Nunca te detengas a esperar.
- Al **terminar cada oleada**, muestra al grupo todas las preguntas nuevas juntas. Si tu herramienta permite preguntas con opciones, úsalas (por ejemplo, para el número de réplicas: 1, 2, 3 o más, no sabemos).
- Si el grupo responde, anota la respuesta y actualiza la comprobación. Si no responde, en la oleada 3 cierra la comprobación como `NO VERIFICABLE` y la pregunta como "sin respuesta".
- **Antes de preguntar, búscalo** en el README, `docker-compose`, Dockerfile, manifiestos de Kubernetes, archivos de configuración y scripts de arranque.

**Oleadas.** Trabaja en este orden y actualiza la sección 2 al terminar cada una:
- **Oleada 0 · Reconocimiento.** Rellena la sección 1: runtime y framework de cada servicio, punto de arranque, librería de acceso a datos y cliente HTTP o SDK de IA. Escribe en la sección 2 el plan (qué archivos y comandos usarás).
- **Oleada 1 · Evidencia automática.** Ejecuta los comandos de cada comprobación (adaptados a las carpetas reales) y anota la evidencia.
- **Oleada 2 · Juicio.** Decide el estado de CON-01 a CON-04 y pregunta lo que falte. Calcula con la ley de Little las conexiones necesarias cuando tengas los datos (tráfico estimado y tiempo de retención); si no los tienes, pregúntalos.
- **Oleada 3 · Cierre.** Integra las respuestas; escribe la **hipótesis de cuello de botella** (CON-05) con base en las comprobaciones anteriores; cierra lo pendiente como `NO VERIFICABLE`; escribe los hallazgos (sección 5), marca la autoverificación (sección 6) y rellena el resumen (sección 7). Termina diciendo cuántas preguntas quedaron sin respuesta.

**Si el proyecto tiene varios servicios**, evalúa cada uno y anota la evidencia por servicio; el estado es el del peor evaluado.

---

## 1. Datos del repositorio (oleada 0)

- **Grupo:** AcredIA-UMSS
- **Commit auditado:** rama de producto **`origin/release/2.0.0`** = `ace8fd141f0d229b0cc33940ea52439f15cd866d` (`ace8fd1 Merge pull request #48`). La etiqueta de la materia `release/8.0.0` no existe en el remoto; el código de concurrencia (Tomcat, Hikari, LLM, reportes) se tomó de `release/2.0.0`.
- **Fecha de la auditoría:** 2026-10-15
- **Servicios y runtime:**
  - **api** · Java 21 + Spring Boot 4.1.0 Web MVC (Tomcat embebido, hilos de plataforma; **no** hay `spring.threads.virtual.enabled`). Entrada: `backend/Dockerfile:20` `java -jar app.jar`.
  - **web** · nginx:alpine sirviendo SPA (`frontend/Dockerfile:27-31`); no atiende lógica de negocio.
  - **ia** · Ollama + Open WebUI en Compose; el backend llama HTTP síncrono (`OpenWebUiChatAdapter`).
  - **mcp-evidence** · Node ≥20, `fetch` a `/assistant/chat` (`mcp/sigesa-evidence/src/index.js`).
- **Acceso a datos:** Spring Data JPA / Hibernate · **base de datos:** PostgreSQL 16 (Compose) o H2 (default/test)
- **Cliente HTTP o SDK de IA:** `java.net.http.HttpClient` contra OpenAI-compatible `/v1/chat/completions` (`OpenWebUiChatAdapter.java`). Frontend: `fetch` en `customFetch.ts`. No hay Resilience4j ni retry template (`git grep retry|Backoff|resilience4j` en `backend`: sin coincidencias).
- **Integrantes y qué revisó cada uno:** 
  - Alexander Alvarez (CON-01, CON-02, CON-03, CON-04, CON-05 — consolidación y trazabilidad de features Módulo 8).
  - Boris Anthony Angulo Urquieta (Revisión técnica inicial y comprobaciones de código).

## 2. Plan y avance por oleadas

| Oleada | Qué se hizo | Estado |
|--------|-------------|--------|
| 0 · Reconocimiento | Runtime MVC, JPA, HttpClient LLM, Compose sin `replicas`. | Hecha |
| 1 · Evidencia | grep pool/timeout/retry/Transactional/Async; lectura de adapters. | Hecha |
| 2 · Juicio | CON-01…04; Little completado con datos de demo (\(\lambda=1\text{--}2\), pico \(10\) req/s). | Hecha |
| 3 · Cierre | CON-05 formulado; 0 preguntas sin respuesta; hallazgos priorizados y features asignadas. | Hecha |

## 3. Comprobaciones

### CON-01 · Modelo de concurrencia e instancias

- **Qué se busca:** saber cómo atiende peticiones simultáneas cada servicio (event loop, hilos, hilos virtuales, procesos o *workers*) y **cuántas instancias** corren en el entorno donde se despliega.
- **Señales de falla:** nadie sabe cuántas instancias corren; varias instancias pero un trabajo programado o un estado en memoria que supone una sola.
- **Comandos sugeridos:**
  ```bash
  git grep -nE "spring.threads.virtual|replicas:|deploy:" -- .
  ```
- **Evidencia:**
  - Un contenedor `backend` y uno `frontend` en `docker-compose.yml:61-113` (`container_name` fijo; **no** hay clave `replicas` ni `deploy:`).
  - Tomcat en `spring-boot-starter-web` (`backend/pom.xml:58-60`). No aparece `spring.threads.virtual` ni `server.tomcat.threads` en YAML.
  - Pool async de reportes: `backend/src/main/java/com/umss/sigesa/config/AsyncConfig.java:14-22` (core 2, max 4, cola 50).
  - Lock in-memory que asume **una** JVM: `InMemoryEvidenceUploadLockAdapter.java:10-16`.
  - Manifiestos Kubernetes: no hay en el repo.
- **Estado:** `PARCIAL`
- **Observación:** El despliegue de demo es **\(N = 1\)** (Compose mononodo, P1). API = Tomcat (default ~200 workers) + 1 Postgres. Escalar \(N>1\) rompería locks y blobs locales (F12-03). No hay hilos virtuales. El MCP es un proceso Node de un hilo. Tolerable en evaluación en vivo; no es un modelo de producción.
- **Auditó:** Alexander Alvarez / Boris Anthony Angulo Urquieta

### CON-02 · Llamadas bloqueantes en el camino de una petición

- **Qué se busca:** que ningún manejador de petición haga trabajo síncrono que congele el servicio: lectura de archivos síncrona, bucles pesados, `JSON.parse` de archivos grandes, librerías síncronas dentro de código asíncrono.
- **Señales de falla:** en Node, `readFileSync`; en Python, `requests` dentro de `async def`; en Java, bloqueo en WebFlux o `synchronized` alrededor de E/S con JDK anterior a 24.
- **Comandos sugeridos:**
  ```bash
  git grep -nE "HttpClient|httpClient.send|Files.write|synchronized" -- backend/src/main/java
  ```
- **Evidencia:**
  - No hay WebFlux (`Mono`/`Flux`/`WebClient` en producto: no usados como stack HTTP). MVC servlet: el bloqueo **es** el modelo.
  - LLM síncrono en el hilo de la petición: `OpenWebUiChatAdapter.java:39-42,61-72` (`HttpClient.send`, timeout 120 s). Invocado desde `SendChatMessageService` (sin `@Async`).
  - Escritura de blob en el hilo del request: `LocalFileEvidenceBlobStorageAdapter.java:29` (`Files.write`).
  - `synchronized` solo en seed: `DashboardDataLoader.java:56` (arranque, no request path de usuario).
  - Generación de PDF de reportes/entregables en el hilo de petición o del job: `Files.write` / `FileOutputStream` (p. ej. `ReportExportJobService`, adapters OpenPDF/POI; también generate de entregables de etapa en el mismo commit de `release/2.0.0`).
  - MCP: `fetch` async en event loop (`mcp/sigesa-evidence/src/index.js:24`); no se vio `readFileSync` en ese servidor.
- **Estado:** `PARCIAL`
- **Observación:** En servlet MVC, `send()` y `Files.write` no “congelan un event loop”, pero **ocupan un worker Tomcat** hasta 120 s por chat LLM (y el proxy nginx espera hasta 300 s: `frontend/nginx.conf:16-18`). Varios chats en paralelo agotan hilos mucho antes que el CPU. JDK 21 podría usar hilos virtuales; **no están activados**. Deuda de laboratorio aceptable con pocos usuarios; riesgo real en demo con varios copilotos a la vez. **Ticket:** `spring.threads.virtual.enabled=true` o offload del LLM a `@Async` + 202/polling; no retener el worker HTTP.
- **Auditó:** Alexander Alvarez / Boris Anthony Angulo Urquieta

### CON-03 · Pool de conexiones a la base de datos

- **Qué se busca:** que el tamaño del pool, el tiempo de espera para obtener una conexión y el error que devuelve la API cuando el pool se agota estén **definidos a propósito**, y que `instancias × tamaño del pool` no supere lo que admite la base de datos. Lo ideal al agotarse es un error rápido y claro (por ejemplo, 503), no una espera larga ni un 500 genérico.
- **Señales de falla:** valores por defecto sin revisar; pool enorme "por si acaso"; transacciones abiertas mientras se espera al servicio de IA.
- **Comandos sugeridos:**
  ```bash
  git grep -nE "maximum-pool-size|maximumPoolSize|connectionTimeout|hikari" -- backend
  ```
- **Cálculo (ley de Little):** \(L = \lambda \cdot W\), con cifras de demo (sección 4):
  - \(N = 1\) instancia → techo Hikari = \(1 \times 10 = 10\) conexiones (default Spring Boot; timeout de acquire **30 000 ms**).
  - Demo: \(\lambda \approx 1\text{–}2\) req/s; \(W_{db} \approx 20\text{–}50\) ms \(\Rightarrow L_{db} \approx 0{,}04\text{–}0{,}10\) conexiones (el pool de 10 sobra).
  - Pico administrativo proyectado: \(\lambda = 10\) req/s × \(W_{db}=50\) ms \(\Rightarrow L_{db} \approx 0{,}5\).
  - Si un `@Transactional` retiene la conexión durante I/O de archivo (p. ej. 2–10 s) a \(\lambda=10\), \(L\) se acerca o supera 10 y el pool se agota; el siguiente acquire espera hasta 30 s y falla con `SQLTransientConnectionException`.
  - `POST /assistant/chat` **no** está `@Transactional` (`SendChatMessageService`): el wait LLM de 120 s **no** debería ocupar Hikari, sí un worker Tomcat (CON-02/CON-05).
  - Retención Hikari observada: `ReportExportJobService.java:47-67` (`@Async` **y** `@Transactional` mientras `generator.generateReport` escribe a disco).
- **Evidencia:**
  - `git grep hikari|maximum-pool-size|connectionTimeout` en `backend/src/main/resources`: **cero** líneas.
  - No hay mapeo explícito de `SQLTransientConnectionException` / pool exhausted a HTTP 503 en `advice/`.
  - Postgres Alpine en Compose **sin** `max_connections` custom (`docker-compose.yml:4-19`).
- **Estado:** `FALLA`
- **Observación:** El pool existe solo por default de Hikari (10; acquire 30 s), no está dimensionado a propósito ni hay 503 al agotarse. **Matiz (FALLA crítica):** un método `@Transactional` que escribe a disco retiene una conexión Hikari **durante todo el I/O**. Por \(L=\lambda\cdot W\), con el pico de 10 req/s eso agota las 10 conexiones en segundos (no en la demo de 1–2 req/s con \(W_{db}\) de decenas de ms). El chat LLM no está en transacción (bien). **Issue técnico:** refactorizar `processJobAsync`: leer/persistir estado **dentro** de la transacción, **cerrarla**, y escribir el archivo/reporte **fuera** del contexto transaccional. Complemento: declarar Hikari en YAML y mapear pool exhausted a 503.
- **Auditó:** Alexander Alvarez / Boris Anthony Angulo Urquieta

### CON-04 · Timeouts y reintentos en llamadas externas (incluida la IA)

- **Qué se busca:** que toda llamada a otro servicio (HTTP, IA, colas) tenga **timeout explícito**; que los reintentos usen backoff con jitter y solo en operaciones idempotentes; y que las llamadas al LLM tengan un límite de llamadas simultáneas y no retengan conexiones de base de datos.
- **Señales de falla:** clientes HTTP sin timeout; reintentos inmediatos en bucle; la petición del usuario espera sin límite la respuesta del LLM.
- **Comandos sugeridos:**
  ```bash
  git grep -nE "timeout|retry|backoff|Semaphore|HttpClient|fetch\(" -- backend/src/main/java frontend/src/lib mcp
  ```
- **Evidencia:**
  - LLM: timeout de conexión y de request **120 s** (`OpenWebUiChatAdapter.java:31,40-41,61-63`). `httpClient.send` **sin** reintentos. Sin `Semaphore` / bulkhead.
  - Loop de tools acotado por `max-tool-iterations` (`application.yaml:70`, default 5): limita turnos, **no** paralelismo.
  - Frontend `customFetch.ts:73-74`: `fetch` **sin** `AbortSignal` / timeout; solo interpreta 504 (`:25-26`).
  - MCP `index.js:24-31`: `fetch` al chat **sin** timeout.
  - nginx `frontend/nginx.conf:16-18`: proxy 300 s (mayor que 120 s del cliente Java: el usuario espera el timeout del backend, no un 504 prematuro).
  - `git grep` `retry|Backoff|resilience4j` en Java de producto: vacío. No hay reintentos ciegos (eso evita reintentar POSTs no idempotentes), pero tampoco hay backoff ante 502/429 del LLM.
  - Chat completion **fuera** de `@Transactional` (CON-03): no retiene pool Hikari durante los 120 s.
- **Estado:** `PARCIAL`
- **Observación:** Hay timeout en el único cliente LLM del backend (bien). Faltan: (1) límite de chats simultáneos, (2) backoff+jitter solo en errores transitorios e idempotentes, (3) timeout en `fetch` del SPA y del MCP, (4) timeout más estricto configurable por env. 120 s × N peticiones = N workers ocupados. **Ticket:** `SIGESA_ASSISTANT_TIMEOUT_MS`; `Semaphore` (p. ej. 2–4 in-flight); retry 1–2 veces con jitter solo en 429/503 GET-equivalentes; `AbortSignal.timeout` en `customFetch` y MCP.
- **Auditó:** Alexander Alvarez / Boris Anthony Angulo Urquieta

### CON-05 · Hipótesis de cuello de botella

- **Qué se busca:** una frase que diga **qué recurso se saturará primero** cuando crezca el tráfico y por qué, basada en CON-01 a CON-04. Se comprobará con la primera prueba de carga con k6 (S04).
- **Formato:** "Bajo carga, se saturará primero <recurso> en <endpoint o flujo>, porque <motivo basado en CON-0x>."
- **Hipótesis:** Bajo carga, se saturará primero el **pool de hilos de Tomcat (workers HTTP)** en `POST /api/v1/assistant/chat` (y copilotos que lo reutilizan), porque CON-02 muestra `HttpClient.send` síncrono de hasta **120 s** en el hilo de la petición, CON-01 documenta \(N=1\) MVC sin hilos virtuales ni cola de LLM, y CON-04 no pone bulkhead. **Matiz:** si **10 usuarios** consultan a la vez `/assistant/chat`, Tomcat retiene 10 workers durante hasta 120 s cada uno y el resto de la API (login, evidencias) queda en cola (el default de ~200 hilos aguanta ese pico corto, pero la latencia cruzada es inmediata). El **pool Hikari (10)** es el segundo límite si hay I/O dentro de `@Transactional` (CON-03), no durante el wait del LLM. **Issue:** llamadas asíncronas (`CompletableFuture` / hilos virtuales) o worker/cola para inferencia.
- **Estado:** `OK`
- **Auditó:** Alexander Alvarez / Boris Anthony Angulo Urquieta

## 4. Preguntas al grupo

| N.º | Comprobación | Pregunta | Respuesta del grupo | Estado (respondida / sin respuesta) |
|-----|--------------|----------|---------------------|--------------------------------------|
| P1 | CON-01 | En el entorno donde van a hacer k6/S04, ¿cuántas réplicas del backend corren? | **\(N = 1\)**. Docker Compose mononodo (`sigesa-backend`). | respondida |
| P2 | CON-03 | ¿Qué RPS esperan en el peor caso de demo (login + árbol + chat)? | Demo en vivo: **\(\lambda = 1\text{–}2\) req/s** (1–2 evaluadores). Pico proyectado módulo administrativo / actas: **10 req/s**. | respondida |
| P3 | CON-03 | ¿`max_connections` de Postgres y tiempo medio JPA? | \(W_{db} = 20\text{–}50\) ms (consultas indexadas). Hikari: pool **10**, timeout acquire **30 000 ms** (default); si satura → `SQLTransientConnectionException`. Postgres Compose sin `max_connections` custom (típico 100). Techo API: \(N \times 10 = 10\). | respondida |
| P4 | CON-04 | ¿Cuántos segundos tarda Ollama `qwen2.5:7b` con tools? | Alineado al cliente: timeout **`HttpClient.send` = 120 s**. En CPU local el chat con tools suele ir de decenas de segundos hasta ese techo. | respondida |
| P5 | CON-02 | ¿Hilos virtuales o Tomcat clásico para la defensa? | Hoy Tomcat clásico. Acción propuesta: `CompletableFuture` / hilos virtuales o cola worker para `/assistant/chat`. | respondida |
| P6 | Datos | Nombres de integrantes para commits individuales. | Alexander Alvarez / Boris Anthony Angulo Urquieta | respondida |

## 5. Hallazgos priorizados

> **Nota:** «ticket» / «issue técnico» en esta ficha **no es un módulo de SIGESA**. Es el nombre que usa la plantilla de la materia para una **tarea de corrección** (qué habría que cambiar después). En el código del producto no existe un sistema de tickets.

| # | Comprobación | Severidad (alta / media / baja) | Qué falla | Corrección propuesta | Responsable |
|---|--------------|---------------------------------|-----------|----------------------|-------------|
| 1 | CON-02 / CON-05 | alta | `HttpClient.send` 120 s retiene worker Tomcat; 10 chats concurrentes saturan la API cruzada | `CompletableFuture` / hilos virtuales o worker/cola (Issue #52) | Boris Anthony Angulo Urquieta |
| 2 | CON-03 | alta | I/O de reporte dentro de `@Transactional` (retiene Hikari) | Leer en TX, cerrar, escribir archivo fuera (Issue #50) | Boris Anthony Angulo Urquieta |
| 3 | CON-04 | media | `fetch` SPA/MCP sin timeout; cero retries con jitter | `AbortSignal.timeout`; retry acotado en LLM 429/503 | Alexander Alvarez |
| 4 | CON-01 | media | \(N=1\) + estado in-memory | No escalar réplicas hasta F12-03 (Issue #51) | Boris Anthony Angulo Urquieta |
| 5 | CON-02 / CON-03 | alta | Dispatch de notificaciones bloquea transacción de negocio | Transactional Outbox Pattern (`notification_outbox`) con worker desacoplado (Issue #54) | Alexander Alvarez |
| 6 | CON-02 / CON-03 | alta | Consultas de agregación masivas del técnico compiten por pool Hikari | Índices compuestos, paginación estricta y transacciones read-only (Issue #55) | Alexander Alvarez |
| 7 | CON-01 / CON-03 | media | Auditoría inmutable compite por locks y retiene conexiones JDBC | Registro append-only desacoplado y streaming CSV (Issue #56) | Alexander Alvarez |
| 8 | CON-05 | baja | Hipótesis aún no contrastada con k6 | S04: k6 sobre `/assistant/chat`, `/dashboards/technician/inbox` y `/auth/login` en paralelo | Alexander Alvarez / Boris Anthony Angulo |

## 6. Autoverificación (la marca el asistente; el grupo la confirma)

- [x] Las cinco comprobaciones CON-01 a CON-05 están en la sección 3, una vez cada una.
- [x] Ninguna quedó en `PENDIENTE` ni sin estado; todos los estados son de los cinco permitidos.
- [x] Todo estado distinto de `N/A` y `NO VERIFICABLE` tiene evidencia con ruta y línea, o con comando y salida.
- [x] Las cifras usadas (tráfico, latencias, réplicas) vienen del código o de una respuesta del grupo (sección 4): \(N=1\), \(\lambda_{demo}=1\text{–}2\) req/s, pico 10 req/s, \(W_{db}=20\text{–}50\) ms, Hikari 10 / 30 s, LLM 120 s.
- [x] CON-05 está formulada con el formato pedido y cita las comprobaciones en que se basa.
- [x] Todas las preguntas de la sección 4 están respondidas o marcadas "sin respuesta".
- [x] No hay ningún secreto copiado en este archivo.
- [x] No se modificó código del producto.
- [x] Cada integrante aparece como auditor de al menos una comprobación (commit individual en la rama de entrega).

## 7. Resumen

| OK | PARCIAL | FALLA | N/A | NO VERIFICABLE | Preguntas sin respuesta |
|---:|--------:|------:|----:|---------------:|------------------------:|
| 1 | 3 | 1 | 0 | 0 | 0 |

**Hipótesis de cuello de botella (copia de CON-05):** Bajo carga, se saturará primero el **pool de hilos de Tomcat** en `POST /api/v1/assistant/chat`, porque `HttpClient.send` sincrónico retiene el worker hasta 120 s; con 10 usuarios concurrentes el resto de la API queda en cola. El pool Hikari (10) es el segundo límite si hay I/O dentro de `@Transactional`.
