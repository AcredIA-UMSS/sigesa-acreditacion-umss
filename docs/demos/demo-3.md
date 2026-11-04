# Demo 3 — AcredIA / SIGESA (`release/8.3.0`)

Registro de funcionalidades y requisitos técnicos por integrante para la **Demo 3 (Miércoles 4 de noviembre de 2026)**. El release **8.3.0** reúne los aportes del equipo con trazabilidad individual en commits y documentación viva.

**Repositorio:** [AcredIA-UMSS/sigesa-acreditacion-umss](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss)  
**Rama:** `release/8.3.0`  
**Tag:** `v8.3.0`

---

## 1. Tabla Resumen de Integrantes

| Integrante | Funcionalidades Presentadas | Tipo | Caso de Uso / Requisito | Repositorio |
|---|---|---|---|---|
| **Alexander Alvarez** | **Exportación Asíncrona del Dossier Digital en ZIP Jerárquico** + **2️⃣🅱️ Análisis de Extracción E3 (ADR-001)** | 1 Grande + 1 Técnico | [`FSD-UC-032`](../product/uc/FSD-UC-032.md) · [`ADR-001`](../adr/ADR-001-extraer-modulo.md) | [AcredIA-UMSS/sigesa-acreditacion-umss](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss) (`release/8.3.0`) |
| **Boris Anthony Angulo Urquieta** | **1️⃣ Imagen y Stack de Producción Endurecido** + **Gestión de Evidencias (Carga/Borrado/Visor)** | 1 Técnico + 1 Mediana | Ops/NFR (`imagen-o-podman.md`) · [`FSD-UC-004`](../product/uc/FSD-UC-004.md) / [`005`](../product/uc/FSD-UC-005.md) | [AcredIA-UMSS/sigesa-acreditacion-umss](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss) (`release/8.3.0`) |
| **Aylen Gonzáles** | **Semáforo Ejecutivo de Jefatura** + **Bandeja de Dictamen Técnico** | 2 Medianas | [`FSD-UC-013`](../product/uc/FSD-UC-013.md) · [`FSD-UC-012`](../product/uc/FSD-UC-012.md) | [AcredIA-UMSS/sigesa-acreditacion-umss](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss) (`release/8.3.0`) |
| **Marlene Velasquez** | **Validación de Registro de Usuarios** + **Login Institucional & Health/Auditoría** | 2 Pequeñas / Medianas | [`FSD-UC-002`](../product/uc/FSD-UC-002.md) · [`FSD-UC-001`](../product/uc/FSD-UC-001.md) | [AcredIA-UMSS/sigesa-acreditacion-umss](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss) (`release/8.3.0`) |

---

## 2. Lo Técnico de Esta Demo

### 1️⃣ Imagen y Stack de Producción (Boris Anthony Angulo Urquieta)

Cumplimiento estricto del requerimiento técnico de despliegue sobre contenedor optimizado y seguro:

| Requisito | Implementación | Evidencia / Ubicación |
|---|---|---|
| **Multi-stage build** | Etapa 1: `eclipse-temurin:21-jdk-alpine` para build y dependencias offline.<br>Etapa 2: `eclipse-temurin:21-jre-alpine` conteniendo únicamente el JAR ejecutable. | [`backend/Dockerfile`](../../backend/Dockerfile) |
| **Compose reproducible** | Despliegue con `docker compose up -d --wait` desde clon limpio. Levanta PostgreSQL con seeds, MinIO/Silo S3, Ollama, Open-WebUI, Backend y Frontend. | [`compose.yaml`](../../compose.yaml) / [`docker-compose.yml`](../../docker-compose.yml) |
| **Seguridad No-Root** | Usuario y grupo sin privilegios `sigesa` (`uid=100 gid=101`). Directorios `/app/data` y `/app/uploads` con permisos acotados. | [`backend/Dockerfile`](../../backend/Dockerfile#L14-L18) |
| **Auditoría Trivy** | **0 CVEs en el Sistema Operativo** (0 High, 0 Critical). Reducción de 92 a 46 vulnerabilidades totales. | [`docs/laboratorios/imagen-o-podman.md`](../laboratorios/imagen-o-podman.md) |
| **Auditoría hadolint** | Sin hallazgos de severidad Error o Warning. Solo informativo `DL3066` por usuario declarativo. | [`docs/laboratorios/imagen-o-podman.md`](../laboratorios/imagen-o-podman.md) |
| **Contexto de build** | `.dockerignore` configurado; reducción del contexto enviado al daemon de **124 MB a 1.2 MB**. | [`backend/.dockerignore`](../../backend/.dockerignore) |

---

### 2️⃣ Análisis Técnico: 🅱️ Extracción a Microservicio E3 (Alexander Alvarez)

Evaluación experimental sobre la viabilidad de extraer el módulo asíncrono de notificaciones (`MOD-NOTIF` / `notification_outbox`) fuera del monolito modular frente a mantenerlo en proceso.

- **Documento de Decisión Arquitectónica:** [`ADR-001: Mantener el módulo de notificaciones en el monolito`](../adr/ADR-001-extraer-modulo.md)
- **Informe de Benchmark:** [`bench/e3/INFORME.md`](../../bench/e3/INFORME.md)
- **Scripts K6 y Suite:** [`bench/e3/k6-e3.js`](../../bench/e3/k6-e3.js) y [`bench/e3/run_e3.sh`](../../bench/e3/run_e3.sh)

#### Formas Evaluadas e Interfaces

1. **Dentro del monolito (`EXTRACTION_MODE=in_process`):**
   - Interfaz en memoria directa mediante puerto `NotificationModulePort` y adaptador `InProcessNotificationModuleAdapter`.
   - Cero serialización de red adicional; invocación directa de repositorios JPA.
2. **Como servicio aparte (`EXTRACTION_MODE=http`):**
   - Proceso independiente de backend (`SIGESA_APP=notification-service`) ejecutado como contenedor sidecar detrás de **Toxiproxy** (`sigesa-toxiproxy:8474`).
   - Adaptador cliente `HttpNotificationModuleAdapter` con Circuit Breaker (`FailureCircuitBreaker`, umbral 5 fallos en ventana de 2 s).

#### Resultados Cuantitativos (Dataset real PostgreSQL con 150,000 filas)

| Criterio / Hipótesis | Cifra Medida | Límite Falsación | ¿Se Acepta Microservicio? |
|---|---|---|---|
| **H1: Sobrecosto de latencia p95** | p95 HTTP 28.90 ms vs in-process 11.56 ms ($\mathbf{\Delta p95 = +17.34\text{ ms}}$) | $\le 15.00\text{ ms}$ | ❌ **Rechazado** (costo de serialización + salto de red) |
| **H2: Aislamiento ante corte de red** | Notificaciones 100% error; `GET /api/health` tuvo **33/2496 colgadas (1.322 %)** | Error health $\le 1.00\%$ | ❌ **Rechazado** (agotamiento del pool HTTP contagió la app) |
| **H3: Tiempo de inicialización** | Mediana arranque 9.213 s vs 7.660 s (**1.20×**) | $\le 2.00\times$ | ✅ Cumple |

**Veredicto Arquitectónico ([ADR-001]):** Se desestima la extracción a microservicio y se mantiene el módulo dentro del monolito modular.

#### Conexión y Extrapolación con Dossier Exports (`MOD-EXP`)

El caso de uso de **Exportación del Dossier Digital** comparte la misma lección aprendida:
- En lugar de forzar una extracción prematura a microservicio que penalizaría la latencia e introduciría fallos de red, se implementó en el monolito modular utilizando **streaming puro hacia S3/MinIO con `S3MultipartOutputStream` y Virtual Threads acotados (8 hilos)**.
- Gracias a la arquitectura hexagonal (`DossierExportPort`, `ExportProcessDossierUseCase`) y la tabla de jobs desacoplada (`tb_report_export_job`), si en el futuro la carga de compresión ZIP justificara un worker externo, la interfaz de dominio permanecerá inmutable.

---

## 3. Detalle de Funcionalidades por Integrante

### Alexander Alvarez — Exportación Asíncrona de Dossier Digital (`FSD-UC-032`)

* **Caso de Uso:** [`FSD-UC-032`](../product/uc/FSD-UC-032.md) — *Exportación Asíncrona del Dossier Digital de Acreditación (ZIP Jerárquico y Foliado)*
* **Design Doc:** [`DD-UC-032`](../design/DD-UC-032.md)
* **Issue / PR:** Issue #73 / PR #74 ([`feat/73-dossier-zip-export`](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss/tree/feat/73-dossier-zip-export))

#### Criterios de Aceptación Demostrados

| Criterio | Regla de Negocio | Implementación y Verificación |
|---|---|---|
| **Zero Disco Local & Heap** | `FSD-BR-32.1` | No se generan archivos `.tmp` en disco efímero ni se carga el ZIP completo en RAM. Streaming continuo con `S3MultipartOutputStream` y `ZipDossierExportStreamingAdapter`. |
| **Límites de Concurrencia** | `FSD-BR-32.2` | Semáforo global (4 a 8 concurrentes) que responde `429 Too Many Requests` (`Retry-After: 60`). Máximo 1 exportación activa por usuario con `409 Conflict`. |
| **Purga y Ciclo de Vida** | `FSD-BR-32.3` | TTL de 24–48h en bucket S3. Tarea programada `DossierTtlCleanupJob` y limpieza de jobs zombies/huérfanos al startup. |
| **RBAC Estricto por Carrera** | `FSD-BR-32.4` | Coordinador de Calidad (`ROLE_CC`) solo puede exportar procesos de sus carreras asignadas. Intentos cruzados responden `404 Not Found` (ocultamiento seguro). |
| **Lectura Paralela Acotada** | `FSD-BR-32.5` | Descarga de evidencias en paralelo con pool acotado de 8 hilos virtuales y backpressure para no saturar el storage ni el heap. |
| **Tolerancia a Fallos** | `FSD-BR-32.7` | Si una evidencia no existe físicamente en el storage, se genera un placeholder `[FOLIO]_ERROR_DOCUMENTO_NO_DISPONIBLE.txt` sin abortar el empaquetado. |
| **UX y Entrega Offline** | NFR / UI | Botón «Exportar Dossier Completo (ZIP)» en `/procesos/{processId}`, hook `useDossierExport`, polling asíncrono y descarga mediante Presigned URL firmada de S3. Incluye `00_Indice_Maestro_Cumplimiento.html` y `00_Manifiesto_Checksums.txt`. |

---

### Boris Anthony Angulo Urquieta — Stack Producción & Gestión de Evidencias

* **Stack:** Dockerfile multi-stage, compose reproducible, hardening de seguridad Trivy/Hadolint.
* **Casos de Uso:** [`FSD-UC-004`](../product/uc/FSD-UC-004.md) / [`FSD-UC-005`](../product/uc/FSD-UC-005.md) — *Carga, visualización y eliminación de evidencias con Storage S3*.
* **Criterios:** Subida de evidencias vinculadas a indicadores, previsualización de blobs (`/api/v1/document-assets/{id}/content`), borrado seguro condicionado a que no esté en estado APROBADO (HTTP 409 `EVIDENCE_IMMUTABLE`).

---

### Aylen Gonzáles — Semáforo Ejecutivo y Bandeja de Dictamen

* **Casos de Uso:** [`FSD-UC-013`](../product/uc/FSD-UC-013.md) / [`FSD-UC-012`](../product/uc/FSD-UC-012.md)
* **Criterios:**
  - Panel ejecutivo `/executive/semaphore` con cálculo de semáforo por carrera y agregación de cumplimiento.
  - Bandeja técnica `/technician/inbox`: filtro por estado, modal accesible para revisión de observaciones y emisión de dictamen de aprobación o rechazo.

---

### Marlene Velasquez — Registro de Usuarios, Autenticación y Health

* **Casos de Uso:** [`FSD-UC-002`](../product/uc/FSD-UC-002.md) / [`FSD-UC-001`](../product/uc/FSD-UC-001.md)
* **Criterios:**
  - Validación de usuarios en cliente y servidor (`RegisterUserRequest`): dominio `@umss.edu.bo`, celular de 8 dígitos y complejidad de clave.
  - Login institucional con emisión de JWT.
  - Endpoint de liveness `GET /api/health` respondiendo `{"status":"UP"}` sin requerir autenticación.
