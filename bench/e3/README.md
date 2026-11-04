# Guía de Ejecución: Laboratorio E3 (K6 + Toxiproxy)

Este directorio contiene la implementación del **Entregable 2** del Laboratorio E3 para SIGESA:
Automatización de escenarios de estrés con usuarios virtuales y coordinación de la inyección de fallas en el tiempo exacto.

---

## Componentes y Entregables

### 1. Script Base en K6 (`stress_toxiproxy.js`)
- **Fases (*Stages*):**
  - **Calentamiento (*Warm-up*):** 15 s de rampa progresiva de 0 a 50 peticiones/s.
  - **Carga constante (*Steady load*):** 45 s a 50 peticiones/s continuas (con inyección de falla entre los segundos 20 y 30).
  - **Bajada (*Ramp-down*):** 10 s reduciendo la carga de 50 peticiones/s a 0.
- **Métricas Custom y Tags por Fase:**
  - **Tags:**
    - `phase`: `'before_failure'` ($t < 20\text{ s}$), `'during_failure'` ($20\text{ s} \le t < 30\text{ s}$), `'after_failure'` ($t \ge 30\text{ s}$).
    - `endpoint`: `'notifications'` o `'health'`.
  - **Métricas personalizadas en K6:**
    - `notifications_duration_before`, `notifications_duration_during`, `notifications_duration_after` (`Trend`).
    - `health_duration_before`, `health_duration_during`, `health_duration_after` (`Trend`).
    - `notifications_errors_before`, `notifications_errors_during`, `notifications_errors_after` (`Rate`).
    - `health_errors_before`, `health_errors_during`, `health_errors_after` (`Rate`).
    - `total_requests_executed` (`Counter`).
  - **Verificación automática de hipótesis en K6:**
    - `notifications_duration_before`: $p95 < 300\text{ ms}$ (H1).
    - `health_errors_during`: $\le 1\%$ (H2 - aislamiento de falla).
    - `notifications_errors_during`: $> 50\%$ (H2 - contención del corte).

### 2. Script de Datos Iniciales (`seed_e3_notifications.sql`)
- **Carreras Reales de la UMSS (`programs`) y sus Facultades:**
  - `LIC-INF`: Licenciatura en Informática (Facultad de Ciencias y Tecnología - FCT).
  - `ING-ELMEC`: Ingeniería Electromecánica (Facultad de Ciencias y Tecnología - FCT).
  - `BIOQ-FARM`: Licenciatura en Bioquímica y Farmacia (Facultad de Bioquímica y Farmacia - FBF).
  - `ODON`: Licenciatura en Odontología (Facultad de Odontología - FO).
  - `CIEN-EDU`: Ciencias de la Educación (Facultad de Humanidades y Ciencias de la Educación - FHCE).
  - `FIS-KIN`: Licenciatura en Fisioterapia y Kinesiología (Facultad de Medicina - FCMM).
- **Usuarios y Roles (`app_user` & `user_program_assignment`):**
  - Coordinadores de Carrera reales (`CC`): `coord.informatica@umss.edu.bo`, `coord.electromecanica@umss.edu.bo`, `coord.bioquimica@umss.edu.bo`, `coord.odontologia@umss.edu.bo`.
  - 1 Evaluador Externo (`EE`): `evaluador.e3@umss.edu.bo`.
  - 1 Miembro del Comité de Autoevaluación (`CAE_MEMBER`): `cae.e3@umss.edu.bo`.
  - Contraseña estándar de pruebas: `Password123!`.
- **Procesos de Acreditación Abiertos (`accreditation_processes`):**
  - 4 procesos con estado `ACTIVE` y modo operacional `ACTIVE` vinculados a las carreras reales y plantillas oficiales (`CEUB 2026` y `ARCU-SUR 2026`).
- **Alertas y Observaciones (`tb_observation` & `notification_outbox`):**
  - 4 observaciones críticas abiertas (`PENDIENTE_SUBSANACION`) contextualizadas en los laboratorios y clínicas reales de cada carrera.
  - 170 registros en `notification_outbox`:
    - Alertas de plazos inminentes vinculadas a los procesos de Informática, Electromecánica, Bioquímica y Odontología.
    - 50 registros `PENDING` (cola activa de alertas).
    - 100 registros `SENT` (histórico entregado).
    - 20 registros `FAILED` (reintentos agotados).
- Garantiza que antes de la inyección de falla, las consultas a `GET /api/v1/e3/notifications` lean registros existentes y retornen `200 OK` con un cuerpo JSON válido:
  ```json
  {
    "pendingCount": 50,
    "totalCount": 170,
    "origin": "http"
  }
  ```

### 3. Inyección de Fallas en Toxiproxy en Segundos Exactos
- Coordinado a través del escenario `fault_injection_scenario` en K6:
  - $T = 0\text{ s}$: Asegura que el proxy `notification_proxy` apunte a `notification-service:8082` y limpia toxics residuales.
  - $T = 20\text{ s}$: Invoca `POST /proxies/notification_proxy/toxics` para inyectar una latencia de 2000 ms (superior al timeout de 200 ms y deadline de 800 ms configurados en el backend).
  - $T = 30\text{ s}$: Invoca `DELETE /proxies/notification_proxy/toxics/sidecar_latency_fault` para retirar la latencia y observar la recuperación del Circuit Breaker.

---

## Arquitectura de Despliegue (`docker-compose.e3.yml`)

| Servicio | Imagen / Build | Puerto | Rol |
|---|---|---|---|
| `db` | `postgres:16-alpine` | `5433:5432` | Base de datos PostgreSQL con `notification_outbox` y seed |
| `notification-service` | `backend/Dockerfile` (`SIGESA_APP=notification-service`) | `8082:8082` | Módulo extraído de notificaciones (Sidecar) |
| `toxiproxy` | `ghcr.io/shopify/toxiproxy:2.11.0` | `8474:8474` (API), `8081:8081` (Proxy) | Proxy TCP manipulable para inyección de caos |
| `backend` | `backend/Dockerfile` (`SIGESA_APP=api`, `EXTRACTION_MODE=http`) | `8080:8080` | Monolito Spring Boot consumiendo notificaciones a través de Toxiproxy |
| `k6` | `grafana/k6:latest` | N/A | Generador de carga de usuarios virtuales y orquestador de caos |

---

## Comandos de Ejecución

### Opción A: Flujo Completo Automatizado
```bash
./bench/e3/run_e3.sh --all
```
Este script:
1. Construye e inicia los contenedores con Docker Compose.
2. Aplica el seed de base de datos en PostgreSQL.
3. Realiza un smoke test inicial contra `/api/health` y `/api/v1/e3/notifications`.
4. Ejecuta el script K6 midiendo las fases y coordinando Toxiproxy.

### Opción B: Pasos Individuales
```bash
# 1. Levantar la infraestructura
./bench/e3/run_e3.sh --up

# 2. Aplicar el seed de notificaciones
./bench/e3/run_e3.sh --seed

# 3. Ejecutar la prueba de estrés K6
./bench/e3/run_e3.sh --run

# 4. Detener el banco de pruebas al finalizar
./bench/e3/run_e3.sh --down
```
