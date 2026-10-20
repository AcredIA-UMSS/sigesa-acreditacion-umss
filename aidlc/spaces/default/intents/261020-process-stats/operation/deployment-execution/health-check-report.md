# Informe de salud — resumen de procesos

Fecha: 2026-10-20. El servicio `backend` de `docker-compose.yml` no define `healthcheck`. La salud se leyó del estado del contenedor y de la prueba de humo.

## Contenedores

| Contenedor | Estado |
| --- | --- |
| `sigesa-backend` | running |
| `sigesa-postgres` | running |
| `sigesa-minio` | running |
| `sigesa-ollama` | running |
| `open-webui` | running |

## Señal del backend

El proceso escucha en el puerto 8080. Sin token, el resumen responde 401. Con un `JD` de demostración, responde 200 y el JSON de conteos. No hay sonda de Docker que marque el contenedor como healthy.

## Resultado

El backend desplegado responde. No hay umbral automático de error ni de latencia en este Compose.
