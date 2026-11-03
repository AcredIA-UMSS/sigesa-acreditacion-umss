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

La corrida que vale es la A/B/A/B del 02/11/2026 con `spring-boot:run` y Open WebUI/Ollama detenidos. Cinco repeticiones por forma. Detalle y amenazas: `bench/e3/INFORME.md`. Resumen:

| Criterio | Resultado | ¿Cabe? |
| --- | --- | --- |
| H1 | mediana p95 HTTP 3.828 ms − in-process 1.702 ms = **2.126 ms** (p50 +1.037 ms, p99 +4.267 ms) | sí |
| H2 | notificaciones 2496/2496 en error; health **33/2496 (1.322 %)**, las 33 colgadas (status 0) | no |
| H3 | mediana de arranque 9.213 s / 7.660 s = **1.20×** | sí |

H1 y H3 no impiden extraer. H2 sí: el error de health pasó el 1 %. En la misma ventana, la forma in-process perdió 8/57495 health (0.014 %). La pérdida está en el proceso que habla con el sidecar. Una corrida previa, con el backend local y Ollama en marcha, había medido health en 0.756 % y no se usa para este veredicto.

El default sigue `in_process` (`matchIfMissing`). El adaptador HTTP queda en el código como experimento, no como forma de despliegue.

## 4. Consecuencias (Impacto)

* **Positivas:** El proceso único no depende de Toxiproxy ni de un segundo JVM. El p95 de la ruta del módulo se queda en una mediana de 1.702 ms en este banco.
* **Negativas / Riesgos:** Un fallo del outbox comparte el proceso con el resto de la API. El aislamiento que H2 buscaba no se compró: al cortar el sidecar, health del proceso HTTP pierde peticiones por encima del umbral.

### Amenazas a la validez

* Hardware compartido. Load average de 1 minuto entre 5.4 y 7.1. Cursor, WindowServer y la VM de Docker siguieron corriendo. A/B/A/B reparte ese ruido; no lo quita.
* La red medida no es solo el bridge de Compose. k6 sale de otro contenedor y entra por `host.docker.internal`.
* Seed vacío en H2, un proceso por forma. No es PostgreSQL con filas.
* El corte apaga el proxy. No se aplicó un toxic de latencia. El circuit breaker (5 fallos, 2 s) hace fallar rápido, y el p95 del corte queda en 5.106 ms.
* En régimen estable también hay peticiones colgadas (status 0), del orden de 8 a 20 por corrida de 20 s, en las dos formas.

### Condición para deshacer esta decisión

Se reabre la extracción solo con una corrida nueva, mismo protocolo (A/B/A/B, ≥ 5 por forma, timeout de cliente 5 s), seed no vacío en PostgreSQL, y las tres a la vez:

1. mediana de (p95 HTTP − p95 in-process) ≤ 15 ms;
2. error de `GET /api/health` durante el corte del sidecar ≤ 1 %;
3. peticiones colgadas (status 0) de ese health ≤ 1 %.

Si falta una, el módulo sigue en el monolito. Cruzar otra vez el 1 % de health, o pasar de 15 ms en la mediana del p95, deja la decisión como está.

## 5. Trazabilidad

* No modifica el DTI ni `docs/baseline/`. Confirma el monolito de ADR_002 para este módulo.
* El DTP registra el veredicto en el changelog del 02/11/2026. No hay fila de delta en §A.2: lo construido sigue siendo el monolito.
