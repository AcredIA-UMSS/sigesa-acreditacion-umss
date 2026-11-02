# Informe: imagen de contenedor (Docker)

Se usó Docker. No se usó Podman. Las imágenes originales se etiquetaron `:baseline`. Después de los cambios se reconstruyeron como `:slim` y se midieron otra vez con las mismas herramientas.

## 1. Datos del integrante y del servicio

| Campo | Valor |
|---|---|
| Integrante | Marlene Velasquez (`marlene.velasquez@umss.edu`) |
| Repositorio | `sigesa-acreditacion-umss` |
| Commit de partida | `ef2084a8` |
| Fecha | 2026-11-02 |
| Servicios | Backend `sigesa-backend` y frontend `sigesa-frontend` |

| Servicio | Qué es | Imagen base original | Puerto |
|---|---|---|---|
| Backend | API Java 21, Spring Boot 4.1.0 | `eclipse-temurin:21-jre-alpine` | 8080 |
| Frontend | Sitio estático de React servido por nginx | `nginx:alpine` | 80, publicado en el host como 3000 |

Las dos imágenes ya eran multi-etapa antes de este laboratorio. El JDK, Maven, Node y las dependencias de desarrollo no estaban en la imagen final.

## 2. Sistema operativo y versiones de Docker

| Componente | Versión |
|---|---|
| Equipo | MacBook Pro, `MacBook-Pro-de-Marlene.local`, arm64 |
| Sistema operativo del host | macOS 26.5.2 (build 25F84), Darwin 25.5.0 |
| Docker Client | 29.6.2, API 1.55, darwin/arm64 |
| Docker Engine | 29.6.2, API 1.55, linux/arm64 |
| Docker Desktop | 4.84.0 (234817) |
| containerd | v2.2.5 |
| runc | 1.3.6 |
| Docker Compose | v5.3.1 |
| Hadolint | 2.15.1 (imagen `hadolint/hadolint`) |
| Trivy | 0.67.2 (imagen `aquasec/trivy:0.67.2`) |
| Dive | 0.13.1 (imagen `wagoodman/dive:latest`) |

El motor corre dentro de la máquina Linux de Docker Desktop. El cliente corre en macOS.

## 3. Mediciones antes y después

Misma herramienta en las dos columnas.

| Medición | Herramienta |
|---|---|
| Tamaño en disco | `docker image inspect`, campo `.Size` |
| Tamaño comprimido | `docker save IMAGEN \| gzip -c \| wc -c` |
| Número de capas | cantidad de `RootFS.Layers` |
| Eficiencia de imagen | `dive IMAGEN --ci` |
| Usuario de ejecución | `docker run --rm --entrypoint id IMAGEN` |
| CVE | Trivy 0.67.2, `--scanners vuln` |
| Avisos hadolint | Hadolint 2.15.1, `--format json` |
| ¿Arranca y responde? | `curl` a `GET /` |

`docker images` muestra 476 MB para el backend y 94.1 MB / 97.6 MB para el frontend. Esa columna no se usó: incluye el índice y las atestaciones de BuildKit. El tamaño en disco de las tablas es `.Size`.

### Backend

| Medición | Antes `sigesa-backend:baseline` | Después `sigesa-backend:slim` |
|---|---|---|
| Tamaño en disco | 163 374 465 B | 163 376 026 B |
| Tamaño comprimido | 162 001 547 B | 162 002 994 B |
| Número de capas | 7 | 8 |
| Eficiencia de imagen | 99.8632 % | 99.8627 % |
| Usuario de ejecución | root (uid 0) | sigesa (uid 10001) |
| CVE totales | 33 | 33 |
| CVE HIGH + CRITICAL | 19 | 19 |
| Avisos hadolint | 1 | 0 |
| ¿Arranca y responde? | Sí, HTTP 401 | No |

### Frontend

| Medición | Antes `sigesa-frontend:baseline` | Después `sigesa-frontend:slim` |
|---|---|---|
| Tamaño en disco | 26 456 912 B | 28 199 694 B |
| Tamaño comprimido | 26 297 805 B | 28 058 131 B |
| Número de capas | 10 | 12 |
| Eficiencia de imagen | 99.4522 % | 84.9989 % |
| Usuario de ejecución | root (uid 0) | nginx (uid 101) |
| CVE totales | 49 | 1 |
| CVE HIGH + CRITICAL | 3 | 0 |
| Avisos hadolint | 0 | 0 |
| ¿Arranca y responde? | Sí, HTTP 200 | Sí, HTTP 200 |

## 4. Qué encontraste en la imagen original

Backend:

- La etapa final era solo el JRE Alpine y `app.jar` (96 MB). El JDK y la caché de Maven quedaban en la etapa de construcción.
- No había `USER`. `id` devolvió `uid=0(root)`.
- No existía `backend/.dockerignore`. El contexto incluía `target/`.
- Hadolint reportó DL3059: `chmod` y `./mvnw dependency:go-offline` eran dos `RUN` seguidos.
- Las 33 CVE están dentro del jar, no en Alpine. Las 19 altas o críticas son Tomcat 11.0.22, Spring WebMVC 7.0.8, Jackson 2.21.4 y 3.1.4, Bouncy Castle 1.84 y el driver PostgreSQL 42.7.11.
- Trivy indica parches: Tomcat 11.0.25, Spring 7.0.9, Jackson 2.21.7 / 3.1.7, Bouncy Castle 1.85 y pgjdbc 42.7.12.
- La etiqueta `eclipse-temurin:21-*-alpine` no estaba fijada por digest.
- `GET /` respondió 401. Es la respuesta de Spring Security sin sesión: el proceso sí estaba escuchando.

Frontend:

- La etapa final era nginx Alpine más el `dist` (660 KB). Node y `node_modules` no pasaban a la imagen final.
- No había `USER`. `id` devolvió `uid=0(root)`.
- `.dockerignore` ya excluía `node_modules`, `dist` y `.env`. El contexto enviado fue 724 KB.
- Hadolint no reportó avisos.
- CVE crítica: `CVE-2026-111497` en `brotli-libs` 1.2.0-r1. CVE altas: `CVE-2026-92392` en `curl` y `libcurl` 8.22.0-r0. El resto eran 2 medias y 44 bajas.
- Alpine 3.24 ya publicaba `brotli-libs` 1.2.1-r0 y `curl` 8.23.0-r0.
- `node:22-alpine` y `nginx:alpine` no estaban fijados por digest. El Dockerfile instalaba `pnpm@latest`.
- `GET /` respondió 200.

## 5. Qué cambios realizaste y por qué

Backend, `backend/Dockerfile` y `backend/.dockerignore`:

- Digest fijo de `eclipse-temurin:21-jdk-alpine` y `eclipse-temurin:21-jre-alpine`, para que la reconstrucción no cambie de base en silencio.
- Un solo `RUN` para `chmod` y la descarga de Maven. Así desaparece DL3059.
- `-Dmaven.test.skip=true` para no meter el código de prueba en la etapa de construcción. `.dockerignore` excluye `target/`, `src/test`, `.git`, `.env` y metadatos del IDE. El contexto bajó a 56 KB.
- Usuario `10001` (`sigesa`) y `--chown=10001:10001` en el jar, para no ejecutar la API como root.

Frontend, `frontend/Dockerfile`, `frontend/nginx.conf`, `frontend/.dockerignore` y `docker-compose.yml`:

- Digest fijo de `node:22-alpine`. pnpm fijado en `11.18.0`, que es la versión con la que está generado el lockfile. `pnpm@latest` podía romper la construcción.
- Imagen final `nginxinc/nginx-unprivileged:alpine`, digest fijo, usuario `101`. Nginx sin root no puede escuchar en el puerto 80, así que el contenedor escucha en 8080. Compose publica `3000:8080`. La URL del host sigue siendo `http://localhost:3000`.
- `apk upgrade` de `brotli-libs=1.2.1-r0`, `curl=8.23.0-r0` y `libcurl=8.23.0-r0`, porque esas versiones son las que Alpine ofrece para las CVE alta y crítica.
- `.dockerignore` también excluye cobertura, reportes de pruebas y Markdown. El contexto bajó a 23 KB.

Hadolint sobre los Dockerfiles nuevos: 0 avisos en los dos. El uid numérico evita el aviso DL3066, que aparece si el `USER` es un nombre.

## 6. Qué problemas no pudiste resolver

- El backend nuevo no responde. Sale con código 1 antes de abrir el puerto:

  ```text
  java.nio.file.AccessDeniedException: /app/./data
      at com.umss.sigesa.adapter.out.report.LocalFileReportArtifactStorageAdapter.<init>(LocalFileReportArtifactStorageAdapter.java:21)
  ```

  El directorio de trabajo es `/app`, de root. El valor por defecto `./data/reports` se crea ahí y el uid 10001 no puede escribir. No cambié el adaptador Java: el laboratorio era la imagen, y dejar el proceso sin arrancar es el resultado medido.

- Las 19 CVE altas o críticas del backend siguen iguales. Están en dependencias que administra el POM de Spring Boot 4.1.0, no en la imagen Alpine. Subir Tomcat, Spring, Jackson, Bouncy Castle o el driver de PostgreSQL es un cambio de aplicación, no de Dockerfile.

- El frontend no adelgazó. El disco pasó de 26 456 912 B a 28 199 694 B y las capas de 10 a 12. Dive bajó la eficiencia de 99.4522 % a 84.9989 % y marcó 17 762 467 B duplicados. `apk upgrade` escribe las librerías nuevas en otra capa y deja las viejas en la base (`libcrypto`, `libssl`, `libcurl`).

- Queda una CVE de severidad desconocida en el frontend: `CVE-2026-107891` en `musl-utils` 1.2.6-r2. No hay parche aplicado en esta imagen.

- El tamaño que muestra `docker images` (476 MB el backend) no bajó. La etapa múltiple ya había sacado el JDK antes de este laboratorio.

## 7. Problemas que tuviste y cómo los solucionaste

| Problema | Cómo se resolvió |
|---|---|
| El cliente Docker no veía el socket dentro del entorno aislado (`permission denied` en `unix:///Users/marvel/.docker/run/docker.sock`). | Los comandos de Docker se ejecutaron con acceso al daemon de Docker Desktop. |
| Hadolint, Trivy y Dive no estaban instalados en el host. | Se usaron sus imágenes: `hadolint/hadolint`, `aquasec/trivy:0.67.2` y `wagoodman/dive:latest`. |
| `docker image inspect` fallaba al leer `.Config.User` cuando el campo no existía. | El tamaño, las capas y el usuario se leyeron del JSON completo. El usuario real se confirmó con `id`. |
| BuildKit no dejaba etiquetadas en local las imágenes base, así que no había digest en `RepoDigests`. | Los digest se obtuvieron con `docker buildx imagetools inspect` y se fijaron en el `FROM`. |
| El primer Trivy guardaba el JSON dentro del contenedor (`--output /tmp`) y `--rm` lo borraba. | Se detuvo ese contenedor y el JSON se redirigió a la salida estándar del host. |
| `GET /` del frontend fallaba con `host not found in upstream "backend"`. Nginx resuelve ese nombre al arrancar. | La prueba se repitió con `--add-host=backend:127.0.0.1`. Las dos imágenes respondieron 200. |
| `--add-host backend:127.0.0.1` llegó a Docker como una sola bandera y el comando fue rechazado. | Se usó la forma `--add-host=backend:127.0.0.1`. |
| Hadolint DL3066 avisaba que un nombre de usuario puede no resolverse en el host. | `USER` pasó a `10001` en el backend y a `101` en el frontend. Hadolint quedó en cero. |
| La columna `docker images` y `.Size` no coincidían (476 MB contra 163 374 465 B). | Antes y después se compararon solo con `.Size` y con `docker save \| gzip`. |

## 8. Todos los comandos que utilizaste

Versiones del entorno:

```bash
sw_vers
uname -a
docker version
docker compose version
docker images --format '{{.Repository}}:{{.Tag}} {{.ID}} {{.Size}}'
```

Construcción original:

```bash
docker build -t sigesa-backend:baseline -f Dockerfile .
docker build -t sigesa-frontend:baseline -f Dockerfile .
```

Esos comandos se ejecutaron dentro de `backend/` y `frontend/`.

Tamaño, capas y usuario, para `sigesa-backend:baseline`, `sigesa-backend:slim`, `sigesa-frontend:baseline` y `sigesa-frontend:slim`:

```bash
docker image inspect IMAGEN
docker history IMAGEN --format '{{.Size}}\t{{.CreatedBy}}'
docker run --rm --entrypoint id IMAGEN
docker save IMAGEN | gzip -c | wc -c
```

Contenido de las imágenes originales:

```bash
docker run --rm --entrypoint sh sigesa-frontend:baseline -c 'ls -la /usr/share/nginx/html; du -sh /usr/share/nginx/html'
docker run --rm --entrypoint sh sigesa-backend:baseline -c 'ls -lh /app; id'
du -sh backend backend/target frontend frontend/node_modules
```

Hadolint, sobre el Dockerfile original (`git show HEAD:…`) y sobre el archivo ya modificado:

```bash
git show HEAD:backend/Dockerfile | docker run --rm -i hadolint/hadolint hadolint --no-fail --format json -
git show HEAD:frontend/Dockerfile | docker run --rm -i hadolint/hadolint hadolint --no-fail --format json -
docker run --rm -i hadolint/hadolint hadolint --no-fail --format json - < backend/Dockerfile
docker run --rm -i hadolint/hadolint hadolint --no-fail --format json - < frontend/Dockerfile
docker run --rm hadolint/hadolint hadolint --version
```

CVE:

```bash
docker pull aquasec/trivy:0.67.2
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock -v trivy-cache:/root/.cache \
  aquasec/trivy:0.67.2 image --quiet --scanners vuln --format json IMAGEN
docker run --rm aquasec/trivy:0.67.2 --version
```

Paquetes parcheados de Alpine y digest de las bases:

```bash
docker run --rm --entrypoint sh sigesa-frontend:baseline -c 'apk update >/dev/null && apk policy brotli-libs curl libcurl'
docker buildx imagetools inspect eclipse-temurin:21-jdk-alpine
docker buildx imagetools inspect eclipse-temurin:21-jre-alpine
docker buildx imagetools inspect node:22-alpine
docker buildx imagetools inspect nginx:alpine
docker buildx imagetools inspect nginxinc/nginx-unprivileged:alpine
cd frontend && pnpm -v
```

Reconstrucción:

```bash
docker build -t sigesa-backend:slim -f Dockerfile .
docker build -t sigesa-frontend:slim -f Dockerfile .
```

Eficiencia:

```bash
docker pull wagoodman/dive:latest
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock wagoodman/dive:latest IMAGEN --ci
docker run --rm wagoodman/dive:latest --version
```

Arranque. El frontend necesita el nombre `backend` porque `nginx.conf` hace `proxy_pass` a ese host.

```bash
docker run -d --name probe-fe-base -p 18180:80 --add-host=backend:127.0.0.1 sigesa-frontend:baseline
docker run -d --name probe-fe-slim -p 18181:8080 --add-host=backend:127.0.0.1 sigesa-frontend:slim
curl -s -o /dev/null -w '%{http_code}' --max-time 2 http://127.0.0.1:18180/
curl -s -o /dev/null -w '%{http_code}' --max-time 2 http://127.0.0.1:18181/
docker run -d --name probe-be-base -p 18182:8080 sigesa-backend:baseline
docker run -d --name probe-be-slim -p 18183:8080 sigesa-backend:slim
curl -s -o /dev/null -w '%{http_code}' --max-time 2 http://127.0.0.1:18182/
curl -s -o /dev/null -w '%{http_code}' --max-time 2 http://127.0.0.1:18183/
docker logs probe-be-slim
docker rm -f probe-fe-base probe-fe-slim probe-be-base probe-be-slim
```

Resultado de ese `curl`: frontend 200 y 200, backend original 401, backend nuevo 000.
