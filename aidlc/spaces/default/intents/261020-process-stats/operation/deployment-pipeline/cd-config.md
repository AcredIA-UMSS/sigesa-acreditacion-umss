# Configuración de entrega — resumen de procesos

No hay workflow de GitHub Actions ni pipeline de promoción para el backend. El único artefacto desplegable es la imagen que arma `backend/Dockerfile` y levanta el servicio `backend` de `docker-compose.yml`. Este cambio no modifica ese Dockerfile ni el compose.

## Construcción

Desde la raíz del repositorio, con Docker disponible y el `.env` que ya usa el compose:

```bash
docker compose build backend
docker compose up -d backend
```

La imagen usa Java 21. El contenedor `sigesa-backend` publica el puerto 8080 y arranca con `SPRING_PROFILES_ACTIVE=dev` contra el Postgres del servicio `db`. Las credenciales salen de `.env` y de las variables ya declaradas en el compose. No se copian secretos a este documento.

## Qué no se añade

No hay registro de imágenes, etiqueta inmutable ni entorno de staging o producción en el repositorio. `frontend/.github/workflows/copilot-setup-steps.yml` no construye ni despliega el backend. No se crea un workflow nuevo.

## Comprobación

Sin token, el resumen debe responder 401:

```bash
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/api/v1/processes/stats
```

El código esperado es `401`.
