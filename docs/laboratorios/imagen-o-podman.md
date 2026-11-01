# Laboratorio sesión 8 — auditar y adelgazar la imagen

## Datos

| Campo | Valor |
|---|---|
| **Integrante(s)** | Boris Anthony Angulo Urquieta |
| **Opción** | A (auditar y adelgazar) |
| **Servicio** | `backend` (`docker compose build backend`) |
| **Máquina** | CachyOS Linux (Arch-like), kernel `7.3.0-4-cachyos`, x86_64 |
| **Docker** | Client y Server **29.9.2** (`linux/amd64`). Build clásico (sin plugin BuildKit). |
| **Podman** | No aplica (opción A). |
| **Rama** | `lab-s08/angulo-urquieta` |
| **Uso de IA** | **Sí.** Cursor (agente) ejecutó los comandos A1–A7, editó `backend/Dockerfile` / `.dockerignore` / `pom.xml` (exclude de `devtools`) y redactó este informe. Las cifras salen de las salidas reales de Docker, Dive, Trivy y hadolint; no se inventaron. |

Herramientas fijadas como pide el enunciado (nunca `latest`):

- Dive `wagoodman/dive:v0.13.1@sha256:f1886e6c32c094fc41a623c1989f5cb3e48aa766da5f0be233f911fc1d85ce10`
- Trivy `aquasec/trivy:0.76.1@sha256:790fd66d1697395d8693d1acaa1ea702406a20f109b6fc7bac8db623eca6c06c`
- hadolint `hadolint/hadolint:v2.15.1@sha256:32dac94127fd60b7b7e3fbfc65e1383b9b5e25c9bfd7b8536de7a539fe68a12d`

Etiquetas de laboratorio:

- `mi-imagen:antes` = `sigesa-acreditacion-umss-backend` **antes** de A6 (`b24232703854`)
- `mi-imagen:despues` = la misma imagen **después** de A6 (`f359c39786ab`)

---

## Tabla antes → después

| Medida | Antes | Después |
|---|---|---|
| Tamaño DISK USAGE | **479 MB** | **507 MB** |
| Tamaño CONTENT SIZE | **166 MB** | **179 MB** |
| Número de capas (`docker image history -q \| wc -l`) | **16** | **18** |
| Usuario | `Config.User` vacío; `id` → **`uid=0(root)`** | **`sigesa` (`uid=100 gid=101`)** |
| CVE HIGH + CRITICAL (Trivy) | OS **14** (13H+1C) + JAR **19** (12H+7C) = **33** | OS **0** + JAR **25** (17H+8C) = **25** |
| CVE totales (todas las severidades) | OS 59 + JAR 33 = **92** | OS 0 + JAR 46 = **46** |
| hadolint | **DL3059** (info, `RUN` consecutivos) | **DL3066** (info, USER no numérico) |
| Dive efficiency / wasted | 99.88 % / 558 kB | 99.89 % / 560 kB |
| ¿Arranca y responde? | — | **Sí.** `GET /api/health` → `{"status":"UP"}` HTTP 200 |

El runtime **no adelgazó**. El JAR pasó de 99.8 MB a 114 MB porque en esta rama se mergeó `release/2.0.0` (más código) entre A1 y A6. La base nueva (Alpine 3.24.3 / JRE 21.0.13) pesa un poco más que la de A1 (Alpine 3.23.5 / JRE 21.0.11). El recorte útil está en el **contexto de build** (~124 MB → **1.2 MB**) y en **no correr como root**.

---

## A1. Construir tal como estaba

```bash
docker compose build backend
docker image ls
docker tag sigesa-acreditacion-umss-backend:latest mi-imagen:antes
```

Salida relevante: contexto enviado **~124 MB** (no había `.dockerignore`). Imagen `sigesa-acreditacion-umss-backend:latest` id `b24232703854`.

```
IMAGE             ID             DISK USAGE   CONTENT SIZE
mi-imagen:antes   b24232703854        479MB          166MB
```

---

## A2. Tamaño, capas y Dive

```bash
docker image ls mi-imagen:antes
docker image history mi-imagen:antes --format 'table {{.Size}}\t{{.CreatedBy}}'
docker run --rm -e CI=true -v /var/run/docker.sock:/var/run/docker.sock \
  wagoodman/dive:v0.13.1@sha256:f1886e6c32c094fc41a623c1989f5cb3e48aa766da5f0be233f911fc1d85ce10 \
  mi-imagen:antes
```

Capas con peso (antes):

| Tamaño | Instrucción |
|---|---|
| 99.8 MB | `COPY … app.jar` |
| 165 MB | instalación del JRE 21 |
| 38.2 MB | `apk add` de Temurin (fontconfig, ttf-dejavu, gnupg, …) |
| 10 MB | `alpine-minirootfs-3.23.5` |

El history **solo** muestra la etapa JRE: el multi-stage ya dejaba fuera el JDK (~555 MB).

Dive (antes): **efficiency 99.8824 %**, **wasted 558 kB**. Desperdicio = duplicados de la base (`ca-certificates.crt`, apk db). No hay `src/`, `.git`, `.m2`, `.env` ni tests en el runtime.

Después (A7): JAR **114 MB**, Alpine **3.24.3**, JRE `jdk-21.0.13+9`, capas 18. Dive **99.8875 % / 560 kB**.

---

## A3. Usuario

```bash
docker image inspect mi-imagen:antes --format "{{.Config.User}}"
docker run --rm --entrypoint id mi-imagen:antes
```

Antes: `Config.User` vacío; `uid=0(root) gid=0(root)`.

```bash
docker image inspect mi-imagen:despues --format "{{.Config.User}}"
docker run --rm --entrypoint id mi-imagen:despues
docker exec sigesa-backend id
```

Después: `User=sigesa`; `uid=100(sigesa) gid=101(sigesa)`. El contenedor en marcha coincide.

---

## A4. Trivy 0.76.1

```bash
docker run --rm \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v trivy-cache:/root/.cache \
  aquasec/trivy:0.76.1@sha256:790fd66d1697395d8693d1acaa1ea702406a20f109b6fc7bac8db623eca6c06c \
  image --scanners vuln mi-imagen:antes
# y lo mismo con --severity HIGH,CRITICAL
# repetido sobre mi-imagen:despues
```

**Antes** (`alpine 3.23.5` + `app.jar`):

| Destino | Total | HIGH | CRITICAL |
|---|---:|---:|---:|
| OS | 59 | 13 | 1 (`brotli-libs` CVE-2026-111497) |
| JAR | 33 | 12 | 7 (Tomcat, Spring WebMVC RCE CVE-2026-47884, …) |

**Después** (`alpine 3.24.3`):

| Destino | Total | HIGH | CRITICAL |
|---|---:|---:|---:|
| OS | **0** | 0 | 0 |
| JAR | 46 | 17 | 8 |

Las CVE de OS se fueron al fijar una base más nueva. Las del JAR **no** las arregla el Dockerfile: dependen del `pom.xml` (Spring 7.0.8, Tomcat embed 11.0.22, Jackson, Netty, …). El JAR “después” tiene más hallazgos en parte porque el merge de `release/2.0.0` metió más dependencias.

---

## A5. hadolint 2.15.1

```bash
docker run --rm -i \
  hadolint/hadolint:v2.15.1@sha256:32dac94127fd60b7b7e3fbfc65e1383b9b5e25c9bfd7b8536de7a539fe68a12d \
  < backend/Dockerfile
```

Antes: **DL3059** info, línea del segundo `RUN` (`chmod` y `dependency:go-offline` separados).

Después: **DL3066** info: `USER sigesa` no es UID numérico. hadolint **no** avisó (ni antes ni ahora) del digest, del root previo, ni del `.dockerignore`.

---

## A6. Qué se cambió y por qué

Archivos: `backend/Dockerfile`, `backend/.dockerignore` (nuevo), `backend/pom.xml` (exclude de `spring-boot-devtools` en el plugin).

1. **`.dockerignore`** con `target`, `.m2`, `.git`, `.env`, `.env.*`, `src/test`, etc. El `Sending build context` bajó de ~124 MB a **1.2 MB**.
2. **`FROM` con digest**  
   - JDK: `eclipse-temurin:21-jdk-alpine@sha256:b78a1d385da875588e21207f07dddf9a1418849971ae857fb24c8f3a7d441a72`  
   - JRE: `eclipse-temurin:21-jre-alpine@sha256:82cf3d9ccbe1ed46252008d51b2f2329e6b9e8a2f4394c4bebefc3ea7f4b1372`  
   Digest tomado de `docker image inspect --format "{{index .RepoDigests 0}}"`.
3. **Usuario no root:** `addgroup`/`adduser` `sigesa` y `USER sigesa`. Sin `--chown` del JAR (Java solo necesita leerlo).
4. Un `RUN` para `chmod +x ./mvnw && ./mvnw -B dependency:go-offline` (DL3059).
5. No copiar `.env` en la imagen; Compose ya usa `env_file`.

**Probado y revertido:** extraer capas Spring (`java -Djarmode=tools … extract --layers`). La imagen subió a **511 MB** de disco (el fat JAR comprimido pesa menos que las capas explotadas). Se volvió a `COPY …jar` + `java -jar`.

No se migró a distroless/DHI ni se bump-earon dependencias Maven (fuera de un exclude de `devtools`): lo primero pierde `id`/shell; lo segundo es un cambio de producto, no de imagen.

```bash
docker compose build backend
docker tag sigesa-acreditacion-umss-backend:latest mi-imagen:despues
```

---

## A7. Medir otra vez y probar el API

```bash
docker compose up -d --wait
docker compose ps
curl -sS -w '\nHTTP %{http_code}\n' http://127.0.0.1:8080/api/health
```

`compose up --wait` **falló**: el puerto **11434** ya estaba ocupado (Ollama en el host). `sigesa-backend` sí se recreó con la imagen nueva. Arranque: `docker start sigesa-backend`.

El primer `curl` a los ~8 s: *Connection reset* (Tomcat aún no escuchaba; boot ~14 s). Después:

```json
{"status":"UP"}
HTTP 200
```

Log: `Started SigesaApplication in 14.057 seconds`. No faltó ningún archivo de la etapa final ni permisos sobre `app.jar`.

`docker compose ps` (tras el start): `sigesa-backend` Up, imagen `sigesa-acreditacion-umss-backend`, puerto 8080. Ollama del compose no quedó levantado por el conflicto de puerto; Postgres, MinIO y frontend sí.

---

## Qué no se pudo resolver

- **Bajar DISK USAGE** por debajo de 479 MB: el peso es JRE (~165 MB) + JAR (~114 MB). Adelgazar de verdad pediría distroless/JRE más chica **y** un JAR más liviano.
- **CVE HIGH/CRITICAL del fat JAR:** hay que actualizar Spring, Tomcat embed, Jackson, Netty, etc. en el `pom`.
- **DL3066:** se dejó `USER sigesa` (legible) en lugar de `USER 100:101`.
- **`compose up --wait` completo** en esta máquina: 11434 tomado por Ollama nativo. El backend nuevo igual respondió contra Postgres/MinIO ya en marcha.
