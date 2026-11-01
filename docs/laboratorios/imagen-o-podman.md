# Laboratorio S08: imagen o Podman

- **Integrante(s):** Alexander Alvarez
- **Opción:** B (stack con Podman)
- **Servicio o stack:** Stack SIGESA (PostgreSQL 16, MinIO/Silo S3, Open-WebUI, Ollama, Backend Spring Boot, Frontend React)
- **Máquina:** Linux 6.8.0-45-generic x86_64 (Ubuntu), Docker Engine 29.9.1 (Community), Podman 4.9.3 (rootless)
- **Uso de IA:** Se utilizó Inteligencia Artificial para el diagnóstico/resolución de errores, la creación del archivo de Podman Compose y la elaboración de este reporte. La instalación y las pruebas de los servicios en línea se realizaron de forma manual.
---

## Configuracion de Podman

- **Podman:** versión `4.9.3`, modo rootless: `true` (`podman info --format "{{.Host.Security.Rootless}}"`)
- **Proveedor de `podman compose`:** `docker-compose` (`/usr/libexec/docker/cli-plugins/docker-compose`, Docker Compose v5.6.0)

| Criterio | Docker (Daemon) | Podman (Rootless) |
|---|---|---|
| **¿Levantó a la primera?** | No a la primera sin demonio: Requiere el servicio central activo (`sudo systemctl start docker`). Al arrancar con contenedores previos, tardó en estabilizar las redes dependientes (`ollama` healthy check). | No a la primera absoluta: requirió activar el socket de usuario rootless (`systemctl --user start podman.socket`) y calificar las imágenes en el compose. |
| **Cambios necesarios en `compose.yaml` o Dockerfiles** | Ninguno en `docker-compose.yml` nativo, pero comparte contención de puertos en el host si ambos runtimes intentan enlazar 5432 o 9000 simultáneamente. | 1. Creación de [`compose.podman.yaml`](file:///mnt/sda2/informatica/maestria/sigesa-acreditacion-umss/compose.podman.yaml) con nombres canónicos de imagen (`docker.io/library/postgres:16-alpine`, `docker.io/ollama/ollama:latest`).<br>2. Flags `:Z` en volúmenes montados para gestión de contexto SELinux/namespaces.<br>3. Cambio de resolución de host a `host.containers.internal`.<br>4. Creación de [`backend/.dockerignore`](file:///mnt/sda2/informatica/maestria/sigesa-acreditacion-umss/backend/.dockerignore) para evitar cargar artefactos locales `target/` al build context. |
| **Tiempo de `up -d` desde cero (una corrida)** | **~2 s** (rearranque de contenedores existentes en Docker daemon). | **~3 s** (`real 0m3.705s`, `user 0m0.103s`, `sys 0m0.046s` para levantar red, volúmenes e instancias con Podman Compose). |
| **Usuario dentro / fuera de un contenedor** | **Sin aislamiento de namespaces en el host:**<br>- `sigesa-backend`: Dentro `root` → Fuera en host (`UID`): **`root`** (PID 48452).<br>- `sigesa-minio`: Dentro `root` → Fuera en host (`UID`): **`root`** (PID 48097).<br>- `sigesa-postgres`: Dentro `postgres` → Fuera en host: **`70`**.<br>*Riesgo:* Si un atacante escapa del contenedor, es `root` en la máquina anfitriona. | **Aislamiento Rootless Real (User Namespaces):**<br>- `sigesa-minio`: Dentro `root` (UID 0) → Fuera en host (`HUSER`): **`alex` (UID 1000)** (usuario regular no privilegiado).<br>- `sigesa-postgres`: Dentro `postgres` (UID 70) → Fuera en host (`HUSER`): **UID `100069`** (rango mapeado en `/etc/subuid`).<br>*Seguridad:* Un escape solo obtiene privilegios del usuario estándar, nunca root. |
| **¿El endpoint responde?** | **Sí, responde HTTP 200 OK:**<br>- Frontend: `http://localhost:3000` → `HTTP/1.1 200 OK`<br>- MinIO Health: `http://localhost:9000/minio/health/live` → `HTTP/1.1 200 OK`<br>- Backend OpenAPI Docs: `http://localhost:8080/v3/api-docs` → `HTTP/1.1 200 OK` | **Sí, responde HTTP 200 OK:**<br>- MinIO S3 Health: `http://localhost:9000/minio/health/live` → `HTTP/1.1 200 OK`<br>- MinIO Console: `http://localhost:9001/` → `HTTP/1.1 200 OK`<br>- PostgreSQL: `/var/run/postgresql:5432 - accepting connections` |

---

### Problemas y cómo los resolvieron

1. **Problema 1: Socket de la API de Podman inactivo para `docker-compose`**
   - **Error exacto:**
     ```text
     >>>> Executing external compose provider "/usr/libexec/docker/cli-plugins/docker-compose". Please refer to the documentation for details. <<<<
     failed to connect to the docker API at unix:///run/user/1000/podman/podman.sock; check if the path is correct and if the daemon is running: dial unix /run/user/1000/podman/podman.sock: connect: no such file or directory
     Error: executing /usr/libexec/docker/cli-plugins/docker-compose ps: exit status 1
     ```
   - **Causa:** `podman compose` delegó en el binario plugin `docker-compose` v5 instalado en el sistema. Este plugin se comunica contra la API compatible de Podman a través del socket de usuario (`/run/user/1000/podman/podman.sock`), el cual estaba inactivo en systemd.
   - **Solución:** Se activó el socket de Podman en espacio de usuario (sin necesidad de `sudo`):
     ```bash
     systemctl --user start podman.socket
     ```

2. **Problema 2: Conflicto de puertos al coexistir Podman y Docker**
   - **Error exacto:**
     ```text
     Error response from daemon: failed to set up container networking: driver failed programming external connectivity on endpoint sigesa-postgres: failed to bind host port 0.0.0.0:5432/tcp: address already in use
     ```
   - **Causa:** Cuando Docker Engine arrancó e intentó encender sus propios contenedores, Podman ya estaba corriendo en rootless escuchando en el puerto 5432 (PostgreSQL) y 9000 (MinIO).
   - **Solución:** Para pruebas comparativas limpias, se debe bajar el stack de un motor antes de levantar el otro (`podman compose down` antes de `docker compose up`, y viceversa).

3. **Problema 3: Ambigüedad en nombres cortos de registros de imágenes en Podman**
   - **Advertencia/Error:** Podman no asume implícitamente `docker.io` en modo no-interactivo si hay múltiples registros definidos.
   - **Solución:** Se creó [`compose.podman.yaml`](file:///mnt/sda2/informatica/maestria/sigesa-acreditacion-umss/compose.podman.yaml) con URIs canónicas completas:
     - `postgres:16-alpine` → `docker.io/library/postgres:16-alpine`
     - `ollama/ollama:latest` → `docker.io/ollama/ollama:latest`

4. **Problema 4: Dependencias de Red y Healthchecks en Docker Compose**
   - **Comportamiento:** En `docker-compose.yml`, `open-webui` y `backend` dependen de que `ollama` esté en estado `healthy`. Al levantar con Docker, hubo una ventana de ~20s de espera mientras Ollama pasaba el healthcheck (`test: ["CMD", "ollama", "list"]`) antes de que Tomcat en Spring Boot inicializara la conexión a PostgreSQL.

---

### Comparación: Qué se gana y qué se pierde con Podman

- **Qué se gana:**
  1. **Seguridad (Rootless por defecto):** No requiere ningún demonio corriendo como `root`. Como se evidenció en la prueba real:
     - En Docker, el proceso del backend corre como `UID: root` (PID 48452) en la máquina host.
     - En Podman, el mismo proceso corre mapeado al usuario `alex` (**UID 1000**) o a subuids (**UID 100069**), evitando que una vulnerabilidad en el backend o en el contenedor otorgue acceso root al host.
  2. **Arquitectura Fork-Exec sin demonio (Daemonless):** Si el demonio de Docker cae o no está iniciado (`Cannot connect to the Docker daemon at unix:///var/run/docker.sock`), nada funciona. Con Podman, cada contenedor corre como un proceso autónomo gestionado por `conmon`.
  3. **No requiere privilegios de administración:** En entornos universitarios o de producción donde los desarrolladores no tienen acceso a `sudo`, Podman permite ejecutar y desarrollar sin pedir permisos especiales.
  4. **Preparado para Kubernetes:** Permite exportar los pods y servicios a manifiestos estándar de Kubernetes (`podman generate kube`).

- **Qué se pierde o fricciones observadas:**
  1. **Compose dependiente de wrapper:** `podman compose` necesita interoperar con `docker-compose` o `podman-compose`, requiriendo mantener activo `systemctl --user start podman.socket`.
  2. **Puertos restringidos (< 1024):** En rootless no se pueden enlazar puertos privilegiados como 80 o 443 sin reconfigurar el kernel (`sysctl`). En SIGESA se resolvió mapeando a puertos altos (`3000:80`).
  3. **Gestión de volúmenes compartidos y UID mapping:** Cuando un proceso dentro del contenedor escribe con un UID específico (ej. `postgres` UID 70), fuera se graba como UID `100069`. Si otro usuario de la máquina intenta modificar esos archivos, puede haber conflictos de permisos de lectura/escritura en el sistema de archivos del host.

---

## Comandos usados

```bash
# 1. Comprobar versiones y estados de runtimes
podman --version
podman info --format "{{.Host.Security.Rootless}}"
docker version
docker info

# 2. Comprobar proveedor de Podman Compose
podman compose version

# 3. Iniciar el socket de usuario para Podman (rootless)
systemctl --user start podman.socket
systemctl --user status podman.socket

# 4. Levantar y medir con Podman Compose
time podman compose -f compose.podman.yaml up -d db minio

# 5. Comprobar aislamiento de usuarios en Podman (User Namespaces)
podman top sigesa-postgres user huser pid args
podman top sigesa-minio user huser pid args

# 6. Probar endpoints con Podman
curl -I http://localhost:9000/minio/health/live
curl -I http://localhost:9001/
podman exec sigesa-postgres pg_isready -U sigesa_user -d sigesa

# 7. Detener Podman para evitar colisión de puertos
podman compose -f compose.podman.yaml down

# 8. Levantar y medir con Docker Engine
time docker compose up -d

# 9. Comprobar UIDs en el host con Docker Engine (sin aislamiento rootless)
docker top sigesa-backend
docker top sigesa-minio
docker top sigesa-postgres

# 10. Probar endpoints con Docker Engine
curl -I http://localhost:3000
curl -I http://localhost:9000/minio/health/live
curl -I http://localhost:8080/v3/api-docs
```
