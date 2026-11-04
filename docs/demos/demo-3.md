# Demo 3 — AcredIA / SIGESA (`release/8.3.0`)

**Fecha de demo:** Miércoles 4 de noviembre de 2026  
**Release:** `8.3.0`  
**Rama:** `release/8.3.0`  
**Tag previsto:** `8.3.0` / `v8.3.0`  
**Repositorio:** [AcredIA-UMSS/sigesa-acreditacion-umss](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss)  

Entrega de aula: enlace al repositorio + nombre del release (`8.3.0`).  
Registro de aportes: [`8.3.0-aportes.md`](../../8.3.0-aportes.md) (raíz). Cada commit con la identidad individual de su autor.

---

## 1. Compromiso por Integrante

Cada integrante presenta **1 a 3** funcionalidades del release (1 grande, 2 medianas o 3 pequeñas), con **caso de uso FSD** y **criterios de aceptación**. Lo técnico de esta demo (imagen/compose y/o análisis E3) cuenta como una funcionalidad de quien lo hace.

| Integrante | Funcionalidades Presentadas | FSD / Contrato | Criterios de Aceptación (Resumen) | Issue / Evidencia |
|---|---|---|---|---|
| **Alexander Alvarez** | 1. Exportación Asíncrona del Dossier Digital en ZIP Jerárquico (Grande)<br>2. Análisis Técnico 2️⃣🅱️ Extracción Microservicio E3 (Técnico) | [`FSD-UC-032`](../product/uc/FSD-UC-032.md)<br>[`ADR-001`](../adr/ADR-001-extraer-modulo.md) | Streaming directo a S3/MinIO sin archivos en disco local ni buffers en heap; semáforo global concurrencia (429 Retry-After) y 1 por usuario (409 Conflict); lectura paralela acotada a 8 hilos; purga automática TTL 24–48h; RBAC carrera CC; Presigned URL S3.<br>Análisis k6 E3: p95 +17.34 ms (> 15 ms) y health 1.322% errores; veredicto ADR-001 de mantener en monolito. | [#73](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss/issues/73)<br>[`bench/e3/INFORME.md`](../../bench/e3/INFORME.md) |
| **Boris Anthony Angulo Urquieta** | 1. RAG ARCU-SUR 2023 en el asistente existente (Grande)<br>2. Contrato uniforme de errores REST (Mediana)<br>3. Imagen y stack de producción endurecido (Técnico 1️⃣) | [`FSD-UC-033`](../product/uc/FSD-UC-033.md)<br>[`FSD-UC-034`](../product/uc/FSD-UC-034.md)<br>Ops/NFR | Corpus MD indexado en classpath + FTS; cita manual 2023; el cliente no transfiere PDF.<br>Envelope `{error, message, details?}`; 400/404/405/415/500; 500 genérico sin stacktrace.<br>Multi-stage Dockerfile JRE 21 Alpine, usuario `sigesa` (UID 100), Trivy 0 CVEs OS (High/Critical). | [#76](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss/issues/76)<br>[#67](https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss/issues/67)<br>[`imagen-o-podman.md`](../laboratorios/imagen-o-podman.md) |
| **Aylen Gonzáles** | 1. Semáforo ejecutivo de jefatura<br>2. Bandeja de dictamen técnico | [`FSD-UC-013`](../product/uc/FSD-UC-013.md)<br>[`FSD-UC-012`](../product/uc/FSD-UC-012.md) | Semáforo de estados y avance agregado por carrera en `/executive/semaphore`. Bandeja `/technician/inbox` con filtro, revisión de observaciones en modal accesible y dictamen aprobado/observado. | Rama release/8.2.0 + 8.3.0 |
| **Marlene Velasquez** | 1. Validación de registro de usuarios<br>2. Login institucional y Liveness (`/api/health`) | [`FSD-UC-002`](../product/uc/FSD-UC-002.md)<br>[`FSD-UC-001`](../product/uc/FSD-UC-001.md) | Validación estricta cliente/servidor (email institucional `@umss.edu.bo`, celular 8 dígitos, contraseña fuerte); login JWT institucional; endpoint `/api/health` respondiendo `{"status":"UP"}`. | Rama release/8.2.0 + 8.3.0 |

---

## 2. Lo Técnico de Esta Demo

### 1️⃣ Imagen y Stack de Producción
- **Dockerfile multi-stage:** Construcción en dos etapas con `eclipse-temurin:21-jdk-alpine` para build y `eclipse-temurin:21-jre-alpine` para ejecución.
- **Usuario no-root:** Ejecución bajo usuario y grupo `sigesa` (`uid=100 gid=101`).
- **Seguridad y Auditoría:** 
  - **Trivy:** 0 vulnerabilidades High o Critical en el OS.
  - **hadolint:** Sin hallazgos de severidad Error ni Warning.
- **Compose reproducible:** Archivos [`compose.yaml`](../../compose.yaml) y [`docker-compose.yml`](../../docker-compose.yml) configurados con healthchecks activos para levantar todo el stack con:
  ```bash
  docker compose up -d --wait
  ```

### 2️⃣ Análisis Técnico: 🅱️ Extracción a Microservicio E3 (Alexander Alvarez)
- **Módulo evaluado:** Módulo de Notificaciones asíncronas (`notification_outbox` / `NotificationModulePort`).
- **Documento de Decisión:** [`ADR-001: Mantener el módulo de notificaciones en el monolito`](../adr/ADR-001-extraer-modulo.md).
- **Informe de Benchmark:** [`bench/e3/INFORME.md`](../../bench/e3/INFORME.md).
- **Resultados empíricos con 150k filas en PostgreSQL:**
  - $\Delta p95 = +17.34\text{ ms} > 15\text{ ms}$ (H1 falsificada por overhead HTTP y serialización).
  - En corte de red con Toxiproxy, el colapso del pool de conexiones del cliente HTTP degradó `GET /api/health` a 1.322% de errores (H2 falsificada al superar el 1% de SLA).
  - Veredicto: **Rechazar microservicio y mantener el módulo en el monolito modular.**

---

## 3. Guía de Ejecución y Despliegue Limpio

```bash
git clone https://github.com/AcredIA-UMSS/sigesa-acreditacion-umss.git
cd sigesa-acreditacion-umss
git checkout release/8.3.0
cp .env.example .env
docker compose up -d --wait
```

- **Frontend:** http://localhost:3000  
- **Backend:** http://localhost:8080 (`/api/health`, `/v3/api-docs`)  
- **MinIO Consola:** http://localhost:9001  
- **Usuarios de prueba:** `admin` / `admin123`, `ceub` / `ceub123`, `docente` / `docente123`.
