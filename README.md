# SIGESA — Acreditación UMSS

## Credenciales de acceso (desarrollo local)

Al arrancar el backend en modo desarrollo (H2 en memoria), se cargan automáticamente usuarios y datos de prueba. Usa estas credenciales para iniciar sesión en **http://localhost:5173**:

| Rol | Descripción | Email | Contraseña |
| --- | ----------- | ----- | ---------- |
| **JD** | Jefe de Departamento | `jd@umss.edu.bo` | `JefeDemo2026!` |
| **TD** | Técnico DUEA | `td@umss.edu.bo` | `TecnicoDemo2026!` |
| **CC** | Coordinador — Ing. Sistemas | `cc@umss.edu.bo` | `CoordDemo2026!` |
| **CC** | Coordinador — CEUB | `cc2@umss.edu.bo` | `Coord2Demo2026!` |
| **CC** | Usuario inactivo (pruebas admin) | `pendiente@umss.edu.bo` | `PendienteDemo2026!` |

> Solo cuentas con estado **ACTIVE** pueden autenticarse. `pendiente@umss.edu.bo` sirve para probar activación desde el panel de administración.

### Datos de prueba precargados

Además de los usuarios, el backend inserta registros de demostración para todos los modelos persistidos:

**Programas** (catálogo estático):

| ID | Código | Nombre |
| --- | ------ | ------ |
| `550e8400-e29b-41d4-a716-446655440000` | INF-SIS | Ingeniería de Sistemas (demo UMSS) |
| `660e8400-e29b-41d4-a716-446655440001` | CEUB | Coordinación CEUB (demo) |
| `770e8400-e29b-41d4-a716-446655440002` | ARCU-SUR | Coordinación ARCU-SUR (demo) |

**Plantillas** (`template`):

| ID | Validada | Taxonomía |
| --- | -------- | --------- |
| `850e8400-e29b-41d4-a716-446655440010` | Sí | CEUB-2026.1 |
| `850e8400-e29b-41d4-a716-446655440011` | Sí | ARCU-SUR-2026.1 |
| `850e8400-e29b-41d4-a716-446655440012` | No | DRAFT-0.1 |

**Procesos de acreditación** (`accreditation_process`):

| ID | Carrera | Periodo | Tipo | Estado |
| --- | ------- | ------- | ---- | ------ |
| `950e8400-e29b-41d4-a716-446655440020` | INF-SIS | 2026-1 | CEUB | ACTIVE |
| `950e8400-e29b-41d4-a716-446655440021` | CEUB | 2025-2 | CEUB | CLOSED |
| `950e8400-e29b-41d4-a716-446655440022` | ARCU-SUR | 2025-2 | ARCU_SUR | ARCHIVED |

**Asignaciones usuario–programa** (`user_program_assignment`):

| Usuario | Programa asignado |
| ------- | ----------------- |
| `cc@umss.edu.bo` | INF-SIS |
| `cc2@umss.edu.bo` | CEUB |
| `pendiente@umss.edu.bo` | ARCU-SUR |

Los identificadores y contraseñas están definidos en `backend/src/main/java/com/umss/sigesa/config/AuthDataLoader.java` y `DevSeedData.java`.

---

Sistema de gestión de acreditación institucional de la UMSS. El repositorio es un monorepo con dos aplicaciones independientes:

| Carpeta     | Stack                                      | Puerto por defecto |
| ----------- | ------------------------------------------ | ------------------ |
| `backend/`  | Java 21, Spring Boot 4.x, Maven            | `8080`             |
| `frontend/` | React 19, TypeScript, Vite, pnpm           | `5173`             |

---

## Requisitos previos

| Herramienta | Versión mínima | Notas |
| ----------- | -------------- | ----- |
| **JDK**     | 21             | Requerido para el backend |
| **Node.js** | 20.19+ o 22.12+ | Requerido para el frontend (Vite 8) |
| **pnpm**    | 9+             | Gestor de paquetes del frontend |

> El backend incluye **Maven Wrapper** (`mvnw` / `mvnw.cmd`), por lo que no es obligatorio instalar Maven de forma global.

### Instalación de dependencias del sistema

#### Linux

```bash
# Ejemplo en distribuciones basadas en Debian/Ubuntu
sudo apt update
sudo apt install openjdk-21-jdk

# Node.js (recomendado: nvm o fnm)
curl -fsSL https://fnm.vercel.app/install | bash
fnm install 22
fnm use 22

# pnpm
corepack enable
corepack prepare pnpm@latest --activate
```

#### Windows

```powershell
# JDK 21 — descargar e instalar desde:
# https://adoptium.net/temurin/releases/?version=21

# Node.js LTS — descargar desde:
# https://nodejs.org/

# pnpm (PowerShell, tras instalar Node)
corepack enable
corepack prepare pnpm@latest --activate
```

---

## Arranque en desarrollo local

El flujo recomendado es levantar **primero el backend** y luego el frontend. El servidor de desarrollo de Vite redirige las peticiones `/api` hacia `http://localhost:8080`.

### 🐳 Despliegue Local con Docker

El proyecto está completamente dockerizado utilizando construcciones multi-etapa (multi-stage builds) para generar entornos ligeros y reproducibles. 

#### 📋 Prerrequisitos

- [Docker Engine](https://docs.docker.com/engine/install/) ejecutándose.
- [Docker Compose](https://docs.docker.com/compose/install/) (V2 recomendado).

#### 🚀 Arranque Rápido

1. Posiciónate en la raíz del proyecto (donde se encuentra el `docker-compose.yml`).
2. Construye las imágenes y levanta los contenedores en segundo plano ejecutando:

   ```bash
   docker-compose up -d --build
   ```

> Nota: Dependiendo de tu versión, el comando podría ser docker compose up -d --build).

#### 🌐 Accesos y Puertos

Una vez que los contenedores estén corriendo (Started), los servicios estarán disponibles en las siguientes direcciones:

💻 Frontend (UI - React/Vite): `http://localhost:3000`

⚙️ Backend (API): `http://localhost:8080`

📚 Swagger Docs (OpenAPI): `http://localhost:8080/swagger-ui/index.html`

🗄️ PostgreSQL: localhost:5432

Base de datos: sigesa

Usuario: sigesa_user
Contraseña: sigesa_password

🛠️ Comandos Útiles de Mantenimiento
Ver los logs en tiempo real (todos los servicios):

```Bash
docker-compose logs -f
Ver los logs de un servicio específico (ej. backend):
```

```Bash
docker-compose logs -f backend
Detener los contenedores (sin borrar la base de datos):
```

```Bash
docker-compose down
Reiniciar todo desde cero (⚠️ ESTO BORRARÁ LA BASE DE DATOS LOCAL):
```

```Bash
docker-compose down -v
```

#### ⚠️ Solución de Problemas Frecuentes (Troubleshooting)

`Error: connect: permission denied (Linux)`: Tu usuario no tiene permisos sobre el socket de Docker. Ejecuta `sudo usermod -aG docker $USER`, luego reinicia tu terminal o ejecuta `newgrp docker`.

`Warning: attribute "version" is obsolete`: Es una advertencia inofensiva de Docker Compose V2. Para quitarla, elimina la línea version: '3.8' al inicio del archivo docker-compose.yml.

El Backend falla al conectar a la BD al iniciar: A veces el backend levanta milisegundos antes que PostgreSQL acepte conexiones. El contenedor del backend se reiniciará automáticamente (restart: always) e intentará reconectar exitosamente.

### 1. Backend

Por defecto el backend usa una base de datos **H2 en memoria** y carga automáticamente usuarios, plantillas y procesos de prueba al iniciar (ver sección [Credenciales de acceso](#credenciales-de-acceso-desarrollo-local)).

#### Linux / macOS

```bash
cd backend

# Dar permisos de ejecución al wrapper (solo la primera vez)
chmod +x mvnw

# Arrancar la API
./mvnw spring-boot:run
```

#### Windows (CMD o PowerShell)

```powershell
cd backend
mvnw.cmd spring-boot:run
```

#### Perfil `dev` (consola H2 habilitada)

```bash
# Linux / macOS
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Windows
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

**URLs del backend:**

| Recurso        | URL |
| -------------- | --- |
| API REST       | http://localhost:8080/api/v1/... |
| OpenAPI (JSON) | http://localhost:8080/v3/api-docs |
| Swagger UI     | http://localhost:8080/swagger-ui.html |
| Consola H2     | http://localhost:8080/h2-console *(solo con perfil `dev`)* |

Ver [Credenciales de acceso](#credenciales-de-acceso-desarrollo-local) al inicio del documento.

---

### 2. Frontend

#### Linux / macOS

```bash
cd frontend
pnpm install
pnpm dev
```

#### Windows (CMD o PowerShell)

```powershell
cd frontend
pnpm install
pnpm dev
```

Abrir en el navegador: **http://localhost:5173**

---

## Comandos útiles

### Backend

```bash
# Linux / macOS
cd backend
./mvnw test                  # Ejecutar pruebas
./mvnw verify                # Pruebas + reporte JaCoCo
./mvnw package -DskipTests   # Generar JAR

# Windows — sustituir ./mvnw por mvnw.cmd
mvnw.cmd test
mvnw.cmd verify
mvnw.cmd package -DskipTests
```

### Frontend

```bash
cd frontend
pnpm lint              # OxLint
pnpm build             # Lint + TypeScript + build de producción
pnpm preview           # Previsualizar build de producción
pnpm generate:api      # Regenerar cliente Orval (requiere backend en :8080)
```

---

## Perfil de producción (backend)

Para entornos productivos el backend usa **PostgreSQL** y **Flyway** para migraciones. Variables de entorno relevantes:

| Variable | Descripción | Ejemplo |
| -------- | ----------- | ------- |
| `DATABASE_URL` | JDBC de PostgreSQL | `jdbc:postgresql://localhost:5432/sigesa` |
| `DATABASE_USERNAME` | Usuario de BD | `sigesa` |
| `DATABASE_PASSWORD` | Contraseña de BD | *(secreto)* |
| `SIGESA_JWT_SECRET` | Clave HMAC para JWT (≥ 256 bits) | *(secreto)* |

```bash
# Linux / macOS
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod

# Windows
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=prod
```

---

## Estructura del repositorio

```
sigesa-acreditacion-umss/
├── backend/          # API REST (Spring Boot)
├── frontend/         # SPA (React + Vite)
├── docs/             # Documentación de producto, diseño y baseline
└── agents.md         # Contexto técnico para agentes de IA
```

## Panel de Control Híbrido (PBAC Dashboard)

El frontend de SIGESA incluye un panel de control híbrido basado en permisos (PBAC) que adapta la interfaz de usuario dinámicamente según las autorizaciones del usuario (`READ_CC_DASHBOARD`, `READ_TD_DASHBOARD`, `READ_JD_DASHBOARD`):
- **Coordinador de Carrera [CC]**: Acceso a indicadores de avance del programa académico asignado, progreso de fases, alertas de cuellos de botella y tabla de observaciones pendientes con paginación y ordenamiento.
- **Técnico DUEA [TD]**: Visualización de evidencias pendientes de revisión y últimas evaluaciones realizadas.
- **Jefatura DUEA [JD]**: Panel ejecutivo con semáforo de calidad de programas y KPIs agregados institucionales.

### Simulación de Roles (Entorno Local)
En entorno de desarrollo (`DEV`), se despliega una barra de herramientas de simulación de rol en la cabecera del dashboard. Esta herramienta está protegida por compilación condicional (`import.meta.env.DEV`) para evitar fugas en producción.

### Exportación de Reportes
Soporta exportaciones binarias robustas en formato Excel (`.xlsx`), PDF (`.pdf`), y CSV (`.csv`):
- Los clientes HTTP detectan automáticamente respuestas binarias para prevenir la corrupción de datos.
- En entorno de desarrollo con rol simulado, las descargas binarias se interceptan con un aviso y se descargan en formato CSV para inspección.

---

## Solución de problemas

| Problema | Posible solución |
| -------- | ---------------- |
| `Permission denied` al ejecutar `./mvnw` | Ejecutar `chmod +x mvnw` dentro de `backend/` |
| El frontend no conecta con la API | Verificar que el backend esté corriendo en el puerto `8080` |
| `pnpm: command not found` | Instalar pnpm con `corepack enable` o `npm install -g pnpm` |
| Error de versión de Java | Confirmar `java -version` muestra JDK 21 |
| `pnpm generate:api` falla | Arrancar el backend antes; Orval lee `http://localhost:8080/v3/api-docs` |
