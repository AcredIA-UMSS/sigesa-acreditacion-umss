# Informe E3 — corrida limpia A/B/A/B

Fecha: 2026-11-02 23:08:52 -04. Entorno: Linux 6.8.0-100-generic x86_64, Docker 29.2.1, 16 CPUs, 14 GiB RAM, Compose v5.0.1, k6 v0.57.0.

Semilla fija en los dos contenedores (`sigesa-e3-db-in-process` y `sigesa-e3-db-http`): 170 filas en `notification_outbox` (50 `PENDING`, 100 `SENT`, 20 `FAILED`).

Script de medición: `medir-limpio.sh`. Carga: 50 VU durante 60 s, rampa de subida 10 s, rampa de bajada 5 s. Orden A, B, A, B, cinco veces.

| Forma | Procesos |
| --- | --- |
| A in-process | API en `:18080` |
| B HTTP | API `:18082` + sidecar `:18081` + Toxiproxy `:18474`, sin toxics |

## Matriz — notificaciones

Percentiles en milisegundos, solo `GET /api/v1/e3/notifications`. Error = status distinto de 200. Colgadas = status 0.

| Orden | Forma | p50 | p95 | p99 | máx | errores | colgadas |
| --- | --- | ---: | ---: | ---: | ---: | ---: | ---: |
| 1 | in-process | 1.192 | 2.334 | 4.677 | 88.506 | 19/34063 | 19 |
| 2 | HTTP | 2.132 | 3.950 | 7.929 | 115.070 | 13/26015 | 13 |
| 3 | in-process | 1.062 | 1.639 | 2.486 | 47.233 | 10/39620 | 10 |
| 4 | HTTP | 2.133 | 3.848 | 7.356 | 96.352 | 8/42716 | 8 |
| 5 | in-process | 1.087 | 1.702 | 2.438 | 25.084 | 20/26770 | 20 |
| 6 | HTTP | 2.107 | 3.828 | 6.815 | 71.940 | 10/27095 | 10 |
| 7 | in-process | 1.097 | 1.848 | 3.502 | 77.885 | 10/39605 | 10 |
| 8 | HTTP | 2.082 | 3.611 | 6.494 | 72.804 | 11/30011 | 11 |
| 9 | in-process | 0.782 | 1.589 | 2.548 | 22.591 | 19/13520 | 19 |
| 10 | HTTP | 2.124 | 3.737 | 6.357 | 106.483 | 10/26861 | 10 |

| Percentil | mediana in-process | rango in-process | mediana HTTP | rango HTTP | HTTP − in-process |
| --- | ---: | --- | ---: | --- | ---: |
| p50 | 1.087 | 0.782 – 1.192 | 2.124 | 2.082 – 2.133 | 1.037 |
| p95 | 1.702 | 1.589 – 2.334 | 3.828 | 3.611 – 3.950 | **2.126** |
| p99 | 2.548 | 2.438 – 4.677 | 6.815 | 6.357 – 7.929 | 4.267 |

En régimen estable cada error de notificaciones fue una petición colgada. No hubo HTTP 4xx/5xx. Health, en la misma corrida, se quedó entre 0 y 12 colgadas (máximo 12/26770). El p99 de `http_req_blocked` estuvo entre 0.661 y 0.790 ms: no había una cola de conexiones esperando.

H1 se sostiene: 2.126 ms frente a 15 ms.

## H2 — corte

Proxy `notify` con `enabled: false`. La forma in-process corrió en paralelo y no usa ese proxy.

| Corrida | p50 | p95 | p99 | errores | colgadas |
| --- | ---: | ---: | ---: | ---: | ---: |
| HTTP, notificaciones | 1.970 | 5.106 | 11.128 | 2496/2496 (100 %) | 7 |
| HTTP, health | 1.964 | 4.805 | 9.474 | 33/2496 (1.322 %) | 33 |
| in-process, notificaciones | 1.151 | 1.825 | 3.011 | 10/57495 (0.017 %) | 10 |
| in-process, health | 1.104 | 1.717 | 2.777 | 8/57495 (0.014 %) | 8 |

De las 2496 notificaciones fallidas, 2489 recibieron respuesta HTTP (503 del adaptador) y 7 se colgaron. Las 33 de health no recibieron status: son colgadas, no un 500 del controller. Al reabrir el proxy, notificaciones volvió a 200 con `origin: "http"`.

H2 no se sostiene. El error de health quedó en 1.322 %, por encima del 1 %. El p95 de notificaciones (5.106 ms) no llegó a 500 ms; la cláusula que sí se cumple es el 100 % de error del módulo. La falla no se queda ahí: health del mismo proceso pierde peticiones.

## H3 — arranque

Reloj desde `docker compose up -d` hasta health 200. En HTTP, también hasta el status del sidecar y el alta del proxy. Las diez corridas `ok`.

| Orden | Forma | segundos |
| --- | --- | ---: |
| 1 | in-process | 7.678 |
| 2 | HTTP | 9.262 |
| 3 | in-process | 7.660 |
| 4 | HTTP | 9.168 |
| 5 | in-process | 7.629 |
| 6 | HTTP | 9.213 |
| 7 | in-process | 7.600 |
| 8 | HTTP | 10.378 |
| 9 | in-process | 7.661 |
| 10 | HTTP | 9.133 |

Mediana in-process 7.660 s (rango 7.600–7.678). Mediana HTTP 9.213 s (rango 9.133–10.378). Razón 1.20×. H3 se sostiene.

## Amenazas a la validez

- Load average de 1 minuto nunca bajó de 5. Cursor, WindowServer y la VM de Docker compartían la CPU. A/B/A/B reparte ese ruido; no lo elimina.
- k6 corre en otro contenedor y entra por `host.docker.internal`, no por la red de Compose. El hop medido incluye el bridge de Docker más ese salto al host.
- Las dos formas no comparten base: cada una tiene un H2 vacío. El p95 no dice nada de un outbox con filas en PostgreSQL.
- El corte es apagar el proxy, no un toxic de latencia. El circuit breaker (5 fallos, 2 s abierto) convierte la caída en 503 rápido, por eso el p95 del corte es 5 ms y no 500.
- Unas 8–20 peticiones colgadas por corrida aparecen también en régimen estable, en las dos formas. El piso de error del cliente no es cero.
- `gracefulStop` escrito en el script no lo acepta k6 0.57 (aviso `unknown field`). Aplicó el cierre por defecto de 30 s.

## Veredicto

H1 y H3 caben en el presupuesto. H2 no: con el sidecar cortado, health pierde 33/2496 (1.322 %). El número de reversión es un **o**. Se cruza. **El módulo se queda en el monolito** (`EXTRACTION_MODE=in_process`).

La frase del borrador que pide las dos falsaciones a la vez (H1 y H2) no se cumple, porque H1 aguanta. Manda el número: error de health > 1 % basta para no extraer. Decisión y condición para reabrirla: [ADR-001](../../docs/adr/ADR-001-extraer-modulo.md).

---

# Anexo E3: Banco de Pruebas Automatizado (K6 + Toxiproxy Inyección de Caos)

## 1. Configuración del Escenario K6 Automatizado
- **Warm-up:** 15 s (rampa progresiva hasta 50 req/s).
- **Carga constante:** 45 s a 50 req/s.
  - $0\text{--}20\text{ s}$: Tráfico estable antes de la falla (`phase: before_failure`).
  - $20\text{--}30\text{ s}$: Inyección de toxic de latencia severa (2000 ms) en Toxiproxy (`phase: during_failure`).
  - $30\text{--}60\text{ s}$: Recuperación post-falla tras eliminar el toxic (`phase: after_failure`).
- **Ramp-down:** 10 s (50 req/s hacia 0).
- **Rutas evaluadas:**
  - `GET /api/v1/e3/notifications` (Usa el módulo vía Toxiproxy).
  - `GET /api/health` (Control: liveness sin usar el módulo).

## 2. Seed de Datos Iniciales en PostgreSQL
- **Carreras reales UMSS (`programs`):** 6 carreras representativas (Lic. en Informática, Ing. Electromecánica, Lic. en Bioquímica y Farmacia, Lic. en Odontología, Ciencias de la Educación, Fisioterapia y Kinesiología).
- **Usuarios creados (`app_user`):** Coordinadores reales asignados a sus programas.
- **Procesos activos (`accreditation_processes`):** 4 procesos en estado `ACTIVE`.
- **Tabla de Notificaciones (`notification_outbox`):** 170 registros insertados (50 `PENDING`, 100 `SENT`, 20 `FAILED`).
- **Verificación:** `GET /api/v1/e3/notifications` retorna `HTTP 200 OK` con `{ "pendingCount": 50, "totalCount": 170 }`.

## 3. Registro de Mediciones de la Suite K6 Automatizada

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

| Métrica / Fase | Antes de Falla ($t < 20\text{ s}$) | Durante Falla ($20\text{ s} \le t < 30\text{ s}$) | Después de Falla ($t \ge 30\text{ s}$) | Comportamiento |
|---|---|---|---|---|
| **Notificaciones p95** | **11.56 ms** (mediana 7.71 ms) | **7.13 ms** (mediana 5.58 ms) | **5.59 ms** (mediana 4.58 ms) | Régimen puramente local / JPA |
| **Notificaciones Error Rate** | **0.00 %** (0/699) | **0.00 %** (0/500) | **0.00 %** (0/1754) | 100 % disponibilidad |
| **Health p95** | **4.65 ms** | **2.65 ms** | **1.94 ms** | Monolito estable |
| **Health Error Rate** | **0.00 %** | **0.00 %** | **0.00 %** | Sin errores |

### Comparativa $\Delta p95$ (H1) en Suite Automatizada
- $\text{p95}_{\text{http}} = 21.41\text{ ms}$
- $\text{p95}_{\text{in\_process}} = 11.56\text{ ms}$
- $\Delta p95 = \mathbf{9.85\text{ ms}} \le 15\text{ ms}$ (H1 Satisfecha en carga estable con base de datos real).
