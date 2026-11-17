---
id: PR-IMPL-047
feature_asociado: E3-bench-infra
fsd_uc: []
fecha: "2026-11-02"
version: "1.0"
estado: Completado
autor: "DevOps E3"
release: "bench/e3"
---

# Prompt Contract — Implementación `PR-IMPL-047`

> **Alcance:** infraestructura del experimento E3 (monolito modular vs servicio extraído + Toxiproxy).  
> **Hipótesis:** [`bench/e3/HIPOTESIS.md`](../../../bench/e3/HIPOTESIS.md)  
> **Registro PM:** `PM-033` (investigación), `PM-034` (infra), `PM-035` (ejecución/corrección) en sprint 03.

---

## 1. Propósito

Dejar lista la infra Docker del experimento E3: misma imagen para API y sidecar, Toxiproxy como único hop HTTP, scripts de toxics/verificación/tiempos. Sin cambiar lógica de negocio del outbox.

## 2. In-Scope

- `bench/e3/docker-compose.yml` y `docker-compose.monolito.yml` (no pisar el compose de producto).
- Scripts `toxic.sh`, `verificar-infra.sh`, `run-limpio.sh`.
- `.env.example` variables `E3_*`.
- Ajustes mínimos de arranque para que `EXTRACTION_MODE=http` levante en contenedor (storage en `/tmp`, `ObjectMapper` local, CRLF de `mvnw`).

## 3. Out-of-Scope

- `docker-compose.yml` de producto en la raíz.
- `docs/baseline/`.
- Scripts k6 de carga (solo el servicio con profile `bench`).

## 4. Reglas

| ID | Regla |
|---|---|
| R1 | API en forma HTTP apunta a `http://toxiproxy:8666`, nunca a `modulo`. |
| R2 | Sidecar `modulo` solo `expose`, sin `ports` al host. |
| R3 | Imagen Toxiproxy fijada `ghcr.io/shopify/toxiproxy:2.9.0`. |
| R4 | Mismos `JAVA_OPTS` / CPU / memoria en `api` y `modulo`. |
| R5 | Registro en `docs/sprints/sprint_03/PROMPT_MAPPING.md` (PM-033…035). |

## 5. Prompts usados (texto exacto)

### 5.1 Investigación del repo (PASO 0) — PM-033

Fecha: 2026-11-02. Objetivo: inventariar stack, arranque, Docker, DB, módulo, puertos y rutas GET antes de escribir infra.

```text
Actúa como ingeniero DevOps. Necesito dejar listo el entorno de infraestructura para un experimento de "monolito modular vs servicio extraído" con inyección de fallas de red usando Toxiproxy. Solo infraestructura: NO modifiques la lógica de negocio ni los adaptadores.

## PASO 0: Investiga el repo antes de escribir nada
Revisa el proyecto y repórtame (en una lista corta) lo que encuentres sobre:
- Stack, versión de Python y gestor de dependencias (requirements.txt, pyproject.toml, etc.). Ignora el package.json: es solo de Playwright.
- Cómo se arranca la app hoy (entry point, puerto, comando de uvicorn).
- Si ya existe Dockerfile, docker-compose, .env o carpeta bench/.
- Base de datos que se usa y dónde vive (archivo SQLite, Postgres, etc.).
- Estructura de módulos y cuál es el candidato a extraer (frontera clara, tablas propias).
- Interfaz/puerto y adaptadores (in-process / HTTP), la variable de entorno que los selecciona y la que guarda la URL del módulo. Si no existen aún, dímelo y propón nombres, sin implementarlos.
- Cliente HTTP usado (httpx, requests, etc.) y si define timeouts.
- Rutas GET disponibles: elige una que use el módulo y una de control que no lo use.
Si algo es ambiguo o falta, pregúntame antes de asumir.
```

### 5.2 Implementación de infraestructura — PM-034

Fecha: 2026-11-02. Objetivo: compose E3, toxics, verificación, tiempos y README, sin pisar el compose de producto.

```text
## Entregables (después del paso 0)
1. Dockerfile (si no existe), sin --reload y con el mismo número de workers para A y B.
2. docker-compose.yml con:
   - api (A): modo HTTP, URL del módulo apuntando al PROXY (toxiproxy:8666), nunca directo a modulo.
   - modulo (B): misma imagen, otro command; `expose`, NO `ports`.
   - toxiproxy: ghcr.io/shopify/toxiproxy:2.9.0 (versión fija), publica 8474.
   - toxiproxy-setup: curlimages/curl, crea el proxy `modulo` (listen 0.0.0.0:8666, upstream modulo:<puerto B>). Idempotente, corre en cada up.
   - k6: grafana/k6 con profiles: ["bench"], volumen ./bench/e3.
   - db solo si el paso 0 demuestra que hace falta.
   - Healthchecks sin curl en imágenes slim (usa python urllib); depends_on con condition: service_healthy.
   - Una sola red; comunicación por nombre de servicio.
3. docker-compose.monolito.yml (override): api en modo in-process, sin modulo/toxiproxy/toxiproxy-setup.
4. bench/e3/toxic.sh (bash, set -euo pipefail):
   - lento on|off → latency downstream 2000, jitter 0
   - colgado on|off → timeout downstream, timeout 0
   - caido on|off → POST /proxies/modulo {"enabled": false|true}
   - reset, status
   - TOXIPROXY_URL configurable (default http://localhost:8474). Idempotente, valida argumentos.
5. bench/e3/verificar-infra.sh: pasos OK/FALLA: ruta del módulo 200; lento ≥2 s; colgado termina con curl -m 5; caido falla al instante; reset vuelve a 200; ruta de control 200 en todos los casos.
6. bench/e3/run-limpio.sh: down -v, up -d --build --wait, y guarda tiempos de build y up en bench/e3/resultados/tiempos.csv (forma,fecha,build_s,up_s).
7. bench/e3/README-infra.md corto con puertos, nombres y uso.

## Reglas
- Versiones de imágenes fijas; variables configurables en .env.example.
- Puerto de modulo no publicado al host.
- Al terminar, lista los archivos creados y los comandos para validar.
```

### 5.3 Ejecución y corrección — PM-035

Fecha: 2026-11-02. Objetivo: ejecutar la secuencia en terminal, corregir fallos hasta que verificar-infra pase, documentar y registrar prompts.

```text
Docker ya está corriendo. Ejecuta tú mismo todo en terminal y corrige lo que falle. Mantén la infra en bench/e3/ y no pises el docker-compose.yml de producto.

## 1. Revisa antes de ejecutar
- start_period del healthcheck de api y modulo (Spring Boot tarda en arrancar).
- Que el healthcheck use una herramienta que exista en la imagen Alpine (wget/busybox, no curl).
- Mismo límite de memoria/CPU y mismas JAVA_OPTS en api y modulo.

## 2. Ejecuta y valida (en este orden)
chmod +x bench/e3/*.sh
./bench/e3/run-limpio.sh http
./bench/e3/verificar-infra.sh
./bench/e3/toxic.sh status
./bench/e3/run-limpio.sh in_process

- Si algo falla: lee los logs (docker compose logs), diagnostica la causa raíz, corrige y vuelve a correr. Repite hasta que verificar-infra.sh pase sus 6 pasos.
- No marques nada como hecho sin haberlo ejecutado. Pégame la salida real de cada comando.
- Al terminar, deja el entorno limpio (docker compose down -v).

## 3. Documenta en bench/e3/README-infra.md
- Timeout y reintentos efectivos con E3_NOTIFICATION_* (valor, deadline, reintentos).
- Cuánto tarda en fallar cada tóxico (lento, colgado, caído) con esos valores.
- Tiempos medidos de build y up (de tiempos.csv).

## 4. Registra los prompts en promptimpl
- Busca primero la convención existente del repo (carpeta o archivo "promptimpl", plantillas PR-IMPL / PM-*). Síguela tal cual; no inventes formato.
- Guarda ahí los prompts usados en esta tarea: el del paso 0 (investigación del repo), el de implementación de infraestructura y este de ejecución/corrección.
- Para cada uno incluye: fecha, objetivo, texto del prompt, archivos creados o modificados y el resultado de la validación.
- Si la convención exige un ID (PM-*), asígnalo según el patrón existente. Si no queda claro cuál usar, pregúntame. Guarda el anterior prompt y este igual

## Al final
Lista los archivos creados o modificados y el resultado de cada paso de verificar-infra.sh (OK/FALLA).
```
