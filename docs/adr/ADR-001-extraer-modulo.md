---
id: ADR-001
titulo: "Mantener el módulo de notificaciones en el monolito"
fecha: "02/11/2026"
estado: aceptado
---

# ADR-001: Mantener el módulo de notificaciones en el monolito

## 1. Contexto y Problema

El outbox de notificaciones (`notification_outbox`) vive en el monolito. [ADR_002](../baseline/05_dti/adrs/ADR_002_monolito_modular.md) eligió esa forma y rechazó microservicios. El puerto `NotificationModulePort` ya puede ir en proceso o por HTTP a un segundo proceso de la misma imagen (`SIGESA_APP=notification-service`) detrás de Toxiproxy. Extraer solo se justifica si el coste cabe en hipótesis escritas antes de medir.

Hipótesis, fijadas en `bench/e3/HIPOTESIS.md` antes de cualquier cifra:

| Id | Predicción cuantitativa | Se falsifica si |
| --- | --- | --- |
| H1 | p95 de `GET /api/v1/e3/notifications` en HTTP sube ≤ 15 ms frente a in-process, Toxiproxy sin toxic | mediana de (p95 HTTP − p95 in-process) > 15 ms |
| H2 | con el sidecar cortado, error de `GET /api/health` ≤ 1 %, y notificaciones > 50 % de error o p95 > 500 ms | health se degrada junto con notificaciones |
| H3 | mediana de `docker compose up` HTTP ≤ 2× in-process | HTTP > 2× |

Número de reversión del mismo documento: diferencia de p95 > 15 ms **o** error de health > 1 % durante el corte. Ese **o** es el que se aplica.

## 2. Opciones Consideradas

* **Opción A:** Mantener el módulo en el monolito (`EXTRACTION_MODE=in_process`). Un proceso, sin hop. Una saturación del cliente HTTP del módulo no existe, porque no hay cliente.
* **Opción B:** Extraer solo este módulo (`EXTRACTION_MODE=http`, sidecar y Toxiproxy). Aísla un corte del sidecar en la ruta del módulo. Cuesta un JVM y un proxy más.

## 3. Decisión Tomada

Mantener el módulo en el monolito (opción A). No se extrae.

La evidencia experimental se basa en dos tandas de medición reportadas en detalle en [`bench/e3/INFORME.md`](../../bench/e3/INFORME.md):
1. **Corrida limpia A/B/A/B (5 repeticiones por forma):** Con el sistema aislado para evaluar percentiles y degradación en corte.
2. **Suite automatizada K6 + Toxiproxy sobre PostgreSQL con 150,000 registros:** Inyectando latencia severa (toxic 2000 ms) y corte de sidecar.

Resumen de resultados:

| Criterio | Resultado | ¿Cabe? |
| --- | --- | --- |
| H1 (Red en reposo / 5 rep.) | mediana p95 HTTP 3.828 ms − in-process 1.702 ms = **2.126 ms** (p50 +1.037 ms, p99 +4.267 ms) | sí |
| H1 (Estrés BD 150k filas) | p95 HTTP 28.90 ms − in-process 11.56 ms = **17.34 ms** (> 15 ms por overhead de serialización + red) | no |
| H2 (Aislamiento en corte) | notificaciones 2496/2496 en error; health **33/2496 (1.322 %)**, las 33 colgadas (status 0) | no |
| H3 (Coste operativo up) | mediana de arranque 9.213 s / 7.660 s = **1.20×** (build ratio: 1.00× al compartir imagen) | sí |

Tanto por la vulneración del límite de aislamiento de H2 (el error de health superó el 1.00%, llegando a 1.322% de peticiones colgadas), como por el overhead en volumen masivo de datos (donde el $\Delta p95$ trepa a 17.34 ms > 15 ms), la extracción queda categóricamente desestimada.

El default sigue `in_process` (`matchIfMissing`). El adaptador HTTP queda en el código como experimento controlable por variable de entorno (`EXTRACTION_MODE=http`), no como forma de despliegue en producción.

## 4. Consecuencias (Impacto)

* **Positivas:** El proceso único no depende de Toxiproxy ni de un segundo JVM. El p95 de la ruta del módulo se queda en una mediana de 1.702 ms (o 11.56 ms con 150k filas). Evita el pool exhaustion y latencias acumuladas de serialización HTTP entre contenedores.
* **Negativas / Riesgos:** Un fallo en el hilo de procesamiento del outbox comparte el mismo runtime con el resto de la API. Sin embargo, el aislamiento que H2 buscaba comprar no se logró: al cortar el sidecar, el cliente HTTP del backend colapsó conexiones haciendo que `GET /api/health` perdiera peticiones por encima del umbral.

### Amenazas a la validez

* Hardware compartido. Load average de 1 minuto fluctuó entre 5.4 y 7.1. Cursor, Docker daemon y procesos del host compartían los cores. El protocolo A/B/A/B mitiga ese sesgo pero no lo elimina.
* La red evaluada es la interfaz de puente Docker (`docker0` / bridge) y el puerto expuesto al host donde k6 genera carga. En un cluster Kubernetes con service mesh (ej. Istio/Envoy), la latencia de proxy mTLS añadiría entre 1 y 3 ms adicionales.
* Base de datos: se evaluó tanto con réplica H2 en memoria como con PostgreSQL real poblado con 150,000 registros en `notification_outbox` (43k PENDING, 100k SENT, 7k FAILED) y 15 carreras UMSS.
* El circuit breaker (5 fallos, ventana 2 s) previene timeouts masivos convirtiendo la indisponibilidad en 503 rápido tras la apertura.

### Condición para deshacer esta decisión (Condición de Reversión)

Se reabrirá la decisión de extracción exclusivamente si una nueva tanda experimental (≥ 5 corridas por forma, con seed real en PostgreSQL ≥ 100k registros) demuestra **simultáneamente**:

1. Mediana de $(\text{p95}_{\text{HTTP}} - \text{p95}_{\text{in-process}}) \le \mathbf{15\text{ ms}}$;
2. Tasa de error de `GET /api/health` durante el corte o saturación del sidecar $\le \mathbf{1.00\%}$;
3. Peticiones colgadas (status 0 / socket timeouts) en la ruta de salud $\le \mathbf{1.00\%}$.

Si no se cumplen las tres condiciones cuantitativas a la vez, el módulo de notificaciones permanecerá dentro del monolito modular.

## 5. Trazabilidad

* No modifica el DTI ni `docs/baseline/`. Confirma el monolito de ADR_002 para este módulo.
* El DTP registra el veredicto en el changelog del 02/11/2026. No hay fila de delta en §A.2: lo construido sigue siendo el monolito.
