# Informe E3 — Experimento de Carga y Caos (Notificaciones Extraídas vs En Proceso)

## Estado de la Implementación
- **Integrante 1 (Boris Angulo):** Adaptador dual (`HttpNotificationModuleAdapter`, `InProcessNotificationModuleAdapter`), Circuit Breaker, endpoints y definición de hipótesis en [`HIPOTESIS.md`](HIPOTESIS.md).
- **Integrante 2 / Automatización:**
  - Script base K6 con fases, métricas custom y tags: [`stress_toxiproxy.js`](stress_toxiproxy.js).
  - Seed SQL con registros representativos en `notification_outbox`: [`seed_e3_notifications.sql`](seed_e3_notifications.sql).
  - Coordinación de inyección y desactivación de fallas en Toxiproxy en segundos exactos ($T=20\text{ s}$ y $T=30\text{ s}$).
  - Entorno de orquestación reproducible en [`docker-compose.e3.yml`](docker-compose.e3.yml) y ejecutor [`run_e3.sh`](run_e3.sh).

---

## 1. Configuración del Escenario K6
- **Warm-up:** 15 s (rampa progresiva hasta 50 req/s).
- **Carga constante:** 45 s a 50 req/s.
  - $0\text{--}20\text{ s}$: Tráfico estable antes de la falla (`phase: before_failure`).
  - $20\text{--}30\text{ s}$: Inyección de toxic de latencia severa (2000 ms) en Toxiproxy (`phase: during_failure`).
  - $30\text{--}60\text{ s}$: Recuperación post-falla tras eliminar el toxic (`phase: after_failure`).
- **Ramp-down:** 10 s (50 req/s hacia 0).
- **Rutas evaluadas:**
  - `GET /api/v1/e3/notifications` (Usa el módulo vía Toxiproxy).
  - `GET /api/health` (Control: liveness sin usar el módulo).

---

## 2. Seed de Datos Iniciales
- **Carreras reales UMSS (`programs`):** 6 carreras representativas (Lic. en Informática - FCT, Ing. Electromecánica - FCT, Lic. en Bioquímica y Farmacia - FBF, Lic. en Odontología - FO, Ciencias de la Educación - FHCE, Fisioterapia y Kinesiología - FCMM).
- **Usuarios creados (`app_user`):** Coordinadores reales (`coord.informatica@umss.edu.bo`, `coord.electromecanica@umss.edu.bo`, `coord.bioquimica@umss.edu.bo`, `coord.odontologia@umss.edu.bo`), 1 EE, 1 CAE_MEMBER asignados a sus respectivos programas.
- **Procesos activos (`accreditation_processes`):** 4 procesos en estado `ACTIVE` (plantillas CEUB 2026 y ARCU-SUR 2026).
- **Observaciones abiertas (`tb_observation`):** 4 observaciones contextualizadas en estado `PENDIENTE_SUBSANACION` con vencimientos inminentes.
- **Tabla de Notificaciones (`notification_outbox`):**
  - Total registros insertados: 170
  - `PENDING`: 50 (incluye alertas críticas inmediatas de plazo y auditoría para las carreras reales)
  - `SENT`: 100
  - `FAILED`: 20
- **Verificación:** Antes del corte, las consultas retornan `HTTP 200 OK` con `{ "pendingCount": 50, "totalCount": 170, "origin": "http" }`.

---

## 3. Registro de Mediciones

### Corrida 1: Modo Extraído (`EXTRACTION_MODE=http` con Caos Toxiproxy)
- **Archivo de respaldo inmutable:** [`k6_run1_baseline_result.json`](k6_run1_baseline_result.json)
- **Peticiones totales:** 5,898 (2,949 a Notificaciones + 2,949 a Health)
- **Aserciones K6 aprobadas:** 6,597 / 6,597 (100.00 %)

| Métrica / Fase | Antes de Falla ($t < 20\text{ s}$) | Durante Falla ($20\text{ s} \le t < 30\text{ s}$) | Después de Falla ($t \ge 30\text{ s}$) | Criterio de Hipótesis |
|---|---|---|---|---|
| **Notificaciones p95** | **21.41 ms** (mediana 8.11 ms, avg 10.44 ms) | **487.12 ms** (mediana 3.64 ms, max 515.65 ms) | **25.24 ms** (mediana 12.51 ms, avg 13.31 ms) | H1: $\Delta p95 \le 15\text{ ms}$ en estable |
| **Notificaciones Error Rate** | **0.00 %** (0/699) | **100.00 %** (500/500 degradadas a 503) | **2.51 %** (44/1750 en ventana CB 2s, luego 0.00%) | H2: Falla contenida y degradación controlada |
| **Health p95** | **4.89 ms** (mediana 1.22 ms) | **6.29 ms** (mediana 2.08 ms) | **5.65 ms** (mediana 1.51 ms) | H2: Aislamiento |
| **Health Error Rate** | **0.00 %** (0/699) | **0.00 %** (0/500) | **0.00 %** (0/1750) | H2: Sin degradación en monolito ($\le 1.00\%$) |

### Corrida 2: Modo Monolítico (`EXTRACTION_MODE=in_process`)
- **Archivo de respaldo:** [`k6_result_in_process_20261103_232228.json`](k6_result_in_process_20261103_232228.json)
- **Peticiones totales:** 5,898 (2,949 a Notificaciones + 2,949 a Health)
- **Comportamiento:** Al ejecutarse dentro del proceso JVM consultando PostgreSQL directamente, es inmune a las fallas de red en Toxiproxy.

| Métrica / Fase | Antes de Falla ($t < 20\text{ s}$) | Durante Falla ($20\text{ s} \le t < 30\text{ s}$) | Después de Falla ($t \ge 30\text{ s}$) | Comportamiento |
|---|---|---|---|---|
| **Notificaciones p95** | **11.56 ms** (mediana 7.71 ms) | **7.13 ms** (mediana 5.58 ms) | **5.59 ms** (mediana 4.58 ms) | Régimen puramente local / JPA |
| **Notificaciones Error Rate** | **0.00 %** (0/699) | **0.00 %** (0/500) | **0.00 %** (0/1754) | 100 % disponibilidad |
| **Health p95** | **4.65 ms** | **2.65 ms** | **1.94 ms** | Monolito estable |
| **Health Error Rate** | **0.00 %** | **0.00 %** | **0.00 %** | Sin errores |

---

## 4. Evaluación Formal de Hipótesis

### H1 — Overhead de red en régimen estable
- **Criterio de Aceptación:** $\Delta p95 = \text{p95}_{\text{http}} - \text{p95}_{\text{in\_process}} \le 15\text{ ms}$ en fase estable ($t < 20\text{ s}$).
- **Valores empíricos:**
  - $\text{p95}_{\text{http}} = 21.41\text{ ms}$
  - $\text{p95}_{\text{in\_process}} = 11.56\text{ ms}$
  - $\Delta p95 = 21.41 - 11.56 = \mathbf{9.85\text{ ms}}$
- **Veredicto:** **APROBADA** ($\Delta p95 = 9.85\text{ ms} \le 15\text{ ms}$). El costo de red del sidecar está dentro del presupuesto de latencia establecido.

### H2 — Aislamiento de falla
- **Criterio de Aceptación:** Durante el corte de Toxiproxy ($20\text{ s} \le t < 30\text{ s}$), `GET /api/health` error $\le 1.00\%$, mientras `GET /api/v1/e3/notifications` supera $50\%$ de error o p95 $> 500\text{ ms}$.
- **Valores empíricos en modo HTTP:**
  - Error rate de Health: **0.00 %** (0 errores en 500 peticiones, p95 = 6.29 ms).
  - Error rate de Notificaciones: **100.00 %** (500 de 500 degradaron controladamente a 503 por Circuit Breaker).
- **Veredicto:** **APROBADA**. La falla del servicio de notificaciones no arrastró al monolito.

### H3 — Coste operativo de arranque (`docker compose up`)
- En modo HTTP se orquestan 4 servicios (`db`, `notification-service`, `toxiproxy`, `backend`).
- Tiempo de arranque y estabilización de healthchecks en modo HTTP: $\approx 20\text{ s}$ vs $\approx 15\text{ s}$ en in-process ($\le 2\times$).
- **Veredicto:** **APROBADA**.

